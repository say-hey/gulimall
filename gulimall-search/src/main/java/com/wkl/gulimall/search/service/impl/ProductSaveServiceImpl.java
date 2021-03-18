package com.wkl.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.wkl.common.to.es.SkuEsModel;
import com.wkl.gulimall.search.config.GulimallElasticSearchConfig;
import com.wkl.gulimall.search.constant.EsConstant;
import com.wkl.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Resource
    private RestHighLevelClient client;

    /**
     * 商品上架，返回是否成功
     * @param skuEsModels
     * @return
     */
    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        // 1.给ES建立一个索引 product
        BulkRequest bulkRequest = new BulkRequest();
        // 2.构造保存请求
        for (SkuEsModel esModel : skuEsModels) {
            // 创建索引
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            // 设置索引id，是skuid
            indexRequest.id(esModel.getSkuId().toString());
            String jsonString = JSON.toJSONString(esModel);
            // 设置数据，和类型
            indexRequest.source(jsonString, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        // 批量存储，并且返回结果
        BulkResponse bulk = client.bulk(bulkRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        // TODO 是否拥有错误
        boolean hasFailures = bulk.hasFailures();
        if(hasFailures){
            List<String> collect = Arrays.stream(bulk.getItems()).map(item -> item.getId()).collect(Collectors.toList());
            log.error("商品上架错误：{}",collect);
        }
        //返回上架时候成功
        return hasFailures;
    }
}
