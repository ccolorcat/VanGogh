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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.support.annotation.ColorInt;

/**
 * Author: cxx
 * Date: 2018-03-06
 * GitHub: https://github.com/ccolorcat
 */
public class CornerTransformation extends BaseTransformation {
    public static final int TYPE_TL = 1;
    public static final int TYPE_TR = 1 << 1;
    public static final int TYPE_BR = 1 << 2;
    public static final int TYPE_BL = 1 << 3;
    public static final int TYPE_ALL = TYPE_TL | TYPE_TR | TYPE_BR | TYPE_BL;

    private static final int[] TYPE_ARRAY = {TYPE_TL, TYPE_TR, TYPE_BR, TYPE_BL};

    private int type = TYPE_ALL;
    private float[] cornerRadius;
    private final float borderWidth;
    @ColorInt
    private final int borderColor;
    private final boolean hasBorder;

    public static CornerTransformation create(int type) {
        return create(type, 0F, Color.WHITE);
    }

    public static CornerTransformation create(int type, float borderWidth, @ColorInt int borderColor) {
        return new CornerTransformation(null, borderWidth, borderColor, type);
    }

    public static CornerTransformation create(float[] cornerRadius) {
        return create(cornerRadius, 0F, Color.WHITE);
    }

    public static CornerTransformation create(float[] cornerRadius, float borderWidth, @ColorInt int borderColor) {
        if (cornerRadius.length != 8) {
            throw new IllegalArgumentException("cornerRadius.length != 8");
        }
        return new CornerTransformation(cornerRadius, borderWidth, borderColor, 0);
    }

    protected CornerTransformation(float[] cornerRadius, float borderWidth, @ColorInt int borderColor, int type) {
        this.cornerRadius = cornerRadius;
        this.borderWidth = borderWidth;
        this.borderColor = borderColor;
        this.hasBorder = borderWidth > 0F;
        this.type = type;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        final int width = source.getWidth(), height = source.getHeight();
        Bitmap.Config config = source.getConfig();
        if (config == null || config == Bitmap.Config.RGB_565) {
            config = Bitmap.Config.ARGB_8888;
        }
        final Bitmap out = Bitmap.createBitmap(width, height, config);
        final Canvas canvas = new Canvas(out);
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(borderColor);
        final Path path = new Path();
        final RectF rectF = new RectF(0F, 0F, width, height);
        final float[] radii = makeRadii(Math.min(width, height));
        path.addRoundRect(rectF, radii, Path.Direction.CW);
        canvas.drawPath(path, paint);
        final Xfermode back = paint.getXfermode();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, 0F, 0F, paint);
        if (hasBorder) {
            paint.setXfermode(back);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(borderWidth);
            canvas.drawPath(path, paint);
        }
        return out;
    }

    private float[] makeRadii(int side) {
        if (cornerRadius == null) {
            cornerRadius = new float[8];
            final int calculated = side >>> 3;
            for (int i = 0, length = TYPE_ARRAY.length; i < length; ++i) {
                int index = i << 1;
                float cr = (TYPE_ARRAY[i] & type) != 0 ? calculated : 0F;
                cornerRadius[index] = cr;
                cornerRadius[index + 1] = cr;
            }
        }
        return cornerRadius;
    }
}
