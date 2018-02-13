package com.samsung.smcl.vr.widgetlib.adapter;

import android.database.DataSetObservable;

public abstract class DataSet<T> extends DataSetObservable {
    public abstract int getCount();

    public abstract T getItem(int index);

    public abstract long getItemId(int index);

    public abstract boolean hasStableIds();

    public boolean isEmpty() {
        return getCount() < 1;
    }
}
