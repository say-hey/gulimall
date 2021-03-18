package com.wkl.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wkl.common.utils.PageUtils;
import com.wkl.gulimall.product.entity.SkuInfoEntity;

import java.util.List;
import java.util.Map;

/**
 * sku信息
 *
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-02 16:26:13
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    //添加sku检索
    PageUtils queryPageCondition(Map<String, Object> params);

    //查出当前spuId对应的所有sku信息，品牌信息等
    List<SkuInfoEntity> getSkusBySpuId(Long spuId);
}

