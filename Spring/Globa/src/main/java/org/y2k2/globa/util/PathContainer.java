package org.y2k2.globa.util;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.ArrayList;
import java.util.List;

public class PathContainer {

    private final PathMatcher pathMatcher;
    private final List<RequestPath> includePathPattern;
    private final List<RequestPath> excludePathPattern;

    public PathContainer() {
        this.pathMatcher = new AntPathMatcher();
        this.includePathPattern = new ArrayList<>();
        this.excludePathPattern = new ArrayList<>();
    }

    public boolean notIncludedPath(String targetPath, String pathMethod) {
        boolean excludePattern = excludePathPattern.stream()
                .anyMatch(requestPath -> anyMatchPathPattern(targetPath, pathMethod, requestPath));

        boolean includePattern = includePathPattern.stream()
                .anyMatch(requestPath -> anyMatchPathPattern(targetPath, pathMethod, requestPath));

        // excludePattern에 있거나, includePattern에 없으면 true를 반환
        return excludePattern || !includePattern;
    }

    private boolean anyMatchPathPattern(String targetPath, String pathMethod, RequestPath requestPath) {
        return pathMatcher.match(requestPath.getPathPattern(), targetPath) &&
                requestPath.matchesMethod(pathMethod);
    }

    public void includePathPattern(String targetPath, PathMethod pathMethod) {
        this.includePathPattern.add(new RequestPath(targetPath, pathMethod));
    }

    public void excludePathPattern(String targetPath, PathMethod pathMethod) {
        this.excludePathPattern.add(new RequestPath(targetPath, pathMethod));
    }
}
