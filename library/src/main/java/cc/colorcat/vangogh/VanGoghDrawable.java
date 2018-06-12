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

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

/**
 * Author: cxx
 * Date: 2017-08-08
 * GitHub: https://github.com/ccolorcat
 */
public class VanGoghDrawable extends BitmapDrawable {
    private final boolean fade;
    private final boolean debugColor;
    private final From from;
    private int maxAlpha = 0xFF;
    private int alpha = 0; // [0, maxAlpha]

    public VanGoghDrawable(Resources res, Bitmap bitmap) {
        this(res, bitmap, true);
    }

    public VanGoghDrawable(Resources res, Bitmap bitmap, boolean animating) {
        this(res, bitmap, animating, false, From.MEMORY);
    }

    public VanGoghDrawable(Resources res, Bitmap bitmap, boolean fade, boolean debugColor, From from) {
        super(res, bitmap);
        this.fade = fade;
        this.debugColor = debugColor;
        this.from = from;
    }

    @Override
    public void draw(Canvas canvas) {
        if (fade && alpha < maxAlpha) {
            alpha += 10;
            super.setAlpha(Math.min(alpha, maxAlpha));
        }
        super.draw(canvas);
    }

    @Override
    public void setAlpha(int alpha) {
        super.setAlpha(alpha);
        if (alpha > 0xFF) {
            maxAlpha = 0xFF;
        } else if (alpha >= 0) {
            maxAlpha = alpha;
        }
    }
}
