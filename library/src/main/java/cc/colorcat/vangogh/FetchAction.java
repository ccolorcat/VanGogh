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

/**
 * Author: cxx
 * Date: 2018-06-14
 * GitHub: https://github.com/ccolorcat
 */
class FetchAction extends Action<Object> {

    FetchAction(Creator creator, Callback callback) {
        super(creator, new Object(), callback);
    }

    @Override
    void prepare() {

    }

    @Override
    void complete(Bitmap result, From from) {
        callback.onSuccess(result);
    }

    @Override
    void error(Throwable cause) {
        callback.onError(cause);
    }
}
