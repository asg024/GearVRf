package com.samsung.smcl.vr.widgets;

import java.util.ArrayList;
import java.util.List;

import com.samsung.smcl.utility.Log;
import com.samsung.smcl.utility.Utility;
import com.samsung.smcl.vr.widgets.Layout.Axis;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

public class LayoutScroller {

	private static final String TAG = LayoutScroller.class.getSimpleName();;

    private int mCurrentItemIndex;

	private final ScrollableList mScrollable;
	private final int mPageSize;
    private final boolean mScrollOver;
    private final boolean mSupportScrollByPage;

    private int mPageCount;
    private int mDeltaScrollAmount;
	private Scroller mScroller;
	private static final int SCROLL_DURATION = 5000; // 5 sec
	private List<OnScrollListener> listeners = new ArrayList<OnScrollListener>();

	public interface OnScrollListener {
        void onScrollStarted(int startPosition);
        void onScrollFinished(int finalPosition);
	}

	interface ScrollableList {
	    int getCount();
	    float getViewPort(final Axis axis);
	    boolean scrollToPosition(final int pos);
	    boolean scrollByOffset(final float xOffset, final float yOffset, final float zOffset);
	    void registerDataSetObserver(final DataSetObserver observer);
        void unregisterDataSetObserver(final DataSetObserver observer);
	}

	protected DataSetObserver mObserver = new DataSetObserver() {
	    @Override
	    public void onChanged() {
	        int count = mScrollable.getCount();
	        mPageCount = mSupportScrollByPage ?
	                (int) Math.ceil((float)count/(float)mPageSize) : 1;

	        if (mCurrentItemIndex >= count) {
	            mCurrentItemIndex = count - 1;
	            scrollToPosition(mCurrentItemIndex);
	        }
	    }

	    @Override
	    public void onInvalidated() {
	        int count = mScrollable.getCount();
	        mPageCount = mSupportScrollByPage ?
	                (int) Math.ceil((float)count/(float)mPageSize) : 1;

	        if (mCurrentItemIndex > 0) {
	            mCurrentItemIndex = 0;
	            scrollToPosition(mCurrentItemIndex);
	        }
	    }
	};

	public LayoutScroller(final Context context, final ScrollableList scrollable) {
        this(context, scrollable, false, 1, 0, 0);
    }

	public LayoutScroller(final Context context, final ScrollableList scrollable,
	        final boolean scrollOver) {
		this(context, scrollable, scrollOver, 1, 0, 0);
	}

	public LayoutScroller(final Context context, final ScrollableList scrollable,
	        final boolean scrollOver,
	        final int pageSize, int deltaScrollAmount, final int currentIndex) {
	    if (scrollable == null) {
	        throw new IllegalArgumentException("scrollable cannot be null!");
	    }
	    mScroller = new Scroller(context, new LinearInterpolator());
	    mScroller.extendDuration(SCROLL_DURATION);
	    mScrollable = scrollable;

		mScrollOver = scrollOver;
		mPageSize = pageSize;
		mSupportScrollByPage = pageSize > 0;
		mCurrentItemIndex = currentIndex;
		mDeltaScrollAmount = deltaScrollAmount;
		mScrollable.registerDataSetObserver(mObserver);
	}

    public void addListener(final OnScrollListener listener) {
        listeners.add(listener);
    }

    public void removeListener(final OnScrollListener listener) {
        listeners.remove(listener);
    }

    static final float VELOCITY_MAX = 30000;
    static final float MAX_VIEWPORT_LENGTHS = 4;
    static final float MAX_SCROLLING_DISTANCE = 500;

    public boolean fling(float velocityX, float velocityY, float velocityZ) {
        boolean scrolled = true;
        float viewportX  = mScrollable.getViewPort(Axis.X);
        if (Float.isNaN(viewportX)) {
            viewportX = 0;
        }
        float maxX = Math.min(MAX_SCROLLING_DISTANCE,
                viewportX * MAX_VIEWPORT_LENGTHS);

        float viewportY  = mScrollable.getViewPort(Axis.Y);
        if (Float.isNaN(viewportY)) {
            viewportY = 0;
        }
        float maxY = Math.min(MAX_SCROLLING_DISTANCE,
                viewportY * MAX_VIEWPORT_LENGTHS);

        float xOffset = (maxX * velocityX)/VELOCITY_MAX;
        float yOffset = (maxY * velocityY)/VELOCITY_MAX;

        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "fling() velocity = [%f, %f, %f] offset = [%f, %f]",
                velocityX, velocityY, velocityZ,
                xOffset, yOffset);

        if (Utility.equal(xOffset, 0)) {
            xOffset = Float.NaN;
        }

        if (Utility.equal(yOffset, 0)) {
            yOffset = Float.NaN;
        }

// TODO: Think about Z-scrolling
        mScrollable.scrollByOffset(xOffset, yOffset, Float.NaN);

        return scrolled;
    }

    public boolean flingToPosition(int vilocity) {
        boolean scrolled = true;
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "flingToPosition() startIndex =%d vilocity = %d",
                mCurrentItemIndex, vilocity);

        vilocity /= -mScrollable.getCount();

        if (!mScroller.isFinished()) {
            mScroller.forceFinished(true);
        }

        mScroller.fling(mCurrentItemIndex, 0, vilocity, 0,
            -mScrollable.getCount(), 2 * mScrollable.getCount(), 0, 0);
        mScroller.computeScrollOffset();
        scrollToPosition(mScroller.getFinalX());
        return scrolled;
    }

    public int scrollToNextPage() {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "scrollToNextPage getCurrentPage() = %d currentIndex = %d",
                getCurrentPage(), mCurrentItemIndex);

        if (mSupportScrollByPage) {
            scrollToPage(getCurrentPage() + 1);
        } else {
            Log.w(TAG, "Pagination is not enabled!");
        }

        return mCurrentItemIndex;
	}

	public int scrollToPrevPage() {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "scrollToPrevPage getCurrentPage() = %d currentIndex = %d",
                getCurrentPage(), mCurrentItemIndex);

        if (mSupportScrollByPage) {
            scrollToPage(getCurrentPage() - 1);
        } else {
            Log.w(TAG, "Pagination is not enabled!");
        }
        return mCurrentItemIndex;
	}

	public int scrollToPage(int pageNumber) {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "scrollToPage pageNumber = %d mPageCount = %d",
                pageNumber, mPageCount);

        if (mSupportScrollByPage) {
            scrollToItem(getFirstItemIndexOnPage(pageNumber));
        } else {
            Log.w(TAG, "Pagination is not enabled!");
        }
        return mCurrentItemIndex;
	}

    public int scrollToBegining() {
        return scrollToItem(0);
    }

	public int scrollToEnd() {
	    return scrollToItem(mScrollable.getCount() - 1);
    }

	public int scrollToNextItem() {
	    return scrollToItem(mCurrentItemIndex + 1);
	}

	public int scrollToPrevItem() {
	    return scrollToItem(mCurrentItemIndex - 1);
    }

    public int scrollToItem(int position) {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "scrollToItem position = %d", position);
        scrollToPosition(position);
        return mCurrentItemIndex;
    }

    public int getCurrentPage() {
        int currentPage = 1;
        if (mSupportScrollByPage && mCurrentItemIndex >= 0 &&
                mCurrentItemIndex < mScrollable.getCount()) {
            currentPage = (int) Math.ceil(
            (float)(mCurrentItemIndex + 1)/(float)mPageSize);
        }
        return currentPage;
    }

    private int getFirstItemIndexOnPage(final int pageNumber) {
        int index = 0;
        if (mSupportScrollByPage) {
            index = ((pageNumber - 1) * mPageSize);
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "getFirstItemIndexOnPage = %d", index);
        }
        return index;
    }

    private int getValidPosition(int position) {
        int pos = position;
        if (pos >= mScrollable.getCount()) {
            if (mScrollOver) {
                pos %= mScrollable.getCount();
            } else {
                pos = mScrollable.getCount() - 1;
            }
        } else if (pos < 0) {
            if (mScrollOver) {
                pos %= mScrollable.getCount();
                pos += mScrollable.getCount();
                pos %= mScrollable.getCount();
            } else {
                pos = 0;
            }
        }

        pos = (pos / mDeltaScrollAmount) *
                mDeltaScrollAmount;
        return pos;
    }

    private boolean scrollToPosition(int newPosition) {
        boolean scrolled = false;
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "scrollToPosition() mCurrentItemIndex=%d newPosition = %d",
                mCurrentItemIndex, newPosition);

        if (newPosition != mCurrentItemIndex) {
            for (OnScrollListener listener: listeners) {
                listener.onScrollStarted(mCurrentItemIndex);
            }
            int pos = getValidPosition(newPosition);

            scrolled = mScrollable.scrollToPosition(pos);

            if (scrolled) {
                mCurrentItemIndex = pos;
            }
            for (OnScrollListener listener: listeners) {
                listener.onScrollFinished(mCurrentItemIndex);
            }
        }

        return scrolled;
    }
}
