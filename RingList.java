package com.samsung.smcl.vr.widgets;

import java.util.ArrayList;
import java.util.List;

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

    public interface LayoutListener {
        void onLayoutItem(int itemIndex, int numItems, float phi, float rho);
    }

    LayoutListener mLayoutListener;

    public void registerLayoutListener(LayoutListener listener) {
        mLayoutListener = listener;
    }

    @Override
    protected void layout() {
        if (mItems.isEmpty()) {
            Log.d(TAG, "layout(): no items to layout!");
            return;
        }

        final int numItems = mItems.size();
        Log.d(TAG, "layout(): laying out %d items", numItems);
        final float[] angularWidths = new float[numItems];
        for (int i = 0; i < numItems; ++i) {
            angularWidths[i] = LayoutHelpers.calculateAngularWidth(mItems.get(i), mRho);
            Log.d(TAG, "layout(): angular width at %d: %f", i, angularWidths[i]);
        }
        float phi = 0;
        for (int i = 0; i < numItems; ++i) {
            Log.d(TAG, "layout(): phi at %d: %f", i, phi);
            mItems.get(i).setRotation(1, 0, 0, 0);
            mItems.get(i).setPosition(0, 0, -(float) mRho);
            mItems.get(i).rotateByAxisWithPivot(phi, 0, 1, 0, 0, 0, 0);
            if (mLayoutListener != null) {
                mLayoutListener.onLayoutItem(i, numItems, phi, -(float)mRho);
            }
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
            mAdapter.getView(pos, mItems.get(pos),
                    RingList.this);
        }

        // Get any additional items
        for (; pos < itemCount; ++pos) {
            Widget item = mAdapter.getView(pos, null, RingList.this);
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

    private Adapter mAdapter;
    private float mItemPadding;
    private List<Widget> mItems = new ArrayList<Widget>();
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
