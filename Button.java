package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject.IntervalFrequency;

import android.graphics.drawable.Drawable;
import android.view.Gravity;

import com.samsung.smcl.vr.gvrf_launcher.R;

public class Button extends Widget implements TextContainer {

    public Button(GVRContext context, float width, float height) {
        super(context, width, height);
        setTexture(R.drawable.button);
        setChildrenFollowFocus(true);
        setChildrenFollowInput(true);
    }

    public Button(GVRContext context, GVRMesh mesh) {
        super(context, mesh);
    }

    public Button(GVRContext context, GVRSceneObject sceneObject,
            NodeEntry attributes) throws InstantiationException {
        super(context, sceneObject, attributes);
    }

    public Button(GVRContext context, GVRSceneObject sceneObject) {
        super(context, sceneObject);
    }

    @Override
    public Drawable getBackGround() {
        return mTextContainer.getBackGround();
    }

    @Override
    public void setBackGround(Drawable drawable) {
        mTextContainer.setBackGround(drawable);
    }

    @Override
    public int getBackgroundColor() {
        return mTextContainer.getBackgroundColor();
    }

    @Override
    public void setBackgroundColor(int color) {
        mTextContainer.setBackgroundColor(color);
    }

    @Override
    public int getGravity() {
        return mTextContainer.getGravity();
    }

    @Override
    public void setGravity(int gravity) {
        mTextContainer.setGravity(gravity);
    }

    @Override
    public void setName(final String name) {
        super.setName(name);
        if (mTextContainer instanceof TextWidget) {
            ((TextWidget) mTextContainer).setName(name + " - TextWidget");
        }
    }

    @Override
    public IntervalFrequency getRefreshFrequency() {
        return mTextContainer.getRefreshFrequency();
    }

    @Override
    public void setRefreshFrequency(IntervalFrequency frequency) {
        mTextContainer.setRefreshFrequency(frequency);
    }

    @Override
    public CharSequence getText() {
        return mTextContainer.getText();
    }

    @Override
    public void setText(CharSequence text) {
        if (text != null && text.length() > 0) {
            if (!(mTextContainer instanceof TextWidget)) {
                // Text was previously empty or null; swap our cached TextParams
                // for a new TextWidget to display the text
                final TextWidget textWidget = new TextWidget(
                        getGVRContext(), getWidth(), getHeight());
                textWidget.setRenderingOrder(GVRRenderingOrder.TRANSPARENT);
                textWidget.setGravity(Gravity.CENTER);
                textWidget.setName(getName() + " - TextWidget");
                textWidget.setTextParams(mTextContainer);
                mTextContainer = textWidget;
                addChildInner(textWidget, textWidget.getSceneObject(), -1);
                requestLayout();
            }
        } else {
            if (mTextContainer instanceof TextWidget) {
                // If the text has been cleared, swap our TextWidget for the
                // TextParams so we don't hang on to the resources
                final TextWidget textWidget = (TextWidget) mTextContainer;
                removeChild(textWidget, textWidget.getSceneObject(), false);
                mTextContainer = textWidget.getTextParams();
            }
        }
        mTextContainer.setText(text);
    }

    @Override
    public int getTextColor() {
        return mTextContainer.getTextColor();
    }

    @Override
    public void setTextColor(int color) {
        mTextContainer.setTextColor(color);
    }

    @Override
    public float getTextSize() {
        return mTextContainer.getTextSize();
    }

    @Override
    public void setTextSize(float size) {
        mTextContainer.setTextSize(size);
    }

    @Override
    public String getTextString() {
        return mTextContainer.getTextString();
    }

    @Override
    protected void onLayout() {
        // TODO: When Layout work is done, use a layout here
        // TODO: Add setLayout() method
        if (mTextContainer instanceof Widget) {
            final Widget w = (Widget) mTextContainer;
            w.reset();
            w.setPositionZ(0.025f);
        }
    }

    private TextContainer mTextContainer = new TextWidget.TextParams();
}
