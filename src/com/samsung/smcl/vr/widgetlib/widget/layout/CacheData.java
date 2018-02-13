package com.samsung.smcl.vr.widgetlib.widget.layout;

/**
 * Created by svetlanag on 1/22/18.
 */

public class CacheData {
    protected float mSize;
    protected float mOffset;
    protected float mStartPadding;
    protected float mEndPadding;
    protected int mId;

    public CacheData(final int id) {
        mId = id;
    }

    public CacheData(final CacheData data) {
        mId = data.mId;
        mSize = data.mSize;
        mOffset = data.mOffset;
        mStartPadding = data.mStartPadding;
        mEndPadding = data.mEndPadding;
    }

    public void setSize(final float size) {
        mSize = size;
    }

    public float getSize() {
        return mSize;
    }

    public void setOffset(final float offset) {
        mOffset = offset;
    }

    public float getOffset() {
        return mOffset;
    }

    public void setPadding(final float start, final float end) {
        mStartPadding = start;
        mEndPadding = end;
    }

    public float getStartPadding() {
        return mStartPadding;
    }

    public float getEndPadding() {
        return mEndPadding;
    }

    private static final String pattern = "id [%d] size [%f] offset [%f] startPadding [%f] endPadding [%f]";

    /**
     * Return the string representation of the LinearLayout
     */
    public String toString() {
        return super.toString() + String.format(pattern, mId, mSize, mOffset, mStartPadding, mEndPadding);
    }
}