package com.samsung.smcl.vr.widgetlib.widget;

import com.samsung.smcl.vr.widgetlib.log.Log;

import org.gearvrf.GVRSceneObject;
import org.gearvrf.utility.RuntimeAssertion;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingFormatArgumentException;
import java.util.Set;

public class NodeEntry {
    private static final String KEY_NAME = "name";
    private static final String KEY_CLASS_NAME = "class";
    private static final String ROOT_NODE_NAME = "RootNode";
    private static final String ROOT_NODE_CLASS_NAME = "com.samsung.smcl.vr.com.samsung.smcl.vr.com.samsung.smcl.vr.widgetlib.widget.Widget";

    private final static Set<String> mandatoryKeys = new HashSet<>();
    private final static Set<String> caseSensitiveKeys = new HashSet<>();

    static {
        mandatoryKeys.add(KEY_NAME);

        caseSensitiveKeys.add(KEY_NAME);
        caseSensitiveKeys.add(KEY_CLASS_NAME);
    }

    protected String name;
    private String className;
    private Map<String, String> properties = new HashMap<>();

    public NodeEntry(GVRSceneObject sceneObject) throws IllegalFormatException {
        String name = sceneObject.getName();
        Log.d(TAG, "NodeEntry(): %s", name);
        if (ROOT_NODE_NAME.equals(name)) {
            properties = new HashMap<>();
            properties.put(KEY_CLASS_NAME, ROOT_NODE_CLASS_NAME);
            properties.put(KEY_NAME, ROOT_NODE_NAME);
        } else {
            properties = NameDemangler.demangleString(name);
        }

        if (properties == null || properties.get(KEY_NAME) == null) {
            if (properties == null) {
                properties = new HashMap<>();
            } else {
                properties.clear();
            }
            properties.put(KEY_NAME, name);
        }

        // Validation
        if (properties != null) {
            for (String key: mandatoryKeys) {
                if (!properties.containsKey(key)) {
                    throw new MissingFormatArgumentException("Incorrect widget properties format for: " + name +
                            " the mandatory key <" + key + "> does not exist!");
                }
            }
            this.name = properties.get(KEY_NAME);
            className = properties.get(KEY_CLASS_NAME);
        }
    }

    public String getClassName() {
        return className;
    }

    public String getName() {
        return name;
    }

    public String getProperty(String key) {
        return properties == null ? null : properties.get(key);
    }

    public String getProperty(Enum<?> key) {
        return getProperty(key, true);
    }

    public String getProperty(Enum<?> key, boolean lowerCase) {
        final String keyName;
        if (lowerCase) {
            keyName = key.name().toLowerCase(Locale.ENGLISH);
        } else {
            keyName = key.name();
        }
        return getProperty(keyName);
    }

    public JSONObject toJSON() {
        try {
            return new JSONObject(properties).putOpt("name", getName());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e, "toJSON()");
            throw new RuntimeAssertion("NodeEntry.toJSON() failed for '%s'", this);
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("name: ").append(name).append(',');
        b.append("className: ").append(className).append(',');
        if (properties != null) {
            for (Entry<String, String> entry : properties.entrySet()) {
                b.append(entry.getKey()).append(": ").append(entry.getValue())
                        .append(',');
            }
        }
        return b.toString();
    }

    static class NameDemangler {
        private static final String ENTRY_SEPERATOR_REGEX = "__";
        private static final String KEY_VALUE_SEPERATOR = "_";
        private static final String KEY_VALUE_SEPERATOR_REGEX = "\\_";

        /**
         * Returns a {@code Map<String, String>} containing key value pairs
         * from a mangled string. The format of the mangled string is
         * "key1_value1__key2_value2". The values can be null: for example,
         * "key1_value1__key3__key4" is also valid. Keys with null values
         * can be seen as tags.
         *
         * @param mangledString The mangled string.
         *
         * @return The {@code Map<String, String>} containing key-value pairs. It
         * returns null if {@code mangledString} is not a mangled string.
         */
        public static Map<String,String> demangleString(String mangledString) {
            Map<String,String> res = new HashMap<>();

            String[] entries = mangledString.split(ENTRY_SEPERATOR_REGEX);
            if (entries.length == 1 && !entries[0].contains(KEY_VALUE_SEPERATOR)) {
                return null;
            }

            for (String entry : entries) {
                String[] keyValuePair = entry.split(KEY_VALUE_SEPERATOR_REGEX);
                String key = keyValuePair[0];

                // The value can be null
                String value = keyValuePair.length >= 2 ? keyValuePair[1] : null;

                if (value != null && !caseSensitiveKeys.contains(key)) {
                    value = value.toLowerCase();
                }

                res.put(key.toLowerCase(), value);
            }

            return res;
        }
    }

    private static final String TAG = NodeEntry.class.getSimpleName();
}
