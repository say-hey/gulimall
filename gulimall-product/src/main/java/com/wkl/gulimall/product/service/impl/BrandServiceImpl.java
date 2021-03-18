package com.wkl.gulimall.product.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkl.common.utils.PageUtils;
import com.wkl.common.utils.Query;

import com.wkl.gulimall.product.dao.BrandDao;
import com.wkl.gulimall.product.entity.BrandEntity;
import com.wkl.gulimall.product.service.BrandService;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    CategoryBrandRelationServiceImpl categoryBrandRelationService;
    /**
     * 查询，增加模糊查询功能
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        //取出key，key可以查name和id
        String key = (String) params.get("key");
        //查询条件
        QueryWrapper<BrandEntity> wrapper = new QueryWrapper<>();
        //不为空，添加条件
        if(!StringUtils.isEmpty(key)){
            //eq是查找这一列的值
            wrapper.eq("brand_id", key).or().like("name", key);
        }

        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 更新品牌-分类关系表，级联更新冗余字段
     * @param brand
     */
    @Override
    public void updateDetailById(BrandEntity brand) {
        //保证冗余字段数据一致
        //1.正常更新
        this.updateById(brand);
        //2.判断更新内容是否更新了品牌名，如果有就更新所有用到品牌名的表
        if(!StringUtils.isEmpty(brand.getName())){
            //3.同步更新其他表中数据
            //更新pms_category_brand_relation表中所有指定id的name
            categoryBrandRelationService.updateBrand(brand.getBrandId(), brand.getName());

            //TODO 更新其他关联
        }
    }

    @Override
    public List<BrandEntity> getBrandByIds(List<Long> brandIds) {
        return baseMapper.selectList(new QueryWrapper<BrandEntity>().in("brand_id",brandIds));
    }

}