package com.wkl.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.wkl.gulimall.product.entity.ProductAttrValueEntity;
import com.wkl.gulimall.product.service.ProductAttrValueService;
import com.wkl.gulimall.product.vo.AttrRespVo;
import com.wkl.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wkl.gulimall.product.service.AttrService;
import com.wkl.common.utils.PageUtils;
import com.wkl.common.utils.R;



/**
 * 商品属性
 *
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-02 16:26:13
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    /**
     * SPU修改规格
     *  /product/attr/update/{spuId}
     * @param spuId
     * @param entities
     * @return
     */
    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId,
                           @RequestBody List<ProductAttrValueEntity> entities){
        //SPU修改规格
        productAttrValueService.updateSpuAttr(spuId,entities);

        return R.ok();
    }

    /**
     * SPU规格维护
     * /product/attr/base/listforspu/{spuId}
     * @param spuId
     * @return
     */
    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrlistforspu(@PathVariable("spuId") Long spuId){

        //SPU规格维护
        List<ProductAttrValueEntity> entities = productAttrValueService.baseAttrlistforspu(spuId);

        return R.ok().put("data",entities);
    }


    /**
     * 规格参数，销售属性共用，attrType来区分
     *
     * 规格参数：列表，表格，树形菜单指定id和输入框模糊查询
     *          /product/attr/base/list/{catelogId}
     * 销售属性：列表，表格，树形菜单指定id和输入框模糊查询
     *          /product/attr/sale/list/{catelogId}
     */
    @RequestMapping("/{attrType}/list/{catelogId}")
    //@RequiresPermissions("product:attr:list")
    public R baseAttrList(@RequestParam Map<String, Object> params
            , @PathVariable("catelogId") Long catelogId
            , @PathVariable("attrType") String type){

        //树形菜单指定id和输入框模糊查询，规格参数，销售属性共用，type来区分
        PageUtils page = attrService.queryBaseAttrPage(params, catelogId, type);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息，获取信息和完整路径，回显使用
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
		//AttrEntity attr = attrService.getById(attrId);

        //新的方法，返回vo类
        AttrRespVo respVo = attrService.getAttrInfoById(attrId);
        return R.ok().put("attr", respVo);
    }

    /**
     * 保存
     * 有了VO类，规格参数保存请求，使用AttrVO做参数
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVo attr){
		attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改，同步修改vo类中的其他属性
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrRespVo respVo){
		attrService.updateAttrById(respVo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
