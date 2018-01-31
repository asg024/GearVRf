package com.samsung.smcl.vr.widgets.widget;

import com.samsung.smcl.vr.widgets.log.Log;
import com.samsung.smcl.vr.widgets.main.WidgetLib;
import com.samsung.smcl.vr.widgets.thread.FPSCounter;
import com.samsung.smcl.vr.widgets.thread.MainThread;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRPicker.GVRPickedObject;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;

import java.lang.ref.WeakReference;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

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

    /**
     * Similar to
     * {@link TouchManager#addOnTouchInterceptor(TouchManager.OnTouch)}
     * TouchManager.setTouchInterceptor(OnTouch)}, instances of this interface
     * can be used to filter the delivery of focus events to
     * {@linkplain GVRSceneObject scene objects}.
     */
    public interface FocusInterceptor {
        /**
         * If the interceptor has completely handled the event and no further
         * processing is necessary -- including the normal focus event mechanism
         * -- return {@code true}. To allow the normal focus mechanism to be
         * executed, return {@code false}.
         * <p>
         * Generally this is useful for restricting focus events to a subset of
         * visible {@linkplain GVRSceneObject scene objects}: return
         * {@code false} for the objects you want to get normal focus processing
         * for, and {@code true} for the ones you don't.
         *
         * @param sceneObject
         *            The {@code GVRSceneObject} to filter
         * @return {@code True} if the focus event has been handled,
         *         {@code false} otherwise.
         */
        boolean onFocus(GVRSceneObject sceneObject);
    }

    interface LongFocusTimeout {
        long getLongFocusTimeout();
    }

    public interface FocusListener {
        void onFocus(boolean focused);
    }

    /**
     * Creates FocusManager
     */
    public FocusManager(GVRContext gvrContext) {
        init(gvrContext);
    }

    public boolean addFocusListener(FocusListener listener) {
        synchronized (mFocusListeners) {
            return mFocusListeners.add(listener);
        }
    }

    public boolean removeFocusListener(FocusListener listener) {
        synchronized (mFocusListeners) {
            return mFocusListeners.remove(listener);
        }
    }

    /**
     * The focus manager will not hold strong references to the sceneObject and the
     * focusable.
     * @param sceneObject
     * @param focusable
     */
    public void register(final GVRSceneObject sceneObject, final Focusable focusable) {
        Log.d(Log.SUBSYSTEM.FOCUS, TAG, "register sceneObject %s , focusable = %s",
                sceneObject.getName(), focusable);
        mFocusableMap.put(sceneObject, new WeakReference<>(focusable));
    }

    public void unregister(final GVRSceneObject sceneObject) {
        unregister(sceneObject, false);
    }

    void unregister(final GVRSceneObject sceneObject,
            final boolean softUnregister) {
        Log.d(Log.SUBSYSTEM.FOCUS, TAG, "unregister sceneObject %s", sceneObject.getName());
        final WeakReference<Focusable> focusableRef = mFocusableMap
                .remove(sceneObject);
        if (focusableRef != null) {
            final boolean allowRelease = !softUnregister
                    || !containsFocusable(focusableRef);
            WidgetLib.getMainThread().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (allowRelease && mCurrentFocus == focusableRef.get()) {
                        releaseCurrentFocus();
                    }
                }
            });
        }
    }

    private void init(GVRContext context) {
        if (mContext == null) {
            mContext = context;
            mContext.registerDrawFrameListener(mDrawFrameListener);
        }
    }

    public void clear() {
        if (mContext != null) {
            mContext.unregisterDrawFrameListener(mDrawFrameListener);
        }
    }

    private boolean containsFocusable(
            final WeakReference<Focusable> focusableRef) {
        final Focusable focusable = focusableRef.get();
        for (WeakReference<Focusable> ref : mFocusableMap.values()) {
            final Focusable f = ref.get();
            if (f != null && f == focusable) {
                return true;
            }
        }
        return false;
    }

    private void cancelLongFocusRunnable() {
        WidgetLib.getMainThread().removeCallbacks(mLongFocusRunnable);
    }

    private void postLongFocusRunnable(long timeout) {
        if (mCurrentFocus != null) {
            WidgetLib.getMainThread().runOnMainThreadDelayed(mLongFocusRunnable,
                                                            timeout);
        }
    }

    private volatile GVRPickedObject[] mPickedObjects;
    private GVRDrawFrameListener mDrawFrameListener = new GVRDrawFrameListener() {
        @Override
        public void onDrawFrame(float frameTime) {
            FPSCounter.timeCheck("onDrawFrame <START>: " + this + " frameTime = " + frameTime);

            final GVRScene mainScene = mContext.getMainScene();
            mPickedObjects = GVRPicker.pickObjects(mainScene, 0, 0, 0, 0, 0, -1.0f);

            WidgetLib.getMainThread().runOnMainThread(mFocusRunnable);
            FPSCounter.timeCheck("onDrawFrame <END>: " + this + " frameTime = " + frameTime);
        }
    };

    private FocusInterceptor focusInterceptor;

    public void setFocusInterceptor(FocusInterceptor interceptor) {
        focusInterceptor = interceptor;
    }

    private final Runnable mFocusRunnable = new Runnable() {
        @Override
        public void run() {
            final GVRPickedObject[] pickedObjectList = mPickedObjects;

            // release old focus
            if (pickedObjectList == null || 0 == pickedObjectList.length) {
                if (mCurrentFocus != null) {
                    Log.d(Log.SUBSYSTEM.FOCUS, TAG, "onDrawFrame(): empty/null pick list; releasing current focus (%s)",
                        mCurrentFocusName);
                }
                releaseCurrentFocus();
                return;
            }

            Focusable focusable = null;
            for (GVRPickedObject picked : pickedObjectList) {
                if (picked == null) {
                    Log.w(TAG, "onDrawFrame(): got a null reference in the pickedObjectList");
                    continue;
                }
                final GVRSceneObject quad = picked.getHitObject();
                if (quad != null) {
                    if (!compareCurrentFocusName(quad)) {
                        Log.d(Log.SUBSYSTEM.FOCUS, TAG, "onDrawFrame(): checking '%s' for focus",
                            quad.getName());
                    }
                    WeakReference<Focusable> ref = mFocusableMap.get(quad);
                    if (null != ref) {
                        focusable = ref.get();
                    } else {
                        mFocusableMap.remove(quad);
                        focusable = null;
                    }
                }

                // already has a focus - do nothing
                if (mCurrentFocus != null && mCurrentFocus == focusable) {
                    Log.d(Log.SUBSYSTEM.FOCUS, TAG, "onDrawFrame(): already has focus (%s)",
                        quad != null ? quad.getName() : "<null>");
                    break;
                }

                if (null == focusable || !focusable.isFocusEnabled()) {
                    continue;
                }

                releaseCurrentFocus();

                if (takeNewFocus(quad, focusable)) {
                    mCurrentFocusName = quad.getName();
                    Log.d(Log.SUBSYSTEM.FOCUS, TAG, "onDrawFrame(): '%s' took focus", mCurrentFocusName);
                    break;
                }
            }

            if (mCurrentFocus != null && focusable != mCurrentFocus) {
                Log.d(Log.SUBSYSTEM.FOCUS, TAG, "onDrawFrame(): no eligible focusable found! (%s)", mCurrentFocusName);
                releaseCurrentFocus();
            }
        }

        private boolean compareCurrentFocusName(final GVRSceneObject quad) {
            final String quadName = quad.getName();
            return (mCurrentFocusName == null && quadName == null)
                    || (mCurrentFocusName != null && mCurrentFocusName
                            .equalsIgnoreCase(quadName));
        }
    };

    private boolean releaseCurrentFocus() {
        boolean ret = true;
        if (mCurrentFocus != null) {
            Log.d(Log.SUBSYSTEM.FOCUS, TAG, "releaseCurrentFocus(): releasing focus for '%s'",
                    mCurrentFocusName);
            cancelLongFocusRunnable();
            ret = mCurrentFocus.onFocus(false);
            mCurrentFocus = null;
            mCurrentFocusName = null;
            notifyFocusListeners(false);
        }
        return ret;
    }

    private boolean takeNewFocus(final GVRSceneObject quad, final Focusable newFocusable) {
        if (newFocusable != null &&
                newFocusable.isFocusEnabled()) {

            if (focusInterceptor != null && focusInterceptor.onFocus(quad)) {
                return false;
            }

            if (newFocusable.onFocus(true)) {
                mCurrentFocus = newFocusable;
                final long longFocusTimeout;
                if (newFocusable instanceof LongFocusTimeout) {
                    longFocusTimeout = ((LongFocusTimeout) newFocusable)
                            .getLongFocusTimeout();
                } else {
                    longFocusTimeout = LONG_FOCUS_TIMEOUT;
                }
                postLongFocusRunnable(longFocusTimeout);

                notifyFocusListeners(true);
                return true;
            }
        }
        return false;
    }

    private void notifyFocusListeners(boolean focused) {
        synchronized (mFocusListeners) {
            for (FocusListener listener : mFocusListeners) {
                    try {
                        listener.onFocus(focused);
                    } catch (Throwable t) {
                        Log.e(TAG, t, "");
                    }
                }
        }
    }

    private GVRContext mContext;
    private Focusable mCurrentFocus = null;
    private String mCurrentFocusName = "";
    private Map<GVRSceneObject, WeakReference<Focusable>> mFocusableMap = new WeakHashMap<>();
    private Set<FocusListener> mFocusListeners = new LinkedHashSet<>();

    private final Runnable mLongFocusRunnable = new Runnable() {

        @Override
        public void run() {
            if (mCurrentFocus != null) {
                mCurrentFocus.onLongFocus();
            }
        }
    };

    static final int LONG_FOCUS_TIMEOUT = 5000;

    @SuppressWarnings("unused")
    private static final String TAG = FocusManager.class.getSimpleName();
}
