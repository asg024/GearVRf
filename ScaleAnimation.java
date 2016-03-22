package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.animation.GVRScaleAnimation;
import org.json.JSONException;
import org.json.JSONObject;

public class ScaleAnimation extends TransformAnimation {

    public ScaleAnimation(final Widget widget, float duration, float scale) {
        super(widget);
        mAdapter = new Adapter(widget, duration, scale);
    }

    public ScaleAnimation(final Widget widget, float duration, float scaleX,
            float scaleY, float scaleZ) {
        super(widget);
        mAdapter = new Adapter(widget, duration, scaleX, scaleY, scaleZ);
    }

    public ScaleAnimation(final Widget target, final JSONObject params)
            throws JSONException {
        super(target);
        if (params.has("scale")) {
            mAdapter = new Adapter(target,
                    (float) params.getDouble("duration"),
                    (float) params.getDouble("scale"));
        } else {
            mAdapter = new Adapter(target,
                    (float) params.getDouble("duration"),
                    (float) params.getDouble("scale_x"),
                    (float) params.getDouble("scale_y"),
                    (float) params.getDouble("scale_z"));
        }
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
}
