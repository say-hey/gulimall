package com.wkl.common.to;

import lombok.Data;

import java.math.BigDecimal;

//远程保存跨数据库，需要to类和feign，to类需要两个服务共同使用，所以放在gulimall-common中
@Data
public class SpuBoundTo {
    private Long spuId;
    //购物积分
    private BigDecimal buyBounds;
    //成长积分
    private BigDecimal growBounds;
}
