package com.samsung.smcl.vr.widgets;

import com.samsung.smcl.utility.Log;
import com.samsung.smcl.utility.RuntimeAssertion;
import com.samsung.smcl.utility.Utility;
import com.samsung.smcl.vr.widgets.Widget.ViewPortVisibility;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        return mViewPort.equals(layout.mViewPort)
                && Utility.equal(mDividerPadding, layout.mDividerPadding)
                && Utility.equal(mOffset, layout.mOffset);
    }

    @Override
    public int hashCode() {
        int result = mViewPort.hashCode();
        result = 31 * result + mDividerPadding.hashCode();
        result = 31 * result + mOffset.hashCode();
        return result;
    }

    public interface WidgetContainer {
        Widget get(final int dataIndex);
        int getDataIndex(Widget widget);
        int size();

        float getBoundsWidth();
        float getBoundsHeight();
        float getBoundsDepth();
        boolean isEmpty();

        /**
         * If the adapter manages the data set - true has to be returned.
         * If the items are statically added to the group widget - false
         * has to be returned. By default data set is dynamic one
         * This method must be overridden for the dynamic data set like a List.
         */
        boolean isDynamic();
        void onLayoutChanged(Layout layout);

    }

    protected ViewPort mViewPort = new ViewPort();
    protected Vector3Axis mDividerPadding = new Vector3Axis();
    protected Vector3Axis mOffset = new Vector3Axis();
    protected WidgetContainer mContainer;
    protected Set<Integer> mMeasuredChildren = new LinkedHashSet<>();

    protected static final String TAG = Layout.class.getSimpleName();

    private static final String pattern = "\nLayout attributes====== divider_padding = %s " +
            "offset = %s viewport [%s]";

    public String toString() {
        return super.toString() + String.format(pattern,
                mDividerPadding, mOffset, mViewPort);
    }

    Layout() {
    }

    protected Layout(final Layout rhs) {
        this();
        mViewPort = rhs.mViewPort;
        mDividerPadding = rhs.mDividerPadding;
        mOffset = rhs.mOffset;
    }

    abstract protected Layout clone();
    abstract protected float calculateWidth(int[] children);
    abstract protected float calculateHeight(int[] children);
    abstract protected float calculateDepth(int[] children);

    /**
     * The size of the ViewPort (virtual area used by the list rendering engine)
     * If {@link Layout#mViewPort} is set to true the ViewPort is applied during layout.
     * The unlimited size can be specified for the layout.
     *
     * @param enable true to apply the view port, false - otherwise, all items are rendered in the list even if they
     * occupy larger space  than the container size is.
     */
    public void enableClipping(boolean enable) {
        if (mViewPort.isClippingEnabled() != enable) {
            mViewPort.enableClipping(enable);
            if (mContainer != null) {
                mContainer.onLayoutChanged(this);
            }
        }
    }

    /**
     * @return true if clipping is enabled, false - otherwise
     */

    public boolean isClippingEnabled() {
        return mViewPort.isClippingEnabled();
    }

    /**
     * Called when the layout is applied to the data
     * @param container WidgetContainer to access the widgets in the layout
     * @param viewPortSize View port for data set
     */
    void onLayoutApplied(final WidgetContainer container, final Vector3Axis viewPortSize) {
        mContainer = container;
        mViewPort.setSize(viewPortSize);
        if (mContainer != null) {
            mContainer.onLayoutChanged(this);
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
        synchronized (mMeasuredChildren) {
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "invalidate all [%d]", mMeasuredChildren.size());
            mMeasuredChildren.clear();
        }
    }

    /**
     * Invalidate the item in layout
     * @param dataIndex data index
     */
    void invalidate(final int dataIndex) {
        synchronized (mMeasuredChildren) {
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "invalidate [%d]", dataIndex);
            mMeasuredChildren.remove(dataIndex);
        }
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Shift all items in layout by offset
     * @param offset
     * @param axis {@link Axis}
     */
    protected void shiftBy(final float offset, final Axis axis) {
        mViewPort.shift(offset, axis);
    }

    /**
     * Calculate the child size along the axis
     * @param dataIndex data index
     * @param axis {@link Axis}
     * @return child size
     */
    protected float getChildSize(final int dataIndex, final Axis axis) {
        float size = 0;
        Widget child = mContainer.get(dataIndex);
        if (child != null) {
            switch (axis) {
                case X:
                    size = child.getLayoutWidth();
                    break;
                case Y:
                    size = child.getLayoutHeight();
                    break;
                case Z:
                    size = child.getLayoutDepth();
                    break;
                default:
                    throw new RuntimeAssertion("Bad axis specified: %s", axis);
            }
        }
        return size;
    }

    /**
     * Calculate the layout container size along the axis
     * @param axis {@link Axis}
     * @return size
     */
    public float getSize(final Axis axis) {
        float size = 0;
        if (mViewPort != null && mViewPort.isClippingEnabled(axis)) {
            size = mViewPort.get(axis);
        } else if (mContainer != null) {
            size = getSizeImpl(axis);
        }
        return size;
    }


    protected float getSizeImpl(final Axis axis) {
        float size = 0;
        switch (axis) {
            case X:
                size = mContainer.getBoundsWidth();
                break;
            case Y:
                size = mContainer.getBoundsHeight();
                break;
            case Z:
                size = mContainer.getBoundsDepth();
                break;
        }
        return size;
    }

    /**
     * Get viewport size along the axis
     * @param axis {@link Axis}
     * @return size
     */
    protected float getViewPortSize(final Axis axis) {
        float size =  mViewPort == null ? 0 : mViewPort.get(axis);
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "getViewPortSize for %s %f mViewPort = %s", axis, size, mViewPort);
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
     * Get the total size with padding
     * @param axis {@link Axis}
     * @return total size with padding
     * /
    protected abstract float getTotalSizeWithPadding(final Axis axis);

    /**
     * @param axis {@link Axis}
     * @return The padding between child objects that is set by {@link Layout#setDividerPadding }.
     */
    public float getDividerPadding(final Axis axis) {
        return mDividerPadding.get(axis);
    }

    /**
     * @param axis {@link Axis}
     * @return The offset between child objects and parent that is set by {@link Layout#setOffset }.
     */
    public float getOffset(final Axis axis) {
        return mOffset.get(axis);
    }

    /**
     * Set the amount of padding between child objects.
     * @param axis {@link Axis}
     * @param padding
     */
    public void setDividerPadding(float padding, final Axis axis) {
        if (!Utility.equal(mDividerPadding.get(axis), padding)) {
            mDividerPadding.set(padding, axis);
            if (mContainer != null) {
                mContainer.onLayoutChanged(this);
            }
        }
    }

    /**
     * Set the amount of offset between child objects and parent.
     * @param axis {@link Axis}
     * @param offset
     */
    public void setOffset(float offset, final Axis axis) {
        if (!Utility.equal(mOffset.get(axis), offset)) {
            mOffset.set(offset, axis);
            if (mContainer != null) {
                mContainer.onLayoutChanged(this);
            }
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
            synchronized (mMeasuredChildren) {
                mMeasuredChildren.add(dataIndex);
            }
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
     * @return true if the layout was recalculated, otherwise - false
     */
    protected boolean measureAll(List<Widget> measuredChildren) {
        boolean changed = false;
        for (int i = 0; i < mContainer.size(); ++i) {

            if (!isChildMeasured(i)) {
                Widget child = measureChild(i, false);
                if (child != null) {
                    if (measuredChildren != null) {
                        measuredChildren.add(child);
                    }
                    changed = true;
                }
            }
        }
        if (changed) {
            postMeasurement();
        }
        return changed;
    }

    /**
     * Return true if the child is measured and can be layout
     * @param dataIndex
     * @return
     */
    synchronized protected boolean isChildMeasured(final int dataIndex) {
        boolean ret;
        synchronized (mMeasuredChildren) {
            ret = mMeasuredChildren.contains(dataIndex);
        }

        return ret;
    }

    /**
     * Measure the children from container until the layout is full (if ViewPort is enabled)
     * @param centerDataIndex of the item in center
     * @param measuredChildren the list of measured children
     * measuredChildren list can be passed as null if it's not needed to
     * create the list of the measured items
     */
    protected boolean measureUntilFull(int centerDataIndex, final Collection<Widget> measuredChildren) {
        if (centerDataIndex == -1) {
            centerDataIndex = 0;
        }

        boolean firstChildInBoundsFound = false;

        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "measureUntilFull: centerDataIndex view = %d mContainer.size() = %d",
                centerDataIndex, mContainer.size());

        for (int i = centerDataIndex; i >= 0 && i < mContainer.size();) {
            boolean inBounds = true;
            boolean childChanged = !isChildMeasured(i);
            Widget view = childChanged ? measureChild(i) : mContainer.get(i);

            if (!mViewPort.isClippingEnabled()) {
                if (childChanged) {
                    postMeasurement();
                }
            } else {
                inBounds = inViewPort(i);
                if (!inBounds) {
                    if (childChanged) {
                        invalidate(i);
                    }
                    childChanged = !childChanged;
                }
            }

            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "measureUntilFull: measureChild view = %s " +
                            "isBounds = %b  dataIndex = %d childChanged = %b, viewport = %s",
                    view == null ? "<null>" : view.getName(), inBounds,
                    i, childChanged, mViewPort);

            if (view != null && inBounds) {
                if (measuredChildren != null) {
                    measuredChildren.add(view);
                }
            }

            boolean directionIsChanged = changeDirection(i, centerDataIndex, inBounds);

            i = getNextIndex(i, centerDataIndex, directionIsChanged);

            firstChildInBoundsFound = firstChildInBoundsFound || inBounds;

            if (directionIsChanged) {
                inBounds = true;
            }

            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "measureUntilFull: directionIsChanged = %b inBounds = %b",
                    directionIsChanged, inBounds);

            if (mViewPort.isClippingEnabled() && childChanged) {
                inBounds = postMeasurement();
            }

            if (firstChildInBoundsFound && !inBounds) {
                break;
            }
        }
        return true;
    }

    protected int getNextIndex(int currentIndex, int centerIndex, boolean changeDirection) {
        return ++currentIndex;
    }

    protected boolean changeDirection(int currentIndex, int centerIndex, boolean inBounds) {
        return false;
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
     */
    protected void layoutChild(final int dataIndex) {
        Widget child = mContainer.get(dataIndex);
        if (child != null) {
            float offset = mOffset.get(Axis.X);
            if (!Utility.equal(offset, 0)) {
                updateTransform(child, Axis.X, offset);
            }

            offset = mOffset.get(Axis.Y);
            if (!Utility.equal(offset, 0)) {
                updateTransform(child, Axis.Y, offset);
            }

            offset = mOffset.get(Axis.Z);
            if (!Utility.equal(offset, 0)) {
                updateTransform(child, Axis.Z, offset);
            }
       }
    }

    /**
     * Do post exam of child inside the layout after it has been positioned in parent
     * @param dataIndex data index
     */
    protected void postLayoutChild(final int dataIndex) {
        if (!mContainer.isDynamic()) {
            boolean visibleInLayout = !mViewPort.isClippingEnabled() || inViewPort(dataIndex);
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

    protected float getFactor(Axis axis) {
        float factor = 0;
        switch(axis) {
            case X:
                factor = 1;
                break;
            case Y:
                factor = -1;
                break;
            case Z:
                factor = -1;
                break;
        }
        return factor;
    }

    protected void updateTransform(Widget child, Axis axis, float offset) {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "updateTransform [%s], offset = [%f], axis = [%s]",
                child.getName(), offset, axis);

        if (Float.isNaN(offset)) {
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "Position is NaN" + axis);
        } else {
            offset -= mViewPort.getShift(axis);
            offset *= getFactor(axis);
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "child [%s] position = [%f, %f, %f] offset = %f, axis = %s",
                    child.getName(), child.getPositionX(), child.getPositionY(), child.getPositionZ(),
                    offset, axis);

            switch (axis) {
                case X:
                    if (!Utility.equal(child.getPositionX(), offset)) {
                        child.setPositionX(offset);
                    }
                    break;
                case Y:
                    if (!Utility.equal(child.getPositionY(), offset)) {
                        child.setPositionY(offset);
                    }
                    break;
                case Z:
                    if (!Utility.equal(child.getPositionZ(), offset)) {
                        child.setPositionZ(offset);
                    }
                    break;
                default:
                    throw new RuntimeAssertion("Bad axis specified: %s", axis);

            }
            child.onTransformChanged();
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

        Set<Integer> copySet;
        synchronized (mMeasuredChildren) {
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "layoutChildren [%d] layout = %s",
                    mMeasuredChildren.size(), this);
            copySet = new HashSet<>(mMeasuredChildren);
        }
        for (int nextMeasured: copySet) {
            Widget child = mContainer.get(nextMeasured);
            if (child != null) {
                child.preventTransformChanged(true);
                layoutChild(nextMeasured);
                postLayoutChild(nextMeasured);
                child.preventTransformChanged(false);
            }

        }
    }

    public static class ViewPort {
        private class ViewPortDimension {
            float size;
            float shift;
            boolean clipping;
            ViewPortDimension(float s, float sh, boolean c) {
                size = s;
                shift = sh;
                clipping = c;
            }

            private static final String pattern = "\nViewPortDimension: size = %f, shift = %f, clip = %b";

            public String toString() {
                return String.format(pattern, size, shift, clipping);
            }

            @Override
            public boolean equals(Object another) {
                boolean ret = false;
                if (another instanceof ViewPortDimension) {
                    ViewPortDimension d = (ViewPortDimension)another;
                    ret = d.size == size && d.shift == shift && d.clipping == clipping;
                }
                return ret;
            }
        }

        private Map<Axis, ViewPortDimension> m3Dimensions = new HashMap<>(3);

        ViewPort(Vector3Axis size, Vector3Axis shift) {
            ViewPortDimension dx = new ViewPortDimension(size.x, shift.x, false);
            ViewPortDimension dy = new ViewPortDimension(size.y, shift.y, false);
            ViewPortDimension dz = new ViewPortDimension(size.z, shift.z, false);
            m3Dimensions.put(Axis.X, dx);
            m3Dimensions.put(Axis.Y, dy);
            m3Dimensions.put(Axis.Z, dz);
        }

        ViewPort(Vector3Axis size) {
            this(size, new Vector3Axis());
        }

        ViewPort() {
            this(new Vector3Axis(), new Vector3Axis());
        }

        @Override
        public boolean equals(Object another) {
            boolean ret = false;
            if (another instanceof ViewPort) {
                ViewPort vp = (ViewPort)another;
                ret = vp.m3Dimensions.get(Axis.X).equals(m3Dimensions.get(Axis.X)) &&
                      vp.m3Dimensions.get(Axis.Y).equals(m3Dimensions.get(Axis.Y)) &&
                      vp.m3Dimensions.get(Axis.Z).equals(m3Dimensions.get(Axis.Z));
            }
            return ret;
        }

        boolean isClippingEnabled(Axis axis) {
            return m3Dimensions.get(axis).clipping;
        }

        boolean isClippingEnabled() {
            return m3Dimensions.get(Axis.X).clipping &&
                   m3Dimensions.get(Axis.Y).clipping &&
                   m3Dimensions.get(Axis.Z).clipping;
        }

        void enableClipping(boolean enable) {
            ViewPortDimension dx = m3Dimensions.get(Axis.X);
            ViewPortDimension dy = m3Dimensions.get(Axis.Y);
            ViewPortDimension dz = m3Dimensions.get(Axis.Z);
            dx.clipping = enable;
            dy.clipping = enable;
            dz.clipping = enable;
            m3Dimensions.put(Axis.X, dx);
            m3Dimensions.put(Axis.Y, dy);
            m3Dimensions.put(Axis.Z, dz);
        }

        void enableClipping(boolean enable, Axis axis) {
            ViewPortDimension d = m3Dimensions.get(axis);
            d.clipping = enable;
            m3Dimensions.put(axis, d);
        }

        void shift(Vector3Axis shift) {
            ViewPortDimension dx = m3Dimensions.get(Axis.X);
            ViewPortDimension dy = m3Dimensions.get(Axis.Y);
            ViewPortDimension dz = m3Dimensions.get(Axis.Z);
            dx.shift += shift.x;
            dy.shift += shift.y;
            dz.shift += shift.z;
            m3Dimensions.put(Axis.X, dx);
            m3Dimensions.put(Axis.Y, dy);
            m3Dimensions.put(Axis.Z, dz);
        }

        void shift(float shift, Axis axis) {
            ViewPortDimension d = m3Dimensions.get(axis);
            d.shift += shift;
            m3Dimensions.put(axis, d);
        }

        float getShift(Axis axis) {
            return m3Dimensions.get(axis).shift;
        }

        void setSize(Vector3Axis size) {
            ViewPortDimension dx = m3Dimensions.get(Axis.X);
            ViewPortDimension dy = m3Dimensions.get(Axis.Y);
            ViewPortDimension dz = m3Dimensions.get(Axis.Z);
            dx.size = size.x;
            dy.size = size.y;
            dz.size = size.z;
            m3Dimensions.put(Axis.X, dx);
            m3Dimensions.put(Axis.Y, dy);
            m3Dimensions.put(Axis.Z, dz);
        }

        void setSize(float size, Axis axis) {
            ViewPortDimension d = m3Dimensions.get(axis);
            d.size = size;
            m3Dimensions.put(axis, d);
        }

        float get(Axis axis) {
            return m3Dimensions.get(axis).size;
        }

        private static final String pattern = "\nViewPort: x = %s, y = %s, z = %s";

        public String toString() {
            return String.format(pattern,
                    m3Dimensions.get(Axis.X), m3Dimensions.get(Axis.Y), m3Dimensions.get(Axis.Z));
        }
    }
}