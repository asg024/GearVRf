package com.samsung.smcl.vr.widgets;

import org.gearvrf.scene_objects.GVRTextViewSceneObject.IntervalFrequency;

import android.graphics.drawable.Drawable;

public interface TextContainer {

    public abstract Drawable getBackGround();

    public abstract int getBackgroundColor();

    public abstract int getGravity();

    public abstract IntervalFrequency getRefreshFrequency();

    public abstract CharSequence getText();

    public abstract int getTextColor();

    public abstract float getTextSize();

    public abstract String getTextString();

    public abstract void setBackGround(Drawable drawable);

    public abstract void setBackgroundColor(int color);

    public abstract void setGravity(int gravity);

    public abstract void setRefreshFrequency(IntervalFrequency frequency);

    public abstract void setText(CharSequence text);

    public abstract void setTextColor(int color);

    public abstract void setTextSize(float size);

}