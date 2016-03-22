package com.samsung.smcl.vr.widgets;

import java.util.List;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

public abstract class GroupWidget extends Widget {

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
    }

    public GroupWidget(final GVRContext context,
            final GVRSceneObject sceneObject, NodeEntry attributes)
            throws InstantiationException {
        super(context, sceneObject, attributes);
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
    protected boolean removeChild(Widget child, boolean preventLayout) {
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

    @Override
    protected abstract void onLayout();
    
    @SuppressWarnings("unused")
    private static final String TAG = GroupWidget.class.getSimpleName();
}
