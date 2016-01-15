package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.animation.GVRColorAnimation;

public class ColorAnimation extends MaterialAnimation {

    public ColorAnimation(final Widget target, final float duration,
            final int color) {
        super(target);
        mAdapter = new Adapter(target, duration, color);
    }

    public ColorAnimation(final Widget target, final float duration,
            final float[] rgb) {
        super(target);
        mAdapter = new Adapter(target, duration, rgb);
    }

    @Override
    protected void animate(Widget target, float ratio) {
        mAdapter.superAnimate(target, ratio);
    }

    @Override
    Animation.AnimationAdapter getAnimation() {
        return mAdapter;
    }

    private class Adapter extends GVRColorAnimation implements
            Animation.AnimationAdapter {

        public Adapter(Widget target, float duration, float[] rgb) {
            super(target.getSceneObject(), duration, rgb);
        }

        public Adapter(Widget target, float duration, int color) {
            super(target.getSceneObject(), duration, color);
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
