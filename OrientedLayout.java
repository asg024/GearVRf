package com.samsung.smcl.vr.widgets;

import android.text.style.AbsoluteSizeSpan;

import java.util.List;

import org.joml.Vector3f;

import com.samsung.smcl.utility.Log;
/**
 * A Layout that arranges its children oriented along x, y or z axis. The orientation can be
 * set by calling setOrientation(). The default orientation is horizontal.
 */
public abstract class OrientedLayout extends AbsoluteLayout {

    /**
     * Orientation specifies if the items lay out along its local x-axis (
     * {@link Orientation#HORIZONTAL}) or it's local y-axis (
     * {@link Orientation#VERTICAL}) or it's local z-axis (
     * {@link Orientation#STACK}).
     * The default orientation is horizontal.
     */
    public enum Orientation {
        HORIZONTAL, VERTICAL, STACK
    }

    /**
     * @return {@link Orientation} of the layout.
     */
    public Orientation getOrientation() {
        return mOrientation;
    }

    /**
     * Set the {@link Orientation} of the layout. The new orientation can be rejected if it is in conflict with the
     * currently applied Gravity
     *
     * @param orientation
     *            One of the {@link Orientation} constants.
     */
    public void setOrientation(final Orientation orientation) {
        if (orientation != mOrientation) {
            mOrientation = orientation;
            invalidate();
        }
    }

    /**
     * Get the axis along the orientation
     * @return
     */
    protected Axis getOrientationAxis() {
        final Axis axis;
        switch(mOrientation) {
            case HORIZONTAL:
                axis = Axis.X;
                break;
            case VERTICAL:
                axis = Axis.Y;
                break;
            case STACK:
                axis = Axis.Z;
                break;
            default:
                Log.w(TAG, "Unsupported orientation %s", mOrientation);
                axis = Axis.X;
                break;
        }
        return axis;
    }

    protected Orientation mOrientation = Orientation.HORIZONTAL;
}
