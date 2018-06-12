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
    final Task task;
    final Drawable loading;
    final Drawable error;
    final boolean fade;
    final boolean debugColor;
    final Callback callback;

    private boolean canceled = false;

    Action(Creator creator, T target) {
        this.target = new WeakReference<>(target);
        this.task = new Task(creator);
        this.loading = creator.loading;
        this.error = creator.error;
        this.fade = creator.fade;
        this.debugColor = creator.debugColor;
        this.callback = creator.callback;
    }

    T target() {
        return target.get();
    }

    String taskKey() {
        return task.taskKey();
    }

    void cancel() {
        canceled = true;
    }

    boolean isCanceled() {
        return canceled;
    }

    abstract void prepare();

    abstract void complete(Bitmap result, From from);

    abstract void error(Exception e);
}
