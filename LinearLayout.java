package com.samsung.smcl.vr.widgets;

import java.util.List;

import org.gearvrf.GVRContext;

import com.samsung.smcl.utility.Log;

/**
 * A widget group class that lays its children out along its local x-axis (
 * {@link Orientation#HORIZONTAL}) or it's local y-axis (
 * {@link Orientation#VERTICAL}). The default orientation is horizontal.
 */
public class LinearLayout extends GroupWidget {

    public enum Orientation {
        HORIZONTAL, VERTICAL
    }

    public LinearLayout(GVRContext gvrContext, float width, float height) {
        super(gvrContext, width, height);
        mWidth = width;
        mHeight = height;
        setOrientation(Orientation.HORIZONTAL);
        mLayoutStrategy = getLayoutStrategy();
    }

    @Override
    public boolean addChild(final Widget child) {
        if (super.addChild(child)) {
            layout();
            return true;
        }
        return false;
    }

    @Override
    public boolean removeChild(final Widget child) {
        if (super.removeChild(child)) {
            layout();
            return true;
        }
        return false;
    }

    /**
     * @return The padding between child objects.
     */
    public float getDividerPadding() {
        return mDividerPadding;
    }

    /**
     * Set the amount of padding between child objects.
     * 
     * @param padding
     */
    public void setDividerPadding(float padding) {
        mDividerPadding = padding;
    }

    /**
     * @return {@link Orientation} of the layout.
     */
    public Orientation getOrientation() {
        return mOrientation;
    }

    /**
     * Set the {@link Orientation} of the layout.
     * 
     * @param orientation
     *            One of the {@link Orientation} constants.
     */
    public void setOrientation(final Orientation orientation) {
        if (orientation != mOrientation) {
            mOrientation = orientation;
            layout();
        }
    }

    /**
     * Layout the object's children in the specified
     * {@linkplain #setOrientation(Orientation) orientation}.
     * <p>
     * <b>NOTE:</b> Currently, this can be called by client code. Might want to
     * change that.
     */
    @Override
    protected void layout() {
        Log.d(TAG, "layout() called (%s)", getName());
        List<Widget> children = getChildren();
        if (children.isEmpty()) {
            return;
        }

        final int xFactor;
        final int yFactor;
        final int zFactor;
        final int axis;
        final float axisSize;

        if (mOrientation == Orientation.HORIZONTAL) {
            axis = 0;
            xFactor = 1;
            yFactor = 0;
            axisSize = mLayoutStrategy.getAxisSize(mWidth);
        } else {
            axis = 1;
            xFactor = 0;
            yFactor = 1;
            axisSize = mLayoutStrategy.getAxisSize(mHeight);
        }
        zFactor = 0;

        final float[] sizes = new float[children.size()];
        float totalSize = getSizes(children, sizes, axis);

        Log.d(TAG, "layout(): totalSize: %5.2f, dimensionSize: %5.2f",
              totalSize, axisSize);

        final int offsetSign = mLayoutStrategy.getOffsetSign();
        float startingOffset = (-offsetSign * (axisSize / 2))
                + (offsetSign * ((axisSize - totalSize) / 2));

        layoutChildren(children, sizes, startingOffset, xFactor, yFactor,
                       zFactor);
    }

    LinearLayoutStrategy getLayoutStrategy() {
        return new LinearLayoutStrategy();
    }

    private void layoutChildren(final List<Widget> children,
            final float[] sizes, float offset, float x, float y, float z) {
        final int numChildren = children.size();
        final int offsetSign = mLayoutStrategy.getOffsetSign();
        final float divider = mLayoutStrategy.getDivider();

        for (int i = 0; i < numChildren; ++i) {
            final Widget child = children.get(i);
            if (child.getVisibility() != Visibility.GONE) {
                final float size = sizes[i];
                final float childOffset = offset + (size / 2);
                Log.d(TAG, "layout(): at %d: offset: %5.2f, size: %5.2f", i,
                      childOffset, size);
                mLayoutStrategy.positionChild(child, childOffset, x, y, z);
                offset += offsetSign * (size + divider);
            }
        }
    }

    private float getSizes(final List<Widget> children, final float[] sizes,
            int axis) {
        final int numChildren = children.size();
        float totalSize = 0;
        for (int i = 0; i < numChildren; ++i) {
            final Widget child = children.get(i);
            final float size;
            if (child.getVisibility() != Visibility.GONE) {
                size = mLayoutStrategy.getChildSize(child, axis);
            } else {
                size = 0;
            }
            sizes[i] = size;
            totalSize += size;
        }

        totalSize += (numChildren - 1) * mLayoutStrategy.getDivider();
        return totalSize;
    }

    abstract class LayoutStrategy {

        protected abstract float getChildSize(final Widget child, int axis);

        protected abstract float getAxisSize(final float size);

        protected abstract float getDivider();

        protected abstract int getOffsetSign();

        protected abstract void positionChild(final Widget child,
                final float childOffset, float xFactor, float yFactor,
                float zFactor);
    }

    class LinearLayoutStrategy extends LayoutStrategy {
        @Override
        protected float getChildSize(final Widget child, int axis) {
            final float size = LayoutHelpers
                    .calculateGeometricDimensions(child)[axis];
            return size;
        }

        @Override
        protected float getAxisSize(final float size) {
            return size;
        }

        @Override
        protected float getDivider() {
            return mDividerPadding;
        }

        @Override
        protected int getOffsetSign() {
            return 1;
        }

        @Override
        protected void positionChild(final Widget child,
                final float childOffset, float xFactor, float yFactor,
                float zFactor) {
            child.setPosition(childOffset * xFactor, childOffset * yFactor,
                              childOffset * zFactor);
        }
    }

    private float mDividerPadding;
    private Orientation mOrientation;
    private float mWidth;
    private float mHeight;
    private LayoutStrategy mLayoutStrategy = new LinearLayoutStrategy();

    private static final String TAG = LinearLayout.class.getSimpleName();
}
