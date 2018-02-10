package com.samsung.smcl.vr.widgets.adapter;

import org.joml.Vector3f;

public abstract class UniformAdapterViewFactory extends BaseAdapterViewFactory {
    public UniformAdapterViewFactory(float dimensions) {
        this(new Vector3f(dimensions));
    }

    public UniformAdapterViewFactory(float x, float y) {
        this(new Vector3f(x, y, 0));
    }

    public UniformAdapterViewFactory(float x, float y, float z) {
        this(new Vector3f(x, y, z));
    }

    public UniformAdapterViewFactory(Vector3f dimensions) {
        mDimensions = dimensions;
    }

    public void setDimensions(float dimensions) {
        mDimensions.set(dimensions);
    }

    public void setDimensions(float x, float y) {
        mDimensions.set(x, y, mDimensions.z);
    }

    public void setDimensions(float x, float y, float z) {
        mDimensions.set(x, y, z);
    }

    public void setDimensions(Vector3f dimensions) {
        mDimensions.set(dimensions);
    }

    @Override
    public boolean hasUniformViewSize() {
        return true;
    }

    @Override
    public float getUniformWidth() {
        return mDimensions.x;
    }

    @Override
    public float getUniformHeight() {
        return mDimensions.y;
    }

    @Override
    public float getUniformDepth() {
        return mDimensions.z;
    }

    private final Vector3f mDimensions;
}
