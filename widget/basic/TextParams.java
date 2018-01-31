package com.samsung.smcl.vr.widgets.widget.basic;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;

import org.json.JSONObject;

import org.gearvrf.scene_objects.GVRTextViewSceneObject;

import com.samsung.smcl.vr.widgets.main.TypefaceManager;
import static com.samsung.smcl.vr.widgets.main.Utility.getId;

import com.samsung.smcl.vr.widgets.log.Log;
import com.samsung.smcl.vr.widgets.main.WidgetLib;

import static com.samsung.smcl.vr.widgets.widget.properties.JSONHelpers.getJSONColor;
import static com.samsung.smcl.vr.widgets.widget.properties.JSONHelpers.optEnum;
import static com.samsung.smcl.vr.widgets.widget.properties.JSONHelpers.optFloat;
import static com.samsung.smcl.vr.widgets.widget.properties.JSONHelpers.optInt;
import static com.samsung.smcl.vr.widgets.widget.properties.JSONHelpers.optJSONObject;
import static com.samsung.smcl.vr.widgets.widget.properties.JSONHelpers.optString;

public class TextParams implements TextContainer {
    public static TextContainer copy(TextContainer src, TextContainer dest) {
        dest.setBackGround(src.getBackGround());

        dest.setBackgroundColor(src.getBackgroundColor());

        dest.setGravity(src.getGravity());

        dest.setRefreshFrequency(src.getRefreshFrequency());

        dest.setText(src.getText());

        dest.setTextSize(src.getTextSize());

        dest.setTextColor(src.getTextColor());

        dest.setTypeface(src.getTypeface());

        return dest;
    }

    public void setFromJSON(Activity activity, JSONObject properties) {
        String backgroundResStr = optString(properties, Properties.background);
        if (backgroundResStr != null && !backgroundResStr.isEmpty()) {
            final int backgroundResId = getId(activity, backgroundResStr, "drawable");
            setBackGround(activity.getResources().getDrawable(backgroundResId));
        }

        setBackgroundColor(getJSONColor(properties, Properties.background_color, getBackgroundColor()));
        setGravity(optInt(properties, TextContainer.Properties.gravity, getGravity()));
        setRefreshFrequency(optEnum(properties, Properties.refresh_freq, getRefreshFrequency()));
        setTextColor(getJSONColor(properties, Properties.text_color, getTextColor()));
        setText(optString(properties, Properties.text, (String) getText()));
        setTextSize(optFloat(properties, Properties.text_size, getTextSize()));

        final JSONObject typefaceJson = optJSONObject(properties, Properties.typeface);

        if (typefaceJson != null) {
            try {
                Typeface typeface = WidgetLib.getTypefaceManager().getTypeface(typefaceJson);
                setTypeface(typeface);
            } catch (Throwable e) {
                Log.e(TAG, e, "Couldn't set typeface from properties: %s", typefaceJson);
            }
        }
    }

    @Override
    public Drawable getBackGround() {
        return mBackground;
    }

    @Override
    public void setBackGround(Drawable drawable) {
        mBackground = drawable;
    }

    @Override
    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    @Override
    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
    }

    @Override
    public int getGravity() {
        return mGravity;
    }

    @Override
    public void setGravity(int gravity) {
        mGravity = gravity;
    }

    @Override
    public GVRTextViewSceneObject.IntervalFrequency getRefreshFrequency() {
        return mIntervalFreq;
    }

    @Override
    public void setRefreshFrequency(GVRTextViewSceneObject.IntervalFrequency frequency) {
        mIntervalFreq = frequency;
    }

    @Override
    public CharSequence getText() {
        return mText;
    }

    @Override
    public void setText(CharSequence text) {
        mText = text;
    }

    @Override
    public int getTextColor() {
        return mTextColor;
    }

    @Override
    public void setTextColor(int color) {
        mTextColor = color;
    }

    @Override
    public float getTextSize() {
        return mTextSize;
    }

    @Override
    public void setTextSize(float size) {
        mTextSize = size;
    }

    @Override
    public void setTypeface(Typeface typeface) {
        mTypeface = typeface;
    }

    @Override
    public Typeface getTypeface() {
        return mTypeface;
    }

    @Override
    public String getTextString() {
        return mText != null ? mText.toString() : null;
    }
    private Drawable mBackground;
    private int mBackgroundColor = Color.TRANSPARENT;
    private int mGravity = Gravity.CENTER;
    private GVRTextViewSceneObject.IntervalFrequency mIntervalFreq = GVRTextViewSceneObject.IntervalFrequency.MEDIUM;
    private CharSequence mText = "";
    private int mTextColor = Color.BLACK;
    private float mTextSize = 15; // Android's default text size

    private Typeface mTypeface;

    private static final String STRING_FORMAT = "background [%s], backgroundColor [%d], " +
            "gravity [%d], textColor [%d], textSize [%f], text [%s]";

    @Override
    public String toString() {
        return String.format(STRING_FORMAT, mBackground, mBackgroundColor, mGravity, mTextColor,
                mTextSize, mText);
    }

    private static final String TAG = TextParams.class.getSimpleName();
}
