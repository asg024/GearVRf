package com.samsung.smcl.vr.widgetlib.widget;

import android.support.annotation.NonNull;

import com.samsung.smcl.vr.widgetlib.main.CommandBuffer.Command;
import com.samsung.smcl.vr.widgetlib.main.CommandBuffer.CommandPool;
import com.samsung.smcl.vr.widgetlib.thread.ConcurrentObjectPool;
import com.samsung.smcl.vr.widgetlib.widget.Widget.ViewPortVisibility;
import com.samsung.smcl.vr.widgetlib.widget.Widget.Visibility;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

/**
 * {@link Command} class encapsulating setting a {@link Widget Widget's} {@linkplain
 * Widget#setVisibility(Visibility) visibility}.
 * <p>
 * Instances are managed by an internal {@linkplain ConcurrentObjectPool pool}.
 */
class UpdateVisibilityCommand extends Command {
    /**
     * @return An instance of {@link UpdateVisibilityCommand} from the internal pool.
     */
    static UpdateVisibilityCommand acquire() {
        return sPool.acquire();
    }

    @Override
    public void run() {
        GVRContext gvrContext = mSceneObject.getGVRContext();
        GVRSceneObject sceneObjectParent = mSceneObject.getParent();
        switch (mNewVisibility) {
            case VISIBLE:
                if (sceneObjectParent != mParentSceneObject &&
                        mViewPortVisibility != ViewPortVisibility.INVISIBLE) {
                    if (null != sceneObjectParent) {
                        sceneObjectParent.removeChildObject(mSceneObject);
                    }
                    mParentSceneObject.addChildObject(mSceneObject);
                    gvrContext.getMainScene().bindShaders(mParentSceneObject);
                }
                break;
            case HIDDEN:
            case GONE:
                if (mCurrentVisibility == Visibility.VISIBLE) {
                    mParentSceneObject.removeChildObject(mSceneObject);
                }
                break;
            case PLACEHOLDER:
                mSceneObject.detachRenderData();
                break;
        }

        super.run();
    }

    /**
     * Set the parameters for the {@linkplain Visibility} change.
     *
     * @param widget {@link Widget} to change the visibility of.
     * @param currentVisibility The {@code Widget's} current visibility.
     * @param newVisibility The {@code Widget's} new visibility.
     * @param viewPortVisibility The {@code Widget's} current {@link ViewPortVisibility}.
     *
     * @return The {@link UpdateVisibilityCommand} instance.
     */
    public UpdateVisibilityCommand setup(Widget widget, Visibility currentVisibility,
                                         Visibility newVisibility,
                                         ViewPortVisibility viewPortVisibility) {
        mSceneObject = widget.getSceneObject();
        mParentSceneObject = widget.getParent().getSceneObject();
        mCurrentVisibility = currentVisibility;
        mNewVisibility = newVisibility;
        mViewPortVisibility = viewPortVisibility;
        return this;
    }

    private UpdateVisibilityCommand(@NonNull CommandPool<UpdateVisibilityCommand> pool) {
        super(pool);
    }

    private GVRSceneObject mSceneObject;
    private GVRSceneObject mParentSceneObject;
    private Visibility mCurrentVisibility;
    private Visibility mNewVisibility;
    private ViewPortVisibility mViewPortVisibility;

    private static final CommandPool<UpdateVisibilityCommand> sPool =
            new CommandPool<>(new CommandPool.CommandFactory<UpdateVisibilityCommand>() {
                @Override
                public UpdateVisibilityCommand create(CommandPool<UpdateVisibilityCommand> pool) {
                    return new UpdateVisibilityCommand(pool);
                }

                @Override
                public String name() {
                    return UpdateVisibilityCommand.class.getSimpleName();
                }
            });
}
