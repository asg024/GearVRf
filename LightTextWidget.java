package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject.IntervalFrequency;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.TextView;

import com.samsung.smcl.utility.Log;
import com.samsung.smcl.vr.gvrf_launcher.util.Helpers;

@SuppressWarnings("deprecation")
public class LightTextWidget extends Widget implements TextContainer {

    private final TextWidget.TextParams params = new TextWidget.TextParams();

    public static TextContainer copy(TextContainer src, TextContainer dest) {
        return TextWidget.TextParams.copy(src, dest);
    }

    /**
     * Construct a wrapper for an existing {@link GVRSceneObject}.
     *
     * @param context     The current {@link GVRContext}.
     * @param sceneObject The {@link GVRSceneObject} to wrap.
     */
    public LightTextWidget(final GVRContext context, final GVRSceneObject sceneObject) {
        super(context, sceneObject);
    }

    /**
     * Deriving classes should override and do whatever processing is
     * appropriate.
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

        String attribute = attributes.getProperty("text");
        if (attribute != null) {
            setText(attribute);
        }

        attribute = attributes.getProperty("text_size");
        if (attribute != null) {
            setTextSize(Float.parseFloat(attribute));
        }

        attribute = attributes.getProperty("background");
        if (attribute != null) {
            setBackGround(context.getContext().getResources()
                    .getDrawable(Integer.parseInt(attribute)));
        }

        attribute = attributes.getProperty("background_color");
        if (attribute != null) {
            setBackgroundColor(Integer.parseInt(attribute));
        }

        attribute = attributes.getProperty("gravity");
        if (attribute != null) {
            setGravity(Integer.parseInt(attribute));
        }

        attribute = attributes.getProperty("refresh_freq");
        if (attribute != null) {
            setRefreshFrequency(IntervalFrequency.valueOf(attribute));
        }

        attribute = attributes.getProperty("text_color");
        if (attribute != null) {
            setTextColor(Integer.parseInt(attribute));
        }
    }

    /**
     * Shows a {@link TextView} on a {@linkplain Widget widget} with view's
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
    }

    /**
     * Shows a {@link TextView} on a {@linkplain Widget widget} with view's
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
        setText(text);
    }

    public LightTextWidget(GVRContext context, GVRSceneObject sceneObject, CharSequence text) {
        super(context, sceneObject);
        setText(text);
    }

    private final Paint mTextPaint = new Paint(Paint.LINEAR_TEXT_FLAG
            | Paint.SUBPIXEL_TEXT_FLAG);


    private static final String TAG = LightTextWidget.class.getSimpleName();
    public static final float TEXT_SCALE = 5;

    private void apply() {
        Log.d(TAG, "text widget apply...");

        int bWidth = (int) (getWidth() * 100);
        int bHeight = (int) (getHeight() * 100);
        Log.d(TAG, "text widget apply size [%d, %d]", bWidth, bHeight);

        Bitmap bitmap = Bitmap.createBitmap(bWidth, bHeight
                , Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // draw bg color
        int bgColor = params.getBackgroundColor();
        Log.d(TAG, "text widget apply bgColor = %d", bgColor);

        canvas.drawColor(bgColor);

        // draw bg bitmap
        Drawable bg = params.getBackGround();
        if (bg != null) {
            Log.d(TAG, "text widget apply bg = %s", bg);
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
        Log.d(TAG, "text widget apply textColor = %d", textColor);

        mTextPaint.setColor(textColor);

        // apply text size
        float textSize = getTextSize();
        Log.d(TAG, "text widget apply textSize = %f scaledTextSixe = %f",
                textSize, TEXT_SCALE * textSize);

        mTextPaint.setTextSize(TEXT_SCALE * textSize);

        // draw text
        String text = getTextString();
        if (text != null) {
            Log.d(TAG, "text widget apply text = %s", text);

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

        // apply texture
        setTexture(Helpers.getFutureBitmapTexture(getGVRContext(), bitmap));
    }

    public Drawable getBackGround() {
        return params.getBackGround();
    }

    public int getBackgroundColor() {
        return params.getBackgroundColor();
    }

    public int getGravity() {
        return params.getGravity();
    }

    public IntervalFrequency getRefreshFrequency() {
        return params.getRefreshFrequency();
    }

    public CharSequence getText() {
        return params.getText();
    }

    public int getTextColor() {
        return params.getTextColor();
    }

    public float getTextSize() {
        return params.getTextSize();
    }

    public String getTextString() {
        return params.getTextString();
    }

    public void setBackGround(Drawable drawable) {
        params.setBackGround(drawable);
        apply();
    }

    public void setBackgroundColor(int color) {
        params.setBackgroundColor(color);
        apply();
    }

    /**
     * @param gravity Supported gravities: {@link Gravity#TOP}, {@link Gravity#BOTTOM}, {@link Gravity#LEFT},
     *                {@link Gravity#RIGHT}, {@link Gravity#CENTER_HORIZONTAL}, {@link Gravity#CENTER_VERTICAL},
     *                {@link Gravity#CENTER}
     */
    public void setGravity(int gravity) {
        params.setGravity(gravity);
        apply();
    }

    public void setRefreshFrequency(IntervalFrequency frequency) {
        params.setRefreshFrequency(frequency);
    }

    public void setText(CharSequence text) {
        params.setText(text);
        apply();
    }

    public void setTextColor(int color) {
        params.setTextColor(color);
        apply();
    }

    /**
     * Text size to the given value, interpreted as "scaled pixel" units.
     */
    public void setTextSize(float size) {
        params.setTextSize(size);
        apply();
    }

    public TextWidget.TextParams getTextParams() {
        return params;
    }

    public void setTextParams(final TextContainer textInfo) {
        LightTextWidget.copy(textInfo, this);
    }

    @Override
    public String toString() {
        return params.toString();
    }
}
