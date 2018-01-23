package com.samsung.smcl.vr.widgets.thread;

import java.util.concurrent.CountDownLatch;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import com.samsung.smcl.vr.widgets.log.Log;
import com.samsung.smcl.vr.widgets.widget.Widget;
import com.samsung.smcl.vr.widgets.main.Holder;
import com.samsung.smcl.vr.widgets.main.HolderHelper;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

/**
 *  MainThread needs to be a final class since the constructor starts
 *  a new thread.
 */
public final class MainThread {

    static public MainThread get(Activity activity) {
        return ((Holder) activity).get(MainThread.class);
    }

    static public MainThread get(GVRContext gvrContext) {
        MainThread mainThread = null;
        if (gvrContext != null) {
            Activity activity = gvrContext.getActivity();
            mainThread = get(activity);
        }
        return mainThread;
    }

    static public MainThread get(Widget widget) {
        MainThread mainThread = null;
        if (widget != null) {
            Activity activity = (Activity)widget.getContext();
            mainThread = get(activity);
        }
        return mainThread;
    }

    static public MainThread get(GVRSceneObject sceneObject) {
        MainThread mainThread = null;
        if (sceneObject != null) {
            Activity activity = sceneObject.getGVRContext().getActivity();
            mainThread = get(activity);
        }
        return mainThread;
    }

    /**
     * Creates and starts the main thread and its message handling Looper.
     * <p>
     * <b><span style="color:red">NOTE:</span></b> This method blocks until the
     * thread is started and the handler is ready to receive messages.
     * <p>
     * An instance of {@link Holder} must be supplied and can only be associated
     * with one {@link MainThread}. If the supplied {@code Holder} instance has
     * already been initialized, an {@link IllegalArgumentException} will be
     * thrown.
     *
     * @param holder
     *            An {@link Activity} that implements {@link Holder}.
     * @throws InterruptedException
     *             if the thread wasn't successfully started.
     * @throws IllegalArgumentException
     *             if {@code holder} is {@code null} or is already holding
     *             another instance of {@code MainThread}.
     */
    public <T extends Activity & Holder> MainThread(T holder) throws InterruptedException {
        HolderHelper.register(holder, this);

        final CountDownLatch latch = new CountDownLatch(1);
        mainThread = new Thread("MainThread") {
            public void run() {
                threadId = Thread.currentThread().getId();
                Looper.prepare();
                handler = new Handler();
                latch.countDown();
                Looper.loop();
                terminated = true;
                sanityCheck(null);
            }
        };
        mainThread.start();
        latch.await();
    }

    /**
     * Shuts down the main thread's looper using {@code Looper#quitSafely()}.
     */
    public void quit() {
        if (!sanityCheck("quit")) {
            return;
        }
        handler.getLooper().quitSafely();
    }

    /**
     * Query whether the current running thread is the main thread
     *
     * @return Returns {@code true} if the thread this method is called from is
     *         the main thread.
     */
    public boolean isMainThread() {
        return Thread.currentThread().equals(mainThread);
    }

    /**
     * Remove any pending posts of {@link Runnable} {@code r} that are in the
     * message queue.
     *
     * @param r
     *            {@code Runnable} to remove from queue.
     */
    public void removeCallbacks(Runnable r) {
        assert handler != null;
        if (!sanityCheck("removeCallbacks " + r)) {
            return;
        }
        handler.removeCallbacks(r);
    }

    /**
     * Executes a {@link Runnable} on the main thread. If this method is called
     * from the main thread, the {@code Runnable} will be executed immediately;
     * otherwise, it will be {@linkplain Handler#post(Runnable) posted} to the
     * {@link Looper Looper's} message queue.
     *
     * @param r
     *            The {@link Runnable} to run on the main thread.
     * @return Returns {@code true} if the Runnable was either immediately
     *         executed or successfully placed in the Looper's message queue.
     */
    public boolean runOnMainThread(final Runnable r) {
        assert handler != null;
        if (!sanityCheck("runOnMainThread " + r)) {
            return false;
        }

        Runnable wrapper =  new Runnable() {
            public void run() {
                FPSCounter.timeCheck("runOnMainThread <START> " + r);
                r.run();
                FPSCounter.timeCheck("runOnMainThread <END> " + r);
            }
        };

        if (isMainThread()) {
            wrapper.run();
            return true;
        } else {
            return runOnMainThreadNext(wrapper);
        }
    }

    /**
     * Queues a Runnable to be run on the main thread on the next iteration of
     * the messaging loop. This is handy when code running on the main thread
     * needs to run something else on the main thread, but only after the
     * current code has finished executing.
     *
     * @param r
     *            The {@link Runnable} to run on the main thread.
     * @return Returns {@code true} if the Runnable was successfully placed in
     *         the Looper's message queue.
     */
    public boolean runOnMainThreadNext(Runnable r) {
        assert handler != null;

        if (!sanityCheck("runOnMainThreadNext " + r)) {
            return false;
        }
        return handler.post(r);
    }

    /**
     * Queues a Runnable to be run on the main thread at the time specified by
     * {@code uptimeMillis}. The time base is {@link SystemClock#uptimeMillis()
     * uptimeMillis()} . A pause in application execution may add an additional
     * delay.
     *
     * @param r
     *            The Runnable to run on the main thread.
     * @param uptimeMillis
     *            The absolute time at which the Runnable should be run, in
     *            milliseconds.
     * @return Returns {@code true} if the Runnable was successfully placed in
     *         the Looper's message queue.
     */
    public boolean runOnMainThreadAtTime(Runnable r, long uptimeMillis) {
        assert handler != null;

        if (!sanityCheck("runOnMainThreadAtTime " + r)) {
            return false;
        }
        return handler.postAtTime(r, uptimeMillis);
    }

    /**
     * Queues a Runnable to be run on the main thread after the time specified
     * by {@code delayMillis} has elapsed. A pause in application execution may
     * add an additional delay.
     *
     * @param r
     *            The Runnable to run on the main thread.
     * @param delayMillis
     *            The delay, in milliseconds, until the Runnable is run.
     * @return Returns {@code true} if the Runnable was successfully placed in
     *         the Looper's message queue.
     */
    public boolean runOnMainThreadDelayed(Runnable r, long delayMillis) {
        assert handler != null;

        if (!sanityCheck("runOnMainThreadDelayed " + r + " delayMillis = " + delayMillis)) {
            return false;
        }
        return handler.postDelayed(r, delayMillis);
    }

    public Handler getHandler() {
        return handler;
    }

    public void assertIsMainThread(final String label) {
        final boolean mainThread = isMainThread();
        Log.d(TAG, "%s: is main thread: %b", label, mainThread);
        if (!mainThread) {
            throw new RuntimeException(
                    "Main thread code run on another thread: "
                            + Thread.currentThread().getName());
        }
    }

    private boolean sanityCheck(String taskName) {
        if (terminated) {
            Log.d(TAG, "MainThread " + threadId + " already terminated" +
                    (taskName == null ? "" : ", rejecting task " + taskName));
            return false;
        }
        return true;
    }

    private Thread mainThread;
    private Handler handler;
    private boolean terminated;
    private long    threadId;

    private static final String TAG = MainThread.class.getSimpleName();
}
