package com.wkl.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wkl.common.utils.PageUtils;
import com.wkl.gulimall.member.entity.UmsMemberReceiveAddressEntity;

import java.util.Map;

/**
 * 会员收货地址
 *
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-03 15:57:12
 */
public interface UmsMemberReceiveAddressService extends IService<UmsMemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

