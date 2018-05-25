package com.samsung.smcl.vr.widgetlib.main;

import com.samsung.smcl.vr.widgetlib.content_scene.ContentSceneController;
import com.samsung.smcl.vr.widgetlib.thread.MainThread;

import com.samsung.smcl.vr.widgetlib.widget.FocusManager;
import com.samsung.smcl.vr.widgetlib.widget.TouchManager;
import com.samsung.smcl.vr.widgetlib.widget.Widget;
import com.samsung.smcl.vr.widgetlib.widget.animation.SimpleAnimationTracker;
import com.samsung.smcl.vr.widgetlib.widget.properties.PropertyManager;
import com.samsung.smcl.vr.widgetlib.widget.properties.TypefaceManager;

import org.gearvrf.GVRContext;
import org.json.JSONException;

import java.lang.ref.WeakReference;

/**
 * Created by svetlanag on 1/29/18.
 */

public class WidgetLib {

    private static WeakReference<WidgetLib> mInstance;
    private final FocusManager mFocusManager;
    private final TouchManager mTouchManager;
    private final ContentSceneController mContentSceneController;
    private final TypefaceManager mTypefaceManager;
    private final SimpleAnimationTracker mSimpleAnimationTracker;
    private final MainThread mMainThread;
    private final PropertyManager mPropertyManager;
    private final CommandBuffer mCommandBuffer;

    /**
     * Initialize an instance of Widget Lib. It has to be done before any usage of library.
     * The application needs to hold onto the returned WidgetLib reference for as long as the
     * library is going to be used.
     * @param gvrContext A valid {@link GVRContext} instance
     * @param customPropertiesAsset An optional asset JSON file containing custom and overridden
     *                              properties for the application
     * @return Instance of Widget library
     * @throws InterruptedException
     * @throws JSONException
     * @throws NoSuchMethodException
     */
    public static WidgetLib init(GVRContext gvrContext, String customPropertiesAsset)
            throws InterruptedException, JSONException, NoSuchMethodException {
        if (mInstance == null) {
            // Constructor sets mInstance to ensure the initialization order
            new WidgetLib(gvrContext, customPropertiesAsset);
        }
        return mInstance.get();
    }

    public static void destroy() {
        if (mInstance != null) {
            getFocusManager().clear();
            getMainThread().quit();
        }
        mInstance = null;
    }

    /**
     * Quick check if the library has been initialized already, see {@link #init}
     * @return true if the library has been initialized and ready to use, otherwise - false
     */
    public static boolean isInitialized() {
        return mInstance != null;
    }

    /**
     * Get instance of {@link FocusManager}. If the library is not initialized {@link IllegalStateException}
     * will be thrown
     * @return The instance of {@link FocusManager}
     */
    public static FocusManager getFocusManager() {
        return get().mFocusManager;
    }

    /**
     * Get instance of {@link TouchManager}. If the library is not initialized {@link IllegalStateException}
     * will be thrown
     * @return The instance of {@link TouchManager}
     */
    public static TouchManager getTouchManager() {
        return get().mTouchManager;
    }

    /**
     * Get instance of {@link ContentSceneController}. If the library is not initialized
     * {@link IllegalStateException} will be thrown
     * @return The instance of {@link ContentSceneController}
     */
    public static ContentSceneController getContentSceneController() {
        return get().mContentSceneController;
    }

    /**
     * Get instance of {@link TypefaceManager}. If the library is not initialized
     * {@link IllegalStateException} will be thrown
     * @return The instance of {@link TypefaceManager}
     */
    public static TypefaceManager getTypefaceManager() {
        return get().mTypefaceManager;
    }

    /**
     * Get instance of {@link SimpleAnimationTracker}. If the library is not initialized
     * {@link IllegalStateException} will be thrown
     * @return The instance of {@link SimpleAnimationTracker}
     */
    public static SimpleAnimationTracker getSimpleAnimationTracker() {
        return get().mSimpleAnimationTracker;
    }

    /**
     * Get instance of {@link MainThread}. If the library is not initialized
     * {@link IllegalStateException} will be thrown
     * @return The instance of {@link MainThread}
     */
    public static MainThread getMainThread() {
        return get().mMainThread;
    }

    /**
     * Get instance of {@link PropertyManager}. If the library is not initialized
     * {@link IllegalStateException} will be thrown
     * @return The instance of {@link PropertyManager}
     */
    public static PropertyManager getPropertyManager() {
        return get().mPropertyManager;
    }

    public static CommandBuffer getCommandBuffer() {
        return get().mCommandBuffer;
    }

    private WidgetLib(GVRContext gvrContext, String customPropertiesAsset)
            throws InterruptedException, JSONException, NoSuchMethodException {
        mInstance = new WeakReference<>(this);

        mMainThread = new MainThread(gvrContext);
        mTypefaceManager = new TypefaceManager(gvrContext);
        mSimpleAnimationTracker = new SimpleAnimationTracker(gvrContext);
        mPropertyManager = new PropertyManager(gvrContext.getContext(), "default_metadata.json",
                customPropertiesAsset);
        mCommandBuffer = new CommandBuffer(gvrContext);

        mFocusManager = new FocusManager(gvrContext);
        mTouchManager = new TouchManager(gvrContext);
        mContentSceneController = new ContentSceneController(gvrContext);
        Widget.init(gvrContext);
    }

    private static WidgetLib get() {
        if (mInstance == null) {
            throw new IllegalStateException("Widget library is not initialized!");
        }
        return mInstance.get();
    }
}