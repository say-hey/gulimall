package com.wkl.gulimall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkl.common.utils.PageUtils;
import com.wkl.common.utils.Query;

import com.wkl.gulimall.product.dao.SpuImagesDao;
import com.wkl.gulimall.product.entity.SpuImagesEntity;
import com.wkl.gulimall.product.service.SpuImagesService;
import org.springframework.util.CollectionUtils;


@Service("spuImagesService")
public class SpuImagesServiceImpl extends ServiceImpl<SpuImagesDao, SpuImagesEntity> implements SpuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuImagesEntity> page = this.page(
                new Query<SpuImagesEntity>().getPage(params),
                new QueryWrapper<SpuImagesEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存spu的图片集 pms_spu_images,由于图片需要跟spuId进行关联，所以imageId不是自增的，直接使用spuId，自定义方法
     * @param spuId
     * @param images
     */
    @Override
    public void saveImages(Long spuId, List<String> images) {
        //判空未提交图片
        if(!CollectionUtils.isEmpty(images)){
            //取出每一个图片封装到entity中，id使用spuId关联
            List<SpuImagesEntity> collect = images.stream().map((img) -> {
                SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
                spuImagesEntity.setSpuId(spuId);
                spuImagesEntity.setImgUrl(img);
                return spuImagesEntity;
            }).collect(Collectors.toList());
            //批量保存
            this.saveBatch(collect);
        }
    }

}