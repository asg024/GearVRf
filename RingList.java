package com.samsung.smcl.vr.widgets;

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;

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
public class RingList extends GVRSceneObject {

    /**
     * Construct a new {@code RingList} instance with a {@link #setRho(double)
     * radius} of zero.
     * 
     * @param gvrContext
     *            The active {@link GVRContext}.
     */
    public RingList(final GVRContext gvrContext) {
        super(gvrContext);
    }

    /**
     * Construct a new {@code RingList} instance with the specified radius.
     * 
     * @param gvrContext
     *            The active {@link GVRContext}.
     * @param rho
     *            The radius of the {@code RingList}.
     */
    public RingList(final GVRContext gvrContext, final double rho) {
        super(gvrContext);
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

    private float calculateAngularWidth(final GVRSceneObject item) {
        final float geometricWidth = calculateGeometricWidth(item);

        // The item is perpendicular to the center of the ring at *its* center,
        // like a "T". The triangle, then, is between the ring's center, the
        // item's center, and the "edge" of the item's bounding box. The length
        // of the "opposite" side, therefore, is only half the item's geometric
        // width.
        final double opposite = geometricWidth / 2;
        final double tangent = opposite / mRho; // The rho is the "adjacent"
                                                // side
        final double radians = Math.atan(tangent);

        // The previous calculation only gives us half the angular width, since
        // it is reckoned from the item's center to its edge.
        return (float) Math.toDegrees(radians) * 2;
    }

    private float calculateGeometricWidth(final GVRSceneObject item) {
        GVRMesh boundingBox = item.getRenderData().getMesh().getBoundingBox();
        final float[] vertices = boundingBox.getVertices();
        final int numVertices = vertices.length / 3;
        float minX = 0, maxX = 0;
        for (int i = 0; i < numVertices; ++i) {
            final float x = vertices[i * 3];
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
        }
        float width = maxX - minX;
        GVRTransform transform = item.getTransform();
        float xScale = transform.getScaleX();
        return width * xScale;
    }

    private void clear() {
        List<GVRSceneObject> children = new ArrayList<GVRSceneObject>(getChildren());
        Log.d(TAG, "clear(): removing %d children", children.size());
        for (GVRSceneObject child : children) {
            removeChildObject(child);
        }
    }

    private void layout() {
        if (mItems.isEmpty()) {
            Log.d(TAG, "layout(): no items to layout!");
            return;
        }

        final int numItems = mItems.size();
        Log.d(TAG, "layout(): laying out %d items", numItems);
        final float[] angularWidths = new float[numItems];
        for (int i = 0; i < numItems; ++i) {
            angularWidths[i] = calculateAngularWidth(mItems.get(i));
            Log.d(TAG, "layout(): angular width at %d: %f", i, angularWidths[i]);
        }
        float phi = 0;
        for (int i = 0; i < numItems; ++i) {
            Log.d(TAG, "layout(): phi at %d: %f", i, phi);
            GVRSceneObject item = mItems.get(i);
            GVRTransform transform = item.getTransform();

            transform.setPosition(0, 0, -(float) mRho);
            transform.rotateByAxisWithPivot(phi, 0, 1, 0, 0, 0, 0);
            phi -= angularWidths[i] + mItemPadding;
        }
    }

    private void onChanged() {
        onChanged(mAdapter);
    }

    private void onChanged(final Adapter adapter) {
        getGVRContext().runOnGlThread(new Runnable() {
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
            mAdapter.getView(pos, mItems.get(pos), RingList.this);
        }

        // Get any additional items
        for (; pos < itemCount; ++pos) {
            GVRSceneObject item = mAdapter
                    .getView(pos, null, RingList.this);
            mItems.add(item);
            Log.d(TAG, "onChanged(): added item at %d", pos);
            addChildObject(item);
        }

        // Trim unused items
        Log.d(TAG, "onChanged(): trimming: %b", pos < mItems.size());
        for (; pos < mItems.size(); ++pos) {
            GVRSceneObject item = mItems.remove(pos);
            removeChildObject(item);
        }

        layout();

        List<GVRSceneObject> children = getChildren();
        for (int i = 0; i < children.size(); ++i) {
            GVRTransform transform = children.get(i).getTransform();
            Log.d(TAG,
                  "layout(): item at %d {%05.2f, %05.2f, %05.2f}, {%05.2f, %05.2f, %05.2f}",
                  i, transform.getPositionX(), transform.getPositionY(),
                  transform.getPositionZ(), transform.getRotationX(),
                  transform.getRotationY(), transform.getRotationZ());
        }
        Log.d(TAG, "onChanged(): child objects: %d", getChildrenCount());
    }

    private Adapter mAdapter;
    private float mItemPadding;
    private List<GVRSceneObject> mItems = new ArrayList<GVRSceneObject>();
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
