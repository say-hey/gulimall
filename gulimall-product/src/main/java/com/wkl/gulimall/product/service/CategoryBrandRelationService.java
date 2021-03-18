package com.wkl.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wkl.common.utils.PageUtils;
import com.wkl.gulimall.product.entity.BrandEntity;
import com.wkl.gulimall.product.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-02 16:26:13
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    //新增品牌和分类关系，新增saveDeltil方法，需要保存两个冗余字段
    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    //更新品牌-分类关系表的品牌，级联更新冗余字段
    void updateBrand(Long brandId, String name);

    //级联更新品牌-分类表的分类，同步冗余字段
    void updateCascade(Long catId, String name);

    //查询分类关联所有品牌
    List<BrandEntity> getBrandsByCatId(Long catId);
}

