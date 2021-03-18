package com.wkl.gulimall.search.service;


import com.wkl.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ProductSaveService {

    //商品上架，返回是否成功
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
