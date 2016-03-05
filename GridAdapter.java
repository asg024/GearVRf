package com.samsung.smcl.vr.widgets;

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRContext;

import com.samsung.smcl.vr.gvrf_launcher.LauncherInfo;
import com.samsung.smcl.vr.widgets.LinearLayout.Orientation;

import android.util.SparseArray;

public class GridAdapter extends BaseAdapter {
    private float mGridPadding;
    private int mNumOfRows;
    private int mNumOfColumns;
    private GVRContext mContext;
    private Adapter adapter;

    public GridAdapter(GVRContext gvrContext, Adapter adapter, float dividerPadding,
            int numOfRows) {
        this.adapter = adapter;
        mContext = gvrContext;
        mGridPadding = dividerPadding;
        mNumOfRows = numOfRows;
        mNumOfColumns = (int) Math.ceil((double) adapter.getCount()
                / (double) mNumOfRows);
    }

    @Override
    public int getCount() {
        return mNumOfColumns;
    }

    @Override
    public List<Object> getItem(int position) {
        List<Object> itemList = new ArrayList<Object>(mNumOfRows);
        int listIndex = 0;
        int itemIdx = position * mNumOfRows;
        for (int index = itemIdx; index < itemIdx + mNumOfRows; index++) {
            Object item = adapter.getItem(index);
            itemList.set(listIndex, item);
            listIndex++;
        }

        return itemList;
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }

    private final String TAG = GridAdapter.class.getSimpleName();

    @Override
    public Widget getView(int position, Widget convertView, GroupWidget parent) {
        int itemIdx = position * mNumOfRows;
        if (convertView != null) {
            List<Widget> children = convertView.getChildren();

            // remove all current children from convertView
            for (int index = 0; index < children.size(); index++) {
                ((LinearLayout) convertView).removeChild(children.get(index));
            }

            // re-add all new children to convertView
            int index = 0;
            for (index = 0; index < children.size(); index++) {
                Widget item = adapter.getView(itemIdx + index,
                                              children.get(index),
                                              ((LinearLayout) convertView));
                ((LinearLayout) convertView).addChild(item);
            }

            for (; index < mNumOfRows; index++) {
                Widget item = adapter.getView(itemIdx + index, null, parent);
                ((LinearLayout) convertView).addChild(item);
            }

            return convertView;
        }

        LinearLayout columnWidget = new LinearLayout(mContext, 0, 0);
        columnWidget.setOrientation(Orientation.VERTICAL);
        columnWidget.setDividerPadding(mGridPadding);
        for (int index = itemIdx; index < itemIdx + mNumOfRows; index++) {
            Widget app = adapter.getView(index, null, parent);
            columnWidget.addChild(app);
        }

        return columnWidget;
    }
};
