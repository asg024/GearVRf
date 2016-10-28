package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRSceneObject;

public class Checkbox extends CheckableButton {

    public Checkbox(GVRContext context, float width, float height) {
        super(context, width, height);
    }

    public Checkbox(GVRContext context, GVRSceneObject sceneObject,
            NodeEntry attributes) throws InstantiationException {
        super(context, sceneObject, attributes);
    }

    public Checkbox(GVRContext context, GVRSceneObject sceneObject) {
        super(context, sceneObject);
    }

    protected Checkbox(GVRContext context, GVRMesh mesh) {
        super(context, mesh);
    }

    @Override
    protected Widget createGraphicWidget() {
        return new Graphic(getGVRContext(), getHeight());
    }

    static private class Graphic extends Widget {
        Graphic(GVRContext context, float size) {
            super(context, size, size);
            setRenderingOrder(GVRRenderingOrder.TRANSPARENT);
        }
    }

    @SuppressWarnings("unused")
    private static final String TAG = Checkbox.class.getSimpleName();
}
