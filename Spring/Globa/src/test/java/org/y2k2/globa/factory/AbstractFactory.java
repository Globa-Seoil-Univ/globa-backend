package org.y2k2.globa.factory;

public abstract class AbstractFactory<T> {
    protected abstract T create();

    protected abstract T setDefaultValues(T entity);

    protected abstract T saveEntity(T entity);

    public T createAndSave() {
        T entity = create();
        entity = setDefaultValues(entity);
        return saveEntity(entity);
    }
}
