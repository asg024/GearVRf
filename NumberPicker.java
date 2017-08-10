package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRSceneObject;

import android.graphics.Color;
import android.view.Gravity;

import com.samsung.smcl.utility.Log;
import com.samsung.smcl.vr.gvrf_launcher.R;
import com.samsung.smcl.vr.gvrf_launcher.TouchManager;
import com.samsung.smcl.vr.gvrf_launcher.util.Helpers;

public class NumberPicker extends GVRSceneObject {

    public interface OnValueChangeListener {
        public void onValueChange(int oldValue, int newValue);
    }

    public NumberPicker(GVRContext gvrContext,
            float width, float height) {
        super(gvrContext, width, height);
        mWidgetWrapper = new GroupWidget(gvrContext, this);
        mWidgetWrapper.applyLayout(new AbsoluteLayout());

        final float selectionHeight = height / 3;
        final float buttonHeight = selectionHeight / 2;
        Log.d(TAG, "NumberPicker: height %.2f, childHeight: %.2f", height,
              selectionHeight);
        mUpButton = makeButton(buttonHeight, R.drawable.up_arrow_circle);
        mDownButton = makeButton(buttonHeight, R.drawable.down_arrow_circle);

        mSelection = new TextWidget(gvrContext, width, selectionHeight, Integer.toString(mValue));
        mSelection.setGravity(Gravity.CENTER);
        mSelection.setTextSize(20);
        mSelection.setTextColor(Color.BLACK);
        mSelection.setBackgroundColor(Color.WHITE);

        mUpButton.getTransform().setPositionY(selectionHeight);
        mDownButton.getTransform().setPositionY(-selectionHeight);

        TouchManager.get(gvrContext).makeTouchable(mUpButton, mTouchHandlerUp);
        TouchManager.get(gvrContext).makeTouchable(mDownButton, mTouchHandlerDown);

        addChildObject(mUpButton);
        addChildObject(mDownButton);
        mWidgetWrapper.addChild((Widget) mSelection);
    }

    private final TouchManager.OnTouch mTouchHandlerUp = new TouchManager.OnTouch() {
        @Override
        public boolean touch(
                GVRSceneObject sceneObject) {
            setValue(getValue() + 1);
            return true;
        }
    };
    private final TouchManager.OnTouch mTouchHandlerDown = new TouchManager.OnTouch() {
        @Override
        public boolean touch(
                GVRSceneObject sceneObject) {
            setValue(getValue() - 1);
            return true;
        }
    };

    public int getMinValue() {
        return mMinValue;
    }

    public void setMinValue(int minValue) {
        mMinValue = minValue;
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public void setMaxValue(int maxValue) {
        mMaxValue = maxValue;
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        if (value != mValue && value >= mMinValue && value <= mMaxValue) {
            final int oldValue = mValue;
            mValue = value;
            mSelection.setText(Integer.toString(mValue));
            if (mListener != null) {
                try {
                    mListener.onValueChange(oldValue, mValue);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public OnValueChangeListener getValueChangeListener() {
        return mListener;
    }

    public void setValueChangeListener(OnValueChangeListener listener) {
        mListener = listener;
    }

    private GVRSceneObject makeButton(final float size, final int bitmapId) {
        final GVRContext gvrContext = getGVRContext();

        GVRSceneObject button = new GVRSceneObject(gvrContext, size, size);
        Helpers.setTextureMaterial(gvrContext, button, bitmapId,
                                   GVRRenderingOrder.TRANSPARENT);

        return button;
    }

    private final GVRSceneObject mUpButton;
    private final GVRSceneObject mDownButton;
    private final TextWidget mSelection;
    private final GroupWidget mWidgetWrapper;
    private int mMinValue = 0;
    private int mMaxValue = Integer.MAX_VALUE;
    private int mValue;
    private OnValueChangeListener mListener;

    private static final String TAG = NumberPicker.class.getSimpleName();
}
