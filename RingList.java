package com.samsung.smcl.vr.widgets;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import android.database.DataSetObserver;
import android.util.SparseBooleanArray;

import com.samsung.smcl.utility.Log;
import com.samsung.smcl.utility.Utility;

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

    private boolean mItemFocusEnabled = true;

    /**
     * @return Whether the {@link RingList} allows it items to be focused.
     */
    public boolean getItemFocusEnabled() {
        return mItemFocusEnabled;
    }

    /**
     * Sets {@linkplain Widget#setFocusEnabled(boolean) focus enabled} (or
     * disabled) for all children of the {@link RingList} that were fetched from
     * the {@link Adapter}. If this is called with {@code false}, any new items
     * gotten from the {@code Adapter} will have {@code setFocusEnabled(false)}
     * called on them. If called with {@code true}, {@code RingList} acts as
     * though it's received an {@link DataSetObserver#onChanged() on changed}
     * notification and refreshes its items with the {@code Adapter}.
     * <p>
     * This is a convenience method only, and the current state of focus
     * enabling for each displayed item is not tracked in any way.
     * {@code Adapters} should ensure that they enable or disable focus as
     * appropriate for their views.
     * 
     * @param enabled
     *            {@code True} to enable focus for all items, {@code false} to
     *            disable.
     */
    public void setItemFocusEnabled(boolean enabled) {
        if (enabled != mItemFocusEnabled) {
            mItemFocusEnabled = enabled;
            if (!mItemFocusEnabled) {
                Log.d(TAG, "setItemFocusEnabled(%s): item focus enabled: %b", getName(), enabled);
                for (ListItemHostWidget host : mItems) {
                    host.guestWidget.setFocusEnabled(enabled);
                }
            } else {
                // This will refresh the data for the items, allowing the
                // Adapter to reset focus enabling
                Log.d(TAG, "setItemFocusEnabled(%s): item focus enabled: %b", getName(), enabled);
                onChanged();
            }
        }
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

    public void setPageNumber(int page) {
        if (mPageNumber >= 0 && mPageNumber != page) {
            mPageNumber = page;
            requestLayout();
        }
    }

    public int getPageNumber() {
        return mPageNumber;
    }

    private int mPageNumber = 0;
    private int mMaxItemsDisplayed = 0;
    private int mItemIncrementPerPage = 0;
    private LayoutType layoutType = LayoutType.LEFT_ORDER;
    private int mAroundItemAtId = -1;

    public enum LayoutType {
        LEFT_ORDER,
        BALANCED,
        WRAP_AROUND_CENTER,
        AROUND_ITEM_AT, // can take the parameter int mAroundItemAtId
        AROUND_SELECTED_ITEM,
    }

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
                    .get(i).guestWidget, mRho);
            totalAngularWidths += angularWidths[i];
            Log.d(TAG, "layout(%s): angular width at %d: %f", getName(), i, angularWidths[i]);
        }

        if (mProportionalItemPadding) {
            int totalNumPerPage = mMaxItemsDisplayed == 0 ? mItems.size() : mMaxItemsDisplayed;
            float uniformAngularWidth = (360.f - totalAngularWidths)
                    / (float) totalNumPerPage;
            for (int i = 0; i < numItems; ++i) {
                angularWidths[i] += uniformAngularWidth;
                Log.d(TAG, "layout(): angular uniform width at %d: %f", i, angularWidths[i]);
            }
        }

        return angularWidths;
    }

    private void layoutAroundItemAt(final int position, final float[] angularWidths) {
        int firstItemIndex = mPageNumber * mItemIncrementPerPage;
        int totalNumPerPage = mMaxItemsDisplayed == 0 ? mItems.size() :
            Math.min(mItems.size(), mMaxItemsDisplayed);
        int lastItemIndex = firstItemIndex + totalNumPerPage - 1;

        float phi = basePhi;

        if (mItems.get(position).getPhi() == -1) {
            int anchorAt = position;

            if (position == -1 || position > lastItemIndex) {
                anchorAt = lastItemIndex;
            } else if (position < firstItemIndex) {
                anchorAt = firstItemIndex;
            }

            for (int i = anchorAt; --i >= firstItemIndex;) {
                if (mItems.get(position).getPhi() >= 0f) {
                    phi += (angularWidths[i] + mItemPadding)
                            - mItems.get(position).getPhi();
                }
            }
        }

        for (int i = firstItemIndex; i <= lastItemIndex; i++) {
            layoutItem(i, mItems.get(i), phi);
            phi -= angularWidths[i] + mItemPadding;
        }
    }

    private void wrapAroundCenterLayout(final float[] angularWidths) {
        final int numItems = mItems.size();

        int itemIndex = mPageNumber * mItemIncrementPerPage;
        int totalNumPerPage = mMaxItemsDisplayed == 0 ? numItems :
            Math.min(numItems, mMaxItemsDisplayed);
        int numItemsFirstHalf = (totalNumPerPage / 2);

        float phi = basePhi;
        for (int i = 0; i < numItemsFirstHalf; i++) {
            int appListIndex = itemIndex + i;
            if (appListIndex >= numItems) {
                appListIndex = appListIndex % numItems;
            }
            layoutItem(appListIndex, mItems.get(appListIndex), phi);
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
            layoutItem(appListIndex, mItems.get(appListIndex), phi);
            phi += angularWidths[appListIndex] - mItemPadding;
        }
    }

    private void leftLayout(final float[] angularWidths) {
        float phi = basePhi;

        int firstItemIndex = mPageNumber * mItemIncrementPerPage;
        int totalNumPerPage = mMaxItemsDisplayed == 0 ? mItems.size() :
            Math.min(mItems.size(), mMaxItemsDisplayed);

        int lastItemIndex = firstItemIndex + totalNumPerPage - 1;

        for (int i = firstItemIndex; i <= lastItemIndex; ++i) {
            layoutItem(i, mItems.get(i), phi);
            phi -= angularWidths[i] + mItemPadding;
        }
    }

    private void balancedLayout(final float[] angularWidths) {
        int firstItemIndex = mPageNumber * mItemIncrementPerPage;
        int totalNumPerPage = mMaxItemsDisplayed == 0 ? mItems.size() :
            Math.min(mItems.size(), mMaxItemsDisplayed);

        int lastItemIndex = firstItemIndex + totalNumPerPage - 1;

        float totalAngularWidths = 0;
        for (int i = firstItemIndex; i <= lastItemIndex; ++i) {
            totalAngularWidths +=  angularWidths[i];
        }

        float phi = basePhi + (totalAngularWidths + mItemPadding * (mItems.size() - 1)) / 2 - (angularWidths[0] / 2);

        Log.d(TAG, "balancedLayout(%s) : firstItemIndex %d lasttItemIndex %d ", getName(), firstItemIndex, lastItemIndex);
        for (int i = firstItemIndex; i <= lastItemIndex; ++i) {
            layoutItem(i, mItems.get(i), phi);
            phi -= angularWidths[i] + mItemPadding;
        }
    }


    private void layoutItem(final int index,  final ListItemHostWidget item, final float phi) {
        Log.d(TAG, "layout(%s): phi [%f]", item.getName(), phi);
        item.setRotation(1, 0, 0, 0);
        item.setPosition(0, 0, -(float) mRho);
        item.rotateByAxisWithPivot(phi, 0, 1, 0, 0, 0, 0);
        item.setPhi(phi);
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
            mProportionalItemPadding = false;
            requestLayout();
        }
    }

    public void enableProportionalItemPadding(final boolean enable) {
        if (mProportionalItemPadding != enable) {
            mProportionalItemPadding = enable;
            if (enable) {
                mItemPadding = 0;
            }
            requestLayout();
        }
    }

    public void setLayoutType(LayoutType type, Object... parameters) {
        boolean relayout = layoutType != type;
        // reset layout parameters
        mAroundItemAtId = -1;

        layoutType = type;
        switch (type) {
            case AROUND_ITEM_AT:
                // set around item Id
                int id  = parameters == null || parameters.length == 0 ?
                        0 : (Integer)parameters[0];
                relayout = mAroundItemAtId != id;
                mAroundItemAtId = id;

                break;
            // no parameters
            case LEFT_ORDER:
            case BALANCED:
            case WRAP_AROUND_CENTER:
            case AROUND_SELECTED_ITEM:
                break;
        }

        if (relayout) {
            requestLayout();
        }
    }

    public LayoutType getLayoutType() {
        return layoutType;
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
            requestLayout();
        }
    }

    /**
     * Rotate the items in the {@link RingList} until the item at {@code pos} is
     * rotated to {@code rotation}.
     * 
     * @param pos
     *            Position of the item in the data set
     * @param rotation
     *            New rotation of the item, relative to the {@code RingList}
     */
    public void scrollItemTo(int pos, float rotation) {
        // TODO: Implement #20239
    }

    private float basePhi = 0f;

    /**
     * Scroll all items in the {@link RingList} by {@code rotation} degrees}.
     * 
     * @param rotationDelta
     *            The amount to scroll, in degrees.
     */
    public void scrollBy(float rotationDelta) {
        basePhi += rotationDelta;
        requestLayout();

        Log.d(TAG, "scrollBy(%s): rotation: %.2f  base %.2f", getName(),
              rotationDelta, basePhi);
    }

    private void clear() {
        List<Widget> children = new ArrayList<Widget>(getChildren());
        Log.d(TAG, "clear(%s): removing %d children", getName(), children.size());
        for (Widget child : children) {
            removeChild(child, true);
        }
    }

    /**
     * Method getItemRotation
     *
     * @param position:  the position in the dataset.
     * @return  the rotation ("phi") of the view displaying the data at position;
     *          if that data is not being displayed, return Float.NaN.
     */
    public float getItemRotation(int position) {
        if (position < 0 || position >= mItems.size()) {
            return Float.NaN;
        }
        if (mItems.get(position).getPhi() == -1) {
            Log.e(TAG, "Error: layout() has not been called!");
            requestLayout();
            return Float.NaN;
        }

        return -mItems.get(position).getPhi() + 180;
    }

    /**
     * getSelectedItemRotation()
     *
     * A convenience method for calling getItemRotation() with the position of the
     * selected data item. If no item is selected, return Float.NaN.
     */
    public float getSelectedItemRotation() {
        //TODO: support multiple selections
        int selectionIndex = getSelectedItemId();
        return selectionIndex != -1 ? getItemRotation(getSelectedItemId()) : Float.NaN;
    }

    private int getSelectedItemId() {
        //TODO: support multiple selections
        int selectionIndex = -1;
        for (int i = 0; i < mItems.size(); i++) {
            Widget w = mItems.get(i);
            if (w != null && w.isSelected()) {
                selectionIndex = i;
                break;
            }
        }
        return selectionIndex;
    }

    @Override
    protected void onLayout() {

        for (Widget child : getChildren()) {
            child.layout();
        }

        final float[] angularWidths = calculateAngularWidth();
        layoutWithNewParam(angularWidths);
    }

    private void layoutWithNewParam(final float[] angularWidths)
    {

        final int numItems = mItems.size();

        if (numItems == 0) {
            Log.d(TAG, "layout(%s): no items to layout!", getName());
        } else {
            Log.d(TAG, "layout(%s): laying out %d items", getName(), numItems);

            switch (layoutType) {
                case WRAP_AROUND_CENTER:
                    wrapAroundCenterLayout(angularWidths);
                    break;
                case AROUND_ITEM_AT:
                    layoutAroundItemAt(mAroundItemAtId, angularWidths);
                    break;
                case AROUND_SELECTED_ITEM:
                    int aroundItemAtId = getSelectedItemId();
                    if (aroundItemAtId >= 0) {
                        layoutAroundItemAt(aroundItemAtId, angularWidths);
                    }
                    break;
                case BALANCED:
                    balancedLayout(angularWidths);
                    break;
                case LEFT_ORDER:
                default:
                    leftLayout(angularWidths);
                    break;
            }
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
        Log.d(TAG, "onChanged(%s): %d items", getName(), itemCount);
        int pos;

        Log.d(TAG, "onChanged(%s): %d views", getName(), mItems.size());
        // Recycle any items we already have

        for (pos = 0; pos < mItems.size() && pos < itemCount; ++pos) {
            final ListItemHostWidget host = mItems.get(pos);
            final Widget item = mAdapter.getView(pos, host.guestWidget, host);
            if (!mItemFocusEnabled) {
                item.setFocusEnabled(false);
            }
            item.addFocusListener(mFocusListener);
            host.setHostedWidget(item, pos, mAdapter.getItemId(pos));
            updateItemSelection(item, mSelectedItems.get(pos));
        }

        // Get any additional items
        for (; pos < itemCount; ++pos) {
            Widget item = null;
            ListItemHostWidget host = makeHost(getGVRContext());
            try {
                item = mAdapter.getView(pos, null, host);
                if (item == null) {
                    break;
                }
                if (!mItemFocusEnabled) {
                    item.setFocusEnabled(false);
                }
                item.addFocusListener(mFocusListener);
                host.setHostedWidget(item, pos, mAdapter.getItemId(pos));
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
            mItems.add(host);
            Log.d(TAG, "onChanged(%s): added item at %d", getName(), pos);
            addChild(host, true);
            updateItemSelection(item, mSelectedItems.get(pos));
        }

        // Trim unused items
        Log.d(TAG, "onChanged(%s): trimming: %b", getName(), pos < mItems.size());
        for (; pos < mItems.size(); ++pos) {
            Widget item = mItems.remove(pos);
            removeChild(item, true);
        }

        Log.d(TAG, "onChanged(%s): requesting layout", getName());
        requestLayout();

    }

    private OnFocusListener mFocusListener = new OnFocusListener() {
        @Override
        public boolean onFocus(final boolean focused, final Widget widget) {
            Log.d(TAG, "onFocus(%s) widget= %s focused [%b]", getName(), widget.getName(), focused);
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
            Log.d(TAG, "onLongFocus(%s) widget= %s", getName(), widget.getName());
            int position = mItems.indexOf(widget);
            if (position >= 0) {
                for (OnItemFocusListener listener : mItemFocusListeners) {
                    listener.onLongFocus(position);
                }
            }
            return false;
        }
    };

    private ListItemHostWidget makeHost(GVRContext gvrContext) {
        ListItemHostWidget host = new ListItemHostWidget(gvrContext);
        return host;
    }

    private class ListItemHostWidget extends AbsoluteLayout {
        public ListItemHostWidget(GVRContext gvrContext) {
            super(gvrContext, 0, 0);
            setTouchable(false);
            setFocusEnabled(false);
        }

        public void setHostedWidget(Widget guest, int pos, long id) {
            Log.d(TAG, "setHostedWidget(%s): hosting %s, same: %b", getName(),
                  guest == null ? "<null>" : guest.getName(),
                  guest == guestWidget);
            if (guest != guestWidget) {
                if (guestWidget != null) {
                    removeChild(guestWidget, guestWidget.getSceneObject(), true);
                }
                guestWidget = guest;
                if (guestWidget != null) {
                    addChildInner(guestWidget, guestWidget.getSceneObject(), -1);
                }
                requestLayout();
            }
            position = pos;
            this.id = id;
            this.phi = -1;
        }

        public void setPhi(float phi) {
            this.phi = phi;
        }

        public float getPhi() {
            return phi;
        }

        Widget guestWidget;
        int position;
        long id;
        private float phi;
    }

    private static boolean MULTI_SELECTION_SUPPORTED = false;

    /**
     * Clear the selection of all the items if any.
     */
    public void clearSelection() {
        int size = mSelectedItems.size();
        for (int i = 0; i < size; i++) {
            if (mSelectedItems.get(i)) {
                updateItemSelection(i, false);
            }
        }
        mSelectedItems.clear();
    }

    /**
     * Select or deselect an item at position {@code pos}.
     * 
     * @param pos
     *            item position
     * @param select
     *            operation to perform select or deselect.
     * @return {@code true} if the requested operation is successful,
     *         {@code false} otherwise.
     */
    public boolean setItemSelected(int pos, boolean select) {
        int size = mSelectedItems.size();

        if (select && MULTI_SELECTION_SUPPORTED == false) {
            for (int i = 0; i < size; i++) {
                if (mSelectedItems.get(i)) {
                    // Deselect any selected item other than the item at 'pos'
                    if (i != pos) {
                        mSelectedItems.put(i, false);
                        updateItemSelection(i, false);
                    }
                    break;
                }
            }
        }

        if (mSelectedItems.get(pos) != select) {
            mSelectedItems.put(pos, select);
            updateItemSelection(pos, select);
            return true;
        }

        return false;
    }

    private void updateItemSelection(int position, boolean select) {
        Widget widget = null;

        for (ListItemHostWidget w : mItems) {
            if (w.position == position) {
                widget = w.guestWidget;
                break;
            }
        }

        if (widget != null && widget.isSelected() != select) {
            widget.setSelected(select);
        }
    }

    private void updateItemSelection(Widget widget, boolean select) {
        if (widget != null && widget.isSelected() != select) {
            widget.setSelected(select);
        }
    }

    /**
     * Check whether the item at position {@code pos} is selected.
     * 
     * @param pos
     *            item position
     * @return {@code true} if the item is selected, {@code false} otherwise.
     */
    public boolean isSelected(int pos) {
        return mSelectedItems.get(pos);
    }

    private Adapter mAdapter;
    private float mItemPadding;
    private boolean mProportionalItemPadding;
    private List<ListItemHostWidget> mItems = new ArrayList<ListItemHostWidget>();
    SparseBooleanArray mSelectedItems = new SparseBooleanArray();
    private double mRho;
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
