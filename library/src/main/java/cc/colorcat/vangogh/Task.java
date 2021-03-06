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

    Task(Creator creator) {
        this.uri = creator.uri;
        this.stableKey = creator.stableKey;
        this.key = creator.key;
        this.fromPolicy = creator.fromPolicy;
        this.connectTimeOut = creator.connectTimeOut;
        this.readTimeOut = creator.readTimeOut;
        this.options = creator.options;
        this.transformations = Utils.immutableList(creator.transformations);
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
    }
}
