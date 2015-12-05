package com.samsung.smcl.vr.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import com.samsung.smcl.utility.Log;

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
        List<GVRSceneObject> children = sceneObject.getChildren();
        for (GVRSceneObject sceneObjectChild : children) {
            final Widget child = WidgetFactory.createWidget(context,
                                                            sceneObjectChild);
            addChildInner(child);
        }
        // layout();
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
     * @param preventLayout
     *            The {@code Widget} whether to call layout().
     * @return {@code True} if {@code child} was added; {@code false} if
     *         {@code child} was previously added to this instance.
     */
    public boolean addChild(Widget child, boolean preventLayout) {
        return addChild(child, child.getSceneObject(), preventLayout);
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
        final List<GroupWidget> groups = new ArrayList<GroupWidget>();
        groups.add(this);

        return findChildByNameInAllGroups(name, groups);
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
        final boolean added = addChildInner(child, childRootSceneObject);
        if (added && !preventLayout) {
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
     * @param preventLayout
     *            The {@code Widget} whether to call layout().
     * @return {@code True} if {@code child} was a child of this instance and
     *         was successfully removed; {@code false} if {@code child} is not a
     *         child of this instance.
     */
    protected boolean removeChild(final Widget child,
            final GVRSceneObject childRootSceneObject, boolean preventLayout) {
        final boolean removed = mChildren.remove(child.getName()) != null;
        if (removed) {
            Log.d(TAG, "removeChild(): '%s' removed", child.getName());
            if (childRootSceneObject.getParent() != getSceneObject()) {
                Log.e(TAG,
                      "removeChild(): '%s' is not a child of '%s' GVRSceneObject!",
                      child.getName(), getName());
            }
            getSceneObject().removeChildObject(childRootSceneObject);
            child.doOnDetached();
            if (!preventLayout) {
                layout();
            }
        } else {
            Log.w(TAG, "removeChild(): '%s' is not a child of '%s'!",
                  child.getName(), getName());
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
                if (childRootSceneObject.getParent() != getSceneObject()) {
                    getSceneObject().addChildObject(childRootSceneObject);
                }
            }
            child.doOnAttached(this);
        }
        return added;
    }

    /**
     * Performs a breadth-first search of the {@link GroupWidget GroupWidgets}
     * in {@code groups} for a {@link Widget} with the specified
     * {@link Widget#getName() name}.
     * 
     * @param name
     *            The name of the {@code Widget} to find.
     * @param groups
     *            The {@code GroupWidgets} to search.
     * @return The first {@code Widget} with the specified name or {@code null}
     *         if no child of {@code groups} has that name.
     */
    static private Widget findChildByNameInAllGroups(final String name,
            List<GroupWidget> groups) {
        ArrayList<GroupWidget> groupChildren = new ArrayList<GroupWidget>();
        Widget result = null;
        for (GroupWidget group : groups) {
            // Search the immediate children of 'groups' for a match, rathering
            // the children that are GroupWidgets themselves.
            result = findChildByNameInOneGroup(name, group, groupChildren);
            if (result != null) {
                return result;
            }
        }

        // No match; Search the children that are GroupWidgets.
        return findChildByNameInAllGroups(name, groupChildren);
    }

    /**
     * Searches the immediate children of {@link GroupWidget groupWidget} for a
     * {@link Widget} with the specified {@link Widget#getName() name}.
     * <p>
     * Any non-matching {@code GroupWidget} children iterated prior to finding a
     * match will be added to {@code groupChildren}. If no match is found, all
     * immediate {@code GroupWidget} children will be added.
     * 
     * @param name
     *            The name of the {@code Widget} to find.
     * @param groupWidget
     *            The {@code GroupWidget} to search.
     * @param groupChildren
     *            Output array for non-matching {@code GroupWidget} children.
     * @return The first {@code Widget} with the specified name or {@code null}
     *         if no child of {@code groupWidget} has that name.
     */
    static private Widget findChildByNameInOneGroup(final String name,
            final GroupWidget groupWidget, ArrayList<GroupWidget> groupChildren) {
        Collection<Widget> children = groupWidget.mChildren.values();
        for (Widget child : children) {
            if (child.getName().equals(name)) {
                return child;
            }
            if (child instanceof GroupWidget) {
                // Save the child for the next level of search if needed.
                groupChildren.add((GroupWidget) child);
            }
        }
        return null;
    }

    private final Map<String, Widget> mChildren = new LinkedHashMap<String, Widget>();

    private static final String TAG = GroupWidget.class.getSimpleName();
}
