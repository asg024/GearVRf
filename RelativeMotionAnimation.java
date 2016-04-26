package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.animation.GVRRelativeMotionAnimation;
import org.json.JSONException;
import org.json.JSONObject;

public class RelativeMotionAnimation extends TransformAnimation {

    public RelativeMotionAnimation(final Widget widget, float duration, float deltaX, float deltaY, float deltaZ) {
        super(widget);
        mDeltaX = deltaX;
        mDeltaY = deltaY;
        mDeltaZ = deltaZ;
        mAdapter = new Adapter(widget, duration, deltaX, deltaY, deltaZ);
    }

    public RelativeMotionAnimation(final Widget widget, final JSONObject params)
            throws JSONException {
        this(widget, (float) params.getDouble("duration"), //
                (float) params.getDouble("delta_x"), //
                (float) params.getDouble("delta_y"), //
                (float) params.getDouble("delta_z"));
    }

    public float getDeltaX() {
        return mDeltaX;
    }

    public float getDeltaY() {
        return mDeltaY;
    }

    public float getDeltaZ() {
        return mDeltaZ;
    }

    public float getCurrentX() {
        return getTarget().getPositionX();
    }

    public float getCurrentY() {
        return getTarget().getPositionY();
    }

    public float getCurrentZ() {
        return getTarget().getPositionZ();
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
    private final float mDeltaX;
    private final float mDeltaY;
    private final float mDeltaZ;
}
