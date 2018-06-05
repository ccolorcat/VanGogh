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

import android.content.res.Resources;
import android.net.Uri;

import java.io.IOException;

/**
 * Author: cxx
 * Date: 2017-08-29
 * GitHub: https://github.com/ccolorcat
 */
class ResourcesInterceptor implements Interceptor {
    private Resources resources;

    ResourcesInterceptor(Resources resources) {
        this.resources = resources;
    }

    @Override
    public Result intercept(Chain chain) throws IOException {
        Task task = chain.task();
        int fromPolicy = task.fromPolicy() & From.DISK.policy;
        if (fromPolicy != 0) {
            Uri uri = task.uri();
            if (Utils.SCHEME_VANGOGH.equals(uri.getScheme()) && Utils.HOST_RESOURCE.equals(uri.getHost())) {
                int resId = Integer.parseInt(uri.getQueryParameter("id"));
                return new Result(resources.openRawResource(resId), From.DISK);
            }
        }
        return chain.proceed(task);
    }
}
