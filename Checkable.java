package com.samsung.smcl.vr.widgets;

public interface Checkable {
    public interface OnCheckChangedListener {
        public void onCheckChanged(Checkable checkable, boolean checked);
    }

    boolean addOnCheckChangedListener(OnCheckChangedListener listener);

    boolean removeOnCheckChangedListener(OnCheckChangedListener listener);

    public boolean isChecked();

    public void setChecked(boolean checked);

    public void toggle();
}
