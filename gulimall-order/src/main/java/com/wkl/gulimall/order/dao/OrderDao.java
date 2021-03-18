package com.wkl.gulimall.order.dao;

import com.wkl.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-03 16:13:10
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
