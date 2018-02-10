package com.samsung.smcl.vr.widgets.adapter;

import android.database.DataSetObserver;

import com.samsung.smcl.vr.widgets.adapter.DataSet;
import com.samsung.smcl.vr.widgets.adapter.BaseAdapter;

public abstract class DataSetAdapter<T extends Object> extends BaseAdapter {
    public DataSetAdapter(final DataSet<T> dataSet) {
        mDataSet = dataSet;
        mDataSet.registerObserver(mDataSetObserver);
    }

    @Override
    public int getCount() {
        return mDataSet.getCount();
    }

    @Override
    public T getItem(int position) {
        if (position >= mDataSet.getCount()) {
            throw new ArrayIndexOutOfBoundsException("AppIconAdapter; Asked for position "
                    + position + " but item's size is " + mDataSet.getCount());
        }
        return mDataSet.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return mDataSet.getItemId(position);
    }

    private final DataSet<T> mDataSet;
    private final DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            notifyDataSetInvalidated();
        }
    };
}
