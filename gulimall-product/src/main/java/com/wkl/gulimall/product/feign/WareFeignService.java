package com.wkl.gulimall.product.feign;

import com.wkl.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
@FeignClient("gulimall-ware")
public interface WareFeignService {
    /**
     * 修改 R类 带上泛型
     */
    @PostMapping("/ware/waresku/hasStock")
//	List<SkuHasStockVo> getSkuHasStock(@RequestBody List<Long> SkuIds);
    R getSkuHasStock(@RequestBody List<Long> SkuIds);
}
