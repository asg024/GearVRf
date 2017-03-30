package com.samsung.smcl.vr.widgets;

import java.util.Collection;
import java.util.List;

import android.util.SparseArray;
import java.util.Set;
import java.util.HashSet;

import com.samsung.smcl.utility.Log;

import com.samsung.smcl.vr.widgets.LinearLayout.Gravity;

/**
 * A layout that shows items in the grid.
 * The items are presented as a grid. The content of the layout is broken down by either
 * the rows or columns depending on the {@link Orientation}.
 * If orientation is {@link Orientation#HORIZONTAL} the data will be organized as the horizontally
 * extended grid with fixed number of rows. The scrolling is supported in HORIZONTAL direction only
 *
 * If orientation is {@link Orientation#VERTICAL} the data will be organized as the vertically
 * extended grid with fixed number of columns. The scrolling is supported in VERTICAL direction only
 *
 * {@link Orientation#STACK} is not supported currently for the GridLayout
 *
 * The size of each cell in the grid layout is proportional (uniform size is enabled by default)
 *
 */

public class GridLayout extends OrientedLayout {
    protected int mRowCount, mColumnCount;
    protected static final String TAG = GridLayout.class.getSimpleName();

    class ChunkedLinearLayout extends LinearLayout {
        protected ChunkBreaker mChunkBreaker;
        protected SparseArray<CacheDataSet> mCaches = new SparseArray<CacheDataSet>();
        protected float mSize;
        protected boolean mForcePostMeasurement;


        ChunkedLinearLayout() {
            super();
        }

        ChunkedLinearLayout(ChunkedLinearLayout rhs) {
            super(rhs);
            mSize = rhs.mSize;
            mForcePostMeasurement = rhs.mForcePostMeasurement;
            mChunkBreaker = rhs.mChunkBreaker;
        }

        @Override
        protected void initCache() {
        }

        @Override
        protected void invalidateCache() {
            for (int i = mCaches.size(); --i >= 0;) {
                mCaches.valueAt(i).invalidate();
            }
            mCaches.clear();
        }

        @Override
        protected void invalidateCache(final int dataIndex) {
            int cacheId = getCacheId(dataIndex);
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "invalidateCache [%d] - cacheId [%d]", dataIndex, cacheId);

            if (cacheId >= 0) {
                CacheDataSet cache = mCaches.valueAt(cacheId);
                cache.removeData(dataIndex);
                if (cache.count() == 0) {
                    mCaches.removeAt(cacheId);
                }
            }
        }

        @Override
        protected void dumpCaches() {
            for (int i = 0; i < mCaches.size(); i++) {
                mCaches.valueAt(i).dump();
            }
        }

        @Override
        protected int getCenterChild() {
            int ret = -1;
            for (int i = mCaches.size(); --i >= 0;) {
                CacheDataSet cache = mCaches.valueAt(i);
                if (cache != null) {
                    ret = getCenterChild(cache);
                    if (ret >= 0) {
                        break;
                    }
                }
            }
            return ret;
        }

        protected Set<Integer> getCenterChildren() {
            Set<Integer> ret = new HashSet<Integer>(mCaches.size());
            for (int i = mCaches.size(); --i >= 0;) {
                CacheDataSet cache = mCaches.valueAt(i);
                if (cache != null) {
                    int id = getCenterChild(cache);
                    if (id >= 0) {
                        ret.add(id);
                    }
                }
            }

            return ret;
        }

        protected void forcePostMeasurement(boolean force) {
            mForcePostMeasurement = force;
        }

        protected void setChunkBreaker(final ChunkBreaker chunkBreaker) {
            mChunkBreaker = chunkBreaker;
            invalidate();
        }

        @Override
        protected int getCacheCount() {
            int count = 0;
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "getCacheCount mCaches.size() = %d", mCaches.size());

            for (int i = mCaches.size(); --i >=0; ) {
                CacheDataSet cache = mCaches.get(i);
                if (cache != null) {
                    count += cache.count();
                }
            }
            return count;
        }

        @Override
        protected Widget measureChild(final int dataIndex, boolean calculateOffset) {
           // int cacheId = mChunkBreaker.getChunkIndex(getCacheCount());
            int cacheId = mChunkBreaker.getChunkIndex(dataIndex);
            CacheDataSet cache = mCaches.get(cacheId);
            if (cache == null) {
                cache = new LinearCacheDataSet(mOuterPaddingEnabled);
                mCaches.put(cacheId, cache);
            }
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "measureChild [%d] orientation = %s cacheId = %d cache.count = %d",
                    dataIndex, mOrientation, cacheId, cache.count());

            Widget w = measureChild(dataIndex, calculateOffset, cache);
            if (mForcePostMeasurement) {
                postMeasurement(cache);
            }
            return w;
        }

        @Override
        protected int getFirstDataIndex() {
            if (mCaches.size() == 0) {
                return -1;
            }
            return mCaches.valueAt(0).getId(0);
        }

        @Override
        protected int getLastDataIndex() {
            if (mCaches.size() == 0) {
                return -1;
            }
            CacheDataSet lastCache = mCaches.valueAt(mCaches.size() - 1);
            return lastCache.getId(lastCache.count() - 1);
        }


        @Override
        protected float preMeasureNext(final List<Widget> measuredChildren,
                final Axis axis, final Direction direction) {
            float totalSize = Float.NaN;
            int dataIndex = getNextDataId(axis, direction);

            if (dataIndex >= 0) {
                int count =  mCaches.size();
                float max = 0;
                int sign = (direction == Direction.BACKWARD ? 1 : -1);
                while (dataIndex >= 0 && dataIndex < mContainer.size() && count-- > 0) {
                    Widget widget = measureChild(dataIndex);
                    float sizeWithPadding  = getMeasuredChildSizeWithPadding(dataIndex, axis);
                    if (!Float.isNaN(sizeWithPadding)) {
                        max = Math.max(max, sizeWithPadding);
                    }

                    if (widget != null && measuredChildren != null) {
                        measuredChildren.add(widget);
                    }
                    dataIndex -= sign;
                }
                if (max > 0) {
                    totalSize = sign * max;
                }
            }
            return totalSize;
        }

        @Override
        protected float getDistanceToChild(int dataIndex, Axis axis) {
            float ret = Float.NaN;
            CacheDataSet cache = getCache(dataIndex);
            if (cache != null) {
                ret = getDistanceToChild(dataIndex, axis, cache);
            }
            return ret;
        }

        protected CacheDataSet getCache(final int dataIndex) {
            for (int i = mCaches.size(); --i >=0; ) {
                CacheDataSet cache = mCaches.valueAt(i);
                if (cache.contains(dataIndex)) {
                    return cache;
                }
            }
            return null;
        }


        protected int getCacheId(final int dataIndex) {
            for (int i = mCaches.size(); --i >=0; ) {
                CacheDataSet cache = mCaches.valueAt(i);
                if (cache.contains(dataIndex)) {
                    return i;
                }
            }

            return -1;
        }

        protected float getSize() {
            return mSize;
        }

        protected void setSize(final float size) {
            if (mSize != size) {
                mSize = size;
                invalidate();
            }
        }

        @Override
        protected boolean inViewPort(final int dataIndex) {
            boolean visible = false;
            CacheDataSet cache = getCache(dataIndex);
            if (cache != null) {
                visible = inViewPort(dataIndex, cache);
            } else {
                Log.e(TAG, "inViewPort(%s): Error: child is not found in the cache", dataIndex);
            }
            return visible;
        }

        @Override
        protected float getDataOffset(final int dataIndex) {
            float offset = Float.NaN;
            CacheDataSet cache = getCache(dataIndex);
            if (cache != null) {
                offset = cache.getDataOffset(dataIndex);
            } else {
                Log.e(TAG, "getDataOffset(%s): Error: child is not found in the cache or " +
                    "offset is not assigned!", dataIndex);
            }
            return offset;
        }

        /**
         * If the item size is setup use it as the default size for all items.
         * If size of all items in the line exceeds the size of the viewport, the item size
         * will be decreased in order to fit the viewport.
         * If size is not specified the item size is measured by regular way.
         */
        @Override
        protected float getChildSize(final int dataIndex, Axis axis) {
            int chunkSize = mChunkBreaker != null ? mChunkBreaker.getChunkSize() : 0;
            float size =  mSize > 0 ?
                            (mApplyViewPort ?
                                  Math.min(mSize, getAxisSize(axis)/(
                                          chunkSize > 0 ? chunkSize : 1)) :
                                  mSize) :
                            super.getChildSize(dataIndex, axis);
            return size;
        }

        @Override
        protected boolean postMeasurement() {
            boolean ret = true;
            for (int i = mCaches.size(); --i >=0; ) {
                ret = postMeasurement(mCaches.valueAt(i)) && ret;
            }
            return ret;
        }

        @Override
        public void shiftBy(final float offset, final Axis axis) {
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "shiftBy offset = %f axis = %s layout = %s", offset, axis, this);

            if (!Float.isNaN(offset) && axis == getOrientationAxis()) {
                for (int i = mCaches.size(); --i >=0; ) {
                    mCaches.valueAt(i).shiftBy(offset);
                }
            }
        }

        @Override
        protected float getMeasuredChildSizeWithPadding(final int dataIndex, final Axis axis) {
            CacheDataSet cache = getCache(dataIndex);
            if (cache != null) {
                return getMeasuredChildSizeWithPadding(dataIndex, cache);
            }
            return Float.NaN;
        }
    }

    /**
     * Construct a new {@code GridLayout} instance. Number of columns and rows will be computed based on rowCount,
     * columnCount, size of layout container and size of the items in the grid.
     * @param rowCount preferable number of rows in the grid. It is taken into account if the grid orientation is
     * {@link Orientation#HORIZONTAL}. The number of rows could be less than <rowCount> if viewport is enabled and
     * <rowCount> number of items cannot be fitted into the viewport
     * @param columnCount preferable number of columns in the grid. It is taken into account if the grid orientation
     * is {@link Orientation#VERTICAL}. The number of columns could be less than <columnCount> if viewport is enabled and
     * <columnCount> number of items cannot be fitted into the viewport
     */
    public GridLayout(final int rowCount, final int columnCount) {
        super();
        mRowCount = rowCount;
        mColumnCount = columnCount;

        mRowLayout = new ChunkedLinearLayout();
        mRowLayout.setOrientation(Orientation.HORIZONTAL);

        mColumnLayout = new ChunkedLinearLayout();
        mColumnLayout.setOrientation(Orientation.VERTICAL);

        layoutSetup();
    }

    public GridLayout(GridLayout rhs) {
        super(rhs);
        mRowCount = rhs.mRowCount;
        mColumnCount = rhs.mColumnCount;
        mRowLayout = new ChunkedLinearLayout(rhs.mRowLayout);
        mColumnLayout = new ChunkedLinearLayout(rhs.mColumnLayout);
    }

    private static final String pattern = "\nGL attributes====== orientation = %s " +
            "divider_padding = %s outerPaddingEnabled [%b] size [%s]";

    /**
     * Return the string representation of the LinearLayout
     */
    public String toString() {
        return super.toString() + String.format(pattern, mOrientation,
                mDividerPadding, mOuterPaddingEnabled, mViewPort);
    }



    private ChunkedLinearLayout mRowLayout;
    private ChunkedLinearLayout mColumnLayout;

    private void layoutSetup() {
        enableUniformSize(true);
        setHorizontalGravity(Gravity.LEFT);
        setVerticalGravity(Gravity.TOP);
        init(mOrientation);
    }

    @Override
    void onLayoutApplied(final WidgetContainer container, final Vector3Axis viewPort) {
        super.onLayoutApplied(container, viewPort);
        mRowLayout.onLayoutApplied(container, viewPort);
        mColumnLayout.onLayoutApplied(container, viewPort);
    }

    @Override

    protected boolean inViewPort(final int dataIndex) {
        return mColumnLayout.inViewPort(dataIndex) && mRowLayout.inViewPort(dataIndex);
    }

    /**
     * Check if the orientation is valid
     * @param orientation
     * @return true if orientation can be applied
     */
    protected boolean isValidLayout(Orientation orientation) {
        return orientation !=  Orientation.STACK;
    }

    /**
     * When set to true, all items in layout will be considered having the size of the largest child. If false, all items are
     * measured normally. Disabled by default.
     * @param enable  true to measure children using the size of the largest child, false - otherwise.
     */
    public void enableUniformSize(final boolean enable) {
        mRowLayout.enableUniformSize(enable);
        mColumnLayout.enableUniformSize(enable);
    }

    /**
     * Set the horizontal {@link Gravity} of the layout.
     * The new gravity can be rejected if it is in conflict with the currently applied Orientation
     *
     * @param gravity
     *            One of the {@link Gravity} constants.
     */
    public void setHorizontalGravity(final Gravity gravity) {
        mRowLayout.setGravity(gravity);
    }

    @Override
    protected void layoutChildren() {
        mRowLayout.dumpCaches();
        mColumnLayout.dumpCaches();
        super.layoutChildren();
    }

    /**
     * Set the vertical {@link Gravity} of the layout.
     * The new gravity can be rejected if it is in conflict with the currently applied Orientation
     *
     * @param gravity
     *            One of the {@link Gravity} constants.
     */
    public void setVerticalGravity(final Gravity gravity) {
        mColumnLayout.setGravity(gravity);
    }

    /**
     * @return horizontal {@link Gravity} of the layout.
     */
    public Gravity getHorizontalGravity() {
        return mRowLayout.getGravity();
    }

    /**
     * @return vertical {@link Gravity} of the layout.
     */
    public Gravity getVerticalGravity() {
        return mColumnLayout.getGravity();
    }

    /**
     * Specify the cell width.
     * @param width new cell width
     */
    public void setCellWidth(final float width) {
        mRowLayout.setSize(width);
    }

    /**
     * Specify the cell height.
     * @param height new cell height
     */
    public void setCellHeight(final float height) {
        mColumnLayout.setSize(height);
    }

    /**
     * Return the cell width. If width equals to 0 - cell width has not been setup
     */
    public float getCellWidht() {
        return mRowLayout.getSize();
    }

    /**
     * Return the cell height. If height equals to 0 - cell height has not been setup
     */
    public float getCellHeight() {
        return mColumnLayout.getSize();
    }

    @Override
    public void setDividerPadding(final float padding, final Axis axis) {
        switch(axis) {
            case X:
                mRowLayout.setDividerPadding(padding, axis);
                break;
            case Y:
                mColumnLayout.setDividerPadding(padding, axis);
                break;
            case Z:
            default:
                break;
        }
        super.setDividerPadding(padding, axis);
    }

    @Override
    public void enableOuterPadding(boolean enable) {
        mColumnLayout.enableOuterPadding(enable);
        mRowLayout.enableOuterPadding(enable);
        super.enableOuterPadding(enable);
    }

    @Override
    public void setOrientation(Orientation orientation) {
        if (init(orientation) && isValidLayout(orientation)) {
            super.setOrientation(orientation);
        }
    }

    @Override
    public void invalidate() {
        mRowLayout.invalidate();
        mColumnLayout.invalidate();
        init(mOrientation);
        super.invalidate();
    }

    @Override
    protected void invalidate(final int dataIndex) {
        mRowLayout.invalidate(dataIndex);
        mColumnLayout.invalidate(dataIndex);
        super.invalidate(dataIndex);
    }


    @Override
    protected void layoutChild(final int dataIndex) {
        mRowLayout.layoutChild(dataIndex);
        mColumnLayout.layoutChild(dataIndex);
        super.layoutChild(dataIndex);
    }

    @Override
    protected Widget measureChild(final int dataIndex, boolean calculateOffset) {
        mColumnLayout.measureChild(dataIndex, calculateOffset);
        mRowLayout.measureChild(dataIndex, calculateOffset);
        return super.measureChild(dataIndex, calculateOffset);
    }


    @Override
    protected float preMeasureNext(final List<Widget> measuredChildren,
            final Axis axis, final Direction direction) {
        float totalSize = Float.NaN;
        int dataIndex = getOrientationLayout().getNextDataId(axis, direction);

        if (dataIndex >= 0) {
            int count =  getOrientationLayout().mCaches.size();
            float max = 0;
            int sign = (direction == Direction.BACKWARD ? 1 : -1);
            while (dataIndex >= 0 && dataIndex < mContainer.size() && count-- > 0) {
                Widget widget = measureChild(dataIndex);
                float sizeWithPadding  = getMeasuredChildSizeWithPadding(dataIndex, axis);
                if (!Float.isNaN(sizeWithPadding)) {
                    max = Math.max(max, sizeWithPadding);
                }

                if (widget != null && measuredChildren != null) {
                    measuredChildren.add(widget);
                }
                dataIndex -= sign;
            }
            if (max > 0) {
                totalSize = sign * max;
            }
        }
        return totalSize;
    }

    @Override
    protected void measureUntilFull(final int centerDataIndex, final Collection<Widget> measuredChildren) {
// TODO: only left & top centralization is supported for now. Need to support center and right as well
        // no preferred position, just feed all data starting from beginning.
        super.measureUntilFull(centerDataIndex == -1 ? 0 : centerDataIndex, measuredChildren);
    }

    @Override
    protected float getDistanceToChild(int dataIndex, Axis axis) {
        return getOrientationLayout().getDistanceToChild(dataIndex, axis);
    }

    @Override
    protected Direction getDirectionToChild(int dataIndex, Axis axis) {
        return getOrientationLayout().getDirectionToChild(dataIndex, axis);
    }

    @Override
    protected int getCenterChild() {
        int id = -1;
        Set<Integer> columns = mColumnLayout.getCenterChildren();
        Set<Integer> rows = mRowLayout.getCenterChildren();
        for (int c: columns) {
            if (rows.contains(c)) {
                id = c;
                break;
            }
        }
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "getCenterChild [%d]", id);

        return id;
    }

    @Override
    protected float getChildSize(int dataIndex, Axis axis) {
        return getOrientationLayout().getChildSize(dataIndex, axis);
    }

    @Override
    protected void resetChildLayout(int dataIndex) {
        mColumnLayout.resetChildLayout(dataIndex);
        mRowLayout.resetChildLayout(dataIndex);
    }

    @Override
    protected boolean postMeasurement() {
        boolean retCol =  mColumnLayout.postMeasurement();
        boolean retRow =  mRowLayout.postMeasurement();
        return retCol && retRow;
    }

    @Override
    protected float getMeasuredChildSizeWithPadding(final int dataIndex, final Axis axis) {
        return getOrientationLayout().getMeasuredChildSizeWithPadding(dataIndex, axis);
    }

    @Override
    public void shiftBy(final float offset, final Axis axis) {
        getOrientationLayout().shiftBy(offset, axis);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GridLayout)) return false;
        if (!super.equals(o)) return false;

        GridLayout that = (GridLayout) o;

        if (mRowCount != that.mRowCount) return false;
        return mColumnCount == that.mColumnCount;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + mRowCount;
        result = 31 * result + mColumnCount;
        return result;
    }

    @Override
    protected Layout clone() {
        return new GridLayout(this);
    }

    private ChunkedLinearLayout getOrientationLayout() {
        return mOrientation == Orientation.VERTICAL ?
                mColumnLayout : mRowLayout;
    }

    private boolean init(Orientation orientation) {
        boolean ret = false;
        switch(orientation) {
            case VERTICAL:
                if (mColumnCount == 0) {
                    Log.w(TAG, "Invalid layout: number of columns is not " +
                            "defined for VERTICALY oriented grid!");
                } else {
                    mRowLayout.setChunkBreaker(new ChunkBreakerBy(mColumnCount));
                    mColumnLayout.setChunkBreaker(new ChunkBreakerTo(mColumnCount));
                    mRowLayout.enableViewPort(mApplyViewPort);
                    mColumnLayout.enableViewPort(mApplyViewPort);

                    mRowLayout.forcePostMeasurement(true);
                    mColumnLayout.forcePostMeasurement(false);

                    ret = true;
                }
                break;
            case HORIZONTAL:
                if (mRowCount == 0) {
                    Log.w(TAG, "Invalid layout: number of columns is not " +
                            "defined for HORIZONTALLY oriented grid!");
                } else {
                    mRowLayout.setChunkBreaker(new ChunkBreakerTo(mRowCount));
                    mColumnLayout.setChunkBreaker(new ChunkBreakerBy(mRowCount));
                    mRowLayout.enableViewPort(mApplyViewPort);
                    mColumnLayout.enableViewPort(mApplyViewPort);

                    mRowLayout.forcePostMeasurement(false);
                    mColumnLayout.forcePostMeasurement(true);

                    ret = true;
                }
                break;
            case STACK:
            default:
                Log.w(TAG, "Unsupported orientation %s", mOrientation);
                break;
        }
        return ret;
    }

    interface ChunkBreaker {
        int getChunkSize();
        int getNumOfChunks();
        int getChunkIndex(int pos);
        int getPositionInChunk(int pos);
    }

    class ChunkBreakerBy implements ChunkBreaker {
        private int mChunkSize;

        ChunkBreakerBy(final int chunkSize) {
            mChunkSize = chunkSize;
        }

        public int getChunkSize() {
            return mChunkSize;
        }

        public int getNumOfChunks() {
            return -1;
        }

        public int getChunkIndex(final int pos) {
            return pos / mChunkSize;
        }

        public int getPositionInChunk(int pos) {
            return pos % mChunkSize;
        }
    }

    class ChunkBreakerTo implements ChunkBreaker {
        private int mNumOfChunks;
        ChunkBreakerTo(final int numOfChunks) {
            mNumOfChunks = numOfChunks;
        }

        public int getChunkSize() {
            return -1;
        }

        public int getNumOfChunks() {
            return mNumOfChunks;
        }

        public int getChunkIndex(int pos) {
            return pos % mNumOfChunks;
        }

        public int getPositionInChunk(int pos) {
            return pos / mNumOfChunks;
        }
    }
}
