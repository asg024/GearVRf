package com.samsung.smcl.vr.widgets;

import com.samsung.smcl.utility.Log;

import org.gearvrf.GVRSceneObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingFormatArgumentException;
import java.util.Set;

public class NodeEntry {
    public static final String KEY_NAME = "name";
    public static final String KEY_CLASS_NAME = "class";
    public static final String ROOT_NODE_NAME = "RootNode";
    public static final String ROOT_NODE_CLASS_NAME = "com.samsung.smcl.vr.widgets.AbsoluteLayout";

    protected static Set<String> mandatoryKeys = new HashSet<String>();
    protected static Set<String> caseSensitiveKeys = new HashSet<String>();

    static {
        mandatoryKeys.add(KEY_NAME);

        caseSensitiveKeys.add(KEY_NAME);
        caseSensitiveKeys.add(KEY_CLASS_NAME);
    }

    protected String name;
    protected String className;
    protected Map<String, String> properties = new HashMap<String, String>();

    public NodeEntry(GVRSceneObject sceneObject) throws IllegalFormatException {
        String name = sceneObject.getName();
        Log.d(TAG, "NodeEntry(): %s", name);
        if (ROOT_NODE_NAME.equals(name)) {
            properties = new HashMap<String, String>();
            properties.put(KEY_CLASS_NAME, ROOT_NODE_CLASS_NAME);
            properties.put(KEY_NAME, ROOT_NODE_NAME);
        } else {
            properties = NameDemangler.demangleString(name);
        }

        if (properties == null || properties.get(KEY_NAME) == null) {
            if (properties == null) {
                properties = new HashMap<String, String>();
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
                            " the mandatery key <" + key + "> does not exist!");
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
        return properties == null ? null : properties.get(keyName);
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
        public static final String ENTRY_SEPERATOR_REGEX = "__";
        public static final String KEY_VALUE_SEPERATOR = "_";
        public static final String KEY_VALUE_SEPERATOR_REGEX = "\\_";

        public static final String KEY_NAME = "name";

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
            Map<String,String> res = new HashMap<String,String>();

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
