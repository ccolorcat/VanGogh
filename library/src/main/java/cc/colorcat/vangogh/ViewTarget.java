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

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;

/**
 * Author: cxx
 * Date: 2017-12-07
 * GitHub: https://github.com/ccolorcat
 */
public abstract class ViewTarget<V extends View> extends WeakTarget<V> {
    public ViewTarget(V v) {
        super(v);
    }

    @Override
    public void onPrepare(Drawable placeholder) {
        setDrawable(placeholder, true);
    }

    @Override
    public void onLoaded(@NonNull Drawable loaded, @NonNull From from) {
        setDrawable(loaded, false);
    }

    @Override
    public void onError(Drawable error, @NonNull Throwable cause) {
        setDrawable(error, true);
    }

    private void setDrawable(Drawable drawable, boolean maybeAnimation) {
        V view = reference.get();
        if (view != null) {
            setDrawable(view, drawable);
            if (drawable != null && maybeAnimation && drawable instanceof AnimationDrawable) {
                ((AnimationDrawable) drawable).start();
            }
        }
    }

    protected abstract void setDrawable(V view, Drawable drawable);
}
