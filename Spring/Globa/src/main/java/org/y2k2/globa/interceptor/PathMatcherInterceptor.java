package org.y2k2.globa.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.y2k2.globa.util.PathContainer;
import org.y2k2.globa.util.PathMethod;

public class PathMatcherInterceptor implements HandlerInterceptor {

    private final HandlerInterceptor handlerInterceptor;
    private final PathContainer pathContainer;

    public PathMatcherInterceptor(HandlerInterceptor handlerInterceptor) {
        this.handlerInterceptor = handlerInterceptor;
        this.pathContainer = new PathContainer();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        // pathContainer에 해당 요청 url과 메서드가 포함되지 않다면 인증 인터셉터 건너뛰기
        if (pathContainer.notIncludedPath(request.getServletPath(), request.getMethod())) {
            return true;
        }

        // 해당 요청 url과 메서드가 포함이 되어있다면 인증 인터셉터에 요청을 위임 (인터셉터 기능 실행)
        return handlerInterceptor.preHandle(request, response, handler);
    }

    // 외부에서 적용 path 패턴을 추가할 때
    public PathMatcherInterceptor includePathPattern(String pathPattern, PathMethod pathMethod) {
        pathContainer.includePathPattern(pathPattern, pathMethod);
        return this;
    }

    // 외부에서 미적용 path 패턴을 추가할 때
    public PathMatcherInterceptor excludePathPattern(String pathPattern, PathMethod pathMethod) {
        pathContainer.excludePathPattern(pathPattern, pathMethod);
        return this;
    }
}
