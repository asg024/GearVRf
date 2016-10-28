package com.samsung.smcl.vr.widgets;

import java.util.LinkedHashSet;
import java.util.Set;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.json.JSONObject;

import com.samsung.smcl.utility.Log;

public abstract class CheckableButton extends Button implements Checkable {

    public interface OnCheckChangedListener {
        public void onCheckChanged(CheckableButton button, boolean checked);
    }

    public CheckableButton(GVRContext context, float width, float height) {
        super(context, width, height);
    }

    public CheckableButton(GVRContext context, GVRSceneObject sceneObject,
            NodeEntry attributes) throws InstantiationException {
        super(context, sceneObject, attributes);
        String attr = attributes.getProperty("checked");
        setChecked(attr != null && attr.compareToIgnoreCase("false") == 0);

        final JSONObject metaData = getObjectMetadata();
        setChecked(metaData.optBoolean("checked"));
    }

    public CheckableButton(GVRContext context, GVRSceneObject sceneObject) {
        super(context, sceneObject);
    }

    public boolean addOnCheckChangedListener(OnCheckChangedListener listener) {
        return mCheckChangedListeners.add(listener);
    }

    public boolean removeOnCheckChangedListener(OnCheckChangedListener listener) {
        return mCheckChangedListeners.remove(listener);
    }

    @Override
    public boolean isChecked() {
        return mIsChecked;
    }

    @Override
    public void setChecked(final boolean checked) {
        if (checked != mIsChecked) {
            mIsChecked = checked;
            updateState();

            // Avoid infinite recursions if setChecked() is called from a
            // listener
            if (mIsBroadcasting) {
                return;
            }

            mIsBroadcasting = true;

            for (OnCheckChangedListener listener : mCheckChangedListeners) {
                listener.onCheckChanged(this, mIsChecked);
            }

            mIsBroadcasting = false;
        }
    }

    @Override
    public void toggle() {
        setChecked(!mIsChecked);
    }

    protected CheckableButton(GVRContext context, GVRMesh mesh) {
        super(context, mesh);
    }

    @Override
    protected void onLayout() {
        final float left = -(getWidth() / 2);
        final float graphicOffset;
        final Widget graphic = findChildByName(".graphic");
        if (graphic != null) {
            graphicOffset = graphic.getWidth();
            graphic.setPositionX(left + (graphicOffset / 2));
            graphic.setPositionZ(.001f);
        } else {
            Log.w(TAG, "onLayout(%s): graphic element is null!", getName());
            graphicOffset = 0;
        }

        final Widget text = findChildByName(".text");
        if (text != null) {
            text.setPositionX((left + graphicOffset) + (text.getWidth() / 2));
            text.setPositionZ(.002f);
        }
    }

    @Override
    protected boolean onTouch() {
        super.onTouch();
        toggle();
        return true;
    }

    /* package */
    @Override
    WidgetState.State getState() {
        if (mIsChecked) {
            return WidgetState.State.CHECKED;
        }
        return super.getState();
    }

    private boolean mIsChecked;
    private boolean mIsBroadcasting;

    private final Set<OnCheckChangedListener> mCheckChangedListeners = new LinkedHashSet<OnCheckChangedListener>();

    private static final String TAG = CheckableButton.class.getSimpleName();
}
