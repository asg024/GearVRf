package com.samsung.smcl.vr.widgetlib.widget.layout.basic;

import com.samsung.smcl.vr.widgetlib.widget.Widget;
import com.samsung.smcl.vr.widgetlib.widget.layout.Layout;

import java.util.List;

/**
 * A layout class that leaves com.samsung.smcl.vr.com.samsung.smcl.vr.widgetlib exactly where they are placed.
 */
public class AbsoluteLayout extends Layout {
    public AbsoluteLayout() {
        super();
    }

    @Override
    protected float getMeasuredChildSizeWithPadding(final int dataIndex, final Axis axis) {
        return getChildSize(dataIndex, axis) + getDividerPadding(axis);
    }

    protected float getTotalSizeWithPadding(final Axis axis) {
        return 0;
    }

    @Override
    protected boolean postMeasurement() {
        return true;
    }

    @Override
    protected void resetChildLayout(final int dataIndex) {
    }

    @Override
    public boolean inViewPort(final int dataIndex) {
        return true;
    }

    @Override
    public int getCenterChild() {
        return 0;
    }

    @Override
    public Direction getDirectionToChild(int dataIndex, Axis axis) {
        return Direction.NONE;
    }

    @Override
    public float getDistanceToChild(int dataIndex, Axis axis) {
        return 0;
    }

    @Override
    public float preMeasureNext(final List<Widget> measuredChildren,
                                   final Axis axis, final Direction direction) {
        return 0;
    }

    protected AbsoluteLayout(final AbsoluteLayout rhs) {
        super(rhs);
    }

    @Override
    public Layout clone() {
        return new AbsoluteLayout(this);
    }

    @Override
    public float calculateWidth(int[] children) { return 0; }

    @Override
    public float calculateHeight(int[] children) { return 0; }

    @Override
    public float calculateDepth(int[] children) { return 0; }

}
