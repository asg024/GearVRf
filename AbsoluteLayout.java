package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

/**
 * A layout class that leaves widgets exactly where they are placed.
 */
public class AbsoluteLayout extends GroupWidget {
    
    /**
     * Construct a wrapper for an existing {@link GVRSceneObject}.
     * 
     * @param context
     *            The current {@link GVRContext}.
     * @param sceneObject
     *            The {@link GVRSceneObject} to wrap.
     */
    public AbsoluteLayout(GVRContext context, GVRSceneObject sceneObject) {
        super(context, sceneObject);
    }
    
    /**
     * Construct a new {@link AbsoluteLayout}.
     * 
     * @param context
     *            A valid {@link GVRContext} instance.
     * @param width
     * @param height
     */
    public AbsoluteLayout(GVRContext context, float width, float height) {
        super(context, width, height);
    }

    @Override
    protected void layout() {
    }
}
