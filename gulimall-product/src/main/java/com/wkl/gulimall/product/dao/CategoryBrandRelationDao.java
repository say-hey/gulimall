package com.wkl.gulimall.product.dao;

import com.wkl.gulimall.product.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 品牌分类关联
 * 
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-02 16:26:13
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {

    //级联更新category，创建一个Dao接口，然后编写sql语句，给参数自定义名字
    void updateCategory(@Param("catId") Long catId,@Param("name") String name);
}
