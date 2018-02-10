package com.samsung.smcl.vr.widgets.adapter;

public class TransformingDataSet<T extends Object, U extends Object> extends DataSet<U> {
    public interface Transform<T extends Object, U extends Object> {
        U getItem(T item);
    }

    public TransformingDataSet(DataSet<T> dataSet, Transform<T, U> transform) {
        mDataSet = dataSet;
        mTransform = transform;
    }

    @Override
    public int getCount() {
        return mDataSet.getCount();
    }

    @Override
    public U getItem(int index) {
        T item = mDataSet.getItem(index);
        return mTransform.getItem(item);
    }

    @Override
    public long getItemId(int index) {
        return mDataSet.getItemId(index);
    }

    @Override
    public boolean hasStableIds() {
        return mDataSet.hasStableIds();
    }

    private final DataSet<T> mDataSet;
    private final Transform<T, U> mTransform;
}
