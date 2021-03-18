package com.wkl.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wkl.common.utils.PageUtils;
import com.wkl.gulimall.order.entity.OrderReturnReasonEntity;

import java.util.Map;

/**
 * 退货原因
 *
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-03 16:13:10
 */
public interface OrderReturnReasonService extends IService<OrderReturnReasonEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

