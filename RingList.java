
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
            final double rho, int pageNumber, int itemIncrementPerPage) {
        super(gvrContext, width, height);
        mRho = rho;
        mPageNumber = pageNumber;
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
    private int mItemIncrementPerPage = 0;
    private LayoutType layoutType = LayoutType.LEFT_ORDER;
    private int mAroundItemAtId = -1;

    private static int FULL_CIRCLE = 360;
    private static int HALF_CIRCLE = 180;

    public enum LayoutType {
        LEFT_ORDER,
        BALANCED,
        WRAP_AROUND_CENTER,
        AROUND_ITEM_AT, // can take the parameter int mAroundItemAtId
        AROUND_SELECTED_ITEM,
    }

    private int layoutAroundItemAt(final int position,
            final float[] prevItemRotationValues) {
        final int numItems = mAdapter.getCount();
        int firstItemIndex = mPageNumber * mItemIncrementPerPage;
        int pos = firstItemIndex;
        float phi = 0;
        int itemsIndexOnDisplay = 0;
        float[] angularWidths = new float[numItems];

        while (phi > -FULL_CIRCLE && itemsIndexOnDisplay < numItems) {
            int appListIndex = getValidItemPosition(pos, numItems);

            setUpItem(appListIndex, itemsIndexOnDisplay,
                      angularWidths[itemsIndexOnDisplay]);
            phi -= angularWidths[itemsIndexOnDisplay] + mItemPadding;

            pos++;
            itemsIndexOnDisplay++;
        }

        if (prevItemRotationValues != null) {
            int anchorAt = position;
            int lastItemIndex = itemsIndexOnDisplay + firstItemIndex - 1;
            if (position == -1 || position > lastItemIndex) {
                    anchorAt = lastItemIndex;
            } else if (position < firstItemIndex) {
                    anchorAt = firstItemIndex;
            }

            phi = 0;
            if (prevItemRotationValues != null) {
                phi = prevItemRotationValues[prevItemRotationValues.length > anchorAt ? anchorAt
                        : prevItemRotationValues.length - 1];
            }

            float anchorPhi = phi;

            for (int index = anchorAt; position < itemsIndexOnDisplay; index++) {
                layoutItem(index,
                           mItems.get(index - firstItemIndex).guestWidget, phi);
                phi -= angularWidths[index] + mItemPadding;
            }

            phi = anchorPhi;

            for (int index = anchorAt - 1; index >= firstItemIndex; index--) {
                phi += angularWidths[index] + mItemPadding;
                layoutItem(index,
                           mItems.get(index - firstItemIndex).guestWidget, phi);
            }
        }

        return itemsIndexOnDisplay;
    }

    private void wrapAroundCenterLayout() {
        final int numItems = mAdapter.getCount();

        int rightHalfNumItems = (int) Math.ceil(numItems / 2);
        int leftHalfNumItems = numItems - rightHalfNumItems;

        int startPosition = mPageNumber * mItemIncrementPerPage;
        int secondHalfPos = numItems - 1 + startPosition;

        int numViewUsedRight = layoutClockwise(HALF_CIRCLE, startPosition, 0,
                                               0.f, rightHalfNumItems);
        layoutCounterclockwise(HALF_CIRCLE, secondHalfPos, numViewUsedRight,
                               0.f, leftHalfNumItems);
    }

    private float getItemAngularWidth(int adapterItemIndex, int reusableIndex)
    {
        Widget w = null;
        if (mItems.size() > reusableIndex) {
            final ListItemHostWidget host = mItems.get(reusableIndex);
            w = mAdapter.getView(adapterItemIndex, host.guestWidget, host);
        } else {
            ListItemHostWidget host = makeHost(getGVRContext());
            w = mAdapter.getView(adapterItemIndex, null, host);
        }

        float angularWidth = 0f;
        if (w != null) {
            angularWidth = LayoutHelpers.calculateAngularWidth(w, mRho);
        }
        return angularWidth;
    }

    private Widget setUpItem(int adapterItemIndex, int reusableIndex,
            float angularWidth) {
        Widget w = null;
        if (mItems.size() > reusableIndex) {
            final ListItemHostWidget host = mItems.get(reusableIndex);
            w = mAdapter.getView(adapterItemIndex, host.guestWidget, host);
            host.setHostedWidget(w, reusableIndex,
                                 mAdapter.getItemId(adapterItemIndex));
        } else {
            ListItemHostWidget host = makeHost(getGVRContext());
            w = mAdapter.getView(adapterItemIndex, null, host);
            w.addFocusListener(mFocusListener);
            host.setHostedWidget(w, reusableIndex,
                                 mAdapter.getItemId(adapterItemIndex));

            mItems.add(host);
            addChild(host);
            host.layout();
            updateItemSelection(w, mSelectedItems.get(adapterItemIndex));
        }

        if (w != null) {
            angularWidth = LayoutHelpers.calculateAngularWidth(w, mRho);
        }

        return w;
    }

    private int getValidItemPosition(int pos, int size) {
        int validItemIndex = pos;
        if (validItemIndex >= size) {
            validItemIndex = validItemIndex % size;
        }
        return validItemIndex;
    }

    private void getValidDegree(int degree) {
        if (degree < 0) {
            degree = -degree;
        }
        if (degree > FULL_CIRCLE) {
            degree = degree % FULL_CIRCLE;
        }
    }

    private int layoutClockwise(int degreeCoverage, int pos,
            int reusedPosition, float phi, int maxCount) {

        final int numItems = mAdapter.getCount();
        getValidDegree(degreeCoverage);
        int initPosition = reusedPosition;

        while (phi > -degreeCoverage
                && reusedPosition < (initPosition + maxCount)) {
            int appListIndex = getValidItemPosition(pos, numItems);

            float angularWidth = 0f;
            Widget w = setUpItem(appListIndex, reusedPosition, angularWidth);
            layoutItem(appListIndex, w, phi);

            // update phi
            phi -= angularWidth + mItemPadding;

            // increment
            pos++;
            reusedPosition++;
        }

        // check for overlap
        int nextItemIndex = getValidItemPosition(pos + 1, numItems);
        float angularWidth = getItemAngularWidth(nextItemIndex,
                                                 reusedPosition + 1);
        phi -= angularWidth + mItemPadding;
        if (phi > -degreeCoverage) {
            trimItems(reusedPosition);
            reusedPosition--;
        }

        return reusedPosition - initPosition;
    }

    private int layoutCounterclockwise(int degreeCoverage, int pos,
            int reusedPosition, float phi, int maxCount) {

        final int numItems = mAdapter.getCount();
        getValidDegree(degreeCoverage);

        int initPosition = reusedPosition;
        while (phi < degreeCoverage
                && reusedPosition < (maxCount + initPosition)) {

            int appListIndex = getValidItemPosition(pos, numItems);
            float angularWidth = 0f;
            Widget w = setUpItem(appListIndex, reusedPosition, angularWidth);

            // update phi
            phi += angularWidth + mItemPadding;
            layoutItem(appListIndex, w, phi);

            // update indices
            pos--;
            reusedPosition++;
        }

        return reusedPosition - initPosition;
    }

    private void trimItems(int pos) {
        for (; pos < mItems.size(); pos++) {
            ListItemHostWidget w = mItems.remove(pos);
            removeChild(w);
            w.layout();
        }
    }

    private int balancedLayout() {

        final int numItems = mAdapter.getCount();
        int startPosition = mPageNumber * mItemIncrementPerPage;
        float totalAngularWidths = 0;
        float[] angularWidths = new float[numItems];
        int itemIndexToDisplay = 0;

        for (int pos = startPosition; pos < numItems; pos++) {
            setUpItem(pos, itemIndexToDisplay,
                      angularWidths[itemIndexToDisplay]);
            totalAngularWidths += angularWidths[itemIndexToDisplay];
            itemIndexToDisplay++;
        }

        float phi = (totalAngularWidths + mItemPadding
                * (itemIndexToDisplay - 1))
                / 2 - (angularWidths[0] / 2);

        itemIndexToDisplay = 0;
        for (int pos = startPosition; pos < numItems; pos++) {
            layoutItem(pos, mItems.get(itemIndexToDisplay).guestWidget, phi);
            phi -= angularWidths[itemIndexToDisplay] + mItemPadding;
            itemIndexToDisplay++;
        }
        return itemIndexToDisplay;
    }

    private void layoutItem(final int index, final Widget item, final float phi) {
        Log.d(TAG, "layout(%s): phi [%f]", item.getName(), phi);
        item.setRotation(1, 0, 0, 0);
        item.setPosition(0, 0, -(float) mRho);
        item.rotateByAxisWithPivot(phi, 0, 1, 0, 0, 0, 0);
        mItemRotationValues[index] = phi;
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
     * Rotate an item in the {@link RingList}.
     * 
     * @param pos
     *            Position of the item in the data set
     * @param rotation
     *            New rotation of the item, relative to the {@code RingList}
     */
    public void scrollItemTo(int pos, float rotation) {
        // TODO: Implement #20239
    }

    /**
     * Scroll all items in the {@link RingList} by {@code rotation} degrees}.
     * 
     * @param rotation
     *            The amount to scroll, in degrees.
     */
    public void scrollBy(float rotation) {
        // TODO: Implement #20241
    }

    private void clear() {
        List<Widget> children = new ArrayList<Widget>(getChildren());
        Log.d(TAG, "clear(%s): removing %d children", getName(), children.size());
        for (Widget child : children) {
            removeChild(child);
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
        if (mItemRotationValues == null) {
            Log.e(TAG, "Error: layout() has not been called!");
            requestLayout();
            return Float.NaN;
        }
        return mItemRotationValues[position];
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
            Widget w = mItems.get(i).guestWidget;
            if (w != null && w.isSelected()) {
                selectionIndex = i;
                break;
            }
        }
        return selectionIndex;
    }

    private float[] mItemRotationValues = null;
    
    @Override
    protected void onLayout() {       
        final int numItems = mAdapter.getCount();

        if (numItems == 0) {
            Log.d(TAG, "layout(%s): no items to layout!", getName());
        } else {
            Log.d(TAG, "layout(%s): laying out %d items", getName(), numItems);

            float[] prevItemRotationValues = mItemRotationValues;
            mItemRotationValues = new float[numItems];
            int numViewsUsed = 0;
            switch (layoutType) {
                case WRAP_AROUND_CENTER:
                    wrapAroundCenterLayout();
                    break;
                case AROUND_ITEM_AT:
                    numViewsUsed = layoutAroundItemAt(mAroundItemAtId, prevItemRotationValues);
                    trimItems(numViewsUsed);
                    break;
                case AROUND_SELECTED_ITEM:
                    int aroundItemAtId = getSelectedItemIndex();
                    if (aroundItemAtId >= 0) {
                        numViewsUsed = layoutAroundItemAt(aroundItemAtId,
                                           prevItemRotationValues);
                        trimItems(numViewsUsed);
                    }
                    break;
                case BALANCED:
                    numViewsUsed = balancedLayout();
                    trimItems(numViewsUsed);
                    break;
                case LEFT_ORDER:
                default:
                    numViewsUsed = layoutClockwise(FULL_CIRCLE, 0, 0, 0.f, numItems);
                    trimItems(numViewsUsed);
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
        clear();
        mItems = new ArrayList<ListItemHostWidget>();
        
        requestLayout();
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
        }

        Widget guestWidget;
        int position;
        long id;
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
