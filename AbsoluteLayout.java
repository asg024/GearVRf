package com.samsung.smcl.vr.widgets;

import java.util.List;

/**
 * A layout class that leaves widgets exactly where they are placed.
 */
public class AbsoluteLayout extends Layout {
    @Override
    protected float getMeasuredChildSizeWithPadding(final int dataIndex, final Axis axis) {
        return getChildSize(dataIndex, axis) + getDividerPadding(axis);
    }

    @Override
    protected void shiftBy(final float offset, final Axis axis) {
    }

    @Override
    protected boolean postMeasurement() {
        return true;
    }

    @Override
    protected void resetChildLayout(final int dataIndex) {
    }

    @Override
    boolean inViewPort(final int dataIndex) {
        return true;
    }

    @Override
    protected int getCenterChild() {
        return 0;
    }

    @Override
    protected Direction getDirectionToChild(int dataIndex, Axis axis) {
        return Direction.NONE;
    }

    @Override
    protected float getDistanceToChild(int dataIndex, Axis axis) {
        return 0;
    }

    @Override
    protected float preMeasureNext(final List<Widget> measuredChildren,
            final Axis axis, final Direction direction) {
        return 0;
    }
}
