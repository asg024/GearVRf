package com.samsung.smcl.vr.widgets;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRNotifications;
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
    
    protected RingList(final GVRContext gvrContext, float width, float height) {
        super(gvrContext, width, height);
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
            int maxNumPerPage) {
        super(gvrContext, width, height);
        mRho = rho;
        mPageNumber = pageNumber;
        mMaxItemsDisplayed = maxItemsDisplayed;
        mMaxNumPerPage = maxNumPerPage;
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

    protected int getMaxNumPerPage() {
        return mMaxNumPerPage;
    }

    protected int getMaxItemsDisplayed() {
        return mMaxItemsDisplayed;
    }

    private int mMaxItemsDisplayed = 0;
    private int mMaxNumPerPage = 0;
    private int mPageNumber = 0;
    private LayoutType layoutType = LayoutType.LEFT_ORDER;
    private int mAroundItemAtId = -1;

    public enum LayoutType {
        LEFT_ORDER,
        BALANCED,
        AROUND_ITEM_AT, // can take the parameter int mAroundItemAtId
        AROUND_SELECTED_ITEM,
    }

    /**
     * Calculates the each angular width for each mItems
     *
     * @return array of floats each mItems' angular width
     *
     */
    protected float[] calculateAngularWidth(boolean isProportionalItemPadding) {
        final int numItems = mItems.size();
        float[] angularWidths = new float[numItems];

        float totalAngularWidths = 0;
        for (int i = 0; i < numItems; ++i) {
            angularWidths[i] = LayoutHelpers.calculateAngularWidth(mItems
                    .get(i).guestWidget, mRho);
            totalAngularWidths += angularWidths[i];
            Log.d(TAG, "calculateAngularWidth(%s): angular width at %d: %f",
                  getName(), i, angularWidths[i]);
        }

        if (isProportionalItemPadding) {
            int totalNumPerPage = mMaxItemsDisplayed == 0 ? mItems.size() : mMaxItemsDisplayed;
            float uniformAngularWidth = (360.f - totalAngularWidths)
                    / totalNumPerPage;
            for (int i = 0; i < numItems; ++i) {
                angularWidths[i] += uniformAngularWidth;
                Log.d(TAG,
                      "calculateAngularWidth(%s): angular uniform width at %d: %f",
                      getName(), i, angularWidths[i]);
            }
        }

        return angularWidths;
    }

    protected float[] calculateUniformAngularWidth(boolean isProportionalItemPadding) {
        int firstElementIndex = 0;
        Widget item = null;
        ListItemHostWidget host = makeHost(getGVRContext());
        item = mAdapter.getView(firstElementIndex, null, host);
        final int numItems = mMaxItemsDisplayed;

        float singleAngularWidth = LayoutHelpers.calculateAngularWidth(item,
                                                                       mRho);
        float[] angularWidths = new float[numItems];

        if (!isProportionalItemPadding) {
            for (int i = 0; i < numItems; ++i) {
                angularWidths[i] = singleAngularWidth;
            }
        } else {
            float totalAngularWidths = singleAngularWidth * numItems;
            float uniformAngularWidth = (360.f - totalAngularWidths) / numItems;

            for (int i = 0; i < numItems; ++i) {
                angularWidths[i] += uniformAngularWidth;
                Log.d(TAG,
                      "calculateAngularWidth(%s): angular uniform width at %d: %f",
                      getName(), i, angularWidths[i]);
            }
        }
        return angularWidths;
    }
    

    private void layoutAroundItemAt(int position, final float[] angularWidths) {
        int start = getLayoutStart();
        int end = getLayoutEnd(start);

        if (position < 0 || position >= end) {
            position = end - 1;
        } else if (position < start) {
            position = start;
        }
        
        final float itemPhi = mItems.get(position).getPhi();
        if (Float.isNaN(itemPhi) == false) {
            final float startPhi = itemPhi + basePhiDelta;
            Log.d(TAG, "layoutAroundItemAt(%s): starting phi: %5.2f",
                  getName(), startPhi);
            // Include position in the layout in case there was a change in
            // basePhi (i.e., basePhiDelta != 0)
            layoutItems(position, start - 1, angularWidths, startPhi);
            
            // Layout to the right of position
            if (end > position) {
                // It's not strictly necessary to include position here, since
                // it was laid out above, but it saves us having to hand
                // calculate the phi of the next item and costs very little
                layoutItems(position, end, angularWidths, startPhi);
            }
        } else {
            Log.d(TAG,
                  "layoutAroundItemAt(%s): phi at position %d is NaN; doing leftLayout()",
                  getName(), position);
            leftLayout(angularWidths);
        }
    }

    private void leftLayout(final float[] angularWidths) {
        float phi = basePhi;
        Log.d(TAG, "leftLayout(%s): starting phi: %5.2f", getName(), phi);
    
        int start = getLayoutStart();
        int end = getLayoutEnd(start);
    
        layoutItems(start, end, angularWidths, phi);
    }

    private void balancedLayout(final float[] angularWidths) {
        int start = getLayoutStart();
        int end = getLayoutEnd(start);

        float totalAngularWidths = 0;
        for (int i = start; i < end; ++i) {
            totalAngularWidths += angularWidths[i];
        }

        float phi = basePhi + (totalAngularWidths + mItemPadding * (mItems.size() - 1)) / 2 - (angularWidths[0] / 2);

        Log.d(TAG, "balancedLayout(%s) : firstItemIndex %d lasttItemIndex %d ",
              getName(), start, end - 1);
        layoutItems(start, end, angularWidths, phi);
    }


    protected void layoutItem(final ListItemHostWidget item, final float phi) {
        Log.d(TAG, "layoutItem(%s): phi [%f] (%s)", getName(), phi, item.guestWidget.getName());
        item.setRotation(1, 0, 0, 0);
        item.setPosition(0, 0, -(float) mRho);
        item.rotateByAxisWithPivot(phi, 0, 1, 0, 0, 0, 0);
        item.setPhi(phi);
    }

    /**
     * Lay out the list's items in the specified range. If {@code end} is
     * greater than {@code start}, the items will be laid out from left to
     * right; if {@code end} is less than {@code start}, the item will be laid
     * out from right to left (i.e., in reverse).
     * <p>
     * <span style="color:red"><strong>WARNING:</strong></span> this method does
     * <em>no bounds checking</em>, either on the items or {@code angularWidths}.
     * 
     * @param start
     *            Index of the first item to lay out
     * @param end
     *            One past the last item to lay out
     * @param angularWidths
     *            The {@linkplain #calculateAngularWidth() angular width} of all
     *            the list's items
     * @param phi
     *            Rotation of the center of the item at {@code start}.
     * @return The accumulated phi of the laid out items
     */
    private float layoutItems(final int start, final int end,
            final float[] angularWidths, float phi) {
        final int factor = end > start ? -1 : 1;
    
        // We have to advance phi by halves so that items get properly centered.
        // If we advance phi by each item's angular width, the next item will be
        // positioned as though it has the same width
    
        // Back phi up by half the width of the first item to prepare for
        // the advance below
        phi -= (angularWidths[start] / 2) * factor;
        for (int i = start; i != end; i -= factor) {
            final float halfWidth = angularWidths[i] / 2;
            // Advance phi by half the item's width to position the center
            phi += halfWidth * factor;
            layoutItem(mItems.get(i), phi);
            // Advance phi by the remaining half width
            phi += (halfWidth + mItemPadding) * factor;
        }
        return phi;
    }

    protected int getLayoutStart() {
        return mPageNumber * mMaxNumPerPage;
    }

    protected int getLayoutEnd(int start) {
        final int limit = mItems.size() - start;
        final int layoutCount = mMaxItemsDisplayed == 0 ? limit : Math
                .min(limit, mMaxItemsDisplayed);
        return start + layoutCount;
    }

    protected int getNumItemsToDisplay(int numItems) {
        final int limit = numItems;
        return mMaxItemsDisplayed == 0 ? limit :
            Math.min(limit, mMaxItemsDisplayed);
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

    /**
     * @return proportional item padding boolean
     */
    public boolean getProportionalItemPadding() {
        return mProportionalItemPadding;
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

    /**
     * Sets the {@link LayoutType} for the {@link RingList}. For
     * {@link LayoutType#AROUND_ITEM_AT AROUND_ITEM_AT}, an additional
     * {@code int} parameter can be optionally specified. The new
     * {@code LayoutType} will take effect on the next layout.
     * <p>
     * <strong>NOTE:</strong> does <em>not</em> trigger a layout! Call
     * {@link #requestLayout()} if immediate layout is needed.
     * 
     * @param type
     *            The new {@code LayoutType} for the {@code RingList}
     * @param parameters
     *            Only used for {@code AROUND_ITEM_AT}: optional index in the
     *            data set of the item to layout around; defaults to 0 (zero)
     */
    public void setLayoutType(LayoutType type, Object... parameters) {
        // reset layout parameters
        mAroundItemAtId = -1;

        layoutType = type;
        switch (type) {
            case AROUND_ITEM_AT:
                // set around item Id
                int id  = parameters == null || parameters.length == 0 ?
                        0 : (Integer)parameters[0];
                mAroundItemAtId = id;

                break;
            // no parameters
            case LEFT_ORDER:
            case BALANCED:
            case AROUND_SELECTED_ITEM:
                break;
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
     * Do not call from the GL thread; notice the waitAfterStep.
     *
     * @param pos
     *            Position of the item in the data set
     * @param rotation
     *            New rotation of the item, relative to the {@code RingList}
     */
    public void scrollItemTo(int pos, float rotation) {
        if (pos < 0 || pos >= mItems.size()) {
            return;
        }

        if (Float.isNaN(rotation)) {
            return;
        }
        float phi = mItems.get(pos).getPhi();
        if (Float.isNaN(phi)) {
            Log.e(TAG, "scrollItemTo(%s): Error: layout() has not been called!", getName());
            requestLayout();
            // skip current frame to make sure the layout is finished
            GVRNotifications.waitAfterStep();
        }

        rotation = -(rotation - 180f);
        phi = mItems.get(pos).getPhi();
        if (!Utility.equal(rotation, phi)) {
            float delta = rotation - phi;

            scrollBy(delta);
        }
    }

    private float basePhi = 0f;
    /**
     * Accumulates the change to basePhi since the last layout; gets reset with
     * each onLayout() call.
     */
    private float basePhiDelta = 0f;

    /**
     * Scroll all items in the {@link RingList} by {@code rotation} degrees}.
     *
     * @param rotationDelta
     *            The amount to scroll, in degrees.
     */
    public void scrollBy(float rotationDelta) {
        if (Float.isInfinite(rotationDelta) || Float.isNaN(rotationDelta)) {
            Log.e(TAG, new IllegalArgumentException(),
                  "Invalid rotation delta: %f", rotationDelta);
            return;
        }

        basePhiDelta += rotationDelta;
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
        if (Float.isNaN(mItems.get(position).getPhi())) {
            Log.e(TAG, "getItemRotation(%s): Error: layout() has not been called!", getName());
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
        int selectionIndex = getSelectedItemIndex();
        return selectionIndex != -1 ? getItemRotation(getSelectedItemIndex()) : Float.NaN;
    }

    private int getSelectedItemIndex() {
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

        final float[] angularWidths = calculateAngularWidth(mProportionalItemPadding);
        final int numItems = mItems.size();

        if (numItems == 0) {
            Log.d(TAG, "layout(%s): no items to layout!", getName());
        } else {
            Log.d(TAG, "layout(%s): laying out %d items", getName(), numItems);

            switch (layoutType) {
                case AROUND_ITEM_AT:
                    layoutAroundItemAt(mAroundItemAtId, angularWidths);
                    break;
                case AROUND_SELECTED_ITEM:
                    int aroundItemAtId = getSelectedItemIndex();
                    layoutAroundItemAt(aroundItemAtId, angularWidths);
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
        // Reset to accumulate until the next layout
        basePhiDelta = 0f;
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

        //TODO remove this once RingList supports recycled views
        if (this.getClass().getSimpleName()
                .equals(WrapAroundList.class.getSimpleName())) {
            return;
        }

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
        for (; mItems.size() > pos;) {
            Widget item = mItems.remove(mItems.size() - 1);
            removeChild(item, true);
        }
        Log.d(TAG, "onChanged(%s): requesting layout", getName());
        requestLayout();

    }

    protected ListItemHostWidget getRecycleableView(int pos, int itemIndex) {
        int numAdapterItems = mAdapter.getCount();
        int numViews = Math.min(getMaxItemsDisplayed(), numAdapterItems);

        ListItemHostWidget host = null;

        // Trim unused items
        if (mItems.size() > numViews) {
            int index = mItems.size() - 1;
            for (; mItems.size() > numViews; index--) {
                Widget item = mItems.remove(index);
                removeChild(item, true);
            }
        }

        // Recycle any items we already have
        if (mItems.size() == numViews) {
            host = mItems.get(pos);
            final Widget item = mAdapter.getView(itemIndex, host.guestWidget,
                                                 host);
            if (!mItemFocusEnabled) {
                item.setFocusEnabled(false);
            }
            item.addFocusListener(mFocusListener);
            host.setHostedWidget(item, itemIndex, mAdapter.getItemId(itemIndex));
            updateItemSelection(item, mSelectedItems.get(itemIndex));
        } else if (mItems.size() < numViews) {
            Widget item = null;
            host = makeHost(getGVRContext());
            try {
                item = mAdapter.getView(itemIndex, null, host);
                if (item == null) {
                    return null;
                }
                if (!mItemFocusEnabled) {
                    item.setFocusEnabled(false);
                }
                item.addFocusListener(mFocusListener);
                host.setHostedWidget(item, itemIndex,
                                     mAdapter.getItemId(itemIndex));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            mItems.add(host);
            Log.d(TAG, "onChanged(%s): added item at %d", getName(), pos);
            addChild(host, true);
            updateItemSelection(item, mSelectedItems.get(itemIndex));
        }

        host.layout();

        return host;
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

    protected class ListItemHostWidget extends AbsoluteLayout {
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
                if (guestWidget != null && guestWidget.getParent() == this) {
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
            this.phi = Float.NaN;
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

    protected ListItemHostWidget getItem(int pos) {
        return mItems.get(pos);
    }

    protected int getNumItems() {
        return mItems.size();
    }
    
    protected int getAdapterCount() {
        return mAdapter.getCount();
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

    private List<ListItemHostWidget> mItems = new ArrayList<ListItemHostWidget>();

    private double mRho;
    private Adapter mAdapter;
    private float mItemPadding;
    private boolean mProportionalItemPadding;
    private SparseBooleanArray mSelectedItems = new SparseBooleanArray();
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
