package com.samsung.smcl.vr.widgetlib.widget;

import android.view.MotionEvent;

import com.samsung.smcl.vr.widgetlib.log.Log;

import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IPickEvents;
import org.gearvrf.ITouchEvents;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class WidgetPickHandler implements GVRInputManager.ICursorControllerSelectListener {

    @Override
    public void onCursorControllerSelected(GVRCursorController newController,
                                           GVRCursorController oldController) {
        if (oldController != null) {
            Log.d(Log.SUBSYSTEM.INPUT, TAG, "onCursorControllerSelected(): removing from old controller");
            oldController.removePickEventListener(this);
        }
        Log.d(Log.SUBSYSTEM.INPUT, TAG, "onCursorControllerSelected(): adding to new controller");
        GVRPicker picker = newController.getPicker();
        picker.setPickClosest(false);
        newController.addPickEventListener(new PickEventsListener());
        newController.addPickEventListener(new TouchEventsListener());
        newController.setEnable(true);
    }

    private static class Selection {
        final GVRPicker.GVRPickedObject hit;
        final Widget focusWidget;

        Selection(GVRPicker.GVRPickedObject hit, Widget widget) {
            this.hit = hit;
            focusWidget = widget;
        }
    }

    static private class PickEventsListener implements IPickEvents {

        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
            Log.d(Log.SUBSYSTEM.FOCUS, TAG, "onEnter(%s)", sceneObj.getName());
            Widget widget = WidgetBehavior.getTarget(sceneObj);

            if (widget != null && widget.isFocusEnabled()) {
                Selection sel = findSelected(sceneObj);
                if (sel == null) {
                    Log.d(Log.SUBSYSTEM.FOCUS, TAG, "onEnter(%s): select widget %s",
                            sceneObj.getName(), widget.getName());
                    mSelected.add(new Selection(collision, widget));
                }
            }
        }

        public void onExit(GVRSceneObject sceneObj) {
            if (sceneObj != null) {
                Log.d(Log.SUBSYSTEM.FOCUS, TAG, "onExit(%s)", sceneObj.getName());
                Selection sel = removeSelected(sceneObj);
                if (sel != null) {
                    sel.focusWidget.dispatchOnFocus(false);
                    Log.e(Log.SUBSYSTEM.FOCUS, TAG, "onExit(%s) deselect", sceneObj.getName());
                }
            }
        }

        public void onPick(GVRPicker picker) {
            if (!picker.hasPickListChanged()) {
                return;
            }

            GVRPicker.GVRPickedObject[] picked = picker.getPicked();

            for (GVRPicker.GVRPickedObject hit : picked) {
                GVRSceneObject hitObj = hit.hitObject;

                Selection sel = findSelected(hitObj);
                if (sel == null) {
                    continue;
                }
                Widget widget = sel.focusWidget;

                if (widget.isFocused() || widget.dispatchOnFocus(true)) {
                    Log.d(Log.SUBSYSTEM.FOCUS, TAG, "onPick(%s) widget focused",
                            widget.getName());
                    break;
                }
            }
        }

        public void onNoPick(GVRPicker picker) {
            Log.d(Log.SUBSYSTEM.FOCUS, TAG, "onNoPick()");
            if (picker.hasPickListChanged()) {
                Log.d(Log.SUBSYSTEM.FOCUS, TAG, "onNoPick(): selection cleared");
                mSelected.clear();
            }
        }

        private Selection findSelected(GVRSceneObject hitObject) {
            for (Selection sel : mSelected) {
                if (sel.hit.hitObject == hitObject) {
                    return sel;
                }
            }
            return null;
        }

        private Selection removeSelected(GVRSceneObject hitObject) {
            Iterator<Selection> iter = mSelected.iterator();
            while (iter.hasNext()) {
                Selection sel = iter.next();
                if (sel.hit.hitObject == hitObject) {
                    iter.remove();
                    return sel;
                }
            }
            return null;
        }

        private final List<Selection> mSelected = new ArrayList<>();

        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
        }
    }

    static private class TouchEventsListener implements ITouchEvents {

        public void onTouchStart(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
            Log.d(Log.SUBSYSTEM.INPUT, TAG, "onTouchStart(%s)", sceneObj.getName());
            Widget widget = WidgetBehavior.getTarget(sceneObj);

            if (widget != null && widget.isTouchable() && !mTouched.contains(widget)) {
                Log.d(Log.SUBSYSTEM.INPUT, TAG, "onTouchStart(%s) start touch widget %s",
                        sceneObj.getName(), widget.getName());
                mTouched.add(widget);
            }
        }

        public void onTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
            Log.d(Log.SUBSYSTEM.INPUT, TAG, "onTouchEnd(%s)", sceneObj.getName());
            Widget widget = WidgetBehavior.getTarget(sceneObj);

            if (widget != null && widget.isTouchable() && mTouched.contains(widget)) {
                if (widget.dispatchOnTouch(sceneObj, collision.hitLocation)) {
                    Log.d(Log.SUBSYSTEM.INPUT, TAG, "onTouchEnd(%s) end touch widget %s",
                            sceneObj.getName(), widget.getName());
                    mTouched.clear();
                } else {
                    mTouched.remove(widget);
                }
            }
        }

        public void onExit(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
        }

        public void onMotionOutside(GVRPicker picker, MotionEvent event) {
            Log.d(Log.SUBSYSTEM.INPUT, TAG, "onMotionOutside()");
        }

        private final List<Widget> mTouched = new ArrayList<>();

        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
        }

        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
        }
    }

    private static final String TAG = WidgetPickHandler.class.getSimpleName();
}
