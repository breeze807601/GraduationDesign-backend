package com.example.backend.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter.match("/**")       // 指定拦截的url
                    .notMatch("/doc.html", "/favicon.ico", "/webjars/**", "/v3/api-docs/**")  // 指定放行的url
                    .check(su -> {
                        StpUtil.checkLogin();
                        StpUtil.renewTimeout(604800); // 更新过期时间
                    });   // 执行校验
        })).addPathPatterns("/**");
    }
}