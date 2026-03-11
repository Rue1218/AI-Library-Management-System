package com.library.library.config;

import com.library.library.exception.ForbiddenException;
import com.library.library.exception.UnauthorizedException;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RoleCheckInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;
            RequireRole annotation = hm.getMethodAnnotation(RequireRole.class);
            if (annotation != null) {
                String requiredRole = annotation.value();
                Integer userRole = UserContextHolder.getRole();
                if (userRole == null || !requiredRole.equals(userRole.toString())) {
                    throw new ForbiddenException("无权访问该资源");
                }
            }
        }
        return true;
    }
}
