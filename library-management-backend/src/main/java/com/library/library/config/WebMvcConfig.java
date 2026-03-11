package com.library.library.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.path.bookimages:C:/Users/Rue/Desktop/58(1)/58/images/bookimages}")
    private String bookImagesPath;

    @Value("${upload.path.userimages:C:\\Users\\Rue\\Desktop\\58(1)\\58\\images\\userimages}")
    private String userImagesPath;

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private RoleCheckInterceptor roleCheckInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/**", "/doc.html", "/webjars/**", "/upload/**", "/images/**");
        // 添加角色检查拦截器（基于 @RequireRole 注解）
        registry.addInterceptor(roleCheckInterceptor)
                .addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        // 图书封面图片静态资源访问
        registry.addResourceHandler("/images/bookimages/**")
                .addResourceLocations("file:" + bookImagesPath + "/");
        // 用户头像静态资源访问
        registry.addResourceHandler("/images/userimages/**")
                .addResourceLocations("file:" + userImagesPath + "/");
    }
}