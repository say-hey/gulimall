package com.wkl.gulimall.ware.vo;

import lombok.Data;

/**
 * 采购人员完成采购
 * 作为提交的采购需求的vo类
 */
@Data
public class PurchaseItemDoneVo {
    //items: [{itemId:1,status:4,reason:""}]//完成/失败的需求详情
    private Long itemId;
    private Integer status;
    private String reason;
}
