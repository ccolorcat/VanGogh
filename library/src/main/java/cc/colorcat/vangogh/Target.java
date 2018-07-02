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

/**
 * Author: cxx
 * Date: 2017-07-10
 * GitHub: https://github.com/ccolorcat
 */
public interface Target {

    void onPrepare(Drawable placeholder);

    void onLoaded(@NonNull Drawable loaded, @NonNull From from);

    void onError(Drawable error, @NonNull Throwable cause);

    Object unique();
}
