package com.wkl.gulimall.coupon.dao;

import com.wkl.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-03 15:28:59
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
