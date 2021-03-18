package com.wkl.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wkl.common.utils.PageUtils;
import com.wkl.common.utils.R;
import com.wkl.gulimall.ware.entity.PurchaseEntity;
import com.wkl.gulimall.ware.vo.MergeVo;
import com.wkl.gulimall.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-03 16:20:52
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    //未领取的采购单
    PageUtils queryPageUnreceivePurchase(Map<String, Object> params);

    //合并多个采购需求为一个采购单
    R mergePurchase(MergeVo mergeVo);

    //采购人员领取采购单
    void received(List<Long> ids);

    //采购人员完成采购
    void done(PurchaseDoneVo doneVo);
}

