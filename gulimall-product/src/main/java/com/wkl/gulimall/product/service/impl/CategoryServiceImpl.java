package com.wkl.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wkl.gulimall.product.service.CategoryBrandRelationService;
import com.wkl.gulimall.product.vo.Catelog2Vo;
import com.wkl.gulimall.product.vo.Catelog3Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkl.common.utils.PageUtils;
import com.wkl.common.utils.Query;

import com.wkl.gulimall.product.dao.CategoryDao;
import com.wkl.gulimall.product.entity.CategoryEntity;
import com.wkl.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 以树形结构显示列表
     * @return
     */
    @Override
    public List<CategoryEntity> listWithTree() {
        //1.查出所有分类数据
        //使用条件查询方法，参数为null，查出所有数据
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2.组装成父子结构的菜单
        //2.1先找出所有的一级菜单，一级菜单的cat_level=1
        //使用stream流式编程，过滤出所有一级菜单，放到集合中(流式编程是交替执行的，执行一次filer再执行一次collect)
        //自己cat_level==1或者父等级parentCid==0都是一样的
        List<CategoryEntity> level1Menus = entities.stream()
                .filter(categoryEntity -> categoryEntity.getCatLevel() == 1)
                //再对过滤后的列表进行处理，将一级分类的子分类找出来,menu就是当菜单分类
                .map((menu)->{
                    //使用一个递归方法找出所有子分类的子分类，传递两个参数，一个是当前分类菜单，一个是所有分类
                    //这个方法的功能就是在所有分类中递归找出当前分类的子分类
                    menu.setChildren(getChildrens(menu, entities));
                    //处理完再返回，进行下一步处理
                    return menu;
                })
                //对查找完的所有菜单进行排序，推荐使用Comparator.comparingInt()排序
                //一级分类肯定不为空，所以可以这样写，但是子菜单分类递归时有可能为空，要先判空
                //.sorted(Comparator.comparingInt(item->item.getSort()==null?0:item.getSort()))
                .sorted(Comparator.comparingInt(CategoryEntity::getSort))
                .collect(Collectors.toList());

        return level1Menus;
    }

    /**
     * 自定义批量删除方法
     * 要先判断改当前菜单是否被引用，但是无法得知，所以先空着
     * @param asList
     */
    @Override
    public void removeMenuByIds(List<Long> asList) {
        //使用TODO添加标记，在左下角窗口查看
        //TODO 1.先判断是否被引用，但是无法得知，所以先空着
        //2.批量删除
        baseMapper.deleteBatchIds(asList);
    }

    /**
     * 根据id查完整路径
     * [父]-[子]-[孙]
     * @param catelogId
     * @return
     */
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        //反转列表
        Collections.reverse(parentPath);
        //返回完整路径
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新品牌-分类表，同步冗余字段，Spring Cache使用失效模式，更新后直接删除缓存
     * 1、指定多个注解@Caching
     * 2、根据分区名删@CacheEvict(value = "category", allEntries = true)
     * 3、存储同一类型，指定同一分区，分区名默认前缀
     * @param category
     */
    //删除两个缓存
//    @Caching(evict = {
//            @CacheEvict(value = "category", key = "'getLevel1Categorys'"),
//            @CacheEvict(value = "category", key = "'getCatelogJson'")
//    })
    //直接删除某分区
    @CacheEvict(value = "category", allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        //1.更新自己
        this.updateById(category);
        //2.更新关联表
        categoryBrandRelationService.updateCascade(category.getCatId(), category.getName());
    }

    /**
     * Spring Cache
     * 获取一级分类所有菜单
     * 1、@Cacheable: 当前方法的结果需要缓存 并指定缓存名字
     * 2、默认行为
     *      1、缓存有，不调用方法
     *      2、key默认生成cateroty::SimpleKey
     *      3、缓存的value的值，默认使用jdk序列化机制，将序列化后的数据存到redis
     *      4、默认ttl时间 -1秒
     * 3、自定义
     *      1、指定缓存key，手动设置值，或者使用spEL表达式
     *      2、指定存活时间spring.cache.redis.time-to-live=3600000 #这里指定存活时间为1小时
     * 4、加锁
     *      sync = true，缓存击穿，只让一个人查
     *      加过期时间，防止雪崩
     *
     * @return
     */
    //将一级分类放入缓存，指定缓存分区。每一个需要缓存的数据，我们都需要来指定要放到哪个名字的缓存中。通常按照业务类型进行划分。
    //@Cacheable(value = {"category"}, key = "'Level1Categorys'")
    @Cacheable(value = "category", key = "#root.methodName", sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        //自己是1级分类，或者父级是0级分类都可以
        List<CategoryEntity> entities = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return entities;
    }

    /**
     * 获取三级分类，spring cache
     * 直接查数据库，然后放到缓存，不用先判断再查询
     * @return
     */
    @Cacheable(value = "category", key = "#root.methodName", sync = true)
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {

        System.out.println("查数据库......");
        //优化，将多次查询数据库改为一次查询，查询List集合
        //将结果提前放入List，然后从List中取值
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);

        // 1、查询所有一级分类
        List<CategoryEntity> level1 = getParentCid(selectList, 0L);// 优化

        // 2、查询二级分类
        // 查询二级分类，封装成Map，key是id，value是二级分类
        Map<String, List<Catelog2Vo>> catelogLevel2Map = level1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 根据每一个一级分类，然后查询二级分类，条件是它们的父类是一级分类
            List<CategoryEntity> level2 = getParentCid(selectList, v.getCatId());// 优化
            // 获取二级分类后封装，并查询三级分类
            List<Catelog2Vo> catelog2Vos = null;
            if (level2 != null) {
                catelog2Vos = level2.stream().map(l2 -> {
                    // 封装二级分类，把三级属性空出来
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), l2.getName(), l2.getCatId().toString(), null);
                    // 找当前二级分类的三级分类
                    List<CategoryEntity> level3 = getParentCid(selectList, l2.getCatId());// 优化
                    // 三级分类有数据的情况下
                    if (level3 != null) {
                        // 封装三级菜单
                        List<Catelog3Vo> catalog3Vos = level3.stream().map(l3 -> new Catelog3Vo(l3.getCatId().toString(), l3.getName(), l2.getCatId().toString())).collect(Collectors.toList());
                        // 完成二级封装
                        catelog2Vo.setCatalog3List(catalog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            // 返回二级map的value
            return catelog2Vos;
        }));

        // 返回二级菜单
        return catelogLevel2Map;
    }

    /**
     * 获取三级分类
     *
     * TODO 堆外内存溢出：OutofDirectMemoryError redis的lettuce源码问题，目前没有问题
     *
     * 1、空结果缓存：解决缓存穿透
     * 2、设置过期时间（加随机值）：解决缓存雪崩
     * 3、加锁：解决缓存击穿
     *
     * 三级分类使用redis缓存
     * @return
     */
    //@Override
    public Map<String, List<Catelog2Vo>> getCatelogJsonOld() {
        //给缓存中放json字符串，所以拿出的json字符串，但是还需要逆转为能用的对象类型；【序列化与反序列化】
        //1、加入缓存逻辑，缓存中存的数据是json字符串。
        //JsoN跨语言，跨平台兼容。
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if(StringUtils.isEmpty(catalogJSON)) {
            //2、缓存中没有，查询数据库
            Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatelogJsonFromDbWithRedissonLock();
            //3、查到的数据再放入缓存，将对象转为json放在缓存中，这一步放在锁中，因为时序问题
        }
        System.out.println("查缓存...");
        //转为我们指定的对象。TypeReference需要匿名内部类
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>(){});
        return result;

    }


    /**
     * 获取三级分类Redisson
     * Redisson锁，分布式锁，多个服务共享同一缓存中的数据
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDbWithRedissonLock() {

        // 这里只要锁的名字一样那锁就是一样的
        // 关于锁的粒度 具体缓存的是某个数据 例如: 11-号商品 product-11-lock 和 product-lock的区别
        RLock lock = redissonClient.getLock("CatelogJson-lock");
        //该方法会阻塞其他线程向下执行，只有释放锁之后才会接着向下执行
        lock.lock();
        Map<String, List<Catelog2Vo>> data;
        try {
            //查数据库
            data = getCatelogJsonFromDb();
        } finally {
            lock.unlock();
        }
        return data;
    }
    /**
     * setnx分布式锁
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDbWithRedisLock() {

        // 1.占分布式锁  设置这个锁10秒自动删除 [原子操作]，每个服务使用自己的uuid锁
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS);

        if (lock) {
            // 2.设置过期时间加锁成功 获取数据释放锁 [分布式下必须是Lua脚本删锁,不然会因为业务处理时间、网络延迟等等引起数据还没返回锁过期或者返回的过程中过期 然后把别人的锁删了]
            Map<String, List<Catelog2Vo>> data;
            try {
                data = getCatelogJsonFromDb();
            } finally {
                //对比自己的锁，然后删除
			    //redisTemplate.delete("lock");
                String lockValue = redisTemplate.opsForValue().get("lock");
                // 删除也必须是原子操作 Lua脚本操作 删除成功返回1 否则返回0
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                // 原子删锁
                redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList("lock"), uuid);
            }
            return data;
        } else {
            // 加锁不成功，休眠重试
            try {
                // 等上两百毫秒
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatelogJsonFromDbWithRedisLock();
        }
    }
    /**
     * setnx分布式锁，使用while方式自旋，自己写的，对错不一定
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDbWithRedisLockWhile() {

        while(true){
            // 1.占分布式锁  设置这个锁10秒自动删除 [原子操作]，每个服务使用自己的uuid锁
            String uuid = UUID.randomUUID().toString();
            Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS);

            if (lock) {
                // 2.设置过期时间加锁成功 获取数据释放锁 [分布式下必须是Lua脚本删锁,不然会因为业务处理时间、网络延迟等等引起数据还没返回锁过期或者返回的过程中过期 然后把别人的锁删了]
                Map<String, List<Catelog2Vo>> data;
                try {
                    // 查数据库保存缓存，并返回
                    data = getCatelogJsonFromDb();
                } finally {
                    //对比自己的锁，然后删除
                    //redisTemplate.delete("lock");
                    String lockValue = redisTemplate.opsForValue().get("lock");
                    // 删除也必须是原子操作 Lua脚本操作 删除成功返回1 否则返回0
                    String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                    // 原子删锁
                    redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList("lock"), uuid);
                }
                return data;
            }
            try {
                // 等上两百毫秒
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 抽取方法，判断redis中是否有缓存，否则查数据库后存入redis
     * @return
     */
    private Map<String, List<Catelog2Vo>> getCatelogJsonFromDb() {
        //得到锁，去缓存中确定是否存在，然后再决定查数据库
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {

            //有数据，直接返回
            //转为我们指定的对象。TypeReference需要匿名内部类
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }

        System.out.println("查数据库......");
        //优化，将多次查询数据库改为一次查询，查询List集合
        //将结果提前放入List，然后从List中取值
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);

        // 1、查询所有一级分类
        List<CategoryEntity> level1 = getParentCid(selectList, 0L);// 优化

        // 2、查询二级分类
        // 查询二级分类，封装成Map，key是id，value是二级分类
        Map<String, List<Catelog2Vo>> catelogLevel2Map = level1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 根据每一个一级分类，然后查询二级分类，条件是它们的父类是一级分类
            List<CategoryEntity> level2 = getParentCid(selectList, v.getCatId());// 优化
            // 获取二级分类后封装，并查询三级分类
            List<Catelog2Vo> catelog2Vos = null;
            if (level2 != null) {
                catelog2Vos = level2.stream().map(l2 -> {
                    // 封装二级分类，把三级属性空出来
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), l2.getName(), l2.getCatId().toString(), null);
                    // 找当前二级分类的三级分类
                    List<CategoryEntity> level3 = getParentCid(selectList, l2.getCatId());// 优化
                    // 三级分类有数据的情况下
                    if (level3 != null) {
                        // 封装三级菜单
                        List<Catelog3Vo> catalog3Vos = level3.stream().map(l3 -> new Catelog3Vo(l3.getCatId().toString(), l3.getName(), l2.getCatId().toString())).collect(Collectors.toList());
                        // 完成二级封装
                        catelog2Vo.setCatalog3List(catalog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            // 返回二级map的value
            return catelog2Vos;
        }));

        //锁-时序问题
        //3、查到的数据再放入缓存，将对象转为json放在缓存中，
        String s = JSON.toJSONString(catelogLevel2Map);
        redisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);

        // 返回二级菜单
        return catelogLevel2Map;
    }

    /**
     * 本地锁
     * 查询数据库，不使用redis缓存
     * 获取二级三级菜单
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDbWithLocalLock() {

        //TODO 本地锁synchronized、JUC(Lock)
        //添加本地锁，springboot中所有组件都是单例的，使用this锁
        synchronized (this){

            //得到锁，去缓存中确定是否存在，然后再决定查数据库
            return getCatelogJsonFromDb();
        }


    }

    /**
     * 抽取方法，在List获取结果
     * @param
     * @return
     */
    private List<CategoryEntity> getParentCid(List<CategoryEntity> entityList, Long parentCid) {
        //直接从全部的结果中过滤，代替原始查数据库
        return entityList.stream().filter(item->item.getParentCid()==parentCid).collect(Collectors.toList());
    }

    /**
     * 根据id，向上递归查完整路径。但是结果是反着的[225]-[25]-[2]
     * @param catelogId
     * @param paths
     * @return
     */
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //1.存进入
        paths.add(catelogId);
        //2.根据id查完整的Bean
        CategoryEntity byId = this.getById(catelogId);
        //3.查父节点
        if(byId.getParentCid()!=0){
            //4.递归向上查
            findParentPath(byId.getParentCid(), paths);
        }
        //5.返回
        return paths;
    }

    /**
     * 在所有菜单中递归找出当菜单的子菜单
     * @param parent 当前菜单
     * @param all 所有菜单
     * @return 所有菜单的子菜单
     */
    public List<CategoryEntity> getChildrens(CategoryEntity parent, List<CategoryEntity> all){

        //对所有菜单进行处理，过滤条件就是如果发现有菜单的父级分类等于自己，就把它放到自己子菜单下、
        //并对它进行递归处理，如果它还有子菜单，就放到子菜单的子菜单下
        List<CategoryEntity> childern = all.stream()
                .filter(e -> e.getParentCid().equals(parent.getCatId()))
                //递归查找子菜单
                .map(categoryEntity->{
                    categoryEntity.setChildren(getChildrens(categoryEntity, all));
                    return categoryEntity;
                })
                //排序，递归时有可能为空，所以要判空
                .sorted(Comparator.comparingInt(item->item.getSort()==null?0:item.getSort()))
                .collect(Collectors.toList());
        return childern;
    }

}