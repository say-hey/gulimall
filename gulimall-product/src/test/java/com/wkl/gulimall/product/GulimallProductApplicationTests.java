package com.wkl.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wkl.gulimall.product.entity.BrandEntity;
import com.wkl.gulimall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimallProductApplicationTests {

    //测试品牌服务的service
    @Autowired
    BrandService brandService;

    @Test
    void contextLoads() {
        //新增
        BrandEntity brandEntity = new BrandEntity();
        //设置品牌名
        brandEntity.setName("二仙桥");
        brandService.save(brandEntity);
        System.out.println("保存完成");
    }
    @Test
    void update() {
        //更新
        BrandEntity brandEntity = new BrandEntity();
        //要跟新对象的id
        brandEntity.setBrandId(1L);
        //更新的值
        brandEntity.setDescript("走成华大道");
        brandService.updateById(brandEntity);
        System.out.println("更新完成："+brandEntity);
    }
    @Test
    void query() {
        //简单查询
        BrandEntity byId = brandService.getById(1);
        System.out.println(byId);
        //在某列查找值
        //注意这个方法是 list()
        Object brand_id = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1));
        System.out.println(brand_id);
    }

}
