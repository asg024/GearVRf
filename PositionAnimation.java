package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.animation.GVRPositionAnimation;
import org.json.JSONException;
import org.json.JSONObject;

public class PositionAnimation extends TransformAnimation {

    public PositionAnimation(final Widget target, final float duration,
            final float x, final float y, final float z) {
        super(target);
        mAdapter = new Adapter(target, duration, x, y, z);
    }

    public PositionAnimation(final Widget target, final JSONObject parameters)
            throws JSONException {
        super(target);
        mAdapter = new Adapter(target,
                (float) parameters.getDouble("duration"),
                (float) parameters.getDouble("x"),
                (float) parameters.getDouble("y"),
                (float) parameters.getDouble("z"));
    }

    @Override
    protected void animate(Widget target, float ratio) {
        mAdapter.superAnimate(target, ratio);
    }

    @Override
    Animation.AnimationAdapter getAnimation() {
        return mAdapter;
    }

    private class Adapter extends GVRPositionAnimation implements
            Animation.AnimationAdapter {
        public Adapter(Widget target, float duration, float x, float y, float z) {
            super(target.getSceneObject(), duration, x, y, z);
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
