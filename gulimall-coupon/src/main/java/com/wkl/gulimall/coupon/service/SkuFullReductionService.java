package com.wkl.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wkl.common.to.SkuReductionTo;
import com.wkl.common.utils.PageUtils;
import com.wkl.gulimall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-03 15:29:00
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    //保存满减、优惠信息
    void saveSkuRerduction(SkuReductionTo skuReductionTo);
}

