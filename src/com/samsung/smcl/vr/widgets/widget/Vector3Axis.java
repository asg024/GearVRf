package com.samsung.smcl.vr.widgets.widget;

import com.samsung.smcl.vr.widgets.widget.layout.Layout;
import static com.samsung.smcl.vr.widgets.main.Utility.equal;

import org.gearvrf.utility.RuntimeAssertion;
import org.joml.Vector3f;

/**
 * ViewPort class basically define the layout container dimensions.
 */
public class Vector3Axis extends Vector3f {
    public Vector3Axis(final float x, final float y, final float z) {
        super(x, y, z);
    }

    public Vector3Axis(Vector3f v) {
        super(v.x, v.y, v.z);
    }

    public Vector3Axis() {
        super();
    }

    public float get(Layout.Axis axis) {
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

    public void set(float val, Layout.Axis axis) {
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
        if (x != Float.NaN && v.x != Float.NaN && !equal(x, v.x)) {
            ret.set(x - v.x, Layout.Axis.X);
        }
        if (y != Float.NaN && v.y != Float.NaN && !equal(y, v.y)) {
            ret.set(y - v.y, Layout.Axis.Y);
        }
        if (z != Float.NaN && v.z != Float.NaN && !equal(z, v.z)) {
            ret.set(z - v.z, Layout.Axis.Z);
        }
        return ret;
    }
}
