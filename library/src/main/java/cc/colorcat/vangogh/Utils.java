/*
 * Copyright 2018 cxx
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.colorcat.vangogh;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.ColorInt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: cxx
 * Date: 2017-07-06
 * GitHub: https://github.com/ccolorcat
 */
class Utils {

    static void flushStackLocalLeaks(Looper looper) {
        Handler handler = new Handler(looper) {
            @Override
            public void handleMessage(Message msg) {
                sendMessageDelayed(obtainMessage(), 1000);
            }
        };
        handler.sendMessageDelayed(handler.obtainMessage(), 1000);
    }

    static <T> List<T> immutableList(List<T> list) {
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    static void checkMain() {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            throw new IllegalStateException("Current thread is not main thread.");
        }
    }

    static Bitmap makeWatermark(Bitmap src, @ColorInt int color, Task.Options to) {
        int width = src.getWidth();
        int height = src.getHeight();
        int size = Math.min(width / 4, height / 4);
        if (size < 2) return src;
        Bitmap.Config config = src.getConfig();
        if (config == null) config = to.config();
        Bitmap result = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0F, 0F, null);
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        int r = size >> 1;
        canvas.drawCircle(r, r, r, paint);
//        canvas.save();
        return result;
    }

    static File getCacheDirectory(Context context) {
        File dir = context.getExternalCacheDir();
        if (dir == null) {
            dir = context.getCacheDir();
        }
        File result = new File(dir, "VanGogh");
        if (result.exists() || result.mkdirs()) {
            return result;
        }
        throw new RuntimeException("Can't create directory " + result.getAbsolutePath());
    }

    static int calculateMemoryCacheSize(Context ctx) {
        ActivityManager am = getService(ctx, Context.ACTIVITY_SERVICE);
        int memoryClass = am.getMemoryClass();
        return 1024 * 1024 * memoryClass / 7;
    }

    static int sizeOf(Bitmap bitmap) {
        return bitmap.getByteCount();
    }

    static long sizeOf(File file) {
        return file.length();
    }

    static void justDump(InputStream is, OutputStream os) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(is);
        BufferedOutputStream bos = new BufferedOutputStream(os);
        byte[] buffer = new byte[4096];
        for (int length = bis.read(buffer); length != -1; length = bis.read(buffer)) {
            bos.write(buffer, 0, length);
        }
        bos.flush();
    }

    static void dumpAndClose(InputStream is, OutputStream os) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(is);
        BufferedOutputStream bos = new BufferedOutputStream(os);
        try {
            byte[] buffer = new byte[4096];
            for (int length = bis.read(buffer); length != -1; length = bis.read(buffer)) {
                bos.write(buffer, 0, length);
            }
            bos.flush();
        } finally {
            close(bis);
            close(bos);
        }
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignore) {
            }
        }
    }

    static void deleteContents(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) throw new IOException("not a readable directory: " + dir);
        for (File file : files) {
            if (file.isDirectory()) {
                deleteContents(file);
            }
            if (!file.delete()) {
                throw new IOException("failed to delete file: " + file);
            }
        }
    }

    static void deleteIfExists(File... files) throws IOException {
        for (File file : files) {
            deleteIfExists(file);
        }
    }

    static void deleteIfExists(File file) throws IOException {
        if (file.exists() && !file.delete()) {
            throw new IOException("failed to delete file: " + file);
        }
    }

    static void renameTo(File from, File to, boolean deleteDest) throws IOException {
        if (deleteDest) {
            deleteIfExists(to);
        }
        if (!from.renameTo(to)) {
            throw new IOException("failed to rename from " + from + " to " + to);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getService(Context ctx, String service) {
        return (T) ctx.getSystemService(service);
    }

    static String createStableKey(Uri uri) {
        return md5(uri.toString());
    }

    /**
     * md5 加密，如果加密失败则原样返回
     */
    private static String md5(String resource) {
        String result = resource;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(resource.getBytes());
            byte[] bytes = digest.digest();
            int len = bytes.length << 1;
            StringBuilder sb = new StringBuilder(len);
            for (byte b : bytes) {
                sb.append(Character.forDigit((b & 0xf0) >> 4, 16));
                sb.append(Character.forDigit(b & 0x0f, 16));
            }
            result = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            LogUtils.e(e);
        }
        return result;
    }

    private static byte[] toBytesAndClose(InputStream is) throws IOException {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            for (int length = is.read(buffer); length != -1; length = is.read(buffer)) {
                os.write(buffer, 0, length);
            }
            os.flush();
            return os.toByteArray();
        } finally {
            close(is);
        }
    }

    static Bitmap transformStreamAndClose(InputStream is, Task.Options to) throws IOException {
        boolean scaleDown = to.hasMaxSize();
        Bitmap result = !to.hasSize() ? decodeStreamAndClose(is) : decodeStreamAndClose(is, to, scaleDown);
        if (to.hasResize() || to.hasRotation()) {
            result = applyOptions(result, to, scaleDown);
        }
        return result;
    }

    private static Bitmap applyOptions(Bitmap result, Task.Options to, boolean onlyScaleDown) {
        int inWidth = result.getWidth(), inHeight = result.getHeight();
        int drawX = 0, drawY = 0;
        int drawWidth = inWidth, drawHeight = inHeight;
        Matrix matrix = new Matrix();
        int targetWidth = to.targetWidth(), targetHeight = to.targetHeight();
        if (to.hasRotationPivot()) {
            matrix.setRotate(to.rotationDegrees(), to.rotationPivotX(), to.rotationPivotY());
        } else if (to.hasRotation()) {
            matrix.setRotate(to.rotationDegrees());
        }
        final int scaleType = to.scaleType();
        switch (scaleType) {
            case Task.Options.SCALE_TYPE_CENTER_CROP: {
                float widthRatio = targetWidth / (float) inWidth;
                float heightRatio = targetHeight / (float) inHeight;
                float scaleX, scaleY;
                if (widthRatio > heightRatio) {
                    int newSize = (int) Math.ceil(inHeight * (heightRatio / widthRatio));
                    drawY = (inHeight - newSize) / 2;
                    drawHeight = newSize;
                    scaleX = widthRatio;
                    scaleY = targetHeight / (float) drawHeight;
                } else {
                    int newSize = (int) Math.ceil(inWidth * (widthRatio / heightRatio));
                    drawX = (inWidth - newSize) / 2;
                    drawWidth = newSize;
                    scaleX = targetWidth / (float) drawWidth;
                    scaleY = heightRatio;
                }
                if (shouldResize(onlyScaleDown, inWidth, inHeight, targetWidth, targetHeight)) {
                    matrix.preScale(scaleX, scaleY);
                }
                break;
            }
            case Task.Options.SCALE_TYPE_CENTER_INSIDE: {
                float widthRatio = targetWidth / (float) inWidth;
                float heightRatio = targetHeight / (float) inHeight;
                float scale = widthRatio < heightRatio ? widthRatio : heightRatio;
                if (shouldResize(onlyScaleDown, inWidth, inHeight, targetWidth, targetHeight)) {
                    matrix.preScale(scale, scale);
                }
                break;
            }
            case Task.Options.SCALE_TYPE_FIT_XY: {
                float sx = targetWidth / (float) inWidth;
                float sy = targetHeight / (float) inHeight;
                if (shouldResize(onlyScaleDown, inWidth, inHeight, targetWidth, targetHeight)) {
                    matrix.preScale(sx, sy);
                }
                break;
            }
            default:
                break;
        }
        return Bitmap.createBitmap(result, drawX, drawY, drawWidth, drawHeight, matrix, true);
    }


    private static boolean shouldResize(boolean onlyScaleDown, int inWidth, int inHeight, int targetWidth, int targetHeight) {
        return !onlyScaleDown || inWidth > targetWidth || inHeight > targetHeight;
    }

    private static Bitmap decodeStreamAndClose(InputStream is) {
        try {
            return BitmapFactory.decodeStream(is);
        } finally {
            close(is);
        }
    }

    private static Bitmap decodeStreamAndClose(InputStream is, Task.Options to, boolean scaleDown) throws IOException {
        BufferedInputStream bis = null;
        InputStream resettable = is;
        try {
            if (resettable.available() == 0) {
                resettable = new ByteArrayInputStream(toBytesAndClose(is));
            }
            bis = new BufferedInputStream(resettable);
            bis.mark(bis.available());
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inPreferredConfig = to.config();
            BitmapFactory.decodeStream(bis, null, options);
            bis.reset();
            options.inSampleSize = calculateInSampleSize(options, to, scaleDown);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(bis, null, options);
        } finally {
            close(bis);
            close(resettable);
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options bo, Task.Options to, boolean scaleDown) {
        int inSampleSize = 1;
        final int reqWidth = to.targetWidth(), reqHeight = to.targetHeight();
        final int width = bo.outWidth, height = bo.outHeight;
        if (width > reqWidth || height > reqHeight) {
            int widthRatio = (int) Math.floor((float) width / (float) reqWidth);
            int heightRatio = (int) Math.floor((float) height / (float) reqHeight);
            inSampleSize = scaleDown ? Math.max(widthRatio, heightRatio) : Math.min(widthRatio, heightRatio);
        }
        return inSampleSize;
    }

    static String createKey(Creator creator) {
        StringBuilder builder = new StringBuilder(creator.stableKey);
        Task.Options options = creator.options;
        if (options.hasMaxSize()) {
            builder.append("|maxSize:")
                    .append(options.targetWidth())
                    .append('x')
                    .append(options.targetHeight());
        } else if (options.hasResize()) {
            builder.append("|resize:")
                    .append(options.targetWidth())
                    .append('x')
                    .append(options.targetHeight())
                    .append("scaleType:")
                    .append(options.scaleType());
        }
        if (options.hasRotation()) {
            builder.append("|rotation:")
                    .append(options.rotationDegrees());
            if (options.hasRotationPivot()) {
                builder.append("pivot:")
                        .append(options.rotationPivotX())
                        .append('x')
                        .append(options.rotationPivotY());
            }
        }
        for (int i = 0, size = creator.transformations.size(); i < size; ++i) {
            builder.append('|').append(creator.transformations.get(i).getKey());
        }
        return builder.toString();
    }

    private Utils() {
        throw new AssertionError("no instance");
    }
}
