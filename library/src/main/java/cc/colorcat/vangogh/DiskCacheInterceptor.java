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
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Author: cxx
 * Date: 2017-07-11
 * GitHub: https://github.com/ccolorcat
 */
class DiskCacheInterceptor implements Interceptor {
    private DiskCache diskCache;

    DiskCacheInterceptor(DiskCache cache) {
        this.diskCache = cache;
    }

    @Override
    public Result intercept(Chain chain) throws IOException {
        Task task = chain.task();
        Task.Options options = task.options();
        int fromPolicy = task.fromPolicy() & From.DISK.policy;
        if (fromPolicy != 0) {
            DiskCache.Snapshot snapshot = diskCache.getSnapshot(task.stableKey());
            Bitmap bitmap = decodeOrDelete(snapshot, options, false);
            if (bitmap != null) {
                return new Result(bitmap, From.DISK);
            }
        }

        Result result = chain.proceed(task);
        From resultFrom = result.from();
        if (resultFrom == From.NETWORK) {
            DiskCache.Snapshot snapshot = diskCache.getSnapshot(task.stableKey());
            OutputStream os = snapshot.getOutputStream();
            if (os != null) {
                InputStream is = result.stream();
                try {
                    Utils.dumpAndClose(is, os);
                    Bitmap bitmap = decodeOrDelete(snapshot, options, true);
                    result = new Result(bitmap, resultFrom);
                } catch (IOException e) {
                    snapshot.requireDelete();
                    throw e;
                }
            }
        }
        return result;
    }

    private static Bitmap decodeOrDelete(DiskCache.Snapshot snapshot, Task.Options ops, boolean canThrow) throws IOException {
        Bitmap result = null;
        InputStream is = snapshot.getInputStream();
        if (is != null) {
            if (ops.hasMaxSize()) {
                result = Utils.decodeStreamAndClose(is, ops);
            } else {
                result = Utils.decodeStreamAndClose(is);
            }
            if (result == null && !canThrow) {
                snapshot.requireDelete();
            }
        }
        if (result == null && canThrow) {
            throw new IOException("decode failed, snapshot = " + snapshot);
        }
        return result;
    }
}
