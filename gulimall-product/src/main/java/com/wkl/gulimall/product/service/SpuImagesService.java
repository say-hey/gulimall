package com.wkl.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wkl.common.utils.PageUtils;
import com.wkl.gulimall.product.entity.SpuImagesEntity;

import java.util.List;
import java.util.Map;

/**
 * spu图片
 *
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-02 16:26:13
 */
public interface SpuImagesService extends IService<SpuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);

    //保存spu的图片集 pms_spu_images,由于图片需要跟spuId进行关联，所以imageId不是自增的，直接使用spuId，自定义方法
    void saveImages(Long spuId, List<String> images);
}

