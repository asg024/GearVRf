package com.samsung.smcl.vr.widgets;

import org.joml.Vector3f;

import com.samsung.smcl.utility.Log;

/**
 * A specialized {@link Layout} that applies the arch curvature to the children on
 * either the {@linkplain Orientation#HORIZONTAL horizontal} or the
 * {@linkplain Orientation#VERTICAL vertical} direction. The radius of the arc
 * can be specified in the class constructor.
 * It is basically designed as the secondary layout in the layout chain.
 */
public class ArchLayout extends OrientedLayout {

    /**
     * Construct a new {@link ArchLayout} with the radius.
     * The size of the container is calculated as the size of the scene object.
     *
     * @param radius - ring radius.
     */
    public ArchLayout(float radius) {
        super();

        if (radius <= 0) {
            Log.w(TAG, "setRadius: Radius cannot be negative [%f] !", radius);
        } else {
            mRadius = radius;
        }
    }

    /**
     * @return ring radius
     */
    public float getRadius() {
        return mRadius;
    }

    private static final String pattern = "\nAL attributes====== orientation = %s  size [%s]";

    /**
     * Return the string representation of the ArchLayout
     */
    public String toString() {
        return super.toString() + String.format(pattern, mOrientation, mViewPort);
    }

    /**
     * Calculate the angle by arc length
     * @param arcLength
     * @return angle
     */
    protected float getSizeAngle(float arcLength) {
        if (mRadius <= 0) {
            throw new IllegalArgumentException("mRadius is not specified!");
        }
        return LayoutHelpers.angleOfArc(arcLength, mRadius);
    }

    /**
     * Calculate the arc length by angle and radius
     * @param angle
     * @return arc length
     */
    protected float getSizeArcLength(float angle) {
        if (mRadius <= 0) {
            throw new IllegalArgumentException("mRadius is not specified!");
        }
        return angle == Float.MAX_VALUE ? Float.MAX_VALUE :
            LayoutHelpers.lengthOfArc(angle, mRadius);
    }

    @Override
    void onLayoutApplied(final WidgetContainer container, final Vector3Axis viewPort) {
        super.onLayoutApplied(container, new Vector3Axis(
                                 getSizeAngle(viewPort.x),
                                 getSizeAngle(viewPort.y),
                                 getSizeAngle(viewPort.z)));
    }


    protected Vector3f getFactor() {
        Vector3f factor = new Vector3f();
        Axis axis = getOrientationAxis();
        switch(axis) {
            case X:
                factor.x = 1;
                break;
            case Y:
                factor.y = -1;
                break;
            case Z:
                factor.z = 1;
                break;
        }
        return factor;
    }

    @Override
    protected void layoutChild(final int dataIndex) {
        Widget child = mContainer.get(dataIndex);
        if (child != null) {
            Vector3f factor = getFactor();
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "positionChild %s with radius = %f factor = %s",
                  child.getName(), mRadius, factor);
            child.setPositionZ(-(float) mRadius);

            if (factor.x != 0) {
                float x = getSizeAngle(child.getPositionX());
                child.setPositionX(0);
                child.rotateByAxisWithPivot(-x, 0, factor.x, 0,
                                            0, 0, 0);
            }
            if (factor.y != 0) {
                float y = getSizeAngle(child.getPositionY());
                child.setPositionY(0);
                child.rotateByAxisWithPivot(-y, factor.y, 0, 0,
                                            0, 0, 0);
            }
            child.onTransformChanged();

            super.layoutChild(dataIndex);
        }
    }

    @Override
    protected void resetChildLayout(final int dataIndex) {
/*        Widget child = mContainer.get(dataIndex);
        Log.d(TAG, "clearChildPosition %s", child);
        child.setRotation(1, 0, 0, 0);
        child.setPosition(0, 0, 0);
        */
    }

    private float mRadius = 0;
}
