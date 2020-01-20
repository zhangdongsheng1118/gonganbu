package com.yixin.tinode.tinode.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * Convert Drafty object into a Spanned object
 */
public class SpanFormatter {
    private static final String TAG = "SpanFormatter";

    public static byte[] handleImageToByte(final Context ctx, final Map<String, Object> data) {
        byte[] arrays = null;
        Bitmap bitmap = handleImage(ctx, data);
        if (bitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//            int bytes = bitmap.getByteCount();
//            ByteBuffer buffer = ByteBuffer.allocate(bytes);
//            bitmap.copyPixelsToBuffer(buffer); //Move the byte data to the buffer
            arrays = baos.toByteArray();
        }

        return arrays;
    }

    public static Bitmap handleImage(final Context ctx, final Map<String, Object> data) {
        Bitmap bmp = null;

        if (data != null) {
            DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
            int mViewport = (int) (metrics.widthPixels * 0.8);
            try {
                Object val = data.get("val");
                // If the message is unsent, the bits could be raw byte[] as opposed to
                // base64-encoded.
                byte[] bits = (val instanceof String) ?
                        Base64.decode((String) val, Base64.DEFAULT) : (byte[]) val;
                bmp = BitmapFactory.decodeByteArray(bits, 0, bits.length);
                // Scale bitmap for display density.
                float width = bmp.getWidth() * metrics.density;
                float height = bmp.getHeight() * metrics.density;
                // Make sure the scaled bitmap is no bigger than the viewport size;
                float scaleX = (width < mViewport ? width : mViewport) / width;
                float scaleY = (height < mViewport ? height : mViewport) / height;
                float scale = scaleX < scaleY ? scaleX : scaleY;

                bmp = Bitmap.createScaledBitmap(bmp, (int) (width * scale), (int) (height * scale), true);
            } catch (NullPointerException | IllegalArgumentException | ClassCastException ex) {
                Log.w(TAG, "Broken Image", ex);
            }
        }

        return bmp;
    }
}
