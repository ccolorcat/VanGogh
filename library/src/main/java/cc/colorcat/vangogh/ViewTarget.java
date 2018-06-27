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

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * Author: cxx
 * Date: 2017-12-07
 * GitHub: https://github.com/ccolorcat
 */
public abstract class ViewTarget<V extends View> implements Target {
    private final Reference<? extends V> ref;
    private final int uniqueCode;

    protected ViewTarget(V view) {
        this.ref = new WeakReference<>(view);
        this.uniqueCode = view.hashCode();
    }

    @Override
    public void onPrepare(Drawable placeHolder) {
        setDrawable(placeHolder);
    }

    @Override
    public void onLoaded(@NonNull Drawable loaded, @NonNull From from) {
        setDrawable(loaded);
    }

    @Override
    public void onFailed(Drawable error, @NonNull Throwable cause) {
        setDrawable(error);
    }

    @Override
    public int uniqueCode() {
        return uniqueCode;
    }

    private void setDrawable(Drawable drawable) {
        V view = ref.get();
        if (view != null) {
            setDrawable(view, drawable);
        }
    }

    protected abstract void setDrawable(V view, Drawable drawable);
}
