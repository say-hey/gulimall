package com.wkl.gulimall.product.service.impl;


import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wkl.common.constant.ProductConstant;
import com.wkl.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.wkl.gulimall.product.entity.AttrGroupEntity;
import com.wkl.gulimall.product.entity.CategoryEntity;
import com.wkl.gulimall.product.service.AttrAttrgroupRelationService;
import com.wkl.gulimall.product.service.AttrGroupService;
import com.wkl.gulimall.product.service.CategoryService;
import com.wkl.gulimall.product.vo.AttrGroupRelationVo;
import com.wkl.gulimall.product.vo.AttrRespVo;
import com.wkl.gulimall.product.vo.AttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkl.common.utils.PageUtils;
import com.wkl.common.utils.Query;

import com.wkl.gulimall.product.dao.AttrDao;
import com.wkl.gulimall.product.entity.AttrEntity;
import com.wkl.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    //关联表
    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;
    //分组
    @Autowired
    AttrGroupService attrGroupService;
    //分类
    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 规格参数保存请求，使用AttrVO做参数，添加事务注解,规格参数，销售属性共用，attrType来区分
     * @param attr
     */
    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
        //1.先保存AttrEntity自己的参数，new一个对象，把属性复制过去
        AttrEntity attrEntity = new AttrEntity();
        //Spring提供工具
        BeanUtils.copyProperties(attr, attrEntity);
        //保存
        this.save(attrEntity);

        //：规格参数需要保存关联，销售属性不需要关联关系
        if(attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId()!=null){
            //2.保存关联关系，具体保存哪几个参数可以查看数据库
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            //保存属性id，使用attrEntity的，因为attrEntity保存之后才有自增id
            relationEntity.setAttrId(attrEntity.getAttrId());
            //保存属性分组id
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationService.save(relationEntity);
        }


    }

    /**
     * 树形菜单指定id和输入框模糊查询，树形菜单指定id和输入框模糊查询，规格参数，销售属性共用，type来区分
     * @param params
     * @param catelogId
     * @param type
     * @return
     */
    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type) {
        //0.开始时需要判断是哪个请求？规格参数，销售属性？给sql添加条件再判断
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>()
                .eq("attr_type", "base".equalsIgnoreCase(type)?ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode():ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());

        //1.判断是否携带指定catelogId，等于0代表查所有，否则查指定
        if(catelogId != 0){
            queryWrapper.eq("catelog_id", catelogId);
        }
        //2.模糊查询
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            queryWrapper.and((wapper)->{
                wapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper
        );

        //0.使用page重新封装进AttrRespVo，注意这一步，下面需要重新封装
        PageUtils pageUtils = new PageUtils(page);
        //1.取出数据
        List<AttrEntity> records = page.getRecords();
        //2.流式编程，一个一个处理
        List<AttrRespVo> attrRespVos = records.stream().map((attrEntity) -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            //3.复制基础数据到vo
            BeanUtils.copyProperties(attrEntity, attrRespVo);
            //4.设置分组attrGroup

            //4.1根据attrId从关联表中查出attrGroupId
            AttrAttrgroupRelationEntity attrId = attrAttrgroupRelationService
                    .getOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));

            //：此处销售属性是没有分组的，所以需要判断
            if("base".equalsIgnoreCase(type)){
                //4.1.5为了防止null数据
                if (attrId != null && attrId.getAttrGroupId() != null) {
                    //4.2有了attrGroupId，可以在分组表中查出分组名
                    AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrId);
                    //4.3取出分组名放到vo
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }


            //5.设置分类category
            //5.1关联表有catelog_id字段，可以直接在分类表中查
            CategoryEntity categoryEntity = categoryService.getById(attrEntity.getCatelogId());
            //5.1.5为了防止null数据
            if (categoryEntity != null) {
                //5.2取出分类名放到vo
                attrRespVo.setCatelogName(categoryEntity.getName());
            }

            return attrRespVo;
        }).collect(Collectors.toList());
        //6.重新封装，把最新的数据放到分页中
        pageUtils.setList(attrRespVos);
        return pageUtils;
    }

    /**
     * 信息，获取信息和完整路径，回显使用
     * @param attrId
     * @return
     */
    @Override
    public AttrRespVo getAttrInfoById(Long attrId) {
        //0.准备好返回对象
        AttrRespVo respVo = new AttrRespVo();
        //1.先查基本信息
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, respVo);

        //：规格参数才有分组信息
        if(attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){
            //2.分组id
            //2.1关联表中查分组id
            AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationService
                    .getOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if(relationEntity != null){
                respVo.setAttrGroupId(relationEntity.getAttrGroupId());
                //2.2分组名字从attrGroupService中根据刚刚的分组id查出来
                AttrGroupEntity attrGroupEntity = attrGroupService.getById(relationEntity.getAttrGroupId());
                if(attrGroupEntity != null){
                    respVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }

        //3.分类完整路径
        //3.1利用属性的分类id，找出分类，之前写过递归查找完整路径
        Long[] catelogPath = categoryService.findCatelogPath(attrEntity.getCatelogId());
        respVo.setCatelogPath(catelogPath);
        //3.3分类名字
        CategoryEntity categoryEntity = categoryService.getById(attrEntity.getCatelogId());
        if(categoryEntity != null){
            respVo.setCatelogName(categoryEntity.getName());
        }
        //4.返回VO
        return respVo;
    }

    /**
     * 修改，同步修改vo类中的其他属性
     * @param respVo
     */
    @Override
    public void updateAttrById(AttrRespVo respVo) {
        //1.基本数据修改，从vo中取出
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(respVo, attrEntity);
        this.updateById(attrEntity);
        //：规格参数才有分组信息
        if(attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){
            //2.修改分组关联表
            //2.0创建好指定对象
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(respVo.getAttrId());
            relationEntity.setAttrGroupId(respVo.getAttrGroupId());
            //2.1调用方法查看是否存在这个属性，存在则更新，不存在就新增
            int count = attrAttrgroupRelationService.count(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", relationEntity.getAttrId()));
            if(count > 0){
                //更新
                attrAttrgroupRelationService.update(relationEntity
                        , new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",relationEntity.getAttrId()));
            }else{
                //新增
                attrAttrgroupRelationService.save(relationEntity);
            }
        }

    }

    /**
     * 属性分组页面需要属性名和可选值，根据attrgroupId在attr中查到需要的关联信息
     * 在关联表中找到属性attrId，然后去attr中查询
     * @param attrgroupId
     * @return
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        //1.根据attrgroupId查询关联信息
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        //2.在关联信息中找到属性attrId
        List<Long> attrIds = relationEntities.stream().map((attr) -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());
        //3.利用attrIds查出所有属性，并返回
        //判断是否空，如果空，就不查，直接返回null
        if(CollectionUtils.isEmpty(attrIds)){
            return null;
        }
        List<AttrEntity> attrEntities = this.listByIds(attrIds);
        return attrEntities;
    }

    /**
     * 属性分组查关联删除，两个请求参数，[{"attrId":1,"attrGroupId":2}]，封装到VO
     * @param vos
     */
    @Override
    public void deleteRelation(AttrGroupRelationVo[] vos) {
        //0.只删除符合的关联关系，在关联表中删，一条语句实现批量删除
        //1.把请求的所有数组封装成关联表AttrAttrgroupRelationEntity
        List<AttrAttrgroupRelationEntity> entities = Arrays.asList(vos).stream().map((item) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        //2.创建删除方法，新建sql语句
        attrAttrgroupRelationService.deleteBatchRelation(entities);
    }

    /**
     * 查询属性分组尚未关联的属性
     * 注意调用方法时使用的是哪个类的service
     * @param params
     * @param attrgroupId
     * @return
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        //1.查出该分组的分类id
        AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();

        //2.查出所有分组
        List<AttrGroupEntity> catelogAttrGroupEntityList = attrGroupService.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> attrGroupList = catelogAttrGroupEntityList.stream().map((attrGroup) -> {
            return attrGroup.getAttrGroupId();
        }).collect(Collectors.toList());

        //3.查出所有分组关联属性
        List<AttrAttrgroupRelationEntity> attrGroupRelationEntities = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", attrGroupList));
        List<Long> attrRelationIdList = attrGroupRelationEntities.stream().map((relationEntity) -> {
            return relationEntity.getAttrId();
        }).collect(Collectors.toList());

        //4.在当前分类的所有属性中排除这些已有属性
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if(!CollectionUtils.isEmpty(attrRelationIdList)){
            wrapper.notIn("attr_id", attrRelationIdList);
        }
        //模糊查询
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and((w)->{
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        //IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
        IPage<AttrEntity> page = this.page(
                //分页
                new Query<AttrEntity>().getPage(params),
                //条件
                wrapper
        );
        return new PageUtils(page);
    }

    /**
     * 从指定ids中查出该id是否有可以检索pms_attr
     * @param attrIds
     * @return
     */
    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {
        //SELECT attr_id FROM `pms_attr` WHERE attr_id IN (?) AND search_type = 1
        return baseMapper.selectSearchAttrIds(attrIds);
    }


}