package com.wkl.gulimall.ware.dao;

import com.wkl.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

/**
 * 商品库存
 * 
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-03 16:20:52
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    //更新库存，自己写sql语句
    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);
    //查询sku是否有库存，库存减去锁定库存
    Boolean getSkuStock(@Param("id") Long id);
}
