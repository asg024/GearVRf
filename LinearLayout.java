package com.samsung.smcl.vr.widgets;

import java.util.List;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import com.samsung.smcl.utility.Log;
import com.samsung.smcl.utility.RuntimeAssertion;

/**
 * A widget group class that lays its children out along its local x-axis (
 * {@link Orientation#HORIZONTAL}) or it's local y-axis (
 * {@link Orientation#VERTICAL}). The default orientation is horizontal.
 */
public class LinearLayout extends GroupWidget {

    public enum Orientation {
        HORIZONTAL, VERTICAL
    }

    /**
     * Construct a wrapper for an existing {@link GVRSceneObject}.
     *
     * @param context
     *            The current {@link GVRContext}.
     * @param sceneObject
     *            The {@link GVRSceneObject} to wrap.
     */
    public LinearLayout(GVRContext context, GVRSceneObject sceneObject) {
        super(context, sceneObject);
        mLayoutStrategy = getLayoutStrategy();
    }

    public LinearLayout(GVRContext gvrContext, GVRSceneObject sceneObject,
                        NodeEntry attributes) throws InstantiationException {
        super(gvrContext, sceneObject, attributes);

        String attribute = attributes.getProperty("orientation");
        if (attribute != null) {
            mOrientation = Orientation.valueOf(attribute);
        }
        attribute = attributes.getProperty("divider_padding");
        if (attribute != null) {
            mDividerPadding = Float.parseFloat(attribute);
        }
    }

    private static final String pattern = "\nLL attributes====== orientation = %s divider_padding = %f";

    public String toString() {
        return super.toString() + String.format(pattern, mOrientation, mDividerPadding);
    }



    /**
     * Construct a new {@link LinearLayout}.
     *
     * @param context
     *            A valid {@link GVRContext} instance.
     * @param width
     * @param height
     */
    public LinearLayout(GVRContext gvrContext, float width, float height) {
        super(gvrContext, width, height);
        mWidth = width;
        mHeight = height;
        mLayoutStrategy = getLayoutStrategy();
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

    public float getWidth() {
        return mWidth;
    }

    public float getHeight() {
        return mHeight;
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
        final LayoutStrategy.Axis axis;
        final float axisSize;

        if (mOrientation == Orientation.HORIZONTAL) {
            axis = LayoutStrategy.Axis.X;
            xFactor = 1;
            yFactor = 0;
            axisSize = mLayoutStrategy.getAxisSize(mWidth);
        } else {
            axis = LayoutStrategy.Axis.Y;
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
            final LayoutStrategy.Axis axis) {
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

    static abstract class LayoutStrategy {
        enum Axis {
            X, Y, Z
        }

        protected abstract float getChildSize(final Widget child, Axis axis);

        protected abstract float getAxisSize(final float size);

        protected abstract float getDivider();

        protected abstract int getOffsetSign();

        protected abstract void positionChild(final Widget child,
                final float childOffset, float xFactor, float yFactor,
                float zFactor);
    }

    class LinearLayoutStrategy extends LayoutStrategy {
        @Override
        protected float getChildSize(final Widget child, Axis axis) {
            switch (axis) {
                case X:
                    return child.getWidth();
                case Y:
                    return child.getHeight();
                case Z:
                    return child.getDepth();
                default:
                    throw new RuntimeAssertion("Bad axis specified: %s", axis);
            }
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

    private float mDividerPadding = 0f;
    private Orientation mOrientation = Orientation.HORIZONTAL;
    private float mWidth;
    private float mHeight;
    private LayoutStrategy mLayoutStrategy = new LinearLayoutStrategy();

    private static final String TAG = LinearLayout.class.getSimpleName();
}
