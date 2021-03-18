package com.wkl.gulimall.product.web;

import com.wkl.gulimall.product.entity.CategoryEntity;
import com.wkl.gulimall.product.service.CategoryService;
import com.wkl.gulimall.product.vo.Catelog2Vo;
import com.wkl.gulimall.product.vo.SearchParam;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redisson;

    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * 测试Lock锁
     * @return
     */
    @GetMapping("/hello")
    @ResponseBody
    public String hello(){
        //1.获取一把锁，只要名字一样，就是同一把锁
        RLock lock = redisson.getLock("my-lock");
        //2.加锁和解锁
        //阻塞时等待，默认加锁30s
        //lock.lock();
        //  1、锁的自动续期，如果业务超长，运行期间自动续期30s，不用担心锁过期
        //  2、加锁的业务只要完成，就不会自动续期，即使不手动解锁，默认30自动解锁
        //3.指定过期时间
        //  1、加锁以后10秒钟自动解锁
        //  2、无需调用unlock方法手动解锁
        //问题：lock.Lock（10，TimeUnit.SECONDS）；在锁时间到了以后，不会自动续期。
        //  1、如果我们传递了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时就是我们指定的时间
        //  2、如果我们未指定锁的超时时间，就使用30*1000【LockwatchdogTimeout看门狗的默认时间】；
        //  只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的过期时间就是看门狗的默认时间】，每隔10s都会自动
        //  internalLockLeaseTime【看门狗时间】/3，10s

        //推荐使用，保证业务小于过期时间，节省续期操作
        lock.lock(10, TimeUnit.SECONDS);
        try {
            System.out.println("加锁成功，执行业务方法..."+Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (Exception e){

        }finally {
            lock.unlock();
            System.out.println("释放锁..."+Thread.currentThread().getId());
        }
        return "hello";
    }

    /**
     * 保证一定能读到最新数据，修改期间，写锁是一个排他锁（互斥锁、独享），读锁是一个共享锁，写锁没释放读就必须等待
     * 读+读：相当于无锁，并发读，只会在redis中记录好，所有当前的读锁。他们都会同时加锁成功
     * 写+读：等待写锁释放
     * 写+写：阻塞方式
     * 读+写：有读锁。写也需要等待。
     * 只要有写的存在，都必须等待写锁
     *
     */
    @GetMapping("/write")
    @ResponseBody
    public String writeValue() {
        //设置写锁
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = lock.writeLock();
        //上锁
        rLock.lock();
        String s = "";
        try {
            s = UUID.randomUUID().toString();
            Thread.sleep(3000);
            redisTemplate.opsForValue().set("writeValue", s);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //解锁
            rLock.unlock();
        }
        return s;
    }

    /**
     * 读锁
     * @return
     */
    @GetMapping("/read")
    @ResponseBody
    public String readValue() {
        //设置读锁
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = lock.readLock();
        String s = "";
        //上锁
        rLock.lock();
        try {
            s = redisTemplate.opsForValue().get("writeValue");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //解锁
            rLock.unlock();
        }
        return s;
    }

    /**
     * 停车 [信号量]
     * 信号量:也可以用作限流
     */
    @ResponseBody
    @GetMapping("/park")
    public String park() throws InterruptedException {

        RSemaphore park = redisson.getSemaphore("park");
        //park.acquire();//获取一个信号量，占一个车位
        boolean acquire = park.tryAcquire();//尝试获取，没有就算了，不会阻塞
        if (acquire) {
            //业务
        }else{
            //error限流
        }
        return "停车 =>" + acquire;
    }
    /**
     * 获取车位
     */
    @ResponseBody
    @GetMapping("/go")
    public String goPark() {

        RSemaphore park = redisson.getSemaphore("park");
        park.release();//释放一个信号量，多一个车位
        return "取车 => 车位+1";
    }

    /**
     * 闭锁 只有设定的人全通过才关门
     */
    @ResponseBody
    @GetMapping("/door")
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        // 设置这里有5个人
        door.trySetCount(5);
        // 等待所有人走完
        door.await();

        return "放假锁门...";
    }
    /**
     * 闭锁 操作计数
     */
    @ResponseBody
    @GetMapping("/gogogo/{id}")
    public String go(@PathVariable("id") Long id) throws InterruptedException {

        RCountDownLatch door = redisson.getCountDownLatch("door");
        // 每访问一次相当于出去一个人
        door.countDown();
        return id + "班走完了";
    }


    /**
     * 首页跳转和一级菜单分类
     * @param model
     * @return
     */
    @RequestMapping({"/", "index", "/index.html"})
    public String indexPage(Model model) {
        // 获取一级分类所有缓存
        List<CategoryEntity> categorys = categoryService.getLevel1Categorys();
        model.addAttribute("categorys", categorys);

        //ThymeleafProperties
        return "index";
    }

    /**
     * 获取二级三级菜单
     * @return
     */
    @ResponseBody
    @RequestMapping("index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatlogJson() {

        Map<String, List<Catelog2Vo>> map = categoryService.getCatelogJson();
        return map;
    }


}
