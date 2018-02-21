package com.samsung.smcl.vr.widgetlib.main;

import com.samsung.smcl.vr.widgetlib.content_scene.ContentSceneController;
import com.samsung.smcl.vr.widgetlib.thread.MainThread;

import com.samsung.smcl.vr.widgetlib.widget.FocusManager;
import com.samsung.smcl.vr.widgetlib.widget.TouchManager;
import com.samsung.smcl.vr.widgetlib.widget.Widget;
import com.samsung.smcl.vr.widgetlib.widget.animation.SimpleAnimationTracker;
import com.samsung.smcl.vr.widgetlib.widget.properties.PropertyManager;

import org.gearvrf.GVRContext;
import org.json.JSONException;

import java.lang.ref.WeakReference;

/**
 * Created by svetlanag on 1/29/18.
 */

public class WidgetLib {

    private static WeakReference<WidgetLib> mInstance;
    private final GVRContext mGvrContext;
    private final FocusManager mFocusManager;
    private final TouchManager mTouchManager;
    private final ContentSceneController mContentSceneController;
    private final TypefaceManager mTypefaceManager;
    private final SimpleAnimationTracker mSimpleAnimationTracker;
    private final MainThread mMainThread;
    private final PropertyManager mPropertyManager;

    private WidgetLib(GVRContext gvrContext, String customPropertiesAsset)
            throws InterruptedException, JSONException, NoSuchMethodException {
        mGvrContext = gvrContext;
        mFocusManager = new FocusManager(gvrContext);
        mTouchManager = new TouchManager(gvrContext);
        mContentSceneController = new ContentSceneController(gvrContext);
        mTypefaceManager = new TypefaceManager(gvrContext);
        mSimpleAnimationTracker = new SimpleAnimationTracker(gvrContext);
        mMainThread = new MainThread(gvrContext);
        mPropertyManager = new PropertyManager(gvrContext.getContext(), "default_metadata.json",
                customPropertiesAsset);

        Widget.init(mGvrContext);
    }

    public static WidgetLib init(GVRContext gvrContext, String customPropertiesAsset) throws InterruptedException, JSONException,
            NoSuchMethodException {
        if (mInstance == null) {
            mInstance = new WeakReference<>(new WidgetLib(gvrContext, customPropertiesAsset));
        }
        return mInstance.get();
    }

    public static boolean isInitialized() {
        return mInstance != null;
    }

    private static WidgetLib get() {
        if (mInstance == null) {
            throw new IllegalStateException("Widget library is not initialized!");
        }
        return mInstance.get();
    }

    public static FocusManager getFocusManager() {
        return get().mFocusManager;

    }

    public static TouchManager getTouchManager() {
        return get().mTouchManager;
    }

    public static ContentSceneController getContentSceneController() {
        return get().mContentSceneController;
    }

    public static TypefaceManager getTypefaceManager() {
        return get().mTypefaceManager;
    }

    public static SimpleAnimationTracker getSimpleAnimationTracker() {
        return get().mSimpleAnimationTracker;
    }

    public static MainThread getMainThread() {
        return get().mMainThread;
    }

    public static PropertyManager getPropertyManager() {
        return get().mPropertyManager;
    }
}