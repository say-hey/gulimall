package com.wkl.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wkl.common.utils.PageUtils;
import com.wkl.gulimall.member.entity.GrowthChangeHistoryEntity;

import java.util.Map;

/**
 * 成长值变化历史记录
 *
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-03 16:04:23
 */
public interface GrowthChangeHistoryService extends IService<GrowthChangeHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

