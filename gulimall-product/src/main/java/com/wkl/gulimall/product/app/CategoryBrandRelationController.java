package com.wkl.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wkl.gulimall.product.entity.BrandEntity;
import com.wkl.gulimall.product.vo.BrandVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wkl.gulimall.product.entity.CategoryBrandRelationEntity;
import com.wkl.gulimall.product.service.CategoryBrandRelationService;
import com.wkl.common.utils.PageUtils;
import com.wkl.common.utils.R;


/**
 * 品牌分类关联
 *
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-02 16:26:13
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;


    /**
     * 查询分类关联所有品牌
     * 关联表用catId查brandId，再去品牌表用brandId查brandName
     *  1、Controller：处理请求，接受和校验数据
     *  2、Service接受controller传来的数据，进行业务处理
     *  3、Controller接受Service处理完的数据，封装页面指定的vo
     */
    @GetMapping(value = "/brands/list")
    public R brandsList(@RequestParam("catId") Long catId) {
        //响应只有两个参数，所以封装vo，但是为了方法通用性，此处返回BrandEntity
        List<BrandEntity> brandEntities = categoryBrandRelationService.getBrandsByCatId(catId);

        //封装成需要返回的vo类，由于属性名不同，不能copy
        List<BrandVo> collect = brandEntities.stream().map((brandEntity) -> {
            BrandVo brandVo = new BrandVo();
            brandVo.setBrandId(brandEntity.getBrandId());
            brandVo.setBrandName(brandEntity.getName());
            return brandVo;
        }).collect(Collectors.toList());
        return R.ok().put("data", collect);
    }

    /**
     * 查询品牌关联分类列表
     */
    //@RequestMapping(value = "/catelog/list", method = RequestMethod.GET)
    @GetMapping(value = "/catelog/list")
    public R catelogList(@RequestParam("brandId") Long brandId) {

        //查询和条件
        List<CategoryBrandRelationEntity> data = categoryBrandRelationService.list(
                new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));

        return R.ok().put("data", data);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:categorybrandrelation:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("product:categorybrandrelation:info")
    public R info(@PathVariable("id") Long id) {
        CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     * 新增品牌和分类关系，新增saveDeltil方法，需要保存两个冗余字段
     * 利用品牌id、分类id找出品牌name、分类name，然后保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:categorybrandrelation:save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {

        //新增方法
        categoryBrandRelationService.saveDetail(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:categorybrandrelation:update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:categorybrandrelation:delete")
    public R delete(@RequestBody Long[] ids) {
        categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
