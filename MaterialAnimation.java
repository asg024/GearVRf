package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRMaterialAnimation;

public abstract class MaterialAnimation extends Animation {

    protected MaterialAnimation(final Widget target, final float duration) {
        super(target);
        mAdapter = new Adapter(target.getSceneObject(), duration);
    }

    /* package */
    MaterialAnimation(final Widget target) {
        super(target);
        mAdapter = null;
    }

    @Override
    Animation.AnimationAdapter getAnimation() {
        return mAdapter;
    }

    private class Adapter extends GVRMaterialAnimation implements
            Animation.AnimationAdapter {
        public Adapter(GVRSceneObject target, float duration) {
            super(target, duration);
        }

        @Override
        public void animate(GVRHybridObject target, float ratio) {
            doAnimate(ratio);
        }
    }

    private final Adapter mAdapter;
}
