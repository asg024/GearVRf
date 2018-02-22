package com.samsung.smcl.vr.widgetlib.widget;

import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

import java.util.concurrent.Future;
// TODO: Replace mExternalData references with posting opcodes to command buffer
// TODO: Extend GVRRenderData for a static "identity" instance for "no render data" scenarios
// IDEA: With above, once we're buffering operations, have a "postOp()" method that does a
// centralized check for whether our render data is valid.  Overhead of fetching opcode instances
// from pool and configuring them should be low, and it would be a much tidier approach than doing
// "if (mRenderDataCache != null)" everywhere.
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
            final GVRMaterial material = mExternalRenderData.getMaterial();
            mRenderData.setMaterial(material);
            mMaterialCache = new MaterialCache(material);
        } else {
            mRenderData = null;
            mMaterialCache = new MaterialCache();
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

    MaterialCache getMaterial() {
        return mMaterialCache;
    }

    void setMaterial(GVRMaterial material) {
        if (mRenderData != null) {
            mExternalRenderData.setMaterial(material);
            mRenderData.setMaterial(material);
        }
        mMaterialCache.set(material);
    }

    static class MaterialCache {

        static final String MATERIAL_DIFFUSE_TEXTURE = "diffuseTexture";

        public void setColor(int color) {
            if (mMaterial != null) {
                mExternalMaterial.setColor(color);
                mMaterial.setColor(color);
            }
        }

        public void setColor(float r, float g, float b) {
            if (mMaterial != null) {
                mExternalMaterial.setColor(r, g, b);
                mMaterial.setColor(r, g, b);
            }
        }

        public void setTexture(GVRTexture texture) {
            if (mMaterial != null) {
                mExternalMaterial.setMainTexture(texture);
                mExternalMaterial.setTexture(MATERIAL_DIFFUSE_TEXTURE, texture);
                mMaterial.setMainTexture(texture);
                //models use the new shader framework which has no single main texture
                mMaterial.setTexture(MATERIAL_DIFFUSE_TEXTURE, texture);
            }
        }

        public void setTexture(Future<GVRTexture> texture) {
            if (mMaterial != null) {
                mExternalMaterial.setMainTexture(texture);
                mExternalMaterial.setTexture(MATERIAL_DIFFUSE_TEXTURE, texture);
                mMaterial.setMainTexture(texture);
                //models use the new shader framework which has no single main texture
                mMaterial.setTexture(MATERIAL_DIFFUSE_TEXTURE, texture);
            }
        }

        public void setTexture(String name, GVRTexture texture) {
            if (mMaterial != null) {
                mExternalMaterial.setTexture(name, texture);
                mMaterial.setTexture(name, texture);
            }
        }

        public void setTexture(String name, Future<GVRTexture> texture) {
            if (mMaterial != null) {
                mExternalMaterial.setTexture(name, texture);
                mMaterial.setTexture(name, texture);
            }
        }

        public float getOpacity() {
            if (mMaterial != null) {
                return mMaterial.getOpacity();
            }
            return 0;
        }

        public void setOpacity(float opacity) {
            if (mMaterial != null) {
                mExternalMaterial.setOpacity(opacity);
                mMaterial.setOpacity(opacity);
            }
        }

        public int getRgbColor() {
            if (mMaterial != null) {
                return mMaterial.getRgbColor();
            }
            return 0;
        }

        public float[] getColor() {
            if (mMaterial != null) {
                return mMaterial.getColor();
            }
            return null;
        }

        private MaterialCache() {

        }

        private MaterialCache(GVRMaterial material) {
            set(material);
        }

        private void set(GVRMaterial material) {
            if (material != null) {
                mMaterial = new GVRMaterial(material.getGVRContext());
                // TODO: Add named texture entry for main texture
                mMaterial.setMainTexture(material.getMainTexture());
                mMaterial.setOpacity(material.getOpacity());
                mMaterial.setColor(material.getRgbColor());
                for (String textureName : material.getTextureNames()) {
                    mMaterial.setTexture(textureName, material.getTexture(textureName));
                }
            } else {
                mMaterial = null;
            }
            mExternalMaterial = material;
        }

        private GVRMaterial mExternalMaterial;
        private GVRMaterial mMaterial;
    }

    private final GVRRenderData mExternalRenderData;
    private final GVRRenderData mRenderData;
    private final MaterialCache mMaterialCache;
}
