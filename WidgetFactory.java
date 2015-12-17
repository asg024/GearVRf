package com.samsung.smcl.vr.widgets;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import com.samsung.smcl.utility.Log;
import com.samsung.smcl.vr.gvrf_launcher.util.Helpers;
import com.samsung.smcl.vr.widgets.NodeEntry.NameDemangler;

public class WidgetFactory {
    /**
     * Create a {@link Widget} to wrap the specified {@link GVRSceneObject}. By
     * default, {@code sceneObject} is wrapped in an {@link AbsoluteLayout}. If
     * another {@code Widget} class is specified in {@code sceneObject}'s
     * metadata (as "{@code class_WidgetClassName}"), it will be wrapped in an
     * instance of the specified class instead.
     * 
     * @see NameDemangler#demangleString(String)
     * 
     * @param sceneObject
     *            The {@code GVRSceneObject} to wrap.
     * @return A new {@code Widget} instance.
     * @throws InstantiationException
     *             If the {@code Widget} can't be instantiated for any reason.
     */
    @SuppressWarnings("unchecked")
    public static Widget createWidget(final GVRSceneObject sceneObject)
            throws InstantiationException {
        Class<? extends Widget> widgetClass = AbsoluteLayout.class;
        NodeEntry attributes = new NodeEntry(sceneObject);

        String className = attributes.getClassName();
        if (className != null) {
            try {
                widgetClass = (Class<? extends Widget>) Class
                        .forName(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new InstantiationException(e.getLocalizedMessage());
            }
        }
        Log.d(TAG, "createWidget(): widgetClass: %s",
              widgetClass.getSimpleName());

        return createWidget(sceneObject, attributes, widgetClass);
    }

    /**
     * Create a {@link Widget} of the specified {@code widgetClass} to wrap
     * {@link GVRSceneObject sceneObject}.
     * 
     * @param sceneObject
     *            The {@code GVRSceneObject} to wrap
     * @param widgetClass
     *            The {@linkplain Class} of the {@code Widget} to wrap
     *            {@code sceneObject} with.
     * @return A new {@code Widget} instance.
     * @throws InstantiationException
     *             If the {@code Widget} can't be instantiated for any reason.
     */
    public static Widget createWidget(final GVRSceneObject sceneObject,
            Class<? extends Widget> widgetClass) throws InstantiationException {
        NodeEntry attributes = new NodeEntry(sceneObject);
        return createWidget(sceneObject, attributes, widgetClass);
    }

    /**
     * Create an {@link AbsoluteLayout} {@link Widget} to wrap a
     * {@link GVRSceneObject} that is a child of the specified {@code root}
     * {@code GVRSceneObject}.
     * 
     * @param root
     *            The root {@code GVRSceneObject} containing the desired child.
     * @param childName
     *            Name of the child of {@code root} to wrap.
     * @return A new {@code AbsoluteLayout} instance.
     * @throws InstantiationException
     *             If the {@code Widget} can't be instantiated for any reason.
     */
    public static Widget createWidget(final GVRSceneObject root,
            final String childName) throws InstantiationException {
        return createWidget(root, childName, AbsoluteLayout.class);
    }

    /**
     * Create an {@link Widget} of the specified {@code widgetClass} to wrap a
     * {@link GVRSceneObject} that is a child of the specified {@code root}
     * {@code GVRSceneObject}.
     * 
     * @param root
     *            The root {@code GVRSceneObject} containing the desired child.
     * @param childName
     *            Name of the child of {@code root} to wrap.
     * @param widgetClass
     *            The {@linkplain Class} of the {@code Widget} to wrap the child
     *            {@code GVRSceneObject} with.
     * @return A new {@code AbsoluteLayout} instance.
     * @throws InstantiationException
     *             If the {@code Widget} can't be instantiated for any reason.
     */
    static public Widget createWidget(GVRSceneObject root,
            final String childName, final Class<? extends Widget> widgetClass)
            throws InstantiationException {
        Widget result = null;
        if (root != null) {
            root = Helpers.findByName(childName, root);
            if (root != null) {
                try {
                    result = createWidget(root, widgetClass);
                    Log.d(TAG, "createWidget(): created %s '%s'",
                          widgetClass.getSimpleName(), childName);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                    Log.e(TAG,
                          "createWidget(): couldn't instantiate '%s' as %s!",
                          childName, widgetClass.getSimpleName());
                    throw e;
                }
            } else {
                Log.e(TAG, "createWidget(): can't find '%s'!", childName);
            }
        } else {
            Log.e(TAG, "createWidget(): root is null!");
        }
        return result;
    }

    /**
     * Create an {@link AbsoluteLayout} to wrap the root {@link GVRSceneObject}
     * of the scene graph loaded from a file.
     * 
     * @param gvrContext
     *            The {@link GVRContext} to load the model into.
     * @param modelFile
     *            The asset file to load the model from.
     * @return A new {@code AbsoluteLayout} instance.
     * @throws InstantiationException
     *             If the {@code Widget} can't be instantiated for any reason.
     * @throws IOException
     *             If the model file can't be read.
     */
    public static Widget createWidgetFromModel(final GVRContext gvrContext,
            final String modelFile) throws InstantiationException, IOException {
        return createWidgetFromModel(gvrContext, modelFile,
                                     AbsoluteLayout.class);
    }

    /**
     * Create a {@link Widget} of the specified {@code widgetClass} to wrap the
     * root {@link GVRSceneObject} of the scene graph loaded from a file.
     * 
     * @param gvrContext
     *            The {@link GVRContext} to load the model into.
     * @param modelFile
     *            The asset file to load the model from.
     * @param widgetClass
     *            The {@linkplain Class} of the {@code Widget} to wrap the root
     *            {@code GVRSceneObject} with.
     * @return A new {@code AbsoluteLayout} instance.
     * @throws InstantiationException
     *             If the {@code Widget} can't be instantiated for any reason.
     * @throws IOException
     *             If the model file can't be read.
     */
    public static Widget createWidgetFromModel(final GVRContext gvrContext,
            final String modelFile, Class<? extends Widget> widgetClass)
            throws InstantiationException, IOException {
        GVRSceneObject rootNode = Helpers.loadModel(gvrContext, modelFile);
        return createWidget(rootNode, widgetClass);
    }

    /**
     * Create an {@link AbsoluteLayout} {@link Widget} to wrap a
     * {@link GVRSceneObject} that is a child of the {@code root}
     * {@code GVRSceneObject} of the scene graph loaded from a file.
     * 
     * @param gvrContext
     *            The {@link GVRContext} to load the model into.
     * @param modelFile
     *            The asset file to load the model from.
     * @param childName
     *            Name of the child of {@code root} to wrap.
     * @return A new {@code AbsoluteLayout} instance.
     * @throws InstantiationException
     *             If the {@code AbsoluteLayout} can't be instantiated for any
     *             reason.
     * @throws IOException
     *             If the model file can't be read.
     */
    public static Widget createWidgetFromModel(final GVRContext gvrContext,
            final String modelFile, final String nodeName)
            throws InstantiationException, IOException {
        return createWidgetFromModel(gvrContext, modelFile, nodeName,
                                     AbsoluteLayout.class);
    }

    /**
     * Create a {@link Widget} of the specified {@code widgetClass} to wrap a
     * {@link GVRSceneObject} that is a child of the {@code root}
     * {@code GVRSceneObject} of the scene graph loaded from a file.
     * 
     * @param gvrContext
     *            The {@link GVRContext} to load the model into.
     * @param modelFile
     *            The asset file to load the model from.
     * @param childName
     *            Name of the child of {@code root} to wrap.
     * @param widgetClass
     *            The {@linkplain Class} of the {@code Widget} to wrap the child
     *            {@code GVRSceneObject} with.
     * @return A new {@code Widget} instance.
     * @throws InstantiationException
     *             If the {@code AbsoluteLayout} can't be instantiated for any
     *             reason.
     * @throws IOException
     *             If the model file can't be read.
     */
    public static Widget createWidgetFromModel(final GVRContext gvrContext,
            final String modelFile, final String nodeName,
            Class<? extends Widget> widgetClass) throws InstantiationException,
            IOException {
        GVRSceneObject rootNode = Helpers.loadModel(gvrContext, modelFile,
                                                    nodeName);
        return createWidget(rootNode, widgetClass);
    }

    private static Widget createWidget(final GVRSceneObject sceneObject,
            NodeEntry attributes, Class<? extends Widget> widgetClass)
            throws InstantiationException {
        try {
            Constructor<?> ctor = widgetClass
                    .getConstructor(GVRContext.class, GVRSceneObject.class,
                                    NodeEntry.class);
            Widget widget = (Widget) ctor.newInstance(sceneObject
                    .getGVRContext(), sceneObject, attributes);
            return widget;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new InstantiationException(e.getLocalizedMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new InstantiationException(e.getLocalizedMessage());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InstantiationException(e.getLocalizedMessage());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new InstantiationException(e.getLocalizedMessage());
        }
    }

    private static String TAG = WidgetFactory.class.getSimpleName();
}
