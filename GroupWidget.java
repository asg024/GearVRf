package com.samsung.smcl.vr.widgets;

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import com.samsung.smcl.utility.Log;
import com.samsung.smcl.vr.widgets.Layout.WidgetContainer;

public class GroupWidget extends Widget implements WidgetContainer {

    /**
     * Construct a wrapper for an existing {@link GVRSceneObject}.
     *
     * @param context
     *            The current {@link GVRContext}.
     * @param sceneObject
     *            The {@link GVRSceneObject} to wrap.
     */
    public GroupWidget(GVRContext context, GVRSceneObject sceneObject) {
        super(context, sceneObject);
        mViewPort = new Vector3Axis(getWidth(), getHeight(), getDepth());
    }

    public GroupWidget(final GVRContext context,
            final GVRSceneObject sceneObject, NodeEntry attributes)
            throws InstantiationException {
        super(context, sceneObject, attributes);
        mViewPort = new Vector3Axis(getWidth(), getHeight(), getDepth());
    }

    /**
     * Construct a new {@link GroupWidget}.
     *
     * @param context
     *            A valid {@link GVRContext} instance.
     * @param width
     * @param height
     */
    public GroupWidget(GVRContext context, float width, float height) {
        super(context, width, height);
        mViewPort = new Vector3Axis(getWidth(), getHeight(), getDepth());
    }

    /**
     * Add another {@link Widget} as a child of this one.
     *
     * @param child
     *            The {@code Widget} to add as a child.
     * @return {@code True} if {@code child} was added; {@code false} if
     *         {@code child} was previously added to this instance.
     */
    public boolean addChild(final Widget child) {
        return addChild(child, child.getSceneObject());
    }

    /**
     * Add another {@link Widget} as a child of this one.
     *
     * @param child
     *            The {@code Widget} to add as a child.
     * @param index
     *            Position at which to add the child.
     * @return {@code True} if {@code child} was added; {@code false} if
     *         {@code child} was previously added to this instance.
     */
    public boolean addChild(final Widget child, int index) {
        return addChild(child, child.getSceneObject(), index);
    }

    /**
     * Add another {@link Widget} as a child of this one.
     *
     * @param child
     *            The {@code Widget} to add as a child.
     * @param preventLayout
     *            The {@code Widget} whether to call layout().
     * @return {@code True} if {@code child} was added; {@code false} if
     *         {@code child} was previously added to this instance.
     */
    public boolean addChild(Widget child, boolean preventLayout) {
        return addChild(child, child.getSceneObject(), preventLayout);
    }

    /**
     * Add another {@link Widget} as a child of this one.
     *
     * @param child
     *            The {@code Widget} to add as a child.
     * @param index
     *            Position at which to add the child.
     * @param preventLayout
     *            The {@code Widget} whether to call layout().
     * @return {@code True} if {@code child} was added; {@code false} if
     *         {@code child} was previously added to this instance.
     */
    public boolean addChild(Widget child, int index, boolean preventLayout) {
        return addChild(child, child.getSceneObject(), index, preventLayout);
    }

    /**
     * Remove a {@link Widget} as a child of this instance.
     *
     * @param child
     *            The {@code Widget} to remove.
     * @return {@code True} if {@code child} was a child of this instance and
     *         was successfully removed; {@code false} if {@code child} is not a
     *         child of this instance.
     */
    public boolean removeChild(final Widget child) {
        return removeChild(child, child.getSceneObject());
    }

    /**
     * Remove a {@link Widget} as a child of this instance.
     *
     * @param child
     *            The {@code Widget} to remove.
     * @param preventLayout
     *            The {@code Widget} whether to call layout().
     * @return {@code True} if {@code child} was a child of this instance and
     *         was successfully removed; {@code false} if {@code child} is not a
     *         child of this instance.
     */
    public boolean removeChild(Widget child, boolean preventLayout) {
        return removeChild(child, child.getSceneObject(), preventLayout);
    }

    /**
     * Performs a breadth-first recursive search for a {@link Widget} with the
     * specified {@link Widget#getName() name}.
     *
     * @param name
     *            The name of the {@code Widget} to find.
     * @return The first {@code Widget} with the specified name or {@code null}
     *         if no child of this {@code Widget} has that name.
     */
    public Widget findChildByName(final String name) {
        return super.findChildByName(name);
    }

    /**
     * @return A copy of the list of {@link Widget widgets} that are children of
     *         this instance.
     */
    public List<Widget> getChildren() {
        return super.getChildren();
    }

    public Widget getChild(int index) {
        return getChildren().get(index);
    }

    /**
     * Add another {@link Widget} as a child of this one.
     * <p>
     * A {@link GVRSceneObject} other than the one directly managed by the child
     * {@code Widget} can be specified as the child's root. This is useful in
     * cases where the parent object needs to insert additional scene objects
     * between the child and its parent.
     * <p>
     * <b>NOTE:</b> it is the responsibility of the caller to keep track of the
     * relationship between the child {@code Widget} and the alternative root
     * scene object.
     *
     * @param child
     *            The {@code Widget} to add as a child.
     * @param childRootSceneObject
     *            The root {@link GVRSceneObject} of the child.
     * @return {@code True} if {@code child} was added; {@code false} if
     *         {@code child} was previously added to this instance.
     */
    protected boolean addChild(final Widget child,
            final GVRSceneObject childRootSceneObject) {
        return addChild(child, childRootSceneObject, -1);
    }

    /**
     * Add another {@link Widget} as a child of this one.
     * <p>
     * A {@link GVRSceneObject} other than the one directly managed by the child
     * {@code Widget} can be specified as the child's root. This is useful in
     * cases where the parent object needs to insert additional scene objects
     * between the child and its parent.
     * <p>
     * <b>NOTE:</b> it is the responsibility of the caller to keep track of the
     * relationship between the child {@code Widget} and the alternative root
     * scene object.
     *
     * @param child
     *            The {@code Widget} to add as a child.
     * @param childRootSceneObject
     *            The root {@link GVRSceneObject} of the child.
     * @param index
     *            Position at which to add the child.
     * @return {@code True} if {@code child} was added; {@code false} if
     *         {@code child} was previously added to this instance.
     */
    protected boolean addChild(final Widget child,
            final GVRSceneObject childRootSceneObject, final int index) {
        return addChild(child, childRootSceneObject, index, false);
    }

    /**
     * Remove a {@link Widget} as a child of this instance.
     * <p>
     * <b>NOTE:</b> if an alternative root scene object was used to
     * {@linkplain #addChild(Widget, GVRSceneObject) add} the child
     * {@code Widget}, the caller must pass the alternative root to this method.
     * Otherwise there may be dangling scene objects. It is the responsibility
     * of the caller to keep track of the relationship between the child
     * {@code Widget} and the alternative root scene object.
     *
     * @param child
     *            The {@code Widget} to remove.
     * @param childRootSceneObject
     * @return {@code True} if {@code child} was a child of this instance and
     *         was successfully removed; {@code false} if {@code child} is not a
     *         child of this instance.
     */
    protected boolean removeChild(final Widget child,
            final GVRSceneObject childRootSceneObject) {
        return removeChild(child, childRootSceneObject, false);
    }

    /**
     * Add another {@link Widget} as a child of this one.
     * <p>
     * A {@link GVRSceneObject} other than the one directly managed by the child
     * {@code Widget} can be specified as the child's root. This is useful in
     * cases where the parent object needs to insert additional scene objects
     * between the child and its parent.
     * <p>
     * <b>NOTE:</b> it is the responsibility of the caller to keep track of the
     * relationship between the child {@code Widget} and the alternative root
     * scene object.
     *
     * @param child
     *            The {@code Widget} to add as a child.
     * @param childRootSceneObject
     *            The root {@link GVRSceneObject} of the child.
     * @param preventLayout
     *            The {@code Widget} whether to call layout().
     * @return {@code True} if {@code child} was added; {@code false} if
     *         {@code child} was previously added to this instance.
     */
    protected boolean addChild(final Widget child,
            final GVRSceneObject childRootSceneObject, boolean preventLayout) {
        return addChild(child, childRootSceneObject, -1, preventLayout);
    }

    /**
     * Add another {@link Widget} as a child of this one.
     * <p>
     * A {@link GVRSceneObject} other than the one directly managed by the child
     * {@code Widget} can be specified as the child's root. This is useful in
     * cases where the parent object needs to insert additional scene objects
     * between the child and its parent.
     * <p>
     * <b>NOTE:</b> it is the responsibility of the caller to keep track of the
     * relationship between the child {@code Widget} and the alternative root
     * scene object.
     *
     * @param child
     *            The {@code Widget} to add as a child.
     * @param childRootSceneObject
     *            The root {@link GVRSceneObject} of the child.
     * @param index
     *            Position at which to add the child.
     * @param preventLayout
     *            The {@code Widget} whether to call layout().
     * @return {@code True} if {@code child} was added; {@code false} if
     *         {@code child} was previously added to this instance.
     */
    protected boolean addChild(final Widget child,
            final GVRSceneObject childRootSceneObject, final int index,
            boolean preventLayout) {
        final boolean added = addChildInner(child, childRootSceneObject, index);
        if (added && !preventLayout) {
            requestLayout();
        }
        return added;
    }

    /**
     * Remove a {@link Widget} as a child of this instance.
     * <p>
     * <b>NOTE:</b> if an alternative root scene object was used to
     * {@linkplain #addChild(Widget, GVRSceneObject) add} the child
     * {@code Widget}, the caller must pass the alternative root to this method.
     * Otherwise there may be dangling scene objects. It is the responsibility
     * of the caller to keep track of the relationship between the child
     * {@code Widget} and the alternative root scene object.
     *
     * @param child
     *            The {@code Widget} to remove.
     * @param childRootSceneObject
     * @param preventLayout
     *            The {@code Widget} whether to call layout().
     * @return {@code True} if {@code child} was a child of this instance and
     *         was successfully removed; {@code false} if {@code child} is not a
     *         child of this instance.
     */
    protected boolean removeChild(final Widget child,
            final GVRSceneObject childRootSceneObject, boolean preventLayout) {
        return super.removeChild(child, childRootSceneObject, preventLayout);
    }

    public void clear() {
        List<Widget> children = getChildren();
        Log.d(TAG, "clear(%s): removing %d children", getName(), children.size());
        for (Widget child : children) {
            removeChild(child, true);
        }
        requestLayout();
    }

    private Vector3Axis mViewPort;
    public void setViewPort(final float viewportWidth, final float viewportHeight, final float viewportDepth) {
        mViewPort = new Vector3Axis(viewportWidth, viewportHeight, viewportDepth);
        for (Layout layout: mLayouts) {
            layout.onLayoutApplied(this, mViewPort);
        }
        Log.d(TAG, "groupWidget[%s] setViewPort : viewport = %s", mViewPort, this);
    }

    protected boolean inViewPort(final int dataIndex) {
        boolean inViewPort = true;

        for (Layout layout: mLayouts) {
            inViewPort = inViewPort && layout.inViewPort(dataIndex);
        }
        return inViewPort;
    }

    /**
     * Apply {@link Layout}
     * @param layout {@link Layout}
     * @return true if layout has been applied successfully , false - otherwise
     */
    public boolean applyLayout(final Layout layout) {
        boolean applied = false;
        if (!mLayouts.contains(layout) && isValidLayout(layout)) {
            layout.onLayoutApplied(this, mViewPort);
            mLayouts.add(layout);
            requestLayout();
            applied = true;
        }
        return applied;
    }

    /**
     * Remove the layout {@link Layout} from the chain
     * @param layout {@link Layout}
     * @return true if layout has been removed successfully , false - otherwise
     */
    public boolean removeLayout(final Layout layout) {
        boolean removed = mLayouts.remove(layout);
        requestLayout();
        return removed;
    }

    /*
     * Any layout is valid by default. Subclass can override the method to add new check
     */
    protected boolean isValidLayout(Layout layout) {
        return true;
    }

    /**
     * Create a child {@link Widget} to wrap a {@link GVRSceneObject}. Deriving
     * classes can override this method to handle creation of specific Widgets.
     *
     * @param context
     *            The current {@link GVRContext}.
     * @param sceneObjectChild
     *            The {@link GVRSceneObject} to wrap.
     * @return
     * @throws InstantiationException
     */
    protected Widget createChild(final GVRContext context,
            GVRSceneObject sceneObjectChild) throws InstantiationException {
        return super.createChild(context, sceneObjectChild);
    }

    protected void createChildren(final GVRContext context,
            final GVRSceneObject sceneObject) throws InstantiationException {
        super.createChildren(context, sceneObject);
    }

    protected boolean mEnableTransitionAnimation;

    public void enableTransitionAnimation(final boolean enable) {
        mEnableTransitionAnimation = enable;
    }

    public boolean isTransitionAnimationEnabled() {
        return mEnableTransitionAnimation;
    }

    @Override
    protected void onLayout() {
        Log.d(TAG, "layout() called (%s)", getName());
        if (mLayouts.isEmpty()) {
            Log.w(TAG, "No any layout has been applied! %s", getName());
            return;
        }

        List<Widget> children = getChildren();
        if (children.isEmpty()) {
            Log.d(TAG, "layout: no items to layout! %s", getName());
            return;
        }

        for (Widget child : children) {
            child.layout();
        }

        for (Layout layout: mLayouts) {
            Log.d(TAG, "[%s] apply layout = %s", this, layout);
            if (!isDynamic()) {
                layout.measureAll(null);
            } else {
                // don't need to measure the items for Dynamic data set. The items are measured
                // before onLayout call
            }
            layout.layoutChildren();
        }
    }

    protected List<Layout> mLayouts = new ArrayList<Layout>();

    @SuppressWarnings("unused")
    private static final String TAG = GroupWidget.class.getSimpleName();

    /**
     * WidgetContainer default implementation
     */
    @Override
    public Widget get(final int dataIndex) {
        return getChild(dataIndex);
    }

    @Override
    public int size() {
        return getChildren().size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}
