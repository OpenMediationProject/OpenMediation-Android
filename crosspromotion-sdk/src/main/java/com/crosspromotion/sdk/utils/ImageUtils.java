// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public final class ImageUtils {
    /**
     * Bitmap to bytes.
     *
     * @param bitmap The bitmap.
     * @param format The format of bitmap.
     * @return bytes
     */
    public static byte[] bitmap2Bytes(final Bitmap bitmap, final CompressFormat format) {
        if (bitmap == null) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(format, 100, baos);
        return baos.toByteArray();
    }

    /**
     * Bytes to bitmap.
     *
     * @param bytes The bytes.
     * @return bitmap
     */
    public static Bitmap bytes2Bitmap(final byte[] bytes) {
        return (bytes == null || bytes.length == 0)
                ? null
                : BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Drawable to bitmap.
     *
     * @param drawable The drawable.
     * @return bitmap
     */
    public static Bitmap drawable2Bitmap(final Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        Bitmap bitmap;
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1,
                    drawable.getOpacity() != PixelFormat.OPAQUE
                            ? Bitmap.Config.ARGB_8888
                            : Bitmap.Config.RGB_565);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    drawable.getOpacity() != PixelFormat.OPAQUE
                            ? Bitmap.Config.ARGB_8888
                            : Bitmap.Config.RGB_565);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Bitmap to drawable.
     *
     * @param bitmap The bitmap.
     * @return drawable
     */
    public static Drawable bitmap2Drawable(Context context, final Bitmap bitmap) {
        return bitmap == null ? null : new BitmapDrawable(context.getResources(), bitmap);
    }

    /**
     * Drawable to bytes.
     *
     * @param drawable The drawable.
     * @param format   The format of bitmap.
     * @return bytes
     */
    public static byte[] drawable2Bytes(final Drawable drawable, final CompressFormat format) {
        return drawable == null ? null : bitmap2Bytes(drawable2Bitmap(drawable), format);
    }

    /**
     * Bytes to drawable.
     *
     * @param bytes The bytes.
     * @return drawable
     */
    public static Drawable bytes2Drawable(Context context, final byte[] bytes) {
        return bitmap2Drawable(context, bytes2Bitmap(bytes));
    }


    /**
     * Return bitmap.
     *
     * @param file The file.
     * @return bitmap
     */
    public static Bitmap getBitmap(final File file) {
        if (file == null) return null;
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    /**
     * Return bitmap.
     *
     * @param filePath The path of file.
     * @return bitmap
     */
    public static Bitmap getBitmap(final String filePath) {
        if (isSpace(filePath)) return null;
        return BitmapFactory.decodeFile(filePath);
    }


    /**
     * Return bitmap.
     *
     * @param is The input stream.
     * @return bitmap
     */
    public static Bitmap getBitmap(final InputStream is) {
        if (is == null) return null;
        return BitmapFactory.decodeStream(is);
    }


    /**
     * Return bitmap.
     *
     * @param data   The data.
     * @param offset The offset.
     * @return bitmap
     */
    public static Bitmap getBitmap(final byte[] data, final int offset) {
        if (data.length == 0) return null;
        return BitmapFactory.decodeByteArray(data, offset, data.length);
    }

    private static boolean isJPEG(final byte[] b) {
        return b.length >= 2
                && (b[0] == (byte) 0xFF) && (b[1] == (byte) 0xD8);
    }

    private static boolean isGIF(final byte[] b) {
        return b.length >= 6
                && b[0] == 'G' && b[1] == 'I'
                && b[2] == 'F' && b[3] == '8'
                && (b[4] == '7' || b[4] == '9') && b[5] == 'a';
    }

    private static boolean isPNG(final byte[] b) {
        return b.length >= 8
                && (b[0] == (byte) 137 && b[1] == (byte) 80
                && b[2] == (byte) 78 && b[3] == (byte) 71
                && b[4] == (byte) 13 && b[5] == (byte) 10
                && b[6] == (byte) 26 && b[7] == (byte) 10);
    }

    private static boolean isBMP(final byte[] b) {
        return b.length >= 2
                && (b[0] == 0x42) && (b[1] == 0x4d);
    }

    private static boolean isEmptyBitmap(final Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }

    /**
     * Return the sample size.
     *
     * @param options   The options.
     * @param maxWidth  The maximum width.
     * @param maxHeight The maximum height.
     * @return the sample size
     */
    private static int calculateInSampleSize(final BitmapFactory.Options options,
                                             final int maxWidth,
                                             final int maxHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        while ((width >>= 1) >= maxWidth && (height >>= 1) >= maxHeight) {
            inSampleSize <<= 1;
        }
        return inSampleSize;
    }

    private static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static byte[] input2Byte(final InputStream is) throws IOException {
        if (is == null) return null;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int len;
            while ((len = is.read(b, 0, 1024)) != -1) {
                os.write(b, 0, len);
            }
            return os.toByteArray();
        } finally {
            is.close();
        }
    }
}
