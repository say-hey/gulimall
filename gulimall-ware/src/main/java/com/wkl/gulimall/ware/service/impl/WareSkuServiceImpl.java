package com.wkl.gulimall.ware.service.impl;

import com.wkl.common.utils.R;
import com.wkl.gulimall.ware.feign.ProductFeignService;
import com.wkl.gulimall.ware.vo.SkuHasStockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkl.common.utils.PageUtils;
import com.wkl.common.utils.Query;

import com.wkl.gulimall.ware.dao.WareSkuDao;
import com.wkl.gulimall.ware.entity.WareSkuEntity;
import com.wkl.gulimall.ware.service.WareSkuService;
import org.springframework.util.StringUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    /**
     * 添加模糊查询
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         * skuId: 1
         * wareId: 2
         */
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if(!StringUtils.isEmpty(skuId)){
            queryWrapper.eq("sku_id",skuId);
        }
        String wareId = (String) params.get("wareId");
        if(!StringUtils.isEmpty(wareId)){
            queryWrapper.eq("ware_id",wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    /**
     * 调用仓库的服务，给库存添加采购的数量，取出skuId，仓库Id，采购数量，进行入库
     * @param skuId
     * @param wareId
     * @param skuNum
     */
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录就新增
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if(entities == null || entities.size() == 0){
            //新增库存
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
            //1、自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                //取出sku名字，需要调用远程服务
                R info = productFeignService.info(skuId);
                //整条信息被查询出来放在map中，取出，判断是否查询成功
                Map<String,Object> data = (Map<String, Object>) info.get("skuInfo");
                if(info.getCode() == 0){
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e){

            }
            //新增库存
            wareSkuDao.insert(skuEntity);
        }else{
            //更新库存，自己写sql语句
            wareSkuDao.addStock(skuId,wareId,skuNum);
        }
    }

    /**
     * 查询sku是否有库存
     * 库存减去锁定库存
     * SELECT SUM(stock - stock_locked) FROM `wms_ware_sku` WHERE sku_id = 1
     *
     * @param skuIds
     * @return
     */
    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {

        return skuIds.stream().map(id -> {

            SkuHasStockVo stockVo = new SkuHasStockVo();
            // 查询当前sku的总库存量
            stockVo.setSkuId(id);
            // 查数据库，这里库存可能为null 要避免空指针异常
            stockVo.setHasStock(baseMapper.getSkuStock(id)==null?false:true);
            return stockVo;
        }).collect(Collectors.toList());
    }


}