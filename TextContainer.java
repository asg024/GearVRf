package com.samsung.smcl.vr.widgets;

import org.gearvrf.scene_objects.GVRTextViewSceneObject.IntervalFrequency;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

public interface TextContainer {

    Drawable getBackGround();

    int getBackgroundColor();

    int getGravity();

    IntervalFrequency getRefreshFrequency();

    CharSequence getText();

    int getTextColor();

    float getTextSize();

    Typeface getTypeface();

    String getTextString();

    void setBackGround(Drawable drawable);

    void setBackgroundColor(int color);

    void setGravity(int gravity);

    void setRefreshFrequency(IntervalFrequency frequency);

    void setText(CharSequence text);

    void setTextColor(int color);

    void setTextSize(float size);

    void setTypeface(Typeface typeface);
}