package com.samsung.smcl.vr.widgetlib.adapter;

import android.database.DataSetObserver;

import com.samsung.smcl.vr.widgetlib.widget.GroupWidget;
import com.samsung.smcl.vr.widgetlib.widget.Widget;
import com.samsung.smcl.vr.widgetlib.log.Log;
import com.samsung.smcl.vr.widgetlib.widget.layout.basic.AbsoluteLayout;

/**
 * Implementation of {@link Adapter} interface for the GridLayout
 * GridAdapter is basically wrapped around any linear Adapter presenting the data
 * as a grid. It breaks the content down by either the rows or columns.
 */
class GridAdapter implements Adapter {
    private int mNumOfRows;
    private int mNumOfColumns;
    private Adapter mAdapter;

    private Type mType = Type.FIXED_COLUMNS;
    private static final String TAG = GridAdapter.class.getSimpleName();

    enum Type {
        FIXED_COLUMNS,
        FIXED_ROWS
    }
    /**
     * The Inner class to specify the position of the item is the grid
     */
    static class Position {
        int row;
        int column;
        private static final String pattern = "\n grid position: [%d, %d]";

        public Position(int r, int c) {
            row = r;
            column = c;
        }
        public String toString() {
            return String.format(pattern, row, column);
        }
    }

    /**
     * Construct a new {@link GridAdapter} with fixed dimension
     *
     * @param adapter
     * @param fixedNum Number of items in fixed dimension
     */
    GridAdapter(Adapter adapter, final Type type, final int fixedNum) {
        mAdapter = adapter;
        mType = type;
        switch(mType) {
            case FIXED_ROWS:
                mNumOfRows = fixedNum;
                break;
            case FIXED_COLUMNS:
                mNumOfColumns = fixedNum;
                break;
        }

        calculateSize();

        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                calculateSize();
            }

            @Override
            public void onInvalidated() {
                calculateSize();
            }
        });
    }

    /**
     * Calculate the number of columns in the grid
     * @return number of columns
     */
    public int getColumnsCount() {
        return mNumOfColumns;
    }

    /**
     * Calculate the number of rows in the grid
     * @return number of rows
     */
    public int getRowsCount() {
        return mNumOfRows;
    }

    /**
     * @return {@link Type} of the {@link GridAdapter}
     */
    public Type getType() {
        return mType;
    }

    /**
     * Get the data item associated with the specified grid {@link Position} in the data set.
     *
     * @param position
     *            {@link Position} of the item whose data we want within the grid adapter's
     *            data set.
     * @return The data at the specified position.
     */
    public Object getItem(final Position position) {
        int itemIdx = getLinearPosition(position);
        return itemIdx == -1 ? null : getItem(itemIdx);
    }

    /**
     * Get the item id associated with the specified grid {@link Position} in the data set.
     *
     * @param position
     *            {@link Position} of the item whose data we want within the grid adapter's
     *            data set.
     * @return The id of the item at the specified {@link Position}.
     */
    public long getItemId(Position position) {
        int itemIdx = getLinearPosition(position);
        return getItemId(itemIdx);
    }

    /**
     * Get a {@link Widget} that displays the data at the specified {@link Position} in
     * the grid data set. Should only be called from GL thread.
     *
     * @param position
     *            The {@link Position} of the item within the grid adapter's data set of the
     *            item whose view we want.
     * @param convertView
     *            The old view to reuse, if possible. Note: You should check
     *            that this view is non-null and of an appropriate type before
     *            using. If it is not possible to convert this view to display
     *            the correct data, this method can create a new view.
     *            Heterogeneous lists can specify their number of view types, so
     *            that this View is always of the right type (see
     *            {@link #getViewTypeCount()} and {@link #getItemViewType(int)}
     *            ).
     * @param parent
     *            The parent that this view will eventually be attached to.
     * @return A Widget corresponding to the data at the specified {@link Position}.
     */
    public Widget getView(Position position, Widget convertView, GroupWidget parent) {
        int itemIdx = getLinearPosition(position);
        return itemIdx == -1 ? null : getView(itemIdx, convertView, parent);
    }

    /**
     * Get a {@link GroupWidget} that displays the group at the specified position in
     * the data set. Should only be called from GL thread.
     * Groups can be either columns or rows depending on the {@link Type}
     *
     * @param position
     *            The position of the group within the adapter's data set of the
     *            item whose view we want.
     * @param convertView
     *            The old view to reuse, if possible. Note: You should check
     *            that this view is non-null and of an appropriate type before
     *            using. If it is not possible to convert this view to display
     *            the correct data, this method can create a new view.
     *            Heterogeneous lists can specify their number of view types, so
     *            that this View is always of the right type (see
     *            {@link #getViewTypeCount()} and {@link #getItemViewType(int)}
     *            ).
     * @param parent
     *            The parent that this view will eventually be attached to.
     * @return A {@link GroupWidget} corresponding to the group at the specified position.
     */
    public GroupWidget getGroupView(int position, GroupWidget convertView, GroupWidget parent) {
        GroupWidget groupWidget = ((GroupWidget)convertView);
        if (groupWidget != null) {
            groupWidget.clear();
        } else if (parent != null) {
            groupWidget = new GroupWidget(parent.getGVRContext(), 0, 0);
            groupWidget.applyLayout(new AbsoluteLayout());
        } else {
            return null;
        }
        switch(mType) {
            case FIXED_ROWS:
                for (int k = 0; k < getRowsCount(); ++k) {
                    groupWidget.addChild(getView
                           (new Position(k, position), null, groupWidget));
                }
                break;
            case FIXED_COLUMNS:
                for (int k = 0; k < getColumnsCount(); ++k) {
                    groupWidget.addChild(getView
                            (new Position(position, k), null, groupWidget));
                }
                break;
            default:
                Log.d(TAG, "Grid type %s is not supported!", mType);
                break;
        }
        return groupWidget;
    }

    /**
     * How many groups are in the data set represented by this GridAdapter.
     * Groups can be either columns or rows depending on the {@link Type}
     *
     * @return Count of groups.
     */
    public int getGroupCount() {
        return mType == Type.FIXED_ROWS ? getColumnsCount() : getRowsCount();
    }

    /**
     * Get the group id associated with the specified position of the group in the data set.
     * Groups can be either columns or rows depending on the {@link Type}
     *
     * @param position
     *            position of the group whose data we want within the grid adapter's
     *            data set.
     * @return The id of the group at the specified position.
     */
    public long getGroupId(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return mAdapter.getItemId(position);
    }

    @Override
    public int getCount() {
        return mAdapter.getCount();
    }

    @Override
    public Object getItem(int position) {
        return mAdapter.getItem(position);
    }

    @Override
    public int getItemViewType(int position) {
        return mAdapter.getItemViewType(position);
    }

    @Override
    public Widget getView(int position, Widget convertView, GroupWidget parent) {
        return mAdapter.getView(position, convertView, parent);
    }

    @Override
    public int getViewTypeCount() {
        return mAdapter.getViewTypeCount();
    }

    @Override
    public boolean hasStableIds() {
        return mAdapter.hasStableIds();
    }

    @Override
    public boolean hasUniformViewSize() {
        return mAdapter.hasUniformViewSize();
    }

    @Override
    public float getUniformWidth() {
        return mAdapter.getUniformWidth();
    }

    @Override
    public float getUniformHeight() {
        return mAdapter.getUniformHeight();
    }

    @Override
    public float getUniformDepth() {
        return mAdapter.getUniformDepth();
    }

    @Override
    public boolean isEmpty() {
        return mAdapter.isEmpty();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mAdapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mAdapter.unregisterDataSetObserver(observer);
    }

    @Override
    public void unregisterAllDataSetObservers() {
        mAdapter.unregisterAllDataSetObservers();
    }


    private int getLinearPosition(final Position position) {
        int itemIdx = -1;
        if (position.column >= 0 && position.column < mNumOfColumns &&
                position.row >= 0 && position.row < mNumOfRows) {
            switch(mType) {
                case FIXED_ROWS:
                    itemIdx = position.column * mNumOfRows + position.row;
                    break;
                case FIXED_COLUMNS:
                    itemIdx = position.row * mNumOfColumns + position.column;
                    break;
                default:
                    Log.d(TAG, "Grid type %s is not supported!", mType);
                    break;
            }
        } else {
            Log.d(TAG, "getItem: position %s is out of bounds!", position);
        }
        return itemIdx;
    }

    private void calculateSize() {
        switch(mType) {
            case FIXED_ROWS:
                mNumOfColumns = (int) Math.ceil((double) mAdapter.getCount()
                                                / (double) mNumOfRows);
                break;
            case FIXED_COLUMNS:
                mNumOfRows = (int) Math.ceil((double) mAdapter.getCount()
                                                / (double) mNumOfColumns);
                break;
        }
    }
}
