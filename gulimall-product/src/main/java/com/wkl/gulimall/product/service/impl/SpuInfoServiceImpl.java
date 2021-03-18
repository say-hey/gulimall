package com.wkl.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.wkl.common.constant.ProductConstant;
import com.wkl.common.to.SkuReductionTo;
import com.wkl.common.to.SpuBoundTo;
import com.wkl.common.to.es.SkuEsModel;
import com.wkl.common.to.es.SkuHasStockVo;
import com.wkl.common.utils.R;
import com.wkl.gulimall.product.entity.*;
import com.wkl.gulimall.product.feign.CouponFeignService;
import com.wkl.gulimall.product.feign.SearchFeignService;
import com.wkl.gulimall.product.feign.WareFeignService;
import com.wkl.gulimall.product.service.*;
import com.wkl.gulimall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkl.common.utils.PageUtils;
import com.wkl.common.utils.Query;

import com.wkl.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    //图片描述信息
    @Autowired
    SpuInfoDescService spuInfoDescService;
    //图片集信息
    @Autowired
    SpuImagesService spuImagesService;
    //查询参数名
    @Autowired
    AttrService attrService;
    //保存基本参数
    @Autowired
    ProductAttrValueService productAttrValueService;
    //sku基本信息
    @Autowired
    SkuInfoService skuInfoService;
    //sku的图片
    @Autowired
    SkuImagesService skuImagesService;
    //sku的销售属性，如颜色，版本，内存
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    //Coupon远程调用积分服务
    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    BrandService brandService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductAttrValueService attrValueService;
    @Autowired
    WareFeignService wareFeignService;
    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存新增商品整个json数据，加上事务
     * TODO 部分内容到高级篇再讲
     * @param spuSaveVo
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        //1. 保存spu基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        //json中有两个值没有需要手动设置，其余对拷
        BeanUtils.copyProperties(spuSaveVo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        //保存，保存之后就有自增id了，这个id贯穿整个流程
        this.save(spuInfoEntity);
        Long spuId = spuInfoEntity.getId();

        //2. 保存Spu的描述图片 pms_spu_info_desc
        //查看json结构，从数组中取出描述图片
        List<String> decript = spuSaveVo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        //只有两项，手动设置，图片集需要用"，"分割
        spuInfoDescEntity.setSpuId(spuId);
        spuInfoDescEntity.setDecript(String.join(",", decript));
        //注意spuInfoDescEntity的id不是自增的，所以添加一个注解@TableId(type = IdType.INPUT)
        //商品id，是自己输入的，要和spu产生关联
        spuInfoDescService.save(spuInfoDescEntity);

        //3. 保存spu的图片集 pms_spu_images
        //查看json结构，从数组中取出图片集
        List<String> images = spuSaveVo.getImages();
        //由于图片需要跟spuId进行关联，所以需要设置使用spuId，自定义方法
        spuImagesService.saveImages(spuId, images);

        //4. 保存spu的规格参数;pms_product_attr_value
        //查看json结构，从数组中取出规格参数
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        //每一个规格参数又是一个数组，遍历每一个，封装到entity
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map((attr) -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setAttrId(attr.getAttrId());
            //json没有提交参数名，需要自己查
            AttrEntity id = attrService.getById(attr.getAttrId());
            productAttrValueEntity.setAttrName(id.getAttrName());
            productAttrValueEntity.setAttrValue(attr.getAttrValues());
            productAttrValueEntity.setQuickShow(attr.getShowDesc());
            productAttrValueEntity.setSpuId(spuId);
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(collect);

        //5. 保存spu的积分信息；gulimall_sms->sms_spu_bounds，
        // 远程保存跨数据库，需要to类和feign，to类需要两个服务共同使用，所以放在gulimall-common中
        //查看json结构，从数组中取出积分信息
        Bounds bounds = spuSaveVo.getBounds();
        //创建to，微服务之间以json格式传递数据
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuId);
        //feign调用远程保存方法
        R r =couponFeignService.saveProductBounds(spuBoundTo);
        if(r.getCode() != 0){
            log.error("远程保存spu积分信息失败");
        }


        //6. 保存当前spu对应的所有sku信息；
        List<Skus> skus = spuSaveVo.getSkus();
        if(!CollectionUtils.isEmpty(skus)){
            skus.forEach(sku -> {
                //6.1. sku的基本信息；pms_sku_info
                //遍历所有sku，分步存储
                //判断默认图片
                String defaultImg = "";
                for (Images image : sku.getImages()){
                    if(image.getDefaultImg() == 1){
                        defaultImg = image.getImgUrl();
                    }
                }
                //sku的基本信息，json能映射的可以直接赋值，其余手动赋值
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                //sku部分属性直接从spu的json中赋值
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.save(skuInfoEntity);

                //skuId贯穿sku相关所有属性
                Long skuId = skuInfoEntity.getSkuId();

                //6.2. sku的图片信息；pms_sku_image
                //sku的图片要和skuId绑定
                List<SkuImagesEntity> collect1 = sku.getImages().stream().map((img) -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter((img) -> {
                    //并不会使用所有的图片，只使用从图集选中的图片，所以需要把空的对象过滤掉
                    return !StringUtils.isEmpty(img.getImgUrl());
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(collect1);

                //6.3. sku的销售属性信息：pms_sku_sale_attr_value
                List<Attr> attrs = sku.getAttr();
                List<SkuSaleAttrValueEntity> collect2 = attrs.stream().map((attr) -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(collect2);

                //6.4. sku的优惠、满减等信息；gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                //跨数据库存储，需要to类和feign
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                //验证满减件数和价格输入是否正确
                //BigDecimal类型比较大小方式不同，排除错误输入，如满0件打0折、满0元减0元等无意义值
                //满减和打折是两个表中的数据，满足一个就可以进行存储
                if(skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1){
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if(r1.getCode() != 0){
                        log.error("远程保存sku优惠信息失败");
                    }
                }

            });
        }



    }

    /**
     * 列表，添加模糊查询功能
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        //添加模糊查询条件
        /**
         * status: 1
         * key:
         * brandId: 3
         * catelogId: 225
         */
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and((w)->{
                w.eq("id", key).or().like("spu_name", key);
            });
        }
        String status = (String) params.get("status");
        if(!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status", status);
        }
        //排除默认输入的0，为0就不添加条件
        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id", brandId);
        }
        //这里有个字段有点问题catelogId，catalog_id
        String catelogId = (String) params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)){
            wrapper.eq("catalog_id", catelogId);
        }

        //正常的分页查询
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 上架商品
     * @param spuId
     */
    @Override
    public void spuUp(Long spuId) {
        //存放上架信息
        //ArrayList<SkuEsModel> upProducts = new ArrayList<>();

        // 1、 组装数据 查出当前spuId对应的所有sku信息，品牌信息等
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);

        //TODO 1、发送远程调用，库存系统查询是否有库存
        //不在这里查询是否有库存，直接让guliamll-ware查询
        //map中存放id，和是否有库存
        Map<Long, Boolean> stockMap = null;
        //取出所有skuid
        List<Long> skuIdList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        //查询每一个sku的库存，然后保存下来，由于保存的是skuId，正好对应EsModule的id，所以可以在外面一次性查询，然后用时取出
        try {
            //发送远程调用，查询这些skuid的库存信息
            R hasStock = wareFeignService.getSkuHasStock(skuIdList);
            // 取出id和boolean转成map
            // TypeReference构造器受保护 所以写成内部类对象
            stockMap = hasStock.getData(new TypeReference<List<SkuHasStockVo>>(){}).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, item -> item.getHasStock()));
            log.warn("服务调用成功" + hasStock);
        } catch (Exception e) {
            log.error("库存服务调用失败: 原因{}",e);
        }




        //TODO 4、查出当前sku的所有可以被用来检索的规格属性
        //因为属性在创建时有的设置不可被检索，并且查库存和查sku的spu，都是相同的数据不要放在循环中查，只要查一次就够了
        //4.1、先查sku对应属性pms_product_attr_value
        //查出指定id的spu信息
        List<ProductAttrValueEntity> baseAttrs = attrValueService.baseAttrlistforspu(spuId);
        //获取所有属性id
        List<Long> attrIds = baseAttrs.stream().map(attr -> attr.getAttrId()).collect(Collectors.toList());
        //4.2、再查该属性是否能被检索
        //从指定ids中查出该id是否有可以检索pms_attr
        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);
        //取出这些ids中的id，然后判断这些id是否在提交的spu中，将这些ids放在set中可以更方便判断
        Set<Long> isSet = new HashSet<>(searchAttrIds);
        //过滤可检索的id，然后将这些属性封装到es模型中
        List<SkuEsModel.Attrs> attrs = baseAttrs.stream().filter(item -> isSet.contains(item.getAttrId())).map(item -> {
            SkuEsModel.Attrs attr = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attr);
            return attr;
        }).collect(Collectors.toList());



        // 2、 封装信息 有的属性与es字段不同，需要手动设置
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> skuEsModels = skus.stream().map(sku -> {
            //组装数据 有的字段不同，有的需要手动查询
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            //TODO 2、热度评分。默认为0
            skuEsModel.setHotScore(0L);
            //TODO 3、查询品牌和分类的名字信息
            BrandEntity brandEntity = brandService.getById(sku.getBrandId());
            skuEsModel.setBrandName(brandEntity.getName());
            skuEsModel.setBrandImg(brandEntity.getLogo());
            CategoryEntity categoryEntity = categoryService.getById(sku.getCatalogId());
            skuEsModel.setCatalogName(categoryEntity.getName());
            //设置可搜索属性，是在上面第四步中查出来的
            skuEsModel.setAttrs(attrs);
            //设置是否有库存，需要手动查询是否有库存，不用管数量，是在上面第一步中查出来的
            skuEsModel.setHasStock(finalStockMap==null?false:finalStockMap.get(sku.getSkuId()));
            return skuEsModel;
        }).collect(Collectors.toList());

        //TODO 5、将数据发给es进行保存：gulimall-search
        R r = searchFeignService.productStatusUp(skuEsModels);
        if(r.getCode() == 0){
            // 远程调用成功
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        }else{
            // 远程调用失败 TODO 接口幂等性 重试机制
            /**
             * Feign 的调用流程  Feign有自动重试机制
             * 1. 构造请求数据，将对象转为json
             * 2. 发送请求进行执行（执行成功会解码响应数据）
             * 3. 执行请求会有重试机制
             */
        }
    }

}