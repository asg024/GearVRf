package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import com.samsung.smcl.utility.RuntimeAssertion;
import com.samsung.smcl.utility.Utility;

/**
 * A specialized {@link LinearLayout} that lays its children out in an arc on
 * either the {@linkplain Orientation#HORIZONTAL horizontal} plane (the plane
 * lying on the x and z axes) or the {@linkplain Orientation#VERTICAL vertical}
 * plane (the plane lying on the x and y axes). If there are enough children, a
 * complete ring will be formed.
 * <p>
 * By default, {@code RingLayout} arranges its children in a ring around its
 * local origin. There are instances, however, when it's useful to add one
 * {@code RingLayout} to another {@code RingLayout}, and have the first layout
 * blend into the second, rather than laying out as a ring <em>on</em> a ring.
 * To enable this, call {@link #setUseVirtualOrigin(boolean)
 * setUseVirtualOrigin(true)}. When using a virtual origin, {@code RingLayout}
 * calculates curvature using a point on it's local z-axis that is
 * {@code radius} units away from its local origin. It's origin, therefore,
 * remains in the edge of the "ring". This distinction allows {@code RingLayout}
 * to be a child of other {@code RingLayouts} and {@link RingList RingLists} and
 * match their curvature without introducing offsetting issues.
 * <p>
 * Some illustration:<br>
 * Be default, {@code RingLayout} arranges its children around its local origin:
 * <br>
 *
 * <img src="ring-layout-local-origin.png" />
 * <p>
 * Using a virtual origin, {@code RingLayout} arranges its children to the left
 * and right (for {@link Orientation#HORIZONTAL horizontal} orientation) of its
 * local origin, centered on the virtual origin; with enough children, the
 * left-most and right-most children will be next to each other:<br>
 *
 * <img src="ring-layout-virtual-origin.png" />
 * <p>
 * As a child of another {@code RingLayout} using its local origin:<br>
 *
 * <img src="ring-ring-layout-local-origin.png" />
 * <p>
 * As a child of another {@code RingLayout} using a virtual origin:<br>
 *
 * <img src="ring-ring-layout-virtual-origin.png" />
 */
public class RingLayout extends LinearLayout {

    /**
     * Construct a wrapper for an existing {@link GVRSceneObject}.
     *
     * @param context
     *            The current {@link GVRContext}.
     * @param sceneObject
     *            The {@link GVRSceneObject} to wrap.
     */
    public RingLayout(GVRContext context, GVRSceneObject sceneObject) {
        super(context, sceneObject);
    }

    public RingLayout(GVRContext context, GVRSceneObject sceneObject,NodeEntry attributes)
            throws InstantiationException {
        super(context, sceneObject, attributes);
    }

    public RingLayout(GVRContext context, float width, float height) {
        super(context, width, height);
    }

    public RingLayout(GVRContext context, float width, float height,
            float radius) {
        super(context, width, height);
        mRadius = radius;
    }

    public float getRadius() {
        return mRadius;
    }

    public void setRadius(final float radius) {
        if (!Utility.equal(radius, mRadius)) {
            mRadius = radius;
            requestLayout();
        }
    }

    public boolean getUseVirtualOrigin() {
        return mUseVirtualOrigin;
    }

    public void setUseVirtualOrigin(boolean useVirtualOrigin) {
        mUseVirtualOrigin = useVirtualOrigin;
    }

    @Override
    LinearLayoutStrategy getLayoutStrategy() {
        return new RadialLayoutStrategy();
    }

    class RadialLayoutStrategy extends LinearLayoutStrategy {
        @Override
        protected float getChildSize(final Widget child, Axis axis) {
            final float segment = super.getChildSize(child, axis);
            final float angle = LayoutHelpers.calculateAngularWidth(segment,
                                                                    mRadius);
            return angle;
        }

        @Override
        protected float getAxisSize(final float size) {
            return LayoutHelpers.angleOfArc(size, mRadius);
        }

        @Override
        protected float getDivider() {
            return LayoutHelpers.angleOfArc(getDividerPadding(), mRadius);
        }

        @Override
        protected int getOffsetSign() {
            return -1;
        }

        @Override
        protected void positionChild(final Widget child,
                final float childOffset, float xFactor, float yFactor,
                float zFactor) {

            final float pivotZ;
            if (mUseVirtualOrigin) {
                pivotZ = mRadius;
            } else {
                child.setPosition(0, 0, -(float) mRadius);
                pivotZ = 0;
            }
            child.rotateByAxisWithPivot(childOffset, xFactor, yFactor, zFactor,
                                        0, 0, pivotZ);
        }
    }

    private float mRadius = 1;
    private boolean mUseVirtualOrigin;
}
