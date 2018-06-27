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
 * Date: 2017-07-11
 * GitHub: https://github.com/ccolorcat
 */
class StableKeyMemoryCacheInterceptor implements Interceptor {
    private Cache<Bitmap> memoryCache;

    StableKeyMemoryCacheInterceptor(Cache<Bitmap> cache) {
        this.memoryCache = cache;
    }

    @Override
    public Result intercept(Chain chain) throws IOException {
        Task task = chain.task();
        int fromPolicy = task.fromPolicy() & From.MEMORY.policy;
        if (fromPolicy != 0) {
            Bitmap bitmap = memoryCache.get(task.stableKey());
            if (bitmap != null) {
                return new Result(bitmap, From.MEMORY);
            }
        }
        Result result = chain.proceed(task);
        Task.Options ops = task.options();
        if (!ops.hasResize() && !ops.hasRotation()) {
            memoryCache.save(task.stableKey(), result.bitmap());
        }
        return result;
    }
}
