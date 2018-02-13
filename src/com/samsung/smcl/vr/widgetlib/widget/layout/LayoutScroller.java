package com.samsung.smcl.vr.widgetlib.widget.layout;

import java.util.HashSet;
import java.util.Set;


import android.content.Context;
import android.database.DataSetObserver;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.samsung.smcl.vr.widgetlib.log.Log;
import static com.samsung.smcl.vr.widgetlib.main.Utility.equal;

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
	private Set<OnScrollListener> mOnScrollListeners = new HashSet<>();
    private Set<OnPageChangedListener> mOnPageChangedListeners = new HashSet<>();

	public interface OnScrollListener {
        void onScrollStarted(int startPosition);
        void onScrollFinished(int finalPosition);
	}

    public interface OnPageChangedListener {
        void pageChanged(int page);
    }

	public interface ScrollableList {
	    int getScrollingItemsCount();
	    float getViewPortWidth();
        float getViewPortHeight();
        float getViewPortDepth();
	    boolean scrollToPosition(final int pos, final OnScrollListener listener);
	    boolean scrollByOffset(final float xOffset, final float yOffset, final float zOffset,
                               final OnScrollListener listener);
	    void registerDataSetObserver(final DataSetObserver observer);
        void unregisterDataSetObserver(final DataSetObserver observer);
        int getCurrentPosition();
    }

	protected DataSetObserver mObserver = new DataSetObserver() {
	    @Override
	    public void onChanged() {
	        int count = mScrollable.getScrollingItemsCount();
	        mPageCount = mSupportScrollByPage ?
	                (int) Math.ceil((float)count/(float)mPageSize) : 1;

	        if (mCurrentItemIndex >= count) {
	            mCurrentItemIndex = count - 1;
	            scrollToPosition(mCurrentItemIndex);
	        }
	    }

	    @Override
	    public void onInvalidated() {
	        int count = mScrollable.getScrollingItemsCount();
	        mPageCount = mSupportScrollByPage ?
	                (int) Math.ceil((float)count/(float)mPageSize) : 1;

	        if (mCurrentItemIndex > 0) {
	            mCurrentItemIndex = 0;
	            scrollToPosition(mCurrentItemIndex);
	        }
	    }
	};

	public LayoutScroller(final Context context, final ScrollableList scrollable) {
        this(context, scrollable, false, 0, 1, scrollable.getCurrentPosition());
    }

	public LayoutScroller(final Context context, final ScrollableList scrollable,
	        final boolean scrollOver) {
		this(context, scrollable, scrollOver, 0, 1, scrollable.getCurrentPosition());
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

    public void addOnScrollListener(final OnScrollListener listener) {
        mOnScrollListeners.add(listener);
    }

    public void removeOnScrollListener(final OnScrollListener listener) {
        mOnScrollListeners.remove(listener);
    }


    public void addOnPageChangedListener(final OnPageChangedListener listener) {
        mOnPageChangedListeners.add(listener);
    }

    public void removeOnPageChangedListener(final OnPageChangedListener listener) {
        mOnPageChangedListeners.remove(listener);
    }

    static final float VELOCITY_MAX = 30000;
    static final float MAX_VIEWPORT_LENGTHS = 4;
    static final float MAX_SCROLLING_DISTANCE = 500;

    public boolean fling(float velocityX, float velocityY, float velocityZ) {
        boolean scrolled = true;
        float viewportX  = mScrollable.getViewPortWidth();
        if (Float.isNaN(viewportX)) {
            viewportX = 0;
        }
        float maxX = Math.min(MAX_SCROLLING_DISTANCE,
                viewportX * MAX_VIEWPORT_LENGTHS);

        float viewportY  = mScrollable.getViewPortHeight();
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

        if (equal(xOffset, 0)) {
            xOffset = Float.NaN;
        }

        if (equal(yOffset, 0)) {
            yOffset = Float.NaN;
        }

// TODO: Think about Z-scrolling
        mScrollable.scrollByOffset(xOffset, yOffset, Float.NaN, mInternalScrollListener);

        return scrolled;
    }

    public boolean flingToPosition(int vilocity) {
        boolean scrolled = true;
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "flingToPosition() startIndex =%d vilocity = %d",
                mCurrentItemIndex, vilocity);

        vilocity /= -mScrollable.getScrollingItemsCount();

        if (!mScroller.isFinished()) {
            mScroller.forceFinished(true);
        }

        mScroller.fling(mCurrentItemIndex, 0, vilocity, 0,
            -mScrollable.getScrollingItemsCount(), 2 * mScrollable.getScrollingItemsCount(), 0, 0);
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

        if (mSupportScrollByPage &&
                (mScrollOver || (pageNumber >= 0 && pageNumber <= mPageCount - 1))) {
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
	    return scrollToItem(mScrollable.getScrollingItemsCount() - 1);
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
        int count = mScrollable.getScrollingItemsCount();
        if (mSupportScrollByPage && mCurrentItemIndex >= 0 &&
                mCurrentItemIndex < count) {
            currentPage = (Math.min(mCurrentItemIndex + mPageSize - 1, count - 1)/mPageSize);
        }
        return currentPage;
    }

    private int getFirstItemIndexOnPage(final int pageNumber) {
        int index = 0;
        if (mSupportScrollByPage) {
            index = (pageNumber * mPageSize);
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "getFirstItemIndexOnPage = %d", index);
        }
        return index;
    }

    private int getValidPosition(int position) {
        int pos = position;
        if (pos >= mScrollable.getScrollingItemsCount()) {
            if (mScrollOver) {
                pos %= mScrollable.getScrollingItemsCount();
            } else {
                pos = mScrollable.getScrollingItemsCount() - 1;
            }
        } else if (pos < 0) {
            if (mScrollOver) {
                pos %= mScrollable.getScrollingItemsCount();
                pos += mScrollable.getScrollingItemsCount();
                pos %= mScrollable.getScrollingItemsCount();
            } else {
                pos = 0;
            }
        }

        pos = (pos / mDeltaScrollAmount) *
                mDeltaScrollAmount;
        return pos;
    }

    private OnScrollListener mInternalScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStarted(int startPosition) {
            for (OnScrollListener listener: mOnScrollListeners) {
                listener.onScrollStarted(startPosition);
            }
        }

        @Override
        public void onScrollFinished(int finalPosition) {
            mCurrentItemIndex = finalPosition;
            for (OnScrollListener listener: mOnScrollListeners) {
                listener.onScrollFinished(mCurrentItemIndex);
            }

            int curPage = getCurrentPage();
            for (OnPageChangedListener listener: mOnPageChangedListeners) {
                listener.pageChanged(curPage);
            }
        }
    };

    private boolean scrollToPosition(int newPosition) {
        boolean scrolled = false;
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "scrollToPosition() mCurrentItemIndex=%d newPosition = %d",
                mCurrentItemIndex, newPosition);

        if (newPosition != mCurrentItemIndex) {
            int pos = getValidPosition(newPosition);
            scrolled = mScrollable.scrollToPosition(pos, mInternalScrollListener);
        }

        return scrolled;
    }
}
