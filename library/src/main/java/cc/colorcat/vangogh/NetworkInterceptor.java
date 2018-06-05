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

import java.io.IOException;

/**
 * Author: cxx
 * Date: 2017-07-11
 * GitHub: https://github.com/ccolorcat
 */
class NetworkInterceptor implements Interceptor {
    private static final String HTTP = "http";
    private static final String HTTPS = "https";

    NetworkInterceptor() {
    }

    @Override
    public Result intercept(Chain chain) throws IOException {
        Task task = chain.task();
        int fromPolicy = task.fromPolicy() & From.NETWORK.policy;
        String scheme = task.uri().getScheme();
        if (fromPolicy != 0 && (HTTP.equalsIgnoreCase(scheme) || HTTPS.equalsIgnoreCase(scheme))) {
            Downloader downloader = chain.loader();
            Result result = downloader.load(task);
            if (result != null) {
                return result;
            }
        }
        return chain.proceed(task);
    }
}
