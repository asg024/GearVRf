package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

/**
 * A {@link GroupWidget} that wraps an existing {@link GVRSceneObject}. Use to
 * establish the root of a {@link Widget} tree.
 */
public class SceneObjectWidget extends GroupWidget {

    /**
     * Construct a new {@link SceneObjectWidget}.
     * 
     * @param context
     *            A valid {@link GVRContext} instance.
     * @param sceneObject
     *            The {@link GVRSceneObject} to wrap.
     */
    SceneObjectWidget(final GVRContext context, final GVRSceneObject sceneObject) {
        super(context, sceneObject);
    }

    @Override
    protected void layout() {
    }
}
