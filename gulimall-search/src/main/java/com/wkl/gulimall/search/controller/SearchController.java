package com.wkl.gulimall.search.controller;


import com.wkl.gulimall.search.service.MallService;
import com.wkl.gulimall.search.vo.SearchParam;
import com.wkl.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {

    @Autowired
    private MallService masllService;

    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam,  Model model, HttpServletRequest request){

        // 获取路径原生的查询属性
        searchParam.set_queryString(request.getQueryString());
        // ES中检索到的结果 传递给页面
        SearchResult result = masllService.search(searchParam);
        model.addAttribute("result", result);
        return "list";
    }
}
