package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;

public class RadioButton extends CheckableButton {
    public RadioButton(GVRContext context, float width, float height) {
        super(context, width, height);
    }

    public RadioButton(GVRContext context, GVRSceneObject sceneObject, NodeEntry attributes) throws InstantiationException {
        super(context, sceneObject, attributes);
    }

    public RadioButton(GVRContext context, GVRSceneObject sceneObject) {
        super(context, sceneObject);
    }

    protected RadioButton(GVRContext context, GVRMesh mesh) {
        super(context, mesh);
    }

    @Override
    public void toggle() {
        if (!isChecked()) {
            super.toggle();
        }
    }

    @Override
    protected Widget createGraphicWidget() {
        return new Graphic(getGVRContext(), getHeight());
    }

    static private class Graphic extends Widget {
        Graphic(GVRContext context, float size) {
            super(context, size, size);
            setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
        }
    }
}
