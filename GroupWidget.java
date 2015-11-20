package com.samsung.smcl.vr.widgets;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public Widget findChild(final String name) {
        return mChildren.get(name);
    }

    /**
     * @return A copy of the list of {@link Widget widgets} that are children of
     *         this instance.
     */
    public List<Widget> getChildren() {
        return new ArrayList<Widget>(mChildren.values());
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
        final boolean added = addChildInner(child, childRootSceneObject);
        if (added) {
            layout();
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
     * @return {@code True} if {@code child} was a child of this instance and
     *         was successfully removed; {@code false} if {@code child} is not a
     *         child of this instance.
     */
    protected boolean removeChild(final Widget child,
            final GVRSceneObject childRootSceneObject) {
        final boolean removed = mChildren.remove(child) != null;
        if (removed) {
            getSceneObject().removeChildObject(childRootSceneObject);
            child.doOnDetached();
            layout();
        }
        return removed;
    }

    protected abstract void layout();

    private boolean addChildInner(final Widget child) {
        return addChildInner(child, child.getSceneObject());
    }

    private boolean addChildInner(final Widget child,
            final GVRSceneObject childRootSceneObject) {
        final Widget previousChild = mChildren.put(child.getName(), child);
        final boolean added = child != previousChild;
        if (added) {
            if (child.getVisibility() == Visibility.VISIBLE) {
                getSceneObject().addChildObject(childRootSceneObject);
            }
            child.doOnAttached(this);
        }
        return added;
    }

    private final Map<String, Widget> mChildren = new LinkedHashMap<String, Widget>();
}
