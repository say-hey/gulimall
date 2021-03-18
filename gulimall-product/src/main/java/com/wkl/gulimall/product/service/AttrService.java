package com.wkl.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wkl.common.utils.PageUtils;
import com.wkl.gulimall.product.entity.AttrEntity;
import com.wkl.gulimall.product.vo.AttrGroupRelationVo;
import com.wkl.gulimall.product.vo.AttrRespVo;
import com.wkl.gulimall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-02 16:26:13
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    //规格参数保存请求，使用AttrVO做参数
    void saveAttr(AttrVo attr);

    //树形菜单指定id和输入框模糊查询，树形菜单指定id和输入框模糊查询，规格参数，销售属性共用，type来区分
    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type);

    //信息，获取信息和完整路径，回显使用
    AttrRespVo getAttrInfoById(Long attrId);

    //修改，同步修改vo类中的其他属性
    void updateAttrById(AttrRespVo respVo);

    //页面需要属性名和可选值，根据attrgroupId在attr中查到需要的关联信息
    List<AttrEntity> getRelationAttr(Long attrgroupId);

    //属性分组查关联删除，两个请求参数，[{"attrId":1,"attrGroupId":2}]，封装到VO
    void deleteRelation(AttrGroupRelationVo[] vos);

    //查询属性分组尚未关联的属性
    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId);

    //从指定ids中查出该id是否有可以检索pms_attr
    List<Long> selectSearchAttrIds(List<Long> attrIds);
}

