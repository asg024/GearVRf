package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.animation.GVROpacityAnimation;

public class OpacityAnimation extends MaterialAnimation {

    public OpacityAnimation(final Widget target, final float duration,
            final float opacity) {
        super(target);
        mAdapter = new Adapter(target, duration, opacity);
    }

    @Override
    protected void animate(Widget target, float ratio) {
        mAdapter.superAnimate(target, ratio);
    }

    @Override
    AnimationAdapter getAnimation() {
        return mAdapter;
    }

    private class Adapter extends GVROpacityAnimation implements
            Animation.AnimationAdapter {

        public Adapter(Widget target, float duration, float opacity) {
            super(target.getSceneObject(), duration, opacity);
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
