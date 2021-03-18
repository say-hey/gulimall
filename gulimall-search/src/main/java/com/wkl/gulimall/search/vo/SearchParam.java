package com.wkl.gulimall.search.vo;

import lombok.Data;

import java.util.List;

//http://search.gulimall.com/list.html?catalog3Id=225&keyword=iPhone&skuPrice=_6999&attrs=5_Apple&attrs=8_162
@Data
public class SearchParam {

    /**
     *     Description：封装页面所有可能传递过来的关键字
     *   		catalog3Id=225&keyword=华为&sort=saleCount_asc&hasStock=0/1&brandId=25&brandId=30
     *
     *   http://search.gulimall.com/list.html?catalog3Id=225&keyword=iPhone&skuPrice=_6999&attrs=5_Apple&attrs=8_162
     */

    /**
     * 全文匹配关键字
     */
    private String keyword;

    /**
     * 三级分类id
     */
    private Long catalog3Id;

    /**
     * sort=saleCount_asc/desc
     * sort=skuPrice_asc/desc
     * sort=hotScore_asc/desc
     */
    private String sort;

    /**
     * 好多的过滤条件
     * hasStock(是否有货)、skuPrice区间、brandId、catalog3Id、attrs
     * hasStock=0/1
     * skuPrice=1_500
     */
    private Integer hasStock;

    /**
     * 价格区间
     */
    private String skuPrice;

    /**
     * 品牌id 可以多选
     */
    private List<Long> brandId;

    /**
     * 按照属性进行筛选
     */
    private List<String> attrs;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 原生所有查询属性
     */
    private String _queryString;
}
