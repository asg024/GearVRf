package com.samsung.smcl.vr.widgets.widget;

import android.app.Activity;

import com.samsung.smcl.vr.widgets.log.Log;
import com.samsung.smcl.vr.widgets.main.Selector;

import org.gearvrf.GVRCollider;
import org.gearvrf.GVRColliderGroup;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker.GVRPickedObject;
import org.gearvrf.GVRSceneObject;

import java.lang.ref.WeakReference;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class TouchManager {

    public interface OnTouch {
        /**
         * To determine if a sceneObject is touchable.
         *
         * @param sceneObject
         * @param coords      GVRF raw coordinates
         * @return True, when event has been handled & no further processing
         * needed. False, when event was intercepted and may need future
         * processing
         */
        boolean touch(GVRSceneObject sceneObject, final float[] coords);

        /**
         * To determine if a sceneObject is processing back key.
         *
         * @param sceneObject
         * @param coords      GVRF raw coordinates
         * @return True, when back key event has been handled & no further processing
         * needed. False, when event was intercepted and may need future
         * processing
         */
        boolean onBackKey(GVRSceneObject sceneObject, final float[] coords);
    }

    private final Map<GVRSceneObject, WeakReference<OnTouch>> touchHandlers = new WeakHashMap<GVRSceneObject, WeakReference<OnTouch>>();
    private Runnable defaultLeftClickAction = null;
    private Runnable defaultRightClickAction = null;
    private Set<OnTouch> mOnTouchInterceptors = new LinkedHashSet<>();

    /**
     * Creates TouchManager
     */
    public TouchManager(GVRContext gvrContext) {
    }

    /**
     * The TouchManager will not hold strong references to sceneObject and handler.
     *
     * @param sceneObject
     * @param handler
     */
    public void makeTouchable(GVRSceneObject sceneObject,
                              OnTouch handler) {
        makePickable(sceneObject);

        if (handler != null) {
            if (sceneObject.getRenderData() != null) {
                touchHandlers.put(sceneObject, new WeakReference<OnTouch>(handler));
            } else if (sceneObject.getChildrenCount() > 0) {
                for (GVRSceneObject child : sceneObject.getChildren()) {
                    touchHandlers.put(child, new WeakReference<OnTouch>(handler));
                }
            }
        }
    }

    // Click code
    public static final int LEFT_CLICK_EVENT = 1;
    public static final int BACK_KEY_EVENT = 2;

    private Set<TouchManagerFilter> mTouchFilters = new LinkedHashSet<>();
    private Set<TouchManagerFilter> mBackKeyFilters = new LinkedHashSet<>();

    public interface TouchManagerFilter extends Selector<GVRSceneObject> {
    }

    public void registerTouchFilter(final TouchManagerFilter filter) {
        mTouchFilters.add(filter);
    }

    public void unregisterTouchFilter(final TouchManagerFilter filter) {
        mTouchFilters.remove(filter);
    }

    public void registerBackKeyFilter(final TouchManagerFilter filter) {
        mBackKeyFilters.add(filter);
    }

    public void unregisterBackKeyFilter(final TouchManagerFilter filter) {
        mBackKeyFilters.remove(filter);
    }

    public boolean handleClick(List<GVRPickedObject> pickedObjectList, int event) {
        boolean isClickableItem = false;
        // Process result(s)
        for (GVRPickedObject pickedObject : pickedObjectList) {
            if (pickedObject == null) {
                Log.w(TAG, "handleClick(): got a null reference in the pickedObjectList");
                continue;
            }
            GVRSceneObject sceneObject = pickedObject.getHitObject();
            if (sceneObject == null) {
                Log.w(TAG, "handleClick(): got a null reference in the pickedObjectList");
                continue;
            }

            final float[] hit = pickedObject.getHitLocation();

            synchronized (mOnTouchInterceptors) {
                for (OnTouch interceptor : mOnTouchInterceptors) {
                    isClickableItem = event == LEFT_CLICK_EVENT ?
                            interceptor.touch(sceneObject, hit) :
                            interceptor.onBackKey(sceneObject, hit);
                }
            }

            if (!isClickableItem) {
                Set<TouchManagerFilter> filters = event == LEFT_CLICK_EVENT ?
                        mTouchFilters :
                        mBackKeyFilters;
                synchronized (mTouchFilters) {
                    for (TouchManagerFilter filter: filters) {
                        if (!filter.select(sceneObject)) {
                            continue;
                        }
                    }
                }

                final WeakReference<OnTouch> handler = touchHandlers.get(sceneObject);
                final OnTouch h = null != handler ? handler.get() : null;
                if (null != h) {
                    isClickableItem = event == LEFT_CLICK_EVENT ?
                            h.touch(sceneObject, hit) :
                            h.onBackKey(sceneObject, hit);

                    Log.d(TAG,
                            "handleClick(): handler for '%s' hit = %s handled event: %b",
                            sceneObject.getName(), hit, isClickableItem);

                } else {
                    touchHandlers.remove(sceneObject);
                }
            }

            if (isClickableItem) {
                break;
            }

            Log.e(TAG, "No handler or displayID for %s",
                    sceneObject.getName());
        }

        if (!isClickableItem) {
            Log.d(TAG, "No clickable items");
            isClickableItem = event == LEFT_CLICK_EVENT ?
                    takeDefaultLeftClickAction() : takeDefaultRightClickAction();
        }
        return isClickableItem;
    }

    public void setDefaultLeftClickAction(Runnable runnable) {
        defaultLeftClickAction = runnable;
    }

    public Runnable getDefaultLeftClickAction() {
        return defaultLeftClickAction;
    }

    public Runnable getDefaultRightClickAction() {
        return defaultRightClickAction;
    }

    public void setDefaultRightClickAction(Runnable runnable) {
        defaultRightClickAction = runnable;
    }

    public void setDefaultClickAction(Runnable runnable) {
        setDefaultLeftClickAction(runnable);
        setDefaultRightClickAction(runnable);
    }

    public boolean takeDefaultLeftClickAction() {
        if (defaultLeftClickAction != null) {
            defaultLeftClickAction.run();
            return true;
        }
        return false;
    }

    // The defaultBackAction defined in LauncherViewManager calls goBack() which
    // involves in mainScene changes sometimes, e.g., exiting from bigscreen to
    // appRing, so we have to make sure scene changes happen before the
    // following operations take places, rather than running it in an async way.
    // Otherwise rendering and displaying problem will occur in high frequency.
    public boolean takeDefaultRightClickAction() {
        if (defaultRightClickAction != null) {
            defaultRightClickAction.run();
            return true;
        }
        return false;
    }

    /**
     * Add a interceptor for {@linkplain OnTouch#touch(GVRSceneObject, float[])}
     * and {@linkplain OnTouch#onBackKey(GVRSceneObject, float[])}} to handle the touch event
     * before it will be passed to other scene objects
     *
     * @param interceptor
     *            An implementation of {@link OnTouch}.
     * @return {@code true} if the interceptor was successfully registered,
     *         {@code false} if the interceptor is already registered.
     */
    public boolean addOnTouchInterceptor(final OnTouch interceptor) {
        return mOnTouchInterceptors.add(interceptor);
    }

    /**
     * Remove a previously {@linkplain #addOnTouchInterceptor(OnTouch)
     * registered} {@linkplain OnTouch interceptor}.
     *
     * @param interceptor
     *            An implementation of {@link OnTouch}.
     * @return {@code true} if the interceptor was successfully unregistered,
     *         {@code false} if the interceptor was not previously
     *         registered with this object.
     */
    public boolean removeOnTouchInterceptor(final OnTouch interceptor) {
        return mOnTouchInterceptors.remove(interceptor);
    }


    public boolean removeHandlerFor(final GVRSceneObject sceneObject) {
        sceneObject.detachComponent(GVRCollider.getComponentType());
        return null != touchHandlers.remove(sceneObject);
    }

    public void makePickable(GVRSceneObject sceneObject) {
        try {
            if (sceneObject.getRenderData() != null) {
                GVRColliderGroup eyePointeeHolder = new GVRColliderGroup(
                        sceneObject.getGVRContext());
                GVRMeshCollider eyePointee = new GVRMeshCollider(sceneObject.getGVRContext(),
                        sceneObject.getRenderData().getMesh().getBoundingBox());
                eyePointeeHolder.addCollider(eyePointee);
                sceneObject.attachComponent(eyePointeeHolder);
            } else if (sceneObject.getChildrenCount() > 0) {
                for (GVRSceneObject child : sceneObject.getChildren()) {
                    makePickable(child);
                }
            }
        } catch (Exception e) {
            // Possible that some objects (X3D panel nodes) are without mesh
        }
    }

    public boolean isTouchable(GVRSceneObject sceneObject) {
        return touchHandlers.containsKey(sceneObject);
    }

    private final static String TAG = "TouchManager";
}
