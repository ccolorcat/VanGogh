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

import java.lang.ref.WeakReference;

/**
 * Author: cxx
 * Date: 2018-06-11
 * GitHub: https://github.com/ccolorcat
 */
abstract class Action<T> {
    private final WeakReference<T> target;
    protected final Drawable loadingDrawable;
    final Drawable errorDrawable;
    final boolean fade;
    final boolean debug;
    final Callback callback;

    Action(T target, Drawable loading, Drawable error, boolean fade, boolean debug, Callback callback) {
        this.target = new WeakReference<>(target);
        this.loadingDrawable = loading;
        this.errorDrawable = error;
        this.fade = fade;
        this.debug = debug;
        this.callback = callback;
    }

    T target() {
        return target.get();
    }

    abstract void prepare();

    abstract void complete(Bitmap result, From from);

    abstract void error(Exception e);
}
