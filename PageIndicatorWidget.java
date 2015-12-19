package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;

import com.samsung.smcl.utility.Log;
import com.samsung.smcl.vr.gvrf_launcher.util.Helpers;

public class PageIndicatorWidget extends LinearLayout {

    private int mCheckedChildIndex = 0;

    public PageIndicatorWidget(GVRContext context, GVRSceneObject sceneObject,
            int[] resIds, float padding, int numIndicators,
            OnTouchListener listener, float indicatorWidth,
            float indicatorHeight) {
        super(context, sceneObject);
        setDividerPadding(padding);
        addIndicatorChildren(context, resIds, numIndicators, listener,
                indicatorWidth, indicatorHeight);
    }

    public void setCheckedId(int index, int textureId) {
        if (index != mCheckedChildIndex) {
            clearCheck();
            mCheckedChildIndex = index;
            ButtonWidget child = (ButtonWidget) this.getChildren().get(
                    mCheckedChildIndex);
            if (child != null) {
                child.setState(textureId);
            }
        }
    }

    private void clearCheck() {
        ButtonWidget child = (ButtonWidget) this.getChildren().get(
                mCheckedChildIndex);
        if (child != null) {
            child.setState(0);
        }
    }

    private void addIndicatorChildren(GVRContext context, int[] resIds,
            int numIndicators, OnTouchListener listener, float indicatorWidth,
            float indicatorHeight) {
        for (int i = 0; i < numIndicators; i++) {
            ButtonWidget buttonWidget = new ButtonWidget(context,
                    indicatorWidth, indicatorHeight, resIds);
            buttonWidget.setName(Integer.toString(i));
            buttonWidget.setTouchable(true);
            buttonWidget.addTouchListener(listener);
            this.addChild(buttonWidget);
        }
    }

    private class ButtonWidget extends Widget {
        private int state = 0;
        private GVRContext mContext;
        private final String buttonWidgetTag = ButtonWidget.class
                .getSimpleName();

        private ButtonWidget(GVRContext context, final float width,
                final float height, int[] resourceIds) {
            super(context, width, height);
            this.mContext = context;
            Helpers.setTextureMaterial(mContext, this.getSceneObject(),
                    resourceIds[0], GVRRenderingOrder.TRANSPARENT);
            setAllTextures(resourceIds);
        }

        private void setState(int state) {
            if (state != this.state) {
                this.state = state;
                final GVRTexture texture = getStateTexture();
                if (texture != null) {
                    getMaterial().setMainTexture(texture);
                } else {
                    Log.d(buttonWidgetTag,
                            "setState(%d): something wrong with textures",
                            state);
                }
            }
        }

        private void setAllTextures(final int[] resourceIds) {
            Runnable r = new Runnable() {
                @Override
                public void run() {

                    for (int i = 0; i < resourceIds.length; ++i) {
                        GVRTexture texture = mContext
                                .loadTexture(new GVRAndroidResource(mContext,
                                        resourceIds[i]));
                        getMaterial().setTexture(Integer.toString(i), texture);
                        if (i == 0) {
                            getMaterial().setMainTexture(texture);
                        }
                    }
                }
            };

            runOnGlThread(r);
        }

        private GVRTexture getStateTexture() {
            return getMaterial().getTexture(Integer.toString(state));
        }
    }
}
