package com.samsung.smcl.vr.widgets.widget.custom;

import org.gearvrf.GVRContext;
import org.joml.Vector3f;
import org.json.JSONObject;

import com.samsung.smcl.vr.widgets.log.Log;
import com.samsung.smcl.vr.widgets.adapter.Adapter;
import com.samsung.smcl.vr.widgets.widget.animation.Animation;
import com.samsung.smcl.vr.widgets.widget.animation.AnimationFactory;
import com.samsung.smcl.vr.widgets.widget.ListWidget;
import com.samsung.smcl.vr.widgets.widget.animation.ScaleAnimation;
import com.samsung.smcl.vr.widgets.widget.Widget;

import static com.samsung.smcl.vr.widgets.widget.properties.JSONHelpers.optJSONObject;
import static com.samsung.smcl.vr.widgets.widget.properties.JSONHelpers.put;

public final class PickerWidget extends ListWidget {

    public PickerWidget(GVRContext context, final Adapter adapter, float width, float height) {
        super(context, adapter, width, height);
        addOnItemFocusListener(mItemFocusListener);

        JSONObject properties = getObjectMetadata();
        Log.d(TAG, "PickerWidget(): properties: %s", properties);
        JSONObject focusAnimationSpec = optJSONObject(properties, Properties.focus_animation, sFocusAnimationSpec);
        mFocusAnimationFactory = AnimationFactory.makeFactory(focusAnimationSpec);
        JSONObject defocusAnimationSpec = optJSONObject(properties, Properties.defocus_animation, sDefocusAnimationSpec);
        mDefocusAnimationFactory = AnimationFactory.makeFactory(defocusAnimationSpec);
    }

    public void enableFocusAnimation(boolean enable) {
        mFocusAnimationEnabled = enable;
    }

    public synchronized void show() {
        setRotation(1, 0, 0, 0);
        Log.d(TAG, "show Picker!");
    }

    public synchronized void hide() {
        if (focusedQuad != null) {
            focusedQuad.setScale(1, 1, 1);
            focusedQuad = null;
        }
        Log.d(TAG, "hide Picker!");
    }

    private enum Properties { focus_animation, defocus_animation }

    private OnItemFocusListener mItemFocusListener = new OnItemFocusListener() {
        @Override
        public void onFocus(ListWidget list, boolean focused, int dataIndex) {
            Log.i(Log.SUBSYSTEM.WIDGET, TAG, TAG + ".onFocus: " + dataIndex);

            if (mFocusAnimationEnabled || focusedQuad != null) {
                final AnimationFactory.Factory animFactory;
                if (focused) {
                    focusedQuad = list.getView(dataIndex);
                    animFactory = mFocusAnimationFactory;
                } else {
                    focusedQuad = null;
                    animFactory = mDefocusAnimationFactory;
                }
                animFactory.create(list.getView(dataIndex))
                        .setRequestLayoutOnTargetChange(false)
                        .start();
            }
        }

        @Override
        public void onLongFocus(ListWidget list, int dataIndex) {
            Log.i(Log.SUBSYSTEM.WIDGET, TAG, TAG + ".onLongFocus: " + dataIndex);
        }
    };

    private Widget focusedQuad;
    private boolean mFocusAnimationEnabled = true;
    private final AnimationFactory.Factory mFocusAnimationFactory;
    private final AnimationFactory.Factory mDefocusAnimationFactory;

    private static final float DURATION_ANIMATION_FOCUSED_SCALE_SECS = 0.2f;
    private static final float SCALE_FOCUSED_QUAD = 1.2f;

    private static JSONObject sFocusAnimationSpec = new JSONObject();
    private static JSONObject sDefocusAnimationSpec = new JSONObject();

    static {
        put(sFocusAnimationSpec, AnimationFactory.Properties.type, AnimationFactory.Type.SCALE);
        put(sFocusAnimationSpec, Animation.Properties.duration, DURATION_ANIMATION_FOCUSED_SCALE_SECS);
        put(sFocusAnimationSpec, ScaleAnimation.Properties.scale, new Vector3f(SCALE_FOCUSED_QUAD, SCALE_FOCUSED_QUAD, 1));

        put(sDefocusAnimationSpec, AnimationFactory.Properties.type, AnimationFactory.Type.SCALE);
        put(sDefocusAnimationSpec, Animation.Properties.duration, DURATION_ANIMATION_FOCUSED_SCALE_SECS);
        put(sDefocusAnimationSpec, ScaleAnimation.Properties.scale, 1f);
    }

    @SuppressWarnings("unused")
    private static final String TAG = PickerWidget.class.getSimpleName();
}
