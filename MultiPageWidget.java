package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The list of the items combined into the multiple pages.
 *
 * The max number of the pages visible in the list can be set by
 * {@link MultiPageWidget#setMaxVisiblePageCount(int)}
 *
 * The custom page list layout can be applied by {@link MultiPageWidget#applyListLayout}
 *
 * The custom item layout (how the items positioned into the page) can be applied by
 * {@link MultiPageWidget#applyLayout}. If the page list is empty at the moment
 * the new item layout is applied, the item layout is stored in the list and it
 * is applied to the page as soon as new page is added to the list.
 *
 * The page list adapter can be either provided in constructor or set by
 * {@link MultiPageWidget#setListAdapter} The page list adapter has to construct
 * {@link ListWidget} type of the view
 *
 * The item adapter can be set by {@link MultiPageWidget#setAdapter}
 *
 */
public class MultiPageWidget extends ListWidget {

    /**
     * Adapter associated with the items in the pages
     */
    private Adapter mItemAdapter;

    /**
     * Keep tracking the item layouts in the page list. If the page list is empty at the moment
     * {@link MultiPageWidget#applyLayout is called, the item layout is stored in the list and it
     * is applied to the page as soon as the page is added to the list.
     */
    private final List<Layout> mItemLayouts = new ArrayList<>();

    private int mMaxVisiblePageCont = 1;

    /**
     * Construct a new {@code MultiPageWidget} instance
     * @param context
     * @param pageAdapter  {@link Adapter} for the  pages. {@link Adapter#getView} should provide
     *                  {@link ListWidget}
     * @param sceneObject
     */
    public MultiPageWidget(GVRContext context, final Adapter pageAdapter, GVRSceneObject sceneObject) {
        super(context, pageAdapter, sceneObject);
    }

    /**
     * Construct a new {@code MultiPageWidget} instance
     * @param context
     * @param pageAdapter  {@link Adapter} for the pages. {@link Adapter#getView} should provide
     *                  {@link ListWidget}
     * @param width
     * @param height
     */
    public MultiPageWidget(GVRContext context, final Adapter pageAdapter, float width, float height) {
        super(context, pageAdapter, width, height);
    }

    /**
     * Construct a new {@code MultiPageWidget} instance
     * @param context
     * @param pageAdapter  {@link Adapter} for the  pages. {@link Adapter#getView} should provide
     *                  {@link ListWidget}
     * @param sceneObject
     */
    public MultiPageWidget(final GVRContext context, final Adapter pageAdapter,
                        final GVRSceneObject sceneObject, NodeEntry attributes)
            throws InstantiationException {
        super(context, pageAdapter, sceneObject, attributes);
    }

    /**
     * Set the {@link Adapter} for the items presented into the pages. The list will
     * immediately attempt to load data from the adapter.
     * {@link Adapter#getView} is
     * guaranteed to be called from the GL thread.
     *
     * @param itemAdapter
     *            An adapter or {@code null} to clear the list.
     */
    @Override
    public void setAdapter(final Adapter itemAdapter) {
        mItemAdapter = itemAdapter;
        // TODO: apply the itemAdapter to the each page in the list
    }


    /**
     * Set the {@link Adapter} for the page list. The list will
     * immediately attempt to load data from the adapter.
     * {@link Adapter#getView} is
     * guaranteed to be called from the GL thread.
     * {@link Adapter#getView} should provide {@link ListWidget}
     *
     * @param listAdapter
     *            An adapter or {@code null} to clear the list.
     */
    protected void setListAdapter(final Adapter listAdapter) {
        super.setAdapter(listAdapter);
    }

    /**
     * Apply the layout to the each page in the list
     * @param itemLayout item layout in the page
     */
    @Override
    public boolean applyLayout(Layout itemLayout) {
        boolean applied = false;
        if (itemLayout != null && !mItemLayouts.contains(itemLayout)) {
            mItemLayouts.add(itemLayout);
            applied = true;
            // TODO: clone the layout  and apply it for each individual page
        }
        return applied;
    }

    /**
     * Apply the layout to the page list
     * @param listLayout page list layout
     */
    public boolean applyListLayout(Layout listLayout) {
        return super.applyLayout(listLayout);
    }

    /**
     * Remove the item layout {@link Layout} from the chain
     * @param layout {@link Layout} item layout
     * @return true if layout has been removed successfully , false - otherwise
     */
    public boolean removeLayout(final Layout layout) {
        boolean removed = mItemLayouts.remove(layout);
        // TODO: remove the layout from the chain for each individual page
        return removed;
    }

    /**
     * Remove the layout {@link Layout} from the chain
     * @param listLayout {@link Layout} page list layout
     * @return true if layout has been removed successfully , false - otherwise
     */
    public boolean removeListLayout(final Layout listLayout) {
        return super.removeLayout(listLayout);
    }

    /**
     * Set the max number of visible pages in the list
     * @param pageCount
     */
    public void setMaxVisiblePageCount(final int pageCount) {
        if (mMaxVisiblePageCont != pageCount) {
            mMaxVisiblePageCont = pageCount;
            // TODO: change viewport based on the pageCount and layout
            requestLayout();
        }
    }

    /**
     * Get the max number of visible pages in the list.
     */
    public int getMaxVisiblePageCount() {
        return mMaxVisiblePageCont;
    }

    @Override
    protected Widget getViewFromAdapter(final int index, ListItemHostWidget host) {
        Widget page = super.getViewFromAdapter(index, host);
        for (Layout layout: mItemLayouts) {
            if (page.mLayouts.contains(layout)) {
                page.applyLayout(layout.clone());
                // TODO: implement clone and equals for all layout types
                // TODO: make a deal with the itemAdapter
            }
        }
        return page;
    }

}
