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

/**
 * Author: cxx
 * Date: 2018-06-11
 * GitHub: https://github.com/ccolorcat
 */
public final class Creator {
    private final VanGogh vanGogh;
    private final Task.Creator taskCreator;
    private Drawable loading;
    private Drawable error;
    private boolean fade;
    private boolean debug;

    Creator(VanGogh vanGogh, Uri uri, String stableKey) {
        this.vanGogh = vanGogh;
        this.taskCreator = new Task.Creator(vanGogh, uri, stableKey);
        this.loading = vanGogh.defaultLoading;
        this.error = vanGogh.defaultError;
        this.fade = vanGogh.fade;
        this.debug = vanGogh.debug;
    }

    public Creator from(int fromPolicy) {
        taskCreator.from(fromPolicy);
        return this;
    }

    public Creator connectTimeOut(int timeOut) {
        taskCreator.connectTimeOut(timeOut);
        return this;
    }

    public Creator readTimeOut(int timeOut) {
        taskCreator.readTimeOut(timeOut);
        return this;
    }

    public Creator config(@NonNull Bitmap.Config config) {
        taskCreator.config(config);
        return this;
    }

    public Creator maxSize(int maxWidth, int maxHeight) {
        taskCreator.maxSize(maxWidth, maxHeight);
        return this;
    }

    public Creator clearMaxSize() {
        taskCreator.clearMaxSize();
        return this;
    }

    public Creator resize(int width, int height) {
        taskCreator.resize(width, height);
        return this;
    }

    public Creator clearResize() {
        taskCreator.clearResize();
        return this;
    }

    public Creator centerInside() {
        taskCreator.centerInside();
        return this;
    }

    public Creator centerCrop() {
        taskCreator.centerCrop();
        return this;
    }

    public Creator fitXY() {
        taskCreator.fitXY();
        return this;
    }

    public Creator rotate(float degrees, float pivotX, float pivotY) {
        taskCreator.rotate(degrees, pivotX, pivotY);
        return this;
    }

    public Creator rotate(float degrees) {
        taskCreator.rotate(degrees);
        return this;
    }

    public Creator addTransformation(Transformation transformation) {
        taskCreator.addTransformation(transformation);
        return this;
    }

    public Creator clearTransformation() {
        taskCreator.clearTransformation();
        return this;
    }

    /**
     * The drawable to be used while the image is being loaded.
     */
    public Creator loading(@DrawableRes int loadingResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.loading = vanGogh.context.getDrawable(loadingResId);
        } else {
            this.loading = vanGogh.resources().getDrawable(loadingResId);
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
            this.error = vanGogh.resources().getDrawable(errorResId);
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

    public Creator debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public void into(ImageView target) {
        this.into(target, null);
    }

    public void into(ImageView target, Callback callback) {
        if (target == null) {
            throw new NullPointerException("target == null");
        }
        if (taskCreator.uri == Uri.EMPTY) {
            vanGogh.cancelExistingCall(target);
            target.setImageDrawable(error);
            return;
        }
        Task task = taskCreator.create();
        Action<ImageView> action = new ImageViewAction(target, loading, error, fade, debug, Utils.nullElse(callback, EmptyCallback.EMPTY));
        Bitmap bitmap = vanGogh.obtainFromMemoryCache(task.key());
        if (bitmap != null) {
            action.complete(bitmap, From.MEMORY);
            return;
        }
        vanGogh.enqueueAndSubmit(new RealCall(vanGogh, task, action));
    }

//    public void into(Target target) {
//
//    }
}
