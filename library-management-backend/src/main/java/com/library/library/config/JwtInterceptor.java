package com.library.library.config;

import com.library.library.exception.ForbiddenException;
import com.library.library.exception.UnauthorizedException;
import com.library.library.utils.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtInterceptor.class);
    private static final Pattern BOOK_DETAIL_PATH = Pattern.compile("^/books/\\d+$");
    private static final Pattern COMMENT_BOOK_PATH = Pattern.compile("^/comments/book/\\d+$");
    private static final Pattern COMMENT_RATING_PATH = Pattern.compile("^/comments/book/\\d+/rating$");

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");
        
        logger.info("JWT Interceptor - Path: {}, AuthHeader: {}", path, authHeader);

        if (isPublicPath(request)) {
            return true;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (isOptionalAuthPath(request)) {
                return true;
            }
            throw new UnauthorizedException("未提供认证令牌");
        }

        String token = authHeader.substring(7);
        try {
            String username = jwtTokenProvider.getUsernameFromToken(token);

            if (username == null) {
                throw new UnauthorizedException("无效的认证令牌");
            }

            // 将用户信息存入 ThreadLocal，供后续使用
            UserContextHolder.setUserId(jwtTokenProvider.getUserIdFromToken(token));
            UserContextHolder.setUsername(username);
            UserContextHolder.setRole(jwtTokenProvider.getRoleFromToken(token));
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("JWT token invalid or expired, path: {}", path, e);
            throw new UnauthorizedException("登录已过期，请重新登录");
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.clear();
    }

    private boolean isPublicPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (path.startsWith("/auth/")) {
            return true;
        }

        if (path.startsWith("/upload/") || path.startsWith("/images/") || path.equals("/doc.html") || path.startsWith("/webjars/")) {
            return true;
        }

        if (path.startsWith("/api/statistics") || path.startsWith("/api/category")) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method)) {
            if ("/books".equals(path) || "/books/categories".equals(path) || "/books/recommended".equals(path)) {
                return true;
            }
            if (BOOK_DETAIL_PATH.matcher(path).matches()) {
                return true;
            }
            if (COMMENT_RATING_PATH.matcher(path).matches()) {
                return true;
            }
        }

        return false;
    }

    private boolean isOptionalAuthPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "GET".equalsIgnoreCase(request.getMethod()) && COMMENT_BOOK_PATH.matcher(path).matches();
    }
}
