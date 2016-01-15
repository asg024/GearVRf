package com.samsung.smcl.vr.widgets;

import java.util.LinkedHashSet;
import java.util.Set;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRInterpolator;
import org.gearvrf.animation.GVROnFinish;

import com.samsung.smcl.vr.gvrf_launcher.util.SimpleAnimationTracker;

public abstract class Animation {
    public interface OnFinish {
        void finished(Animation animation);
    }

    public void track(SimpleAnimationTracker tracker, final Widget target) {
        track(tracker, target, null);
    }

    public void track(SimpleAnimationTracker tracker, final Widget target,
            final OnFinish onFinish) {
        track(tracker, target, null, onFinish);
    }

    public void track(SimpleAnimationTracker tracker, final Widget target,
            final Runnable onStart, OnFinish onFinish) {
        tracker.track(target.getSceneObject(), (GVRAnimation) getAnimation(),
                      onStart, new GVROnFinishProxy(onFinish));
    }

    public void track(SimpleAnimationTracker tracker) {
        track(tracker, (OnFinish) null);
    }

    public void track(SimpleAnimationTracker tracker,
            final OnFinish onFinish) {
        track(tracker, (Runnable) null, onFinish);
    }

    public void track(SimpleAnimationTracker tracker,
            final Runnable onStart, OnFinish onFinish) {
        tracker.track(mTarget.getSceneObject(), (GVRAnimation) getAnimation(),
                      onStart, new GVROnFinishProxy(onFinish));
    }

    public Animation(Widget target, float duration) {
        mTarget = target;
        final Adapter animationAdapter = new Adapter(this,
                target.getSceneObject(), duration);
        mAnimation = animationAdapter;
    }

    public Animation setInterpolator(GVRInterpolator interpolator) {
        getAnimation().setInterpolator(interpolator);
        return this;
    }

    public Animation setRepeatMode(int mode) {
        getAnimation().setRepeatMode(mode);
        return this;
    }

    public Animation setRepeatCount(int count) {
        getAnimation().setRepeatMode(count);
        return this;
    }

    @Deprecated
    public Animation setOnFinish(final GVROnFinish onFinish) {
        return addOnFinish(new OnFinish() {
            @Override
            public void finished(Animation animation) {
                onFinish.finished((GVRAnimation) animation.getAnimation());
            }
        });
    }

    @Deprecated
    public Animation setOnFinish(OnFinish onFinish) {
        return addOnFinish(onFinish);
    }

    public Animation addOnFinish(OnFinish onFinish) {
        if (mOnFinish == null) {
            mOnFinish = new OnFinishManager();
            getAnimation().setOnFinish(mOnFinish);
        }
        mOnFinish.addOnFinish(onFinish);
        return this;
    }

    public void removeOnFinish(OnFinish onFinish) {
        if (mOnFinish != null) {
            mOnFinish.removeOnFinish(onFinish);
        }
    }

    /**
     * Start the animation.
     * 
     * Changing properties once the animation is running can have unpredictable
     * results.
     * 
     * @return {@code this}, so you can save the instance at the end of a chain
     *         of calls.
     */
    public Animation start() {
        return start(GVRAnimationEngine.getInstance(mTarget.getGVRContext()));
    }

    /**
     * Start the animation.
     * 
     * Changing properties once the animation is running can have unpredictable
     * results.
     * 
     * @param engine
     *            The global animation engine.
     * @return {@code this}, so you can save the instance at the end of a chain
     *         of calls.
     */
    public Animation start(GVRAnimationEngine engine) {
        ((GVRAnimation) getAnimation()).start(engine);
        mIsRunning = true;
        return this;
    }

    /**
     * Stop the animation, even if it is still running: the animated object will
     * be left in its current state, not reset to the start or end values.
     * 
     * This is probably not what you want to do! Usually you will either
     * <ul>
     * <li>Use {@link #setRepeatCount(int) setRepeatCount(0)} to 'schedule'
     * termination at the end of the current repetition, or
     * <li>Call {@link #finish(GVRAnimationEngine) finish()} to set the
     * animation to its end state and notify any {@linkplain OnFinish listeners}
     * </ul>
     * You <em>may</em> want to {@code stop()} an animation if you are also
     * removing the animated object the same time. For example, you may be
     * spinning some sort of In Progress object. In a case like this, stopping
     * in mid-animation is harmless.
     */
    public void stop() {
        stop(GVRAnimationEngine.getInstance(mTarget.getGVRContext()));
    }

    /**
     * Stop the animation, even if it is still running: the animated object will
     * be left in its current state, not reset to the start or end values.
     * 
     * This is probably not what you want to do! Usually you will either
     * <ul>
     * <li>Use {@link #setRepeatCount(int) setRepeatCount(0)} to 'schedule'
     * termination at the end of the current repetition, or
     * <li>Call {@link #finish(GVRAnimationEngine) finish()} to set the
     * animation to its end state and notify any {@linkplain OnFinish listeners}
     * </ul>
     * You <em>may</em> want to {@code stop()} an animation if you are also
     * removing the animated object the same time. For example, you may be
     * spinning some sort of In Progress object. In a case like this, stopping
     * in mid-animation is harmless.
     * 
     * @param engine
     *            The global animation engine.
     */
    public void stop(GVRAnimationEngine engine) {
        engine.stop((GVRAnimation) getAnimation());
        mIsRunning = false;
    }

    /**
     * Stops the animation running and calls any
     * {@linkplain #addOnFinish(OnFinish) registered} {@linkplain OnFinish
     * listeners}.
     * 
     * @return {@code True} if the animation was running and finishing was
     *         successful; {@code false} if the animation was not running.
     */
    public boolean finish() {
        return finish(GVRAnimationEngine.getInstance(mTarget.getGVRContext()));
    }

    /**
     * Stops the animation running and calls any
     * {@linkplain #addOnFinish(OnFinish) registered} {@linkplain OnFinish
     * listeners}.
     * 
     * @param engine
     *            The global animation engine.
     * @return {@code True} if the animation was running and finishing was
     *         successful; {@code false} if the animation was not running.
     */
    public boolean finish(GVRAnimationEngine engine) {
        if (mIsRunning) {
            stop(engine);
            getAnimation().animate(mTarget.getSceneObject(), 1);
            mOnFinish.finished((GVRAnimation) getAnimation());
            return true;
        }
        return false;
    }

    public boolean isFinished() {
        return getAnimation().isFinished();
    }

    public int getRepeatCount() {
        return getAnimation().getRepeatCount();
    }

    public float getDuration() {
        return getAnimation().getDuration();
    }

    public float getElapsedTime() {
        return getAnimation().getElapsedTime();
    }

    protected abstract void animate(Widget target, float ratio);

    /* package */
    Animation(Widget target) {
        mTarget = target;
    }

    /* package */
    void doAnimate(float ratio) {
        animate(mTarget, ratio);
    }

    /**
     * @return An implementation of {@code AnimationAdapter}.
     *         <p>
     *         <span style="color:red"><b>NOTE:</b></span> Implementations of
     *         this method <em>must</em> be idempotent!
     */
    /* package */
    AnimationAdapter getAnimation() {
        return mAnimation;
    }

    /* package */
    interface AnimationAdapter {
        void animate(GVRHybridObject target, float ratio);

        float getElapsedTime();

        float getDuration();

        int getRepeatCount();

        boolean isFinished();

        GVRAnimation setOnFinish(GVROnFinish onFinish);

        GVRAnimation setRepeatMode(int mode);

        GVRAnimation setInterpolator(GVRInterpolator interpolator);
    }

    private class Adapter extends GVRAnimation implements AnimationAdapter {

        public Adapter(Animation parent, GVRHybridObject target, float duration) {
            super(target, duration);
        }

        @Override
        public void animate(GVRHybridObject target, float ratio) {
            doAnimate(ratio);
        }
    }

    private class GVROnFinishProxy implements GVROnFinish {

        public GVROnFinishProxy(OnFinish onFinish) {
            mOnFinish = onFinish;
        }

        @Override
        public void finished(GVRAnimation animation) {
            mOnFinish.finished((Animation) ((AnimationAdapter) animation));
        }

        private final OnFinish mOnFinish;
    }

    private class OnFinishManager implements GVROnFinish {
        @Override
        public void finished(GVRAnimation unused) {
            mIsRunning = false;
            for (OnFinish listener : mListeners) {
                try {
                    listener.finished(Animation.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void addOnFinish(final OnFinish onFinish) {
            mListeners.add(onFinish);
        }

        public void removeOnFinish(final OnFinish onFinish) {
            mListeners.remove(onFinish);
        }

        private Set<OnFinish> mListeners = new LinkedHashSet<OnFinish>();
    }

    private final Widget mTarget;
    private AnimationAdapter mAnimation;
    private OnFinishManager mOnFinish;
    private boolean mIsRunning;
}
