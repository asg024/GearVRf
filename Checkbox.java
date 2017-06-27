package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;

public class Checkbox extends CheckableButton {

    private static final float PADDING_Z = 0.025f;
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

    protected LightTextWidget createTextWidget() {
        LightTextWidget textWidget = super.createTextWidget();
        textWidget.setPositionZ(PADDING_Z);
        return textWidget;
    }

    @Override
    protected float getTextWidgetWidth() {
        return getWidth() - getHeight() - getDefaultLayout().getDividerPadding(Layout.Axis.X);
    }

    @Override
    protected Widget createGraphicWidget() {
        Widget graphic = new Graphic(getGVRContext(), getHeight());
        graphic.setPositionZ(PADDING_Z);
        graphic.setRenderingOrder(getRenderingOrder() + 1);
        return graphic;
    }

    static private class Graphic extends Widget {
        Graphic(GVRContext context, float size) {
            super(context, size, size);
        }
    }

    @SuppressWarnings("unused")
    private static final String TAG = Checkbox.class.getSimpleName();
}
