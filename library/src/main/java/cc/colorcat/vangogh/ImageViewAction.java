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
import android.widget.ImageView;

/**
 * Author: cxx
 * Date: 2018-06-12
 * GitHub: https://github.com/ccolorcat
 */
class ImageViewAction extends Action<ImageView> {

    ImageViewAction(Creator creator, ImageView target) {
        super(creator, target);
    }

    @Override
    void prepare() {
        ImageView target = this.target();
        if (target != null) {
            target.setImageDrawable(loading);
        }
    }

    @Override
    void complete(Bitmap result, From from) {
        ImageView target = target();
        if (target != null) {
            target.setImageDrawable(new VanGoghDrawable(target.getResources(), result, fade, debugColor, from));
            callback.onSuccess(result);
        }
    }

    @Override
    void error(Exception e) {
        ImageView target = this.target();
        if (target != null) {
            target.setImageDrawable(error);
            callback.onError(e);
        }
    }
}
