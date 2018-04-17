package com.samsung.smcl.vr.widgetlib.widget.basic;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTextureParameters;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject.IntervalFrequency;
import org.json.JSONObject;

import java.util.concurrent.Future;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;

import com.samsung.smcl.vr.widgetlib.log.Log;

import com.samsung.smcl.vr.widgetlib.widget.Widget;
import com.samsung.smcl.vr.widgetlib.widget.NodeEntry;

import static com.samsung.smcl.vr.widgetlib.main.TextureFutureHelper.getFutureBitmapTexture;

/**
 * Lightweight version of TextWidget.
 * Standard {@link TextWidget} is using {@link GVRTextViewSceneObject} to display the text. It is
 * quite heavy object. Using many of them in the same scene might affect UI performance.
 * LightTextWidget implementation is using canvas.drawText to display the text.
 */

@SuppressWarnings("deprecation")
public class LightTextWidget extends Widget implements TextContainer {
    /**
     * Construct LightTextWidget wrapper for an existing {@link GVRSceneObject}.
     *
     * @param context     The current {@link GVRContext}.
     * @param sceneObject The {@link GVRSceneObject} to wrap.
     */
    public LightTextWidget(final GVRContext context, final GVRSceneObject sceneObject) {
        super(context, sceneObject);
        init((CharSequence) null);
    }

    /**
     * Construct LightTextWidget wrapper for an existing {@link GVRSceneObject}.
     *
     * @param context     The current {@link GVRContext}.
     * @param properties A structured set of properties for the {@code LightTextWidget} instance.
     *                   See {@code lighttextwidget.json} for schema.
     */
    public LightTextWidget(GVRContext context, JSONObject properties) {
        super(context, properties);
        init((CharSequence) null);
    }

    /**
     * A constructor for wrapping existing {@link GVRSceneObject} instances.
     *
     * @param context     The current {@link GVRContext}
     * @param sceneObject The {@link GVRSceneObject} to wrap.
     * @param attributes  A set of class-specific attributes.
     * @throws InstantiationException
     */
    @SuppressWarnings("deprecation")
    public LightTextWidget(GVRContext context, GVRSceneObject sceneObject,
                           NodeEntry attributes) throws InstantiationException {
        super(context, sceneObject, attributes);
        init((CharSequence) null);
    }

    /**
     * Shows a {@link LightTextWidget} on a {@linkplain Widget widget} with view's
     * default height and width.
     *
     * @param context current {@link GVRContext}
     * @param width   Widget height, in GVRF scene graph units.
     *                <p>
     *                Please note that your widget's size is independent of the size
     *                of the internal {@code TextView}: a large mismatch between the
     *                scene object's size and the view's size will result in
     *                'spidery' or 'blocky' text.
     * @param height  Widget width, in GVRF scene graph units.
     */
    public LightTextWidget(GVRContext context, float width, float height) {
        super(context, width, height);
        init((CharSequence) null);
    }

    /**
     * Shows a {@link LightTextWidget} on a {@linkplain Widget widget} with view's
     * default height and width.
     *
     * @param context current {@link GVRContext}
     * @param width   Widget height, in GVRF scene graph units.
     *                <p>
     *                Please note that your widget's size is independent of the size
     *                of the internal {@code TextView}: a large mismatch between the
     *                scene object's size and the view's size will result in
     *                'spidery' or 'blocky' text.
     * @param height  Widget width, in GVRF scene graph units.
     * @param text    {@link CharSequence} to show on the textView
     */
    public LightTextWidget(GVRContext context, float width, float height,
                           CharSequence text) {
        super(context, width, height);
        init(text);
    }

    /**
     * Construct LightTextWidget wrapper for an existing {@link GVRSceneObject}.
     *
     * @param context     The current {@link GVRContext}.
     * @param sceneObject The {@link GVRSceneObject} to wrap.
     * @param text
     */
    public LightTextWidget(GVRContext context, GVRSceneObject sceneObject, CharSequence text) {
        super(context, sceneObject);
        init(text);
    }

    /**
     * Copy text settings from one TextContainer to another one
     * @param src TextContainer the text settings are copied from
     * @param dest TextContainer the text settings are copied to
     * @return updated TextContainer
     */
    public static TextContainer copy(TextContainer src, TextContainer dest) {
        return TextParams.copy(src, dest);
    }

    @Override
    public Drawable getBackGround() {
        return params.getBackGround();
    }

    @Override
    public int getBackgroundColor() {
        return params.getBackgroundColor();
    }

    @Override
    public int getGravity() {
        return params.getGravity();
    }

    @Override
    public IntervalFrequency getRefreshFrequency() {
        return params.getRefreshFrequency();
    }

    @Override
    public CharSequence getText() {
        return params.getText();
    }

    @Override
    public int getTextColor() {
        return params.getTextColor();
    }

    @Override
    public float getTextSize() {
        return params.getTextSize();
    }

    @Override
    public String getTextString() {
        return params.getTextString();
    }

    @Override
    public void setBackGround(Drawable drawable) {
        params.setBackGround(drawable);
        apply();
    }

    @Override
    public void setBackgroundColor(int color) {
        params.setBackgroundColor(color);
        apply();
    }

    /**
     * @param gravity Supported gravities: {@link Gravity#TOP}, {@link Gravity#BOTTOM}, {@link Gravity#LEFT},
     *                {@link Gravity#RIGHT}, {@link Gravity#CENTER_HORIZONTAL}, {@link Gravity#CENTER_VERTICAL},
     *                {@link Gravity#CENTER}
     */
    @Override
    public void setGravity(int gravity) {
        params.setGravity(gravity);
        apply();
    }

    @Override
    public void setRefreshFrequency(IntervalFrequency frequency) {
        params.setRefreshFrequency(frequency);
    }

    @Override
    public void setText(CharSequence text) {
        params.setText(text);
        apply();
    }

    @Override
    public void setTextColor(int color) {
        params.setTextColor(color);
        apply();
    }

    /**
     * Text size to the given value, interpreted as "scaled pixel" units.
     */
    @Override
    public void setTextSize(float size) {
        params.setTextSize(size);
        apply();
    }

    @Override
    public void setTypeface(Typeface typeface) {
        Log.d(TAG, "setTypeface(%s): setting typeface: %s", getName(), typeface);
        params.setTypeface(typeface);
        apply();
    }

    @Override
    public Typeface getTypeface() {
        return params.getTypeface();
    }

    /**
     * Gets the text parameters for the LightTextWidget
     * @return the copy of {@link TextParams}. Changing this instance does not actually affect
     * LightTextWidget. To change the parameters of TextWidget, {@link #setTextParams} should be used.
     */
    public TextParams getTextParams() {
        return params;
    }

    /**
     * Sets the text parameters for the LightTextWidget
     * @return the copy of {@link TextParams}. Changing this instance does not actually effect
     * LightTextWidget. To change the parameters of TextWidget, {@link #setTextParams} should be used.
     */
    public void setTextParams(final TextContainer textInfo) {
        LightTextWidget.copy(textInfo, this);
    }

    @Override
    public String toString() {
        return params.toString();
    }

    private void init(CharSequence text) {
        mNoApply = true;
        params.setText(text);
        try {
            JSONObject properties = getObjectMetadata();
            params.setFromJSON(getGVRContext().getActivity(), properties);
        } finally {
            mNoApply = false;
        }
        apply();
    }

    private boolean mNoApply = false;

    private final Paint mTextPaint = new Paint(Paint.LINEAR_TEXT_FLAG
            | Paint.SUBPIXEL_TEXT_FLAG);


    private static final String TAG = LightTextWidget.class.getSimpleName();

    private void apply() {
        Log.d(TAG, "apply(%s): apply...", getName());
        if (mNoApply) {
            Log.d(TAG, "apply(%s): apply is blocked", getName());
            return;
        }

        int bWidth = (int) (getWidth() * 100);
        int bHeight = (int) (getHeight() * 100);
        Log.d(TAG, "apply(%s): size [%d, %d]", getName(), bWidth, bHeight);

        Bitmap bitmap = Bitmap.createBitmap(bWidth, bHeight
                , Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // draw bg color
        int bgColor = params.getBackgroundColor();
        Log.d(TAG, "apply(%s): bgColor = %d", getName(), bgColor);

        canvas.drawColor(bgColor);

        // draw bg bitmap
        Drawable bg = params.getBackGround();
        if (bg != null) {
            Log.d(TAG, "apply(%s): bg = %s", getName(), bg);
            Bitmap bgBitmap = null;
            if (bg instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) bg;
                bgBitmap = bitmapDrawable.getBitmap();
            } else {
                int width = bg.getIntrinsicWidth();
                int height = bg.getIntrinsicHeight();
                if (width > 0 && height > 0) {
                    final Rect bounds = bg.getBounds();
                    bg.setBounds(0, 0, width - 1, height - 1);
                    bgBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas c = new Canvas(bgBitmap);
                    bg.draw(c);
                    bg.setBounds(bounds);
                }
            }

            if (bgBitmap != null) {
                Rect source = new Rect(0, 0, bgBitmap.getWidth(), bgBitmap.getHeight());
                Rect dist = new Rect(0, 0, bWidth, bHeight);
                canvas.drawBitmap(bgBitmap, source, dist, null);
            }
        }

        // apply text color
        int textColor = getTextColor();
        Log.d(TAG, "apply(%s): textColor = %d", getName(), textColor);

        mTextPaint.setColor(textColor);

        // apply text size
        float textSize = getTextSize();
        Log.d(TAG, "apply(%s): textSize = %f scaledTextSize = %f", getName(),
                textSize, TEXT_SCALE * textSize);

        mTextPaint.setTextSize(TEXT_SCALE * textSize);

        Typeface typeface = params.getTypeface();
        if (typeface != null) {
            Log.d(TAG, "apply(%s): setting text paint typeface to %s", getName(), typeface);
            mTextPaint.setTypeface(typeface);
        } else {
            Log.w(TAG, "apply(%s): typeface is null!", getName());
        }

        // draw text
        String text = getTextString();
        if (text != null) {
            Log.d(TAG, "apply(%s): text = %s", getName(), text);

            Rect textBounds = new Rect();
            mTextPaint.getTextBounds(text, 0, text.length(), textBounds);

            float x = 0;
            float y = 0;

            int textGravity = getGravity();

            // vertical gravity
            int gravity = textGravity & Gravity.VERTICAL_GRAVITY_MASK;
            if (gravity == Gravity.TOP) {
                y = textBounds.height();
            } else if (gravity == Gravity.BOTTOM) {
                y = bHeight;
            } else {// (gravity == Gravity.CENTER_VERTICAL)
                y = (bHeight + textBounds.height()) / 2;
            }

            // horizontal gravity
            gravity = textGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
            if (gravity == Gravity.LEFT) {
                x = 0;
            } else if (gravity == Gravity.RIGHT) {
                x = bWidth - textBounds.width();
            } else {//(gravity == Gravity.CENTER_HORIZONTAL)
                x = (bWidth - textBounds.width()) / 2;
            }

            canvas.drawText(text, x, y, mTextPaint);
        }

        Future<GVRTexture> texture = getFutureBitmapTexture(getGVRContext(), bitmap);
        // Apply trilinear and anisotropic filtering
        GVRTextureParameters textureParameters = new GVRTextureParameters(getGVRContext());
        textureParameters.setMinFilterType(GVRTextureParameters.TextureFilterType.GL_LINEAR_MIPMAP_LINEAR);
        textureParameters.setAnisotropicValue(4);
        try {
            texture.get().updateTextureParameters(textureParameters);
        }
        catch(Exception e){
            Log.e(TAG, "InterruptedException, ExecutionException");
        }
        setTexture(texture);
    }

    private final TextParams params = new TextParams();
    private static final float TEXT_SCALE = 5;

}
