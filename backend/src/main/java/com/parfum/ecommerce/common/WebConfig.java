package com.parfum.ecommerce.common;

import com.parfum.ecommerce.admin.AdminAuditInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AdminAuditInterceptor adminAuditInterceptor;

    public WebConfig(AdminAuditInterceptor adminAuditInterceptor) {
        this.adminAuditInterceptor = adminAuditInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuditInterceptor)
                .addPathPatterns(
                        "/api/admin/**",
                        "/api/products/admin/**",
                        "/api/orders/*/status",
                        "/api/orders/*/cancel",
                        "/api/orders/*/shipment");
    }
        @org.springframework.beans.factory.annotation.Value("${app.images.local-dir}")
    private String imagesDirectory;

    @Override
    public void addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
        String path = java.nio.file.Paths.get(imagesDirectory).toAbsolutePath().normalize().toString();

        registry.addResourceHandler("/media/**")
                .addResourceLocations("file:" + path + "/")
                .setCachePeriod(3600);
    }
}