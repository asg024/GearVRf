package com.samsung.smcl.vr.widgets;

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

import com.samsung.smcl.vr.gvrf_launcher.MainThread;
import com.samsung.smcl.utility.Log;

/**
 * A class for tracking line-of-sight focus for {@link Widget} instances. In
 * addition to notifying gain and loss of focus, also manages "long focus".
 * "Long focus" is similar to "long press" and occurs when an object has held
 * line-of-sight focus for {@link #LONG_FOCUS_TIMEOUT} milliseconds or longer.
 * The long focus timeout is reset each time an object gains focus and is
 * stopped entirely when no object has line-of-sight focus.
 */
public class FocusManager {
    public interface Focusable {
        boolean isFocusEnabled();
        boolean onFocus(boolean focused);
        void onLongFocus();
    }

    static void init(GVRContext context) {
        synchronized (FocusManager.class) {
            if (sInstance == null) {
                sInstance = new FocusManager(context);
            }
        }
    }

    public static FocusManager getInstance() {
        return sInstance;
    }

    public void register(final GVRSceneObject sceneObject, final Focusable focusable) {
        Log.d(TAG, "register sceneObject %s , focusable = %s", sceneObject, focusable);
        mFocusableMap.put(sceneObject, focusable);
    }

    public void unregister(final GVRSceneObject sceneObject) {
        Log.d(TAG, "unregister sceneObject %s", sceneObject);
        mFocusableMap.remove(sceneObject);
    }

    private FocusManager(GVRContext context) {
        mContext = context;
        context.registerDrawFrameListener(mDrawFrameListener);
    }

    private void cancelLongFocusRunnable() {
        if (mLongFocusEvent != null) {
            mLongFocusEvent.cancel();
            mLongFocusEvent = null;
        }
    }

    private void postLongFocusRunnable() {
        if (mCurrentFocus != null) {
            LongFocusRunnable r = new LongFocusRunnable(mCurrentFocus);
            mLongFocusEvent = GVRPeriodicEngine.getInstance(mContext)
                    .runAfter(r, LONG_FOCUS_TIMEOUT / 1000.0f);
        }
    }

    private GVRDrawFrameListener mDrawFrameListener = new GVRDrawFrameListener() {
        @Override
        public void onDrawFrame(float frameTime) {
            MainThread.get(mContext).runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    final GVRScene mainScene = mContext.getMainScene();
                    final List<GVRPickedObject> pickedObjectList = GVRPicker
                            .findObjects(mainScene, 0, 0, 0, 0, 0, -1.0f);

                    // release old focus
                    if (pickedObjectList == null ||
                            pickedObjectList.isEmpty()) {
                        releaseCurrentFocus();
                        return;
                    }

                    for (GVRPickedObject picked : pickedObjectList) {
                        final GVRSceneObject quad = picked.getHitObject();
                        Focusable focusable = null;
                        if (quad != null) {
                            focusable = mFocusableMap.get(quad);
                        }

                        // already has a focus - do nothing
                        if (mCurrentFocus != null &&
                            mCurrentFocus == focusable) {
                            break;
                        }

                        releaseCurrentFocus();

                        if (takeNewFocus(focusable)) {
                            break;
                        }
                    }
                }
            });
        }
    };


    private boolean releaseCurrentFocus() {
        boolean ret = true;
        if (mCurrentFocus != null) {
            cancelLongFocusRunnable();
            ret = mCurrentFocus.onFocus(false);
            mCurrentFocus = null;
        }
        return ret;
    }

    private boolean takeNewFocus(final Focusable newFocusable) {
        boolean ret = false;
        if (newFocusable != null &&
                newFocusable.isFocusEnabled()) {

            ret = newFocusable.onFocus(true);
            if (ret) {
                mCurrentFocus = newFocusable;
                postLongFocusRunnable();
            }
        }
        return ret;
    }

    private final GVRContext mContext;
    private Focusable mCurrentFocus = null;
    private Map<GVRSceneObject, Focusable> mFocusableMap = new WeakHashMap<GVRSceneObject, Focusable>();

    static class LongFocusRunnable implements Runnable {
        Focusable mFocusable;

        LongFocusRunnable(Focusable focusable) {
            mFocusable = focusable;
        }

        @Override
        public void run() {
            if (mFocusable != null) {
                mFocusable.onLongFocus();
            }
        }
    }

    private GVRPeriodicEngine.PeriodicEvent mLongFocusEvent;

    private static FocusManager sInstance;
    static final int LONG_FOCUS_TIMEOUT = 5000;
    @SuppressWarnings("unused")
    private static final String TAG = FocusManager.class.getSimpleName();
}
