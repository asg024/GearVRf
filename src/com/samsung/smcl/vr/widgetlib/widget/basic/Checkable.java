package com.samsung.smcl.vr.widgetlib.widget.basic;

/**
 * Defines an interface for Checkable Widgets
 */
public interface Checkable {
    /**
     * Interface definition for a callback to be invoked when the checked state of Widget is changed.
     */
    interface OnCheckChangedListener {
        /**
         * Called on changing the checked state of Widget
         * @param checkable checkable Widget whose state has changed.
         * @param checked  The new checked state of Widget
         */
        void onCheckChanged(Checkable checkable, boolean checked);
    }

    /**
     * Add {@link OnCheckChangedListener listener}
     * @param listener
     * @return true if the listener has been successfully added
     */
    boolean addOnCheckChangedListener(OnCheckChangedListener listener);

    /**
     * Remove {@link OnCheckChangedListener listener}
     * @param listener
     * @return true if the listener has been successfully removed
     */
    boolean removeOnCheckChangedListener(OnCheckChangedListener listener);

    /**
     * Check if Widget is checked or not
     * @return The current checked state of the Widget
     */
    boolean isChecked();

    /**
     * Change the checked state of the Widget
     * @param checked The new checked state
     */
    void setChecked(boolean checked);

    /**
     * Change the checked state of the Widget to the inverse of its current state
     */
    void toggle();
}
