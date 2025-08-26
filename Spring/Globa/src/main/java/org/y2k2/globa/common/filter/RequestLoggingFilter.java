package org.y2k2.globa.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (isStaticResource(request.getRequestURI()) || isSwaggerResource(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString().substring(0, 8);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logRequest(wrappedRequest, wrappedResponse, duration, requestId);
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long duration, String requestId) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String ipAddress = getClientIp(request);
        String agent = request.getHeader("User-Agent");
        String device = getDeviceType(agent);
        int status = response.getStatus();

        StringBuilder logMessage = new StringBuilder();
        logMessage.append(String.format("[%s] %s %s - IP: %s - Device: %s - Status: %d - Duration: %dms",
                requestId, method, uri, ipAddress, device, status, duration));

        // 요청 바디 로깅
        if (shouldLogRequestBody(request)) {
            String requestBody = getRequestBody(request);
            logMessage.append(" - ReqBody: ").append(requestBody);
        }

        log.info(logMessage.toString());
    }

    private boolean shouldLogRequestBody(HttpServletRequest request) {
        String method = request.getMethod();
        String contentType = request.getContentType();

        return ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) &&
                contentType != null &&
                (contentType.contains("application/json") ||
                        contentType.contains("application/x-www-form-urlencoded"));
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        try {
            byte[] content = request.getContentAsByteArray();

            if (content.length > 0) {
                String body = new String(content, request.getCharacterEncoding());
                return maskSensitiveData(body);
            }
        } catch (Exception e) {
            log.warn("Failed to read request body", e);
        }

        return "[empty]";
    }

    private String maskSensitiveData(String body) {
        if (body == null) return "[null]";

        return body
                .replaceAll("\"accessToken\"\\s*:\\s*\"[^\"]*\"", "\"accessToken\":\"***\"")
                .replaceAll("\"refreshToken\"\\s*:\\s*\"[^\"]*\"", "\"refreshToken\":\"***\"");
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");

        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }

        return xfHeader.split(",")[0].trim();
    }

    private String getDeviceType(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }

        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
            if (userAgent.contains("android")) return "Android";
            if (userAgent.contains("iphone")) return "iPhone";
            return "Mobile";
        } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return "Tablet";
        } else if (userAgent.contains("windows")) {
            return "Windows";
        } else if (userAgent.contains("macintosh") || userAgent.contains("mac os x")) {
            return "Mac";
        } else if (userAgent.contains("linux")) {
            return "Linux";
        } else {
            return "Desktop";
        }
    }

    private boolean isStaticResource(String uri) {
        return uri.startsWith("/static/") ||
                uri.startsWith("/css/") ||
                uri.startsWith("/js/") ||
                uri.startsWith("/images/") ||
                uri.endsWith(".ico") ||
                uri.endsWith(".png") ||
                uri.endsWith(".jpg") ||
                uri.endsWith(".css") ||
                uri.endsWith(".js");
    }

    private boolean isSwaggerResource(String uri) {
        return uri.startsWith("/swagger/") ||
               uri.startsWith("/v3/api-docs") ||
               uri.startsWith("/swagger-ui/");
    }
}