package com.hcreator.creator.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AdminAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        boolean isOpenAdminAsset = path.startsWith("/admin/login.html")
                || path.startsWith("/admin/css/")
                || path.startsWith("/admin/js/")
                || path.startsWith("/admin/img/")
                || path.startsWith("/admin/components/");

        boolean isAdminPage = path.startsWith("/admin/") && path.endsWith(".html") && !isOpenAdminAsset;
        boolean isAdminApi = path.startsWith("/api/admin/");

        if (isAdminPage || isAdminApi) {
            HttpSession session = request.getSession(false);
            Object role = session != null ? session.getAttribute("role") : null;

            if (!"ADMIN".equals(role)) {
                if (isAdminPage) {
                    response.sendRedirect("/admin/login.html");
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\":\"관리자 로그인이 필요합니다.\"}");
                }
                return;
            }
        }

        chain.doFilter(request, response);
    }
}