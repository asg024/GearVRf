package com.samsung.smcl.vr.widgets;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import android.database.DataSetObserver;

import com.samsung.smcl.utility.Log;
import com.samsung.smcl.utility.Utility;

// TODO: Add "holder" scene objects: these are what we will translate & rotate for layout
// TODO: Add rotate-to-item
// TODO: Split layout calculations out into main or background thread
// TODO: Recycle views
// TODO: Add support for animation (in particular, we need to handle layout changes)
// TODO: Scrolling (this is different from rotating, a la AppRing)
// TODO: Selection
public class RingList extends GroupWidget {

    public interface OnItemFocusListener {
        public void onFocus(int position, boolean focused);
        public void onLongFocus(int position);
    }

    private final Set<OnItemFocusListener> mItemFocusListeners = new LinkedHashSet<OnItemFocusListener>();

    /**
     * Add a listener for {@linkplain OnItemFocusListener#onFocus(boolean) focus}
     * and {@linkplain OnItemFocusListener#onLongFocus() long focus} notifications
     * for this object.
     *
     * @param listener
     *            An implementation of {@link OnItemFocusListener}.
     * @return {@code true} if the listener was successfully registered,
     *         {@code false} if the listener is already registered.
     */
    public boolean addOnItemFocusListener(final OnItemFocusListener listener) {
        return mItemFocusListeners.add(listener);
    }

    /**
     * Remove a previously {@linkplain #addOnItemFocusListener(OnItemFocusListener)
     * registered} focus notification {@linkplain OnItemFocusListener listener}.
     *
     * @param listener
     *            An implementation of {@link OnItemFocusListener}
     * @return {@code true} if the listener was successfully unregistered,
     *         {@code false} if the specified listener was not previously
     *         registered with this object.
     */
    public boolean removeOnItemFocusListener(final OnItemFocusListener listener) {
        return mItemFocusListeners.remove(listener);
    }
    /**
     * Construct a new {@code RingList} instance with a {@link #setRho(double)
     * radius} of zero.
     *
     * @param gvrContext
     *            The active {@link GVRContext}.
     */
    public RingList(final GVRContext gvrContext, GVRSceneObject sceneObj) {
        super(gvrContext, sceneObj);
    }

    /**
     * Construct a new {@code RingList} instance with the specified radius.
     *
     * @param gvrContext
     *            The active {@link GVRContext}.
     * @param rho
     *            The radius of the {@code RingList}.
     */
    public RingList(final GVRContext gvrContext, GVRSceneObject sceneObj,
            final double rho) {
        super(gvrContext, sceneObj);
        mRho = rho;
    }

    /**
     * Construct a new {@code RingList} instance with the specified radius.
     *
     * @param gvrContext
     *            The active {@link GVRContext}.
     * @param rho
     *            The radius of the {@code RingList}.
     */

    public RingList(final GVRContext gvrContext, float width, float height,
            final double rho) {
        super(gvrContext, width, height);
        mRho = rho;
    }

    /**
     * Construct a new {@code RingList} instance with the specified radius, page
     * number, max number of items, incrementation of items per page.
     *
     * @param gvrContext
     *            The active {@link GVRContext}.
     * @param rho
     *            The radius of the {@code RingList}.
     * @param pageNumber
     *            The page number {@code RingList}.
     * @param maxItemsDisplayed
     *            The max number of items {@code RingList}.
     * @param itemIncrementPerPage
     *            The increment of items per page of the {@code RingList}.
     */

    public RingList(final GVRContext gvrContext, float width, float height,
            final double rho, int pageNumber, int maxItemsDisplayed,
            int itemIncrementPerPage) {
        super(gvrContext, width, height);
        mRho = rho;
        mPageNumber = pageNumber;
        mMaxItemsDisplayed = maxItemsDisplayed;
        mItemIncrementPerPage = itemIncrementPerPage;
    }

    /**
     * Set the {@link Adapter} for the {@code RingList}. The list will
     * immediately attempt to load data from the adapter.
     * {@link Adapter#getView(int, GVRSceneObject, GVRSceneObject)} is
     * guaranteed to be called from the GL thread.
     *
     * @param adapter
     *            An adapter or {@code null} to clear the list.
     */
    public void setAdapter(final Adapter adapter) {
        onChanged(adapter);
    }

    public boolean getBalanceLayout() {
        return mBalanceLayout;
    }

    public void setBalanceLayout(boolean balanceLayout) {
        if (balanceLayout != mBalanceLayout) {
            mBalanceLayout = balanceLayout;
            layout();
        }
    }

    private int mPageNumber = 0;
    private int mMaxItemsDisplayed = 0;
    private int mItemIncrementPerPage;
    private boolean isWrapAroundLayout = false;

    /**
     * Calculates the each angular width for each mItems
     *
     * @return array of floats each mItems' angular width
     *
     */
    private float[] calculateAngularWidth() {
        final int numItems = mItems.size();
        float[] angularWidths = new float[numItems];

        float totalAngularWidths = 0;
        for (int i = 0; i < numItems; ++i) {
            angularWidths[i] = LayoutHelpers.calculateAngularWidth(mItems
                    .get(i), mRho);
            totalAngularWidths += angularWidths[i];
            Log.d(TAG, "layout(): angular width at %d: %f", i, angularWidths[i]);
        }

        if (mItemPadding < 0) {
            float uniformAngularWidth = (360.f - totalAngularWidths)
                    / (float) mMaxItemsDisplayed;
            for (int i = 0; i < numItems; ++i) {
                angularWidths[i] = uniformAngularWidth;
            }
        }

        return angularWidths;
    }

    /**
     * Calculates the total angular width for mItems
     *
     * @return total angular width for mItems
     *
     */
    private float calculateTotalAngularWidth() {
        float totalAngularWidths = 0;
        for (int i = 0; i < mItems.size(); ++i) {
            totalAngularWidths += LayoutHelpers.calculateAngularWidth(mItems
                    .get(i), mRho);
        }

        return totalAngularWidths;
    }

    private void wrapAroundLayout() {
        if (mItems.isEmpty()) {
            Log.d(TAG, "wrapAroundLayout(): no items to layout!");
            return;
        }
        final int numItems = mItems.size();

        final float[] angularWidths = calculateAngularWidth();

        int itemIndex = mPageNumber * mItemIncrementPerPage;
        int totalNumPerPage = Math.min(numItems, mMaxItemsDisplayed);
        int numItemsFirstHalf = (totalNumPerPage / 2);

        float phi = 0;
        for (int i = 0; i < numItemsFirstHalf; i++) {
            int appListIndex = itemIndex + i;
            if (appListIndex >= numItems) {
                appListIndex = appListIndex % numItems;
            }

            mItems.get(appListIndex).setRotation(1, 0, 0, 0);
            mItems.get(appListIndex).setPosition(0, 0, -(float) mRho);
            mItems.get(appListIndex).rotateByAxisWithPivot(phi, 0, 1, 0, 0, 0,
                                                           0);
            phi -= angularWidths[appListIndex] + mItemPadding;

        }

        phi = angularWidths[0];

        int numItemsSecondHalf = totalNumPerPage - numItemsFirstHalf;
        int index = numItems - (numItemsSecondHalf - itemIndex);

        for (int j = index + numItemsSecondHalf - 1; j >= index; j--) {
            int appListIndex = j;
            if (j >= numItems) {
                appListIndex = j % numItems;
            }

            mItems.get(appListIndex).setRotation(1, 0, 0, 0);
            mItems.get(appListIndex).setPosition(0, 0, -(float) mRho);
            mItems.get(appListIndex).rotateByAxisWithPivot(phi, 0, 1, 0, 0, 0,
                                                           0);
            phi += angularWidths[appListIndex] - mItemPadding;
        }
    }

    /**
     * @return The angular separation between list items, in degrees.
     */
    public float getItemPadding() {
        return mItemPadding;
    }

    /**
     * Sets the angular separation, in degrees, between items in the list. The
     * separation is between the right-most vertex of one item and the left-most
     * vertex of the next item in the list.
     *
     * @param itemPadding
     *            Separation between items, in degrees.
     */
    public void setItemPadding(final float itemPadding) {
        if (Utility.equal(mItemPadding, itemPadding) == false) {
            mItemPadding = itemPadding;
            layout();
        }
    }

    /**
     * Set to enable wrap around layout {@code RingList}.
     */
    public void setWrapAroundLayout(boolean isWrapAround) {
        isWrapAroundLayout = isWrapAround;
    }

    /**
     * @return The boolean for wrap around layout {@code RingList}.
     */
    public boolean getWrapAroundLayout() {
        return isWrapAroundLayout;
    }

    /**
     * @return The radius of the {@code RingList}.
     */
    public double getRho() {
        return mRho;
    }

    /**
     * Set the radius of the {@code RingList}.
     *
     * @param rho
     *            The new radius.
     */
    public void setRho(final double rho) {
        if (Utility.equal(mRho, rho) == false) {
            mRho = rho;
            layout();
        }
    }

    private void clear() {
        List<Widget> children = new ArrayList<Widget>(getChildren());
        Log.d(TAG, "clear(): removing %d children", children.size());
        for (Widget child : children) {
            removeChild(child, true);
        }
    }

    @Override
    protected void layout() {
        if (mItems.isEmpty()) {
            Log.d(TAG, "layout(): no items to layout!");
            return;
        }

        if (isWrapAroundLayout) {
            wrapAroundLayout();
            return;
        }

        final int numItems = mItems.size();
        Log.d(TAG, "layout(): laying out %d items", numItems);

        float phi = 0;
        final float[] angularWidths = calculateAngularWidth();

        if (mBalanceLayout && numItems > 0) {
            float totalAngularWidths = calculateTotalAngularWidth();
            phi = (totalAngularWidths / 2) - (angularWidths[0] / 2);
        }

        for (int i = 0; i < numItems; ++i) {
            Log.d(TAG, "layout(%s): phi at %d: %f", getName(), i, phi);
            mItems.get(i).setRotation(1, 0, 0, 0);
            mItems.get(i).setPosition(0, 0, -(float) mRho);
            mItems.get(i).rotateByAxisWithPivot(phi, 0, 1, 0, 0, 0, 0);
            phi -= angularWidths[i] + mItemPadding;
        }
    }

    private void onChanged() {
        onChanged(mAdapter);
    }

    private void onChanged(final Adapter adapter) {
        runOnGlThread(new Runnable() {
            @Override
            public void run() {
                if (adapter != mAdapter) {
                    if (mAdapter != null) {
                        mAdapter.unregisterDataSetObserver(mObserver);
                        clear();
                    }
                    mAdapter = adapter;
                    if (mAdapter != null) {
                        mAdapter.registerDataSetObserver(mObserver);
                    }
                }
                if (mAdapter == null) {
                    return;
                }
                onChangedImpl();
            }
        });
    }

    private void onChangedImpl() {

        final int itemCount = mAdapter.getCount();
        Log.d(TAG, "onChanged(): %d items", itemCount);
        int pos;

        Log.d(TAG, "onChanged(): %d views", mItems.size());
        // Recycle any items we already have

        for (pos = 0; pos < mItems.size() && pos < itemCount; ++pos) {
            Widget w = mAdapter.getView(pos, mItems.get(pos), RingList.this);
            w.addFocusListener(mFocusListener);
        }

        // Get any additional items
        for (; pos < itemCount; ++pos) {
            Widget item = null;
            try {
                item = mAdapter.getView(pos, null, RingList.this);
                if (item == null) {
                    break;
                }
                item.addFocusListener(mFocusListener);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
            mItems.add(item);
            Log.d(TAG, "onChanged(): added item at %d", pos);
            addChild(item, true);
        }

        // Trim unused items
        Log.d(TAG, "onChanged(): trimming: %b", pos < mItems.size());
        for (; pos < mItems.size(); ++pos) {
            Widget item = mItems.remove(pos);
            removeChild(item, true);
        }

        layout();

        List<Widget> children = getChildren();
        for (int i = 0; i < children.size(); ++i) {
            Widget child = children.get(i);
            Log.d(TAG,
                  "layout(): item at %d {%05.2f, %05.2f, %05.2f}, {%05.2f, %05.2f, %05.2f}",
                  i, child.getPositionX(), child.getPositionY(),
                  child.getPositionZ(), child.getRotationX(),
                  child.getRotationY(), child.getRotationZ());
        }
        Log.d(TAG, "onChanged(): child objects: %d", this.getChildren().size());
    }

    private OnFocusListener mFocusListener = new OnFocusListener() {
        @Override
        public boolean onFocus(final boolean focused, final Widget widget) {
            Log.d(TAG, "onFocus widget= %s focused [%b]", widget, focused);
            int position = mItems.indexOf(widget);
            if (position >= 0) {
                for (OnItemFocusListener listener : mItemFocusListeners) {
                    listener.onFocus(position, focused);
                }
            }
            return false;
        }

        @Override
        public boolean onLongFocus(Widget widget) {
            Log.d(TAG, "onLongFocus widget= %s", widget);
            int position = mItems.indexOf(widget);
            if (position >= 0) {
                for (OnItemFocusListener listener : mItemFocusListeners) {
                    listener.onLongFocus(position);
                }
            }
            return false;
        }
    };

    private Adapter mAdapter;
    private float mItemPadding;
    private List<Widget> mItems = new ArrayList<Widget>();
    private double mRho;
    private boolean mBalanceLayout;
    private DataSetObserver mObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            RingList.this.onChanged();
        }

        @Override
        public void onInvalidated() {
            RingList.this.onChanged();
        }
    };

    private static final String TAG = RingList.class.getSimpleName();
}
