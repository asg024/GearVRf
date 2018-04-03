package com.samsung.smcl.vr.widgetlib.main;

import android.support.annotation.NonNull;

import com.samsung.smcl.vr.widgetlib.thread.ConcurrentListPool;
import com.samsung.smcl.vr.widgetlib.thread.ConcurrentObjectPool;
import com.samsung.smcl.vr.widgetlib.thread.MainThread;

import org.gearvrf.GVRContext;
import org.gearvrf.utility.Log;

import java.util.List;

/**
 * A utility class for buffering {@link Runnable} "commands" for batch execution on the {@linkplain
 * GVRContext#runOnGlThread(Runnable) GL thread}.
 * <p>
 * Includes utilities -- {@link Command}, {@link CommandPool}, and {@link CommandPool.CommandFactory}
 * -- to make management of command instances a bit easier and to reduce memory fragmentation and
 * usage.  The typical implementation pattern looks like this:
 * <pre>
 *     class MyCommand extends Command {
 *         static MyCommand acquire() {
 *             return sPool.acquire();
 *         }
 *
 *         MyCommand(CommandPool&lt;MyCommand> pool) {
 *             super(pool);
 *         }
 *
 *         public void run() {
 *             // Command-specific implementation goes here
 *             super.run();  // Call last! See {@link Command#run()}
 *         }
 *
 *         public MyCommand setup(...) { // Parameters relevant to executing the command
 *             // Setup code here
 *             return this;
 *         }
 *
 *         private static final CommandPool&lt;MyCommand> sPool =
 *             new CommandPool&lt;>(new CommandPool.CommandFactory&lt;MyCommand>() {
 *                 public MyCommand create(CommandPool&lt;MyCommand> pool) {
 *                     return new MyCommand(pool);
 *                 }
 *
 *                 String name() {
 *                     return MyCommand.class.getSimpleName();
 *                 }
 *             });
 *     }
 * </pre>
 * The typical usage pattern looks like this:
 * <pre>
 *     MyCommand command = MyCommand.acquire();
 *     command.setup(...);  // Relevant parameters go here
 *     WidgetLib.getCommandBuffer().add(command);
 * </pre>
 *
 * @see Command#run()
 */
public class CommandBuffer {

    /**
     * An extension of {@link Runnable} that manages the boiler-plate of releasing back to a
     * {@linkplain CommandPool pool} after running.
     */
    static public abstract class Command implements Runnable {
        /**
         * Construct an instance with an attached {@link CommandPool pool}.
         *
         * @param pool Reference to the {@link CommandPool} to release the instance back to. Must
         *             not be null.
         */
        public Command(@NonNull CommandPool<? extends Command> pool) {
            if (pool == null) {
                throw new IllegalArgumentException("Parameter 'pool' must be non-null!");
            }
            mPool = pool;
        }

        /**
         * Releases this instance back to the attached {@link CommandPool pool}.
         * <p>
         * <span style="color:red"><b>NOTE:</b></span> Call <code>super.run()</code> from your
         * override of {@link Runnable#run()} immediately before returning, after all other logic
         * has executed; doing so sooner may result in this object being reacquired in the {@link
         * MainThread main thread} and data members being overwritten by that thread.
         */
        @Override
        public void run() {
            mPool.release(this);
        }

        private final CommandPool mPool;
    }

    /**
     * A pool for managing instances of {@link Command}.  Works with {@link Command} and {@link
     * CommandFactory} to reduce boiler-plate and semi-automate clean up of the commands added to
     * {@link CommandBuffer}.
     */
    static public class CommandPool<T extends Command> extends ConcurrentObjectPool<T> {
        /**
         * Factory pattern interface for creating instances of {@link Command}.
         */
        public interface CommandFactory<T extends Command> {
            /**
             * Implement to create new {@link Command} instances.
             *
             * @param pool The {@link CommandPool} the instances are being created for.
             *             {@link Command} caches the reference to semi-automate releasing back to
             *             the pool.
             * @return A new instance of an implementation of {@link Command}.
             */
            T create(CommandPool<T> pool);

            /**
             * @return A tag for the {@link Command} implementation created by this factory; usually
             *          the {@link Class#getSimpleName() class name}.
             */
            String name();
        }

        /**
         * Construct an instance of {@link CommandPool} that uses {@link CommandFactory factory} to
         * create new {@link Command} instances.
         *
         * @param factory An implementation of {@link CommandFactory}.  Must be non-null.
         */
        public CommandPool(@NonNull CommandFactory<T> factory) {
            super(factory.name());
            mFactory = factory;
        }

        @Override
        protected T create() {
            return mFactory.create(this);
        }

        private final CommandFactory<T> mFactory;
    }

    /**
     * Start a new buffer.  Calls to {@link #start()} can be nested, so if there is already an
     * active buffer, that buffer will continue to be used.  Calls to {@code start()} must have a
     * matching number of calls to {@link #flush()} in order for the commands in the buffer to get
     * executed.
     */
    public void start() {
        synchronized (mBufferLock) {
            if (mCurrentBuffer == null) {
                mCurrentBuffer = mBuffers.acquire();
            }
            ++mBufferDepth;
        }
    }

    /**
     * Add a {@link Runnable} to the current buffer.  If no buffer has been {@linkplain #start()
     * started}, {@code command} will be {@linkplain GVRContext#runOnGlThread(Runnable) posted}
     * directly to the GL thread to be executed in the next frame.
     *
     * @param command The command to add to the buffer.  Should be non-null.
     */
    public void add(@NonNull Runnable command) {
        synchronized (mBufferLock) {
            if (mCurrentBuffer == null) {
                mContext.runOnGlThread(command);
            } else {
                mCurrentBuffer.add(command);
            }
        }
    }

    /**
     * {@linkplain GVRContext#runOnGlThread(Runnable) Post} the current buffer to the GL thread for
     * execution.  The number of calls to {@code flush()} must match the number of calls to {@link
     * #start()}.
     *
     * @throws IllegalStateException if called when there is no active buffer.
     */
    public void flush() {
        synchronized (mBufferLock) {
            if (mCurrentBuffer == null) {
                throw new IllegalStateException("No buffer to flush!");
            }
            --mBufferDepth;
            if (mBufferDepth == 0) {
                final GLRunnable r = mRunnablePool.acquire();
                r.set(mCurrentBuffer);
                mCurrentBuffer = null;
                mContext.runOnGlThread(r);
            }
        }
    }

    /* package */
    CommandBuffer(GVRContext context) {
        mContext = context;
    }

    private final class GLRunnable implements Runnable {
        @Override
        public void run() {
            for (Runnable c : mBuffer) {
                c.run();
            }
            mBuffers.release(mBuffer);
            mBuffer = null;
            mRunnablePool.release(this);
        }

        public void set(List<Runnable> buffer) {
            mBuffer = buffer;
        }

        private List<Runnable> mBuffer;
    }

    private final GVRContext mContext;
    private final ConcurrentListPool<Runnable> mBuffers = new ConcurrentListPool<>(TAG) ;
    private int mBufferDepth;
    private final Object[] mBufferLock = new Object[0];
    private List<Runnable> mCurrentBuffer;
    private final ConcurrentObjectPool<GLRunnable> mRunnablePool = new ConcurrentObjectPool<GLRunnable>("GLRunnable Pool") {
        @Override
        protected GLRunnable create() {
            return new GLRunnable();
        }
    };

    private static final String TAG = Log.tag(CommandBuffer.class);
}
