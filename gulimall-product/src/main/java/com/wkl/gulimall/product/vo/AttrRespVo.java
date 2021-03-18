package com.wkl.gulimall.product.vo;

import lombok.Data;

/**
 * 由于需要使用AttrVo的所有属性，所以新的VO可以直接继承AttrVo类
 */
@Data
public class AttrRespVo extends AttrVo{
    /*
    			"catelogName": "手机/数码/手机", //所属分类名字
			"groupName": "主体", //所属分组名字
     */
    private String catelogName;
    private String groupName;
    /*
    		"catelogPath": [
			2,
			34,
			225
		] //完整分类路径,回显
     */
    private Long[] catelogPath;


}
