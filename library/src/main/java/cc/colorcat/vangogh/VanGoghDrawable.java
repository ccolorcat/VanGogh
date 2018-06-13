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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;

/**
 * Author: cxx
 * Date: 2017-08-08
 * GitHub: https://github.com/ccolorcat
 */
public class VanGoghDrawable extends BitmapDrawable {
    private static final Paint DEBUG_PAINT;

    private final boolean fade;
    private final boolean debugColor;
    private final From from;
    private float density;

    private int maxAlpha = 0xFF;
    private int alpha = 0; // [0, maxAlpha]

    static {
        DEBUG_PAINT = new Paint();
        DEBUG_PAINT.setStyle(Paint.Style.FILL);
    }

    VanGoghDrawable(VanGogh vanGogh, Bitmap bitmap, boolean fade, boolean debugColor, From from) {
        this(vanGogh.resources(), bitmap, fade, debugColor, from, vanGogh.context);
    }

    VanGoghDrawable(Resources res, Bitmap bitmap, boolean fade, boolean debugColor, From from, Context context) {
        super(res, bitmap);
        this.fade = fade;
        this.debugColor = debugColor;
        this.from = from;
        this.density = context.getResources().getDisplayMetrics().density;
    }

    @Override
    public void draw(Canvas canvas) {
        if (fade && alpha < maxAlpha) {
            alpha += 10;
            super.setAlpha(Math.min(alpha, maxAlpha));
            super.draw(canvas);
        } else {
            super.draw(canvas);
            if (debugColor) {
                drawDebugColor(canvas);
            }
        }
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

    private void drawDebugColor(Canvas canvas) {
        DEBUG_PAINT.setColor(Color.WHITE);
        Path path = getTrianglePath(0, 0, (int) (16 * density));
        canvas.drawPath(path, DEBUG_PAINT);

        DEBUG_PAINT.setColor(from.debugColor);
        path = getTrianglePath(0, 0, (int) (15 * density));
        canvas.drawPath(path, DEBUG_PAINT);
    }

    private static Path getTrianglePath(int startX, int startY, int width) {
        Path path = new Path();
        path.moveTo(startX, startY);
        path.lineTo(startX + width, startY);
        path.lineTo(startX, startY + width);
        return path;
    }
}
