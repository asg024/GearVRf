package com.samsung.smcl.vr.widgets;

import java.io.File;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.samsung.smcl.utility.Log;
import com.samsung.smcl.utility.UnmodifiableJSONArray;
import com.samsung.smcl.utility.UnmodifiableJSONObject;
import com.samsung.smcl.utility.Utility;

abstract public class JSONHelpers {

    public static final JSONObject EMPTY_OBJECT = new UnmodifiableJSONObject(new JSONObject());
    public static final JSONArray EMPTY_ARRAY = new UnmodifiableJSONArray(new JSONArray());

    public static <P extends Enum<P>> Object get(final JSONObject json, P e)
            throws JSONException {
        return json.get(e.name());
    }

    public static <P extends Enum<P>> Object opt(final JSONObject json, P e) {
        return json.opt(e.name());
    }

    public static <P extends Enum<P>> boolean getBoolean(final JSONObject json,
            P e) throws JSONException {
        return json.getBoolean(e.name());
    }

    public static <P extends Enum<P>> boolean optBoolean(final JSONObject json,
            P e) {
        return json.optBoolean(e.name());
    }

    public static <P extends Enum<P>> boolean optBoolean(final JSONObject json,
            P e, boolean fallback) {
        return json.optBoolean(e.name(), fallback);
    }

    public static <P extends Enum<P>> double getDouble(final JSONObject json,
            P e) throws JSONException {
        return json.getDouble(e.name());
    }

    public static <P extends Enum<P>> double optDouble(final JSONObject json,
            P e) {
        return json.optDouble(e.name());
    }

    public static <P extends Enum<P>> double optDouble(final JSONObject json,
            P e, double fallback) {
        return json.optDouble(e.name(), fallback);
    }

    public static <P extends Enum<P>> int getInt(final JSONObject json, P e)
            throws JSONException {
        return json.getInt(e.name());
    }

    public static <P extends Enum<P>> int optInt(final JSONObject json, P e) {
        return json.optInt(e.name());
    }

    public static <P extends Enum<P>> int optInt(final JSONObject json, P e,
            int fallback) {
        return json.optInt(e.name(), fallback);
    }

    public static <P extends Enum<P>> long getLong(final JSONObject json, P e)
            throws JSONException {
        return json.getLong(e.name());
    }

    public static <P extends Enum<P>> long optLong(final JSONObject json, P e) {
        return json.optLong(e.name());
    }

    public static <P extends Enum<P>> long optLong(final JSONObject json, P e,
            long fallback) {
        return json.optLong(e.name(), fallback);
    }

    public static <P extends Enum<P>> String getString(final JSONObject json,
            P e) throws JSONException {
        return json.getString(e.name());
    }

    public static <P extends Enum<P>> String optString(final JSONObject json,
            P e) {
        return json.optString(e.name());
    }

    public static <P extends Enum<P>> String optString(final JSONObject json,
            P e, String fallback) {
        return json.optString(e.name(), fallback);
    }

    public static <P extends Enum<P>, R extends Enum<R>> R getEnum(
            final JSONObject json, P e, Class<R> r) throws JSONException {
        return Enum.valueOf(r, json.getString(e.name()));
    }

    public static <P extends Enum<P>, R extends Enum<R>> R optEnum(
            final JSONObject json, P e, Class<R> r) {
        final String value = json.optString(e.name());
        if (value == null) {
            return null;
        }
        return Enum.valueOf(r, value);
    }

    @SuppressWarnings("unchecked")
    public static <P extends Enum<P>, R extends Enum<R>> R optEnum(
            final JSONObject json, P e, R fallback) {
        final String value = json.optString(e.name(), fallback.name());
        return (R) Enum.valueOf(fallback.getClass(), value);
    }

    public static <P extends Enum<P>> JSONObject getJSONObject(
            final JSONObject json, P e) throws JSONException {
        return json.getJSONObject(e.name());
    }

    public static <P extends Enum<P>> JSONObject optJSONObject(
            final JSONObject json, P e) {
        return json.optJSONObject(e.name());
    }

    public static <P extends Enum<P>> JSONObject optJSONObject(final JSONObject json, P e,
                                                               boolean emptyForNull) {
        JSONObject jsonObject = optJSONObject(json, e);
        if (jsonObject == null && emptyForNull) {
            jsonObject = EMPTY_OBJECT;
        }
        return jsonObject;
    }

    public static <P extends Enum<P>> JSONArray getJSONArray(
            final JSONObject json, P e) throws JSONException {
        return json.getJSONArray(e.name());
    }

    public static <P extends Enum<P>> JSONArray optJSONArray(
            final JSONObject json, P e) {
        return json.optJSONArray(e.name());
    }

    public static <P extends Enum<P>> JSONArray optJSONArray(final JSONObject json, P e,
                                                             boolean emptyForNull) {
        JSONArray jsonArray = optJSONArray(json, e);
        if (jsonArray == null && emptyForNull) {
            jsonArray = EMPTY_ARRAY;
        }
        return jsonArray;
    }

    public static <P extends Enum<P>> boolean has(final JSONObject json, P e) {
        return json.has(e.name());
    }

    /**
     * Check if the value at {@code key} is a {@link Boolean}.
     *
     * @param json
     *            {@link JSONObject} to inspect
     * @param key
     *            Item in object to check
     * @return {@code True} if the item exists and is a {@code Boolean};
     *         {@code false} otherwise
     */
    public static boolean isBoolean(final JSONObject json, final String key) {
        return isInstanceOf(json, key, Boolean.class);
    }

    /**
     * Check if the value at {@code key} is a {@link Boolean} or can,
     * optionally, be coerced into a {@code Boolean}.
     *
     * @param json
     *            {@link JSONObject} to inspect
     * @param key
     *            Item in object to check
     * @param coerce
     *            If {@code true}, check if the value can be coerced to
     *            {@code Boolean}
     * @return {@code True} if the item exists and is a {@code Boolean};
     *         {@code false} otherwise
     */
    public static boolean isBoolean(final JSONObject json, final String key,
            final boolean coerce) {
        if (!coerce) {
            return isBoolean(json, key);
        }

        // This could be trivially implemented as
        // `return JSON.toBoolean(json.opt(key)) != null`
        // but JSON is not public
        Object o = json.opt(key);
        if (o == null || o == JSONObject.NULL) {
            return false;
        }
        if (o instanceof Boolean) {
            return true;
        }
        if (o instanceof Integer || o instanceof Long) {
            final Long num = (Long) o;
            return num == 0 || num == 1;
        }
        if (o instanceof String) {
            final String s = (String) o;
            return s.compareToIgnoreCase("false") == 0
                    || s.compareToIgnoreCase("true") == 0;
        }
        return false;
    }

    /**
     * Check if the value at {@code key} is a {@link Double}.
     *
     * @param json
     *            {@link JSONObject} to inspect
     * @param key
     *            Item in object to check
     * @return {@code True} if the item exists and is a {@code Double};
     *         {@code false} otherwise
     */
    public static boolean isDouble(final JSONObject json, final String key) {
        return isInstanceOf(json, key, Double.class);
    }

    /**
     * Check if the value at {@code key} is an {@link Integer}.
     *
     * @param json
     *            {@link JSONObject} to inspect
     * @param key
     *            Item in object to check
     * @return {@code True} if the item exists and is an {@code Integer};
     *         {@code false} otherwise
     */
    public static boolean isInt(final JSONObject json, final String key) {
        return isInstanceOf(json, key, Integer.class);
    }

    /**
     * Check if the value at {@code key} is a {@link Long}.
     *
     * @param json
     *            {@code JSONObject} to inspect
     * @param key
     *            Item in object to check
     * @return {@code True} if the item exists and is a {@code Long};
     *         {@code false} otherwise
     */
    public static boolean isLong(final JSONObject json, final String key) {
        return isInstanceOf(json, key, Long.class);
    }

    /**
     * Check if the value at {@code key} is a {@link Number}.
     *
     * @param json
     *            {@link JSONObject} to inspect
     * @param key
     *            Item in object to check
     * @return {@code True} if the item exists and is a {@code Number};
     *         {@code false} otherwise
     */
    public static boolean isNumber(final JSONObject json, final String key) {
        return isInstanceOf(json, key, Number.class);
    }

    /**
     * Check if the value at {@code key} is a {@link Number} or can, optionally,
     * be coerced into a {@code Number}.
     *
     * @param json
     *            {@code JSONObject} to inspect
     * @param key
     *            Item in object to check
     * @param coerce
     *            If {@code true}, check if the value can be coerced to a
     *            {@code Number}
     * @return {@code True} if the item exists and is a {@code Number};
     *         {@code false} otherwise
     */
    public static boolean isNumber(final JSONObject json, final String key,
            final boolean coerce) {
        if (!coerce) {
            return isNumber(json, key);
        }
        final Object o = json.opt(key);
        if (o == null || o == JSONObject.NULL) {
            return false;
        }
        if (o instanceof Number) {
            return true;
        }
        if (o instanceof Boolean) {
            return true;
        }
        if (o instanceof String) {
            final String s = (String) o;
            try {
                Double.valueOf(s);
                return true;
            } catch (NumberFormatException e) {
                Log.e(TAG, e,
                      "isNumber(): failed to coerce value at '%s' (%s)", key, o);
            }
        }
        return false;
    }

    /**
     * Check if the specified {@link JSONObject} has a {@code String} at
     * {@code key}.
     *
     * @param json
     *            {@code JSONObject} to inspect
     * @param key
     *            Item in object to check
     * @return {@code True} if the item exists and is a {@code String};
     *         {@code false} otherwise
     */
    public static boolean isString(final JSONObject json, final String key) {
        return isInstanceOf(json, key, String.class);
    }

    /**
     * Check if the specified {@link JSONObject} has an {@link Enum} at {@code key}.
     * <p>
     * If the item is not naturally an {@code Enum}, checks to see if it is a {@link String} with
     * one of the {@code Enum's} {@linkplain Enum#valueOf(Class, String) values}.
     *
     * @param json
     *            {@code JSONObject} to inspect
     * @param key
     *            Item in object to check
     * @return {@code True} if the item exists and is an {@code Enum};
     *         {@code false} otherwise
     */
    public static <P extends Enum<P>> boolean isEnum(final JSONObject json,
            final String key, final Class<P> enumType) {
        if (isInstanceOf(json, key, enumType)) {
            return true;
        }

        final Object o = json.opt(key);
        if (o == null || o == JSONObject.NULL) {
            return false;
        }

        if (o instanceof String) {
            final String s = (String) o;
            try {
                Enum.valueOf(enumType, s);
                return true;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, e,
                      "isEnum(): failed to coerce value at '%s' to %s (%s)",
                      key, enumType.getSimpleName(), o);
            }
        }
        return false;
    }

    /**
     * Check if the specified {@link JSONObject} has a {@code JSONArray} at
     * {@code key}.
     *
     * @param json
     *            {@code JSONObject} to inspect
     * @param key
     *            Item in object to check
     * @return {@code True} if the item exists and is a {@code JSONArray};
     *         {@code false} otherwise
     */
    public static boolean isJSONArray(final JSONObject json, final String key) {
        return isInstanceOf(json, key, JSONArray.class);
    }

    /**
     * Check if the specified {@link JSONObject} has a {@code JSONObject} at
     * {@code key}.
     *
     * @param json
     *            {@code JSONObject} to inspect
     * @param key
     *            Item in object to check
     * @return {@code True} if the item exists and is a {@code JSONObject};
     *         {@code false} otherwise
     */
    public static boolean isJSONObject(final JSONObject json, final String key) {
        return isInstanceOf(json, key, JSONObject.class);
    }

    /**
     * Check if the {@link JSONObject} has an item at {@code key} that is an
     * instance of {@code type}.
     *
     * @param json
     *            {@code JSONObject} to inspect
     * @param key
     *            Item in object to check
     * @param type
     *            Type to check the item against
     * @return {@code True} if the item exists and is of the specified
     *         {@code type}; {@code false} otherwise
     */
    public static boolean isInstanceOf(final JSONObject json, final String key,
            Class<?> type) {
        Object o = json.opt(key);
        return o != null && o != JSONObject.NULL && type.isInstance(o);
    }

    /**
     * Load a JSON file from the application's "asset" directory.
     *
     * @param context
     *            Valid {@link Context}
     * @param asset
     *            Name of the JSON file
     * @return New instance of {@link JSONObject}
     * @throws JSONException
     */
    public static JSONObject loadJSONAsset(Context context, final String asset)
            throws JSONException {
        return getJsonObject(Utility.readTextFile(context, asset));
    }

    /**
     * Load a JSON file from one of the public directories defined by {@link Environment}.
     *
     * @param publicDirectory
     *            One of the {@code DIRECTORY_*} constants defined by {@code Environment}.
     * @param file
     *            Relative path to file in the public directory.
     * @return New instance of {@link JSONObject}
     * @throws JSONException
     */
    public static JSONObject loadPublicJSONFile(final String publicDirectory, final String file)
            throws JSONException {
        final File dir = Environment.getExternalStoragePublicDirectory(publicDirectory);
        return loadJSONFile(dir, file);
    }

    public static JSONObject loadJSONFile(Context context, final String file) throws JSONException {
        return loadJSONFile(context.getFilesDir(), file);
    }

    public static JSONObject loadJSONFile(Context context, final String directory, final String file) throws JSONException {
        Log.d(TAG, "loadJSONFile(): Context.getDir(): %s", context.getDir(Environment.DIRECTORY_DOCUMENTS, Context.MODE_PRIVATE));
        File dir = new File(context.getFilesDir(), directory);
        return loadJSONFile(dir, file);
    }

    public static JSONObject loadJSONDocument(Context context, final String file) throws JSONException {
        return loadJSONFile(context, Environment.DIRECTORY_DOCUMENTS, file);
    }

    @NonNull
    private static JSONObject loadJSONFile(File dir, String file) throws JSONException {
        String rawJson = null;
        if (dir.exists()) {
            final File f = new File(dir, file);
            if (f.exists()) {
                rawJson = Utility.readTextFile(f);
                Log.d(TAG, "loadJSONFile(): %s", f.getPath());
            } else {
                Log.w(TAG, "loadJSONFile(): file %s doesn't exists", f.getPath());
            }
        } else {
            Log.w(TAG, "loadJSONFile(): directory %s doesn't exists", dir.getPath());
        }

        return getJsonObject(rawJson);
    }

    /**
     * Load a JSON file from {@link Environment#DIRECTORY_DOCUMENTS}.
     *
     * @param file
     *            Relative path to file in "Documents" directory.
     * @return New instance of {@link JSONObject}
     * @throws JSONException
     */
    public static JSONObject loadPublicJSONDocument(final String file) throws JSONException {
        return loadPublicJSONFile(Environment.DIRECTORY_DOCUMENTS, file);
    }

    /**
     * Load a JSON file from a private application directory as defined by {@link Environment}.
     *
     * @param directory
     *            One of the {@code DIRECTORY_*} constants defined by {@code Environment}.
     * @param file
     *            Relative path to file in the public directory.
     * @return New instance of {@link JSONObject}
     * @throws JSONException
     */
    public static JSONObject loadExternalJSONFile(Context context, final String directory,
                                                  final String file) throws JSONException {
        final File dir = context.getExternalFilesDir(directory);
        return loadJSONFile(dir, file);
    }

    /**
     * Load a JSON file from the application's private {@link Environment#DIRECTORY_DOCUMENTS}.
     *
     * @param file
     *            Relative path to file in "Documents" directory.
     * @return New instance of {@link JSONObject}
     * @throws JSONException
     */
    public static JSONObject loadExternalJSONDocument(Context context, final String file) throws JSONException {
        return loadExternalJSONFile(context, Environment.DIRECTORY_DOCUMENTS, file);
    }

    @NonNull
    private static JSONObject getJsonObject(String rawJson) throws JSONException {
        Log.v(TAG, "getJsonObject(): raw JSON: %s", rawJson);
        if (rawJson == null) {
            return new JSONObject();
        }
        return new JSONObject(rawJson);
    }

    /**
     * Create a deep copy of {@code src} in a new {@link JSONArray}.
     * <p>
     * Equivalent to calling {@link #copy(JSONArray, boolean) copy(src, true)}.
     *
     * @param src
     *            {@code JSONArray} to copy
     * @return A new {@code JSONArray} copied from {@code src}
     */
    public static JSONArray copy(final JSONArray src) {
        final JSONArray dest = new JSONArray();
        final int len = src.length();
        for (int i = 0; i < len; ++i) {
            dest.put(src.opt(i));
        }
        return dest;
    }

    /**
     * Copies {@code src} into a new {@link JSONArray}.
     *
     * @param src
     *            {@code JSONArray} to copy
     * @param deep
     *            {@code True} to perform a deep copy, {@code false} to perform
     *            a shallow copy
     * @return A new {@code JSONArray} copied from {@code src}
     */
    public static JSONArray copy(final JSONArray src, final boolean deep) {
        final JSONArray dest = new JSONArray();
        final int len = src.length();
        for (int i = 0; i < len; ++i) {
            final Object value = src.opt(i);
            if (deep) {
                if (value instanceof JSONObject) {
                    dest.put(copy((JSONObject) value));
                } else if (value instanceof JSONArray) {
                    dest.put(copy((JSONArray) value));
                } else {
                    dest.put(value);
                }
            } else {
                dest.put(value);
            }
        }
        return dest;
    }

    /**
     * Create a deep copy of {@code src} in a new {@link JSONObject}.
     * <p>
     * Equivalent to calling {@link #copy(JSONObject, boolean) copy(src, true)}.
     *
     * @param src
     *            {@code JSONObject} to copy
     * @return A new {@code JSONObject} copied from {@code src}
     */
    public static JSONObject copy(final JSONObject src) {
        return copy(src, true);
    }

    /**
     * Copies {@code src} into a new {@link JSONObject}.
     *
     * @param src
     *            {@code JSONObject} to copy
     * @param deep
     *            {@code True} to perform a deep copy, {@code false} to perform
     *            a shallow copy
     * @return A new {@code JSONObject} copied from {@code src}
     */
    public static JSONObject copy(final JSONObject src, final boolean deep) {
        final JSONObject dest = new JSONObject();
        Iterator<String> keys = src.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            final Object value = src.opt(key);
            if (deep) {
                if (value instanceof JSONObject) {
                    safePut(dest, key, copy((JSONObject) value));
                } else if (value instanceof JSONArray) {
                    safePut(dest, key, copy((JSONArray) value));
                } else {
                    safePut(dest, key, value);
                }
            } else {
                safePut(dest, key, value);
            }
        }
        return null;
    }

    /**
     * Merges values from {@code src} into {@code dest}. {@link JSONObject} and
     * {@link JSONArray} values are {@linkplain #copy(JSONObject) deep copied}.
     * Additional values in {@code src} are appended to {@code dest}.
     * <p>
     * Values at matching indices are overwritten in {@code dest}; if
     * {@code src} is longer than {@dest}, this turns {@dest} into a copy of
     * {@src}.
     *
     * @param src
     *            {@code JSONArray} to merge from
     * @param dest
     *            {@code JSONArray} to merge to
     * @return The modified {@code dest} array.
     */
    public static JSONArray merge(final JSONArray src, final JSONArray dest) {
        return merge(src, dest, true);
    }

    /**
     * Merges values from {@code src} into {@code dest}. {@link JSONObject} and
     * {@link JSONArray} values are {@linkplain #copy(JSONObject) deep copied}.
     * Null values at matching indices are overwritten in {@code dest}.
     * Additional values in {@code src} are appended to {@code dest}.
     * <p>
     * Optionally, non-null values at matching indices may be overwritten in
     * {@code dest}; if enabled and {@code src} is longer than {@dest}, this
     * turns {@dest} into a copy of {@src}.
     *
     * @param src
     *            {@code JSONArray} to merge from
     * @param dest
     *            {@code JSONArray} to merge to
     * @param overwrite
     *            If {@code true}, overwrites non-null values in {@code dest}
     * @return The modified {@code dest} array.
     */
    public static JSONArray merge(JSONArray src, JSONArray dest,
            boolean overwrite) {
        return merge(src, dest, overwrite, true);
    }

    /**
     * Merges values from {@code src} into {@code dest}. Null values at matching
     * indices are overwritten in {@code dest}. Additional values in {@code src}
     * are appended to {@code dest}.
     * <p>
     * Optionally, non-null values at matching indices may be overwritten in
     * {@code dest}; if enabled and {@code src} is longer than {@dest}, this
     * turns {@dest} into a copy of {@src}.
     * <p>
     * Optionally, {@link JSONObject} and {@link JSONArray} values are
     * {@linkplain #copy(JSONObject) deep copied}.
     *
     * @param src
     *            {@code JSONArray} to merge from
     * @param dest
     *            {@code JSONArray} to merge to
     * @param overwrite
     *            If {@code true}, overwrites non-null values in {@code dest}
     * @param deep
     *            If {@code true}, makes deep copies of any {@code JSONObject}
     *            and {@code JSONArray} values
     * @return The modified {@code dest} array.
     */
    public static JSONArray merge(JSONArray src, JSONArray dest,
            final boolean overwrite, boolean deep) {
        final int srcLen = src.length();
        final int destLen = dest.length();
        int i = 0;
        try {
            for (; i < srcLen && i < destLen; ++i) {
                final Object destVal = dest.get(i);
                if (destVal == null || destVal == JSONObject.NULL || overwrite) {
                    Object value = src.get(i);
                    if (deep) {
                        if (value instanceof JSONObject) {
                            value = copy((JSONObject) value);
                        } else if (value instanceof JSONArray) {
                            value = copy((JSONArray) value);
                        }
                    }
                    dest.put(i, value);
                }
            }
            for (; i < srcLen; ++i) {
                dest.put(src.get(i));
            }
        } catch (JSONException e) {
            Log.e(TAG, e, "merge(): This shouldn't be able to happen! (at %d)",
                  i);
        }
        return dest;
    }

    /**
     * Merges values from {@code src} into a copy of {@code dest}.
     * See {@link #merge(JSONArray, JSONArray)}.
     *
     * @param src
     *            {@code JSONArray} to merge from
     * @param dest
     *            {@code JSONArray} to merge to
     * @return The modified {@code dest} array.
     */
    public static JSONArray merge(final JSONArray src,
            final UnmodifiableJSONArray dest) {
        return merge(src, copy(dest));
    }

    /**
     * Merges values from {@code src} into a copy of {@code dest}.
     * See {@link #merge(JSONArray, JSONArray, boolean)}.
     *
     * @param src
     *            {@code JSONArray} to merge from
     * @param dest
     *            {@code JSONArray} to merge to
     * @param overwrite
     *            If {@code true}, overwrites non-null values in {@code dest}
     * @return The modified {@code dest} array.
     */
    public static JSONArray merge(JSONArray src, UnmodifiableJSONArray dest,
            boolean overwrite) {
        return merge(src, copy(dest), overwrite);
    }

    /**
     * Merges values from {@code src} into a copy of {@code dest}.
     * See {@link #merge(JSONArray, JSONArray, boolean, boolean)}.
     *
     * @param src
     *            {@code JSONArray} to merge from
     * @param dest
     *            {@code JSONArray} to merge to
     * @param overwrite
     *            If {@code true}, overwrites non-null values in {@code dest}
     * @param deep
     *            If {@code true}, makes deep copies of any {@code JSONObject}
     *            and {@code JSONArray} values
     * @return The modified {@code dest} array.
     */
    public static JSONArray merge(JSONArray src, UnmodifiableJSONArray dest,
            boolean overwrite, boolean deep) {
        return merge(src, copy(dest), overwrite, deep);
    }

    /**
     * Recursively merges values from {@code src} into {@code dest}, overwriting
     * values in {@code dest} when matching keys are present in {@code src}.
     * Deep copies unmerged {@link JSONObject JSONObjects} and {@link JSONArray
     * JSONArrays}.
     * <p>
     * Equivalent to {@link #merge(JSONObject, JSONObject, boolean) merge(src,
     * dest, true)}.
     *
     * @param src
     *            {@code JSONObject} to merge from
     * @param dest
     *            {@code JSONObject} to merge to
     * @return The modified {@code dest} object
     */
    public static JSONObject merge(JSONObject src, JSONObject dest) {
        return merge(src, dest, true);
    }

    /**
     * Recursively merges values from {@code src} into {@code dest}, optionally
     * overwriting values of matching keys.
     * <p>
     * If {@code overwrite} is {@code true}, any matching keys in {@code dest}
     * will have their values overwritten by values from {@code src}. If the
     * values of the matching keys are both {@link JSONObject JSONObjects} or
     * {@link JSONArray JSONArrays}, those objects will be merged; otherwise,
     * the value in {@code dest} will be overwritten.
     * <p>
     * Any {@code JSONObject} or {@code JSONArray} values that are not merged
     * will be {@linkplain #copy(JSONObject, boolean) deep copied}.
     *
     * @param src
     *            {@code JSONObject} to merge from
     * @param dest
     *            {@code JSONObject} to merge to
     * @param overwrite
     *            If {@code true}, overwrites matching keys in {@code dest}
     * @return The modified {@code dest} object
     */
    public static JSONObject merge(JSONObject src, JSONObject dest,
            final boolean overwrite) {
        final Iterator<String> keys = src.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            final Object value = src.opt(key);
            if (!dest.has(key)) {
                safePut(dest, key, value);
            } else if (value instanceof JSONObject) {
                mergeSubObject(dest, key, (JSONObject) value, overwrite);
            } else if (value instanceof JSONArray) {
                mergeSubArray(dest, key, (JSONArray) value);
            } else if (overwrite) {
                safePut(dest, key, value);
            }
        }
        return dest;
    }

    /**
     * Recursively merges values from {@code src} into a copy of {@code dest}.
     *
     * @param src
     *            {@code JSONObject} to merge from
     * @param dest
     *            {@code JSONObject} to merge to
     * @return A copy of {@code dest} with {@code src} merged in
     * @see #merge(JSONObject, JSONObject)
     */
    public static JSONObject merge(JSONObject src, UnmodifiableJSONObject dest) {
        return merge(src, copy(dest));
    }

    /**
     * Recursively merges values from {@code src} into a copy of {@code dest},
     * optionally overwriting values of matching keys.
     *
     * @param src
     *            {@code JSONObject} to merge from
     * @param dest
     *            {@code JSONObject} to merge to
     * @param overwrite
     *            If {@code true}, overwrites matching keys in {@code dest}
     * @return A copy of {@code dest} with {@code src} merged in
     * @see #merge(JSONObject, JSONObject, boolean)
     */
    public static JSONObject merge(JSONObject src, UnmodifiableJSONObject dest,
            boolean overwrite) {
        return merge(src, copy(dest), overwrite);
    }

    private JSONHelpers() {

    }

    private static void mergeSubArray(JSONObject dest, final String key,
            JSONArray value) {
        final JSONArray subArray = dest.optJSONArray(key);
        if (subArray != null) {
            merge(value, subArray);
        } else {
            safePut(dest, key, copy(value));
        }
    }

    private static void mergeSubObject(JSONObject dest, final String key,
            JSONObject value, final boolean overwrite) {
        final JSONObject subObject = dest.optJSONObject(key);
        if (subObject != null) {
            merge(value, subObject, overwrite);
        } else {
            safePut(dest, key, copy(value));
        }
    }

    /**
     * A convenience wrapper for {@linkplain JSONObject#put(String, Object)
     * put()} when {@code key} and {@code value} are both known to be "safe"
     * (i.e., neither should cause the {@code put()} to throw
     * {@link JSONException}). Cuts down on unnecessary {@code try/catch} blocks
     * littering the code and keeps the call stack clean of
     * {@code JSONException} throw declarations.
     *
     * @param dest
     *            {@link JSONObject} to call {@code put()} on
     * @param key
     *            The {@code key} parameter for {@code put()}
     * @param value
     *            The {@code value} parameter for {@code put()}
     * @throws RuntimeException
     *             If either {@code key} or {@code value} turn out to
     *             <em>not</em> be safe.
     */
    private static void safePut(JSONObject dest, final String key,
            final Object value) {
        try {
            dest.put(key, value);
        } catch (JSONException e) {
            throw new RuntimeException("This should not be able to happen!", e);
        }
    }

    private static final String TAG = JSONHelpers.class.getSimpleName();
}
