package com.samsung.smcl.vr.widgets;

import java.io.File;
import java.util.Iterator;

import org.joml.Vector3f;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.samsung.smcl.utility.Log;
import com.samsung.smcl.utility.UnmodifiableJSONArray;
import com.samsung.smcl.utility.UnmodifiableJSONObject;
import com.samsung.smcl.utility.Utility;

abstract public class JSONHelpers {

    /**
     * An {@link UnmodifiableJSONObject} that is returned by several methods if a {@link JSONObject}
     * is not mapped to a specified key.  Helps to avoid a lot of {@code null}-checking logic.
     */
    public static final JSONObject EMPTY_OBJECT = new UnmodifiableJSONObject(new JSONObject());
    /**
     * An {@link UnmodifiableJSONArray} that is returned by several methods if a {@link JSONArray}
     * is not mapped to a specified key.  Helps to avoid a lot of {@code null}-checking logic.
     */
    public static final JSONArray EMPTY_ARRAY = new UnmodifiableJSONArray(new JSONArray());

    public static <P extends Enum<P>> Object get(final JSONObject json, P e)
            throws JSONException {
        return json.get(e.name());
    }

    public static <P extends Enum<P>> Object opt(final JSONObject json, P e) {
        return json.opt(e.name());
    }

    public static final <P extends Enum<P>, R> R get(final JSONObject json, P e, Class<R> r)
            throws JSONException {
        return r.cast(json.get(e.name()));
    }

    public static final <P extends Enum<P>, R> R opt(final JSONObject json, P e, Class<R> r) {
        Object o = opt(json, e);
        if (o != null) {
            return r.cast(o);
        }
        return null;
    }

    public static final <P extends Enum<P>> JSONObject put(JSONObject json, P e, Object value) {
        try {
            return json.put(e.name(), value);
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
    }

    public static <P extends Enum<P>> JSONObject putDefault(final JSONObject json, P e, Object value) {
        if (json != null && !has(json, e)) {
            put(json, e, value);
        }
        return json;
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

    public static <P extends Enum<P>> JSONObject put(JSONObject json, P e, boolean value)
            throws JSONException {
        return json.put(e.name(), value);
    }

    public static <P extends Enum<P>> JSONObject putDefault(final JSONObject json, P e, boolean value) {
        if (!has(json, e)) {
            safePut(json, e, value);
        }
        return json;
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

    public static <P extends Enum<P>> JSONObject put(JSONObject json, P e, double value)
            throws JSONException {
        return json.put(e.name(), value);
    }

    public static <P extends Enum<P>> JSONObject putDefault(final JSONObject json, P e, double value) {
        if (!has(json, e)) {
            try {
                json.put(e.name(), value);
            } catch (JSONException e1) {
                throw new RuntimeException(e1.getLocalizedMessage(), e1);
            }
        }
        return json;
    }

    public static <P extends Enum<P>> float getFloat(final JSONObject json,
                                                       P e) throws JSONException {
        return (float) json.getDouble(e.name());
    }

    public static <P extends Enum<P>> float optFloat(final JSONObject json,
                                                       P e) {
        return (float) json.optDouble(e.name());
    }

    public static <P extends Enum<P>> float optFloat(final JSONObject json,
                                                       P e, double fallback) {
        return (float) json.optDouble(e.name(), fallback);
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

    public static <P extends Enum<P>> JSONObject put(JSONObject json, P e, int value)
            throws JSONException {
        return json.put(e.name(), value);
    }

    public static <P extends Enum<P>> JSONObject putDefault(final JSONObject json, P e, int value) {
        if (!has(json, e)) {
            safePut(json, e, value);
        }
        return json;
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

    public static <P extends Enum<P>> JSONObject put(JSONObject json, P e, long value)
            throws JSONException {
        return json.put(e.name(), value);
    }

    public static <P extends Enum<P>> JSONObject putDefault(final JSONObject json, P e, long value) {
        if (!has(json, e)) {
            safePut(json, e, value);
        }
        return json;
    }

    public static <P extends Enum<P>> String getString(final JSONObject json, P e) {
        try {
            return json.getString(e.name());
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
    }

    /**
     * Returns {@link String} value mapped to {@code e}, or the empty string if the mapping doesn't
     * exist.
     *
     * @param json
     * @param e
     * @return
     */
    public static <P extends Enum<P>> String optString(final JSONObject json,
            P e) {
        return json.optString(e.name());
    }

    public static <P extends Enum<P>> String optString(final JSONObject json,
            P e, String fallback) {
        return json.optString(e.name(), fallback);
    }

    public static <P extends Enum<P>> JSONObject put(JSONObject json, P e, String value) {
        try {
            return json.put(e.name(), value);
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
    }

    public static <P extends Enum<P>, R extends Enum<R>> R getEnum(
            final JSONObject json, P e, Class<R> r) throws JSONException {
        return getEnum(json, e, r, false);
    }

    @NonNull
    public static <P extends Enum<P>, R extends Enum<R>> R getEnum(
            JSONObject json, P e, Class<R> r, boolean uppercase) throws JSONException {
        String value = json.getString(e.name());
        if (uppercase) {
            value = value.toUpperCase();
        }
        return Enum.valueOf(r, value);
    }

    public static <P extends Enum<P>, R extends Enum<R>> R optEnum(
            final JSONObject json, P e, Class<R> r) {
        return optEnum(json, e, r, false);
    }

    @Nullable
    public static <P extends Enum<P>, R extends Enum<R>> R optEnum(
            JSONObject json, P e, Class<R> r, boolean uppercase) {
        String value = json.optString(e.name());
        if (value == null) {
            return null;
        }
        if (uppercase) {
            value = value.toUpperCase();
        }
        return Enum.valueOf(r, value);
    }

    @SuppressWarnings("unchecked")
    public static <P extends Enum<P>, R extends Enum<R>> R optEnum(
            final JSONObject json, P e, R fallback) {
        return optEnum(json, e, fallback, false);
    }

    public static <P extends Enum<P>, R extends Enum<R>> R optEnum(
            JSONObject json, P e, R fallback, boolean uppercase) {
        String value = json.optString(e.name(), fallback.name());
        if (uppercase) {
            value = value.toUpperCase();
        }

        return (R) Enum.valueOf(fallback.getDeclaringClass(), value);
    }

    public static <P extends Enum<P>, V extends Enum<V>> JSONObject put(JSONObject json, P e,
                                                                        V value) throws JSONException {
        return put(json, e, value, false);
    }

    public static <P extends Enum<P>, V extends Enum<V>> JSONObject put(JSONObject json, P e,
                                                                        V value, boolean lowerCase) throws JSONException {
        String strVal = value.name();
        if (lowerCase) {
            strVal = strVal.toLowerCase();
        }
        return json.put(e.name(), strVal);
    }

    public static <P extends Enum<P>, V extends Enum<V>> JSONObject putDefault(
            final JSONObject json, P e, V value) {
        if (!has(json, e)) {
            safePut(json, e, value);
        }
        return json;
    }

    public static <P extends Enum<P>, V extends Enum<V>> JSONObject putDefault(
            final JSONObject json, P e, V value, boolean lowerCase) {
        if (!has(json, e, lowerCase)) {
            safePut(json, e, value, lowerCase);
        }
        return json;
    }

    public static <P extends Enum<P>> JSONObject getJSONObject(
            final JSONObject json, P e) throws JSONException {
        return json.getJSONObject(e.name());
    }

    public static <P extends Enum<P>> JSONObject optJSONObject(
            final JSONObject json, P e) {
        return json.optJSONObject(e.name());
    }

    /**
     * Returns the value mapped by enum if it exists and is a {@link JSONObject}. If the value does
     * not exist by that enum, and {@code emptyForNull} is {@code true}, returns
     * {@link #EMPTY_OBJECT}. Otherwise, returns {@code null}.
     *
     * @param json {@link JSONObject} to get data from
     * @param e {@link Enum} labeling the data to get
     * @param emptyForNull {@code True} to return {@code EMPTY_OBJECT} if there is no mapped data,
     *                                 {@code false} to return {@code null} in that case
     * @return A {@code JSONObject} if the mapping exists; {@code EMPTY_OBJECT} if it doesn't and
     * {@code emptyForNull} is {@code true}; {@code null} otherwise
     */
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


    /**
     * Returns the value mapped by enum if it exists and is a {@link JSONArray}. If the value does not
     * exist by that enum, and {@code emptyForNull} is {@code true}, returns {@link #EMPTY_ARRAY}.
     * Otherwise, returns {@code null}.
     *
     * @param json {@code JSONObject} to get data from
     * @param e {@link Enum} labeling the data to get
     * @param emptyForNull {@code True} to return {@code EMPTY_ARRAY} if there is no mapped data,
     *                                 {@code false} to return {@code null} in that case
     * @return A {@code JSONObject} if the mapping exists; {@code EMPTY_ARRAY} if it doesn't and
     * {@code emptyForNull} is {@code true}; {@code null} otherwise
     */
    public static <P extends Enum<P>> JSONArray optJSONArray(final JSONObject json, P e,
                                                             boolean emptyForNull) {
        JSONArray jsonArray = optJSONArray(json, e);
        if (jsonArray == null && emptyForNull) {
            jsonArray = EMPTY_ARRAY;
        }
        return jsonArray;
    }

    public static <P extends Enum<P>> Point getPoint(final JSONObject json, P e) throws JSONException {
        JSONObject value = getJSONObject(json, e);
        return asPoint(value);
    }

    /**
     * Return the value mapped by enum if it exists and is a {@link JSONObject} by mapping "x" and
     * "y" members into a {@link Point}.
     *
     * @param json {@code JSONObject} to get data from
     * @param e {@link Enum} labeling the data to get
     * @return An instance of {@code Point} or {@null} if there is no object mapping for {@code e}.
     */
    public static <P extends Enum<P>> Point optPoint(final JSONObject json, P e) {
        return optPoint(json, e, null);
    }

    /**
     * Return the value mapped by enum if it exists and is a {@link JSONObject} by mapping "x" and
     * "y" members into a {@link Point}. The values in {@code fallback} are used if either field is
     * missing; if {@code fallback} is {@code null}, 0 (zero) is used.
     *
     * @param json {@code JSONObject} to get data from
     * @param e {@link Enum} labeling the data to get
     * @param fallback Default value to return if there is no mapping
     * @return An instance of {@code Point} or {@code fallback} if there is no object mapping for
     * {@code e}.
     */
    public static <P extends Enum<P>> Point optPoint(final JSONObject json, P e, Point fallback) {
        JSONObject value = optJSONObject(json, e);
        Log.d(TAG, "optPoint(): raw: %s", value);
        Point p = asPoint(value, fallback);

        return p;
    }

    public static Point asPoint(JSONObject json) {
        return asPoint(json, new Point());
    }

    public static Point asPoint(JSONObject json, Point defValue) {
        Point p = defValue;
        if (json != null && isPoint(json)) {
            int x = json.optInt("x", defValue != null ? defValue.x : 0);
            int y = json.optInt("y", defValue != null ? defValue.y : 0);
            Log.d(TAG, "optPoint(): x: %d, y: %d", x, y);
            p = new Point(x, y);
        }
        return p;
    }

    /**
     * Return the value mapped by enum if it exists and is a {@link JSONObject} by mapping "x" and
     * "y" members into a {@link Point}. If the value does not exist by that enum, and
     * {@code emptyForNull} is {@code true}, returns a default constructed {@code Point}. Otherwise,
     * returns {@code null}.
     *
     * @param json {@code JSONObject} to get data from
     * @param e {@link Enum} labeling the data to get
     * @param emptyForNull {@code True} to return a default constructed {@code Point} if there is no
     *                                 mapped data, {@code false} to return {@code null} in that case
     * @return A {@code Point} if the mapping exists or {@code emptyForNull} is {@code true};
     * {@code null} otherwise
     */
    public static <P extends Enum<P>> Point optPoint(final JSONObject json, P e, boolean emptyForNull) {
        Point p = optPoint(json, e);
        if (p == null && emptyForNull) {
            p = new Point();
        }
        return p;
    }

    public static <P extends Enum<P>> PointF getPointF(final JSONObject json, P e) throws JSONException {
        JSONObject value = getJSONObject(json, e);
        return asPointF(value);
    }

    /**
     * Return the value mapped by enum if it exists and is a {@link JSONObject} by mapping "x" and
     * "y" members into a {@link PointF}.
     *
     * @param json {@code JSONObject} to get data from
     * @param e {@link Enum} labeling the data to get
     * @return An instance of {@code PointF} or {@code null} if there is no object mapping for
     *          {@code e}.
     */
    public static <P extends Enum<P>> PointF optPointF(final JSONObject json, P e) {
        return optPointF(json, e, null);
    }

    /**
     * Return the value mapped by enum if it exists and is a {@link JSONObject} by mapping "x" and
     * "y" members into a {@link PointF}.  The values in {@code fallback} are used if either field
     * is missing; if {@code fallback} is {@code null}, {@link Float#NaN} is used.
     *
     * @param json {@code JSONObject} to get data from
     * @param e {@link Enum} labeling the data to get
     * @param fallback Default value to return if there is no mapping
     * @return An instance of {@code PointF} or {@code fallback} if there is no object mapping for
     * {@code e}.
     */
    public static <P extends Enum<P>> PointF optPointF(final JSONObject json, P e, PointF fallback) {
        JSONObject value = optJSONObject(json, e);
        PointF p = asPointF(value, fallback);

        return p;
    }

    public static PointF asPointF(JSONObject json) {
        return asPointF(json, new PointF());
    }

    public static PointF asPointF(JSONObject json, PointF fallback) {
        PointF p = fallback;
        if (json != null && isPoint(json)) {
            float x = (float) json.optDouble("x", fallback != null ? fallback.x : Float.NaN);
            float y = (float) json.optDouble("y", fallback != null ? fallback.y : Float.NaN);

            p = new PointF(x,y);
        }
        return p;
    }

    /**
     * Return the value mapped by enum if it exists and is a {@link JSONObject} by mapping "x" and
     * "y" members into a {@link Point}. If the value does not exist by that enum, and
     * {@code emptyForNull} is {@code true}, returns a default constructed {@code Point}. Otherwise,
     * returns {@code null}.
     *
     * @param json {@code JSONObject} to get data from
     * @param e {@link Enum} labeling the data to get
     * @param emptyForNull {@code True} to return a default constructed {@code Point} if there is no
     *                                 mapped data, {@code false} to return {@code null} in that case
     * @return A {@code Point} if the mapping exists or {@code emptyForNull} is {@code true};
     * {@code null} otherwise
     */
    public static <P extends Enum<P>> PointF optPointF(final JSONObject json, P e, boolean emptyForNull) {
        PointF p = optPointF(json, e);
        if (p == null && emptyForNull) {
            p = new PointF();
        }
        return p;
    }

    public static <P extends Enum<P>> void put(JSONObject json, P e, PointF defPoint) {
        JSONObject defJson = new JSONObject();
        try {
            defJson.put("x", defPoint.x);
            defJson.put("y", defPoint.y);
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
        safePut(json, e, defJson);
    }

    public static <P extends Enum<P>> JSONObject putDefault(JSONObject json, P e, PointF defPoint) {
        if (json != null) {
            JSONObject pointJson = optJSONObject(json, e);
            if (pointJson != null) {
                // Check both members; default if missing
                try {
                    if (!hasNumber(pointJson, "x", true)) {
                        pointJson.put("x", defPoint.x);
                    }
                    if (!hasNumber(pointJson, "y", true)) {
                        pointJson.put("y", defPoint.y);
                    }
                } catch (JSONException e1) {
                    throw new RuntimeException(e1);
                }
            } else {
                put(json, e, defPoint);
            }
        }
        return json;
    }

    /**
     * Checks whether the value mapped by enum exists, is a {@link JSONObject}, has at least one
     * field named either "x" or "y", and that if either field is present, it is a number.  If at
     * least one of the fields is present, it can be assumed that the missing field is defaulted to
     * zero.  If both are missing, it's ambiguous, at best, whether the object in question can
     * reasonably be treated as a {@link Point}.  This specification is permissive -- there can be
     * other, non-{@code Point}, fields present.  In that case, it can be considered that the object
     * <em>extends</em> {@code Point}.
     *
     * @param json {@code JSONObject} to check
     * @param e {@link Enum} labeling the data to check
     * @return {@code True} if the mapping exists and meets the conditions above; {@code false}
     * otherwise.
     */
    public static <P extends Enum<P>> boolean hasPoint(final JSONObject json, P e) {
        Object o = opt(json, e);
        return o != null && o != JSONObject.NULL && o instanceof JSONObject &&
                isPoint((JSONObject) o);
    }

    public static boolean isPoint(JSONObject jo) {
        Object x = jo.opt("x");
        Object y = jo.opt("y");
        Log.d(TAG, "isPoint(): x: %s, y: %s", x, y);
        if (x == null && y == null) return false;
        if (x != null && !(x instanceof Number)) return false;
        if (y != null && !(y instanceof Number)) return false;

        return true;
    }

    public static <P extends Enum<P>> Vector3f optVector3f(JSONObject json, P e) {
        return optVector3f(json, e, null);
    }

    public static <P extends Enum<P>> Vector3f optVector3f(JSONObject json, P e, Vector3f fallback) {
        JSONObject value = optJSONObject(json, e);

        return asVector3f(value, fallback);
    }

    public static <P extends Enum<P>> JSONObject putDefault(JSONObject json, P e, Vector3f defVector) {
        if (json != null && !has(json, e)) {
            JSONObject defJson = new JSONObject();
            try {
                defJson.put("x", defVector.x);
                defJson.put("y", defVector.y);
                defJson.put("z", defVector.z);
            } catch (JSONException e1) {
                throw new RuntimeException(e1.getLocalizedMessage(), e1);
            }
            safePut(json, e, defJson);
        }
        return json;
    }

    public static Vector3f asVector3f(JSONObject value, Vector3f fallback) {
        Vector3f v = fallback;
        if (value != null && isVector3f(value)) {
            float x = (float) value.optDouble("x", fallback != null ? fallback.x : Float.NaN);
            float y = (float) value.optDouble("y", fallback != null ? fallback.y : Float.NaN);
            float z = (float) value.optDouble("z", fallback != null ? fallback.z : Float.NaN);

            v = new Vector3f(x, y, z);
        }
        return v;
    }

    public static <P extends Enum<P>> Vector3f optVector3f(final JSONObject json, P e,
                                                           boolean emptyForNull) {
        Vector3f v = optVector3f(json, e);
        if (v == null && emptyForNull) {
            v = new Vector3f();
        }
        return v;
    }
    public static <P extends Enum<P>> boolean hasVector3f(final JSONObject json, P e) {
        Object o = opt(json, e);
        return o != null && o != JSONObject.NULL && o instanceof JSONObject &&
                isVector3f((JSONObject) o);
    }

    public static boolean isVector3f(JSONObject jo) {
        Object x = jo.opt("x");
        Object y = jo.opt("y");
        Object z = jo.opt("z");
        Log.d(TAG, "isVector3f(): x: %s, y: %s, z: %s", x, y, z);
        if (x == null && y == null && z == null) return false;
        if (x != null && !(x instanceof Number)) return false;
        if (y != null && !(y instanceof Number)) return false;
        if (z != null && !(z instanceof Number)) return false;

        return true;
    }

    public static <P extends Enum<P>> boolean has(final JSONObject json, P e) {
        return has(json, e, false);
    }

    private static <P extends Enum<P>> boolean has(JSONObject json, P e, boolean lowerCase) {
        String name = e.name();
        if (lowerCase) {
            name = name.toLowerCase();
        }
        return json.has(name);
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
    public static boolean hasBoolean(final JSONObject json, final String key) {
        return hasInstanceOf(json, key, Boolean.class);
    }

    public static <P extends Enum<P>> boolean hasBoolean(final JSONObject json, P e) {
        return hasBoolean(json, e.name());
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
    public static boolean hasBoolean(final JSONObject json, final String key,
                                     final boolean coerce) {
        if (!coerce) {
            return hasBoolean(json, key);
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
    public static boolean hasDouble(final JSONObject json, final String key) {
        return hasInstanceOf(json, key, Double.class);
    }

    public static <P extends Enum<P>> boolean hasDouble(final JSONObject json, P e) {
        return hasDouble(json, e.name());
    }

    /**
     * Alias for {@link #hasDouble(JSONObject, String)}.
     */
    public static boolean hasFloat(final JSONObject json, final String key) {
        return hasDouble(json, key);
    }

    public static <P extends Enum<P>> boolean hasFloat(final JSONObject json, P e) {
        return hasDouble(json, e);
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
    public static boolean hasInt(final JSONObject json, final String key) {
        return hasInstanceOf(json, key, Integer.class);
    }

    public static <P extends Enum<P>> boolean hasInt(final JSONObject json, P e) {
        return hasInt(json, e.name());
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
    public static boolean hasLong(final JSONObject json, final String key) {
        return hasInstanceOf(json, key, Long.class);
    }

    public static <P extends Enum<P>> boolean hasLong(final JSONObject json, P e) {
        return hasLong(json, e.name());
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
    public static boolean hasNumber(final JSONObject json, final String key) {
        return hasInstanceOf(json, key, Number.class);
    }

    public static <P extends Enum<P>> boolean hasNumber(final JSONObject json, P e) {
        return hasNumber(json, e.name());
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
    public static boolean hasNumber(final JSONObject json, final String key,
                                    final boolean coerce) {
        if (!coerce) {
            return hasNumber(json, key);
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
                      "hasNumber(): failed to coerce value at '%s' (%s)", key, o);
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
    public static boolean hasString(final JSONObject json, final String key) {
        return hasInstanceOf(json, key, String.class);
    }

    public static <P extends Enum<P>> boolean hasString(final JSONObject json, P e) {
        return hasString(json, e.name());
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
    public static <P extends Enum<P>> boolean hasEnum(final JSONObject json,
                                                      final String key, final Class<? extends Enum> enumType) {
        if (hasInstanceOf(json, key, enumType)) {
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
                      "hasEnum(): failed to coerce value at '%s' to %s (%s)",
                      key, enumType.getSimpleName(), o);
            }
        }
        return false;
    }

    public static <P extends Enum<P>> boolean hasEnum(final JSONObject json, P e,
                                                      Class<? extends Enum> enumType) {
        return hasEnum(json, e.name(), enumType);
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
    public static boolean hasJSONArray(final JSONObject json, final String key) {
        return hasInstanceOf(json, key, JSONArray.class);
    }

    public static <P extends Enum<P>> boolean hasJSONArray(final JSONObject json, P e) {
        return hasJSONArray(json, e.name());
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
    public static boolean hasJSONObject(final JSONObject json, final String key) {
        return hasInstanceOf(json, key, JSONObject.class);
    }

    public static <P extends Enum<P>> boolean hasJSONObject(final JSONObject json, P e) {
        return hasJSONObject(json, e.name());
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
    public static boolean hasInstanceOf(final JSONObject json, final String key,
                                        Class<?> type) {
        Object o = json.opt(key);
        return isInstanceOf(o, type);
    }

    public static <P extends Enum<P>> boolean hasInstanceOf(final JSONObject json, P e, Class<?> type) {
        return hasInstanceOf(json, e.name(), type);
    }

    public static boolean isInstanceOf(Object o, Class<?> type) {
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
        return dest;
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

    private static <P extends Enum<P>> void safePut(JSONObject dest, P e, Object value) {
        safePut(dest, e, value, false);
    }

    private static <P extends Enum<P>> void safePut(JSONObject dest, P e, Object value, boolean lowerCase) {
        String name = e.name();
        if (lowerCase) {
            name = name.toLowerCase();
        }
        safePut(dest, name, value);
    }

    private static final String TAG = JSONHelpers.class.getSimpleName();
}
