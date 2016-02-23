package com.samsung.smcl.vr.widgets;

import static com.samsung.smcl.utility.Exceptions.RuntimeAssertion;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.samsung.smcl.utility.Log;

class WidgetState {
    public enum State {
        NORMAL, FOCUSED, SELECTED, DISABLED
    }

    public WidgetState(final Widget widget, final JSONObject stateSpec) {
        Log.d(TAG, "WidgetState(): states for '%s': %s", widget.getName(), stateSpec);
        if (check(stateSpec, true)) {
            // One or more states are explicitly specified
            loadStates(widget, stateSpec);
        } else if (check(stateSpec, false)) {
            // No explicitly specified state; "NORMAL" is implied
            final WidgetState.State state = State.NORMAL;
            loadState(widget, stateSpec, state);
        } else {
            // Either something is missing, or we've got a mix of fields
            throw RuntimeAssertion("Bad format in state spec for '%s': %s",
                                      widget.getName(),
                                      stateSpec.toString());
        }
    }

    public WidgetState.State getState() {
        return mState;
    }

    public void setState(final Widget widget, final WidgetState.State state) {
        Log.d(TAG, "setState(): for '%s'; is %s, setting to %s", widget.getName(), mState, state);
        if (state != mState) {
            final WidgetState.State nextState = getNextState(state);
            Log.d(TAG, "setState(): setting '%s' for '%s'", nextState, widget.getName());
            if (nextState != mState) {
                setCurrentState(widget, false);
                mState = nextState;
                setCurrentState(widget, true);
            }
        }
    }

    private void setCurrentState(final Widget widget, boolean set) {
        if (mState != null) {
            final WidgetStateInfo stateInfo = mStates.get(mState);
            if (stateInfo != null) {
                stateInfo.set(widget, set);
            }
        }
    }

    private WidgetState.State getNextState(final WidgetState.State state) {
        if (state == null) {
            return null;
        } else if (mStates.containsKey(state)) {
            return state;
        } else {
            // Default to NORMAL for anything that hasn't been specified
            return State.NORMAL;
        }
    }

    private boolean check(JSONObject stateSpec, boolean isExplicit) {
        final boolean hasExplicitStates = has(stateSpec, State.SELECTED)
                || has(stateSpec, State.DISABLED)
                || has(stateSpec, State.FOCUSED)
                || has(stateSpec, State.NORMAL);

        final boolean isImplicitNormal = has(stateSpec,
                                             WidgetStateInfo.Properties.animation)
                || has(stateSpec, WidgetStateInfo.Properties.material)
                || has(stateSpec, WidgetStateInfo.Properties.scene_object);

        return (hasExplicitStates == isExplicit)
                && (isImplicitNormal != isExplicit);
    }

    private boolean has(JSONObject spec, Enum<?> e) {
        return spec.has(e.name().toLowerCase(Locale.ENGLISH));
    }

    private void loadStates(final Widget widget, final JSONObject stateSpecs) {
        Log.d(TAG, "loadStates(): for '%s': %s", widget.getName(), stateSpecs);
        if (stateSpecs != null) {
            Iterator<String> iter = stateSpecs.keys();
            while (iter.hasNext()) {
                final String key = iter.next();
                loadState(widget, stateSpecs, key);
            }
        }
    }

    private void loadState(final Widget widget, JSONObject states,
            String key) {
        try {
            final JSONObject stateSpec = states.getJSONObject(key);
            Log.d(TAG, "loadState(): for state '%s': %s", key, stateSpec);
            key = key.toUpperCase(Locale.ENGLISH);
            final WidgetState.State state = State.valueOf(key);
            loadState(widget, stateSpec, state);
        } catch (JSONException e) {
            throw RuntimeAssertion(e, "Invalid state spec for '%s': %s",
                                      widget.getName(), states.opt(key));
        }
    }

    private void loadState(final Widget widget, final JSONObject stateSpec,
            final WidgetState.State state) {
        try {
            mStates.put(state, new WidgetStateInfo(widget, stateSpec));
        } catch (Exception e) {
            throw RuntimeAssertion(e,
                                      "Failed to load state '%s' for '%s'",
                                      state, widget.getName());
        }
    }

    private WidgetState.State mState = null;
    private final Map<WidgetState.State, WidgetStateInfo> mStates = new HashMap<WidgetState.State, WidgetStateInfo>();

    private final static String TAG = WidgetState.class.getSimpleName();
}