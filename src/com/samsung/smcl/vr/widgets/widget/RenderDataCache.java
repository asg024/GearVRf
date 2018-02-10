package com.samsung.smcl.vr.widgets.widget;

import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

// TODO: Encapsulate access/modification of material
// TODO: Replace mExternalData references with posting opcodes to command buffer
class RenderDataCache {
    RenderDataCache(GVRSceneObject sceneObject) {
        mExternalRenderData = sceneObject.getRenderData();
        if (mExternalRenderData != null) {
            mRenderData = new GVRRenderData(sceneObject.getGVRContext());
            mRenderData.setDepthTest(mExternalRenderData.getDepthTest());
            mRenderData.setMesh(mExternalRenderData.getMesh());
            mRenderData.setOffset(mExternalRenderData.getOffset());
            mRenderData.setOffsetFactor(mExternalRenderData.getOffsetFactor());
            mRenderData.setOffsetUnits(mExternalRenderData.getOffsetUnits());
            mRenderData.setRenderingOrder(mExternalRenderData.getRenderingOrder());

            // No getters available!!!
//            mRenderData.setStencilFunc(...);
//            mRenderData.setStencilMask(renderData.getStencilMask());
//            mRenderData.setStencilTest(renderData.getStencilTest());
        } else {
            mRenderData = null;
        }
    }

    boolean hasRenderData() {
        return mRenderData != null;
    }

    void setMesh(GVRMesh mesh) {
        if (mRenderData != null) {
            mExternalRenderData.setMesh(mesh);
            mRenderData.setMesh(mesh);
        }
    }

    void setOffset(boolean offset) {
        if (mRenderData != null) {
            mExternalRenderData.setOffset(offset);
            mRenderData.setOffset(offset);
        }
    }

    float getOffsetFactor() {
        if (mRenderData != null) {
            return mRenderData.getOffsetFactor();
        }
        return 0;
    }

    boolean getOffset() {
        return mRenderData != null && mRenderData.getOffset();
    }

    float getOffsetUnits() {
        if (mRenderData != null) {
            return mRenderData.getOffsetUnits();
        }
        return 0;
    }

    int getRenderingOrder() {
        if (mRenderData != null) {
            return mRenderData.getRenderingOrder();
        }
        return -1;
    }

    GVRMesh getMesh() {
        if (mRenderData != null) {
            return mRenderData.getMesh();
        }
        return null;
    }

    void setOffsetFactor(float offsetFactor) {
        if (mRenderData != null) {
            mExternalRenderData.setOffsetFactor(offsetFactor);
            mRenderData.setOffsetFactor(offsetFactor);
        }
    }

    void setRenderingOrder(int renderingOrder) {
        if (mRenderData != null) {
            mExternalRenderData.setRenderingOrder(renderingOrder);
            mRenderData.setRenderingOrder(renderingOrder);
        }
    }

    void setOffsetUnits(float offsetUnits) {
        if (mRenderData != null) {
            mExternalRenderData.setOffsetUnits(offsetUnits);
            mRenderData.setOffsetUnits(offsetUnits);
        }
    }

    boolean getDepthTest() {
        return mRenderData != null && mRenderData.getDepthTest();
    }

    void setDepthTest(boolean depthTest) {
        if (mRenderData != null) {
            mExternalRenderData.setDepthTest(depthTest);
            mRenderData.setDepthTest(depthTest);
        }
    }

    void setStencilTest(boolean flag) {
        if (mRenderData != null) {
            mExternalRenderData.setStencilTest(flag);
            mRenderData.setStencilTest(flag);
        }
    }

    void setStencilFunc(int func, int ref, int mask) {
        if (mRenderData != null) {
            mExternalRenderData.setStencilFunc(func, ref, mask);
            mRenderData.setStencilFunc(func, ref, mask);
        }
    }

    void setStencilMask(int mask) {
        if (mRenderData != null) {
            mExternalRenderData.setStencilMask(mask);
            mRenderData.setStencilMask(mask);
        }
    }

    GVRMaterial getMaterial() {
        if (mRenderData != null) {
            return mExternalRenderData.getMaterial();
        }
        return null;
    }

    void setMaterial(GVRMaterial material) {
        if (mRenderData != null) {
            mExternalRenderData.setMaterial(material);
            mRenderData.setMaterial(material);
        }
    }

    private final GVRRenderData mExternalRenderData;
    private final GVRRenderData mRenderData;
}
