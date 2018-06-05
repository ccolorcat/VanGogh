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
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

/**
 * Author: cxx
 * Date: 2017-08-08
 * GitHub: https://github.com/ccolorcat
 */
public class OvalTransformation implements Transformation {

    @Override
    public Bitmap transform(Bitmap source) {
        final int width = source.getWidth(), height = source.getHeight();
        Bitmap.Config config = source.getConfig();
        if (config == null || config == Bitmap.Config.RGB_565) {
            config = Bitmap.Config.ARGB_8888;
        }
        Bitmap out = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(out);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        RectF rf = new RectF(0, 0, width, height);
        canvas.drawRoundRect(rf, width >> 1, height >> 1, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, 0, 0, paint);
        return out;
    }
}
