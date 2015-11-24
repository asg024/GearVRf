package com.samsung.smcl.vr.widgets;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRPicker.GVRPickedObject;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.periodic.GVRPeriodicEngine;

import com.samsung.smcl.utility.Log;
import com.samsung.smcl.vr.gvrf_launcher.MainThread;

/**
 * A class for tracking line-of-sight focus for {@link Widget} instances. In
 * addition to notifying gain and loss of focus, also manages "long focus".
 * "Long focus" is similar to "long press" and occurs when an object has held
 * line-of-sight focus for {@link #LONG_FOCUS_TIMEOUT} milliseconds or longer.
 * The long focus timeout is reset each time an object gains focus and is
 * stopped entirely when no object has line-of-sight focus.
 */
class FocusManager {

    static void init(GVRContext context) {
        synchronized (FocusManager.class) {
            if (sInstance == null) {
                sInstance = new FocusManager(context);
            }
        }
    }

    static FocusManager getInstance() {
        return sInstance;
    }

    void register(final Widget widget) {
        mWidgetMap.put(widget.getSceneObject(), widget);
    }

    void unregister(final Widget widget) {
        mWidgetMap.remove(widget.getSceneObject());
    }

    private FocusManager(GVRContext context) {
        mContext = context;
        context.registerDrawFrameListener(mDrawFrameListener);
    }

    private Widget getPickedFocusable() {
        final GVRScene mainScene = mContext.getMainScene();
        final List<GVRPickedObject> pickedObjectList = GVRPicker
                .findObjects(mainScene, 0, 0, 0, 0, 0, -1.0f);
        for (GVRPickedObject picked : pickedObjectList) {
            final GVRSceneObject quad = picked.getHitObject();
            final Widget widget = mWidgetMap.get(quad);
            if (widget != null && widget.isFocusEnabled()) {
                return widget;
            }
        }

        return null;
    }

    private void cancelLongFocusRunnable() {
        if (mLongFocusEvent != null) {
            mLongFocusEvent.cancel();
        }
    }

    private void postLongFocusRunnable() {
        mLongFocusEvent = GVRPeriodicEngine.getInstance(mContext)
                .runAfter(mLongFocusRunnable,
                          (long) LONG_FOCUS_TIMEOUT / 1000.0f);
    }

    private GVRDrawFrameListener mDrawFrameListener = new GVRDrawFrameListener() {
        @Override
        public void onDrawFrame(float frameTime) {
//            Log.d(TAG, "onDrawFrame(): called at %05.2f", frameTime);
            Widget quad = getPickedFocusable();
            Widget focus = mFocusRef.get();
//            Log.d(TAG, "onDrawFrame(): quad: %s, focus: %s", quad, focus);
            if (quad != focus) {
//                Log.d(TAG, "onDrawFrame(): focus changed");
                final Widget oldFocus = focus;
                mFocusRef.clear();
                cancelLongFocusRunnable();
                final Widget newFocus;
                if (quad != null) {
                    newFocus = (Widget) quad;
                    mFocusRef = new WeakReference<Widget>(focus);
                    postLongFocusRunnable();
                } else {
                    newFocus = null;
                }

                MainThread.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (oldFocus != null) {
                            oldFocus.doOnFocus(false);
                        }
                        if (newFocus != null) {
                            newFocus.doOnFocus(true);
                        }
                    }
                });
            }
        }
    };

    private final GVRContext mContext;
    private WeakReference<Widget> mFocusRef = new WeakReference<Widget>(null);
    private Map<GVRSceneObject, Widget> mWidgetMap = new WeakHashMap<GVRSceneObject, Widget>();
    private Runnable mLongFocusRunnable = new Runnable() {
        @Override
        public void run() {
            final Widget focus = mFocusRef.get();
            if (focus != null) {
                focus.doOnLongFocus();
            }
        }
    };
    private GVRPeriodicEngine.PeriodicEvent mLongFocusEvent;

    private static FocusManager sInstance;
    static final int LONG_FOCUS_TIMEOUT = 5000;
    private static final String TAG = FocusManager.class.getSimpleName();
}