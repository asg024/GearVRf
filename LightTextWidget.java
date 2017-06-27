package com.samsung.smcl.vr.widgets;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject.IntervalFrequency;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.TextView;

import com.samsung.smcl.utility.Log;
import com.samsung.smcl.vr.gvrf_launcher.util.Helpers;

import static com.samsung.smcl.vr.widgets.JSONHelpers.*;

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
        init((CharSequence) null);
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
        init((CharSequence) null);
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
        init((CharSequence) null);
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
        init(text);
    }

    public LightTextWidget(GVRContext context, GVRSceneObject sceneObject, CharSequence text) {
        super(context, sceneObject);
        init(text);
    }

    private enum Attributes {
        text, text_size, background, background_color, gravity, refresh_freq, text_color, typeface
    }

    private void init(CharSequence text) {
        final JSONObject metadata = getObjectMetadata();
        text = optString(metadata, Attributes.text, text != null ? text.toString() : "");
        final float textSize = optFloat(metadata, Attributes.text_size, params.getTextSize());
        String backgroundResStr = optString(metadata, Attributes.background);
        final int backgroundResId;
        if (backgroundResStr != null && !backgroundResStr.isEmpty()) {
            backgroundResId = Helpers.getId(getContext(), backgroundResStr, "drawable");
        } else {
            backgroundResId = -1;
        }
        final int backgroundColor = Helpers.getJSONColor(metadata, Attributes.background_color, params.getBackgroundColor());
        final IntervalFrequency refresh = optEnum(metadata, Attributes.refresh_freq, params.getRefreshFrequency());
        final int textColor = Helpers.getJSONColor(metadata, Attributes.text_color, params.getTextColor());
        final JSONObject typefaceJson = optJSONObject(metadata, Attributes.typeface);

        mNoApply = true;
        try {
            if (typefaceJson != null) {
                Typeface typeface = TypefaceManager.get(this).getTypeface(typefaceJson);
                setTypeface(typeface);
            }

            setTextSize(textSize);
            if (backgroundResId != -1) {
                setBackGround(getContext().getResources()
                            .getDrawable(backgroundResId));
            }
            setBackgroundColor(backgroundColor);
            setRefreshFrequency(refresh);
            setTextColor(textColor);
            setText(text);
        } finally {
            mNoApply = false;
        }
        apply();
    }

    private boolean mNoApply = false;

    private final Paint mTextPaint = new Paint(Paint.LINEAR_TEXT_FLAG
            | Paint.SUBPIXEL_TEXT_FLAG);


    private static final String TAG = LightTextWidget.class.getSimpleName();
    public static final float TEXT_SCALE = 5;

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
