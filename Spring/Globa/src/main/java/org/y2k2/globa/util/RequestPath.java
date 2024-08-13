package org.y2k2.globa.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RequestPath {
    private final String pathPattern;
    private final PathMethod method;

    public boolean matchesMethod(String method) {
        return this.method.name().equalsIgnoreCase(method);
    }
}
