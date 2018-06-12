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
import android.os.Looper;
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
    static <T> List<T> immutableList(List<T> list) {
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    static void checkMain() {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            throw new IllegalStateException("Method call should not happen reqFrom the main thread.");
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

    static boolean dumpAndCloseQuietly(InputStream is, OutputStream os) {
        BufferedInputStream bis = new BufferedInputStream(is);
        BufferedOutputStream bos = new BufferedOutputStream(os);
        try {
            byte[] buffer = new byte[4096];
            for (int length = bis.read(buffer); length != -1; length = bis.read(buffer)) {
                bos.write(buffer, 0, length);
            }
            bos.flush();
            return true;
        } catch (IOException e) {
            LogUtils.e(e);
            return false;
        } finally {
            close(bis);
            close(bos);
        }
    }

    static void close(Closeable closeable) {
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

    /**
     * md5 加密，如果加密失败则原样返回
     */
    static String md5(String resource) {
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

    static Bitmap decodeStreamAndClose(InputStream is) {
        try {
            return BitmapFactory.decodeStream(is);
        } finally {
            close(is);
        }
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

    static Bitmap decodeStreamAndClose(InputStream is, Task.Options to) throws IOException {
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
            options.inSampleSize = calculateInSampleSize(options, to);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(bis, null, options);
        } finally {
            close(bis);
            close(resettable);
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options bo, Task.Options to) {
        final int maxWidth = to.targetWidth(), maxHeight = to.targetHeight();
        final int width = bo.outWidth, height = bo.outHeight;
        int inSampleSize = 1;
        while (width / inSampleSize > maxWidth && height / inSampleSize > maxHeight) {
            inSampleSize <<= 1;
        }
        return inSampleSize;
    }

    static Bitmap transformResult(Bitmap result, Task.Options ops, List<Transformation> transformations) {
        Bitmap newResult = result;
        if (ops.hasSize() || ops.hasRotation()) {
            newResult = applyOptions(result, ops);
        }
        for (Transformation transformation : transformations) {
            newResult = transformation.transform(newResult);
        }
        return newResult;
    }

    static Bitmap applyOptions(Bitmap result, Task.Options ops) {
        Matrix matrix = new Matrix();
        final int width = result.getWidth(), height = result.getHeight();
        if (ops.hasSize()) {
            final int reqWidth = ops.targetWidth(), reqHeight = ops.targetHeight();
            if (reqWidth != width && reqHeight != height
                    || reqWidth == width && reqHeight < height
                    || reqHeight == height && reqWidth < width) {
                float scaleX = ((float) reqWidth) / width;
                float scaleY = ((float) reqHeight) / height;
                float scale = Math.min(scaleX, scaleY);
                matrix.postScale(scale, scale);
            }
        }
        if (ops.hasRotationPivot()) {
            matrix.postRotate(ops.rotationDegrees(), ops.rotationPivotX(), ops.rotationPivotY());
        } else if (ops.hasRotation()) {
            matrix.postRotate(ops.rotationDegrees());
        }
        return Bitmap.createBitmap(result, 0, 0, width, height, matrix, true);
    }

    static String createTaskKey(Creator creator) {
        StringBuilder builder = new StringBuilder(creator.uriKey);
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

    //    static String createKey(Task.Creator creator) {
//        StringBuilder sb = new StringBuilder(creator.uriKey).append('@');
//        Task.Options op = creator.options;
//        if (op.hasSize()) {
//            sb.append(op.hasMaxSize() ? "maxSize:" : "resize:")
//                    .append(op.targetWidth()).append('x').append(op.targetHeight())
//                    .append("scaleType:").append(op.scaleType());
//
//        }
//        if (op.hasRotation()) {
//            sb.append("rotation:").append(op.rotationDegrees());
//            if (op.hasRotationPivot()) {
//                sb.append("pivot:").append(op.rotationPivotX()).append("x").append(op.rotationPivotY());
//            }
//        }
//        for (int i = 0, size = creator.transformations.size(); i < size; ++i) {
//            sb.append(creator.transformations.get(i).getKey());
//        }
//        return sb.toString();
//    }
//
    static void checkNotNull(Object object, String msg) {
        if (object == null) {
            throw new NullPointerException(msg);
        }
    }

    static <T> T nullElse(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    private Utils() {
        throw new AssertionError("no instance");
    }
}
