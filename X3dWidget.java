package com.samsung.smcl.vr.widgets;


import com.samsung.smcl.utility.Log;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import java.util.List;

public class X3dWidget extends Widget {
    private static final String TAG = X3dWidget.class.getSimpleName();
    private List<String> clickableNodes;
    private OnNodeClickListener nodeClickListener;
    OnTouchListener touchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(Widget widget) {
            if (clickableNodes.contains(widget.getName())) {
                if (nodeClickListener != null) {
                    nodeClickListener.onClick(widget.getName());
                    return true;
                }
            }
            return false;
        }
    };

    public X3dWidget(GVRContext context, GVRSceneObject sceneObject) throws InstantiationException {
        super(context, sceneObject, null);

        // c.f. Mihail's comment @http://mcl-redmine.sisa.samsung.com/redmine/issues/21474
        getGVRContext().getMainScene().bindShaders(sceneObject);

        setName(TAG);
    }

    /**
     * Register the list of nodes interested to listen for click events.
     *
     * @param clickableNodes list of clickable nodes
     * @param clickListener  listener callback to call when a node is clicked
     */
    public void setClickableNodes(List<String> clickableNodes, OnNodeClickListener clickListener) {
        if (clickableNodes == null || clickableNodes.size() == 0) {
            return;
        }

        this.clickableNodes = clickableNodes;
        this.nodeClickListener = clickListener;
        registerNodesForClickEvents(this);
    }

    private void registerNodesForClickEvents(Widget rootWidget) {
        if (rootWidget == null) {
            return;
        }

        for (Widget child : rootWidget.getChildren()) {
            if (clickableNodes.contains(child.getName())) {
                child.addTouchListener(touchListener);
                child.setTouchable(true);
                Log.d(TAG, "Registered click events for X3D node - " + child.getName());
            }
            registerNodesForClickEvents(child);
        }
    }


    /**
     * Interface definition for a callback to be invoked when a node is clicked.
     */
    public interface OnNodeClickListener {
        /**
         * Called when a registered node has been clicked.
         *
         * @param node The node that was clicked.
         */
        void onClick(String node);
    }
}
