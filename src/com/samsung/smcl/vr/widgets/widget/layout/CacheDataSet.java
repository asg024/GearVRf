package com.samsung.smcl.vr.widgets.widget.layout;

public interface CacheDataSet {
    enum InvalidateOp {
        ALL,
        POSITION,
        OFFSET,
        SIZE,
        PADDING
    };

    int count();
    void invalidate();
    void invalidate(InvalidateOp op);
    void copyTo(CacheDataSet to);
    float getTotalSize();
    void dump();
    float getTotalSizeWithPadding();
    void shiftBy(final float offset);
    float uniformSize();
    float uniformPadding(final float uniformPadding);

    float addData(final int id, final int pos,
            final float size, final float startPadding, final float endPadding);
    boolean contains(final int id);
    void removeData(final int id);
    float getDataOffset(final int id);
    float getSizeWithPadding(final int id);

    float getStartDataOffset(final int id);
    float getEndDataOffset(final int id);

    float getStartPadding(final int id);
    float getEndPadding(final int id);

    float setDataOffsetBefore(final int id, float endDataOffset);
    float setDataOffsetAfter(final int id, float startDataOffset);
    int getId(final int pos);
    int getPos(final int id);
    int searchPos(final int dataIndex);
}