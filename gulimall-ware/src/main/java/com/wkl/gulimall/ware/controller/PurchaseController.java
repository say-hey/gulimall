package com.wkl.gulimall.ware.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.wkl.gulimall.ware.vo.MergeVo;
import com.wkl.gulimall.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.wkl.gulimall.ware.entity.PurchaseEntity;
import com.wkl.gulimall.ware.service.PurchaseService;
import com.wkl.common.utils.PageUtils;
import com.wkl.common.utils.R;



/**
 * 采购信息
 *
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-03 16:20:52
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 采购人员完成采购
     *  vo类封装下面的请求信息
     *  id: 123,//采购单id
     *  items: [{itemId:1,status:4,reason:""}]//完成/失败的需求详情
     * /ware/purchase/done
     * @param doneVo
     * @return
     */
    @PostMapping("/done")
    public R finish(@RequestBody PurchaseDoneVo doneVo){

        //完成采购
        purchaseService.done(doneVo);

        return R.ok();
    }

    /**
     * 采购人员领取采购单
     * /ware/purchase/received
     * [1,2,3,4]//采购单id
     * @param ids
     * @return
     */
    @RequestMapping("/received")
    public R received(@RequestBody List<Long> ids){
        //采购人员领取采购单
        purchaseService.received(ids);
        return R.ok();
    }

    /**
     * 合并多个采购需求为采购单，需要vo
     * /ware/purchase/merge
     * @param mergeVo
     * @return
     */
    @RequestMapping("/merge")
    public R merge(@RequestBody MergeVo mergeVo){
        //合并采购需求
        R r = purchaseService.mergePurchase(mergeVo);
        if (!StringUtils.isEmpty(r.get("msg"))){
            return R.error().put("msg","不能合并");
        }
        return R.ok();
    }

    /**
     * 未领取的采购单
     * /ware/purchase/unreceive/list
     * @param params
     * @return
     */
    @RequestMapping("/unreceive/list")
    public R unreceivelist(@RequestParam Map<String, Object> params){
        //未领取的采购单
        PageUtils page = purchaseService.queryPageUnreceivePurchase(params);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase){
        //修改保存controller，在保存时设置时间属性
        purchase.setUpdateTime(new Date());
        purchase.setCreateTime(new Date());
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
