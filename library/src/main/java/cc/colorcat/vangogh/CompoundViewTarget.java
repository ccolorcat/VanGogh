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
import android.widget.TextView;

/**
 * Author: cxx
 * Date: 2018-06-29
 * GitHub: https://github.com/ccolorcat
 */
public final class CompoundViewTarget<V extends TextView> extends ViewTarget<V> {
    public static final int COMPOUND_START = 1;
    public static final int COMPOUND_TOP = 1 << 1;
    public static final int COMPOUND_END = 1 << 2;
    public static final int COMPOUND_BOTTOM = 1 << 3;
    public static final int COMPOUND_ALL = COMPOUND_START | COMPOUND_TOP | COMPOUND_END | COMPOUND_BOTTOM;

    public static <V extends TextView> CompoundViewTarget<V> createDrawStart(V v) {
        return create(v, COMPOUND_START);
    }

    public static <V extends TextView> CompoundViewTarget<V> createDrawTop(V v) {
        return create(v, COMPOUND_TOP);
    }

    public static <V extends TextView> CompoundViewTarget<V> createDrawEnd(V v) {
        return create(v, COMPOUND_END);
    }

    public static <V extends TextView> CompoundViewTarget<V> createDrawBottom(V v) {
        return create(v, COMPOUND_BOTTOM);
    }

    public static <V extends TextView> CompoundViewTarget<V> create(V v, int compound) {
        if (v == null) {
            throw new NullPointerException("v == null");
        }
        if ((compound & COMPOUND_ALL) == 0) {
            throw new IllegalArgumentException("illegal compound == " + compound);
        }
        return new CompoundViewTarget<>(v, compound);
    }

    private final int compound;

    private CompoundViewTarget(V v, int compound) {
        super(v);
        this.compound = compound;
    }

    @Override
    protected void setDrawable(V view, Drawable drawable) {
        Drawable[] drawables = view.getCompoundDrawables();
        for (int i = 0; i < 4; ++i) {
            if (((compound >> i) & 1) != 0) {
                drawables[i] = drawable;
            }
        }
        view.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3]);
    }
}
