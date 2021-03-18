package com.wkl.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wkl.gulimall.product.entity.BrandEntity;
import com.wkl.gulimall.product.entity.CategoryEntity;
import com.wkl.gulimall.product.service.BrandService;
import com.wkl.gulimall.product.service.CategoryService;
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

import com.wkl.gulimall.product.dao.CategoryBrandRelationDao;
import com.wkl.gulimall.product.entity.CategoryBrandRelationEntity;
import com.wkl.gulimall.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    CategoryService categoryService;
    @Autowired
    BrandService brandService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 新增品牌和分类关系，新增saveDeltil方法，需要保存两个冗余字段
     * 利用品牌id、分类id找出品牌name、分类name，然后保存
     * @param categoryBrandRelation
     */
    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        //利用品牌id、分类id找出品牌name、分类name，然后保存
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        //查对象
        BrandEntity brand = brandService.getById(brandId);
        CategoryEntity category = categoryService.getById(catelogId);
        //添加属性
        categoryBrandRelation.setBrandName(brand.getName());
        categoryBrandRelation.setCatelogName(category.getName());
        //保存，this就是当前service，也可以用baseMapper
        this.save(categoryBrandRelation);
    }

    /**
     * 更新品牌-分类关系表的品牌，级联更新冗余字段
     * @param brandId
     * @param name
     */
    @Override
    public void updateBrand(Long brandId, String name) {
        //1.准备更新数据
        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        categoryBrandRelationEntity.setBrandId(brandId);
        categoryBrandRelationEntity.setBrandName(name);
        //2.更新语句，只更新携带的对象中的字段
        this.update(categoryBrandRelationEntity
                , new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
    }

    /**
     * 级联更新品牌-分类表的分类，同步冗余字段
     * @param catId
     * @param name
     */
    @Override
    public void updateCascade(Long catId, String name) {
        //调用自己的Dao，创建一个Dao接口，然后编写sql语句
        this.baseMapper.updateCategory(catId, name);
    }

    /**
     * 查询分类关联所有品牌
     * 关联表用catId查brandId，再去品牌表用brandId查brandName
     * @param catId
     * @return
     */
    @Override
    public List<BrandEntity> getBrandsByCatId(Long catId) {
        //1.关联表用catId查brandId
        List<CategoryBrandRelationEntity> catelogIds = this.list(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
        List<BrandEntity> collect = catelogIds.stream().map((catelogId) -> {
            //2.再去品牌表用brandId查brandName
            Long brandId = catelogId.getBrandId();
            return brandService.getById(brandId);
        }).collect(Collectors.toList());
        return collect;
    }


}