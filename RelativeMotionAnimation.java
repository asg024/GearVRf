package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.animation.GVRRelativeMotionAnimation;

public class RelativeMotionAnimation extends TransformAnimation {

    public RelativeMotionAnimation(final Widget widget, float duration, float deltaX, float deltaY, float deltaZ) {
        super(widget);
        mAdapter = new Adapter(widget, duration, deltaX, deltaY, deltaZ);
    }

    @Override
    protected void animate(Widget target, float ratio) {
        mAdapter.superAnimate(target, ratio);
    }


    @Override
    Animation.AnimationAdapter getAnimation() {
        return mAdapter;
    }

    private class Adapter extends GVRRelativeMotionAnimation implements Animation.AnimationAdapter{
        Adapter(Widget widget, float duration, float deltaX, float deltaY, float deltaZ) {
            super(widget.getSceneObject(), duration, deltaX, deltaY, deltaZ);
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
