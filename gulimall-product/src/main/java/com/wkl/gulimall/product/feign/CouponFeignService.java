package com.wkl.gulimall.product.feign;

import com.wkl.common.to.SkuReductionTo;
import com.wkl.common.to.SpuBoundTo;
import com.wkl.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 远程调用Coupon服务
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    //保存积分信息
    //feign调用远程保存方法，参数类型可以不一样，但是json映射一定要一样
    @PostMapping("/coupon/spubounds/save")
    R saveProductBounds(@RequestBody SpuBoundTo spuBoundTo);

    //保存满减，优惠信息
    //feign调用远程保存方法，参数类型可以不一样，但是json映射一定要一样
    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
