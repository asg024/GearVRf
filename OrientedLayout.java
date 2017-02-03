package com.samsung.smcl.vr.widgets;

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

    public OrientedLayout() {
        super();
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
    public Axis getOrientationAxis() {
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

    protected OrientedLayout(final OrientedLayout rhs) {
        super(rhs);
        mOrientation = rhs.mOrientation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrientedLayout)) return false;
        if (!super.equals(o)) return false;

        OrientedLayout that = (OrientedLayout) o;

        return mOrientation == that.mOrientation;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + mOrientation.hashCode();
        return result;
    }

    protected Orientation mOrientation = Orientation.HORIZONTAL;
}
