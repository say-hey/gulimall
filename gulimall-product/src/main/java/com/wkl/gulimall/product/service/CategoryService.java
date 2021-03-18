package com.wkl.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wkl.common.utils.PageUtils;
import com.wkl.gulimall.product.entity.CategoryEntity;
import com.wkl.gulimall.product.vo.Catelog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-02 16:26:13
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    //以树形结构显示列表
    List<CategoryEntity> listWithTree();

    //自定义批量删除方法
    void removeMenuByIds(List<Long> asList);

    //查询完整路径回显
    Long[] findCatelogPath(Long catelogId);

    //级联更新品牌-分类表，同步冗余字段
    void updateCascade(CategoryEntity category);

    //获取一级分类所有缓存
    List<CategoryEntity> getLevel1Categorys();

    //获取二级三级菜单
    Map<String, List<Catelog2Vo>> getCatelogJson();
}

