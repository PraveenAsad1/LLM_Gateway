package com.praveen.llm_gateway.api.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Servlet filter that logs every HTTP request and its resulting status code.
 *
 * <p>Placed at the front of the filter chain so all requests (including errors)
 * are captured. Uses SLF4J so log level can be adjusted at runtime.</p>
 */
@Component
public class RequestLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  httpReq  = (HttpServletRequest)  request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        long start = System.currentTimeMillis();

        log.info("→ {} {} (from {})",
                httpReq.getMethod(),
                httpReq.getRequestURI(),
                httpReq.getRemoteAddr());

        try {
            chain.doFilter(request, response);
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            log.info("← {} {} | status={} | {}ms",
                    httpReq.getMethod(),
                    httpReq.getRequestURI(),
                    httpResp.getStatus(),
                    elapsed);
        }
    }
}
