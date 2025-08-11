package org.y2k2.globa.common.usecase;

public interface UseCase<T, R> {
    R execute(T command);
}
