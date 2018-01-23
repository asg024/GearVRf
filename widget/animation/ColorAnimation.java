package com.samsung.smcl.vr.widgets.widget.animation;

import java.util.Arrays;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.animation.GVRColorAnimation;
import org.gearvrf.utility.Colors;
import org.json.JSONException;
import org.json.JSONObject;

import com.samsung.smcl.vr.gvrf_launcher.util.Helpers;
import com.samsung.smcl.vr.widgets.widget.Widget;

import static com.samsung.smcl.vr.widgets.widget.properties.JSONHelpers.getFloat;

public class ColorAnimation extends MaterialAnimation {

    public enum Properties { color }

    public ColorAnimation(final Widget target, final float duration,
                          final int color) {
        this(target, duration, Colors.toColors(color));
    }

    public ColorAnimation(final Widget target, final float duration,
            final float[] rgb) {
        super(target);
        mTargetColor = Arrays.copyOf(rgb, rgb.length);
        mAdapter = new Adapter(target, duration, rgb);
    }

    public ColorAnimation(final Widget target, final JSONObject parameters)
            throws JSONException {
        this(target, getFloat(parameters, Animation.Properties.duration), //
                Helpers.getJSONColorGl(parameters, Properties.color));
    }

    public float[] getColor() {
        return Arrays.copyOf(mTargetColor, mTargetColor.length);
    }

    public float[] getCurrentColor() {
        return getTarget().getColor();
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

        @Override
        public void animate(GVRHybridObject target, float ratio) {
            doAnimate(ratio);
        }

        void superAnimate(Widget target, float ratio) {
            super.animate(target.getSceneObject(), ratio);
        }
    }

    private final Adapter mAdapter;

    private final float[] mTargetColor;
}
