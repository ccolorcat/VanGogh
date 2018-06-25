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
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: cxx
 * Date: 2018-06-11
 * GitHub: https://github.com/ccolorcat
 */
public final class Creator {
    private final VanGogh vanGogh;
    final Uri uri;
    final String stableKey;
    int fromPolicy;
    int connectTimeOut;
    int readTimeOut;
    Task.Options options;
    List<Transformation> transformations;
    Drawable loading;
    Drawable error;
    boolean fade;
    boolean indicatorEnabled;
    Object tag;

    Creator(VanGogh vanGogh, Uri uri, String stableKey) {
        this.vanGogh = vanGogh;
        this.uri = uri;
        this.stableKey = stableKey;
        this.fromPolicy = vanGogh.defaultFromPolicy;
        this.connectTimeOut = vanGogh.connectTimeOut;
        this.readTimeOut = vanGogh.readTimeOut;
        this.options = vanGogh.defaultOptions.clone();
        this.transformations = new ArrayList<>(vanGogh.transformations);
        this.loading = vanGogh.defaultLoading;
        this.error = vanGogh.defaultError;
        this.fade = vanGogh.fade;
        this.indicatorEnabled = vanGogh.indicatorEnabled;
        this.tag = stableKey;
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

    public Creator config(@NonNull Bitmap.Config config) {
        this.options.config(config);
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
    public Creator loading(@DrawableRes int loadingResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.loading = vanGogh.context.getDrawable(loadingResId);
        } else {
            this.loading = vanGogh.context.getResources().getDrawable(loadingResId);
        }
        return this;
    }

    /**
     * The drawable to be used while the image is being loaded.
     */
    public Creator loading(Drawable loading) {
        this.loading = loading;
        return this;
    }

    /**
     * The drawable to be used if the request image could not be loaded.
     */
    public Creator error(@DrawableRes int errorResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.error = vanGogh.context.getDrawable(errorResId);
        } else {
            this.error = vanGogh.context.getResources().getDrawable(errorResId);
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

    /**
     * @param fade Enable or disable fade in of images loaded.
     */
    public Creator fade(boolean fade) {
        this.fade = fade;
        return this;
    }

    public Creator enableIndicator(boolean enabled) {
        this.indicatorEnabled = enabled;
        return this;
    }

    public Creator tag(Object tag) {
        if (tag == null) {
            throw new IllegalArgumentException("tag == null");
        }
        this.tag = tag;
        return this;
    }

    public void fetch(Callback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback == null");
        }
        into(new FetchAction(this, callback), false);
    }

    public void into(ImageView target) {
        into(target, null);
    }

    public void into(ImageView target, Callback callback) {
        if (target == null) {
            throw new IllegalArgumentException("target == null");
        }
        into(new ImageViewAction(this, target, callback), true);
    }

    public void into(Target target) {
        into(target, null);
    }

    public void into(Target target, Callback callback) {
        if (target == null) {
            throw new IllegalArgumentException("target == null");
        }
        into(new TargetAction(this, target, vanGogh.context, callback), true);
    }

    private void into(Action action, boolean enqueue) {
        Utils.checkMain();
        if (uri == Uri.EMPTY) {
            vanGogh.cancelExistingAction(action.target());
            action.error(new UnsupportedOperationException("unsupported uri: " + uri));
            return;
        }
        if ((fromPolicy & From.MEMORY.policy) != 0) {
            Bitmap bitmap = vanGogh.obtainFromMemoryCache(action.key);
            if (bitmap != null) {
                vanGogh.cancelExistingAction(action.target());
                action.complete(bitmap, From.MEMORY);
                return;
            }
        }
        if (enqueue) {
            vanGogh.enqueueAndSubmit(action);
        } else {
            vanGogh.submit(action);
        }
    }
}
