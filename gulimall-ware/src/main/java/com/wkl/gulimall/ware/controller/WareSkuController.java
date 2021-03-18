package com.wkl.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.wkl.gulimall.ware.vo.SkuHasStockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wkl.gulimall.ware.entity.WareSkuEntity;
import com.wkl.gulimall.ware.service.WareSkuService;
import com.wkl.common.utils.PageUtils;
import com.wkl.common.utils.R;



/**
 * 商品库存
 *
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-03 16:20:52
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 查询sku是否有库存
     * @param SkuIds
     * @return
     */
    @PostMapping("/hasStock")
    //返回值方式不同，最简单的直接返回List中，或者修改R类
    //public List<SkuHasStockVo> getSkuHasStock(@RequestBody List<Long> SkuIds){
    public R getSkuHasStock(@RequestBody List<Long> SkuIds){
        //返回skuId，和库存量
        List<SkuHasStockVo> vos = wareSkuService.getSkuHasStock(SkuIds);
        return R.ok().setData(vos);
    }

    /**
     * 列表，添加模糊查询
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
