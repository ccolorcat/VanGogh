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

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: cxx
 * Date: 2017-07-11
 * GitHub: https://github.com/ccolorcat
 */
@SuppressWarnings("unused")
public final class Task {
    final VanGogh vanGogh;
    final Uri uri;
    final int fromPolicy;
    final int connectTimeOut;
    final int readTimeOut;
    final boolean fade;
    final boolean indicatorEnabled;
    final List<Transformation> transformations;
    final Drawable placeholder;
    final Drawable error;
    final Options options;
    final Callback callback;

    final Target target;
    final String stableKey;
    final String key;
    final Object tag;

    private boolean canceled;

    private Task(Creator creator) {
        vanGogh = creator.vanGogh;
        uri = creator.uri;
        fromPolicy = creator.fromPolicy;
        connectTimeOut = creator.connectTimeOut;
        readTimeOut = creator.readTimeOut;
        fade = creator.fade;
        indicatorEnabled = creator.indicatorEnabled;
        transformations = Utils.immutableList(creator.transformations);
        placeholder = creator.placeholder;
        error = creator.error;
        options = creator.options;
        callback = creator.callback;
        target = creator.target;
        stableKey = creator.stableKey;
        key = creator.key;
        tag = creator.tag;
        canceled = false;
    }

    public Uri uri() {
        return uri;
    }

    public int fromPolicy() {
        return fromPolicy;
    }

    public int connectTimeOut() {
        return connectTimeOut;
    }

    public int readTimeOut() {
        return readTimeOut;
    }

    public List<Transformation> transformations() {
        return transformations;
    }

    public Options options() {
        return options;
    }

    public String stableKey() {
        return stableKey;
    }

    public String key() {
        return key;
    }

    public Object tag() {
        return tag;
    }

    void cancel() {
        canceled = true;
    }

    boolean isCanceled() {
        return canceled;
    }

    void onPreExecute() {
        target.onPrepare(placeholder);
    }

    void onPostResult(Result result, Throwable cause) {
        if (result != null) {
            Bitmap bitmap = result.bitmap();
            From from = result.from();
            Drawable drawable = new VanGoghDrawable(vanGogh.context, bitmap, from, fade, indicatorEnabled);
            target.onLoaded(drawable, from);
            callback.onSuccess(bitmap);
        } else if (cause != null) {
            target.onFailed(error, cause);
            callback.onError(cause);
        }
    }


    public static final class Options implements Cloneable {
        static final int SCALE_TYPE_NO = 0;
        static final int SCALE_TYPE_CENTER_INSIDE = 1;
        static final int SCALE_TYPE_CENTER_CROP = 1 << 1;
        static final int SCALE_TYPE_FIT_XY = 1 << 2;

        private Bitmap.Config config;
        private int targetWidth;
        private int targetHeight;
        private boolean hasMaxSize;
        private boolean hasResize;
        private int scaleType;
        private float rotationDegrees;
        private boolean hasRotation;
        private float rotationPivotX;
        private float rotationPivotY;
        private boolean hasRotationPivot;

        public Options() {
            config = Bitmap.Config.ARGB_8888;
            targetWidth = 0;
            targetHeight = 0;
            hasMaxSize = false;
            hasResize = false;
            scaleType = SCALE_TYPE_NO;
            rotationDegrees = 0F;
            hasRotation = false;
            rotationPivotX = 0F;
            rotationPivotY = 0F;
            hasRotationPivot = false;
        }

        public void config(Bitmap.Config config) {
            if (config == null) {
                throw new NullPointerException("config == null");
            }
            this.config = config;
        }

        public Bitmap.Config config() {
            return config;
        }

        public void maxSize(int maxWidth, int maxHeight) {
            setSize(maxWidth, maxHeight, false);
        }

        public void clearMaxSize() {
            hasMaxSize = false;
            tryClearSize();
        }

        public boolean hasMaxSize() {
            return hasMaxSize;
        }

        public void resize(int width, int height) {
            setSize(width, height, true);
            scaleType = SCALE_TYPE_CENTER_INSIDE;
        }

        public void clearResize() {
            hasResize = false;
            scaleType = SCALE_TYPE_NO;
            tryClearSize();
        }

        public boolean hasResize() {
            return hasResize;
        }

        public boolean hasSize() {
            return hasMaxSize || hasResize;
        }

        public int targetWidth() {
            return targetWidth;
        }

        public int targetHeight() {
            return targetHeight;
        }

        public void centerInside() {
            scaleType = SCALE_TYPE_CENTER_INSIDE;
        }

        public void centerCrop() {
            scaleType = SCALE_TYPE_CENTER_CROP;
        }

        public void fitXY() {
            scaleType = SCALE_TYPE_FIT_XY;
        }

        public int scaleType() {
            return scaleType;
        }

        public void rotate(float degrees, float pivotX, float pivotY) {
            rotate(degrees);
            rotationPivotX = pivotX;
            rotationPivotY = pivotY;
            hasRotationPivot = true;
        }

        public boolean hasRotationPivot() {
            return hasRotationPivot;
        }

        public void rotate(float degrees) {
            if (degrees == 0) {
                throw new IllegalArgumentException("illegal degrees");
            }
            rotationDegrees = degrees;
            hasRotation = true;
        }

        public boolean hasRotation() {
            return hasRotation;
        }

        public float rotationDegrees() {
            return rotationDegrees;
        }

        public float rotationPivotX() {
            return rotationPivotX;
        }

        public float rotationPivotY() {
            return rotationPivotY;
        }

        private void setSize(int width, int height, boolean isResize) {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("width <= 0 || height <= 0");
            }
            targetWidth = width;
            targetHeight = height;
            hasResize = isResize;
            hasMaxSize = !hasResize;
        }

        private void tryClearSize() {
            if (!hasMaxSize && !hasResize) {
                targetWidth = 0;
                targetHeight = 0;
            }
        }

        @SuppressWarnings("CloneDoesntCallSuperClone")
        public Options clone() {
            try {
                return (Options) super.clone();
            } catch (CloneNotSupportedException e) {
                LogUtils.e(e);
                throw new RuntimeException(e);
            }
        }

        @Override
        public String toString() {
            return "Options{" +
                    "config=" + config +
                    ", targetWidth=" + targetWidth +
                    ", targetHeight=" + targetHeight +
                    ", hasMaxSize=" + hasMaxSize +
                    ", hasResize=" + hasResize +
                    ", scaleType=" + scaleType +
                    ", rotationDegrees=" + rotationDegrees +
                    ", hasRotation=" + hasRotation +
                    ", rotationPivotX=" + rotationPivotX +
                    ", rotationPivotY=" + rotationPivotY +
                    ", hasRotationPivot=" + hasRotationPivot +
                    '}';
        }
    }


    public final static class Creator {
        final VanGogh vanGogh;
        final Uri uri;
        int fromPolicy;
        int connectTimeOut;
        int readTimeOut;
        boolean fade;
        boolean indicatorEnabled;
        List<Transformation> transformations;
        Drawable placeholder;
        Drawable error;
        Options options;
        Callback callback;

        Target target;
        String stableKey;
        String key;
        Object tag;

        Creator(VanGogh vanGogh, Uri uri) {
            this.vanGogh = vanGogh;
            this.uri = uri;
            this.fromPolicy = vanGogh.defaultFromPolicy;
            this.connectTimeOut = vanGogh.connectTimeOut;
            this.readTimeOut = vanGogh.readTimeOut;
            this.fade = vanGogh.fade;
            this.indicatorEnabled = vanGogh.indicatorEnabled;
            this.transformations = new ArrayList<>(vanGogh.transformations);
            this.placeholder = vanGogh.defaultPlaceholder;
            this.error = vanGogh.defaultError;
            this.options = vanGogh.defaultOptions.clone();
            this.callback = EmptyCallback.INSTANCE;
        }

        /**
         * The policy of image source.
         * Any source, <code>From.ANY.policy</code>
         * Memory and Disk, <code>From.MEMORY.policy | From.DISK.policy</code>
         * Memory and Network, <code>From.MEMORY.policy | From.NETWORK.policy</code>
         * ...
         *
         * @see From
         */
        public Creator from(int fromPolicy) {
            From.checkFromPolicy(fromPolicy);
            this.fromPolicy = fromPolicy;
            return this;
        }

        public Creator connectTimeOut(int timeOut) {
            if (timeOut < 0) {
                throw new IllegalArgumentException("timeOut < 0");
            }
            this.connectTimeOut = timeOut;
            return this;
        }

        public Creator readTimeOut(int timeOut) {
            if (timeOut < 0) {
                throw new IllegalArgumentException("timeOut < 0");
            }
            this.readTimeOut = timeOut;
            return this;
        }

        /**
         * @param fade Enable or disable fade in of images loaded.
         */
        public Creator fade(boolean fade) {
            this.fade = fade;
            return this;
        }

        public Creator indicator(boolean enabled) {
            this.indicatorEnabled = enabled;
            return this;
        }

        public Creator addTransformation(Transformation transformation) {
            if (transformation == null) {
                throw new NullPointerException("transformation == null");
            }
            if (!transformations.contains(transformation)) {
                transformations.add(transformation);
            }
            return this;
        }

        public Creator clearTransformation() {
            transformations.clear();
            return this;
        }

        /**
         * The drawable to be used while the image is being loaded.
         */
        public Creator placeholder(@DrawableRes int placeholderResId) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                placeholder = vanGogh.context.getDrawable(placeholderResId);
            } else {
                placeholder = vanGogh.context.getResources().getDrawable(placeholderResId);
            }
            return this;
        }

        /**
         * The drawable to be used while the image is being loaded.
         */
        public Creator placeholder(Drawable placeholder) {
            this.placeholder = placeholder;
            return this;
        }


        /**
         * The drawable to be used if the request image could not be loaded.
         */
        public Creator error(@DrawableRes int errorResId) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                error = vanGogh.context.getDrawable(errorResId);
            } else {
                error = vanGogh.context.getResources().getDrawable(errorResId);
            }
            return this;
        }

        /**
         * The drawable to be used if the request image could not be loaded.
         */
        public Creator error(Drawable error) {
            this.error = error;
            return this;
        }

        public Creator config(Bitmap.Config config) {
            options.config(config);
            return this;
        }

        /**
         * Resize the image to less than the specified size in pixels.
         */
        public Creator maxSize(int maxWidth, int maxHeight) {
            options.maxSize(maxWidth, maxHeight);
            return this;
        }

        public Creator clearMaxSize() {
            options.clearMaxSize();
            return this;
        }

        /**
         * Resize the image to the specified size in pixels.
         */
        public Creator resize(int width, int height) {
            options.resize(width, height);
            return this;
        }

        public Creator clearResize() {
            options.clearResize();
            return this;
        }

        public Creator centerInside() {
            options.centerInside();
            return this;
        }

        public Creator centerCrop() {
            options.centerCrop();
            return this;
        }

        public Creator fitXY() {
            options.fitXY();
            return this;
        }

        /**
         * Rotate the image by the specified degrees around a pivot point.
         */
        public Creator rotate(float degrees, float pivotX, float pivotY) {
            options.rotate(degrees, pivotX, pivotY);
            return this;
        }

        /**
         * Rotate the image by the specified degrees.
         */
        public Creator rotate(float degrees) {
            options.rotate(degrees);
            return this;
        }


        public Creator callback(Callback callback) {
            this.callback = (callback != null ? callback : EmptyCallback.INSTANCE);
            return this;
        }

        public void into(ImageView view) {
            if (view == null) {
                throw new NullPointerException("view == null");
            }
            quickFetchOrEnqueue(new ImageViewTarget(view), true);
        }

        public void fetch(Callback callback) {
            if (callback == null) {
                throw new NullPointerException("callback == null");
            }
            quickFetchOrEnqueue(new FetchTarget(callback), false);
        }

        public void into(Target target) {
            if (target == null) {
                throw new NullPointerException("target == null");
            }
            quickFetchOrEnqueue(target, true);
        }

        private void quickFetchOrEnqueue(Target target, boolean enqueue) {
            Utils.checkMain();
            this.target = target;
            if (uri == Uri.EMPTY) {
                vanGogh.cancelExistingTask(this.target.uniqueCode());
                Throwable cause = new UnsupportedOperationException("unsupported uri: " + uri);
                this.target.onFailed(error, cause);
                callback.onError(cause);
                return;
            }

            stableKey = Utils.createStableKey(this);
            key = Utils.createKey(this);
            if (tag == null) tag = stableKey;
            if ((fromPolicy & From.MEMORY.policy) != 0) {
                Bitmap bitmap = vanGogh.getFromMemoryCache(key);
                if (bitmap != null) {
                    Drawable drawable = new VanGoghDrawable(vanGogh.context, bitmap, From.MEMORY, false, indicatorEnabled);
                    this.target.onLoaded(drawable, From.MEMORY);
                    callback.onSuccess(bitmap);
                    return;
                }
            }
            Task task = new Task(this);
            if (enqueue) {
                vanGogh.enqueueAndSubmit(task);
            } else {
                vanGogh.submit(task);
            }
        }
    }
}
