package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.json.JSONException;
import org.json.JSONObject;

public class RotationByAxisAnimation extends TransformAnimation {

    public RotationByAxisAnimation(final Widget target, float duration,
            float angle, float x, float y, float z) {
        super(target);
        mAdapter = new Adapter(target, duration, angle, x, y, z);
    }

    public RotationByAxisAnimation(final Widget target, final JSONObject params)
            throws JSONException {
        super(target);
        mAdapter = new Adapter(target, (float) params.getDouble("duration"),
                (float) params.getDouble("angle"),
                (float) params.getDouble("x"), (float) params.getDouble("y"),
                (float) params.getDouble("z"));
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

    private class Adapter extends GVRRotationByAxisAnimation implements
            Animation.AnimationAdapter {
        Adapter(Widget target, float duration, float angle, float x, float y,
                float z) {
            super(target.getSceneObject(), duration, angle, x, y, z);
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
