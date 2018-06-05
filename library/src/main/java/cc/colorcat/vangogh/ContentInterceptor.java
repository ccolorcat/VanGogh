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

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import java.io.IOException;

/**
 * Author: cxx
 * Date: 2018-06-05
 * GitHub: https://github.com/ccolorcat
 */
class ContentInterceptor implements Interceptor {
    private Context context;

    ContentInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Result intercept(Chain chain) throws IOException {
        Task task = chain.task();
        int fromPolicy = task.fromPolicy() & From.DISK.policy;
        if (fromPolicy != 0) {
            Uri uri = task.uri();
            if (uri == Uri.EMPTY) {
                throw new IOException("empty uri");
            }
            String scheme = uri.getScheme();
            if (ContentResolver.SCHEME_FILE.equals(scheme)
                    || ContentResolver.SCHEME_CONTENT.equals(scheme)
                    || ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
                return new Result(context.getContentResolver().openInputStream(uri), From.DISK);
            }
        }
        return chain.proceed(task);
    }
}
