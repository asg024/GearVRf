package com.samsung.smcl.vr.widgets;

import android.app.Activity;

import com.samsung.smcl.utility.Log;
import com.samsung.smcl.vr.gvrf_launcher.FPSCounter;
import com.samsung.smcl.vr.gvrf_launcher.Holder;
import com.samsung.smcl.vr.gvrf_launcher.HolderHelper;
import com.samsung.smcl.vr.gvrf_launcher.MainThread;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRPicker.GVRPickedObject;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;

import java.lang.ref.WeakReference;
import java.util.Map;
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
     * {@link com.samsung.smcl.vr.gvrf_launcher.TouchManager#setTouchInterceptor(com.samsung.smcl.vr.gvrf_launcher.TouchManager.OnTouch)
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

    static public FocusManager get(Activity activity) {
        return ((Holder) activity).get(FocusManager.class);
    }

    static public FocusManager get(GVRContext gvrContext) {
        FocusManager focusManager = null;
        if (gvrContext != null) {
            Activity activity = gvrContext.getActivity();
            focusManager = get(activity);
        }
        return focusManager;
    }

    /**
     * Creates FocusManager
     * An instance of {@link Holder} must be supplied and can only be associated
     * with one {@link FocusManager}. If the supplied {@code Holder} instance has
     * already been initialized, an {@link IllegalArgumentException} will be
     * thrown.
     *
     * @param holder
     *            An {@link Activity} that implements {@link Holder}.
     * @throws IllegalArgumentException
     *             if {@code holder} is {@code null} or is already holding
     *             another instance of {@code FocusManager}.
     */
    public <T extends Activity & Holder> FocusManager(T holder) {
        HolderHelper.register(holder, this);
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
            MainThread.get(mContext).runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (allowRelease && mCurrentFocus == focusableRef.get()) {
                        releaseCurrentFocus();
                    }
                }
            });
        }
    }

    void init(GVRContext context) {
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
        MainThread.get(mContext).removeCallbacks(mLongFocusRunnable);
    }

    private void postLongFocusRunnable(long timeout) {
        if (mCurrentFocus != null) {
            MainThread.get(mContext).runOnMainThreadDelayed(mLongFocusRunnable,
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

            MainThread.get(mContext).runOnMainThread(mFocusRunnable);
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

                return true;
            }
        }
        return false;
    }

    private GVRContext mContext;
    private Focusable mCurrentFocus = null;
    private String mCurrentFocusName = "";
    private Map<GVRSceneObject, WeakReference<Focusable>> mFocusableMap = new WeakHashMap<GVRSceneObject, WeakReference<Focusable>>();

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
