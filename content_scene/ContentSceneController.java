package com.samsung.smcl.vr.widgets.content_scene;

import android.app.Activity;
import java.util.LinkedList;

import org.gearvrf.GVRContext;

import com.samsung.smcl.vr.widgets.main.Holder;
import com.samsung.smcl.vr.widgets.main.HolderHelper;
import com.samsung.smcl.vr.gvrf_launcher.LauncherViewManager;
import com.samsung.smcl.vr.widgets.main.MainScene;
import com.samsung.smcl.vr.widgets.thread.MainThread;
import com.samsung.smcl.vr.widgets.log.Log;
import com.samsung.smcl.vr.widgets.thread.ExecutionChain;

public class ContentSceneController implements LauncherViewManager.OnInitListener {

    @Override
    public void onInit(GVRContext context, MainScene scene) {
        gvrContext = context;
    }

    @Override
    public void onPostInit() {

    }

    /**
     * An entity that can be subjected to management by the
     * ContentSceneController
     */
    public interface ContentScene {
        /**
         * Make this content scene visible and interact-able by the user
         */
        void show();

        void hide();

        void onSystemDialogRemoved();

        void onSystemDialogPosted();

        void onProximityChange(boolean onProximity);

        String getName();
    }

    static public ContentSceneController get(Activity activity) {
        return ((Holder) activity).get(ContentSceneController.class);
    }

    static public ContentSceneController get(GVRContext gvrContext) {
        ContentSceneController sceneController = null;
        if (gvrContext != null) {
            Activity activity = gvrContext.getActivity();
            sceneController = get(activity);
        }

        return sceneController;
    }

    /**
     * Creates ContentSceneController
     * An instance of {@link Holder} must be supplied and can only be associated
     * with one {@link ContentSceneController}. If the supplied {@code Holder} instance has
     * already been initialized, an {@link IllegalArgumentException} will be
     * thrown.
     *
     * @param holder An {@link Activity} that implements {@link Holder}.
     * @throws IllegalArgumentException if {@code holder} is {@code null} or is already holding
     *                                  another instance of {@link ContentSceneController}.
     */
    public <T extends Activity & Holder> ContentSceneController(T holder) {
        HolderHelper.register(holder, this);
    }

    public void goTo(final ContentScene contentScene) {
        Log.d(TAG, "Go to %s", contentScene.getName());

        if (!goBackTo(contentScene)) {
            mContentSceneViewStack.push(contentScene);
        }

        executeHideShowCycle(contentScene);
    }

    public void goBack() {
        if (mContentSceneViewStack.size() < 2) {
            return;
        }

        mContentSceneViewStack.pop();

        Log.d(TAG, "Go back to %s", mContentSceneViewStack.peek().getName());
        executeHideShowCycle(mContentSceneViewStack.peek());
    }

    // Internal API: only used by goTo() to go to an exiting contentScene in
    // stack
    private boolean goBackTo(final ContentScene contentScene) {
        final int index;

        if (mContentSceneViewStack.isEmpty()
                || (index = mContentSceneViewStack.indexOf(contentScene)) == -1) {
            return false;
        }

        for (int i = 0; i < index; ++i) {
            mContentSceneViewStack.pop();
        }

        return true;
    }

    public void pause() {
        if (mContentSceneViewStack.isEmpty()) {
            return;
        }

        new ExecutionChain(gvrContext).runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (mContentSceneViewStack.isEmpty()) {
                    return;
                }

                mContentSceneViewStack.peek().hide();
            }
        }).execute();
    }

    public void resume() {
        if (mContentSceneViewStack.isEmpty()) {
            return;
        }

        new ExecutionChain(gvrContext).runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (mContentSceneViewStack.isEmpty()) {
                    return;
                }

                mContentSceneViewStack.peek().show();
            }
        }).execute();
    }

    public void onSystemDialogRemoved() {
        if (null != curContentScene) {
            curContentScene.onSystemDialogRemoved();
        }
    }

    public void onSystemDialogPosted() {
        if (null != curContentScene) {
            curContentScene.onSystemDialogPosted();
        }
    }

    public void onProximityChange(boolean onProximity) {
        if (null != curContentScene) {
            curContentScene.onProximityChange(onProximity);
        }
    }

    /**
     * Execute a task in the {@linkplain MainThread#runOnMainThread(Runnable)
     * main thread} and {@link ContentScene#hide() hide()} has been called on
     * the {@link ContentScene} at the top of the view stack (if there is one).
     * <p>
     * The task's {@link Runnable#run() run()} method may freely manipulate the
     * {@code ContentScene} view stack.
     * <p>
     * After the task has been run, if there is a {@code ContentScene} on the
     * view stack, {@code drawFrameListener} will be re-registered and
     * {@link ContentScene#show() show()} will be called on the
     * {@code ContentScene}.
     *
     * @param contentScene
     *            The {@link Runnable} to execute.
     */
    private void executeHideShowCycle(final ContentScene contentScene) {
        // When there is no ongoing show-hide execution chain, create a new one
        if (nextContentScene == null) {
            nextContentScene = contentScene;
            new ExecutionChain(gvrContext).runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    ContentScene localNextContentScene = null;

                    // Recursively execute hide-show cycle until stablized
                    while (nextContentScene != localNextContentScene) {
                        localNextContentScene = nextContentScene;

                        if (curContentScene == localNextContentScene) {
                            Log.d(TAG,
                                    "skip the same scene to show %s",
                                    curContentScene.getName());
                            break;
                        }

                        // Hide current contentScene
                        if (curContentScene != null) {
                            Log.d(TAG,
                                    "executeHideShowCycle(): hiding %s",
                                    curContentScene.getName());
                            curContentScene.hide();
                        }

                        // Show next contentScene
                        if (localNextContentScene != null) {
                            Log.d(TAG,
                                    "executeHideShowCycle(): showing %s",
                                    localNextContentScene.getName());
                            localNextContentScene.show();
                            curContentScene = localNextContentScene;
                        }
                    }

                    nextContentScene = null;
                }
            }).execute();
        } else {
            nextContentScene = contentScene;
        }
    }

    private ContentScene curContentScene = null;
    private ContentScene nextContentScene = null;

    private final LinkedList<ContentScene> mContentSceneViewStack = new LinkedList<ContentScene>();

    @SuppressWarnings("unused")
    private void printContentSceneViewStack() {
        Log.d(TAG, "Print ContentSceneViewStack from top to bottom: ");
        for (int i = 0; i < mContentSceneViewStack.size(); i++) {
            Log.d(TAG, "        Level %d: %s", i, mContentSceneViewStack.get(i)
                    .getName());
        }
    }

    /**
     * Modifications to the {@code ContentScene} view stack must always be done:
     * <ul>
     * <li>On the {@linkplain MainThread#runOnMainThread(Runnable) main thread}</li>
     * </ul>
     * In other words, only modify from the {@link Runnable#run() run()} method
     * of a task passed to {@link #executeHideShowCycle}.
     */
    private GVRContext gvrContext;

    private static final String TAG = ContentSceneController.class
            .getSimpleName();
}
