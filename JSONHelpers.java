package com.samsung.smcl.vr.widgets;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

abstract public class JSONHelpers {

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

    public static <P extends Enum<P>> JSONArray getJSONArray(
            final JSONObject json, P e) throws JSONException {
        return json.getJSONArray(e.name());
    }

    public static <P extends Enum<P>> JSONArray optJSONArray(
            final JSONObject json, P e) {
        return json.optJSONArray(e.name());
    }

    public static <P extends Enum<P>> boolean has(final JSONObject json, P e) {
        return json.has(e.name());
    }

    private JSONHelpers() {

    }
}
