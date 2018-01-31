package com.samsung.smcl.vr.widgets.log;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.util.Pools.SynchronizedPool;
import android.util.SparseArray;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

/**
 * Persistent logs are written to:
 *
 * // TODO: WIDGET_LIBRARY remove dependancy

 * /storage/emulated/0/Android/data/com.samsung.smcl.vr.gvrf_launcher/files/Documents/vrtop
 */
@SuppressWarnings("unused")
public class Log {
    private enum TYPE {
        ANDROID,
        PERSISTENT
    }

    public enum MODE {
        DEBUG, // android log + start full log
        DEVELOPER, // persistent log
        USER // android log
    }

    public enum SUBSYSTEM {
        MAIN,
        WIDGET,
        LAYOUT,
        PANELS,
        FOCUS,
        TRACING,
        JSON
    }

    private static Set<SUBSYSTEM> mEnabledSubsystems = new HashSet<>();

    private static final String TAG = "Log";
    public static MODE mode;

    private static final TYPE DEFAULT_TYPE = TYPE.PERSISTENT; // TYPE.ANDROID;
    private static final boolean frequentlyUpdating = false;
    private static boolean enable_logging;

    private static LogBase currentLog;

    public static void init(Context context, boolean enable) {
        PersistentLog.init(context);

        // `adb shell setprop vrtop.logging.enable true`
        enable_logging = enable;
        android.util.Log.d(TAG, "enable_logging: " + enable_logging);
        rebuild(MODE.DEVELOPER);

        enableSubsystem(SUBSYSTEM.MAIN, true);
//        enableSubsystem(SUBSYSTEM.TRACING, true);
//        enableSubsystem(SUBSYSTEM.LAYOUT, true);
//        enableSubsystem(SUBSYSTEM.WIDGET, true);
//        enableSubsystem(SUBSYSTEM.PANELS, true);

    }

    public static void enableSubsystem(SUBSYSTEM subsystem, boolean enable) {
        if (enable_logging) {
            if (enable) {
                mEnabledSubsystems.add(subsystem);
            } else {
                mEnabledSubsystems.remove(subsystem);
            }
        }
    }

    public static void enableAllSubsystems(boolean enable) {
        if (enable_logging) {
            if (enable) {
                for (SUBSYSTEM s : SUBSYSTEM.values()) {
                    mEnabledSubsystems.add(s);
                }
            } else {
                mEnabledSubsystems.clear();
            }
        }
    }

    public static void rebuild(final MODE newMode) {
        if (mode != newMode) {
            mode = newMode;
            TYPE type;
            switch (mode) {
            case DEBUG:
                type = TYPE.ANDROID;
                Log.startFullLog();
                break;
            case DEVELOPER:
                type = TYPE.PERSISTENT;
                Log.stopFullLog();
                break;
            case USER:
                type = TYPE.ANDROID;
                break;
            default:
                type =  DEFAULT_TYPE;
                Log.stopFullLog();
                break;
            }
            currentLog = getLog(type);
        }
    }

    private static LogBase getLog(Log.TYPE type) {
        LogBase log = null;
        switch(type) {
        case ANDROID:
            log = new AndroidLog();
            break;
        case PERSISTENT:
            log = new PersistentLog();
            break;
        default:
            android.util.Log.w(TAG, "Incorrect logger type! type = " + type +
                    " Use default log type: " + DEFAULT_TYPE);
            break;
        }
        return log;
    }

    public static boolean isEnabled(SUBSYSTEM subsystem) {
        return mEnabledSubsystems.contains(subsystem);
    }

    private static final String LOG_MSG_FORMAT = "<%s> %s";

    private static String getMsg(SUBSYSTEM subsystem, String msg) {
        return subsystem == SUBSYSTEM.MAIN ? msg :
                String.format(LOG_MSG_FORMAT, subsystem, msg);
    }

    public static void pause() {
        currentLog.pause();
    }
    public static void resume() {
        currentLog.resume();
    }

    public static int d(String tag, String msg) {
        return d(SUBSYSTEM.MAIN, tag, msg);
    }
    public static int d(String tag, String msg, Throwable tr) {
        return d(SUBSYSTEM.MAIN, tag, msg, tr);
    }
    public static int e(String tag, String msg) {
        return e(SUBSYSTEM.MAIN, tag, msg);
    }
    public static int e(String tag, String msg, Throwable tr) {
        return e(SUBSYSTEM.MAIN, tag, msg, tr);
    }
    public static int i(String tag, String msg) {
        return i(SUBSYSTEM.MAIN, tag, msg);
    }
    public static int i(String tag, String msg, Throwable tr) {
        return i(SUBSYSTEM.MAIN, tag, msg, tr);
    }
    public static int v(String tag, String msg, Throwable tr) {
        return v(SUBSYSTEM.MAIN, tag, msg, tr);
    }
    public static int v(String tag, String msg) {
        return v(SUBSYSTEM.MAIN, tag, msg);
    }
    public static int w(String tag, Throwable tr) {
        return w(SUBSYSTEM.MAIN, tag, tr);
    }
    public static int w(String tag, String msg, Throwable tr) {
        return w(SUBSYSTEM.MAIN, tag, msg, tr);
    }
    public static int w(String tag, String msg) {
        return w(SUBSYSTEM.MAIN, tag, msg);
    }
    public static int wtf(String tag, Throwable tr) {
        return wtf(SUBSYSTEM.MAIN, tag, tr);
    }
    public static int wtf(String tag, String msg, Throwable tr) {
        return wtf(SUBSYSTEM.MAIN, tag, msg, tr);
    }
    public static int wtf(String tag, String msg) {
        return wtf(SUBSYSTEM.MAIN, tag, msg);
    }


    public static int d(SUBSYSTEM subsystem, String tag, String msg) {
        return isEnabled(subsystem) ?
                currentLog.d(tag, getMsg(subsystem,msg)) : 0;
    }

    public static int d(SUBSYSTEM subsystem, String tag, String msg, Throwable tr) {
        return isEnabled(subsystem) ?
                currentLog.d(tag, getMsg(subsystem,msg), tr) : 0;
    }
    public static int e(SUBSYSTEM subsystem, String tag, String msg) {
        return isEnabled(subsystem) ?
                currentLog.e(tag, getMsg(subsystem,msg)) : 0;
    }
    public static int e(SUBSYSTEM subsystem, String tag, String msg, Throwable tr) {
        return isEnabled(subsystem) ?
                currentLog.e(tag, getMsg(subsystem,msg), tr) : 0;
    }
    public static int i(SUBSYSTEM subsystem, String tag, String msg) {
        return isEnabled(subsystem) ?
                currentLog.i(tag, getMsg(subsystem,msg)) : 0;
    }
    public static int i(SUBSYSTEM subsystem, String tag, String msg, Throwable tr) {
        return isEnabled(subsystem) ?
                currentLog.i(tag, getMsg(subsystem,msg), tr) : 0;
    }
    public static int v(SUBSYSTEM subsystem, String tag, String msg, Throwable tr) {
        return isEnabled(subsystem) ?
                currentLog.v(tag, getMsg(subsystem,msg), tr) : 0;
    }
    public static int v(SUBSYSTEM subsystem, String tag, String msg) {
        return isEnabled(subsystem) ?
                currentLog.v(tag, getMsg(subsystem,msg)) : 0;
    }
    public static int w(SUBSYSTEM subsystem, String tag, Throwable tr) {
        return isEnabled(subsystem) ?
                currentLog.w(tag, getMsg(subsystem, ""), tr) : 0;
    }
    public static int w(SUBSYSTEM subsystem, String tag, String msg, Throwable tr) {
        return isEnabled(subsystem) ?
                currentLog.w(tag, getMsg(subsystem,msg), tr) : 0;
    }
    public static int w(SUBSYSTEM subsystem, String tag, String msg) {
        return isEnabled(subsystem) ?
                currentLog.w(tag, getMsg(subsystem,msg)) : 0;
    }
    public static int wtf(SUBSYSTEM subsystem, String tag, Throwable tr) {
        return isEnabled(subsystem) ?
                currentLog.wtf(tag, getMsg(subsystem, ""), tr) : 0;
    }
    public static int wtf(SUBSYSTEM subsystem, String tag, String msg, Throwable tr) {
        return isEnabled(subsystem) ?
                currentLog.wtf(tag, getMsg(subsystem,msg), tr) : 0;
    }
    public static int wtf(SUBSYSTEM subsystem, String tag, String msg) {
        return isEnabled(subsystem) ?
                currentLog.wtf(tag, getMsg(subsystem,msg)) : 0;
    }

    public static void d(SUBSYSTEM subsystem, String TAG, String pattern, Object... parameters) {
        if (!isEnabled(subsystem)) return;
        d(subsystem, TAG, format(pattern, parameters));
    }
    public static void e(SUBSYSTEM subsystem, String TAG, String pattern, Object... parameters) {
        if (!isEnabled(subsystem)) return;
        e(subsystem, TAG, format(pattern, parameters));
    }
    public static void e(SUBSYSTEM subsystem, String TAG, Throwable t, String pattern, Object... parameters) {
        if (!isEnabled(subsystem)) return;
        e(subsystem, TAG, format(pattern, parameters), t);
    }
    public static void i(SUBSYSTEM subsystem, String TAG, String pattern, Object... parameters) {
        if (!isEnabled(subsystem)) return;
        i(subsystem, TAG, format(pattern, parameters));
    }
    public static void v(SUBSYSTEM subsystem, String TAG, String pattern, Object... parameters) {
        if (!isEnabled(subsystem)) return;
        v(subsystem, TAG, format(pattern, parameters));
    }
    public static void w(SUBSYSTEM subsystem, String TAG, String pattern, Object... parameters) {
        if (!isEnabled(subsystem)) return;
        w(subsystem, TAG, format(pattern, parameters));
    }

    @SuppressWarnings("unused")
    public static String getStackTraceString(Throwable tr ) {
        return LogBase.getStackTraceString(tr);
    }

    public static void d(String TAG, String pattern, Object... parameters) {
        d(SUBSYSTEM.MAIN, TAG, pattern, parameters);
    }
    public static void e(String TAG, String pattern, Object... parameters) {
        e(SUBSYSTEM.MAIN, TAG, pattern, parameters);
    }
    public static void e(String TAG, Throwable t, String pattern, Object... parameters) {
        e(SUBSYSTEM.MAIN, TAG, t, pattern, parameters);
    }
    public static void i(String TAG, String pattern, Object... parameters) {
        i(SUBSYSTEM.MAIN, TAG, pattern, parameters);
    }
    public static void v(String TAG, String pattern, Object... parameters) {
        v(SUBSYSTEM.MAIN, TAG, pattern, parameters);
    }
    public static void w(String TAG, String pattern, Object... parameters) {
        w(SUBSYSTEM.MAIN, TAG, pattern, parameters);
    }

    public static void startFullLog() {
        PersistentLog.startFullLog();
    }

    public static void stopFullLog() {
        PersistentLog.stopFullLog();
    }

    private static String format(String pattern, Object... parameters) {
        return parameters == null || parameters.length == 0 ? pattern :
                String.format(pattern, parameters);
    }

    private static abstract class LogBase {
        protected static String getStackTraceString (Throwable tr) {
            return android.util.Log.getStackTraceString(tr) + "\n";
        }

        public void pause() {
        }

        public void resume() {
        }

        abstract public int d(String tag, String msg);
        abstract public int d(String tag, String msg, Throwable tr);
        abstract public int e(String tag, String msg);
        abstract public int e(String tag, String msg, Throwable tr);
        abstract public int i(String tag, String msg);
        abstract public int i(String tag, String msg, Throwable tr);
        abstract public int v(String tag, String msg, Throwable tr);
        abstract public int v(String tag, String msg);
        abstract public int w(String tag, Throwable tr);
        abstract public int w(String tag, String msg, Throwable tr);
        abstract public int w(String tag, String msg);
        @SuppressWarnings("unused")
        abstract public int wtf(String tag, Throwable tr);
        abstract public int wtf(String tag, String msg, Throwable tr);
        abstract public int wtf(String tag, String msg);
    }

    private static class AndroidLog extends LogBase {
        public int d(String tag, String msg) {
            return android.util.Log.d(tag, msg);
        }
        public int d(String tag, String msg, Throwable tr) {
            return android.util.Log.d(tag, msg, tr);
        }
        public int e(String tag, String msg) {
            return android.util.Log.e(tag, msg);
        }
        public int e(String tag, String msg, Throwable tr) {
            return android.util.Log.e(tag, msg, tr);
        }
        public int i(String tag, String msg) {
            return android.util.Log.i(tag, msg);
        }
        public int i(String tag, String msg, Throwable tr) {
            return android.util.Log.i(tag, msg, tr);
        }
        public int v(String tag, String msg, Throwable tr)  {
            return android.util.Log.v(tag, msg, tr);
        }
        public int v(String tag, String msg) {
            return android.util.Log.v(tag, msg);
        }
        public int w(String tag, Throwable tr) {
            return android.util.Log.w(tag, tr);
        }
        public int w(String tag, String msg, Throwable tr) {
            return android.util.Log.w(tag, msg, tr);
        }
        public int w(String tag, String msg) {
            return android.util.Log.w(tag, msg);
        }
        public int wtf(String tag, Throwable tr) {
            return android.util.Log.wtf(tag, tr);
        }
        public int wtf(String tag, String msg, Throwable tr) {
            return android.util.Log.wtf(tag, msg, tr);
        }
        public int wtf(String tag, String msg) {
            return android.util.Log.wtf(tag, msg);
        }
    }

    private static class PersistentLog extends LogBase {
        private static final int WRITER_BUFFER_LEN = 8192; // number of chars
        static final String TAG = "PersistentLog";

        private static final SimpleDateFormat FILE_TIMESTAMP_FORMAT =
                new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss",
                java.util.Locale.getDefault());

        private static final SimpleDateFormat MSG_TIMESTAMP_FORMAT =
                new SimpleDateFormat("MM-dd HH:mm:ss.SSS",
                java.util.Locale.getDefault());

        private static final String MSG_FORMAT = "%s %s/%s (%s): %s\n";  // timestamp: PRIORITY/tag (PID): message
        private static final String LOG_FILE_NAME_FORMAT = "%s.%s";  // FILE_PATH_BASE_timestamp
        private static final int FILE_SIZE_LIMIT = 10000000;
        private static final long MAXFILEAGE = 86400000L; // 1 day in milliseconds

        private static File LOG_FILE_DIR;
        private static String FILE_PATH_BASE;
        private static String FULL_FILE_PATH_BASE;
        private BufferedWriter bufferedWriter;
        private File currentLogFile;
        private boolean isLogging = true; // start logging as earlier as possible

        private static final SparseArray<String> PRIORITY_NAME = new SparseArray<>();

        static {
            PRIORITY_NAME.put(android.util.Log.VERBOSE, "V");
            PRIORITY_NAME.put(android.util.Log.DEBUG, "D");
            PRIORITY_NAME.put(android.util.Log.INFO, "I");
            PRIORITY_NAME.put(android.util.Log.WARN, "W");
            PRIORITY_NAME.put(android.util.Log.ERROR, "E");
            PRIORITY_NAME.put(android.util.Log.ASSERT, "F");
        }

        private final LinkedBlockingQueue<LogRequest> requestQueue;
        private ExecutorService executorService;
        private final LogWorker worker;

        static void init(Context context) {
            final File documentsDirs = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            LOG_FILE_DIR = new File(documentsDirs, "vrtop");
            FILE_PATH_BASE = new File(LOG_FILE_DIR, "logFile").getAbsolutePath();
            FULL_FILE_PATH_BASE = new File(LOG_FILE_DIR, "fullLogFile").getAbsolutePath();
        }

        PersistentLog() {
            requestQueue = new LinkedBlockingQueue<>();
            worker = new LogWorker(requestQueue);
        }

        public void pause() {
            d(TAG, "pause logging!");
            writeToFile(new CloseLogRequest());
        }

        public void resume() {
            // start log request worker thread
            if (executorService == null) {
                executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
                    public Thread newThread(@NonNull Runnable r) {
                        Thread t = new Thread(r);
                        t.setPriority((Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2);
                        return t;
                    }
                });
                executorService.submit(worker);
            }
            writeToFile(new OpenLogRequest());
            d(TAG, "resume logging!");
            d(TAG, "LOG_FILE_DIR: " + LOG_FILE_DIR);
            d(TAG, "FILE_PATH_BASE: " + FILE_PATH_BASE);
            d(TAG, "FULL_FILE_PATH_BASE: " + FULL_FILE_PATH_BASE);
        }

        public int d(String tag, String msg) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.DEBUG, tag, msg));
            return android.util.Log.d(tag, msg);
        }
        public int d(String tag, String msg, Throwable tr) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.DEBUG, tag, msg, tr));
            return android.util.Log.d(tag, msg, tr);
        }
        public int e(String tag, String msg) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.ERROR, tag, msg));
            return android.util.Log.e(tag, msg);
        }
        public int e(String tag, String msg, Throwable tr) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.ERROR, tag, msg, tr));
            return android.util.Log.e(tag, msg, tr);
        }
        public int i(String tag, String msg) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.INFO, tag, msg));
            return android.util.Log.i(tag, msg);
        }
        public int i(String tag, String msg, Throwable tr) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.INFO, tag, msg, tr));
            return android.util.Log.i(tag, msg, tr);
        }
        public int v(String tag, String msg, Throwable tr)  {
            writeToFile(MsgLogRequest.obtain(android.util.Log.VERBOSE, tag, msg, tr));
            return android.util.Log.v(tag, msg, tr);
        }
        public int v(String tag, String msg) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.VERBOSE, tag, msg));
            return android.util.Log.v(tag, msg);
        }
        public int w(String tag, Throwable tr) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.WARN, tag, tr));
            return android.util.Log.w(tag, tr);
        }
        public int w(String tag, String msg, Throwable tr) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.WARN, tag, msg, tr));
            return android.util.Log.w(tag, msg, tr);
        }
        public int w(String tag, String msg) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.WARN, tag, msg));
            return android.util.Log.w(tag, msg);
        }
        public int wtf(String tag, Throwable tr) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.ASSERT, tag, tr));
            return android.util.Log.wtf(tag, tr);
        }
        public int wtf(String tag, String msg, Throwable tr) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.ASSERT, tag, msg, tr));
            return android.util.Log.wtf(tag, msg, tr);
        }
        public int wtf(String tag, String msg) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.ASSERT, tag, msg));
            return android.util.Log.wtf(tag, msg);
        }

        private void writeToFile(final LogRequest request) {
            try {
                requestQueue.put(request);
            } catch (InterruptedException e) {
                android.util.Log.e(TAG, "writeToFile error: " + e.toString());
            }
        }

        private static String formatMsg(final int priority, final String tag, final String msg) {
            return String.format(MSG_FORMAT, getCurrentTimeStamp(MSG_TIMESTAMP_FORMAT),
                    getPriorityName(priority), tag, android.os.Process.myPid(), msg);
        }

        private static String getPriorityName(int priority) {
            String name = PRIORITY_NAME.get(priority);
            return name == null ? "" : name;
        }

        interface LogRequest {
            enum TYPE {
                OPEN,
                CLOSE,
                MSG
            }
            void process(final boolean isLogging, final BufferedWriter bufferedWriter);
            TYPE getType();
        }

        private static class MsgLogRequest implements LogRequest {
            private String logMessage;
            private String trMessage;
            private boolean flushRequired;
            private static final int MAX_POOL_SIZE = 5;
            private static final SynchronizedPool<MsgLogRequest> sPool =
                    new SynchronizedPool<>(MAX_POOL_SIZE);

            public static MsgLogRequest obtain(final int priority, final String tag, final String msg, final Throwable tr) {
                MsgLogRequest instance = sPool.acquire();
                if (instance != null) {
                    instance.init(priority, tag, msg, tr);
                } else {
                    instance = new MsgLogRequest(priority, tag, msg, tr);
                }
                return instance;
            }

            public static MsgLogRequest obtain(final int priority, final String tag, final String msg) {
                return obtain(priority, tag, msg, null);
            }

            public static MsgLogRequest obtain(final int priority, final String tag, final Throwable tr) {
                return obtain(priority, tag, null, tr);
            }

            public void init(final int priority, String tag, final String msg, final Throwable tr) {
                if (msg != null) {
                    if (tag == null) {
                        tag = "";
                    }
                    logMessage = formatMsg(priority, tag, msg);
                }
                if (tr != null) {
                    trMessage = getStackTraceString(tr);
                }
                flushRequired = frequentlyUpdating ||
                        tr != null ||
                        priority >= android.util.Log.WARN;
            }

            public TYPE getType() {
                return TYPE.MSG;
            }

            private MsgLogRequest(final int priority, final String tag, final String msg, final Throwable tr) {
                init(priority, tag, msg, tr);
            }

            private void recycle() {
                // Clear state if needed.
                sPool.release(this);
            }

            public void process(final boolean isLogging, final BufferedWriter writer) {
                if (isLogging) {
                    try {
                        if (logMessage != null) {
                            writer.write(logMessage);
                        }

                        if (trMessage != null) {
                            writer.write(trMessage);
                        }

                        if (flushRequired) {
                            writer.flush();
                        }
                    } catch (IOException e) {
                        android.util.Log.e(TAG, e.toString());
                    } finally {
                        recycle();
                    }
                }
            }
        }

        private class CloseLogRequest implements LogRequest {
            public void process(final boolean isLogging, final BufferedWriter bufferedWriter) {
                close();
                deleteOldAndEmptyFiles();
                if (requestQueue != null) {
                    requestQueue.clear();
                }
                executorService.shutdown();
                executorService = null;
                enableLog(false);
            }

            public TYPE getType() {
                return TYPE.CLOSE;
            }

        }

        private class OpenLogRequest implements LogRequest {
            public void process(final boolean isLogging, final BufferedWriter bufferedWriter) {
                enableLog(true);
            }

            public TYPE getType() {
                return TYPE.OPEN;
            }
        }

        private void enableLog(final boolean enable) {
            isLogging = enable;
        }

        private class LogWorker implements Runnable {
            private final LinkedBlockingQueue<LogRequest> queue;

            LogWorker(LinkedBlockingQueue<LogRequest> queue) {
                this.queue = queue;
            }

            @Override
            public void run() {
                while (true) {
                    try {
                        // Get the next log request item off of the queue
                        LogRequest request = queue.take();

                        // Process log request
                        if (request != null) {
                            request.process(isLogging, getWriter());
                        }
                    }
                    catch ( InterruptedException ie ) {
                        break;
                    }
                }
            }
        }

        private BufferedWriter getWriter() {
            try {
                if (currentLogFile != null && currentLogFile.length() > FILE_SIZE_LIMIT) {
                    close();
                }

                if (currentLogFile == null && isLogging) {
                    currentLogFile = new File(nextFileName());
                    File parent = currentLogFile.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    } else if (currentLogFile.exists()) {
                        currentLogFile.delete();
                    }
                    currentLogFile.createNewFile();
                    bufferedWriter = new BufferedWriter(new FileWriter(currentLogFile, true), WRITER_BUFFER_LEN);
                }
            } catch (IOException e) {
                android.util.Log.e(TAG, "close error: " + e.toString());
            }
            return bufferedWriter;
        }

        public void close() {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                    bufferedWriter = null;
                    currentLogFile = null;
                }
            } catch (IOException e) {
                android.util.Log.e(TAG, "close error: " + e.toString());
            }
        }

        // delete of files more than 1 day old
        private static void deleteOldAndEmptyFiles() {
            File dir = LOG_FILE_DIR;
            if (dir.exists()) {
                File[] files = dir.listFiles();

                for (File f : files) {
                    if (f.length() == 0 ||
                            f.lastModified() + MAXFILEAGE < System.currentTimeMillis()) {
                        f.delete();
                    }
                }
            }
        }

        private static String getCurrentTimeStamp(final SimpleDateFormat dateFormat) {
            String currentTimeStamp = null;
            try {
                currentTimeStamp = dateFormat.format(new Date());
            } catch (Exception e) {
                android.util.Log.e(TAG, "getCurrentTimeStamp error: " + e.toString());
            }

            return currentTimeStamp;
        }

        private static String nextFileName() {
            return String.format(LOG_FILE_NAME_FORMAT, FILE_PATH_BASE, getCurrentTimeStamp(FILE_TIMESTAMP_FORMAT));
        }

        static void stopFullLog() {
            if (fullLogProcess != null) {
                fullLogProcess.destroy();
                fullLogProcess = null;
            }
        }

        static Process fullLogProcess;
        static void startFullLog() {
            deleteOldAndEmptyFiles();
            try {
                // clear logcat  buffer
                new ProcessBuilder()
                .command("logcat", "-c")
                .redirectErrorStream(true)
                .start();

                File fullLog = new File(String.format(LOG_FILE_NAME_FORMAT, FULL_FILE_PATH_BASE,
                        getCurrentTimeStamp(FILE_TIMESTAMP_FORMAT)));
                File parent = fullLog.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                } else if (fullLog.exists()) {
                    fullLog.delete();
                }

                // start logcat
                fullLogProcess = Runtime.getRuntime().exec("logcat -v time -f " + fullLog.getAbsolutePath());
            } catch (IOException e) {
               Log.e(TAG, "startLog error: " + e.toString());
            }
        }
    }
}
