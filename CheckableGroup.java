package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.samsung.smcl.vr.widgets.JSONHelpers.optInt;

public class CheckableGroup extends GroupWidget {

    // Widgets are referenced by name; we have no "resource ID" infrastructure
    public interface OnCheckChangedListener {
        <T extends Widget & Checkable> void onCheckChanged(CheckableGroup group, T checkedWidget,
                                                           int checkableIndex);
    }

    public CheckableGroup(GVRContext context, GVRSceneObject sceneObject) {
        super(context, sceneObject);
        init();
    }

    public CheckableGroup(GVRContext context, GVRSceneObject sceneObject, NodeEntry attributes)
            throws InstantiationException {
        super(context, sceneObject, attributes);
        init();
        String attr = attributes.getProperty(CheckableGroupProperties.checkedIndex);
        if (attr != null) {
            int checkedIndex = Integer.parseInt(attr);
            check(checkedIndex);
        }
    }

    public CheckableGroup(GVRContext context, float width, float height) {
        super(context, width, height);
        init();
    }

    @Override
    public boolean addChild(Widget child, int index, boolean preventLayout) {
        boolean added = super.addChild(child, index, preventLayout);
        if (added && child instanceof Checkable) {
            ((Checkable) child).addOnCheckChangedListener(mCheckChangedListener);
        }
        return added;
    }

    @Override
    public Layout getDefaultLayout() {
        return mDefaultLayout;
    }

    @Override
    public boolean removeChild(Widget child, boolean preventLayout) {
        boolean removed = super.removeChild(child, preventLayout);
        if (removed && child instanceof Checkable) {
            ((Checkable) child).removeOnCheckChangedListener(mCheckChangedListener);
        }
        return removed;
    }

    public <T extends Widget & Checkable> boolean addOnCheckChangedListener(OnCheckChangedListener listener) {
        final boolean added;
        synchronized (mListeners) {
            added = mListeners.add(listener);
        }
        if (added) {
            List<T> c = getCheckableChildren();
            for (int i = 0; i < c.size(); ++i) {
                listener.onCheckChanged(this, c.get(i), i);
            }
        }
        return added;
    }

    public boolean removeOnCheckChangedListener(OnCheckChangedListener listener) {
        synchronized (mListeners) {
            return mListeners.remove(listener);
        }
    }

    /**
     * Set the specified {@link Checkable} {@link Widget} as checked, if it is a child of this
     * {@link CheckableGroup} and not already checked.
     *
     * @param checkableWidget The {@code Checkable Widget} to {@linkplain Checkable#setChecked(boolean) set checked}.
     * @return {@code True} if {@code checkableWidget} is a child of this {@code CheckableGroup} and
     * was not already checked; {@code false} otherwise.
     */
    public <T extends Widget & Checkable> boolean check(T checkableWidget) {
        if (hasChild(checkableWidget)) {
            return checkInternal(checkableWidget, true);
        }

        return false;
    }

    public <T extends Widget & Checkable> boolean check(int checkableIndex) {
        List<T> children = getCheckableChildren();
        T checkableWidget = children.get(checkableIndex);
        return checkInternal(checkableWidget, true);
    }

    public <T extends Widget & Checkable> boolean uncheck(T checkableWidget) {
        if (hasChild(checkableWidget)) {
            return checkInternal(checkableWidget, false);
        }
        return false;
    }

    public <T extends Widget & Checkable> boolean uncheck(int checkableIndex) {
        List<T> children = getCheckableChildren();
        T checkableWidget = children.get(checkableIndex);
        return checkInternal(checkableWidget, false);
    }

    public <T extends Widget & Checkable> void clearChecks() {
        List<T> children = getCheckableChildren();
        for (T c : children) {
            c.setChecked(false);
        }
    }

    public <T extends Widget & Checkable> List<T> getCheckedWidgets() {
        List<T> checked = new ArrayList<>();

        for (Widget c : getChildren()) {
            if (c instanceof Checkable && ((Checkable) c).isChecked()) {
                checked.add((T) c);
            }
        }

        return checked;
    }

    public <T extends Widget & Checkable> List<Integer> getCheckedWidgetIndexes() {
        List<Integer> checked = new ArrayList<>();
        List<Widget> children = getChildren();

        final int size = children.size();
        for (int i = 0, j = -1; i < size; ++i) {
            Widget c = children.get(i);
            if (c instanceof Checkable) {
                ++j;
                if (((Checkable) c).isChecked()) {
                    checked.add(j);
                }
            }
        }

        return checked;
    }

    public <T extends Widget & Checkable> List<T> getCheckableChildren() {
        List<Widget> children = getChildren();
        ArrayList<T> result = new ArrayList<>();
        for (Widget c : children) {
            if (c instanceof Checkable) {
                result.add((T) c);
            }
        }
        return result;
    }

    public int getCheckableCount() {
        return getCheckableChildren().size();
    }

    public void setAllowMultiCheck(boolean allow) {
        mAllowMultiCheck = allow;
    }

    public boolean getAllowMultiCheck() {
        return mAllowMultiCheck;
    }

    private <T extends Widget & Checkable> boolean checkInternal(T checkableWidget, boolean check) {
        if (checkableWidget.isChecked() != check) {
            checkableWidget.setChecked(check);
            return true;
        }
        return false;
    }

    private void init() {
        JSONObject metadata = getObjectMetadata();
        int checkedIndex = optInt(metadata, CheckableGroupProperties.checkedIndex, -1);
        if (checkedIndex >= 0) {
            check(checkedIndex);
        }
        mDefaultLayout.setOrientation(OrientedLayout.Orientation.VERTICAL);
    }

    private <T extends Widget & Checkable> void onCheckChanged(Checkable checkable) {
        if (mProtectFromCheckChanged) {
            return;
        }

        mProtectFromCheckChanged = true;
        if (!mAllowMultiCheck && checkable.isChecked()) {
            List<T> children = getCheckableChildren();
            for (Widget w : children) {
                Checkable c = (Checkable) w;
                if (c != checkable) {
                    c.setChecked(false);
                }
            }
        }
        mProtectFromCheckChanged = false;

        notifyOnCheckChanged((T) checkable);
    }

    protected <T extends Widget & Checkable> void notifyOnCheckChanged(final T checkableWidget) {
        final Object[] listeners;
        synchronized (mListeners) {
            listeners = mListeners.toArray();
        }
        int index = getCheckableChildren().indexOf(checkableWidget);
        for (Object listener : listeners) {
            ((OnCheckChangedListener) listener).onCheckChanged(this, checkableWidget, index);
        }
    }

    private enum CheckableGroupProperties {
        checkedIndex
    }

    private boolean mAllowMultiCheck;
    private Set<OnCheckChangedListener> mListeners = new LinkedHashSet<>();
    private boolean mProtectFromCheckChanged;
    private LinearLayout mDefaultLayout = new LinearLayout();

    private Checkable.OnCheckChangedListener mCheckChangedListener = new Checkable.OnCheckChangedListener() {
        @Override
        public void onCheckChanged(Checkable checkable, boolean checked) {
            CheckableGroup.this.onCheckChanged(checkable);
        }
    };
}
