package com.samsung.smcl.vr.widgets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.Set;

import org.gearvrf.GVRSceneObject;

public class NodeEntry {
    public static final String KEY_NAME = "name";
    public static final String KEY_CLASS_NAME = "class";
    public static final String ROOT_NODE_NAME = "RootNode";
    public static final String ROOT_NODE_CLASS_NAME = "com.samsung.smcl.vr.widgets.AbsoluteLayout";

    protected static Set<String> mandatoryKeys = new HashSet<String>();

    static {
        mandatoryKeys.add(KEY_NAME);
    }

    protected String name;
    protected String className;
    protected Map<String, String> properties = new HashMap<String, String>();

    public NodeEntry(GVRSceneObject sceneObject) throws IllegalFormatException {
        String name = sceneObject.getName();
        if (ROOT_NODE_NAME.equals(name)) {
            properties = new HashMap<String, String>();
            properties.put(KEY_CLASS_NAME, ROOT_NODE_CLASS_NAME);
            properties.put(KEY_NAME, ROOT_NODE_NAME);
        } else {
            properties = NameDemangler.demangleString(name);
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
        return properties.get(key);
    }

    static class NameDemangler {
        public static final String ENTRY_SEPERATOR_REGEX = "--";
        public static final String KEY_VALUE_SEPERATOR = "-";
        public static final String KEY_VALUE_SEPERATOR_REGEX = "\\-";

        public static final String KEY_NAME = "name";

        /**
         * Returns a {@code Map<String, String>} containing key value pairs
         * from a mangled string. The format of the mangled string is
         * "key1-value1--key2-value2". The values can be null: for example,
         * "key1-value1--key3--key4" is also valid. Keys with null values
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

                res.put(key, value);
            }

            return res;
        }
    }
}
