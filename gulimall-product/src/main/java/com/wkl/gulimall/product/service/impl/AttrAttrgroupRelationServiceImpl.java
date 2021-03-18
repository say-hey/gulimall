package com.wkl.gulimall.product.service.impl;

import com.wkl.gulimall.product.vo.AttrGroupRelationVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkl.common.utils.PageUtils;
import com.wkl.common.utils.Query;

import com.wkl.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.wkl.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.wkl.gulimall.product.service.AttrAttrgroupRelationService;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 只删除符合的关联关系，在关联表中删，一条语句实现批量删除
     * @param entities
     */
    @Override
    public void deleteBatchRelation(List<AttrAttrgroupRelationEntity> entities) {
        this.baseMapper.deleteBatchRelation(entities);
    }

    /**
     * 保存属性分组关联关系
     * @param vos
     */
    @Override
    public void saveBachRelation(List<AttrGroupRelationVo> vos) {
        List<AttrAttrgroupRelationEntity> collect = vos.stream().map((vo) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(vo, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        //已经存在这个方法，但是接收参数是entity，需要vo转entity
        this.saveBatch(collect);
    }

}