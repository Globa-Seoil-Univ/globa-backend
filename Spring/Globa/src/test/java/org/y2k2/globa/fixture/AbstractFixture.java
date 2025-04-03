package org.y2k2.globa.fixture;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFixture<T> {
    protected abstract T build();

    public T create() {
        return build();
    }
}
