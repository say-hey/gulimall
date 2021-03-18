package com.wkl.gulimall.product.vo;


import lombok.Data;

//属性分组查关联删除请求的参数封装
@Data
public class AttrGroupRelationVo {
    //[{"attrId":1,"attrGroupId":2}]
    private Long attrId;
    private Long attrGroupId;
}
