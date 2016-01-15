package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.animation.GVRRotationByAxisAnimation;

public class RotationByAxisAnimation extends TransformAnimation {

    public RotationByAxisAnimation(final Widget target, float duration,
            float angle, float x, float y, float z) {
        super(target);
        mAdapter = new Adapter(target, duration, angle, x, y, z);
    }

    @Override
    protected void animate(Widget target, float ratio) {
        mAdapter.superAnimate(target, ratio);
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
