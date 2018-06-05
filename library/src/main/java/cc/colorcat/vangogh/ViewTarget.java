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
import android.support.annotation.Nullable;
import android.view.View;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * Author: cxx
 * Date: 2017-12-07
 * GitHub: https://github.com/ccolorcat
 */
public abstract class ViewTarget<V extends View> implements Target {
    private static final int TAG_ID = R.string.app_name;

    private final Reference<? extends V> ref;
    private final Object tag;

    public ViewTarget(V view, Object tag) {
        view.setTag(TAG_ID, tag);
        this.ref = new WeakReference<>(view);
        this.tag = tag;
    }

    @Override
    public void onPrepare(@Nullable Drawable placeHolder) {
        setDrawableWithCheck(placeHolder);
    }

    @Override
    public void onLoaded(Drawable drawable, From from) {
        setDrawableWithCheck(drawable);
    }

    @Override
    public void onFailed(@Nullable Drawable error, Exception cause) {
        setDrawableWithCheck(error);
        LogUtils.e(cause);
    }

    private void setDrawableWithCheck(Drawable drawable) {
        V view = ref.get();
        if (view != null && checkTag(view)) {
            setDrawable(view, drawable);
        }
    }

    private boolean checkTag(V view) {
        Object obj = view.getTag(TAG_ID);
        return tag == obj || (tag != null && tag.equals(obj));
    }

    protected abstract void setDrawable(V view, Drawable drawable);
}
