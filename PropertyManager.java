package com.samsung.smcl.vr.widgets;

import android.content.Context;
import android.support.annotation.NonNull;

import com.samsung.smcl.utility.Log;
import com.samsung.smcl.utility.UnmodifiableJSONObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PropertyManager {
    public static PropertyManager get() {
        return sInstance;
    }

    @NonNull
    public UnmodifiableJSONObject getInstanceProperties(Class<?> clazz, String name) {
        JSONObject properties = buildInstanceProperties(name, clazz);
        return new UnmodifiableJSONObject(properties);
    }

    @NonNull
    public UnmodifiableJSONObject getWidgetProperties(Widget widget) {
        return getInstanceProperties(widget.getClass(), widget.getName());
    }

    /* package */
    static void init(Context context) throws JSONException {
        sInstance = new PropertyManager(context);
    }

    private PropertyManager(Context context) throws JSONException {
        loadClassProperties(context);
        loadInstanceProperties(context);
    }

    private JSONObject buildInstanceProperties(String name, Class<?> clazz) {
        final JSONObject properties = mInstanceJson.optJSONObject(name);
        final JSONObject defaultMetadata = getClassProperties(clazz, name);

        if (defaultMetadata == null) {
            if (properties == null) {
                return new JSONObject();
            }
            return properties;
        } else if (properties == null) {
            return defaultMetadata;
        }

        // Overwrite class properties for this widget type with instance-specific properties
        return JSONHelpers.merge(properties, defaultMetadata);
    }

    private String getCanonicalName(Class<?> clazz) {
        String canonicalName = mCanonicalNames.get(clazz);
        if (canonicalName == null) {
            canonicalName = clazz.getCanonicalName();
            mCanonicalNames.put(clazz, canonicalName);
        }
        return canonicalName;
    }

    /* package */
    @SuppressWarnings("unchecked")
    private UnmodifiableJSONObject getClassProperties(Class<?> clazz, String name) {
        final String canonicalName = getCanonicalName(clazz);
        // TODO: Why does caching class properties in mClassProperties not work?
        UnmodifiableJSONObject classProperties = buildClassProperties(clazz, name, canonicalName);

        return classProperties;
    }

    private UnmodifiableJSONObject buildClassProperties(Class<?> clazz, String name, String canonicalName) {
        // Recursively check for class properties up the class hierarchy
        if (name == null || name.isEmpty()) {
            name = clazz.getSimpleName();
        }
        final UnmodifiableJSONObject superProperties;
        final Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            superProperties = getClassProperties(superclass, name);
        } else {
            superProperties = new UnmodifiableJSONObject();
        }
        Log.d(TAG,
                "buildClassProperties(%s): getting super properties for %s: %s",
                name, canonicalName, superProperties);

        UnmodifiableJSONObject classProperties = mClassJson.optJSONObject(canonicalName);
        Log.d(TAG,
                "buildClassProperties(%s): getting class properties for %s: %s",
                name, canonicalName, classProperties);
        if (classProperties == null) {
            classProperties = new UnmodifiableJSONObject();
        } else {
            Log.d(TAG, "buildClassProperties(%s): copy class properties for %s ...", name,
                    canonicalName);
            classProperties = new UnmodifiableJSONObject(JSONHelpers.copy(classProperties));
        }

        JSONObject mergedProperties = JSONHelpers.merge(classProperties, superProperties, name);
        Log.d(TAG,
                "buildClassProperties(%s): getting merged properties for %s: %s",
                name, canonicalName, mergedProperties);
        return new UnmodifiableJSONObject(mergedProperties);
    }

    private static boolean isWidgetClass(Class<?> clazz) {
        return Widget.class.isAssignableFrom(clazz);
    }

    private JSONObject loadClassProperties(Context context)
            throws JSONException {
        JSONObject json = JSONHelpers.loadJSONAsset(context,
                "default_metadata.json");
        JSONObject publicJson = JSONHelpers.loadExternalJSONDocument(context, "user_default_metadata.json");
        Log.d(TAG, "loadClassProperties(): public: %s", publicJson);

        JSONHelpers.merge(publicJson, json);

        Log.d(TAG, "loadClassProperties(): %s", json);
        mClassJson = new UnmodifiableJSONObject(json.optJSONObject("objects"));
        return json;
    }

    private void loadInstanceProperties(Context context)
            throws JSONException {
        final JSONObject json = JSONHelpers.loadJSONAsset(context, "objects.json");
        mInstanceJson = new UnmodifiableJSONObject(json.optJSONObject("objects"));
        Log.v(Log.SUBSYSTEM.WIDGET, TAG, "loadInstanceProperties(): loaded object properties: %s",
                mInstanceJson);
    }

    private final Map<Class<?>, String> mCanonicalNames = new HashMap<>();
    private UnmodifiableJSONObject mClassJson;
    private Map<Class<?>, JSONObject> mClassProperties = new HashMap<>();
    private JSONObject mInstanceJson;

    private static PropertyManager sInstance;
    private static final String TAG = PropertyManager.class.getSimpleName();
}
