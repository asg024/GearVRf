package com.samsung.smcl.vr.widgets.main;

/**
 * Interface for the custodian of a class instances. Each instance of
 * {@code Holder} can be associated with only one class instance.
 */
public interface Holder {
    public <T> T get(Class<T> clazz);

    public <T> void register(T holdee);
}
