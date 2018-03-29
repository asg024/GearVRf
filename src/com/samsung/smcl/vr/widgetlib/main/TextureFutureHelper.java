package com.samsung.smcl.vr.widgetlib.main;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import org.gearvrf.GVRAtlasInformation;
import org.gearvrf.GVRBitmapImage;
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

    private static HashMap<Integer, ImmutableBitmapTexture> sColorTextureCache = new HashMap<>();

    private static class ImmutableBitmapTexture extends GVRBitmapTexture {
        public ImmutableBitmapTexture(GVRContext gvrContext, Bitmap bitmap) {
            super(gvrContext, bitmap);
        }

        @Override
        public void setAtlasInformation(List<GVRAtlasInformation> atlasInformation) {
            onMutatingCall("setAtlasInformation");
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

    public static GVRTexture getFutureBitmapTexture(
            final GVRContext gvrContext, int resId) {
        final Resources resources = gvrContext.getActivity().getResources();
        final Bitmap bitmap = BitmapFactory.decodeResource(resources, resId);
        return new GVRBitmapTexture(gvrContext, bitmap);
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

    @NonNull
    private static Bitmap makeSolidColorBitmap(int color) {
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        if (color != -1) {
            bitmap.eraseColor(color);
        }
        return bitmap;
    }
}
