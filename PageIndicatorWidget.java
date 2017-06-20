package com.samsung.smcl.vr.widgets;

import com.samsung.smcl.utility.Log;

import org.gearvrf.GVRContext;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PageIndicatorWidget extends CheckableGroup {
    private Set<OnPageSelectedListener> mListeners = new LinkedHashSet<>();

    private float mPageIndicatorButtonWidth,  mPageIndicatorButtonHeight;
    private int mCurrentPage;
    private static final String TAG = PageIndicatorWidget.class.getSimpleName();

    public interface OnPageSelectedListener {
        void onPageSelected(final int pageId);
    }

    public PageIndicatorWidget(GVRContext context, int numIndicators, int defaultPageId,
                               float indicatorWidth, float indicatorHeight) {
        super(context, 0, 0);

        Log.d(TAG, "PageIndicatorWidget numIndicators = %d defaultPageId = %d", numIndicators, defaultPageId);
        mPageIndicatorButtonWidth = indicatorWidth;
        mPageIndicatorButtonHeight = indicatorHeight;
        getDefaultLayout().setDividerPadding(mPageIndicatorButtonHeight / 2, Layout.Axis.Y);

        if (numIndicators > 0) {
            addIndicatorChildren(numIndicators);
            check(defaultPageId);
        }
    }

    private void addIndicatorChildren(int numIndicators) {
        while (numIndicators-- > 0) {
            PageIndicatorButton buttonWidget = new PageIndicatorButton(getGVRContext(),
                    mPageIndicatorButtonWidth, mPageIndicatorButtonHeight);
            addChild(buttonWidget);
        }
    }

    private void removeIndicatorChildren(int numIndicators) {
        List<PageIndicatorButton> children = getCheckableChildren();
        for (Widget child: children) {
            if (numIndicators-- <= 0) {
                break;
            }
            removeChild(child);
        }
    }

    @Override
    protected <T extends Widget & Checkable> void notifyOnCheckChanged(final T checkableWidget) {
        super.notifyOnCheckChanged(checkableWidget);
        if (checkableWidget.isChecked()) {
            mCurrentPage = getCheckableChildren().indexOf(checkableWidget);
            Log.d(TAG, "onCheckChanged mCurrentPage = %d", mCurrentPage);

            final Object[] listeners;
            synchronized (mListeners) {
                listeners = mListeners.toArray();
            }
            for (Object listener : listeners) {
                ((OnPageSelectedListener) listener).onPageSelected(mCurrentPage);
            }
        }
    }

    public <T extends Widget & Checkable> boolean addOnPageSelectedListener(OnPageSelectedListener listener) {
        final boolean added;
        synchronized (mListeners) {
            added = mListeners.add(listener);
        }
        if (added) {
            listener.onPageSelected(mCurrentPage);
        }
        return added;
    }

    public boolean removeOnPageSelectedListener(OnPageSelectedListener listener) {
        synchronized (mListeners) {
            return mListeners.remove(listener);
        }
    }

    public int getPageCount() {
        return getCheckableCount();
    }

    public int setPageCount(final int num) {
        int diff = num - getCheckableCount();

        if (diff > 0) {
            addIndicatorChildren(diff);
        } else if (diff < 0) {
            removeIndicatorChildren(-diff);
        }
        if (mCurrentPage >=num ) {
            mCurrentPage = 0;
        }
        setCurrentPage(mCurrentPage);
        return diff;
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public boolean setCurrentPage(final int page) {
        Log.d(TAG, "setPageId pageId = %d", page);
        return page >= 0 && page < getCheckableCount() ? check(page) : false;
    }

    private class PageIndicatorButton extends CheckableButton {
        private final String TAG = PageIndicatorButton.class.getSimpleName();
        float iWidth, iHeight;

        private PageIndicatorButton(GVRContext context, final float width,
                                    final float height) {
            super(context, 0, 0);
            iWidth = width;
            iHeight = height;
        }

        @Override
        protected Widget createGraphicWidget() {
            return new PageIndicatorButton.Graphic(getGVRContext(), iWidth, iHeight);
        }

        private class Graphic extends Widget {
            Graphic(GVRContext context, float width, float height) {
                super(context, width, height);
            }
        }
    }
}
