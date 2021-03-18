package com.wkl.gulimall.product.dao;

import com.wkl.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-02 16:26:13
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    //只删除符合的关联关系，在关联表中删，一条语句实现批量删除
    void deleteBatchRelation(@Param("entities") List<AttrAttrgroupRelationEntity> entities);
}
