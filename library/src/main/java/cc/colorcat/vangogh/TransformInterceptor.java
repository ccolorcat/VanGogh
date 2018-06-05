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

import java.io.IOException;

/**
 * Author: cxx
 * Date: 2017-08-08
 * GitHub: https://github.com/ccolorcat
 */
class TransformInterceptor implements Interceptor {

    TransformInterceptor() {
    }

    @Override
    public Result intercept(Chain chain) throws IOException {
        Task task = chain.task();
        Result result = chain.proceed(task);
        Bitmap bitmap = Utils.transformResult(result.bitmap(), task.options(), task.transformations());
        return new Result(bitmap, result.from());
    }
}
