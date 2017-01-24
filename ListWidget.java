package com.samsung.smcl.vr.widgets;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;

import android.database.DataSetObserver;

import com.samsung.smcl.utility.Log;
import com.samsung.smcl.utility.Utility;
import com.samsung.smcl.vr.gvrf_launcher.util.SimpleAnimationTracker;
import com.samsung.smcl.vr.widgets.Layout.Axis;
import com.samsung.smcl.vr.widgets.Layout.Direction;
import com.samsung.smcl.vr.widgets.LayoutScroller.ScrollableList;

// TODO: Add support for animation (in particular, we need to handle layout changes)
// TODO: Scrolling (this is different from rotating, a la AppRing)
// TODO: Check the synchronization between data set update and items layout

/**
 * A group widget that shows items in the list. Adapter must be associated with this layout.
 * The {@link ListWidget} can apply {@link LinearLayout} or {@link RingLayout} or {@link GridLayout}
 *
 * The extended features for the {@link ListWidget} are
 * - data set can be updated dynamically
 * - item selection
 * - item focus listener
 * - scrolling
 */
public class ListWidget extends GroupWidget implements ScrollableList {

    /**
     * Interface definition for a callback to be invoked when an item in this {@link ListWidget}
     *  has been focused.
     */
    public interface OnItemFocusListener {

        /**
         * Callback method to be invoked when an item in this {@link ListWidget} has been focused.
         * @param dataIndex item position in the list
         * @param focused true if the item is focused, false - otherwise
         */
        public void onFocus(int dataIndex, boolean focused);

        /**
         * Callback method to be invoked when the long focus occurred for the item in this {@link ListWidget} .
         * @param dataIndex item position in the list
         */
        public void onLongFocus(int dataIndex);
    }

    protected final Set<OnItemFocusListener> mItemFocusListeners = new LinkedHashSet<OnItemFocusListener>();

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

    protected boolean mItemFocusEnabled = true;

    /**
     * @return Whether the {@link ListWidget} allows it items to be focused.
     */
    public boolean getItemFocusEnabled() {
        return mItemFocusEnabled;
    }

    /**
     * Sets {@linkplain Widget#setFocusEnabled(boolean) focus enabled} (or
     * disabled) for all children of the {@link ListWidget} that were fetched from
     * the {@link Adapter}. If this is called with {@code false}, any new items
     * gotten from the {@code Adapter} will have {@code setFocusEnabled(false)}
     * called on them. If called with {@code true}, {@code ListLayout} acts as
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
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "setItemFocusEnabled(%s): item focus enabled: %b", getName(), enabled);

                for (Widget w : getChildren()) {
                    ListItemHostWidget host = (ListItemHostWidget)w;
                    host.getGuest().setFocusEnabled(enabled);
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
     * Interface definition for a callback to be invoked when an item in this {@link ListWidget}
     *  has been focused.
     */
    public interface OnItemTouchListener {
        /**
         * Called when a list item is touched (tapped).
         *
         * @param dataIndex target by touch event.
         *
         * @return {@code True} to indicate that no further processing of the
         *         touch event should take place; {@code false} to allow further
         *         processing.
         */
        public boolean onTouch(int dataIndex);
    }

    protected final Set<OnItemTouchListener> mItemTouchListeners = new LinkedHashSet<OnItemTouchListener>();

    /**
     * Add a listener for {@linkplain OnItemTouchListener#onTouch notification
     * for this object.
     *
     * @param listener
     *            An implementation of {@link OnItemTouchListener}.
     * @return {@code true} if the listener was successfully registered,
     *         {@code false} if the listener is already registered.
     */
    public boolean addOnItemTouchListener(final OnItemTouchListener listener) {
        return mItemTouchListeners.add(listener);
    }

    /**
     * Remove a previously added @linkplain #addOnItemTouchListener(OnItemTouchListener)}
     * registered} touch notification {@linkplain OnItemTouchListener listener}.
     *
     * @param listener
     *            An implementation of {@link OnItemTouchListener}
     * @return {@code true} if the listener was successfully unregistered,
     *         {@code false} if the specified listener was not previously
     *         registered with this object.
     */
    public boolean removeOnItemTouchListener(final OnItemTouchListener listener) {
        return mItemTouchListeners.remove(listener);
    }

    protected boolean mItemTouchable = true;

    /**
     * @return Whether the {@link ListWidget} allows it items to be touchable.
     */
    public boolean getItemTouchable() {
        return mItemTouchable;
    }

    /**
     * Sets {@linkplain Widget#setTouchable(boolean) touch enabled} (or
     * disabled) for all children of the {@link ListWidget} that were fetched from
     * the {@link Adapter}. If this is called with {@code false}, any new items
     * gotten from the {@code Adapter} will have {@code setTouchable(false)}
     * called on them. If called with {@code true}, {@code ListLayout} acts as
     * though it's received an {@link DataSetObserver#onChanged() on changed}
     * notification and refreshes its items with the {@code Adapter}.
     * <p>
     * This is a convenience method only, and the current state of focus
     * enabling for each displayed item is not tracked in any way.
     * {@code Adapters} should ensure that they enable or disable touch as
     * appropriate for their views.
     *
     * @param enabled
     *            {@code True} to enable touch for all items, {@code false} to
     *            disable.
     */
    public void setItemTouchable(boolean enabled) {
        if (enabled != mItemTouchable) {
            mItemTouchable = enabled;
            if (!mItemTouchable) {
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "mItemTouchable(%s): item touch enabled: %b", getName(), enabled);

                for (Widget w : getChildren()) {
                    ListItemHostWidget host = (ListItemHostWidget)w;
                    host.getGuest().setTouchable(enabled);
                }
            } else {
                // This will refresh the data for the items, allowing the
                // Adapter to reset touch enabling
                Log.d(TAG, "mItemTouchable(%s): item touch enabled: %b", getName(), enabled);
                onChanged();
            }
        }
    }


    /**
     * Construct a new {@code ListWidget} instance with  LinearLayout applied by default
     *
     * @param gvrContext
     *            The active {@link GVRContext}.
     * @param adapter  {@link Adapter} associated with this layout.
     * @param sceneObj
     *
     */
    public ListWidget(final GVRContext gvrContext, final Adapter adapter, GVRSceneObject sceneObj) {
        super(gvrContext, sceneObj);
        onChanged(adapter);
    }

    /**
     * Construct a new {@code ListWidget} instance with  LinearLayout applied by default
     *
     * @param gvrContext
     *            The active {@link GVRContext}.
     * @param adapter  {@link Adapter} associated with this layout.
     * @param width
     * @param height
     */
    public ListWidget(final GVRContext gvrContext, final Adapter adapter, float width, float height) {
        super(gvrContext, width, height);
        onChanged(adapter);
    }

    public ListWidget(final GVRContext context, final Adapter adapter,
            final GVRSceneObject sceneObject, NodeEntry attributes)
            throws InstantiationException {
        super(context, sceneObject, attributes);
        onChanged(adapter);
    }

    protected OnFocusListener getOnFocusListener() {
        return mOnFocusListener;
    }

    protected OnTouchListener getOnTouchListener() {
        return mOnTouchListener;
    }


    private OnFocusListener mOnFocusListener = new OnFocusListener() {
        @Override
        public boolean onFocus(final boolean focused, final Widget widget) {
            Log.d(TAG, "onFocus(%s) widget= %s focused [%b]", getName(), widget, focused);
            Widget parent = widget.getParent();
            boolean ret = false;
            if (parent instanceof ListItemHostWidget) {
                int dataIndex = ((ListItemHostWidget) parent).getDataIndex();
                for (OnItemFocusListener listener : mItemFocusListeners) {
                    listener.onFocus(dataIndex, focused);
                }
                ret = true;
            } else {
                Log.d(TAG, "Focused widget is not a list item!");
            }
            return ret;
        }

        @Override
        public boolean onLongFocus(Widget widget) {
            Log.d(TAG, "onLongFocus(%s) widget= %s", getName(), widget.getName());
            Widget parent = widget.getParent();
            boolean ret = false;
            if (parent instanceof ListItemHostWidget) {
                int dataIndex = ((ListItemHostWidget) parent).getDataIndex();
                for (OnItemFocusListener listener : mItemFocusListeners) {
                    listener.onLongFocus(dataIndex);
                }
                ret = true;
            } else {
                Log.d(TAG, "Long focused widget is not a list item!");
            }
            return ret;
        }
    };


    private OnTouchListener mOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(Widget widget) {
            Log.d(TAG, "onTouch(%s) widget= %s ", getName(), widget);
            Widget parent = widget.getParent();
            if (parent instanceof ListItemHostWidget) {
                int dataIndex = ((ListItemHostWidget) parent).getDataIndex();
                for (OnItemTouchListener listener : mItemTouchListeners) {
                    listener.onTouch(dataIndex);
                }
            } else {
                Log.d(TAG, "onTouch widget is not a list item!");
            }
            return false;
        }
    };

    /**
     * Set the {@link Adapter} for the {@code ListLayout}. The list will
     * immediately attempt to load data from the adapter.
     * {@link Adapter#getView} is
     * guaranteed to be called from the GL thread.
     *
     * @param adapter
     *            An adapter or {@code null} to clear the list.
     */
    protected void setAdapter(final Adapter adapter) {
        onChanged(adapter);
    }

    private void onChanged() {
        onChanged(mAdapter);
    }

    private List<DataSetObserver> mObservers = new ArrayList<DataSetObserver>();

    @Override
    public void registerDataSetObserver(final DataSetObserver observer) {
        mObservers.add(observer);
    }

    @Override
    public void unregisterDataSetObserver(final DataSetObserver observer) {
        mObservers.remove(observer);
    }

    @Override
    public boolean applyLayout(final Layout layout) {
        boolean ret = super.applyLayout(layout);
        if (ret && mAdapter != null) {
            onChanged();
        }
        return ret;
    }

    private void onChanged(final Adapter adapter) {
        runOnGlThread(new Runnable() {
            @Override
            public void run() {
                if (adapter != mAdapter) {
                    if (mAdapter != null) {
                        mAdapter.unregisterDataSetObserver(mInternalObserver);
                        clear();
                    }
                    mAdapter = adapter;
                    if (mAdapter != null) {
                        mAdapter.registerDataSetObserver(mInternalObserver);
                    }
                    for(DataSetObserver observer: mObservers) {
                        observer.onInvalidated();
                    }
                }

                onChangedImpl(-1);
            }
        });
    }

    /**
     * This method is called if the data set has been changed. Subclass might want to override this method to
     * add some extra logic.
     *
     * Go through all items in the list:
     * - reuse the existing views in the list
     * - add new views in the list if needed
     * - trim the unused views
     * - request re-layout
     *
     * @param preferableCenterPosition the preferable center position. If it is -1 - keep the
     * current center position.
     */
    protected void onChangedImpl(final int preferableCenterPosition) {
        int dataCount = getDataCount();
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onChangedImpl(%s): items [%d] views [%d] mLayouts.size() = %d " +
              "preferableCenterPosition = %d",
              getName(), dataCount, getViewCount(), mLayouts.size(), preferableCenterPosition);


        // TODO: selectively recycle data based on the changes in the data set
        recycleChildren();

        List<Widget> measuredChildren = new ArrayList<Widget>();
        for (int i = 0; i < mLayouts.size(); i++) {
            mLayouts.get(i).measureUntilFull(preferableCenterPosition, measuredChildren);
        }

        for (Widget child: measuredChildren) {
            ((ListItemHostWidget)child).setInLayout();
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onChangedImpl: item [%s] is set to the layout", child.getName());
        }

        for (Widget child: getChildren()) {
            if (child instanceof ListItemHostWidget) {
                ListItemHostWidget host = (ListItemHostWidget)child;
                if (!host.isSetInLayout()) {
                    recycle(host);
                }
            }
        }

        mTrimRequest = true;
        requestLayout();
    }

    List<ListItemHostWidget> mRecycledViews = new ArrayList<ListItemHostWidget>();
    private boolean mTrimRequest;

    private void recycleChildren() {
        for (Widget child: this.getChildren()) {
            if (child instanceof ListItemHostWidget) {
                recycle((ListItemHostWidget)child);
            }
        }
    }

    private void recycle(ListItemHostWidget host) {
        removeChild(host, true);
        for (Layout layout: mLayouts) {
            layout.invalidate(host.getDataIndex());
        }
        host.recycle();
        if (!mRecycledViews.contains(host)) {
            mRecycledViews.add(host);
        }
    }

    /**
     * This method is called if the data set has been scrolled.
     */
    protected void onScrollImpl(final Vector3Axis offset) {
        ScrollingProcessor scroller = new ScrollingProcessor(offset);
        scroller.scroll();
    }

    ScrollingProcessor mScroller = null;
    protected void onScrollImpl(final int scrollToPosition) {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onScrollImpl(): scrollToPosition = %d animated = %b",
              scrollToPosition, isTransitionAnimationEnabled());

        if (isTransitionAnimationEnabled()) {
            if (mScroller != null) {
                return;
            }
            mScroller = new ScrollingProcessor(scrollToPosition);
            mScroller.scroll();
        } else {
            onChangedImpl(scrollToPosition);
        }
    }

    //  Scrolling processor
    class ScrollingProcessor {
        private int mScrollToPosition = -1;
        private Vector3Axis mScrollByOffset = new Vector3Axis(Float.NaN, Float.NaN, Float.NaN);
        private SimpleAnimationTracker animationTracker = SimpleAnimationTracker.get(getGVRContext());

        protected List<ScrollAnimation> mScrollAnimations = new ArrayList<ScrollAnimation>();

        private class ScrollAnimation extends GVRAnimation {
            private static final float ANIMATION_SPEED = 20f; // 20 unit per sec
            private final float mShiftBy;
            private float mShiftedBy;
            private final Layout mLayout;
            private final Axis mAxis;
            private final GVRSceneObject mSceneKey;

            ScrollAnimation(Layout layout, float shiftBy, Axis axis) {
                super(new GVRSceneObject(getGVRContext()), Math.abs(shiftBy/ANIMATION_SPEED));
                mShiftBy = shiftBy;
                mLayout = layout;
                mAxis = axis;
                mSceneKey = getSceneObject();
            }

            @Override
            protected void animate(GVRHybridObject target, float ratio) {
                float shifted  = mShiftedBy;
                mShiftedBy = ratio * mShiftBy;
                mLayout.shiftBy(mShiftedBy - shifted, mAxis);
                requestLayout();
            }
        }

        ScrollingProcessor(final int pos) {
            mScrollToPosition = pos;
        }

        ScrollingProcessor(final Vector3Axis offset) {
            mScrollByOffset = offset;
        }

        private float preMeasure(Layout layout, Axis axis, List<Widget> measuredChildren) {
            float offset = Float.NaN;
            Direction direction = Direction.NONE;
            if (mScrollByOffset.isNaN()) {
                direction = layout.getDirectionToChild(mScrollToPosition, axis);
            } else {
                float distance = mScrollByOffset.get(axis);
                if (!Float.isNaN(distance)) {
                    direction = distance < 0 ? Direction.FORWARD : Direction.BACKWARD;
                }
            }
            if (direction != Direction.NONE) {
                offset = layout.preMeasureNext(measuredChildren, axis, direction);
                // reached the end of list, just move to some amount
                if (Float.isNaN(offset)) {
                    if (mScrollByOffset.isNaN()) {
                        // calculate the current distance to scrolled position
                        offset = layout.getDistanceToChild(mScrollToPosition, axis);
                    } else {
                        // the offset should not exceed the distance between the currently centered
                        // item and the tail/head
                        if (direction == Direction.FORWARD) {
                           offset = Math.max(mScrollByOffset.get(axis),
                                   layout.getDistanceToChild(getDataCount() - 1, axis));
                        } else if (direction == Direction.BACKWARD) {
                            offset = Math.min(mScrollByOffset.get(axis),
                                    layout.getDistanceToChild(0, axis));
                        }
                    }
                }
            }
            Log.d(TAG, "preMeasure direction = %s offset = %f axis = %s", direction, offset, axis);
            return offset;
        }

        private void startShifting(Layout layout, float offset, Axis axis) {
            if (Float.isNaN(offset)) {
                return;
            }

            float distance = mScrollByOffset.isNaN() ?
                    layout.getDistanceToChild(mScrollToPosition, axis) :
                        mScrollByOffset.get(axis);
            float shiftBy = offset;

            if (!Float.isNaN(distance)) {
                if (offset < 0) {
                    shiftBy = Math.max(distance, offset);
                } else {
                    shiftBy = Math.min(distance, offset);
                }

                if (!mScrollByOffset.isNaN()) {
                    mScrollByOffset.set(distance - shiftBy, axis);
                }
            }

            if (isTransitionAnimationEnabled()) {
                ScrollAnimation animation = new ScrollAnimation(layout, shiftBy, axis);
                mScrollAnimations.add(animation);
            } else {
                layout.shiftBy(shiftBy, axis);
                requestLayout();
            }
        }

        void scroll() {
            Log.d(TAG, "scroll() mScrollToPosition = %d mScrollByOffset = %s",
                  mScrollToPosition, mScrollByOffset);

            for (Layout layout: mLayouts) {
                // measure all directions. Finally measuredChildren has to contain all
                // views required for shifting toward the scrolling position.
                List<Widget> measuredChildren = new ArrayList<Widget>();
                float xOffset = preMeasure(layout, Axis.X, measuredChildren);
                float yOffset = preMeasure(layout, Axis.Y, measuredChildren);
                float zOffset = preMeasure(layout, Axis.Z, measuredChildren);

                if (Float.isNaN(xOffset) && Float.isNaN(yOffset) && Float.isNaN(zOffset)) {
                    continue;
                }

                for (Widget view: measuredChildren) {
                    ((ListItemHostWidget)view).setInLayout();
                    Log.d(TAG, "measured item: %s set in layout xOffset= %f yOffset= %f zOffset= %f",
                          view.getName(), xOffset, yOffset, zOffset);
                }

                startShifting(layout, xOffset, Axis.X);
                startShifting(layout, yOffset, Axis.Y);
                startShifting(layout, zOffset, Axis.Z);
            }

            if (mScrollAnimations.isEmpty()) {
                finish(true);
            } else {
                for (ScrollAnimation animation: mScrollAnimations) {
                    animationTracker.track(animation.mSceneKey, animation,
                                           new GVROnFinish() {
                        @Override
                        public final void finished(GVRAnimation animation) {
                            mScrollAnimations.remove(animation);
                            if (mScrollAnimations.isEmpty()) {
                                finish(false);
                            }
                        }
                    });
                }
            }
        }

        void finish(boolean force) {
            Log.d(TAG, "finish scrolling with force = %b", force);
            if (!force) {
                for (Layout layout: mLayouts) {
                    force = true;
                    int pos = layout.getCenterChild();
                    if (mScrollByOffset.isNaN()) {
                        force = pos == mScrollToPosition;
                    } else {
                        force = (Utility.equal(mScrollByOffset.get(Axis.X), 0) ||
                                    Float.isNaN(mScrollByOffset.get(Axis.X))) &&
                                (Utility.equal(mScrollByOffset.get(Axis.Y), 0) ||
                                    Float.isNaN(mScrollByOffset.get(Axis.Y))) &&
                                (Utility.equal(mScrollByOffset.get(Axis.Z), 0) ||
                                    Float.isNaN(mScrollByOffset.get(Axis.Z)));
                    }
                    if (!force) {
                        Log.d(TAG, "finish scrolling pos = %d", pos);
                        break;
                    }
                }
            }

            if (force) {
                mTrimRequest = true;
                mScroller = null;
                requestLayout();
            } else {
                scroll();
            }
        }
    }

    /**
     * Set up the view at specified position in {@link Adapter}
     * @param dataIndex position in {@link Adapter} associated with this layout.
     * @return host view
     */
    protected ListItemHostWidget setupView(final int dataIndex) {
        ListItemHostWidget host = getHostView(dataIndex);
        Widget view = getViewFromAdapter(dataIndex, host);
        if (view != null && host.isRecycled()) {
            view.setFocusEnabled(mItemFocusEnabled);
            view.setTouchable(mItemTouchable);

            view.addFocusListener(getOnFocusListener());
            view.addTouchListener(getOnTouchListener());
            host.setGuest(view, dataIndex);
            if (getChildren().contains(host)) {
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "setupItem(%s): added item(%s) at dataIndex [%d]",
                      getName(), view.getName(), dataIndex);
            } else {
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "setupItem(%s): reuse item(%s) at dataIndex [%d]",
                      getName(), view.getName(), dataIndex);
            }
        }
        return host;
    }

    /**
     * Get view displays the data at the specified position in the {@link Adapter}
     * @param index - item index in {@link Adapter}
     * @param host - view using as a host for the adapter view
     * @return view displays the data at the specified position
     */
    protected Widget getViewFromAdapter(final int index, ListItemHostWidget host) {
        Widget widget;

        widget = mAdapter.getView(index, host.getGuest(), host);
        return widget;
    }

    /**
     * Get the item id associated with the specified position in the {@link Adapter}.
     *
     * @param index
     *            The position of the item within the adapter's data set
     * @return The id of the item at the specified position.
     */
    protected long getItemId(final int index) {
        long id = -1;
        if (index < getDataCount() && index >= 0) {
            id =  mAdapter.getItemId(index);
        }
        return id;
    }

    /**
     * Trim unused views in the list
     */
    private void trim() {
        // TODO: check why some items stay INVISIBLE after the scrolling is completed.
        // Basically the layout has to be shifted by amount big enough to make the new
        // added items visible
        for (Widget widget: getChildren()) {
            if (widget.getViewPortVisibility() == ViewPortVisibility.INVISIBLE) {
                recycle((ListItemHostWidget)widget);
            }
        }
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "Trim %d items ", mRecycledViews.size());
        mRecycledViews.clear();
        mTrimRequest = false;
    }

    @Override
    protected void onLayout() {
        super.onLayout();
        if (mTrimRequest) {
            trim();
        }
    }

    /**
    * Get the view at specified list position and with specified position in {@link Adapter}
    * New host view might be created or the recycleable view might be reused if possible.
    * @param dataIndex position in {@link Adapter} associated with this layout.
    * @return host view
    */
    protected ListItemHostWidget getRecycleableView(int dataIndex) {
        ListItemHostWidget host = null;
        try {
            host = setupView(dataIndex);
            if (host != null) {
                boolean added = addChild(host, true);
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "getRecycleableView: item [%s] is added [%b] to the list",
                      host, added);
            }
        } catch (Exception e) {
            Log.e(TAG, e, "getRecycleableView(%s): exception at %d: %s",
                  getName(), dataIndex, e.getMessage());
        }
        return host;
    }

    /**
     * Create blank host view
     * @param gvrContext
     * @return host view
     */
    protected ListItemHostWidget makeHost(GVRContext gvrContext) {
        ListItemHostWidget host = new ListItemHostWidget(gvrContext);
        return host;
    }

    /**
     * Inner class to host the view from the Adapter.
     * Host widget can be recycled and reused later as a host for another guest widget.
     * Basically the host is the wrapper for the guest widget but it has its own scene
     * object associated with it. It makes possible to transform the host widget with
     * no affecting the guest transformation.
     */
    protected class ListItemHostWidget extends GroupWidget {
        public ListItemHostWidget(GVRContext gvrContext) {
            super(gvrContext, 0, 0);
            applyLayout(new AbsoluteLayout());
            recycle();
        }

        /**
         * Attach the specific guest widget to the host.
         *
         * @param guest guest widget associated with the host. It can be null.
         * @param dataIndex data index in adapter
         */
        public void setGuest(Widget guest, int dataIndex) {
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "setHostedWidget(%s): hosting [%s], same: %b", getName(),
                        guest == null ? "<null>" : guest.getName(),
                        guest == mGuestWidget);
            if (guest != mGuestWidget) {
                if (mGuestWidget != null && mGuestWidget.getParent() == this) {
                    removeChild(mGuestWidget, true);
                }
                mGuestWidget = guest;
                if (mGuestWidget != null) {
                    addChildInner(mGuestWidget, mGuestWidget.getSceneObject(), -1);
                    hostWidth = mGuestWidget.getWidth();
                    hostHeight = mGuestWidget.getHeight();
                    hostDepth = mGuestWidget.getDepth();
                    setName("HostWidget <" + mGuestWidget.getName() + ">");
                }
            }
            mDataIndex = dataIndex;
        }

        /**
         * Recycle the host. It can be later reused for another guest widget.
         */
        public void recycle() {
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "recycle(%s), dataIndex = %d", getName(), mDataIndex);
            setGuest(null, -1);
            setTouchable(false);
            setFocusEnabled(false);
            setSelected(false);
            setViewPortVisibility(ViewPortVisibility.INVISIBLE);
            mSetInLayout = false;
            hostWidth = hostHeight = hostDepth = 0;
        }

        @Override
        protected void onTransformChanged() {
            super.onTransformChanged();
            if (!isRecycled()) {
                boolean inViewport = ListWidget.this.inViewPort(mDataIndex);
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onTransformChanged inViewPort [%s], visible = %b",
                      getName(), inViewport);
                if (inViewport) {
                    Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onTransformChanged: FULLY_VISIBLE [%s] position = [%f, %f, %f]",
                          getName(), getPositionX(), getPositionY(), getPositionZ());
                    setViewPortVisibility(ViewPortVisibility.FULLY_VISIBLE);

                } else if (getViewPortVisibility() != ViewPortVisibility.INVISIBLE) {
                    Log.d(TAG, "view [%s] is outside the viewport : recycle(it!)", getName());
                    ListWidget.this.recycle(this);
                }
            } else {
                Log.w(TAG, "onTransformChanged on recycled view [%s]!", getName());
            }
        }

        @Override
        public float getWidth() {
            return hostWidth;
        }

        @Override
        public float getHeight() {
            return hostHeight;
        }

        @Override
        public float getDepth() {
            return hostDepth;
        }

        @Override
        public void setSelected(final boolean selected) {
            super.setSelected(selected);
            if (!isRecycled()) {
                mGuestWidget.setSelected(selected);
            }
        }

        public boolean isSelected() {
            return !isRecycled() ? mGuestWidget.isSelected() :
                super.isSelected();
        }

        /**
         * @return guest widget associated with the host. It can be null if the host is not visible.
         */
        public Widget getGuest() {
            return mGuestWidget;
        }

        private boolean isRecycled() {
            return mDataIndex == -1 || mGuestWidget == null;
        }

        public int getDataIndex() {
            return mDataIndex;
        }

        public boolean isSetInLayout() {
            return mSetInLayout;
        }

        public void setInLayout() {
            mSetInLayout = true;
        }

        public void removeFromLayout() {
            mSetInLayout = false;
        }

        private float hostWidth, hostHeight, hostDepth;
        private Widget mGuestWidget;
        private int mDataIndex = -1;
        private boolean mSetInLayout;
    }

    private boolean mMultiSelectionSupported;

    /**
     * Enable/disable multi-selection option
     * @param enable
     */
    public void enableMultiSelection(boolean enable) {
        mMultiSelectionSupported = enable;
    }

    /**
     * @return {@code true} if the multi-selection option is enabled
     *         {@code false} otherwise.
     */
    public boolean isMultiSelectionEnabled() {
        return mMultiSelectionSupported;
    }

    /**
     * Clear the selection of all the items if any.
     * @return {@code true} if at least one item was deselected,
     *         {@code false} otherwise.
     */
    public boolean clearSelection() {
        boolean updateLayout = false;
        for (Widget w : getChildren()) {
            ListItemHostWidget hostWidget = (ListItemHostWidget)w;
            if (hostWidget.isSelected()) {
                hostWidget.setSelected(false);
                updateLayout = true;
            }
        }
        if (updateLayout) {
            requestLayout();
        }
        return updateLayout;
    }

    /**
     * Select or deselect an item at position {@code pos}.
     *
     * @param dataIndex
     *            item position in the adapter
     * @param select
     *            operation to perform select or deselect.
     * @return {@code true} if the requested operation is successful,
     *         {@code false} otherwise.
     */
    public boolean setItemSelected(int dataIndex, boolean select) {
        boolean done = false;

        ListItemHostWidget hostWidget = (ListItemHostWidget)getHostView(dataIndex, false);
        if (hostWidget != null && hostWidget.isSelected() != select) {
            if (select && !mMultiSelectionSupported) {
                clearSelection();
            }
            hostWidget.setSelected(select);
            requestLayout();
            done = true;
        }
        return done;
    }

    /**
     * Check whether the item at position {@code pos} is selected.
     *
     * @param dataIndex
     *            item position in adapter
     * @return {@code true} if the item is selected, {@code false} otherwise.
     */
    public boolean isSelected(int dataIndex) {
        ListItemHostWidget hostWidget = (ListItemHostWidget)getHostView(dataIndex, false);
        return  (hostWidget != null) && hostWidget.isSelected();
    }

    /**
     * @return get the list of selected views.
     */
    public List<Widget> getSelectedItems() {
        List<Widget> seletcedList = new ArrayList<Widget>();
        for (Widget w : getChildren()) {
            ListItemHostWidget hostWidget = (ListItemHostWidget)w;
            if (hostWidget.isSelected()) {
                seletcedList.add(hostWidget.getGuest());
            }
        }
        return seletcedList;
    }

    //  =================== Scrolling <start> =========================

    @Override
    public boolean scrollToPosition(final int dataIndex) {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "scrollToPosition, position = %d", dataIndex);

        boolean scrolled = false;
        if (dataIndex >= 0 && dataIndex < getDataCount()) {
            onScrollImpl(dataIndex);
            scrolled = true;
        } else {
            Log.d(TAG, "Scroll out of bounds pos = [%d] getDataCount() = [%d]", dataIndex, getDataCount());
        }
        return scrolled;
    }

    /**
     * Rotate the items in the {@link ListWidget} until the item at {@code pos} is
     * rotated to {@code rotation}.
     *
     * Do not call from the GL thread; notice the waitAfterStep.
     *
     * @param dataIndex
     *            Position of the item in the data set
     * @param xOffset
     * @param yOffset
     * @param zOffset
     */
    public void scrollItemTo(final int dataIndex,
            final float xOffset, final float yOffset, final float zOffset) {
        if (dataIndex < 0 || dataIndex >= getDataCount()) {
            return;
        }
        Vector3Axis offset = new Vector3Axis(xOffset, yOffset, zOffset);
        if (offset.isInfinite() || offset.isNaN()) {
            return;
        }

        // -(offset - 180)
        Vector3Axis requiredOffset = new Vector3Axis(offset.add(-180, -180, -180).mul(-1));
        Vector3Axis currentOffset = new Vector3Axis(getItemOffsetX(dataIndex),
                       getItemOffsetY(dataIndex), getItemOffsetZ(dataIndex));
        Vector3Axis deltaOffset = requiredOffset.delta(currentOffset);

        scrollByOffset(deltaOffset.get(Axis.X),
                       deltaOffset.get(Axis.Y), deltaOffset.get(Axis.Z));
    }

    /**
     * Scroll all items in the {@link ListWidget} by {@code rotation} degrees}.
     *
     * @param xOffset
     * @param yOffset
     * @param zOffset
     *            The amount to scroll, in degrees.
     */
    @Override
    public boolean scrollByOffset(final float xOffset, final float yOffset, final float zOffset) {
        Vector3Axis offset = new Vector3Axis(xOffset, yOffset, zOffset);
        if (offset.isInfinite() || offset.isNaN()) {
            Log.e(TAG, new IllegalArgumentException(),
                  "Invalid scrolling delta: %s", offset);
            return false;
        }
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "scrollBy(%s): offset %s", getName(), offset);

        onScrollImpl(offset);
        return true;
    }

    @Override
    public int getCount() {
        return getDataCount();
    }

    /**
     * Get offset of the item along X
     *
     * @param position:  the position in the dataset.
     * @return  the x-offset of the view displaying the data at position;
     *          if that data is not being displayed, return Float.NaN.
     */
    public float getItemOffsetX(int position) {
        if (position < 0 || position >= getDataCount()) {
            return Float.NaN;
        }
        return Float.NaN;
    }

    /**
     * Get offset of the item along Y
     *
     * @param position:  the position in the dataset.
     * @return  the y-offset of the view displaying the data at position;
     *          if that data is not being displayed, return Float.NaN.
     */
    public float getItemOffsetY(int position) {
        if (position < 0 || position >= getDataCount()) {
            return Float.NaN;
        }
        return Float.NaN;
    }

    /**
     * Get offset of the item along Z
     *
     * @param position:  the position in the dataset.
     * @return  the z-offset of the view displaying the data at position;
     *          if that data is not being displayed, return Float.NaN.
     */
    public float getItemOffsetZ(int position) {
        if (position < 0 || position >= getDataCount()) {
            return Float.NaN;
        }
        return Float.NaN;
    }


    //===================== Scrolling <end> =============================

    protected ListItemHostWidget getHostView(int dataIndex) {
        return getHostView(dataIndex, true);
    }

    protected ListItemHostWidget getHostView(int dataIndex, boolean enforceNew) {
        ListItemHostWidget host = null;
        for (Widget child: this.getChildren()) {
            if (child instanceof ListItemHostWidget &&
               ((ListItemHostWidget)child).getDataIndex() == dataIndex) {
                host = ((ListItemHostWidget)child);
                break;
            }
        }

        if (host == null) {
            if (!mRecycledViews.isEmpty()) {
                host = mRecycledViews.get(0);
                mRecycledViews.remove(0);
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "reuse recycled view: %s", host);

            } else if (enforceNew) {
                host = makeHost(getGVRContext());
            }
        }

        return host;
    }

    protected int getViewCount() {
        return getChildren().size();
    }


    protected int getDataCount() {
        return mAdapter.getCount();
    }

    protected Adapter mAdapter;
    protected DataSetObserver mInternalObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            ListWidget.this.onChanged();
            // make sure it is executed after finishing onChanged()
            runOnGlThread(new Runnable() {
                @Override
                public void run() {
                    for(DataSetObserver observer: mObservers) {
                        observer.onChanged();
                    }
                }
            });
        }

        @Override
        public void onInvalidated() {
            ListWidget.this.onChanged();
            // make sure it is executed after finishing onChanged()
            runOnGlThread(new Runnable() {
                @Override
                public void run() {
                    for(DataSetObserver observer: mObservers) {
                        observer.onInvalidated();
                    }
                }
            });
        }
    };

    private static final String TAG = ListWidget.class.getSimpleName();

    /**
     * WidgetContainer implementation through Adapter
     */
    @Override
    public ListItemHostWidget get(final int dataIndex) {
        return getRecycleableView(dataIndex);
    }

    @Override
    public int size() {
        return getDataCount();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}
