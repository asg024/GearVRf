package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CheckableGroup extends GroupWidget {

    // Widgets are referenced by name; we have no "resource ID" infrastructure
    public interface OnCheckChangedListener {
        <T extends Widget & Checkable> void onCheckChanged(CheckableGroup group, T checkedWidgetName, int checkableIndex);
    }

    public CheckableGroup(GVRContext context, GVRSceneObject sceneObject) {
        super(context, sceneObject);
    }

    public CheckableGroup(GVRContext context, GVRSceneObject sceneObject, NodeEntry attributes) throws InstantiationException {
        super(context, sceneObject, attributes);
    }

    public CheckableGroup(GVRContext context, float width, float height) {
        super(context, width, height);
    }

    public boolean addOnCheckChangedListener(OnCheckChangedListener listener) {
        // TODO: Call added listeners with any checked items
        synchronized (mListeners) {
            return mListeners.add(listener);
        }
    }

    public boolean removeOnCheckChangedListener(OnCheckChangedListener listener) {
        synchronized (mListeners) {
            return mListeners.remove(listener);
        }
    }

    public <T extends Widget & Checkable> boolean check(T checkableWidget) {
        // TODO: Implement
        return false;
    }

    public boolean check(int checkableIndex) {
        // TODO: Implement
        return false;
    }

    public <T extends Widget & Checkable> boolean uncheck(T checkableWidget) {
        // TODO: Implement
        return false;
    }

    public boolean uncheck(int checkableIndex) {
        // TODO: Implement
        return false;
    }

    public void clearChecks() {
        // TODO: Implement
    };

    public <T extends Widget & Checkable> List<T> getCheckedWidgets() {
        // TODO: Implement
        return null;
    };

    public <T extends Widget & Checkable> List<T> getCheckableChildren() {
        // TODO: Implement
        return null;
    }

    public int getCheckableCount() {
        // TODO: Implement
        return 0;
    }

    public void setAllowMultiCheck(boolean allow) {
        mAllowMultiCheck = allow;
    }

    public boolean getAllowMultiCheck() {
        return mAllowMultiCheck;
    }

    private boolean mAllowMultiCheck;
    private Set<OnCheckChangedListener> mListeners = new LinkedHashSet<>();
}
