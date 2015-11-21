package com.samsung.smcl.vr.widgets;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

public class WidgetFactory {
    public static Widget createWidget(final GVRContext gvrContext,
            final GVRSceneObject sceneObject)
            throws InstantiationException {
        try {
            NodeEntry attributes = new NodeEntry(sceneObject);

            Class<?> widgetClass;
            widgetClass = Class.forName(attributes.getClassName());
            Constructor<?> ctor = widgetClass.getConstructor(GVRContext.class,
                                                             GVRSceneObject.class,
                                                             NodeEntry.class);
            Widget widget = (Widget) ctor.newInstance(gvrContext, sceneObject, attributes);

            return widget;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new InstantiationException(e.getLocalizedMessage());
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
}
