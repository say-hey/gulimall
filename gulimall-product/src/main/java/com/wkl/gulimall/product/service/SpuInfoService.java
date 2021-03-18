package com.wkl.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wkl.common.utils.PageUtils;
import com.wkl.gulimall.product.entity.SpuInfoEntity;
import com.wkl.gulimall.product.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-02 16:26:13
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    //保存新增商品整个json数据
    void saveSpuInfo(SpuSaveVo spuSaveVo);

    //列表，添加模糊查询功能
    PageUtils queryPageByCondition(Map<String, Object> params);

    //上架商品
    void spuUp(Long spuId);
}

