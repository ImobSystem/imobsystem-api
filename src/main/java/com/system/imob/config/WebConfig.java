package com.system.imob.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private PlanoInterceptor planoInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // As exceções ficam dentro do próprio interceptor (rotaLiberada)
        registry.addInterceptor(planoInterceptor)
                .addPathPatterns("/**");
    }
}
