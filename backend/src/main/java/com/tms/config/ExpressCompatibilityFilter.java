package com.tms.config;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Mirrors the default cors() middleware: wildcard origin, reflected request headers, no credentials. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class ExpressCompatibilityFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        res.setHeader("Access-Control-Allow-Origin", "*");
        if ("OPTIONS".equals(req.getMethod())) {
            res.setHeader("Access-Control-Allow-Methods", "GET,HEAD,PUT,PATCH,POST,DELETE");
            res.setHeader("Vary", "Access-Control-Request-Headers");
            String requested = req.getHeader("Access-Control-Request-Headers");
            if (requested != null) res.setHeader("Access-Control-Allow-Headers", requested);
            res.setStatus(204);
            res.setContentLength(0);
            return;
        }
        chain.doFilter(req, res);
    }
}
