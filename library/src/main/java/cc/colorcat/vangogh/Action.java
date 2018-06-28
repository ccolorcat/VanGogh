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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Author: cxx
 * Date: 2018-06-11
 * GitHub: https://github.com/ccolorcat
 */
class Action {
    private final Context context;
    private final Target target;
    private final Drawable placeholder;
    private final Drawable error;
    private final boolean indicatorEnabled;
    private final boolean fade;
    private final Callback callback;
    final String key;
    final Task task;
    final Object tag;

    private boolean canceled;

    Action(Creator creator) {
        context = creator.vanGogh.context;
        target = creator.target;
        placeholder = creator.placeholder;
        error = creator.error;
        indicatorEnabled = creator.indicatorEnabled;
        fade = creator.fade;
        callback = creator.callback;
        key = creator.key;
        task = new Task(creator);
        tag = creator.tag;
        canceled = false;
    }

    @Nullable
    Object targetUnique() {
        return target.unique();
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

    void onSuccess(@NonNull Bitmap result, @NonNull From from) {
        Drawable drawable = new VanGoghDrawable(context, result, from, fade, indicatorEnabled);
        target.onLoaded(drawable, from);
        callback.onSuccess(result);
    }

    void onFailed(@NonNull Throwable cause) {
        target.onFailed(error, cause);
        callback.onError(cause);
    }
}
