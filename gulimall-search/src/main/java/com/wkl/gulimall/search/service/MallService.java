package com.wkl.gulimall.search.service;

import com.wkl.gulimall.search.vo.SearchParam;
import com.wkl.gulimall.search.vo.SearchResult;

public interface MallService {
    /**
     * 检索所有参数
     * @param searchParam
     * @return
     */
    SearchResult search(SearchParam searchParam);
}
