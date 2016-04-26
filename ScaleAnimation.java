package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.animation.GVRScaleAnimation;
import org.json.JSONException;
import org.json.JSONObject;

public class ScaleAnimation extends TransformAnimation {

    public ScaleAnimation(final Widget widget, float duration, float scale) {
        super(widget);
        mScaleX = mScaleY = mScaleZ = scale;
        mAdapter = new Adapter(widget, duration, scale);
    }

    public ScaleAnimation(final Widget widget, float duration, float scaleX,
            float scaleY, float scaleZ) {
        super(widget);
        mScaleX = scaleX;
        mScaleY = scaleY;
        mScaleZ = scaleZ;
        mAdapter = new Adapter(widget, duration, scaleX, scaleY, scaleZ);
    }

    public ScaleAnimation(final Widget target, final JSONObject params)
            throws JSONException {
        super(target);
        if (params.has("scale")) {
            final float scale = (float) params.getDouble("scale");
            mScaleX = mScaleY = mScaleZ = scale;
            mAdapter = new Adapter(target,
                    (float) params.getDouble("duration"),
                    scale);
        } else {
            mScaleX = (float) params.getDouble("scale_x");
            mScaleY = (float) params.getDouble("scale_y");
            mScaleZ = (float) params.getDouble("scale_z");
            mAdapter = new Adapter(target,
                    (float) params.getDouble("duration"), mScaleX, mScaleY,
                    mScaleZ);
        }
    }

    public float getScaleX() {
        return mScaleX;
    }

    public float getScaleY() {
        return mScaleY;
    }

    public float getScaleZ() {
        return mScaleZ;
    }

    public float getCurrentScaleX() {
        return getTarget().getScaleX();
    }

    public float getCurrentScaleY() {
        return getTarget().getScaleY();
    }

    public float getCurrentScaleZ() {
        return getTarget().getScaleZ();
    }

    @Override
    protected void animate(Widget target, float ratio) {
        mAdapter.superAnimate(target, ratio);
        target.checkTransformChanged();
    }

    @Override
    Animation.AnimationAdapter getAnimation() {
        return mAdapter;
    }

    private class Adapter extends GVRScaleAnimation implements
            Animation.AnimationAdapter {
        Adapter(Widget widget, float duration, float scale) {
            super(widget.getSceneObject(), duration, scale);
        }

        Adapter(Widget widget, float duration, float scaleX, float scaleY,
                float scaleZ) {
            super(widget.getSceneObject(), duration, scaleX, scaleY, scaleZ);
        }

        @Override
        public void animate(GVRHybridObject target, float ratio) {
            doAnimate(ratio);
        }

        void superAnimate(Widget target, float ratio) {
            super.animate(target.getSceneObject(), ratio);
        }
    }

    private final Adapter mAdapter;
    private final float mScaleX;
    private final float mScaleY;
    private final float mScaleZ;
}
