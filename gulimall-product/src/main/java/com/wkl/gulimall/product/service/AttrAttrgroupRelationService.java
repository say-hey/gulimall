package com.wkl.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wkl.common.utils.PageUtils;
import com.wkl.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.wkl.gulimall.product.vo.AttrGroupRelationVo;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-02 16:26:13
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    //只删除符合的关联关系，在关联表中删，一条语句实现批量删除
    void deleteBatchRelation(List<AttrAttrgroupRelationEntity> entities);

    //保存属性分组关联关系
    void saveBachRelation(List<AttrGroupRelationVo> vos);
}

