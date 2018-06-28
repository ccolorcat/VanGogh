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
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: cxx
 * Date: 2018-06-11
 * GitHub: https://github.com/ccolorcat
 */
public final class Creator {
    final VanGogh vanGogh;
    final Uri uri;
    final String stableKey;
    int fromPolicy;
    int connectTimeOut;
    int readTimeOut;
    boolean fade;
    boolean indicatorEnabled;
    List<Transformation> transformations;
    Drawable placeholder;
    Drawable error;
    Task.Options options;
    Object tag;
    Callback callback;

    Target target;
    String key;

    Creator(VanGogh vanGogh, Uri uri, String stableKey) {
        this.vanGogh = vanGogh;
        this.uri = uri;
        this.stableKey = stableKey;
        this.fromPolicy = vanGogh.fromPolicy;
        this.connectTimeOut = vanGogh.connectTimeOut;
        this.readTimeOut = vanGogh.readTimeOut;
        this.fade = vanGogh.fade;
        this.indicatorEnabled = vanGogh.indicatorEnabled;
        this.transformations = new ArrayList<>(vanGogh.transformations);
        this.placeholder = vanGogh.placeholder;
        this.error = vanGogh.error;
        this.options = vanGogh.options.clone();
        this.tag = stableKey;
        this.callback = EmptyCallback.INSTANCE;
    }

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
            throw new IllegalArgumentException("transformation == null");
        }
        if (!this.transformations.contains(transformation)) {
            this.transformations.add(transformation);
        }
        return this;
    }

    public Creator clearTransformation() {
        this.transformations.clear();
        return this;
    }

    /**
     * The drawable to be used while the image is being loaded.
     */
    public Creator placeholder(@DrawableRes int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.placeholder = vanGogh.context.getDrawable(resId);
        } else {
            this.placeholder = vanGogh.context.getResources().getDrawable(resId);
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
    public Creator error(@DrawableRes int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.error = vanGogh.context.getDrawable(resId);
        } else {
            this.error = vanGogh.context.getResources().getDrawable(resId);
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

    public Creator config(@NonNull Bitmap.Config config) {
        this.options.config(config);
        return this;
    }

    public Creator maxSizeWithDP(int maxWidth, int maxHeight) {
        this.options.maxSize(toPx(maxWidth), toPx(maxHeight));
        return this;
    }

    public Creator maxSize(int maxWidth, int maxHeight) {
        this.options.maxSize(maxWidth, maxHeight);
        return this;
    }

    public Creator clearMaxSize() {
        this.options.clearMaxSize();
        return this;
    }

    public Creator resizeWithDP(int width, int height) {
        this.options.resize(toPx(width), toPx(height));
        return this;
    }

    public Creator resize(int width, int height) {
        this.options.resize(width, height);
        return this;
    }

    public Creator clearResize() {
        this.options.clearResize();
        return this;
    }

    public Creator centerInside() {
        this.options.centerInside();
        return this;
    }

    public Creator centerCrop() {
        this.options.centerCrop();
        return this;
    }

    public Creator fitXY() {
        this.options.fitXY();
        return this;
    }

    public Creator rotate(float degrees, float pivotX, float pivotY) {
        this.options.rotate(degrees, pivotX, pivotY);
        return this;
    }

    public Creator rotate(float degrees) {
        this.options.rotate(degrees);
        return this;
    }

    public Creator tag(Object tag) {
        if (tag == null) {
            throw new IllegalArgumentException("tag == null");
        }
        this.tag = tag;
        return this;
    }

    public Creator callback(Callback callback) {
        this.callback = callback != null ? callback : EmptyCallback.INSTANCE;
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
            vanGogh.cancelExistingAction(this.target.unique());
            Throwable cause = new UnsupportedOperationException("unsupported uri: " + uri);
            this.target.onFailed(error, cause);
            this.callback.onError(cause);
            LogUtils.e(cause);
            return;
        }

        this.key = Utils.createKey(this);
        if ((fromPolicy & From.MEMORY.policy) != 0) {
            Bitmap bitmap = vanGogh.obtainFromMemoryCache(this.key);
            if (bitmap != null) {
                vanGogh.cancelExistingAction(this.target.unique());
                Drawable drawable = new VanGoghDrawable(vanGogh.context, bitmap, From.MEMORY, false, indicatorEnabled);
                this.target.onLoaded(drawable, From.MEMORY);
                this.callback.onSuccess(bitmap);
                return;
            }
        }
        Action action = new Action(this);
        if (enqueue) {
            vanGogh.enqueueAndSubmit(action);
        } else {
            vanGogh.submit(action);
        }
    }

    private int toPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                vanGogh.context.getResources().getDisplayMetrics()
        );
    }
}
