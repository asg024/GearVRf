package com.samsung.smcl.vr.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.opengl.GLES30;
import android.support.annotation.NonNull;
import android.view.MotionEvent;

import com.samsung.smcl.utility.Log;
import com.samsung.smcl.utility.UnmodifiableJSONObject;
import com.samsung.smcl.utility.Utility;
import com.samsung.smcl.vr.gvrf_launcher.FPSCounter;
import com.samsung.smcl.vr.gvrf_launcher.LauncherViewManager.OnInitListener;
import com.samsung.smcl.vr.gvrf_launcher.MainScene;
import com.samsung.smcl.vr.gvrf_launcher.R;
import com.samsung.smcl.vr.gvrf_launcher.TouchManager;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRImportSettings;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.joml.Vector3f;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import static com.samsung.smcl.utility.Exceptions.RuntimeAssertion;
import static com.samsung.smcl.vr.widgets.JSONHelpers.*;

public class Widget  implements Layout.WidgetContainer {

    private static final String MATERIAL_DIFFUSE_TEXTURE = "diffuseTexture";

    /**
     * Call to initialize the Widget infrastructure. Parses {@code objects.json}
     * to load metadata for {@code Widgets}, as well as animation and material
     * specs.
     *
     * @param context
     *            A valid Android {@link Context}.
     * @throws JSONException
     *             if the {@code objects.json} file is invalid JSON
     * @throws NoSuchMethodException
     *             if a constructor can't be found for an animation type
     *             specified in {@code objects.json}.
     */
    static public void init(Context context) throws JSONException,
            NoSuchMethodException {
        PropertyManager.init(context);

        loadAnimations(context);
    }

    /**
     * Register this with LauncherViewManager. An alternative would be to have
     * {@link #init(Context) init()} do this work and just call it directly from
     * LauncherViewManager.onInit().
     */
    static public final OnInitListener onInitListener = new OnInitListener() {
        @Override
        public void onInit(GVRContext gvrContext, MainScene scene) {
            FocusManager.get(gvrContext).init(gvrContext);
            gvrContext.runOnGlThread(new Runnable() {
                @Override
                public void run() {
                    sGLThread = new WeakReference<>(Thread.currentThread());
                }
            });
            GVRAssetLoader assetLoader = new GVRAssetLoader(gvrContext);
            sDefaultTexture = assetLoader.loadTexture(new GVRAndroidResource(
                    gvrContext, R.raw.default_bkgd));
            Log.d(TAG, "onInit(): default texture: %s", sDefaultTexture);
        }

        @Override
        public void onPostInit() {

        }
    };

    /**
     * @return The time, in milliseconds, that a widget must have continuous
     *         focus before an {@link OnFocusListener#onLongFocus()
     *         onLongFocus()} event is sent.
     */
    static public long getLongFocusTime() {
        return FocusManager.LONG_FOCUS_TIMEOUT;
    }

    static public GVRTexture getDefaultTexture() {
        return sDefaultTexture;
    }

    /**
     * Implement and {@link Widget#addFocusListener(OnFocusListener) register}
     * this interface to listen for focus changes on widgets.
     */
    public interface OnFocusListener {
        /**
         * Called when a widget gains or loses focus.
         *
         * @param focused
         *            {@code True} is the widget has gained focus; {@code false}
         *            if the widget has lost focus.
         * @return {@code True} to indicate that no further processing of the
         *         focus change should take place; {@code false} to allow
         *         further processing.
         */
        boolean onFocus(Widget widget, boolean focused);

        /**
         * Called when a widget has had focus for more than
         * {@link Widget#getLongFocusTime()} milliseconds.
         *
         * @return {@code True} to indicate that no further processing of the
         *         event should take place; {@code false} to allow further
         *         processing.
         */
        boolean onLongFocus(Widget widget);
    }

    /**
     * Implement and {@link Widget#addBackKeyListener(OnBackKeyListener)
     * register} this interface to listen for back key events on widgets.
     */
    public interface OnBackKeyListener {
        /**
         * Called when widget is target of back key event.
         *
         * @param widget
         *            {@link Widget} target by back key event.
         * @return {@code True} to indicate that no further processing of the
         *         touch event should take place; {@code false} to allow further
         *         processing.
         */
        boolean onBackKey(Widget widget);
    }

    /**
     * Implement and {@link Widget#addTouchListener(OnTouchListener) register}
     * this interface to listen for touch events on widgets.
     */
    public interface OnTouchListener {
        /**
         * Called when a widget is touched (tapped).
         *
         * @param widget
         *            {@link Widget} target by touch event.
         * @param coords GVRF raw coordinates

         * @return {@code True} to indicate that no further processing of the
         *         touch event should take place; {@code false} to allow further
         *         processing.
         */
        boolean onTouch(Widget widget, final float[] coords);
    }

    /**
     * Options for {@link Widget#setVisibility(Visibility)}.
     */
    public enum Visibility {
        /** Show the object and include in layout calculations. */
        VISIBLE,
        /** Hide the object, but include in layout calculations. */
        HIDDEN,
        /** Hide the object, but extract the size for layout calculations. */
        PLACEHOLDER,
        /** Hide the object, and do not include in layout calculations. */
        GONE
    }

    /**
     * Options for {@link Widget#setViewPortVisibility(ViewPortVisibility)}.
     */
    public enum ViewPortVisibility {
        /** The object is fully visible in layout ViewPort */
        FULLY_VISIBLE,
        /** The object is partially visible in layout ViewPort */
        PARTIALLY_VISIBLE,
        /** The object is not visible in layout ViewPort */
        INVISIBLE
    }

    /**
     * Construct a {@link Widget} whose initial properties will be entirely determined by metadata.
     *
     * @param context
     *            The current {@link GVRContext}.
     */
    protected Widget(final GVRContext context) {
        this(context, new JSONObject(), false);
    }

    /**
     * Construct a wrapper for an existing {@link GVRSceneObject}.
     *
     * @param context
     *            The current {@link GVRContext}.
     * @param sceneObject
     *            The {@link GVRSceneObject} to wrap.
     */
    protected Widget(final GVRContext context, final GVRSceneObject sceneObject) {
        this(context, packageSceneObject(sceneObject), false);
    }

    private static JSONObject packageSceneObject(GVRSceneObject sceneObject) {
        final JSONObject json = new JSONObject();
        put(json, Properties.scene_object, sceneObject);
        return json;
    }

    /**
     * A constructor for wrapping existing {@link GVRSceneObject} instances.
     * Deriving classes should override and do whatever processing is
     * appropriate.
     *
     * @param context
     *            The current {@link GVRContext}
     * @param sceneObject
     *            The {@link GVRSceneObject} to wrap.
     * @param attributes
     *            TODO
     * @throws InstantiationException
     */
    public Widget(final GVRContext context, final GVRSceneObject sceneObject,
            NodeEntry attributes) throws InstantiationException {
        this(context, packageSceneObjectWithAttributes(sceneObject, attributes), false);
    }

    private static JSONObject packageSceneObjectWithAttributes(GVRSceneObject sceneObject,
                                                               NodeEntry attributes)
            throws InstantiationException {
        final JSONObject json;

        if (attributes != null) {
            json = attributes.toJSON();
            put(json, Properties.preapply_attribs, true);
        } else {
            json = new JSONObject();
        }

        put(json, Properties.scene_object, sceneObject);

        return json;
    }

    // private static final String pattern = Widget.class.getSimpleName()
    // + "name : %s size = (%f, %f, %f) \n"
    // + "touchable = %b focus_enabled = %b Visible = %s selected = %b";
    //
    // public String toString() {
    // return String.format(pattern, getName(), getWidth(), getHeight(),
    // getDepth(),
    // mIsTouchable, mFocusEnabled, mVisibility, mIsSelected);
    // }

    private static GVRModelSceneObject loadSceneObjectFromModel(GVRContext context, String modelFile) {
        final GVRAssetLoader loader = context.getAssetLoader();
        final EnumSet<GVRImportSettings> settings = GVRImportSettings.getRecommendedSettings();
        try {
            Log.d(Log.SUBSYSTEM.WIDGET, TAG, "loadSceneObjectFromModel(): attemping to load '%s'", modelFile);
            return loader.loadModel(modelFile, settings, true, null);
        } catch (IOException e) {
            Log.e(TAG, e, "loadSceneObjectFromModel(): failed to load model for Widget: %s", modelFile);
            throw new RuntimeException("Failed to load Widget model from " + modelFile, e);
        }
    }

    public Widget(final GVRContext context, final float width, final float height) {
        this(context, makeQuad(context, width, height));
    }

    private static final GVRSceneObject makeQuad(GVRContext context, final float width,
                                                 final float height) {
        GVRSceneObject sceneObject = new GVRSceneObject(context, width, height);
        setupDefaultMaterial(context, sceneObject);
        return sceneObject;
    }

    private static void setupDefaultMaterial(GVRContext context, GVRSceneObject sceneObject) {
        GVRRenderData renderData = sceneObject.getRenderData();
        if (renderData != null) {
            GVRMaterial material = new GVRMaterial(context,
                    GVRShaderType.Texture.ID);
            material.setMainTexture(sDefaultTexture);
            renderData.setMaterial(material);
        }
    }

    /**
     * @return The Android {@link Context} this {@code Widget} is in.
     */
    public Context getContext() {
        return getGVRContext().getContext();
    }

    /**
     * Set whether or not the object can receive line-of-sight focus. If
     * enabled, the object will receive {@link #onFocus(boolean)} and
     * {@link #onLongFocus()} notifications and
     * {@linkplain #addFocusListener(OnFocusListener) registered}
     * {@linkplain OnFocusListener listeners} can also receive those
     * notifications.
     * <p>
     * Focus is enabled by default.
     *
     * @param enabled
     *            {@code True} to enable line-of-sight focus, {@code false} to
     *            disable.
     */
    public void setFocusEnabled(boolean enabled) {
        if (mFocusEnabled != enabled) {
            mFocusEnabled = enabled;
            registerPickable();
        }
    }

    /**
     * @return Whether line-of-sight focus is enabled for this object.
     */
    public boolean isFocusEnabled() {
        return mFocusEnabled;
    }

    /**
     * @return Whether the object currently has line-of-sight focus.
     */
    public boolean isFocused() {
        return mIsFocused;
    }

    /**
     * Determines whether this {@link Widget} is currently handling focus events
     * for the specified {@link GVRSceneObject}. This will be true if either
     * <p>
     * <ul>
     * <li>
     * The {@code GVRSceneObject} is wrapped by this {@code Widget} <em>and</em>
     * this {@code Widget} is not following its parent's focus</li>
     * <li>The {@code GVRSceneObject} is wrapped by a child of this
     * {@code Widget} and the child <em>is</em> following this {@code Widget's}
     * focus</li>
     * </ul>
     *
     * @param sceneObject
     *            The {@code GVRSceneObject} to check
     * @return {@code True} if this {@code Widget} handles focus events for the
     *         {@code GVRSceneObject}, {@code false} if it doesn't.
     */
    public boolean handlesFocusFor(final GVRSceneObject sceneObject) {
        return handlesEventFor(sceneObject, mHandlesFocusEvent);
    }

    /**
     * {@link Widget} version of {@link #handlesFocusFor(GVRSceneObject)}.
     *
     * @param widget
     *            The {@code Widget} to check
     */
    public boolean handlesFocusFor(final Widget widget) {
        return handlesFocusFor(widget.getSceneObject());
    }

    /**
     * @return The timeout, in milliseconds, before a continuous focus state
     *         triggers an {@link #onLongFocus()} event. By default this is
     *         {@link FocusManager#LONG_FOCUS_TIMEOUT}.
     */
    public long getLongFocusTimeout() {
        return mLongFocusTimeout;
    }

    /**
     * Set the timeout, in milliseconds, before a continuous focus state trigger
     * an {@link #onLongFocus()} event.
     *
     * @param longFocusTimeout
     *            Timeout value, in milliseconds.
     */
    public void setLongFocusTimeout(long longFocusTimeout) {
        mLongFocusTimeout = longFocusTimeout;
    }

    /**
     * Add a listener for {@linkplain OnFocusListener#onFocus(boolean) focus}
     * and {@linkplain OnFocusListener#onLongFocus() long focus} notifications
     * for this object.
     *
     * @param listener
     *            An implementation of {@link OnFocusListener}.
     * @return {@code True} if the listener was successfully registered,
     *         {@code false} if the listener is already registered.
     */
    public boolean addFocusListener(final OnFocusListener listener) {
        synchronized (mFocusListeners) {
            return mFocusListeners.add(listener);
        }
    }

    /**
     * Remove a previously {@linkplain #addFocusListener(OnFocusListener)
     * registered} focus notification {@linkplain OnFocusListener listener}.
     *
     * @param listener
     *            An implementation of {@link OnFocusListener}
     * @return {@code True} if the listener was successfully unregistered,
     *         {@code false} if the specified listener was not previously
     *         registered with this object.
     */
    public boolean removeFocusListener(final OnFocusListener listener) {
        synchronized (mFocusListeners) {
            return mFocusListeners.remove(listener);
        }
    }

    /**
     * @return Whether children of this {@link Widget} will be grouped with
     *         their parent for purposes of managing focus.
     * @see #setChildrenFollowFocus(boolean)
     */
    public boolean getChildrenFollowFocus() {
        return mChildrenFollowFocus;
    }

    /**
     * When children follow the focus of their parent {@link Widget}, the parent
     * and children are treated as a single entity for focus management
     * purposes. When any of them would normally gain focus, they all gain
     * focus, and the group only loses focus when none of them would normally
     * have focus.
     * <p>
     * The focus hook methods -- {@link #onFocus(boolean)} and
     * {@link #onLongFocus()} -- are invoked as usual, but en masse for the
     * parent and its children. There is one caveat: only the parent's
     * {@code onFocus()} method determines whether focus is accepted or
     * rejected. If the parent rejects focus, it is rejected for the entire
     * group and none of the children will have {@code onFocus()} called on
     * them.
     * <p>
     * Children can {@linkplain #setFocusEnabled(boolean) enable and disable}
     * focus individually; if a child disables focus, it will not receive calls
     * to the hook methods, but it will still be considered when determining
     * focus for the entire group. However, if the parent disables focus,
     * neither it nor any of its children will receive focus events.
     * <p>
     * Focus hook methods will be called as appropriate when children follow
     * focus is enabled or disabled:
     * <ul>
     * <li>If the parent has focus when the feature is enabled, the children
     * will gain focus as well</li>
     * <li>If the group has focus when the feature is disabled, the children
     * will lose focus (children that are independently
     * {@linkplain #setFollowParentFocus(boolean) following parent focus} are
     * excepted from this). If a child would have focus normally, the parent
     * will then lose focus and the child will gain focus again</li>
     * <li>If the parent does not have focus when the feature is enabled, but
     * one of the children does have focus, the focused child will first lose
     * focus, and then the entire group will gain focus</li>
     * </ul>
     *
     * @param follow
     *            {@code true} to enable children following focus, {@code false}
     *            to disable.
     */
    public void setChildrenFollowFocus(final boolean follow) {
        if (follow != mChildrenFollowFocus) {
            mChildrenFollowFocus = follow;
            final boolean focused = isFocused();
            List<Widget> children = getChildren();
            if (focused && follow) {
                for (Widget child : children) {
                    if (child.isFocusEnabled()) {
                        child.doOnFocus(true);
                    }
                }
            } else if (focused && !follow) {
                for (Widget child : children) {
                    if (child.isFocusEnabled() && !child.mFollowParentFocus) {
                        child.doOnFocus(false);
                    }
                }
            }
            for (Widget child : children) {
                if (focused && child.isFocusEnabled()) {
                    if (follow) {
                        child.doOnFocus(true);
                    } else if (!child.getFollowParentFocus()) {
                        child.doOnFocus(false);
                    }
                }

                Log.d(Log.SUBSYSTEM.WIDGET, TAG,
                      "setChildrenFollowFocus(%s): calling registerPickable",
                      getName());
                child.registerPickable();
            }
        }
    }

    /**
     * Whether this {@link Widget} will be grouped with its parent for purposes
     * of managing focus. This is different from
     * {@link #setChildrenFollowFocus(boolean)} in that the parent is not in
     * control of whether or not the child follows focus, as the following has
     * been initiated by the child.
     *
     * @return {@code true} if this {@code Widget} is following its parent's
     *         focus, {@code false} if not.
     */
    public boolean getFollowParentFocus() {
        return mFollowParentFocus;
    }

    /**
     * This method is nearly identical to
     * {@link #setChildrenFollowFocus(boolean)}, with the only difference being
     * that the child is independently grouping itself with the parent for
     * purposes of managing focus. If either feature is enabled, the child will
     * be focused with the parent.
     *
     * @param follow
     *            {@code true} to enable this {@link Widget} to follow its
     *            parent's focus, {@code false} to disable.
     */
    public void setFollowParentFocus(final boolean follow) {
        if (follow != mFollowParentFocus) {
            mFollowParentFocus = follow;
            Log.d(Log.SUBSYSTEM.WIDGET, TAG, "setFollowParentFocus(%s): calling registerPickable",
                  getName());
            registerPickable();
        }
    }

    /**
     * @return Whether the children of this {@link Widget} will be grouped with
     *         their parent as a single touchable object.
     */
    public boolean getChildrenFollowInput() {
        return mChildrenFollowInput;
    }

    /**
     * When children follow the input of their parent {@link Widget}, the parent
     * and children are treated as a single entity for touch event purposes.
     * When any of them would normally get a touch event, they all get a touch
     * event.
     * <p>
     * The touch hook method -- {@link Widget#onTouch()}  -- is invoked as
     * usual, but en masse for the parent and its children. There is one caveat:
     * only the parent's {@code onTouch()} method determines whether the touch
     * event is accepted or rejected. If the parent rejects the event, it is
     * rejected for the entire group and none of the children will have
     * {@code onTouch()} called on them.
     * <p>
     * Children can {@linkplain #setTouchable(boolean) enable and disable} touch
     * individually; if a child disables touch, it will not receive calls to
     * {@code onTouch()}, but it will still be considered for dispatching touch
     * events to the entire group. However, if the parent disables touch,
     * neither it nor any of its children will receive touch events.
     *
     * @param follow
     *            {@code true} to enable children following input, {@code false}
     *            to disable.
     */
    public void setChildrenFollowInput(final boolean follow) {
        if (follow != mChildrenFollowInput) {
            mChildrenFollowInput = follow;
            for (Widget child : getChildren()) {
                child.registerPickable();
            }
        }
    }

    /**
     * Whether this {@link Widget} will be grouped with its parent for
     * receiving input. This is different from
     * {@link #setChildrenFollowInput(boolean) in that the parent is not in
     * control of whether or not the child follows input, as the following has
     * been initiated by the child.
     *
     * @return {@code true} if this {@code Widget} is following its parent's
     *         input, {@code false} if not.
     */
    public boolean getFollowParentInput() {
        return mFollowParentInput;
    }

    public void setFollowParentInput(final boolean follow) {
        if (follow != mFollowParentInput) {
            mFollowParentInput = follow;
            registerPickable();
        }
    }

    protected interface OnHierarchyChangedListener {
        void onChildWidgetAdded(Widget parent, Widget child);
        void onChildWidgetRemoved(Widget parent, Widget child);
    }

    private final class FocusableImpl implements FocusManager.Focusable,
            FocusManager.LongFocusTimeout {
        /**
         * Hook method for handling changes in focus for this object.
         *
         * @param focused
         *            {@code True} if the object has gained focus, {@code false}
         *            if it has lost focus.
         */
        @Override
        public boolean onFocus(boolean focused) {
            return Widget.this.doOnFocus(focused);
        }

        /**
         * Hook method for handling long focus events. Called when the object
         * has held focus for longer than a certain period of time. This is
         * similar to
         * {@link android.view.GestureDetector.OnGestureListener#onLongPress(MotionEvent)
         * OnGestureListener.onLongPress()}.
         */
        @Override
        public void onLongFocus() {
            Widget.this.doOnLongFocus();
        }

        @Override
        public boolean isFocusEnabled() {
            return Widget.this.isFocusEnabled();
        }

        @Override
        public long getLongFocusTimeout() {
            // We use the getter method instead of directly accessing the
            // property so that deriving classes can override
            // getLongFocusTimeout() and return a constant; this is a handy
            // pattern for ensuring that instances of a class will always have
            // the specified timeout.
            return Widget.this.getLongFocusTimeout();
        }

        @Override
        public String toString() {
            return target().getName();
        }

        public Widget target() {
            return Widget.this;
        }
    }

    private FocusableImpl mFocusableImpl = new FocusableImpl();

    /**
     * Set whether or not the {@code Widget} can receive touch and back key
     * events. If enabled, the object will receive {@link #onTouch()} and
     * {@link #onBackKey()} notifications and registered
     * {@linkplain #addTouchListener(OnTouchListener) touch} and
     * {@linkplain #addBackKeyListener(OnBackKeyListener) back key} listeners
     * can also receive those notifications.
     * <p>
     * Objects are touchable by default.
     *
     * @param touchable
     *            {@code True} to enable touch events for this object,
     *            {@code false} to disable.
     */

    public void setTouchable(boolean touchable) {
        if (touchable != mIsTouchable) {
            mIsTouchable = touchable;
            registerPickable();
        }
    }

    /**
     * @return Whether touch and back key events are enabled for this object.
     */
    public boolean isTouchable() {
        return mIsTouchable;
    }

    /**
     * Determines whether this {@link Widget} is currently handling touch events
     * for the specified {@link GVRSceneObject}. This will be true if either
     * <p>
     * <ul>
     * <li>
     * The {@code GVRSceneObject} is wrapped by this {@code Widget} <em>and</em>
     * this {@code Widget} is not following its parent's input</li>
     * <li>The {@code GVRSceneObject} is wrapped by a child of this
     * {@code Widget} and the child <em>is</em> following this {@code Widget's}
     * input</li>
     * </ul>
     *
     *
     * @param sceneObject
     *            The {@code GVRSceneObject} to check
     * @return {@code True} if this {@code Widget} handles focus events for the
     *         {@code GVRSceneObject}, {@code false} if it doesn't.
     */
    public boolean handlesTouchFor(final GVRSceneObject sceneObject) {
        return handlesEventFor(sceneObject, mHandlesTouchEvent);
    }

    /**
     * {@link Widget} version of {@link #handlesTouchFor(GVRSceneObject)}.
     *
     * @param widget
     *            The {@code Widget} to check
     */
    public boolean handlesTouchFor(final Widget widget) {
        return handlesTouchFor(widget.getSceneObject());
    }

    /**
     * Add a listener for {@linkplain OnBackKeyListener#onBackKey(Widget) back
     * key} notifications for this object.
     *
     * @param listener
     *            An implementation of {@link OnBackKeyListener}.
     * @return {@code True} if the listener was successfully registered,
     *         {@code false} if the listener was already registered.
     */
    public boolean addBackKeyListener(final OnBackKeyListener listener) {
        return mBackKeyListeners.add(listener);
    }

    /**
     * Remove a previously {@linkplain #addBackKeyListener(OnBackKeyListener)
     * registered} back key notification {@linkplain OnBackKeyListener listener}
     * .
     *
     * @param listener
     *            An implementation of {@link OnBackKeyListener}
     * @return {@code True} if the listener was successfully unregistered,
     *         {@code false} if the specified listener was not previously
     *         registered with this object.
     */
    public boolean removeBackKeyListener(final OnBackKeyListener listener) {
        return mBackKeyListeners.remove(listener);
    }

    /**
     * Add a listener for {@linkplain OnTouchListener#onTouch(Widget) touch}
     * notifications for this object.
     *
     * @param listener
     *            An implementation of {@link OnTouchListener}.
     * @return {@code True} if {@code listener} was successfully registered,
     *         {@code false} if {@code listener} was already registered or {@code listener} is
     *         {@code null}.
     */
    public boolean addTouchListener(final OnTouchListener listener) {
        if (listener != null) {
            return mTouchListeners.add(listener);
        }
        return false;
    }

    /**
     * Remove a previously {@linkplain #addTouchListener(OnTouchListener)
     * registered} touch notification {@linkplain OnTouchListener listener}.
     *
     * @param listener
     *            An implementation of {@link OnTouchListener}
     * @return {@code True} if {@code listener} was successfully unregistered,
     *         {@code false} if {@code listener} was not previously
     *         registered with this object or is {@code null}.
     */
    public boolean removeTouchListener(final OnTouchListener listener) {
        return mTouchListeners.remove(listener);
    }

    public boolean getChildrenFollowState() {
        return mChildrenFollowState;
    }

    public void setChildrenFollowState(final boolean follow) {
        if (follow != mChildrenFollowState) {
            mChildrenFollowState = follow;
            updateState();
        }
    }

    public boolean getFollowParentState() {
        return mFollowParentState;
    }

    public void setFollowParentState(final boolean follow) {
        if (follow != mFollowParentState) {
            mFollowParentState = follow;
            updateState();
        }
    }

    /**
     * Set the current "level" of the {@link Widget}. This is useful for
     * indicating different states that reflect a change in quantity (e.g.,
     * battery charge, WiFi signal strength, etc.). The visual change for each
     * level can be a change in material, an animation, or showing a sub-object.
     *
     * @param level
     *            The new level value. Values will be clamped to the range
     *            [0,num_levels).
     */
    public void setLevel(int level) {
        if (level >= 0 && mLevel != level && level < mLevelInfo.size()) {
            Log.d(Log.SUBSYSTEM.WIDGET, TAG, "setLevel(%d): clearing level: %d", level, mLevel);
            if (mLevel >= 0) {
                mLevelInfo.get(mLevel).setState(this, null);
            }

            mLevel = level;

            updateState();
        }
    }

    /**
     * @return The current {@linkplain #setLevel(int) level} of the
     *         {@link Widget}.
     */
    public int getLevel() {
        return mLevel;
    }

    public void setPressed(final boolean pressed) {
        if (pressed != mIsPressed) {
            mIsPressed = pressed;
            updateState();
        }
    }

    public boolean isPressed() {
        return mIsPressed;
    }

    /**
     * Sets the state of the {@link Widget} to "selected". This state may be
     * accompanied by visual changes -- material, animation, displayed mesh --
     * if it has been specified in the {@code Widget's} metadata.
     *
     * @param selected
     *            {@code True} to set the {@code Widget} as selected,
     *            {@code false} to set as unselected.
     */
    public void setSelected(final boolean selected) {
        if (selected != mIsSelected) {
            mIsSelected = selected;
            if (onSelected(mIsSelected)) {
                updateState();
            }
        }
    }

    /**
     * @return {@code True} if the {@link Widget Widget's} state is set to
     *         {@linkplain #setSelected(boolean) "selected", {@code false} if
     *         it is not.
     */
    public boolean isSelected() {
        return mIsSelected;
    }

    /**
     * Get the (optional) name of the {@link Widget}.
     *
     * @return The name of the {@code Widget}.
     */
    public String getName() {
        return mName;
    }

    /**
     * Set the (optional) name of the {@link Widget}. {@code Widget} names are
     * not needed: they are only for the application's convenience.
     *
     * @param name
     */
    public void setName(String name) {
        mName = name;
        if (mSceneObject != null) {
            mSceneObject.setName(name);
        }
    }

    public String getMetadata() {
        return mSceneObject.getName();
    }

    /**
     * @return The {@link Widget Widget's} parent. If the {@code Widget} has not
     *         been {@linkplain GroupWidget#addChild(Widget) added} to a
     *         {@code GroupWidget}, returns {@code null}.
     */
    public final Widget getParent() {
        return mParent;
    }

    /**
     * Set the order in which this {@link Widget} will be rendered.
     *
     * @param renderingOrder
     *            See {@link GVRRenderingOrder}.
     */
    public void setRenderingOrder(final int renderingOrder) {
        final GVRRenderData renderData = getRenderData();
        if (renderData != null) {
            renderData.setRenderingOrder(renderingOrder);
        }
    }

    /**
     * @return The order in which this {@link Widget} will be rendered.
     * @see GVRRenderingOrder
     */
    public final int getRenderingOrder() {
        final GVRRenderData renderData = getRenderData();
        if (renderData != null) {
            return renderData.getRenderingOrder();
        }
        return -1;
    }

    private boolean mClippingEnabled;
    public void enableClipRegion() {
        if (mClippingEnabled) {
            Log.w(TAG, "Clipping has been enabled already for %s!", getName());
            return;
        }
        Log.d(Log.SUBSYSTEM.WIDGET, TAG, "enableClipping for %s [%f, %f, %f]",
                getName(), getViewPortWidth(), getViewPortHeight(), getViewPortDepth());

        mClippingEnabled = true;

        GVRTexture texture = mContext.getAssetLoader().loadTexture(new GVRAndroidResource(mContext, R.drawable.yellow));

        GVRSceneObject clippingObj = new GVRSceneObject(mContext, getViewPortWidth(), getViewPortHeight(), texture);
        clippingObj.setName("clippingObj");
        clippingObj.getRenderData()
                .setRenderingOrder(GVRRenderData.GVRRenderingOrder.STENCIL)
                .setStencilTest(true)
                .setStencilFunc(GLES30.GL_ALWAYS, 1, 0xFF)
                .setStencilOp(GLES30.GL_KEEP, GLES30.GL_KEEP, GLES30.GL_REPLACE)
                .setStencilMask(0xFF);

        mSceneObject.addChildObject(clippingObj);

        for (Widget child : getChildren()) {
            setObjectClipped(child);
        }
    }

    private static void setObjectClipped(Widget widget) {
        Log.d(Log.SUBSYSTEM.WIDGET, TAG, "setObjectClipped for %s", widget.getName());

        widget.getRenderData()
                .setStencilTest(true)
                .setStencilFunc(GLES30.GL_EQUAL, 1, 0xFF)
                .setStencilMask(0x00);

        widget.mClippingEnabled = true;

        for (Widget child : widget.getChildren()) {
            setObjectClipped(child);
        }
    }

    /**
     * Sets the {@linkplain GVRMaterial#setMainTexture(GVRTexture) main texture}
     * of the {@link Widget}.
     *
     * @param texture
     *            The new texture.
     */
    public void setTexture(final GVRTexture texture) {
        final GVRMaterial material = getMaterial();
        material.setMainTexture(texture);
        //models use the new shader framework which has no single main texture
        material.setTexture(MATERIAL_DIFFUSE_TEXTURE, texture);
    }

    /**
     * Sets the {@linkplain GVRMaterial#setMainTexture(GVRTexture) main texture}
     * of the {@link Widget}.
     *
     * @param name
     *            Name of the texture
     * @param texture
     *            The new texture.
     */
    public void setTexture(String name, final GVRTexture texture) {
        getMaterial().setTexture(name, texture);
    }

    /**
     * Sets the {@linkplain GVRMaterial#setMainTexture(GVRTexture) main texture}
     * of the {@link Widget}.
     *
     * @param name
     *            Name of the texture
     * @param texture
     *            The new texture.
     */
    public void setTexture(String name, final Future<GVRTexture> texture) {
        getMaterial().setTexture(name, texture);
    }

    /**
     * Sets the {@linkplain GVRMaterial#setMainTexture(GVRTexture) main texture}
     * of the {@link Widget}.
     *
     * @param texture
     *            The new texture.
     */
    public void setTexture(final Future<GVRTexture> texture) {
        final GVRMaterial material = getMaterial();
        if (material != null) {
            material.setMainTexture(texture);
            material.setTexture(MATERIAL_DIFFUSE_TEXTURE, texture);
            Log.w(TAG, "setTexture(%s): texture = %s!", getName(), texture);
        } else {
            Log.w(TAG, "setTexture(%s): material is null!", getName());
        }
    }

    /**
     * Sets the {@linkplain GVRMaterial#setMainTexture(GVRTexture) main texture}
     * of the {@link Widget}.
     *
     * @param bitmapId
     *            Resource ID of the bitmap to create the texture from.
     */
    public void setTexture(final int bitmapId) {
        if (bitmapId < 0)
            return;

        final GVRAndroidResource resource = new GVRAndroidResource(
                mContext.getContext(), bitmapId);
        setTexture(mContext.getAssetLoader().loadFutureTexture(resource));
    }

    /**
     * This group of methods get the mesh size of the widget
     */
    public float getWidth() {
        return getBoundingBoxInternal().getWidth();
    }

    public float getHeight() {
        return getBoundingBoxInternal().getHeight();
    }

    public float getDepth() {
        return getBoundingBoxInternal().getDepth();
    }

    public float getLayoutSize(final Layout.Axis axis) {
        float size = 0;
        for (Layout layout : mLayouts) {
            size = Math.max(size, layout.getSize(axis));
        }
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "getLayoutSize [%s] axis [%s] size [%f]", getName(), axis, size);
        return size;
    }

    public float getLayoutWidth() {
        return getLayoutSize(Layout.Axis.X);
    }

    public float getLayoutHeight() {
        return getLayoutSize(Layout.Axis.Y);
    }

    public float getLayoutDepth() {
        return getLayoutSize(Layout.Axis.Z);
    }

    public float getBoundsWidth() {
        if (mSceneObject != null) {
            GVRSceneObject.BoundingVolume v = mSceneObject.getBoundingVolume();
            if (v != null) {
                return v.maxCorner.x - v.minCorner.x;
            }
        }
        return 0f;
    }

    public float getBoundsHeight(){
        if (mSceneObject != null) {
            GVRSceneObject.BoundingVolume v = mSceneObject.getBoundingVolume();
            if (v != null) {
                return v.maxCorner.y - v.minCorner.y;
            }
        }
        return 0f;
    }

    public float getBoundsDepth() {
        if (mSceneObject != null) {
            GVRSceneObject.BoundingVolume v = mSceneObject.getBoundingVolume();
            if (v != null) {
                return v.maxCorner.z - v.minCorner.z;
            }
        }
        return 0f;
    }

    /**
     * This group of methods set/get the viewport size of the widget
     */
    private Vector3Axis mViewPort;

    public float getViewPortWidth() {
        return mViewPort.get(Layout.Axis.X);
    }

    public float getViewPortHeight() {
        return mViewPort.get(Layout.Axis.Y);
    }

    public float getViewPortDepth() {
        return mViewPort.get(Layout.Axis.Z);
    }

    private void updateViewPort(float size, Layout.Axis axis) {
        Log.d(Log.SUBSYSTEM.WIDGET, TAG, "Widget[%s] setViewPort : viewport = %s size = %f", this, mViewPort, size);
        if (mViewPort.get(axis) != size) {
            mViewPort.set(size, axis);
            for (Layout layout : mLayouts) {
                layout.onLayoutApplied(this, mViewPort);
            }
        }
    }

    public void setViewPortWidth(float viewPortWidth) {
        updateViewPort(viewPortWidth, Layout.Axis.X);
    }

    public void setViewPortHeight(float viewPortHeight) {
        updateViewPort(viewPortHeight, Layout.Axis.Y);
    }

    public void setViewPortDepth(float viewPortDepth) {
        updateViewPort(viewPortDepth, Layout.Axis.Z);
    }

    /**
     * Get a {@link BoundingBox} that is axis-aligned with this {@link Widget}'s
     * parent and sized to contain the {@code Widget} with its local transform
     * applied.
     * <p>
     * Note: The {@code Widget}'s children are <em>not</em> explicitly included,
     * so the bounding box may or may not be big enough to include them. If you
     * want to make sure that the bounding box fully encompasses the
     * {@code Widget}'s children, call GVRSceneObject.getBoundingVolume}.
     *
     * @return A {@link BoundingBox} that contains the {@code Widget}.
     */
    public BoundingBox getBoundingBox() {
        return new BoundingBox(getBoundingBoxInternal());
    }

    /**
     * Set the {@code GL_DEPTH_TEST} option
     *
     * @param depthTest
     *            {@code true} if {@code GL_DEPTH_TEST} should be enabled,
     *            {@code false} if not.
     */
    public void setDepthTest(boolean depthTest) {
        GVRRenderData renderData = getRenderData();
        if (renderData != null) {
            renderData.setDepthTest(depthTest);
        }
    }

    /**
     * @return {@code true} if {@code GL_DEPTH_TEST} is enabled, {@code false}
     *         if not.
     */
    public boolean getDepthTest() {
        GVRRenderData renderData = getRenderData();
        if (renderData != null) {
            return renderData.getDepthTest();
        }
        return false;
    }

    /**
     * Set the {@code GL_POLYGON_OFFSET_FILL} option
     *
     * @param offset
     *            {@code true} if {@code GL_POLYGON_OFFSET_FILL} should be
     *            enabled, {@code false} if not.
     */
    public void setOffset(boolean offset) {
        GVRRenderData renderData = getRenderData();
        if (renderData != null) {
            renderData.setOffset(offset);
        }
    }

    /**
     * @return {@code true} if {@code GL_POLYGON_OFFSET_FILL} is enabled,
     *         {@code false} if not.
     */
    public boolean getOffset() {
        GVRRenderData renderData = getRenderData();
        if (renderData != null) {
            return renderData.getOffset();
        }
        return false;
    }

    /**
     * Set the {@code factor} value passed to {@code glPolygonOffset()} if
     * {@code GL_POLYGON_OFFSET_FILL} is enabled.
     *
     * @param offsetFactor
     *            Per OpenGL docs: Specifies a scale factor that is used to
     *            create a variable depth offset for each polygon. The initial
     *            value is 0.
     * @see #setOffset(boolean)
     */
    public void setOffsetFactor(float offsetFactor) {
        GVRRenderData renderData = getRenderData();
        if (renderData != null) {
            renderData.setOffsetFactor(offsetFactor);
        }
    }

    /**
     * @return The {@code factor} value passed to {@code glPolygonOffset()} if
     *         {@code GL_POLYGON_OFFSET_FILL} is enabled.
     * @see #setOffset(boolean)
     */
    public float getOffsetFactor() {
        GVRRenderData renderData = getRenderData();
        if (renderData != null) {
            return renderData.getOffsetFactor();
        }
        return 0;
    }

    /**
     * Set the {@code units} value passed to {@code glPolygonOffset()} if
     * {@code GL_POLYGON_OFFSET_FILL} is enabled.
     *
     * @param offsetUnits
     *            Per OpenGL docs: Is multiplied by an implementation-specific
     *            value to create a constant depth offset. The initial value is
     *            0.
     * @see #setOffset(boolean)
     */
    public void setOffsetUnits(float offsetUnits) {
        GVRRenderData renderData = getRenderData();
        if (renderData != null) {
            renderData.setOffsetUnits(offsetUnits);
        }
    }

    /**
     * @return The {@code units} value passed to {@code glPolygonOffset()} if
     *         {@code GL_POLYGON_OFFSET_FILL} is enabled.
     * @see #setOffset(boolean)
     */
    public float getOffsetUnits() {
        GVRRenderData renderData = getRenderData();
        if (renderData != null) {
            return renderData.getOffsetUnits();
        }
        return 0;
    }

    /**
     * Get the X component of the widget's position.
     *
     * @return 'X' component of the widget's position.
     */
    public float getPositionX() {
        return getTransform().getPositionX();
    }

    /**
     * Get the 'Y' component of the widget's position.
     *
     * @return 'Y' component of the widget's position.
     */
    public float getPositionY() {
        return getTransform().getPositionY();
    }

    /**
     * Get the 'Z' component of the widget's position.
     *
     * @return 'Z' component of the widget's position.
     */
    public float getPositionZ() {
        return getTransform().getPositionZ();
    }

    /**
     * Set absolute position.
     *
     * Use {@link #translate(float, float, float)} to <em>move</em> the object.
     *
     * @param x
     *            'X' component of the absolute position.
     * @param y
     *            'Y' component of the absolute position.
     * @param z
     *            'Z' component of the absolute position.
     */
    public void setPosition(float x, float y, float z) {
        getTransform().setPosition(x, y, z);
        if (mTransformCache.setPosition(x, y, z)) {
            onTransformChanged();
        }
    }

    /**
     * Set the 'X' component of absolute position.
     *
     * Use {@link #translate(float, float, float)} to <em>move</em> the object.
     *
     * @param x
     *            New 'X' component of the absolute position.
     */
    public void setPositionX(float x) {
        getTransform().setPositionX(x);
        if (mTransformCache.setPosX(x)) {
            onTransformChanged();
        }
    }

    /**
     * Set the 'Y' component of the absolute position.
     *
     * Use {@link #translate(float, float, float)} to <em>move</em> the object.
     *
     * @param y
     *            New 'Y' component of the absolute position.
     */
    public void setPositionY(float y) {
        getTransform().setPositionY(y);
        if (mTransformCache.setPosY(y)) {
            onTransformChanged();
        }
    }

    /**
     * Set the 'Z' component of the absolute position.
     *
     * Use {@link #translate(float, float, float)} to <em>move</em> the object.
     *
     * @param z
     *            New 'Z' component of the absolute position.
     */
    public void setPositionZ(float z) {
        getTransform().setPositionZ(z);
        if (mTransformCache.setPosZ(z)) {
            onTransformChanged();
        }
    }

    /**
     * Get the quaternion 'W' component.
     *
     * @return 'W' component of the widget's rotation, treated as a quaternion.
     */
    public float getRotationW() {
        return getTransform().getRotationW();
    }

    /**
     * Get the quaternion 'X' component.
     *
     * @return 'X' component of the widget's rotation, treated as a quaternion.
     */
    public float getRotationX() {
        return getTransform().getRotationX();
    }

    /**
     * Get the quaternion 'Y' component.
     *
     * @return 'Y' component of the widget's rotation, treated as a quaternion.
     */
    public float getRotationY() {
        return getTransform().getRotationY();
    }

    /**
     * Get the quaternion 'Z' component.
     *
     * @return 'Z' component of the widget's rotation, treated as a quaternion.
     */
    public float getRotationZ() {
        return getTransform().getRotationZ();
    }

    /**
     * Get the rotation around the 'Y' axis, in degrees.
     *
     * @return The widget's current rotation around the 'Y' axis, in degrees.
     */
    public float getRotationYaw() {
        return getTransform().getRotationYaw();
    }

    /**
     * Get the rotation around the 'X' axis, in degrees.
     *
     * @return The widget's rotation around the 'X' axis, in degrees.
     */
    public float getRotationPitch() {
        return getTransform().getRotationPitch();
    }

    /**
     * Get the rotation around the 'Z' axis, in degrees.
     *
     * @return The widget's rotation around the 'Z' axis, in degrees.
     */
    public float getRotationRoll() {
        return getTransform().getRotationRoll();
    }

    /**
     * Set rotation, as a quaternion.
     *
     * Sets the widget's current rotation in quaternion terms. Overrides any
     * previous rotations using {@link #rotate(float, float, float, float)
     * rotate()}, {@link #rotateByAxis(float, float, float, float)
     * rotateByAxis()} , or
     * {@link #rotateByAxisWithPivot(float, float, float, float, float, float, float)
     * rotateByAxisWithPivot()} .
     *
     * @param w
     *            'W' component of the quaternion.
     * @param x
     *            'X' component of the quaternion.
     * @param y
     *            'Y' component of the quaternion.
     * @param z
     *            'Z' component of the quaternion.
     */
    public void setRotation(float w, float x, float y, float z) {
        getTransform().setRotation(w, x, y, z);
        if (mTransformCache.setRotation(w, x, y, z)) {
            onTransformChanged();
        }
    }

    /**
     * Get the 'X' scale
     *
     * @return The widget's current scaling on the 'X' axis.
     */
    public float getScaleX() {
        return getTransform().getScaleX();
    }

    /**
     * Get the 'Y' scale
     *
     * @return The widget's current scaling on the 'Y' axis.
     */
    public float getScaleY() {
        return getTransform().getScaleY();
    }

    /**
     * Get the 'Z' scale
     *
     * @return The widget's current scaling on the 'Z' axis.
     */
    public float getScaleZ() {
        return getTransform().getScaleZ();
    }

    /**
     * Set [X, Y, Z] current scale
     *
     * @param x
     *            Scaling factor on the 'X' axis.
     * @param y
     *            Scaling factor on the 'Y' axis.
     * @param z
     *            Scaling factor on the 'Z' axis.
     */
    public void setScale(float x, float y, float z) {
        getTransform().setScale(x, y, z);
        if (mTransformCache.setScale(x, y, z)) {
            onTransformChanged();
        }
    }

    /**
     * Set [X, Y, Z] current scale to same value
     *
     * @param scale
     *            Scaling factor for all axes.
     */
    public void setScale(float scale) {
        setScale(scale, scale, scale);
    }

    /**
     * Set the widget's current scaling on the 'X' axis.
     *
     * @param x
     *            Scaling factor on the 'X' axis.
     */
    public void setScaleX(float x) {
        getTransform().setScaleX(x);
        if (mTransformCache.setScaleX(x)) {
            onTransformChanged();
        }
    }

    /**
     * Set the widget's current scaling on the 'Y' axis.
     *
     * @param y
     *            Scaling factor on the 'Y' axis.
     */
    public void setScaleY(float y) {
        getTransform().setScaleY(y);
        if (mTransformCache.setScaleY(y)) {
            onTransformChanged();
        }
    }

    /**
     * Set the widget's current scaling on the 'Z' axis.
     *
     * @param z
     *            Scaling factor on the 'Z' axis.
     */
    public void setScaleZ(float z) {
        getTransform().setScaleZ(z);
        if (mTransformCache.setScaleZ(z)) {
            onTransformChanged();
        }
    }

    /**
     * Get the 4x4 single matrix.
     *
     * @return An array of 16 {@code float}s representing a 4x4 matrix in
     *         OpenGL-compatible column-major format.
     */
    public float[] getModelMatrix() {
        return getTransform().getModelMatrix();
    }

    /**
     * Set the 4x4 model matrix and set current scaling, rotation, and
     * transformation based on this model matrix.
     *
     * @param mat
     *            An array of 16 {@code float}s representing a 4x4 matrix in
     *            OpenGL-compatible column-major format.
     */
    public void setModelMatrix(float[] mat) {
        if (mat.length != 16) {
            throw new IllegalArgumentException("Size not equal to 16.");
        }
        getTransform().setModelMatrix(mat);
    }

    /**
     * Move the object, relative to its current position.
     *
     * Modify the tranform's current translation by applying translations on all
     * 3 axes.
     *
     * @param x
     *            'X' delta
     * @param y
     *            'Y' delta
     * @param z
     *            'Z' delta
     */
    public void translate(float x, float y, float z) {
        getTransform().translate(x, y, z);
        checkTransformChanged();
    }

    /**
     * Sets the absolute rotation in angle/axis terms.
     *
     * Rotates using the right hand rule.
     *
     * <p>
     * Contrast this with {@link #rotate(float, float, float, float) rotate()},
     * {@link #rotateByAxis(float, float, float, float) rotateByAxis()}, or
     * {@link #rotateByAxisWithPivot(float, float, float, float, float, float, float)
     * rotateByAxisWithPivot()}, which all do relative rotations.
     *
     * @param angle
     *            Angle of rotation in degrees.
     * @param x
     *            'X' component of the axis.
     * @param y
     *            'Y' component of the axis.
     * @param z
     *            'Z' component of the axis.
     */
    public void setRotationByAxis(float angle, float x, float y, float z) {
        getTransform().setRotationByAxis(angle, x, y, z);
        checkTransformChanged();
    }

    /**
     * Modify the tranform's current rotation in quaternion terms.
     *
     * @param w
     *            'W' component of the quaternion.
     * @param x
     *            'X' component of the quaternion.
     * @param y
     *            'Y' component of the quaternion.
     * @param z
     *            'Z' component of the quaternion.
     */
    public void rotate(float w, float x, float y, float z) {
        getTransform().rotate(w, x, y, z);
        checkTransformChanged();
    }

    /**
     * Modify the tranform's current rotation in quaternion terms, around a
     * pivot other than the origin.
     *
     * @param w
     *            'W' component of the quaternion.
     * @param x
     *            'X' component of the quaternion.
     * @param y
     *            'Y' component of the quaternion.
     * @param z
     *            'Z' component of the quaternion.
     * @param pivotX
     *            'X' component of the pivot's location.
     * @param pivotY
     *            'Y' component of the pivot's location.
     * @param pivotZ
     *            'Z' component of the pivot's location.
     */
    public void rotateWithPivot(float w, float x, float y, float z,
            float pivotX, float pivotY, float pivotZ) {
        getTransform().rotateWithPivot(w, x, y, z, pivotX, pivotY, pivotZ);
        checkTransformChanged();
    }

    /**
     * Modify the widget's current rotation in angle/axis terms.
     *
     * @param angle
     *            Angle of rotation in degrees.
     * @param x
     *            'X' component of the axis.
     * @param y
     *            'Y' component of the axis.
     * @param z
     *            'Z' component of the axis.
     */
    public void rotateByAxis(float angle, float x, float y, float z) {
        getTransform().rotateByAxis(angle, x, y, z);
        checkTransformChanged();
    }

    /**
     * Modify the widget's current rotation in angle/axis terms, around a pivot
     * other than the origin.
     *
     * @param angle
     *            Angle of rotation in degrees.
     * @param axisX
     *            'X' component of the axis.
     * @param axisY
     *            'Y' component of the axis.
     * @param axisZ
     *            'Z' component of the axis.
     * @param pivotX
     *            'X' component of the pivot's location.
     * @param pivotY
     *            'Y' component of the pivot's location.
     * @param pivotZ
     *            'Z' component of the pivot's location.
     */
    public void rotateByAxisWithPivot(float angle, float axisX, float axisY,
            float axisZ, float pivotX, float pivotY, float pivotZ) {
        getTransform().rotateByAxisWithPivot(angle, axisX, axisY, axisZ,
                                             pivotX, pivotY, pivotZ);
        checkTransformChanged();
    }

    /**
     * Reset the widget's transform.
     * <p>
     * This will undo any translations, rotations, or scaling and reset them
     * back to default values. This is the equivalent to setting the widget's
     * transform to an identity matrix.
     */
    public void reset() {
        getTransform().reset();
        checkTransformChanged();
    }

    public void setColor(final int color) {
        getMaterial().setColor(color);
    }

    public void setColor(final float[] rgb) {
        getMaterial().setColor(rgb[0], rgb[1], rgb[2]);
    }

    public void setColor(final float r, final float g, final float b) {
        getMaterial().setColor(r, g, b);
    }

    public float[] getColor() {
        return getMaterial().getColor();
    }

    /**
     * A convenience method that wraps {@link #getColor()} and returns an
     * Android {@link Color}
     *
     * @return An Android {@link Color}
     */
    public int getRgbColor() {
        return getMaterial().getRgbColor();
    }

    /**
     * Set the widget's opacity. This is dependent on the shader; see
     * {@link GVRMaterial#setOpacity(float)}.
     *
     * @param opacity
     *            Value between {@code 0.0f} and {@code 0.1f}, inclusive.
     */
    public void setOpacity(final float opacity) {
        getMaterial().setOpacity(opacity);
    }

    /**
     * Get the widget's opacity. This is dependent on the shader; see
     * {@link GVRMaterial#setOpacity(float)}.
     *
     * @return Current opacity value, between {@code 0.0f} and {@code 0.1f},
     *         inclusive.
     */
    public float getOpacity() {
        return getMaterial().getOpacity();
    }

    /**
     * Set the visibility of the object.
     *
     * @see Visibility
     * @param visibility
     *            The visibility of the object.
     * @return {@code true} if the visibility was changed, {@code false} if it
     *         wasn't.
     */
    public boolean setVisibility(final Visibility visibility) {
        if (visibility != mVisibility) {
            Log.d(Log.SUBSYSTEM.WIDGET, TAG, "setVisibility(%s) for %s", visibility, getName());
            updateVisibility(visibility);
            mVisibility = visibility;
            return true;
        }
        return false;
    }

    /**
     * @see Visibility
     * @return The object's current visibility
     */
    public Visibility getVisibility() {
        return mVisibility;
    }

    private void updateVisibility(final Visibility visibility) {
        Log.d(Log.SUBSYSTEM.WIDGET, TAG, "change visibility for widget<%s> to visibility = %s",
                getName(), visibility);
        if (mParent != null) {
            final GVRSceneObject parentSceneObject = mParent
                    .getSceneObject();
            switch (visibility) {
                case VISIBLE:
                    final GVRSceneObject sceneObjectParent = mSceneObject.getParent();
                    if (sceneObjectParent != parentSceneObject &&
                            mIsVisibleInViewPort != ViewPortVisibility.INVISIBLE ) {
                        if (null != sceneObjectParent) {
                            sceneObjectParent.removeChildObject(mSceneObject);
                        }
                        parentSceneObject.addChildObject(mSceneObject);
                        getGVRContext().getMainScene().bindShaders(parentSceneObject);
                    }
                    break;
                case HIDDEN:
                case GONE:
                    if (mVisibility == Visibility.VISIBLE) {
                        parentSceneObject.removeChildObject(mSceneObject);
                    }
                    break;
                case PLACEHOLDER:
                    getSceneObject().detachRenderData();
                    break;
            }
            if (mVisibility == Visibility.GONE
                    || visibility == Visibility.GONE) {
                mParent.onTransformChanged();
                mParent.invalidateAllLayouts();
                mParent.requestLayout();
            }
        }
    }

    /**
     * Set ViewPort visibility of the object.
     *
     * @see ViewPortVisibility
     * @param viewportVisibility
     *            The ViewPort visibility of the object.
     * @return {@code true} if the ViewPort visibility was changed, {@code false} if it
     *         wasn't.
     */
    public boolean setViewPortVisibility(final ViewPortVisibility viewportVisibility) {
        boolean visibilityIsChanged = viewportVisibility != mIsVisibleInViewPort;
        if (visibilityIsChanged) {
            Visibility visibility = mVisibility;

            switch(viewportVisibility) {
                case FULLY_VISIBLE:
                case PARTIALLY_VISIBLE:
                    break;
                case INVISIBLE:
                    visibility = Visibility.HIDDEN;
                    break;
            }

            mIsVisibleInViewPort = viewportVisibility;

            updateVisibility(visibility);
        }
        return visibilityIsChanged;
    }

    /**
     * @see ViewPortVisibility
     * @return The object's current ViewPort visibility
     */
    public ViewPortVisibility getViewPortVisibility() {
        return mIsVisibleInViewPort;
    }

    /**
     * Call this method to notify ancestors of this {@link Widget} that its
     * dimensions, position, or orientation have been altered so that they can
     * respond by running {@link #layout()} if needed.
     */
    public void requestLayout() {
        mLayoutRequested = true;

        Log.v(Log.SUBSYSTEM.LAYOUT, TAG,
                "requestLayout(%s): mParent: '%s', mParent.isLayoutRequested: %b",
                getName(), mParent == null ? "<NULL>" : mParent.getName(),
                mParent != null && mParent.isLayoutRequested());

        if (mParent != null && !mParent.isLayoutRequested()) {
            Log.v(Log.SUBSYSTEM.LAYOUT, TAG, "requestLayout(%s) requesting", getName());
            mParent.requestLayout();
            // new RuntimeException().printStackTrace();
        } else if (isInLayout()) {
            requestInnerLayout(this);
        }
    }

    protected void requestInnerLayout(Widget widget)  {
        if (mParent != null) {
            mParent.requestInnerLayout(widget);
        }
    }

    public boolean isChanged() {
        return mChanged;
    }

    public boolean isInLayout() {
        return mParent != null && mParent.isInLayout();
    }

    public boolean isLayoutRequested() {
        return mLayoutRequested;
    }

    /**
     * Get a recursive count of the number of {@link Widget} children of this
     * {@code Widget}.
     *
     * @param includeHidden
     *            Pass {@code false} to only count children whose
     *            {@link #setVisibility(Visibility) visibility} is
     *            {@link Visibility#VISIBLE}.
     * @return The count of child {@code Widgets}.
     */
    public int getChildCount(boolean includeHidden) {
        int count = 0;
        for (Widget child : getChildren()) {
            if (includeHidden || child.mVisibility == Visibility.VISIBLE) {
                ++count;
                count += child.getChildCount(includeHidden);
            }
        }
        return count;
    }

    /**
     * A small class for {@linkplain Widget#getChildInfo(boolean) building} a
     * recursive set of information about a {@link Widget Widget's} children.
     */
    public static class ChildInfo {
        /** The {@link Widget#getName() name} of the child {@link Widget}. */
        public final String name;
        /**
         * A {@link List} of information about this child's {@link Widget}
         * children.
         */
        public final List<ChildInfo> children;

        private ChildInfo(final String n, final List<ChildInfo> c) {
            name = n;
            children = c;
        }
    }

    /**
     * Get a recursive set of {@link ChildInfo} instances describing the
     * {@link Widget} children of this {@code Widget}.
     *
     * @param includeHidden
     *            Pass {@code false} to only count children whose
     *            {@link #setVisibility(Visibility) visibility} is
     *            {@link Visibility#VISIBLE}.
     *
     * @return A {@link List} of {@code ChildInfo}.
     */
    public List<ChildInfo> getChildInfo(boolean includeHidden) {
        List<ChildInfo> children = new ArrayList<>();
        for (Widget child : getChildren()) {
            if (includeHidden || child.mVisibility == Visibility.VISIBLE) {
                children.add(new ChildInfo(child.getName(), child
                        .getChildInfo(includeHidden)));
            }
        }
        return children;
    }

    protected Widget(final GVRContext context, final GVRMesh mesh) {
        this(context, new GVRSceneObject(context, mesh, sDefaultTexture));
    }

    /**
     * Initialize the instance on the GL thread. This method is called
     * automatically for you when the instance is
     * {@linkplain GroupWidget#addChild(Widget) attached} to another
     * {@code Widget}, but you may call it explicitly to do early
     * initialization. However many times this method is called, the creation
     * code will only be executed <em>once</em>.
     * <p>
     * Override {@link #onCreate()} to implement your GL thread initialization.
     */
    // TODO: Should this be public?
    protected final void create() {
        if (!mIsCreated) {
            // Set the default layout if necessary
            if (mLayouts.isEmpty()) {
                applyLayout(getDefaultLayout());
            }

            runOnGlThread(new Runnable() {
                @Override
                public void run() {
                    doOnCreate();
                }
            });
            mIsCreated = true;
        }
    }

    /**
     * Determine whether the calling thread is the GL thread.
     *
     * @return {@code True} if called from the GL thread, {@code false} if
     *         called from another thread, or if the {@link Widget} framework
     *         has not been {@linkplain #onInitListener GL initialized} yet.
     */
    protected final boolean isGLThread() {
        final Thread glThread = sGLThread.get();
        return glThread != null && glThread.equals(Thread.currentThread());
    }

    /**
     * Determine whether the specified {@link GVRSceneObject} is the object
     * wrapped by this {@link Widget}.
     *
     * @param sceneObject
     *            The {@code GVRSceneObject} to test against.
     * @return {@code true} if {@code sceneObject} is wrapped by this instance,
     *         {@code false} otherwise.
     */
    public final boolean isSceneObject(GVRSceneObject sceneObject) {
        return mSceneObject == sceneObject;
    }

    /**
     * Does layout on the {@link Widget}. If you override this method and don't
     * call {@code super}, bad things will almost certainly happen.
     * @return true if the widget has been relaidout, otherwise - false
     */
    @SuppressLint("WrongCall")
    protected boolean layout() {
        boolean relaidout = false;
        Log.v(Log.SUBSYSTEM.LAYOUT, TAG, "layout(%s): changed: %b, requested: %b", getName(),
                isChanged(), mLayoutRequested);

        if (isChanged() || mLayoutRequested) {
            Log.v(Log.SUBSYSTEM.LAYOUT, TAG, "layout(%s): calling onLayout", getName());
            relaidout = onLayout();
        }

        mLayoutRequested = false;
        mChanged = false;
        return relaidout;
    }

    /**
     * Execute a {@link Runnable} on the GL thread. If this method is called
     * from the GL thread, the {@code Runnable} is executed immediately;
     * otherwise, the {@code Runnable} will be executed in the next frame.
     * <p>
     * This differs from {@link GVRContext#runOnGlThread(Runnable)}: that method
     * always queues the {@code Runnable} for execution in the next frame.
     * <p>
     * Note: if the {@link Widget} framework has not yet been
     * {@link #onInitListener GL initialized}, the {@code Runnable} will be
     * executed in the next frame.
     *
     * @param r
     *            {@link Runnable} to execute on the GL thread.
     */
    protected final void runOnGlThread(final Runnable r) {

        getGVRContext().runOnGlThread(new Runnable() {
            public void run() {
                FPSCounter.timeCheck("runOnGlThread <START>: " + r);
                r.run();
                FPSCounter.timeCheck("runOnGlThread <END>: " + r);
            }
        });
    }

    protected final GVRContext getGVRContext() {
        return mContext;
    }

    /**
     * Get the {@link GVRMaterial material} for the underlying
     * {@link GVRSceneObject scene object}.
     *
     * @return The scene object's material or {@code null}.
     */
    protected GVRMaterial getMaterial() {
        final GVRRenderData renderData = getRenderData();
        if (renderData != null) {
            return renderData.getMaterial();
        }
        return null;
    }

    /**
     * Set the {@linkplain GVRMaterial material} for the underlying
     * {@linkplain GVRSceneObject scene object}.
     *
     * @param material
     *            The new material.
     */
    protected void setMaterial(final GVRMaterial material) {
        final GVRRenderData renderData = getRenderData();
        if (renderData != null) {
            renderData.setMaterial(material);
        }
    }

    /**
     * Get the {@link GVRMesh mesh} for the underlying {@link GVRSceneObject
     * scene object}.
     *
     * @return The scene object's mesh or {@code null}.
     */
    protected GVRMesh getMesh() {
        final GVRRenderData renderData = getRenderData();
        if (renderData != null) {
            return renderData.getMesh();
        }
        return null;
    }

    /**
     * Set the {@linkplain GVRMesh mesh} for the underlying
     * {@linkplain GVRSceneObject scene object}.
     *
     * @param mesh
     *            The new mesh.
     */
    protected void setMesh(final GVRMesh mesh) {
        final GVRRenderData renderData = getRenderData();
        if (renderData != null) {
            renderData.setMesh(mesh);
        }
    }

    protected JSONObject getObjectMetadata() {
        return mMetadata;
    }

    /**
     * A hook method called after the {@code Widget} instance has been
     * {@linkplain GroupWidget#addChild(Widget) added} to another {@link Widget}
     * as a child.
     * <p>
     * <b>NOTE:</b> The order of execution between this method and
     * {@link #onCreate()} is <em>not</em> guaranteed. As a general rule, you
     * should not write code that has dependencies between this method and
     * {@code onCreate()}.
     */
    protected void onAttached() {

    }

    /**
     * A hook method for doing any initialization that must be performed on the
     * GL Thread (e.g., creation of {@link GVRBitmapTexture bitmap textures}).
     * <p>
     * If {@link #create()} has not been explicitly called, this method will be
     * called automatically when the instance is added to another {@link Widget}
     * as a child.
     * <p>
     * <b>NOTE:</b> The order of execution between the
     * {@linkplain #onAttached() attach} and {@linkplain #onDetached() detach}
     * hooks and this method is <em>not</em> guaranteed. As a general rule, you
     * should not write code that has dependencies between the attachment hooks
     * and this method!
     *
     * @see #create()
     */
    protected void onCreate() {

    }

    /**
     * A hook method called after the {@code Widget} instance has been
     * {@linkplain GroupWidget#removeChild(Widget) removed} from another
     * {@link GroupWidget} as a child. At this point, the instance has no
     * {@linkplain #getParent() parent}.
     * <p>
     * <b>NOTE:</b> The order of execution between this method and
     * {@link #onCreate()} is <em>not</em> guaranteed. As a general rule, you
     * should not write code that has dependencies between this method and
     * {@code onCreate()}.
     */
    protected void onDetached() {

    }

    /**
     * Hook method for handling changes in focus for this object.
     *
     * @param focused
     *            {@code True} if the object has gained focus, {@code false} if
     *            it has lost focus.
     * @return {@code True} to accept focus, {@code false} if not.
     */
    protected boolean onFocus(boolean focused) {
        return true;
    }

    /**
     * Hook method for handling long focus events. Called when the object has
     * held focus for longer than a certain period of time. This is similar to
     * {@link android.view.GestureDetector.OnGestureListener#onLongPress(MotionEvent)
     * OnGestureListener.onLongPress()}.
     */
    protected void onLongFocus() {

    }

    /**
     * Hook method for handling layout events. For {@link GroupWidget} in
     * particular, this is where layout of children is done.
     * {@code GroupWidgets} should call {@link #layout()} on their children,
     * most likely before performing their own layout <em>of</em> their
     * children.
     * @return true if the widget has been relaidout, otherwise - false
     */
    protected long time;

    protected boolean onLayout() {
        boolean changed = isChanged();
        boolean runLayout = false;
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onLayout() called (%s) mChanged = %b ", getName(), changed);

        FPSCounter.timeCheck("onLayout <START>: " + this + "<" + getName() + "> changed = " + changed);

        float oldWidth = getLayoutWidth();
        float oldHeight = getLayoutHeight();
        float oldDepth = getLayoutDepth();

        for (Widget child : getChildren()) {
            if (child.layout()) {
                invalidateAllLayouts();
                runLayout = true;
            }
        }

        if (mLayouts == null || mLayouts.isEmpty()) {
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onLayout: no layouts applied %s!", getName());
        } else if (runLayout || changed) {

            for (Layout layout : mLayouts) {
                measureLayout(layout);
                layout.layoutChildren();
            }

            float newWidth = getLayoutWidth();
            float newHeight = getLayoutHeight();
            float newDepth = getLayoutDepth();

            changed = changed ||
                    !Utility.equal(oldWidth, newWidth) ||
                    !Utility.equal(oldHeight, newHeight) ||
                    !Utility.equal(oldDepth, newDepth);

            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onLayout: layout changed %s " +
                            "old = [%f, %f, %f] new [%f, %f, %f]!",

                  getName(), oldWidth, oldHeight, oldDepth, newWidth, newHeight, newDepth);
        } else {
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onLayout: layout is not changed %s!", getName());
        }

        FPSCounter.timeCheck("onLayout <END>: " + this + "<" + getName() + "> changed = " + changed);
        return changed;
    }

    protected boolean measureLayout(Layout layout) {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "[%s] measure layout = %s", this, layout);
        return layout.measureAll(null);
    }

    /**
     * Hook method for handling back key events.
     *
     * @return {@code True} if the back key event was successfully processed,
     *         {@code false} otherwise.
     */
    protected boolean onBackKey() {
        return false;
    }

    /**
     * Hook method for handling changes in selection state for this object.
     *
     * @param selected
     *            {@code True} if the object has gained selection, {@code false} if it has lost
     *            selection.
     * @return {@code True} to accept selection, {@code false} if not. Returns {@code true} by
     *            default.
     */
    protected boolean onSelected(boolean selected) {
        return true;
    }

    /**
     * Hook method for handling touch events.
     *
     * @return {@code True} if the touch event was successfully processed,
     *         {@code false} otherwise.
     */
    protected boolean onTouch() {
        return false;
    }

    /**
     * Called when the {@link Widget}'s transform is altered, whether by
     * scaling, rotation, or translation. Flags the {@code Widget} for layout
     * (client code will still have to call {@link #requestLayout()} to initiate
     * the layout cycle) and invalidates its {@linkplain #getBoundingBox()
     * bounding box}.
     * <p>
     * Deriving classes that override this method <em>really</em> need to call
     * {@code super}: otherwise the {@code Widget} won't get laid out until
     * somebody explicitly calls {@code requestLayout()} on it, and the cached
     * bounding box will get out of date.
     */
    protected void onTransformChanged() {
        Log.v(Log.SUBSYSTEM.WIDGET, TAG, "onTransformChanged(): %s mPreventTransformChanged = %b",
                getName(), mPreventTransformChanged);

        // Even if the calling code that altered the transform doesn't request a
        // layout, we'll do a layout the next time a layout is requested on our
        // part of the scene graph.
        if (!mPreventTransformChanged) {
            mChanged = true;

            // Clear this to indicate that the bounding box has been invalidated and
            // needs to be constructed and transformed anew.
            mBoundingBox = null;
        }
    }

    private boolean mPreventTransformChanged;
    void preventTransformChanged(boolean prevent) {
        mPreventTransformChanged = prevent;
    }

    /* package */
    boolean addChildInner(final Widget child,
            final GVRSceneObject childRootSceneObject, int index) {
        if (child == this || child.getSceneObject() == getSceneObject()) {
            Log.e(TAG, "Attempted to add widget '%s' to itself!", getName());
            throw RuntimeAssertion("Attempted to add widget '%s' to itself!", getName());
        }
        final boolean added = mChildren.indexOf(child) == -1;
        if (added) {
            Widget parent = child.getParent();
            if (parent != null) {
                parent.removeChild(child, child.getSceneObject(), true);
            }
            if (index == -1 || index > mChildren.size()) {
                mChildren.add(child);
            } else {
                mChildren.add(index, child);
            }
            if (child.getVisibility() == Visibility.VISIBLE &&
                child.getViewPortVisibility() != ViewPortVisibility.INVISIBLE) {
                final GVRSceneObject childRootSceneObjectParent = childRootSceneObject.getParent();
                if (childRootSceneObjectParent != getSceneObject()) {
                    if (null != childRootSceneObjectParent) {
                        childRootSceneObjectParent.removeChildObject(childRootSceneObject);
                    }
                    getSceneObject().addChildObject(childRootSceneObject);
                } else {
                    Log.v(Log.SUBSYSTEM.WIDGET, TAG,
                          "addChildInner(): child '%s' already attached to this Group ('%s')",
                          child.getName(), getName());
                }
            } else {
                Log.v(TAG,
                      "addChildInner(): child '%s' is not visible visibility = %s, mIsVisibleInViewPort = %s ",
                      child.getName(), child.getVisibility(), child.getViewPortVisibility());
            }
            child.doOnAttached(this);

            final List<OnHierarchyChangedListener> listeners;
            synchronized (mOnHierarchyChangedListeners) {
                listeners = new ArrayList<>(mOnHierarchyChangedListeners);
            }
            for (OnHierarchyChangedListener listener : listeners) {
                listener.onChildWidgetAdded(this, child);
            }
        }

        if (mClippingEnabled) {
            setObjectClipped(child);
        }

        return added;
    }

    void checkTransformChanged() {
        if (mTransformCache.save(this, true)) {
            onTransformChanged();
        }
    }

    /* package */
    Widget createChild(GVRContext context, GVRSceneObject sceneObjectChild)
            throws InstantiationException {
        final Widget child = WidgetFactory.createWidget(sceneObjectChild);
        return child;
    }

    /* package */
    void createChildren(final GVRContext context,
            final GVRSceneObject sceneObject) throws InstantiationException {
        Log.d(Log.SUBSYSTEM.WIDGET, TAG, "createChildren(%s): creating children", getName());
        List<GVRSceneObject> children = sceneObject.getChildren();
        Log.d(Log.SUBSYSTEM.WIDGET, TAG, "createChildren(%s): child count: %d", getName(), children.size());
        for (GVRSceneObject sceneObjectChild : children) {
            Log.d(Log.SUBSYSTEM.WIDGET, TAG, "createChildren(%s): creating child '%s'", getName(),
                  sceneObjectChild.getName());
            final Widget child = createChild(context, sceneObjectChild);
            if (child != null) {
                addChildInner(child);
            }
        }
    }

    protected boolean addOnHierarchyChangedListener(OnHierarchyChangedListener listener) {
        return mOnHierarchyChangedListeners.add(listener);
    }

    protected boolean removeOnHierarchyChangedListener(OnHierarchyChangedListener listener) {
        return mOnHierarchyChangedListeners.remove(listener);
    }

    public Widget findChildByName(final String name) {
        final List<Widget> groups = new ArrayList<>();
        groups.add(this);

        return findChildByNameInAllGroups(name, groups);
    }

    /* package */
    List<Widget> getChildren() {
        return new ArrayList<>(mChildren);
    }

    /* package */
    boolean hasChild(final Widget child) {
        return mChildren.contains(child);
    }

    /* package */
    int indexOfChild(final Widget child) {
        return mChildren.indexOf(child);
    }

    /**
     * Add another {@link Widget} as a child of this one. Convenience method for
     * {@link #addChild(Widget, int, boolean) addChild(child, -1, false)}.
     *
     * @param child
     *            The {@code Widget} to add as a child.
     * @return {@code True} if {@code child} was added; {@code false} if
     *         {@code child} was previously added to this instance.
     */
    protected boolean addChild(final Widget child) {
        return addChild(child, -1, false);
    }

    /**
     * Add another {@link Widget} as a child of this one. Convenience method for
     * {@link #addChild(Widget, int, boolean) addChild(child, index, false)}.
     *
     * @param child
     *            The {@code Widget} to add as a child.
     * @param index
     *            Position at which to add the child. Pass -1 to add at end.
     * @return {@code True} if {@code child} was added; {@code false} if
     *         {@code child} was previously added to this instance.
     */
    protected boolean addChild(final Widget child, int index) {
        return addChild(child, index, false);
    }

    /**
     * Add another {@link Widget} as a child of this one. Convenience method for
     * {@link #addChild(Widget, int, boolean) addChild(child, -1, preventLayout)}.
     *
     * @param child
     *            The {@code Widget} to add as a child.
     * @param preventLayout
     *            The {@code Widget} whether to call layout().
     * @return {@code True} if {@code child} was added; {@code false} if
     *         {@code child} was previously added to this instance.
     */
    protected boolean addChild(Widget child, boolean preventLayout) {
        return addChild(child, -1, preventLayout);
    }

    /**
     * Add another {@link Widget} as a child of this one. Overload to intercept all child adds.
     *
     * @param child
     *            The {@code Widget} to add as a child.
     * @param index
     *            Position at which to add the child. Pass -1 to add at end.
     * @param preventLayout
     *            The {@code Widget} whether to call layout().
     * @return {@code True} if {@code child} was added; {@code false} if
     *         {@code child} was previously added to this instance.
     */
    protected boolean addChild(Widget child, int index, boolean preventLayout) {
        return addChild(child, child.getSceneObject(), index, preventLayout);
    }

    /**
     * Add another {@link Widget} as a child of this one. Convenience method for
     * {@link #addChild(Widget, GVRSceneObject, int, boolean) addChild(child, childRootSceneObject,
     * -1, false)}.
     *
     * @param child
     *            The {@code Widget} to add as a child.
     * @param childRootSceneObject
     *            The root {@link GVRSceneObject} of the child.
     * @return {@code True} if {@code child} was added; {@code false} if
     *         {@code child} was previously added to this instance.
     */
    protected boolean addChild(final Widget child,
                               final GVRSceneObject childRootSceneObject) {
        return addChild(child, childRootSceneObject, -1, false);
    }

    /**
     * Add another {@link Widget} as a child of this one. Convenience method for
     * {@link #addChild(Widget, GVRSceneObject, int, boolean) addChild(child, childRootSceneObject,
     * index, false)}.
     *
     * @param child
     *            The {@code Widget} to add as a child.
     * @param childRootSceneObject
     *            The root {@link GVRSceneObject} of the child.
     * @param index
     *            Position at which to add the child. Pass -1 to add at end.
     * @return {@code True} if {@code child} was added; {@code false} if
     *         {@code child} was previously added to this instance.
     */
    protected boolean addChild(final Widget child,
            final GVRSceneObject childRootSceneObject, final int index) {
        return addChild(child, childRootSceneObject, index, false);
    }

    /**
     * Add another {@link Widget} as a child of this one. Convenience method for
     * {@link #addChild(Widget, GVRSceneObject, int, boolean) addChild(child, childRootSceneObject,
     * -1, preventLayout)}.
     *
     * @param child
     *            The {@code Widget} to add as a child.
     * @param childRootSceneObject
     *            The root {@link GVRSceneObject} of the child.
     * @param preventLayout
     *            The {@code Widget} whether to call layout().
     * @return {@code True} if {@code child} was added; {@code false} if
     *         {@code child} was previously added to this instance.
     */
    protected boolean addChild(final Widget child,
            final GVRSceneObject childRootSceneObject, boolean preventLayout) {
        return addChild(child, childRootSceneObject, -1, preventLayout);
    }

    /**
     * Add another {@link Widget} as a child of this one.
     * <p>
     * A {@link GVRSceneObject} other than the one directly managed by the child
     * {@code Widget} can be specified as the child's root. This is useful in
     * cases where the parent object needs to insert additional scene objects
     * between the child and its parent.
     * <p>
     * <b>NOTE:</b> it is the responsibility of the caller to keep track of the
     * relationship between the child {@code Widget} and the alternative root
     * scene object.
     *
     * @param child
     *            The {@code Widget} to add as a child.
     * @param childRootSceneObject
     *            The root {@link GVRSceneObject} of the child.
     * @param index
     *            Position at which to add the child. Pass -1 to add at end.
     * @param preventLayout
     *            The {@code Widget} whether to call layout().
     * @return {@code True} if {@code child} was added; {@code false} if
     *         {@code child} was previously added to this instance.
     */
    protected boolean addChild(final Widget child,
                               final GVRSceneObject childRootSceneObject, final int index,
                               boolean preventLayout) {
        final boolean added = addChildInner(child, childRootSceneObject, index);
        Log.d(Log.SUBSYSTEM.WIDGET, TAG, "addChild [%s] %b", child, added);
        if (added) {
            onTransformChanged();
            if (!preventLayout) {
                invalidateAllLayouts();
                requestLayout();
            }
        }
        return added;
    }

    /**
     * Remove a {@link Widget} as a child of this instance. Convenience method for
     * {@link #removeChild(Widget, boolean) removeChild(child, false)}.
     *
     * @param child
     *            The {@code Widget} to remove.
     * @return {@code True} if {@code child} was a child of this instance and
     *         was successfully removed; {@code false} if {@code child} is not a
     *         child of this instance.
     */
    protected boolean removeChild(final Widget child) {
        return removeChild(child, false);
    }

    /**
     * Remove a {@link Widget} as a child of this instance. Overload to intercept all child removes.
     *
     * @param child
     *            The {@code Widget} to remove.
     * @param preventLayout
     *            Tell the {@code Widget} whether to layout after removal.
     * @return {@code True} if (@code child} was a child of this instance and
     *         was successfully removed; {@code false} if {@code child} is not a
     *         child of this instance.
     */
    protected boolean removeChild(Widget child, boolean preventLayout) {
        return removeChild(child, child.getSceneObject(), preventLayout);
    }

    /**
     * Remove a {@link Widget} as a child of this instance.
     * <p>
     * <b>NOTE:</b> if an alternative root scene object was used to
     * {@linkplain #addChild(Widget, GVRSceneObject) add} the child
     * {@code Widget}, the caller must pass the alternative root to this method.
     * Otherwise there may be dangling scene objects. It is the responsibility
     * of the caller to keep track of the relationship between the child
     * {@code Widget} and the alternative root scene object.
     *
     * @param child
     *            The {@code Widget} to remove.
     * @param childRootSceneObject
     * @return {@code True} if {@code child} was a child of this instance and
     *         was successfully removed; {@code false} if {@code child} is not a
     *         child of this instance.
     */
    protected boolean removeChild(final Widget child,
                                  final GVRSceneObject childRootSceneObject) {
        return removeChild(child, childRootSceneObject, false);
    }

    /**
     * Remove a {@link Widget} as a child of this instance.
     * <p>
     * <b>NOTE:</b> if an alternative root scene object was used to
     * {@linkplain #addChild(Widget, GVRSceneObject) add} the child
     * {@code Widget}, the caller must pass the alternative root to this method.
     * Otherwise there may be dangling scene objects. It is the responsibility
     * of the caller to keep track of the relationship between the child
     * {@code Widget} and the alternative root scene object.
     *
     * @param child
     *            The {@code Widget} to remove.
     * @param childRootSceneObject
     * @param preventLayout
     *            The {@code Widget} whether to call layout().
     * @return {@code True} if {@code child} was a child of this instance and
     *         was successfully removed; {@code false} if {@code child} is not a
     *         child of this instance.
     */
    protected boolean removeChild(final Widget child,
            final GVRSceneObject childRootSceneObject, boolean preventLayout) {
        final boolean removed = mChildren.remove(child);

        Log.d(Log.SUBSYSTEM.WIDGET, TAG, "removeChild [%s] %b", child, removed);
        if (removed) {
            Log.d(Log.SUBSYSTEM.WIDGET, TAG, "removeChild(): '%s' removed", child.getName());
            if (childRootSceneObject.getParent() != getSceneObject()) {
                Log.e(Log.SUBSYSTEM.WIDGET, TAG,
                      "removeChild(): '%s' is not a child of '%s' GVRSceneObject!",
                      child.getName(), getName());
            }
            getSceneObject().removeChildObject(childRootSceneObject);
            child.doOnDetached();

            final List<OnHierarchyChangedListener> listeners;
            synchronized (mOnHierarchyChangedListeners) {
                listeners = new ArrayList<>(mOnHierarchyChangedListeners);
            }
            for (OnHierarchyChangedListener listener : listeners) {
                listener.onChildWidgetRemoved(this, child);
            }

            onTransformChanged();
            if (!preventLayout) {
                invalidateAllLayouts();
                requestLayout();
            }
        } else {
            Log.w(TAG, "removeChild(): '%s' is not a child of '%s'!",
                  child.getName(), getName());
        }
        return removed;
    }

    /* package */
    GVRRenderData getRenderData() {
        return mSceneObject.getRenderData();
    }

    /* package */
    // NOTE: If you find yourself wanting to make this public, don't! You're
    // either working *against* Widget or Widget needs some extending.
    GVRSceneObject getSceneObject() {
        return mSceneObject;
    }

    /* package */
    GVRTransform getTransform() {
        return mSceneObject.getTransform();
    }

    public Widget(final GVRContext context, @NonNull final JSONObject properties) {
        this(context, properties, true);
    }

    /**
     * Core {@link Widget} constructor.
     *
     * @param context A valid {@link GVRContext}.
     * @param properties A structured set of properties for the {@code Widget} instance. See
     *                       {@code widget.json} for schema.
     * @param copyProperties Properties which are passed in from client code are copied so that they
     *                       cannot be modified after we receive them.
     */

    private Widget(final GVRContext context, @NonNull JSONObject properties, boolean copyProperties) {
        if (properties != null) {
            if (copyProperties) {
                properties = copy(properties);
            }
        } else {
            properties = new JSONObject();
        }

        initMetadata(properties);

        mContext = context;
        try {
            final JSONObject metadata = getObjectMetadata();
            mSceneObject = getSceneObjectProperty(context, metadata);

            Log.v(Log.SUBSYSTEM.WIDGET, TAG,
    	            "Widget(context, properties): %s (%s) width = %f height = %f depth = %f",
    	            getName(), mSceneObject.getName(), getWidth(), getHeight(), getDepth());

	        mTransformCache = new TransformCache(getTransform());

            Log.d(Log.SUBSYSTEM.WIDGET, TAG,
                    "Widget(context, properties): setting up metadata for %s: %s",
                    getName(), metadata);
            setupProperties(metadata);
            createChildren(context, mSceneObject);
            setupStatesAndLevels(metadata);
        } catch (Exception e) {
            Log.e(TAG, e, "Widget(): DANGER WILL ROBINSON DANGER");
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    private GVRSceneObject getSceneObjectProperty(GVRContext context, final JSONObject properties) {
        Log.d(Log.SUBSYSTEM.WIDGET, TAG, "getSceneObjectProperty(%s): called ", getName());
        GVRSceneObject sceneObject = opt(properties, Properties.scene_object, GVRSceneObject.class);
        if (sceneObject == null) {
            if (hasJSONObject(properties, Properties.model)) {
                JSONObject modelSpec = optJSONObject(properties, Properties.model);
                Log.d(Log.SUBSYSTEM.WIDGET, TAG, "getSceneObjectProperty(%s): model specified: %s", getName(), modelSpec);
                String id = getString(modelSpec, Properties.id);
                sceneObject = loadSceneObjectFromModel(context, id);
            } else if (hasFloat(properties, Properties.size)) {
                float size = optFloat(properties, Properties.size);
                Log.d(Log.SUBSYSTEM.WIDGET, TAG, "getSceneObjectProperty(%s): specified only size: %f", getName(), size);
                sceneObject = makeQuad(context, size, size);
            } else if (hasPoint(properties, Properties.size)) {
                PointF size = optPointF(properties, Properties.size);
                Log.d(Log.SUBSYSTEM.WIDGET, TAG, "getSceneObjectProperty(%s): specified size: %s", getName(), size);
                sceneObject = makeQuad(context, size.x, size.y);
            } else {
                Log.d(Log.SUBSYSTEM.WIDGET, TAG, "getSceneObjectProperty(%s): empty object!", getName());
                // TODO: Ideally, we wouldn't create a mesh here, but if we don't, things hang
                sceneObject = new GVRSceneObject(context, 0, 0);
                setupDefaultMaterial(context, sceneObject);
            }
        } else {
            Log.d(Log.SUBSYSTEM.WIDGET, TAG, "getSceneObjectProperty(%s): got a scene object: %s", getName(), sceneObject);
        }

        if (sceneObject != null  && mName != null) {
            sceneObject.setName(mName);
        }
        // TODO: Add support for specifying mesh
        // TODO: Add support for specifying a primitive (quad, rounded_quad, sphere, cylinder, etc.)
        return sceneObject;
    }

    /**
     * Does post-{@linkplain GroupWidget#addChild(Widget) attachment} setup:
     * <ul>
     * <li>Runs GL thread {@linkplain #create() initialization}</li>
     * <li>Registers for touch and focus notifications, if they are enabled</li>
     * <li>Invokes {@link #onAttached()}
     * </ul>
     *
     * @param parent
     *            The {@link GroupWidget} this instance is being
     *            {@linkplain GroupWidget#addChild(Widget) attached} to.
     */
    private synchronized final void doOnAttached(final Widget parent) {
        if (parent != mParent) {
            mParent = parent;
            create();
            registerPickable();
            onAttached();
        }
    }

    private boolean doOnBackKey() {
        for (OnBackKeyListener listener : mBackKeyListeners) {
            if (listener.onBackKey(this)) {
                return true;
            }
        }
        return onBackKey();
    }

    private void doOnCreate() {
        final int level = mLevel;
        mLevel = -1;
        setLevel(level);

        onCreate();
    }

    /**
     * Does post-{@linkplain GroupWidget#removeChild(Widget) detachment}
     * cleanup:
     * <ul>
     * <li>Clears parent reference</li>
     * <li>Unregisters for touch and focus notifications</li>
     * <li>Invokes {@link #onDetached()}</li>
     * </ul>
     */
    private synchronized final void doOnDetached() {
        mParent = null;
        registerPickable();
        onDetached();
    }

    /**
     * Called when this {@link Widget} gains line-of-sight focus. Notifies all
     * {@linkplain OnFocusListener#onFocus(boolean) listeners}; if none of the
     * listeners has completely handled the event, {@link #onFocus(boolean)} is
     * called.
     */
    private boolean doOnFocus(boolean focused) {
        if (!mFocusEnabled) {
            return false;
        }

        final boolean oldFocus = mIsFocused;

        Log.v(Log.SUBSYSTEM.WIDGET, TAG, "doOnFocus(%s): mIsFocused: %b, focused: %b", getName(),
                mIsFocused, focused);


        final List<OnFocusListener> focusListeners;
        synchronized (mFocusListeners) {
            focusListeners = new ArrayList<OnFocusListener>(mFocusListeners);
        }

        for (OnFocusListener listener : focusListeners) {
            if (listener.onFocus(this, focused)) {
                return true;
            }
        }
        final boolean tookFocus = onFocus(focused);
        Log.d(Log.SUBSYSTEM.WIDGET, TAG, "doOnFocus(%s): tookFocus: %b", getName(), tookFocus);
        if (focused) {
            // onFocus() can refuse to take focus
            mIsFocused = tookFocus;
        } else {
            // But when we lose focus, we don't get a choice about it
            mIsFocused = focused;
        }
        updateState();
        if (oldFocus != mIsFocused) {
            final boolean inFollowFocusGroup = isInFollowFocusGroup();
            for (Widget child : getChildren()) {
                if (child.mFocusEnabled
                        && child.isFocused() != focused
                        && (mChildrenFollowFocus || child.mFollowParentFocus || inFollowFocusGroup)) {
                    child.doOnFocus(mIsFocused);
                }
            }
        }
        return tookFocus;
    }

    /**
     * Called when this {@link Widget} has had line-of-sight focus for more than
     * {@link #getLongFocusTime()} milliseconds. Notifies all
     * {@linkplain OnFocusListener#onLongFocus() listeners}; if none of the
     * listeners has completely handled the event, {@link #onLongFocus()} is
     * called.
     */
    private void doOnLongFocus() {
        final List<OnFocusListener> focusListeners;
        synchronized (mFocusListeners) {
            focusListeners = new ArrayList<OnFocusListener>(mFocusListeners);
        }

        for (OnFocusListener listener : focusListeners) {
            if (listener.onLongFocus(this)) {
                return;
            }
        }
        onLongFocus();
        final boolean inFollowFocusGroup = isInFollowFocusGroup();
        for (Widget child : getChildren()) {
            if (child.mFocusEnabled
                    && (mChildrenFollowFocus || child.mFollowParentFocus || inFollowFocusGroup)) {
                child.doOnLongFocus();
            }
        }
    }

    private boolean doOnTouch(final float[] coords) {
        OnTouchListener[] listenersCopy = new OnTouchListener[mTouchListeners.size()];
        mTouchListeners.toArray(listenersCopy);

        for (OnTouchListener listener : listenersCopy) {
            if (listener.onTouch(this, coords)) {
                return true;
            }
        }

        final boolean acceptedTouch = onTouch();
        if (acceptedTouch) {
            final boolean inFollowInputGroup = isInFollowInputGroup();
            for (Widget child : getChildren()) {
                if (child.isTouchable()
                        && (mChildrenFollowInput
                                || child.getFollowParentInput() || inFollowInputGroup)) {
                    child.doOnTouch(coords);
                }
            }
        }

        return acceptedTouch;
    }

    private boolean addChildInner(final Widget child) {
        return addChildInner(child, child.getSceneObject(), -1);
    }

    /**
     * Searches the immediate children of {@link GroupWidget groupWidget} for a
     * {@link Widget} with the specified {@link Widget#getName() name}.
     * <p>
     * Any non-matching {@code GroupWidget} children iterated prior to finding a
     * match will be added to {@code groupChildren}. If no match is found, all
     * immediate {@code GroupWidget} children will be added.
     *
     * @param name
     *            The name of the {@code Widget} to find.
     * @param groupWidget
     *            The {@code GroupWidget} to search.
     * @param groupChildren
     *            Output array for non-matching {@code GroupWidget} children.
     * @return The first {@code Widget} with the specified name or {@code null}
     *         if no child of {@code groupWidget} has that name.
     */
    private static Widget findChildByNameInOneGroup(final String name,
            final Widget groupWidget, ArrayList<Widget> groupChildren) {
        Collection<Widget> children = groupWidget.getChildren();
        for (Widget child : children) {
            if (child.getName() != null && child.getName().equals(name)) {
                return child;
            }
            if (child instanceof GroupWidget) {
                // Save the child for the next level of search if needed.
                groupChildren.add(child);
            }
        }
        return null;
    }

    /**
     * Performs a breadth-first search of the {@link GroupWidget GroupWidgets}
     * in {@code groups} for a {@link Widget} with the specified
     * {@link Widget#getName() name}.
     *
     * @param name
     *            The name of the {@code Widget} to find.
     * @param groups
     *            The {@code GroupWidgets} to search.
     * @return The first {@code Widget} with the specified name or {@code null}
     *         if no child of {@code groups} has that name.
     */
    private static Widget findChildByNameInAllGroups(final String name,
            List<Widget> groups) {
        if (groups.isEmpty()) {
            return null;
        }

        ArrayList<Widget> groupChildren = new ArrayList<>();
        Widget result;
        for (Widget group : groups) {
            // Search the immediate children of 'groups' for a match, rathering
            // the children that are GroupWidgets themselves.
            result = findChildByNameInOneGroup(name, group, groupChildren);
            if (result != null) {
                return result;
            }
        }

        // No match; Search the children that are GroupWidgets.
        return findChildByNameInAllGroups(name, groupChildren);
    }

    private BoundingBox getBoundingBoxInternal() {
        if (mBoundingBox == null) {
            mBoundingBox = new BoundingBox(this);
        }
        return mBoundingBox;
    }

    private interface HandlesEvent {
        boolean isInFollowEventGroup();

        boolean getChildrenFollowEvent();

        boolean followsParentEvent(Widget widget);

        boolean handlesEvent(Widget widget, GVRSceneObject sceneObject);

        String getName();
    }

    private boolean handlesEventFor(final GVRSceneObject sceneObject,
            final HandlesEvent handler) {
        if (getSceneObject() == sceneObject) {
            final boolean handlesEvent = !handler.followsParentEvent(this)
                    && !handler.isInFollowEventGroup();
            Log.d(Log.SUBSYSTEM.WIDGET, TAG, "handlesEventFor(%s): handles '%s' for scene object %s",
                  getName(), handler.getName(), sceneObject.getName());
            return handlesEvent;
        } else {
            final boolean childrenFollowEvent = handler
                    .getChildrenFollowEvent();
            for (Widget child : getChildren()) {
                if ((childrenFollowEvent || handler.followsParentEvent(child))
                        && (child.isSceneObject(sceneObject) || handler
                                .handlesEvent(child, sceneObject))) {
                    Log.d(Log.SUBSYSTEM.WIDGET, TAG,
                          "handlesEventFor(%s): handles '%s' for child '%s'",
                          getName(), handler.getName(), child.getName());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return {@code true} if one of this {@link Widget}'s ancestors has
     *         {@linkplain #setChildrenFollowFocus(boolean) children follow
     *         focus} set, {@code false} if not.
     */
    private boolean isInFollowFocusGroup() {
        return mFocusableImpl != null
                && mParent != null
                && mFocusableImpl.target() != this
                && (mParent.mChildrenFollowFocus || mFocusableImpl.target() != mParent);
    }

    /**
     * @return {@code true} if one of this {@link Widget}'s ancestors has
     *         {@linkplain #setChildrenFollowInput(boolean) children follow
     *         input} set, {@code false} if not.
     */
    private boolean isInFollowInputGroup() {
        return mTouchHandler != null
                && mParent != null
                && mTouchHandler.target() != this
                && (mParent.mChildrenFollowInput || mTouchHandler.target() != mParent);
    }

    /**
     * @return {@code true} is one of this {@link Widget}'s ancestors has
     *         {@linkplain #setChildrenFollowState(boolean) children follow
     *         state set, {@code false} if not.
     */
    private boolean isInFollowStateGroup() {
        return mParent != null && mParent.mChildrenFollowState;
    }

    private boolean needsOwnFocusable() {
        return mFocusableImpl.target() != this;
    }

    private boolean needsOwnTouchHandler() {
        return mTouchHandler.target() != this;
    }

    private void registerPickable() {
        final TouchManager touchManager = TouchManager.get(getGVRContext());
        if (touchManager == null) {
            Log.e(TAG,
                  "Attempted to register widget as touchable with NULL TouchManager!");
            return;
        }

        final boolean hasRenderData = getRenderData() != null;
        final FocusManager focusManager = FocusManager.get(mContext);
        final TouchManager.OnTouch currentTouchHandler = mTouchHandler;
        final FocusManager.Focusable currentFocusable = mFocusableImpl;

        final boolean needsOwnFocusable = needsOwnFocusable();
        if (useParentFocusable()) {
            mFocusableImpl = mParent.mFocusableImpl;
        } else if (needsOwnFocusable) {
            mFocusableImpl = new FocusableImpl();
        }

        if (useParentTouchHandler()) {
            mTouchHandler = mParent.mTouchHandler;
        } else if (needsOwnTouchHandler()) {
            mTouchHandler = new OnTouchImpl();
        }

        if (mParent != null && hasRenderData && (mIsTouchable || mFocusEnabled)) {
            if (mIsTouchable) {
                touchManager.makeTouchable(getSceneObject(), mTouchHandler);
            } else {
                touchManager.makePickable(getSceneObject());
            }

            if (mFocusEnabled) {
                focusManager.register(getSceneObject(), mFocusableImpl);
            } else {
                Log.d(Log.SUBSYSTEM.WIDGET, TAG, "registerPickable(): '%s' is not focus-enabled",
                      getName());
                focusManager.unregister(getSceneObject(), needsOwnFocusable);
            }
        } else {
            touchManager.removeHandlerFor(getSceneObject());
            Log.d(Log.SUBSYSTEM.WIDGET, TAG,
                  "registerPickable(): unregistering '%s'; focus-enabled: %b",
                  getName(), mFocusEnabled);
            focusManager.unregister(getSceneObject(), needsOwnFocusable);
        }

        // If our focusable or touch handler have changed, we need to let any
        // children that are part of the same focus/input group or might be
        // following this widget know
        if (currentFocusable != mFocusableImpl
                || currentTouchHandler != mTouchHandler) {
            for (Widget child : getChildren()) {
                child.registerPickable();
            }
        }
    }

    public enum Properties {
        name, touchable, focusenabled, id, visibility, states, levels, level, model, selected,
        scene_object, preapply_attribs, size, transform, viewport
    }

    public enum TransformProperties {
        position, scale, rotation, pivot, angle
    }

    private static void loadAnimations(Context context) throws JSONException, NoSuchMethodException {
        JSONObject json = JSONHelpers.loadJSONAsset(context, "animations.json");
        if (json != null) {
            JSONObject animationMetadata = json.optJSONObject("animations");
            AnimationFactory.init(animationMetadata);
            Log.v(Log.SUBSYSTEM.WIDGET, TAG, "loadAnimations(): loaded animation metadata: %s",
                    animationMetadata);
        } else {
            Log.w(Log.SUBSYSTEM.JSON, TAG, "loadAnimations(): no animations.json");
        }
    }

    private void initMetadata(JSONObject properties) {
        setName(optString(properties, Properties.name, getName()));

        Log.v(Log.SUBSYSTEM.WIDGET, TAG, "initMetadata(%s): properties: %s", getName(), properties);
        UnmodifiableJSONObject objectMetadata = PropertyManager.get().getWidgetProperties(this);
        Log.v(Log.SUBSYSTEM.WIDGET, TAG, "initMetadata(%s): objectMetadata: %s", getName(), objectMetadata);
        final boolean preApplyAttribs = optBoolean(properties, Properties.preapply_attribs);
        Log.v(Log.SUBSYSTEM.WIDGET, TAG, "initMetadata(%s): preApplyAttribs: %b", getName(), preApplyAttribs);
        if (preApplyAttribs) {
            // Allow JSON metadata to overwrite metadata from the model
            mMetadata = merge(objectMetadata, properties);
        } else {
            mMetadata = merge(properties, objectMetadata);
        }

        // We do this a second time because the properties received by initMetadata() may have been
        // overwritten if they were pre-applied
        setName(optString(mMetadata, Properties.name, getName()));

        Log.v(Log.SUBSYSTEM.WIDGET, TAG, "initMetadata(%s): merged metadata: %s", getName(), mMetadata);
    }

    private void setupProperties(JSONObject properties) {
        final boolean hasRenderData = getRenderData() != null;
        mIsTouchable = hasRenderData && optBoolean(properties, Properties.touchable,
                mIsTouchable);
        mFocusEnabled = optBoolean(properties, Properties.focusenabled,
                mFocusEnabled);
        mIsSelected = hasRenderData && optBoolean(properties, Properties.selected, mIsSelected);
        Visibility visibility = optEnum(properties, Properties.visibility,
                mVisibility, true);
        setVisibility(visibility);

        // Set up transform positioning
        Log.d(Log.SUBSYSTEM.WIDGET, TAG, "setupProperties(%s): %s", getName(), properties);
        Vector3f position = optVector3f(properties, TransformProperties.position);
        Log.d(Log.SUBSYSTEM.WIDGET, TAG, "setupProperties(%s): position: %s", getName(), position);
        if (position != null) {
            setPosition(position.x, position.y, position.z);
        }

        // Set up transform scaling
        if (hasVector3f(properties, TransformProperties.scale)) {
            Vector3f scale = optVector3f(properties, TransformProperties.scale);
            Log.d(Log.SUBSYSTEM.WIDGET, TAG, "setupProperties(%s): scale: %s", getName(), scale);
            if (scale != null) {
                setScale(scale.x, scale.y, scale.z);
            }
        } else if (hasNumber(properties, TransformProperties.scale)) {
            final float scale = optFloat(properties, TransformProperties.scale, 1);
            Log.d(Log.SUBSYSTEM.WIDGET, TAG, "setupProperties(%s): scale: %.2f", getName(), scale);
            setScale(scale);
        }

        // Set up transform rotation
        JSONObject rotation = optJSONObject(properties, TransformProperties.rotation);
        if (rotation != null) {
            Vector3f scalars = asVector3f(rotation, new Vector3f(1, 1, 1));
            float angle;
            angle = getFloat(rotation, TransformProperties.angle);
            if (hasVector3f(rotation, TransformProperties.pivot)) {
                Vector3f pivot = optVector3f(rotation, TransformProperties.pivot);
                rotateByAxisWithPivot(angle, scalars.x, scalars.y, scalars.z, pivot.x, pivot.y, pivot.z);
            } else {
                rotateByAxis(angle, scalars.x, scalars.y, scalars.z);
            }
        }

        // Setup viewport
        Vector3f viewport = optVector3f(properties, Properties.viewport);
        if (viewport != null) {
            mViewPort = new Vector3Axis(viewport);
        } else {
            mViewPort = new Vector3Axis(getWidth(), getHeight(), getDepth());
        }
    }

    private void setupStatesAndLevels(JSONObject metaData) throws JSONException, NoSuchMethodException {
        final boolean hasStates = has(metaData, Properties.states);
        final boolean hasLevels = has(metaData, Properties.levels);
        final boolean hasLevel = has(metaData, Properties.level);
        Log.d(Log.SUBSYSTEM.WIDGET, TAG,
                "setupStatesAndLevels(): for '%s'; states: %b, levels %b, level %b",
                getName(), hasStates, hasLevels, hasLevel);
        if (hasStates) {
            if (hasLevels || hasLevel) {
                throw RuntimeAssertion("Invalid metadata for '%s': both 'states' and 'levels' are present",
                        getName());
            }
            setupStates(metaData);
        } else if (hasLevels) {
            if (hasLevel) {
                mLevel = getInt(metaData, Properties.level);
                setupLevels(metaData);
            }
        } else if (hasLevel) {
            throw RuntimeAssertion("Invalid metadata for '%s': 'level' specified without level specifications",
                    getName());
        }
    }

    private void setupLevels(JSONObject metaData) throws JSONException,
            NoSuchMethodException {
        JSONArray levelsArray = optJSONArray(metaData, Properties.levels);

        if (levelsArray != null) {
            Log.d(Log.SUBSYSTEM.WIDGET, TAG, "setupLevels(): for %s", getName());
            for (int i = 0; i < levelsArray.length(); ++i) {
                mLevelInfo.add(new WidgetState(this, levelsArray
                        .getJSONObject(i)));
            }
        } else {
            Log.d(Log.SUBSYSTEM.WIDGET, TAG, "setupLevels(): No levels metadata for %s", getName());
        }
    }

    private void setupStates(JSONObject metadata) throws JSONException,
            NoSuchMethodException {
        JSONObject states = optJSONObject(metadata, Properties.states);
        Log.d(Log.SUBSYSTEM.WIDGET, TAG, "setupStates(): for '%s': %s", getName(), states);
        mLevelInfo.add(new WidgetState(this, states));
    }

    /* package : Called by CheckableButton to update CHECKED state */
    // NOT protected because states are defined internally and are not for
    // external consumption
    void updateState() {
        final WidgetState.State state;
        if (useParentState() || getFollowParentState()) {
            state = mParent.getState();
        } else {
            state = getState();
        }

        Log.d(Log.SUBSYSTEM.WIDGET, TAG, "updateState(): %s for '%s'", state, getName());
        if (!mLevelInfo.isEmpty() && mLevel >= 0) {
            mLevelInfo.get(mLevel).setState(this, state);
        }

        boolean updateChildren = isInFollowStateGroup()
                || getChildrenFollowState();
        for (Widget child : getChildren()) {
            if (updateChildren || child.getFollowParentState()) {
                child.updateState();
            }
        }
    }

    /* package : Overridden by CheckableButton to return CHECKED state */
    // NOT protected because states are defined internally and are not for
    // external consumption
    WidgetState.State getState() {
        final WidgetState.State state;
        if (mIsPressed) {
            state = WidgetState.State.PRESSED;
        } else if (mIsSelected) {
            state = WidgetState.State.SELECTED;
        } else if (mIsFocused) {
            state = WidgetState.State.FOCUSED;
        } else {
            state = WidgetState.State.NORMAL;
        }
        return state;
    }

    private boolean useParentFocusable() {
        return mParent != null
                && (mFollowParentFocus || mParent.mChildrenFollowFocus || mParent
                        .isInFollowFocusGroup());
    }

    private boolean useParentTouchHandler() {
        return mParent != null
                && (mFollowParentInput || mParent.mChildrenFollowInput || mParent
                        .isInFollowInputGroup());
    }

    private boolean useParentState() {
        return mParent != null
                && (mFollowParentState || mParent.mChildrenFollowState || mParent
                        .isInFollowStateGroup());
    }

    private final class OnTouchImpl implements TouchManager.OnTouch {
        @Override
        public boolean touch(GVRSceneObject sceneObject, final float[] coords) {// , float[] hit) {
            return doOnTouch(coords);
        }

        @Override
        public boolean onBackKey(GVRSceneObject sceneObject, final float[] coords) {
            return doOnBackKey();
        }

        public Widget target() {
            return Widget.this;
        }
    }

    /**
     * Override to provide a default layout for deriving {@link Widget Widgets}.  If no layout has
     * been {@linkplain #applyLayout(Layout) applied} by the time {@link #create()} is called, this
     * method will be called to provide a default layout.
     * <p>
     *     <strong>NOTE:</strong> this means that if <em>any</em> layout has been applied prior to
     *     {@code create()} being called, this method will <em>not be called</em>. Therefore, if
     *     you want to apply an layout <em>in addition</em> to the default layout, you must do so
     *     after {@code create()} has been called ({@link #onAttached()} is a good place) <em>or</em>
     *     you must add the default layout explicitly.
     * </p>
     * <p>
     *     The default layout is handled this way because in the most common case a user applying a
     *     custom layout wants to use it <em>instead</em> of the default and will set the layout
     *     at construction or at least prior to attaching to a parent; we don't want the user
     *     to have to explicitly remove the default layout in this case.
     * </p>
     * @return An instance derived from {@link Layout} or {@code null}.
     */
    public Layout getDefaultLayout() {
        return mDefaultLayout;
    }

    private Layout mDefaultLayout = new AbsoluteLayout();

    /**
     * Apply the specified {@link Layout}. Optionally, a call to {@link #requestLayout()} can be
     * prevented.
     *
     * @param layout
     *          The {@code Layout} to apply
     * @return {@code True} if the layout has been applied successfully, {@code false} otherwise.
     */
    public boolean applyLayout(Layout layout) {
        boolean applied = false;
        if (layout != null && isValidLayout(layout)) {
            Layout defaultLayout  = getDefaultLayout();
            if (layout != defaultLayout) {
                mLayouts.remove(defaultLayout);
            }
            if(mLayouts.add(layout)) {
                layout.onLayoutApplied(this, mViewPort);
                applied = true;
            }
        }
        return applied;
    }

    public void dump() {
        Log.d(TAG, "===== DUMP WIDGET ===== \n %s [%f, %f, %f]", toString(),
                getLayoutWidth(), getLayoutHeight(), getLayoutDepth());
        for (Layout l:  mLayouts) {
            l.dump();
        }
    }

    /**
     * Check if specified {@link Layout} has been applied.
     * @param layout
     *          The {@code Layout} to apply
     * @return {@code True} if the layout has been applied, {@code false} otherwise.
     */
    public boolean hasLayout(Layout layout) {
        return layout != null && mLayouts.contains(layout);
    }

    /**
     * Remove the layout {@link Layout} from the chain
     * @param layout {@link Layout}
     * @return true if layout has been removed successfully , false - otherwise
     */
    public boolean removeLayout(final Layout layout) {
        boolean removed = mLayouts.remove(layout);
        if (layout != null && removed) {
            layout.onLayoutApplied(null, new Vector3Axis());
        }
        return removed;
    }

    /*
     * Any layout is valid by default. Subclass can override the method to add new check
     */
    protected boolean isValidLayout(Layout layout) {
        return true;
    }

    protected final Set<Layout> mLayouts = new LinkedHashSet<>();

    /**
     * WidgetContainer default implementation
     */
    public Widget get(final int dataIndex) {
        return dataIndex >= size() ? null : getChildren().get(dataIndex);
    }

    public int size() {
        return getChildren().size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    public int getDataIndex(Widget widget) {
        int id = -1;
        if (!isEmpty()) {
            id = indexOfChild(widget);
        }
        return id;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public void onLayoutChanged(final Layout layout) {
        invalidateLayout(layout);
        onTransformChanged();
        requestLayout();
    }

    protected void invalidateLayout(final Layout layout) {
        // temporary  fix for the most common synchronisation issue
        // of changing layout while layout() is in progress
        // ticket #21859 is filed for complete solution
        runOnGlThread(new Runnable() {
            @Override
            public void run() {
                if (mLayouts.contains(layout)) {
                    layout.invalidate();
                    onTransformChanged();
                    requestLayout();
                }
            }
        });
    }

    protected void invalidateLayout(final Layout layout, final Widget child) {
        // temporary  fix for the most common synchronisation issue
        // of changing layout while layout() is in progress
        // ticket #21859 is filed for complete solution
        runOnGlThread(new Runnable() {
            @Override
            public void run() {
                int dataIndex = getDataIndex(child);
                if (dataIndex != -1) {
                    if (mLayouts.contains(layout)) {
                        layout.invalidate(dataIndex);
                        //onTransformChanged();
                        //requestLayout();
                    }
                }
            }
        });
    }

    protected void invalidateAllLayouts() {
        for (Layout layout: mLayouts) {
            invalidateLayout(layout);
        }
    }

    protected void invalidateAllLayouts(Widget child) {
        for (Layout layout : mLayouts) {
            invalidateLayout(layout, child);
        }
    }

    private static void getGvrfHierarchy(GVRSceneObject sceneObject, String space) {
        if (sceneObject == null) return;

        GVRRenderData rd = sceneObject.getRenderData();
        if (rd != null) {
            Log.d(TAG, space + "%s [%s]", sceneObject.getName(), rd.getRenderingOrder());
        }
        Iterable<GVRSceneObject> i = sceneObject.children();
        if (i != null) {
            Iterator<GVRSceneObject> it = i.iterator();
            while (it.hasNext()) {
                getGvrfHierarchy(it.next(), space + "  ");
            }
        }
    }

    public void printGvrfHierarchy() {
        Log.d(TAG, "========= GVRF Hierarchy for %s =========", getName());
        getGvrfHierarchy(mSceneObject, "");
    }

    private final GVRSceneObject mSceneObject;

    private final GVRContext mContext;

    private final TransformCache mTransformCache;
    private BoundingBox mBoundingBox;
    private boolean mLayoutRequested;
    private boolean mChanged = true;
    private boolean mIsCreated;

    private boolean mFocusEnabled = true;
    private boolean mChildrenFollowFocus = false;
    private boolean mFollowParentFocus = false;
    private HandlesEvent mHandlesFocusEvent = new HandlesEvent() {

        @Override
        public boolean isInFollowEventGroup() {
            return isInFollowFocusGroup();
        }

        @Override
        public boolean handlesEvent(Widget widget, GVRSceneObject sceneObject) {
            return widget.handlesFocusFor(sceneObject);
        }

        @Override
        public String getName() {
            return "focus";
        }

        @Override
        public boolean getChildrenFollowEvent() {
            return getChildrenFollowFocus();
        }

        @Override
        public boolean followsParentEvent(Widget widget) {
            return widget.getFollowParentFocus();
        }
    };

    private boolean mIsFocused;
    private long mLongFocusTimeout = FocusManager.LONG_FOCUS_TIMEOUT;
    private boolean mChildrenFollowInput = false;
    private boolean mFollowParentInput = false;
    private HandlesEvent mHandlesTouchEvent = new HandlesEvent() {

        @Override
        public boolean isInFollowEventGroup() {
            return isInFollowInputGroup();
        }

        @Override
        public boolean getChildrenFollowEvent() {
            return getChildrenFollowInput();
        }

        @Override
        public boolean followsParentEvent(Widget widget) {
            return widget.getFollowParentInput();
        }

        @Override
        public boolean handlesEvent(Widget widget, GVRSceneObject sceneObject) {
            return widget.handlesTouchFor(sceneObject);
        }

        @Override
        public String getName() {
            return "touch";
        }

    };
    private boolean mIsPressed;
    private boolean mIsSelected;
    private boolean mIsTouchable = true;

    private boolean mChildrenFollowState;
    private boolean mFollowParentState;

    private Visibility mVisibility = Visibility.VISIBLE;
    private ViewPortVisibility mIsVisibleInViewPort = ViewPortVisibility.FULLY_VISIBLE;
    private Widget mParent;
    private String mName;

    private JSONObject mMetadata;

    private int mLevel = 0;
    private List<WidgetState> mLevelInfo = new ArrayList<>();

    private final List<Widget> mChildren = new ArrayList<>();

    private final Set<OnBackKeyListener> mBackKeyListeners = new LinkedHashSet<>();
    private final Set<OnFocusListener> mFocusListeners = new LinkedHashSet<>();
    private final Set<OnTouchListener> mTouchListeners = new LinkedHashSet<>();
    private final Set<OnHierarchyChangedListener> mOnHierarchyChangedListeners = new LinkedHashSet<>();

    private OnTouchImpl mTouchHandler = new OnTouchImpl();

    private static WeakReference<Thread> sGLThread = new WeakReference<>(null);
    private static GVRTexture sDefaultTexture;

    private static final String TAG = Widget.class.getSimpleName();
}
