package org.y2k2.globa.fixture;

public abstract class AbstractFixture<T> {
    protected abstract T build();

    public T create() {
        return build();
    }
}
