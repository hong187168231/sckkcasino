package com.qianyi.casinoadmin.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfigurer implements WebMvcConfigurer {
    @Autowired
    private PreReadUploadConfig preReadUploadConfig;
    //设置静态文件的目录
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler("/static/**").addResourceLocations("file:" + preReadUploadConfig.getBasePath());
    }
}
