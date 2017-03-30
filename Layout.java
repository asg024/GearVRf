package com.samsung.smcl.vr.widgets;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.joml.Vector3f;

import com.samsung.smcl.utility.Log;
import com.samsung.smcl.utility.RuntimeAssertion;
import com.samsung.smcl.utility.Utility;
import com.samsung.smcl.vr.widgets.Layout.Axis;
import com.samsung.smcl.vr.widgets.Widget.ViewPortVisibility;

abstract public class Layout {
    /**
     * Base Layout strategy class for applying various organization/setup on layout
     *
     */
    public enum Axis {
        X, Y, Z
    }

    public enum Direction {
        FORWARD,
        BACKWARD,
        NONE
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Layout)) return false;

        Layout layout = (Layout) o;

        if (mApplyViewPort != layout.mApplyViewPort) return false;
        return mDividerPadding.equals(layout.mDividerPadding);
    }

    @Override
    public int hashCode() {
        int result = (mApplyViewPort ? 1 : 0);
        result = 31 * result + mDividerPadding.hashCode();
        return result;
    }

    interface WidgetContainer {
        Widget get(final int dataIndex);
        int size();
        float getWidthGuess(final int dataIndex);
        float getHeightGuess(final int dataIndex);
        float getDepthGuess(final int dataIndex);
        boolean isEmpty();

        /**
         * If the adapter manages the data set - true has to be returned.
         * If the items are statically added to the group widget - false
         * has to be returned. By default data set is dynamic one
         * This method must be overridden for the dynamic data set like a List.
         */
        boolean isDynamic();
    }

    protected Vector3Axis mViewPort = new Vector3Axis();
    protected boolean mApplyViewPort;
    protected Vector3Axis mDividerPadding = new Vector3Axis();
    protected WidgetContainer mContainer;
    protected Set<Integer> mMeasuredChildren = new LinkedHashSet<>();

    protected static final String TAG = Layout.class.getSimpleName();

    Layout() {
    }

    protected Layout(final Layout rhs) {
        this();
        mApplyViewPort = rhs.mApplyViewPort;
        mDividerPadding = rhs.mDividerPadding;
    }

    abstract protected Layout clone();
    abstract protected float calculateWidth(int[] children);
    abstract protected float calculateHeight(int[] children);
    abstract protected float calculateDepth(int[] children);

    /**
     * The size of the ViewPort (virtual area used by the list rendering engine)
     * If {@link Layout#mApplyViewPort} is set to true the ViewPort is applied during layout.
     * The unlimited size can be specified for the layout.
     *
     * @param enable true to apply the view port, false - otherwise, all items are rendered in the list even if they
     * occupy larger space  than the container size is.
     */
    public void enableViewPort(boolean enable) {
        if (mApplyViewPort != enable) {
            mApplyViewPort = enable;
            invalidate();
        }
    }

    /**
     * @return true if ViewPort is enabled, false - otherwise
     */

    public boolean isViewPortEnabled() {
        return mApplyViewPort;
    }

    /**
     * Called when the layout is applied to the data
     * @param container WidgetContainer to access the widgets in the layout
     * @param viewPort View port for data set
     */
    void onLayoutApplied(final WidgetContainer container, final Vector3Axis viewPort) {
        if (mContainer != container && mViewPort != viewPort) {
            mContainer = container;
            mViewPort = viewPort;
            invalidate();
        }
    }

    /**
     * Check if the item is at least partially visible in view port
     * @param dataIndex data index
     * @return true is the item is at least partially visible, false - otherwise
     */
    abstract boolean inViewPort(final int dataIndex);

    /**
     * Invalidate layout setup.
     */
    public void invalidate() {
        Log.d(TAG, "invalidate all [%d]", mMeasuredChildren.size());
        mMeasuredChildren.clear();
    }

    /**
     * Invalidate the item in layout
     * @param dataIndex data index
     */
    void invalidate(final int dataIndex) {
        Log.d(TAG, "invalidate [%d]", dataIndex);
        mMeasuredChildren.remove(dataIndex);
    }

    boolean isInvalidated() {
        return mMeasuredChildren.isEmpty();
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Shift all items in layout by offset
     * @param offset
     * @param axis {@link Axis}
     */
    protected abstract void shiftBy(final float offset, final Axis axis);

    /**
     * Calculate the child size along the axis
     * @param dataIndex data index
     * @param axis {@link Axis}
     * @return child size
     */
    protected float getChildSize(final int dataIndex, final Axis axis) {
        float size = 0;
        switch (axis) {
            case X:
                size = mContainer.getWidthGuess(dataIndex);
                if (Float.isNaN(size)) {
                    Widget child = mContainer.get(dataIndex);
                    if (child != null) {
                        size = child.getLayoutWidth();
                    }
                }
                break;
            case Y:
                size = mContainer.getHeightGuess(dataIndex);
                if (Float.isNaN(size)) {
                    Widget child = mContainer.get(dataIndex);
                    if (child != null) {
                        size = child.getLayoutHeight();
                    }
                }
                break;
            case Z:
                size = mContainer.getDepthGuess(dataIndex);
                if (Float.isNaN(size)) {
                    Widget child = mContainer.get(dataIndex);
                    if (child != null) {
                        size = child.getLayoutDepth();
                    }
                }
                break;
            default:
                throw new RuntimeAssertion("Bad axis specified: %s", axis);
        }
        return size;
    }

    /**
     * Calculate the layout container size along the axis
     * @param axis {@link Axis}
     * @return size
     */
    protected float getAxisSize(final Axis axis) {
        float size =  mViewPort == null ? 0 : mViewPort.get(axis);
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "getAxisSize for %s %f mViewPort = %s", axis, size, mViewPort);
        return size;
    }

    /**
     * Get the child size with padding
     * @param dataIndex
     * @param axis {@link Axis}
     * @return child size with padding
     */
    protected abstract float getMeasuredChildSizeWithPadding(final int dataIndex, final Axis axis);

    /**
     * @param axis {@link Axis}
     * @return The padding between child objects that is set by {@link Layout#setDividerPadding }.
     */
    public float getDividerPadding(final Axis axis) {
        return mDividerPadding.get(axis);
    }

    /**
     * Set the amount of padding between child objects.
     * @param axis {@link Axis}
     * @param padding
     */
    public void setDividerPadding(float padding, final Axis axis) {
        if (!Utility.equal(mDividerPadding.get(axis), padding)) {
            mDividerPadding.set(padding, axis);
            invalidate();
        }
    }

    /**
     * Calculate the child size along the axis and measure the offset inside the
     * layout container
     * @param dataIndex of child in Container
     * @return true item fits the container, false - otherwise
     */
    synchronized Widget measureChild(final int dataIndex, boolean calculateOffset) {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "measureChild dataIndex = %d", dataIndex);

        Widget widget = mContainer.get(dataIndex);
        if (widget != null) {
            mMeasuredChildren.add(dataIndex);
        }
        return widget;
    }

    /**
     * Calculate the child size along the axis and measure the offset inside the
     * layout container
     * @param dataIndex of child in Container
     * @return true item fits the container, false - otherwise
     */
    synchronized Widget measureChild(final int dataIndex) {
        return measureChild(dataIndex, true);
    }


    /**
     * Measure the children in the specified direction along the specified axis
     * @param measuredChildren is the list of measured children
     * @param axis
     * @return size occupied by the measured children along the axis
     */
    protected abstract float preMeasureNext(final List<Widget> measuredChildren,
            final Axis axis, final Direction direction);

    /**
     * @return The index of center child in layout. The item index is
     * used to get the item from the {@link WidgetContainer}
     */
    protected abstract int getCenterChild();

    /**
     * Get the {@link Direction} the layout content has to be shifted along the axis
     * to centralize the child with specified dataIndex
     * @param dataIndex
     * @param axis
     * @return {@link Direction#BACKWARD} or {@link Direction#FORWARD}.
     * {@link Direction#NONE} is returned if the layout cannot give the direction
     * or the layout does not have to be shifted along the specified axis.
     */
    protected abstract Direction getDirectionToChild(final int dataIndex, final Axis axis);

    /**
     * Get the distance to the child the layout content has to be shifted along the axis
     * to have the child in the center.
     * @param dataIndex
     * @param axis
     * @return {@link Float#NaN} is returned if the distance cannot be computed (basically
     * extra measurements have to be done) or if the item is already in center along the
     * specified axis.
     */
    protected abstract float getDistanceToChild(final int dataIndex, final Axis axis);

    /**
     * Measure all children from container if needed
     * @param measuredChildren the list of measured children
     * measuredChildren list can be passed as null if it's not needed to
     * create the list of the measured items
     */
    protected void measureAll(List<Widget> measuredChildren) {
        if (isInvalidated()) {
            invalidate();
            for (int i = 0; i < mContainer.size(); ++i) {
                Widget child = measureChild(i, false);
                if (measuredChildren != null  && child != null) {
                    measuredChildren.add(child);
                }
            }
            postMeasurement();
        }
    }

    /**
     * Return true if the child is measured and can be layout
     * @param dataIndex
     * @return
     */
    synchronized protected boolean isChildMeasured(final int dataIndex) {
        return mMeasuredChildren.contains(dataIndex);
    }

    /**
     * Measure the children from container until the layout is full (if ViewPort is enabled)
     * @param centerDataIndex of the item in center
     * @param measuredChildren the list of measured children
     * measuredChildren list can be passed as null if it's not needed to
     * create the list of the measured items
     */
    protected void measureUntilFull(final int centerDataIndex, final Collection<Widget> measuredChildren) {
        boolean inBounds = true;
        for (int i = centerDataIndex; i < mContainer.size() && inBounds; ++i) {
            Widget view = measureChild(i);
            inBounds = inViewPort(i);
            if (!inBounds) {
                invalidate(i);
            } else {
                inBounds = postMeasurement() || !isViewPortEnabled();
            }
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "measureUntilFull: measureChild view = %s " +
                            "isBounds = %b isViewPortEnabled=%b dataIndex = %d layout = %s",
                    view == null ? "<null>" : view.getName(), inBounds, isViewPortEnabled(),
                    i, this);

            if (measuredChildren != null && view != null && inBounds) {
                measuredChildren.add(view);
            }
        }
    }

    /**
     * Compute the offset and apply layout parameters to all measured items
     * @return true if all items fit the container, false - otherwise
     *
     */
    protected abstract boolean postMeasurement();

    /**
     * Position the child inside the layout based on the offset and axis-s factors
     * @param dataIndex data index
     * false - otherwise
     */
    protected void layoutChild(final int dataIndex) {
        if (!mContainer.isDynamic()) {
            boolean visibleInLayout = !isViewPortEnabled() || inViewPort(dataIndex);
            ViewPortVisibility visibility = visibleInLayout ?
                    ViewPortVisibility.FULLY_VISIBLE : ViewPortVisibility.INVISIBLE;
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onLayout: child with dataId [%d] viewportVisibility = %s",
                    dataIndex, visibility);
            Widget childWidget = mContainer.get(dataIndex);
            if (childWidget != null) {
                childWidget.setViewPortVisibility(visibility);
            }
        }
    }

    /**
     * Reset child layout
     * @param dataIndex data index
     */
    protected abstract void resetChildLayout(final int dataIndex);

    /**
     * Layout children inside the layout container
     */
    protected void layoutChildren() {

        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "layoutChildren [%d] layout = %s",
                mMeasuredChildren.size(), this);

        Set<Integer> copySet = new HashSet<>(mMeasuredChildren);
        for (int nextMeasured: copySet) {
            layoutChild(nextMeasured);
        }
    }
}

/**
 * ViewPort class basically define the layout container dimensions.
 */
class Vector3Axis extends Vector3f {
    Vector3Axis(final float x, final float y, final float z) {
        super(x, y, z);
    }

    Vector3Axis(Vector3f v) {
        super(v.x, v.y, v.z);
    }

    Vector3Axis() {
        super();
    }

    public float get(Axis axis) {
        switch (axis) {
            case X:
                return x;
            case Y:
                return y;
            case Z:
                return z;
            default:
                throw new RuntimeAssertion("Bad axis specified: %s", axis);
        }
    }

    public void set(float val, Axis axis) {
        switch (axis) {
            case X:
                x = val;
                break;
            case Y:
                y = val;
                break;
            case Z:
                z = val;
                break;
            default:
                throw new RuntimeAssertion("Bad axis specified: %s", axis);
        }
    }

    public boolean isNaN() {
        return Float.isNaN(x) && Float.isNaN(y) && Float.isNaN(z);
    }

    public boolean isInfinite() {
        return Float.isInfinite(x) && Float.isInfinite(y) && Float.isInfinite(z);
    }

    public Vector3Axis delta(Vector3f v) {
        Vector3Axis ret = new Vector3Axis(Float.NaN, Float.NaN, Float.NaN);
        if (x != Float.NaN && v.x != Float.NaN && !Utility.equal(x, v.x)) {
            ret.set(x - v.x, Axis.X);
        }
        if (y != Float.NaN && v.y != Float.NaN && !Utility.equal(y, v.y)) {
            ret.set(y - v.y, Axis.Y);
        }
        if (z != Float.NaN && v.z != Float.NaN && !Utility.equal(z, v.z)) {
            ret.set(z - v.z, Axis.Z);
        }
        return ret;
    }
}
