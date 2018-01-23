package com.samsung.smcl.vr.widgets.main;

import com.samsung.smcl.vr.widgets.main.Holder;

abstract public class HolderHelper {

    public static <T> void register(Holder holder, T instance) {
        check(holder, instance);

        holder.register(instance);
    }

    public static <T> void check(Holder holder, T instance) {
        Class<? extends Object> clazz = instance.getClass();

        if (holder == null) {
            throw new IllegalArgumentException("NULL holder passed to "
                    + clazz.getSimpleName());
        }

        if (holder.get(clazz) != null) {
            throw new IllegalArgumentException(
                    "The holder already has an instance of " + clazz.getSimpleName());
        }
    }

    private HolderHelper() {

    }
}
