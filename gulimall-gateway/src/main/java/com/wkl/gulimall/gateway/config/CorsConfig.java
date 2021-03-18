package com.wkl.gulimall.gateway.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@Configuration
public class CorsConfig {
    /**
     * 网关统一配置跨域
     * @return
     */
    @Bean
    public CorsWebFilter corsWebFilter(){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        //配置CORS响应头文件，允许跨域
        //允许哪些请求头跨域，允许所有，Collections.singletonList创建不可变列表
        corsConfiguration.setAllowedHeaders(Collections.singletonList(CorsConfiguration.ALL));
        //允许哪些请求方式跨域，允许所有
        corsConfiguration.setAllowedMethods(Collections.singletonList(CorsConfiguration.ALL));
        //允许哪些请求来源跨域，允许所有
        corsConfiguration.setAllowedOrigins(Collections.singletonList(CorsConfiguration.ALL));
        //是否允许携带cookie跨域，允许
        corsConfiguration.setAllowCredentials(true);

        //注册跨域路径，/**任何路径
        source.registerCorsConfiguration("/**",corsConfiguration);
        return new CorsWebFilter(source);
    }
}
