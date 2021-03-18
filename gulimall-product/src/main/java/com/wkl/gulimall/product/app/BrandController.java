package com.wkl.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.wkl.common.valid.AddGroup;
import com.wkl.common.valid.UpdateGroup;
import com.wkl.common.valid.UpdateStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.wkl.gulimall.product.entity.BrandEntity;
import com.wkl.gulimall.product.service.BrandService;
import com.wkl.common.utils.PageUtils;
import com.wkl.common.utils.R;


/**
 * 品牌
 *
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-02 16:26:13
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     * 增加模糊查询功能
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    @GetMapping("/infos")
    public R info(@RequestParam("brandIds") List<Long> brandIds) {
        List<BrandEntity> brand = brandService.getBrandByIds(brandIds);
        return R.ok().put("data", brand);
    }

    /**
     * 保存
     * @Valid：开启jsr303
     * BindingResult：接收结果，有了统一异常处理就不需要了
     * @Validated(value = {AddGroup.class})：jsr303分组校验
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:brand:save")
    public R save(@Validated(value = {AddGroup.class}) @RequestBody BrandEntity brand/*, BindingResult bindingResult*/){

        //有统一异常之后，就不需要单独处理了，也不需要BindingResult了
//        if(bindingResult.hasErrors()){
//            HashMap<Object, Object> map = new HashMap<>();
//            // 获取校验结果
//            bindingResult.getFieldErrors().forEach((item)->{
//                map.put(item.getField(), item.getDefaultMessage());
//            });
//            return R.error(400, "提交的数据不合法").put("data", map);
//        }else{
//            brandService.save(brand);
//        }

		 brandService.save(brand);

        return R.ok();
    }

    /**
     * 修改
     * 更新品牌-分类关系表，级联更新冗余字段
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:brand:update")
    public R update(@Validated(value = {UpdateGroup.class}) @RequestBody BrandEntity brand){
        //新增方法
		brandService.updateDetailById(brand);

        return R.ok();
    }

    /**
     * 修改showStatus状态
     */
    @RequestMapping("/update/status")
    //@RequiresPermissions("product:brand:update")
    public R updateStatus(@Validated(value = {UpdateStatusGroup.class}) @RequestBody BrandEntity brand){
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
