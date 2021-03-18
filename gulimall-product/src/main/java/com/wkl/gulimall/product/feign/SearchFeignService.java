package com.wkl.gulimall.product.feign;

import com.wkl.common.to.es.SkuEsModel;
import com.wkl.common.utils.R;
import com.wkl.gulimall.product.vo.SearchParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@FeignClient("gulimall-search")
public interface SearchFeignService {
    //上架商品
    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
