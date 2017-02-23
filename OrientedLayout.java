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

    private float getSize(int[] children, Axis axis) {
        boolean calculateMaxSize = getOrientationAxis() != axis;
        float size = 0;
        for (int i = 0; i < children.length ; ++i) {
            int child = children[i];
            float sizeWithPadding = getMeasuredChildSizeWithPadding(child, axis);
            if (Float.isNaN(sizeWithPadding)) {
                // child is not measured yet
                sizeWithPadding = getChildSize(child, axis);
                if (i > 0) {
                    sizeWithPadding += getDividerPadding(axis) / 2;
                }
                if (i < children.length - 1) {
                    sizeWithPadding += getDividerPadding(axis) / 2;
                }
            }
            if (Float.isNaN(sizeWithPadding)) {
                return Float.NaN;
            }

            if (calculateMaxSize) {
                size = Math.max(size, sizeWithPadding);
            } else {
                size += sizeWithPadding;
            }
        }
        return size;
    }

    @Override
    protected float calculateWidth(int[] children) {
        return getSize(children, Axis.X);
    }

    @Override
    protected float calculateHeight(int[] children) {
        return getSize(children, Axis.Y);
    }

    @Override
    protected float calculateDepth(int[] children) {
        return getSize(children, Axis.Z);
    }

    protected Orientation mOrientation = Orientation.HORIZONTAL;
}
