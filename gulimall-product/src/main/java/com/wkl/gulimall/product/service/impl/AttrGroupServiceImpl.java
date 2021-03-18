package com.wkl.gulimall.product.service.impl;

import com.wkl.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.wkl.gulimall.product.entity.AttrEntity;
import com.wkl.gulimall.product.service.AttrService;
import com.wkl.gulimall.product.vo.AttrGroupRelationVo;
import com.wkl.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.wkl.gulimall.product.vo.AttrRespVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkl.common.utils.PageUtils;
import com.wkl.common.utils.Query;

import com.wkl.gulimall.product.dao.AttrGroupDao;
import com.wkl.gulimall.product.entity.AttrGroupEntity;
import com.wkl.gulimall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 新建根据id查，模糊查询
     * @param params
     * @param catelogId
     */
    @Override
    public PageUtils queryPageById(Map<String, Object> params, Long catelogId) {
        //1.模糊查询
        //select * from pms_attr_group where catelog_id and (attr_group_id=key or attr_group_name like %key%)
        //模糊查询参数
        String key = (String)params.get("key");
        //查询条件，拼装id和模糊key，eq：在某列查找
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if(!StringUtils.isEmpty(key)){
            //关键字不为空，继续拼接key，模糊查询有两处，分组id和name
            wrapper.and((obj)->{
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }

        //2.如果没传id，默认为0，查所有
        if(catelogId != 0){
            wrapper.eq("catelog_id", catelogId);
        }
        //封装
        IPage<AttrGroupEntity> page = this.page(
                //分页
                new Query<AttrGroupEntity>().getPage(params),
                //条件
                wrapper
        );
        //封装
        return new PageUtils(page);
    }

    /**
     * 获取分类下所有分组&关联属性
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        //1.查出分组信息
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));


        //3.组装成vo，并返回
        List<AttrGroupWithAttrsVo> collect = attrGroupEntities.stream().map((attrGroupEntity) -> {
            AttrGroupWithAttrsVo vo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(attrGroupEntity, vo);
            //2.在属性表中查出所有数据

            List<AttrEntity> attr = attrService.getRelationAttr(vo.getAttrGroupId());
            if (attr != null){
                vo.setAttrs(attr);
            }
            return vo;
        }).collect(Collectors.toList());
        return collect;
    }

}