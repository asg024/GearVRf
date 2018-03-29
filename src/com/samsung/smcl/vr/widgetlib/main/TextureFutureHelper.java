package com.samsung.smcl.vr.widgetlib.main;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import org.gearvrf.GVRAtlasInformation;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTextureParameters;
import org.gearvrf.utility.RuntimeAssertion;
import static org.gearvrf.utility.Log.tag;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.WeakHashMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.samsung.smcl.vr.widgetlib.log.Log;

/**
 * Utilities for TextureFutures
 */
public final class TextureFutureHelper {
    private static final String TAG = tag(TextureFutureHelper.class);
    private TextureFutureHelper() {
    }

    private static abstract class TextureFuture implements RunnableFuture<GVRTexture> {
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            mIsCancelled = true;
            if (mIsDone) {
                mIsCancelled = false;
                return false;
            } else {
                return true;
            }
        }

        @Override
        public GVRTexture get() throws InterruptedException,
                ExecutionException {
            try {
                return get(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (TimeoutException e) {
                // With a timeout of MAX_VALUE days, this will never actually
                // happen
                e.printStackTrace();
                Log.e(TAG, e, "TextureFuture.get()");
                return null;
            }
        }

        @Override
        public GVRTexture get(long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException,
                TimeoutException {
            if (mIsCancelled) {
                // Was already cancelled
                throw new CancellationException();
            }
            if (!mCountdown.await(timeout, unit)) {
                throw new TimeoutException();
            }
            if (mIsCancelled) {
                // Was cancelled during processing
                throw new CancellationException();
            }
            Log.d(TAG, "BitmapFuture.get(): result for '%s': %s", mName, mResult);
            return mResult;
        }

        @Override
        public boolean isCancelled() {
            return mIsCancelled;
        }

        @Override
        public boolean isDone() {
            return mIsDone;
        }

        @Override
        public void run() {
            if (!isDone()) {
                if (!isCancelled()) {
                    mResult = getTexture(mContext);
                    Log.d(TAG, "TextureFuture.run(): result for '%s': %s", mName, mResult);
                }
                mIsDone = true;
                mCountdown.countDown();
            }
        }

        protected TextureFuture(GVRContext gvrContext, String name) {
            mContext = gvrContext;
            mName = name;
        }

        @NonNull
        protected abstract GVRTexture getTexture(GVRContext context);

        private final GVRContext mContext;
        private final CountDownLatch mCountdown = new CountDownLatch(1);
        private final String mName;
        private volatile boolean mIsDone;
        private GVRTexture mResult;
        private volatile boolean mIsCancelled;
    }

    /**
     * TextureFuture built around resolved texture
     */
    public static class ResolvedTextureFuture extends TextureFuture {
        /**
         * Create {@link #TextureFuture} wrapped around the resolved texture
         * @param gvrContext
         * @param texture
         * @param name
         */
        public ResolvedTextureFuture(GVRContext gvrContext, @NonNull GVRTexture texture, String name) {
            super(gvrContext, name);
            mTexture = texture;
        }

        @Override
        public GVRTexture get(long timeout, TimeUnit timeUnit) {
            return mTexture;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @NonNull
        @Override
        protected GVRTexture getTexture(GVRContext gvrContext) {
            return mTexture;
        }

        private final GVRTexture mTexture;
    }

    /**
     * Gets a {@link Future} for an {@link GVRBitmapTexture bitmap texture},
     * returning a cached texture instance if possible.
     *
     * @param gvrContext
     * @param resId
     * @return
     */
    public static Future<GVRTexture> getFutureBitmapTexture(
            final GVRContext gvrContext, int resId) {
        final Resources resources = gvrContext.getActivity().getResources();
        final Bitmap bitmap = BitmapFactory.decodeResource(resources, resId);
        return getFutureBitmapTexture(gvrContext, bitmap);
    }

    /**
     * Gets a {@link Future} for an {@linkplain ImmutableBitmapTexture immutable texture} with the
     * specified color, returning a cached texture instance if possible.
     *
     * @param gvrContext
     * @param color
     * @return
     */
    public static Future<GVRTexture> getFutureColorBitmapTexture(final GVRContext gvrContext, int color) {
        RunnableFuture<GVRTexture> future;
        final String s = String.format(Locale.getDefault(), "color: 0x%X", color);

        final ImmutableBitmapTexture texture;
        synchronized (sColorTextureCache) {
            Log.d(TAG, "getFutureColorBitmapTexture(): fetching cached texture for color 0x%08X", color);
            texture = sColorTextureCache.get(color);
        }

        if (texture == null) {
            synchronized (sColorTextureFutureCache) {
                future = sColorTextureFutureCache.get(color);
                if (future == null) {
                    Log.d(TAG, "getFutureColorBitmapTexture(): creating new texture for color 0x%08X", color);
                    future = new ColorTextureFuture(gvrContext, color, s);
                    sColorTextureFutureCache.put(color, (ColorTextureFuture) future);
                    gvrContext.runOnGlThread(future);
                } else {
                    Log.d(TAG, "getFutureColorBitmapTexture(): returning cached future for color 0x%08X", color);
                }
            }
        } else {
            Log.d(TAG, "getFutureColorBitmapTexture(): returning cached texture for color 0x%08X", color);
            future = new ResolvedTextureFuture(gvrContext, texture, s);
        }

        return future;
    }

    /**
     * Gets a {@link Future} for an {@link GVRBitmapTexture bitmap texture},
     * returning a cached texture instance if possible.
     *
     * @param gvrContext
     * @param bitmap
     * @return
     */
    public static Future<GVRTexture> getFutureBitmapTexture(
            final GVRContext gvrContext, final Bitmap bitmap) {
        final String s = "";
        return getFutureBitmapTexture(gvrContext, bitmap, s);
    }

    /**
     * Gets an {@linkplain ImmutableBitmapTexture immutable texture} with the specified color,
     * returning a cached instance if possible.
     *
     * @param gvrContext
     * @param color
     * @return
     */
    public static ImmutableBitmapTexture getSolidColorTexture(GVRContext gvrContext, int color) {
        ImmutableBitmapTexture texture;
        synchronized (sColorTextureCache) {
            texture = sColorTextureCache.get(color);
            Log.d(TAG, "getSolidColorTexture(): have cached texture for 0x%08X: %b", color, texture != null);
            if (texture == null) {
                texture = new ImmutableBitmapTexture(gvrContext, makeSolidColorBitmap(color));
                Log.d(TAG, "getSolidColorTexture(): caching texture for 0x%08X", color);
                sColorTextureCache.put(color, texture);
                Log.d(TAG, "getSolidColorTexture(): succeeded caching for 0x%08X: %b", color, sColorTextureCache.containsKey(color));
            }
        }

        return texture;
    }

    private static class BitmapTextureFuture extends TextureFuture {

        public BitmapTextureFuture(final GVRContext gvrContext, final Bitmap bitmap, String name) {
            super(gvrContext, name);
            mBitmap = bitmap;
        }

        @Override
        @NonNull
        protected GVRTexture getTexture(GVRContext context) {
            return new GVRBitmapTexture(context, mBitmap);
        }

        private final Bitmap mBitmap;
    }

    private static class ColorTextureFuture extends TextureFuture {
        ColorTextureFuture(GVRContext gvrContext, int color, String name) {
            super(gvrContext, name);
            mColor = color;
        }

        @Override
        @NonNull
        protected GVRTexture getTexture(GVRContext gvrContext) {
            return getSolidColorTexture(gvrContext, mColor);
        }

        private final int mColor;
    }

    private static HashMap<Integer, ImmutableBitmapTexture> sColorTextureCache = new HashMap<>();
    private static WeakHashMap<Integer, ColorTextureFuture> sColorTextureFutureCache = new WeakHashMap<>();

    private static class ImmutableBitmapTexture extends GVRBitmapTexture {
        public ImmutableBitmapTexture(GVRContext gvrContext, Bitmap bitmap) {
            super(gvrContext, bitmap);
        }

        @Override
        public void setAtlasInformation(List<GVRAtlasInformation> atlasInformation) {
            onMutatingCall("setAtlasInformation");
        }

        @Override
        public void setTexCoords(String vertexTexCoord, String shaderVariable) throws UnsupportedOperationException {
            onMutatingCall("setTexCoords");
        }

        @Override
        public Future<Boolean> update(Bitmap bitmap) {
            onMutatingCall("update");
            return null;
        }

        @Override
        public Future<Boolean> update(int width, int height, byte[] grayscaleData) {
            onMutatingCall("update");
            return null;
        }

        @Override
        public void updateTextureParameters(GVRTextureParameters textureParameters) {
            onMutatingCall("updateTextureParameters");
        }

        private void onMutatingCall(final String method) {
            final String msg = "%s(): mutating call on ImmutableBitmapTexture!";
            Log.e(TAG, msg, method);
            throw new RuntimeAssertion(msg, method);
        }
    }

    @NonNull
    private static Future<GVRTexture> getFutureBitmapTexture(GVRContext gvrContext, Bitmap bitmap, String s) {
        final BitmapTextureFuture bitmapFuture = new BitmapTextureFuture(gvrContext, bitmap, s);
        gvrContext.runOnGlThread(bitmapFuture);
        return bitmapFuture;
    }

    @NonNull
    private static Bitmap makeSolidColorBitmap(int color) {
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        if (color != -1) {
            bitmap.eraseColor(color);
        }
        return bitmap;
    }
}
