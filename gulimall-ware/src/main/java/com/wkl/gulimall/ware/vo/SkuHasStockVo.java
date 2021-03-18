package com.wkl.gulimall.ware.vo;

import lombok.Data;

@Data
public class SkuHasStockVo {

    private Long skuId;
    //是否有库存
    private Boolean hasStock;
}
