package com.samsung.smcl.vr.widgets;

import static com.samsung.smcl.vr.widgets.JSONHelpers.*;
import static com.samsung.smcl.utility.Exceptions.RuntimeAssertion;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Future;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;

import com.samsung.smcl.utility.Log;
import com.samsung.smcl.utility.UnmodifiableJSONObject;
import com.samsung.smcl.utility.Utility;
import com.samsung.smcl.vr.gvrf_launcher.LauncherViewManager.OnInitListener;
import com.samsung.smcl.vr.gvrf_launcher.MainScene;
import com.samsung.smcl.vr.gvrf_launcher.Policy;
import com.samsung.smcl.vr.gvrf_launcher.R;
import com.samsung.smcl.vr.gvrf_launcher.TouchManager;

public class Widget {

    /**
     * Call to initialize the Widget infrastructure. Parses {@code objects.json}
     * to load metadata for {@code Widgets}, as well as animation and material
     * specs.
     *
     * @param context
     *            A valid Android {@link Context}.
     * @param touchManager
     *            The global {@link TouchManager} instance.
     * @throws JSONException
     *             if the {@code objects.json} file is invalid JSON
     * @throws NoSuchMethodException
     *             if a constructor can't be found for an animation type
     *             specified in {@code objects.json}.
     */
    static public void init(Context context, TouchManager touchManager)
            throws JSONException, NoSuchMethodException {
        String rawJson = Utility.readTextFile(context, "objects.json");
        if (Policy.LOGGING_VERBOSE) {
            Log.v(TAG, "init(): raw JSON: %s", rawJson);
        }
        if (rawJson == null) {
            rawJson = "";
        }
        final JSONObject json = new JSONObject(rawJson);
        sObjectMetadata = new UnmodifiableJSONObject(
                json.optJSONObject("objects"));
        if (Policy.LOGGING_VERBOSE) {
            Log.v(TAG, "init(): loaded object metadata: %s", sObjectMetadata.toString());
        }

        JSONObject animationMetadata = json.optJSONObject("animations");
        AnimationFactory.init(animationMetadata);
        if (Policy.LOGGING_VERBOSE) {
            Log.v(TAG, "init(): loaded animation metadata: %s", animationMetadata);
        }
    }

    /**
     * Register this with LauncherViewManager. An alternative would be to have
     * {@link #init(Context, TouchManager) init()} do this work and just call it
     * directly from LauncherViewManager.onInit().
     */
    static public final OnInitListener onInitListener = new OnInitListener() {
        @Override
        public void onInit(GVRContext gvrContext, MainScene scene) {
            FocusManager.get(gvrContext).init(gvrContext);
            sGLThread = new WeakReference<Thread>(Thread.currentThread());
            sDefaultTexture = gvrContext.loadTexture(new GVRAndroidResource(
                    gvrContext, R.drawable.default_bkgd));
            Log.d(TAG, "onInit(): default texture: %s", sDefaultTexture);
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
        public boolean onFocus(boolean focused, Widget widget);

        /**
         * Called when a widget has had focus for more than
         * {@link Widget#getLongFocusTime()} milliseconds.
         *
         * @return {@code True} to indicate that no further processing of the
         *         event should take place; {@code false} to allow further
         *         processing.
         */
        public boolean onLongFocus(Widget widget);
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
        public boolean onBackKey(Widget widget);
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
         *
         * @return {@code True} to indicate that no further processing of the
         *         touch event should take place; {@code false} to allow further
         *         processing.
         */
        public boolean onTouch(Widget widget);
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
     * Construct a wrapper for an existing {@link GVRSceneObject}.
     *
     * @param context
     *            The current {@link GVRContext}.
     * @param sceneObject
     *            The {@link GVRSceneObject} to wrap.
     */
    public Widget(final GVRContext context, final GVRSceneObject sceneObject) {
        mContext = context;
        mSceneObject = sceneObject;

        if (Policy.LOGGING_VERBOSE) {
            Log.v(TAG,
                  "Widget constructor: %s width = %f height = %f depth = %f",
                  sceneObject.getName(), getWidth(), getHeight(), getDepth());
        }

        mTransformCache = new TransformCache(getTransform());
        requestLayout();
    }

    /**
     * A constructor for wrapping existing {@link GVRSceneLayout} instances.
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
        this(context, sceneObject);

        // This gives us the demangled name, which is the name we'll use to
        // refer to the widget
        String attribute = attributes.getProperty("name");
        setName(attribute);

        final boolean hasRenderData = sceneObject.getRenderData() != null;

        attribute = attributes.getProperty("touchable");
        if (attribute != null) {
            setTouchable(hasRenderData && attribute.compareToIgnoreCase("false") != 0);
        }

        attribute = attributes.getProperty("focusenabled");
        if (attribute != null) {
            setFocusEnabled(attribute.compareToIgnoreCase("false") != 0);
        }

        attribute = attributes.getProperty("selected");
        setSelected(attribute != null && hasRenderData
                && attribute.compareToIgnoreCase("false") != 0);

        attribute = attributes.getProperty("visibility");
        setVisibility(attribute != null ? Visibility.valueOf(attribute
                .toUpperCase(Locale.ENGLISH)) : Visibility.VISIBLE);

        createChildren(context, sceneObject);

        try {
            setupMetadata();
        } catch (Exception e) {
            throw new InstantiationException(e.getLocalizedMessage());
        }
    }

//    private static final String pattern = Widget.class.getSimpleName()
//            + "name : %s size = (%f, %f, %f) \n"
//            + "touchable = %b focus_enabled = %b Visibile = %s selected = %b";
//
//    public String toString() {
//        return String.format(pattern, getName(), getWidth(), getHeight(), getDepth(),
//                             mIsTouchable, mFocusEnabled, mVisibility, mIsSelected);
//    }

    public Widget(final GVRContext context, final float width,
            final float height) {
        this(context, new GVRSceneObject(context, width, height));

        GVRRenderData renderData = getRenderData();
        if (renderData != null) {
            GVRMaterial material = new GVRMaterial(mContext,
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
     * @return The timeout, in milliseconds, before a continuous focus state
     *         triggers an {@link #onLongFocus()} event. By default this is
     *         {@link FocusManger#LONG_FOCUS_TIMEOUT}.
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
        return mFocusListeners.add(listener);
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
        return mFocusListeners.remove(listener);
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
            if (focused && follow) {
                for (Widget child : mChildren) {
                    if (child.isFocusEnabled()) {
                        child.doOnFocus(true);
                    }
                }
            } else if (focused && !follow) {
                for (Widget child : mChildren) {
                    if (child.isFocusEnabled() && !child.mFollowParentFocus) {
                        child.doOnFocus(false);
                    }
                }
            }
            for (Widget child : mChildren) {
                if (focused && child.isFocusEnabled()) {
                    if (follow) {
                        child.doOnFocus(true);
                    } else if (!child.getFollowParentFocus()) {
                        child.doOnFocus(false);
                    }
                }

                Log.d(TAG, "setChildrenFollowFocus(%s): calling registerPickable", getName());
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
            Log.d(TAG, "setFollowParentFocus(%s): calling registerPickable", getName());
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
     * The touch hook method -- {@link #onTouch(boolean)} -- is invoked as
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
            for (Widget child : mChildren) {
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

    private final class FocusableImpl implements FocusManager.Focusable, FocusManager.LongFocusTimeout {
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
         * {@link android.View.GestureDetector.OnGestureListener#onLongPress(MotionEvent)
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
     * Add a listener for {@linkplain OnTouchListener#onTouch() touch}
     * notifications for this object.
     *
     * @param listener
     *            An implementation of {@link OnTouchListener}.
     * @return {@code True} if the listener was successfully registered,
     *         {@code false} if the listener was already registered.
     */
    public boolean addTouchListener(final OnTouchListener listener) {
        return mTouchListeners.add(listener);
    }

    /**
     * Remove a previously {@linkplain #addTouchListener(OnTouchListener)
     * registered} touch notification {@linkplain OnTouchListener listener}.
     *
     * @param listener
     *            An implementation of {@link OnTouchListener}
     * @return {@code True} if the listener was successfully unregistered,
     *         {@code false} if the specified listener was not previously
     *         registered with this object.
     */
    public boolean removeTouchListener(final OnTouchListener listener) {
        return mTouchListeners.remove(listener);
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
            Log.d(TAG, "setLevel(%d): clearing level: %d", level, mLevel);
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
            updateState();
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

    /**
     * Sets the {@linkplain GVRMaterial#setMainTexture(GVRTexture) main texture}
     * of the {@link Widget}.
     *
     * @param texture
     *            The new texture.
     */
    public void setTexture(final GVRTexture texture) {
        getMaterial().setMainTexture(texture);
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
     * @param texture
     *            The new texture.
     */
    public void setTexture(final Future<GVRTexture> texture) {
        getMaterial().setMainTexture(texture);
    }

    /**
     * Sets the {@linkplain GVRMaterial#setMainTexture(GVRTexture) main texture}
     * of the {@link Widget}.
     *
     * @param bitmapId
     *            Resource ID of the bitmap to create the texture from.
     */
    public void setTexture(final int bitmapId) {
        final GVRAndroidResource resource = new GVRAndroidResource(
                mContext.getContext(), bitmapId);
        setTexture(mContext.loadFutureTexture(resource));
    }

    public float getWidth() {
        return getBoundingBoxInternal().getWidth();
    }

    public float getHeight() {
        return getBoundingBoxInternal().getHeight();
    }

    public float getDepth() {
        return getBoundingBoxInternal().getDepth();
    }

    /**
     * Get a {@link BoundingBox} that is axis-aligned with this {@link Widget}'s
     * parent and sized to contain the {@code Widget} with its local transform
     * applied.
     * <p>
     * Note: The {@code Widget}'s children are <em>not</em> explicitly included,
     * so the bounding box may or may not be big enough to include them. If you
     * want to make sure that the bounding box fully encompasses the
     * {@code Widget}'s children, use {@link #getBoundingBox(boolean)}.
     *
     * @return A {@link BoundingBox} that contains the {@code Widget}.
     */
    public BoundingBox getBoundingBox() {
        return getBoundingBox(false);
    }

    /**
     * Get a {@link BoundingBox} that is axis-aligned with this {@link Widget}'s
     * parent and sized to contain the {@code Widget} with its local transform
     * applied. Optionally, the bounding box can be
     * {@linkplain BoundingBox#expand(BoundingBox) expanded} to contain the
     * bounding boxes of the {@code Widget}'s children that would otherwise lie
     * outside it.
     *
     * @param includeChildren
     *            {@code True} to explicitly include the {@code Widget}'s
     *            children, {@code false} to ignore them.
     * @return A {@code BoundingBox} that contains the {@code Widget} and,
     *         optionally, its children.
     */
    public BoundingBox getBoundingBox(boolean includeChildren) {
        if (includeChildren) {
            return expandBoundingBox(new BoundingBox(this));
        }
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
        renderData.setOffsetFactor(offsetFactor);
        if (renderData != null) {
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
        if (mTransformCache.setPosition(x, y, z) && !mChanged) {
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
        if (mTransformCache.setPosX(x) && !mChanged) {
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
        if (mTransformCache.setPosY(y) && !mChanged) {
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
        if (mTransformCache.setPosZ(z) && !mChanged) {
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
        if (mTransformCache.setRotation(w, x, y, z) && !mChanged) {
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
        if (mTransformCache.setScale(x, y, z) && !mChanged) {
            onTransformChanged();
        }
    }

    /**
     * Set the widget's current scaling on the 'X' axis.
     *
     * @param x
     *            Scaling factor on the 'X' axis.
     */
    public void setScaleX(float x) {
        getTransform().setScaleX(x);
        if (mTransformCache.setScaleX(x) && !mChanged) {
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
        if (mTransformCache.setScaleY(y) && !mChanged) {
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
        if (mTransformCache.setScaleZ(z) && !mChanged) {
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
            Log.d(TAG, "setVisibility(%s) for %s", visibility, getName());
            if (mParent != null) {
                final GVRSceneObject parentSceneObject = mParent
                        .getSceneObject();
                switch (visibility) {
                    case VISIBLE:
                        if (mSceneObject.getParent() != parentSceneObject) {
                            parentSceneObject.addChildObject(mSceneObject);
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
                    mParent.requestLayout();
                }
            }
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

    /**
     * Call this method to notify ancestors of this {@link Widget} that its
     * dimensions, position, or orientation have been altered so that they can
     * respond by running {@link #layout()} if needed.
     */
    public void requestLayout() {
        mLayoutRequested = true;

        if (Policy.LOGGING_VERBOSE) {
            Log.v(TAG,
                  "requestLayout(%s): mParent: '%s', mParent.isLayoutRequested: %b",
                  getName(), mParent == null ? "<NULL>" : mParent.getName(),
                  mParent != null && mParent.isLayoutRequested());
        }

        if (mParent != null && !mParent.isLayoutRequested()) {
            if (Policy.LOGGING_VERBOSE) {
                Log.v(TAG, "requestLayout(%s) requesting", getName());
            }

            mParent.requestLayout();
//            new RuntimeException().printStackTrace();
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
        runOnGlThread(new Runnable() {
            @Override
            public void run() {
                if (!mIsCreated) {
                    doOnCreate();
                    mIsCreated = true;
                }
            }
        });
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
     */
    @SuppressLint("WrongCall")
    protected void layout() {
        if (Policy.LOGGING_VERBOSE) {
            Log.v(TAG, "layout(%s): changed: %b, requested: %b", getName(), mChanged, mLayoutRequested);
        }

        if (mChanged || mLayoutRequested) {
            if (Policy.LOGGING_VERBOSE) {
                Log.v(TAG, "layout(%s): calling onLayout", getName());
            }
            onLayout();
        }

        mLayoutRequested = false;
        mChanged = false;
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
        if (isGLThread()) {
            r.run();
        } else {
            getGVRContext().runOnGlThread(r);
        }
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

    final protected JSONObject getObjectMetadata() {
        return sObjectMetadata.optJSONObject(getName());
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
     * {@linkplain WidgetGroup#removeChild(Widget) removed} from another
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
    public boolean onFocus(boolean focused) {
        return true;
    }

    /**
     * Hook method for handling long focus events. Called when the object has
     * held focus for longer than a certain period of time. This is similar to
     * {@link android.View.GestureDetector.OnGestureListener#onLongPress(MotionEvent)
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
     */
    protected void onLayout() {

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
        Log.d(TAG, "onTransformChanged(): %s", getName());
//        new RuntimeException().printStackTrace();

        // Even if the calling code that altered the transform doesn't request a
        // layout, we'll do a layout the next time a layout is requested on our
        // part of the scene graph.
        mChanged = true;

        // Clear this to indicate that the bounding box has been invalidated and
        // needs to be constructed and transformed anew.
        mBoundingBox = null;
    }

    /* package */
    boolean addChildInner(final Widget child, final GVRSceneObject childRootSceneObject, int index) {
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
            if (child.getVisibility() == Visibility.VISIBLE) {
                if (childRootSceneObject.getParent() != getSceneObject()) {
                    getSceneObject().addChildObject(childRootSceneObject);
                } else if (Policy.LOGGING_VERBOSE) {
                    Log.v(TAG,
                          "addChildInner(): child '%s' already attached to this Group ('%s')",
                          child.getName(), getName());
                }
            }
            child.doOnAttached(this);
        }
        return added;
    }

    void checkTransformChanged() {
        if (mTransformCache.save(this, true) && !mChanged) {
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
        Log.d(TAG, "GroupWidget(): creating children");
        List<GVRSceneObject> children = sceneObject.getChildren();
        Log.d(TAG, "GroupWidget(): child count: %d", children.size());
        for (GVRSceneObject sceneObjectChild : children) {
            Log.d(TAG, "GroupWidget(): creating child '%s'",
                  sceneObjectChild.getName());
            final Widget child = createChild(context, sceneObjectChild);
            if (child != null) {
                addChildInner(child);
            }
        }
    }

    /* package */
    Widget findChildByName(final String name) {
        final List<Widget> groups = new ArrayList<Widget>();
        groups.add(this);

        return findChildByNameInAllGroups(name, groups);
    }

    /* package */
    List<Widget> getChildren() {
        return new ArrayList<Widget>(mChildren);
    }

    /* package */
    boolean removeChild(final Widget child, final GVRSceneObject childRootSceneObject, boolean preventLayout) {
        final boolean removed = mChildren.remove(child);
        if (removed) {
            Log.d(TAG, "removeChild(): '%s' removed", child.getName());
            if (childRootSceneObject.getParent() != getSceneObject()) {
                Log.e(TAG,
                      "removeChild(): '%s' is not a child of '%s' GVRSceneObject!",
                      child.getName(), getName());
            }
            getSceneObject().removeChildObject(childRootSceneObject);
            child.doOnDetached();
            if (!preventLayout) {
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
            create();
            mParent = parent;
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
        final boolean oldFocus = mIsFocused;
        if (Policy.LOGGING_VERBOSE) {
            Log.v(TAG, "doOnFocus(%s): mIsFocused: %b, focused: %b", getName(),
                  mIsFocused, focused);
        }

        for (OnFocusListener listener : mFocusListeners) {
            if (listener.onFocus(focused, this)) {
                return true;
            }
        }
        final boolean tookFocus = onFocus(focused);
        Log.d(TAG, "doOnFocus(%s): tookFocus: %b", getName(), tookFocus);
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
            for (Widget child : mChildren) {
                if (child.mFocusEnabled
                        && !child.isFocused()
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
        for (OnFocusListener listener : mFocusListeners) {
            if (listener.onLongFocus(this)) {
                return;
            }
        }
        onLongFocus();
        final boolean inFollowFocusGroup = isInFollowFocusGroup();
        for (Widget child : mChildren) {
            if (child.mFocusEnabled
                    && (mChildrenFollowFocus || child.mFollowParentFocus || inFollowFocusGroup)) {
                child.doOnLongFocus();
            }
        }
    }

    private boolean doOnTouch() {
        for (OnTouchListener listener : mTouchListeners) {
            if (listener.onTouch(this)) {
                return true;
            }
        }

        final boolean acceptedTouch = onTouch();
        if (acceptedTouch) {
            final boolean inFollowInputGroup = isInFollowInputGroup();
            for (Widget child : mChildren) {
                if (child.isTouchable()
                        && (mChildrenFollowInput
                                || child.getFollowParentInput() || inFollowInputGroup)) {
                    child.doOnTouch();
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
        Collection<Widget> children = groupWidget.mChildren;
        for (Widget child : children) {
            if (child.getName().equals(name)) {
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
    private static Widget findChildByNameInAllGroups(final String name, List<Widget> groups) {
        if (groups.isEmpty()) {
            return null;
        }

        ArrayList<Widget> groupChildren = new ArrayList<Widget>();
        Widget result = null;
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

    private BoundingBox expandBoundingBox(final BoundingBox boundingBox) {
        boundingBox.expand(getBoundingBoxInternal());
        for (Widget child : mChildren) {
            child.expandBoundingBox(boundingBox);
        }
        return boundingBox;
    }

    private BoundingBox getBoundingBoxInternal() {
        if (mBoundingBox == null) {
            mBoundingBox = new BoundingBox(this);
        }
        return mBoundingBox;
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

        if (useParentFocusable()) {
            mFocusableImpl = mParent.mFocusableImpl;
        } else if (needsOwnFocusable()) {
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
                Log.d(TAG, "registerPickable(): '%s' is not focus-enabled",
                      getName());
                focusManager.unregister(getSceneObject());
            }
        } else {
            touchManager.removeHandlerFor(getSceneObject());
            Log.d(TAG, "registerPickable(): unregistering '%s'; focus-enabled: %b", getName(), mFocusEnabled);
            focusManager.unregister(getSceneObject());
        }

        // If our focusable or touch handler have changed, we need to let any
        // children that are part of the same focus/input group or might be
        // following this widget know
        if (currentFocusable != mFocusableImpl
                || currentTouchHandler != mTouchHandler) {
            for (Widget child : mChildren) {
                child.registerPickable();
            }
        }
    }

    private enum Properties {
        touchable, focusenabled, visibility, states, levels, level, selected
    }

    private void setupMetadata() throws JSONException, NoSuchMethodException {
        JSONObject metaData = getObjectMetadata();
        if (metaData != null) {
            Log.d(TAG, "setupMetadata(): setting up metadata for %s: %s", getName(), metaData);

            mIsTouchable = optBoolean(metaData, Properties.touchable, mIsTouchable);
            mFocusEnabled = optBoolean(metaData, Properties.focusenabled, mFocusEnabled);
            mIsSelected = optBoolean(metaData, Properties.selected, mIsSelected);
            Visibility visibility = optEnum(metaData, Properties.visibility, mVisibility);
            setVisibility(visibility);

            final boolean hasStates = has(metaData, Properties.states);
            final boolean hasLevels = has(metaData, Properties.levels);
            final boolean hasLevel = has(metaData, Properties.level);
            Log.d(TAG,
                  "setupMetadata(): for '%s'; states: %b, levels %b, level %b",
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
    }

    private void setupLevels(JSONObject metaData) throws JSONException,
            NoSuchMethodException {
        JSONArray levelsArray = optJSONArray(metaData, Properties.levels);

        if (levelsArray != null) {
            Log.d(TAG, "setupLevels(): for %s", getName());
            for (int i = 0; i < levelsArray.length(); ++i) {
                mLevelInfo.add(new WidgetState(this, levelsArray
                        .getJSONObject(i)));
            }
        } else {
            Log.d(TAG, "setupLevels(): No levels metadata for %s", getName());
        }
    }

    private void setupStates(JSONObject metadata) throws JSONException,
            NoSuchMethodException {
        JSONObject states = optJSONObject(metadata, Properties.states);
        Log.d(TAG, "setupStates(): for '%s': %s", getName(), states);
        mLevelInfo.add(new WidgetState(this, states));
    }

    private void updateState() {
        final WidgetState.State state;
        if (mIsSelected) {
            state = WidgetState.State.SELECTED;
        } else if (mIsFocused) {
            state = WidgetState.State.FOCUSED;
        } else {
            state = WidgetState.State.NORMAL;
        }

        Log.d(TAG, "updateState(): %s for '%s'", state, getName());
        if (!mLevelInfo.isEmpty()) {
            mLevelInfo.get(mLevel).setState(this, state);
        }
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

    private final class OnTouchImpl implements TouchManager.OnBackKey {
        @Override
        public boolean touch(GVRSceneObject sceneObject) {// , float[] hit) {
            return doOnTouch();
        }

        @Override
        public boolean onBackKey(GVRSceneObject sceneObject) {
            return doOnBackKey();
        }

        public Widget target() {
            return Widget.this;
        }
    }

    private final GVRSceneObject mSceneObject;

    private final GVRContext mContext;

    private final TransformCache mTransformCache;
    private BoundingBox mBoundingBox;
    private boolean mLayoutRequested;
    private boolean mChanged;
    private boolean mIsCreated;

    private boolean mFocusEnabled = true;
    private boolean mChildrenFollowFocus = false;
    private boolean mFollowParentFocus = false;
    private boolean mIsFocused;
    private long mLongFocusTimeout = FocusManager.LONG_FOCUS_TIMEOUT;
    private boolean mChildrenFollowInput = false;
    private boolean mFollowParentInput = false;
    private boolean mIsSelected;
    private boolean mIsTouchable = true;
    private Visibility mVisibility = Visibility.VISIBLE;
    private Widget mParent;
    private String mName;

    private int mLevel = 0;
    private List<WidgetState> mLevelInfo = new ArrayList<WidgetState>();

    private final List<Widget> mChildren = new ArrayList<Widget>();

    private final Set<OnBackKeyListener> mBackKeyListeners = new LinkedHashSet<OnBackKeyListener>();
    private final Set<OnFocusListener> mFocusListeners = new LinkedHashSet<OnFocusListener>();
    private final Set<OnTouchListener> mTouchListeners = new LinkedHashSet<OnTouchListener>();

    private OnTouchImpl mTouchHandler = new OnTouchImpl();

    private static WeakReference<Thread> sGLThread = new WeakReference<Thread>(null);
    private static GVRTexture sDefaultTexture;

    private static JSONObject sObjectMetadata;
    private static final String TAG = Widget.class.getSimpleName();
}

