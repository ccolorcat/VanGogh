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
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: cxx
 * Date: 2017-07-11
 * GitHub: https://github.com/ccolorcat
 */
@SuppressWarnings("unused")
public final class Task {
    private final Uri uri;
    private final String stableKey;
    private final String key;
    private final int fromPolicy;
    private final int connectTimeOut;
    private final int readTimeOut;
    private final Options options;
    private final List<Transformation> transformations;

    private Task(Creator creator) {
        uri = creator.uri;
        stableKey = creator.stableKey;
        key = creator.key;
        fromPolicy = creator.fromPolicy;
        connectTimeOut = creator.connectTimeOut;
        readTimeOut = creator.readTimeOut;
        options = creator.options;
        transformations = Utils.immutableList(creator.transformations);
    }

    public Uri uri() {
        return uri;
    }

    public String stableKey() {
        return stableKey;
    }

    public String key() {
        return key;
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
            tryClearSize();
            scaleType = SCALE_TYPE_NO;
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
    }


    public final static class Creator {
        Uri uri;
        String stableKey;
        int fromPolicy;
        int connectTimeOut;
        int readTimeOut;
        Options options;
        List<Transformation> transformations;
        String key;

        Creator(VanGogh vanGogh, Uri uri, String stableKey) {
            this.uri = uri;
            this.stableKey = stableKey;
            this.fromPolicy = vanGogh.defaultFromPolicy;
            this.connectTimeOut = vanGogh.connectTimeOut;
            this.readTimeOut = vanGogh.readTimeOut;
            this.options = vanGogh.defaultOptions.clone();
            this.transformations = new ArrayList<>(vanGogh.transformations);
        }

        Creator(Task task) {
            this.uri = task.uri;
            this.stableKey = task.stableKey;
            this.fromPolicy = task.fromPolicy;
            this.connectTimeOut = task.connectTimeOut;
            this.readTimeOut = task.readTimeOut;
            this.options = task.options;
            this.transformations = new ArrayList<>(task.transformations);
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
            connectTimeOut = timeOut;
            return this;
        }

        public Creator readTimeOut(int timeOut) {
            if (timeOut < 0) {
                throw new IllegalArgumentException("timeOut < 0");
            }
            readTimeOut = timeOut;
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
            options.clearMaxSize();
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

        public Task create() {
            this.key = buildKey();
            return new Task(this);
        }

        private String buildKey() {
            StringBuilder builder = new StringBuilder(stableKey);
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
            for (int i = 0, size = transformations.size(); i < size; ++i) {
                builder.append('|').append(transformations.get(i).getKey());
            }
            return builder.toString();
        }
    }
}
