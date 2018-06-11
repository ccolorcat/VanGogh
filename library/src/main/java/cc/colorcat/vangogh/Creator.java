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

/**
 * Author: cxx
 * Date: 2018-06-11
 * GitHub: https://github.com/ccolorcat
 */
public final class Creator {
    private final VanGogh vanGogh;
    private final Task.Creator creator;

    private Target target;
    private Drawable loadingDrawable;
    private Drawable errorDrawable;

    private boolean fade;
    private Callback callback;

    Creator(VanGogh vanGogh, Uri uri, String stableKey) {
        this.vanGogh = vanGogh;
        this.creator = new Task.Creator(vanGogh, uri, stableKey);
        this.target = EmptyTarget.EMPTY;
        this.loadingDrawable = vanGogh.defaultLoading;
        this.errorDrawable = vanGogh.defaultError;
        this.fade = vanGogh.fade;
        this.callback = EmptyCallback.EMPTY;
    }

    public Creator from(int fromPolicy) {
        creator.from(fromPolicy);
        return this;
    }

    public Creator connectTimeOut(int timeOut) {
        creator.connectTimeOut(timeOut);
        return this;
    }

    public Creator readTimeOut(int timeOut) {
        creator.readTimeOut(timeOut);
        return this;
    }

    /**
     * The drawable to be used while the image is being loaded.
     */
    public Creator loading(@DrawableRes int loadingResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.loadingDrawable = vanGogh.context.getDrawable(loadingResId);
        } else {
            this.loadingDrawable = vanGogh.resources().getDrawable(loadingResId);
        }
        return this;
    }

    /**
     * The drawable to be used while the image is being loaded.
     */
    public Creator loading(Drawable loading) {
        this.loadingDrawable = loading;
        return this;
    }

    /**
     * The drawable to be used if the request image could not be loaded.
     */
    public Creator error(@DrawableRes int errorResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.errorDrawable = vanGogh.context.getDrawable(errorResId);
        } else {
            this.errorDrawable = vanGogh.resources().getDrawable(errorResId);
        }
        return this;
    }

    /**
     * The drawable to be used if the request image could not be loaded.
     */
    public Creator error(Drawable error) {
        this.errorDrawable = error;
        return this;
    }

    /**
     * Resize the image to the specified size in pixels.
     */
    public Creator resize(int width, int height) {
        creator.resize(width, height);
        return this;
    }

    public Creator config(Bitmap.Config config) {
        creator.config(config);
        return this;
    }

    /**
     * Rotate the image by the specified degrees.
     */
    public Creator rotate(float degrees) {
        creator.rotate(degrees);
        return this;
    }

    /**
     * Rotate the image by the specified degrees around a pivot point.
     */
    public Creator rotate(float degrees, float pivotX, float pivotY) {
        creator.rotate(degrees, pivotX, pivotY);
        return this;
    }

    /**
     * Resize the image to less than the specified size in pixels.
     */
    public Creator maxSize(int maxWidth, int maxHeight) {
        creator.maxSize(maxWidth, maxHeight);
        return this;
    }

    public Creator clearMaxSize() {
        creator.clearMaxSize();
        return this;
    }

    public Creator addTransformation(Transformation transformation) {
        creator.addTransformation(transformation);
        return this;
    }

    public Creator clearTransformation() {
        creator.clearTransformation();
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

    public void into(Target target) {

    }
}
