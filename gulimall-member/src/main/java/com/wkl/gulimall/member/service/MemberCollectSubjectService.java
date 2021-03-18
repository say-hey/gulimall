package com.wkl.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wkl.common.utils.PageUtils;
import com.wkl.gulimall.member.entity.MemberCollectSubjectEntity;

import java.util.Map;

/**
 * 会员收藏的专题活动
 *
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-03 16:04:23
 */
public interface MemberCollectSubjectService extends IService<MemberCollectSubjectEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

