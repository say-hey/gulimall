package com.wkl.gulimall.product.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.wkl.gulimall.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;

@Data
public class AttrGroupWithAttrsVo {
    //获取分类下所有分组&关联属性
    //全部分组数据+一个属性数据分组
    /**
     * 分组id
     */
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;

    /**
     * 报错所属的属性
     */
    private List<AttrEntity> attrs;
}
