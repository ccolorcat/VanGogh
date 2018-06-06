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
import android.graphics.drawable.BitmapDrawable;
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
public class Task {
    private final VanGogh vanGogh;

    private final Uri uri;
    private final String stableKey;
    private final int fromPolicy;

    private final int connectTimeOut;
    private final int readTimeOut;

    private final Target target;
    private final Drawable loadingDrawable;
    private final Drawable errorDrawable;

    private final Options options;

    private final List<Transformation> transformations;
    private final boolean fade;
    private final Callback callback;

    private Task(Creator creator) {
        vanGogh = creator.vanGogh;
        uri = creator.uri;
        stableKey = creator.stableKey;
        fromPolicy = creator.fromPolicy;
        connectTimeOut = creator.connectTimeOut;
        readTimeOut = creator.readTimeOut;
        target = creator.target;
        loadingDrawable = creator.loadingDrawable;
        errorDrawable = creator.errorDrawable;
        options = creator.options;
        transformations = Utils.immutableList(creator.transformations);
        fade = creator.fade;
        callback = creator.callback;
    }

    public Uri uri() {
        return uri;
    }

    public String stableKey() {
        return stableKey;
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

    public Options options() {
        return options;
    }

    public List<Transformation> transformations() {
        return transformations;
    }

    public Task.Creator newCreator() {
        return new Creator(this);
    }

    void onPreExecute() {
        target.onPrepare(loadingDrawable);
    }

    void onPostResult(Result result, Exception cause) {
        if (result != null) {
            Bitmap bitmap = result.bitmap();
            target.onLoaded(new VanGoghDrawable(vanGogh.resources(), bitmap, fade), result.from());
            callback.onSuccess(bitmap);
        } else if (cause != null) {
            target.onFailed(errorDrawable, cause);
            callback.onError(cause);
        }
    }

    @Override
    public String toString() {
        return "Task{" +
                "vanGogh=" + vanGogh +
                ", uri=" + uri +
                ", stableKey='" + stableKey + '\'' +
                ", fromPolicy=" + fromPolicy +
                ", connectTimeOut=" + connectTimeOut +
                ", readTimeOut=" + readTimeOut +
                ", target=" + target +
                ", loadingDrawable=" + loadingDrawable +
                ", errorDrawable=" + errorDrawable +
                ", options=" + options +
                ", transformations=" + transformations +
                ", fade=" + fade +
                ", callback=" + callback +
                '}';
    }

    public static class Options implements Cloneable {
        private Bitmap.Config config = Bitmap.Config.ARGB_8888;
        private int reqWidth = 0;
        private int reqHeight = 0;
        private float rotationDegrees;
        private boolean hasRotation;
        private float rotationPivotX;
        private float rotationPivotY;
        private boolean hasRotationPivot;
        private int maxWidth = 0;
        private int maxHeight = 0;

        public Options() {

        }

        public Bitmap.Config config() {
            return config;
        }

        public void config(Bitmap.Config config) {
            if (config == null) {
                throw new NullPointerException("config == null");
            }
            this.config = config;
        }

        public boolean hasSize() {
            return reqWidth > 0 && reqHeight > 0;
        }

        public void resize(int width, int height) {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("width <= 0 || height <= 0");
            }
            this.reqWidth = width;
            this.reqHeight = height;
        }

        public int reqWidth() {
            return reqWidth;
        }

        public int reqHeight() {
            return reqHeight;
        }

        public boolean hasMaxSize() {
            return maxWidth > 0 && maxHeight > 0;
        }

        public void maxSize(int maxWidth, int maxHeight) {
            if (maxWidth <= 0 || maxHeight <= 0) {
                throw new IllegalArgumentException("maxWidth <= 0 || maxHeight <= 0");
            }
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
        }

        public void clearMaxSize() {
            this.maxWidth = 0;
            this.maxHeight = 0;
        }

        public int maxWidth() {
            return maxWidth;
        }

        public int maxHeight() {
            return maxHeight;
        }

        public void rotate(float degrees, float pivotX, float pivotY) {
            rotate(degrees);
            rotationPivotX = pivotX;
            rotationPivotY = pivotY;
            hasRotationPivot = true;
        }

        public void rotate(float degrees) {
            rotationDegrees = degrees;
            hasRotation = true;
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

        public boolean hasRotation() {
            return hasRotation;
        }

        public boolean hasRotationPivot() {
            return hasRotationPivot;
        }

        @Override
        public String toString() {
            return "Options{" +
                    "config=" + config +
                    ", reqWidth=" + reqWidth +
                    ", reqHeight=" + reqHeight +
                    ", rotationDegrees=" + rotationDegrees +
                    ", hasRotation=" + hasRotation +
                    ", rotationPivotX=" + rotationPivotX +
                    ", rotationPivotY=" + rotationPivotY +
                    ", hasRotationPivot=" + hasRotationPivot +
                    ", maxWidth=" + maxWidth +
                    ", maxHeight=" + maxHeight +
                    '}';
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
    }

    public static class Creator {
        private final VanGogh vanGogh;

        private Uri uri;
        private String stableKey;
        private int fromPolicy;

        private int connectTimeOut;
        private int readTimeOut;

        private Target target = EmptyTarget.EMPTY;
        private Drawable loadingDrawable;
        private Drawable errorDrawable;

        private Options options;

        private List<Transformation> transformations;
        private boolean fade;
        private Callback callback = EmptyCallback.EMPTY;

        Creator(VanGogh vanGogh, Uri uri, String stableKey) {
            this.vanGogh = vanGogh;
            this.uri = uri;
            this.stableKey = stableKey;
            this.fromPolicy = vanGogh.defaultFromPolicy;
            this.connectTimeOut = vanGogh.connectTimeOut;
            this.readTimeOut = vanGogh.readTimeOut;
            this.loadingDrawable = vanGogh.defaultLoading;
            this.errorDrawable = vanGogh.defaultError;
            this.options = vanGogh.defaultOptions.clone();
            this.transformations = new ArrayList<>(vanGogh.transformations);
            this.fade = vanGogh.fade;
        }

        Creator(Task task) {
            this.vanGogh = task.vanGogh;
            this.uri = task.uri;
            this.stableKey = task.stableKey;
            this.fromPolicy = task.fromPolicy;
            this.connectTimeOut = task.connectTimeOut;
            this.readTimeOut = task.readTimeOut;
            this.target = task.target;
            this.loadingDrawable = task.loadingDrawable;
            this.errorDrawable = task.errorDrawable;
            this.options = task.options;
            this.transformations = new ArrayList<>(task.transformations);
            this.fade = task.fade;
            this.callback = task.callback;
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
         * The drawable to be used while the image is being loaded.
         */
        public Creator loading(@DrawableRes int loadingResId) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                loadingDrawable = vanGogh.resources().getDrawable(loadingResId, vanGogh.theme());
            } else {
                loadingDrawable = vanGogh.resources().getDrawable(loadingResId);
            }
            return this;
        }

        /**
         * The drawable to be used while the image is being loaded.
         */
        public Creator loading(Drawable loading) {
            if (loading == null) throw new NullPointerException("loading == null");
            loadingDrawable = loading;
            return this;
        }


        /**
         * The drawable to be used if the request image could not be loaded.
         */
        public Creator error(@DrawableRes int errorResId) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                errorDrawable = vanGogh.resources().getDrawable(errorResId, vanGogh.theme());
            } else {
                errorDrawable = vanGogh.resources().getDrawable(errorResId);
            }
            return this;
        }

        /**
         * The drawable to be used if the request image could not be loaded.
         */
        public Creator error(Drawable error) {
            if (error == null) {
                throw new NullPointerException("error == null");
            }
            errorDrawable = error;
            return this;
        }

        /**
         * Resize the image to the specified size in pixels.
         */
        public Creator resize(int width, int height) {
            options.resize(width, height);
            return this;
        }

        public Creator config(Bitmap.Config config) {
            options.config(config);
            return this;
        }

        /**
         * Rotate the image by the specified degrees.
         */
        public Creator rotate(float degrees) {
            options.rotate(degrees);
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
         * @param fade Enable or disable fade in of images loaded.
         */
        public Creator fade(boolean fade) {
            this.fade = fade;
            return this;
        }

        public Creator callback(Callback callback) {
            this.callback = (callback != null ? callback : EmptyCallback.EMPTY);
            return this;
        }

        public void into(ImageView view) {
            if (view == null) {
                throw new NullPointerException("view == null");
            }
            this.into(new ImageViewTarget(view, stableKey));
        }

        public void into(Target target) {
            if (target == null) {
                throw new NullPointerException("target == null");
            }
            this.target = target;
            quickFetchOrEnqueue();
        }

        public void fetch() {
            quickFetchOrEnqueue();
        }

        public void fetch(Callback callback) {
            this.callback = (callback != null ? callback : EmptyCallback.EMPTY);
            quickFetchOrEnqueue();
        }

        public Task create() {
            return new Task(this);
        }

        private void quickFetchOrEnqueue() {
            int policy = fromPolicy & From.MEMORY.policy;
            if (policy != 0 && transformations.isEmpty() && !vanGogh.debug) {
                Bitmap bitmap = vanGogh.checkMemoryCache(stableKey);
                if (bitmap != null) {
                    target.onLoaded(new BitmapDrawable(vanGogh.resources(), bitmap), From.MEMORY);
                    callback.onSuccess(bitmap);
                    return;
                }
            }
            vanGogh.enqueue(create());
        }
    }
}
