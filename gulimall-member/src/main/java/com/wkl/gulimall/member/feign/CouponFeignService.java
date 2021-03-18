package com.wkl.gulimall.member.feign;

import com.wkl.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

//注册中心服务提供者的服务名
@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    //接口方法与提供者完全相同，注意类上的地址映射
    @RequestMapping("coupon/coupon/member/list")
    public R memberCoupons();
}
