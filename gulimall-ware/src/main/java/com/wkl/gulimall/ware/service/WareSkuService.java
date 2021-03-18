package com.wkl.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wkl.common.utils.PageUtils;
import com.wkl.gulimall.ware.entity.WareSkuEntity;
import com.wkl.gulimall.ware.vo.SkuHasStockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-03 16:20:52
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    //调用仓库的服务，给库存添加采购的数量，取出skuId，仓库Id，采购数量，进行入库
    void addStock(Long skuId, Long wareId, Integer skuNum);

    //查询sku是否有库存
    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);
}

