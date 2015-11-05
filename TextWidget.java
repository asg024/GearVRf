package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRContext;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject.IntervalFrequency;

import android.graphics.drawable.Drawable;
import android.widget.TextView;

public class TextWidget extends Widget {

    /**
     * Shows a {@link TextView} on a {@linkplain Widget widget} with view's
     * default height and width.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * @param width
     *            Widget height, in GVRF scene graph units.
     * 
     *            Please note that your widget's size is independent of the size
     *            of the internal {@code TextView}: a large mismatch between the
     *            scene object's size and the view's size will result in
     *            'spidery' or 'blocky' text.
     * 
     * @param height
     *            Widget width, in GVRF scene graph units.
     */
    public TextWidget(GVRContext context, float width, float height) {
        this(context, width, height, null);
    }

    /**
     * Shows a {@link TextView} on a {@linkplain Widget widget} with view's
     * default height and width.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * @param width
     *            Widget height, in GVRF scene graph units.
     * 
     *            Please note that your widget's size is independent of the size
     *            of the internal {@code TextView}: a large mismatch between the
     *            scene object's size and the view's size will result in
     *            'spidery' or 'blocky' text.
     * 
     * @param height
     *            Widget width, in GVRF scene graph units.
     * @param text
     *            {@link CharSequence} to show on the textView
     */

    public TextWidget(GVRContext context, float width, float height,
            CharSequence text) {
        super(context, new GVRTextViewSceneObject(context,
                context.getActivity(), width, height, text));
        mTextViewSceneObject = (GVRTextViewSceneObject) getSceneObject();
    }

    /**
     * Shows a {@link TextView} on a {@linkplain Widget widget}.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * @param width
     *            Widget height, in GVRF scene graph units.
     * 
     *            Please note that your widget's size is independent of the size
     *            of the internal {@code TextView}: a large mismatch between the
     *            scene object's size and the view's size will result in
     *            'spidery' or 'blocky' text.
     * 
     * @param height
     *            Widget width, in GVRF scene graph units.
     * @param viewWidth
     *            Width of the {@link TextView}
     * @param viewHeight
     *            Height of the {@link TextView}
     */
    public TextWidget(GVRContext context, float width, float height,
            int viewWidth, int viewHeight) {
        this(context, width, height, viewWidth, viewHeight, null);
    }

    /**
     * Shows a {@link TextView} on a {@linkplain Widget widget}.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * @param width
     *            Widget height, in GVRF scene graph units.
     * 
     *            Please note that your widget's size, is independent of the
     *            size of the internal {@code TextView}: a large mismatch
     *            between the scene object's size and the view's size will
     *            result in 'spidery' or 'blocky' text.
     * 
     * @param height
     *            Widget width, in GVRF scene graph units.
     * @param viewWidth
     *            Width of the {@link TextView}
     * @param viewHeight
     *            Height of the {@link TextView}
     * @param text
     *            {@link CharSequence} to show on the textView
     */
    public TextWidget(GVRContext context, float width, float height,
            int viewWidth, int viewHeight, CharSequence text) {
        super(context, new GVRTextViewSceneObject(context,
                context.getActivity(), width, height, viewWidth, viewHeight,
                text));
        mTextViewSceneObject = (GVRTextViewSceneObject) getSceneObject();
    }

    public Drawable getBackGround() {
        return mTextViewSceneObject.getBackGround();
    }

    public int getGravity() {
        return mTextViewSceneObject.getGravity();
    }

    public IntervalFrequency getRefreshFrequency() {
        return mTextViewSceneObject.getRefreshFrequency();
    }

    public CharSequence getText() {
        return mTextViewSceneObject.getText();
    }

    public float getTextSize() {
        return mTextViewSceneObject.getTextSize();
    }

    public String getTextString() {
        return mTextViewSceneObject.getTextString();
    }

    public void setBackGround(Drawable drawable) {
        mTextViewSceneObject.setBackGround(drawable);
    }

    public void setBackgroundColor(int color) {
        mTextViewSceneObject.setBackgroundColor(color);
    }

    public void setGravity(int gravity) {
        mTextViewSceneObject.setGravity(gravity);
    }

    public void setRefreshFrequency(IntervalFrequency frequency) {
        mTextViewSceneObject.setRefreshFrequency(frequency);
    }

    public void setText(CharSequence text) {
        mTextViewSceneObject.setText(text);
    }

    public void setTextColor(int color) {
        mTextViewSceneObject.setTextColor(color);
    }

    public void setTextSize(float size) {
        mTextViewSceneObject.setTextSize(size);
    }

    private GVRTextViewSceneObject mTextViewSceneObject;
}
