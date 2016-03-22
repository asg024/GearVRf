package com.samsung.smcl.vr.widgets;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

import com.samsung.smcl.utility.Log;

public class PageIndicatorWidget extends LinearLayout {
    private static final int DEFAULT_STATE = 0;
    private int mCheckedChildIndex = -1;

    public PageIndicatorWidget(GVRContext context, GVRSceneObject sceneObject,
            int[] resIds, float padding, int numIndicators, int defaultPageNumber, int textureId,
            OnTouchListener listener, float indicatorWidth,
            float indicatorHeight) {
        super(context, sceneObject);
        setDividerPadding(padding);
        addIndicatorChildren(context, resIds, numIndicators, listener,
                indicatorWidth, indicatorHeight);

        // TODO: this is needed otherwise the children won't get onTouch
        // Yes, we need to fix this.
        setTouchable(false);

        setCheckedId(defaultPageNumber, textureId);
    }

    public void setCheckedId(int index, int textureId) {
        if (mCheckedChildIndex != index) {
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
        if (mCheckedChildIndex < 0) {
            return;
        }

        ButtonWidget child = (ButtonWidget) this.getChildren().get(
                mCheckedChildIndex);
        if (child != null) {
            child.setState(DEFAULT_STATE);
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
            this.addChild(buttonWidget, true);
        }
        if (numIndicators > 0) {
            requestLayout();
        }
    }

    private class ButtonWidget extends Widget {
        private int state = DEFAULT_STATE;
        private GVRContext mContext;
        private final String buttonWidgetTag = ButtonWidget.class
                .getSimpleName();

        private ButtonWidget(GVRContext context, final float width,
                final float height, int[] resourceIds) {
            super(context, width, height);
            this.mContext = context;
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
                            "setState(%d): something is wrong with textures",
                            state);
                }
            }
        }

        private void setAllTextures(final int[] resourceIds) {
            for (int i = 0; i < resourceIds.length; ++i) {
                Future<GVRTexture> futureTexture = mContext
                        .loadFutureTexture(new GVRAndroidResource(mContext,
                                resourceIds[i]));
                GVRTexture texture = null;
                try {
                    texture = futureTexture.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                setTexture(Integer.toString(i), texture);

                if (i == DEFAULT_STATE) {
                    setTexture(texture);
                }
            }
        }

        private GVRTexture getStateTexture() {
            return getMaterial().getTexture(Integer.toString(state));
        }
    }
}
