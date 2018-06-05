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

import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Author: cxx
 * Date: 2017-08-29
 * GitHub: https://github.com/ccolorcat
 */
public class FileInterceptor implements Interceptor {

    @Override
    public Result intercept(Chain chain) throws IOException {
        Task task = chain.task();
        int fromPolicy = task.fromPolicy() & From.DISK.policy;
        Uri uri = task.uri();
        if (fromPolicy != 0 && "file".equals(uri.getScheme())) {
            File file = new File(uri.getPath());
            long length = file.length();
            return new Result(new FileInputStream(file), length, From.DISK);
        }
        return chain.proceed(task);
    }
}
