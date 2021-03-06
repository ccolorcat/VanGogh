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
import java.util.List;

/**
 * Author: cxx
 * Date: 2017-07-11
 * GitHub: https://github.com/ccolorcat
 */
class RealInterceptorChain implements Interceptor.Chain {
    private final List<Interceptor> interceptors;
    private final int index;
    private final Task task;
    private final Downloader downloader;

    RealInterceptorChain(List<Interceptor> interceptors, int index, Task task, Downloader downloader) {
        this.interceptors = interceptors;
        this.index = index;
        this.task = task;
        this.downloader = downloader;
    }

    @Override
    public Downloader loader() {
        return downloader;
    }

    @Override
    public Task task() {
        return task;
    }

    @Override
    public Result proceed(Task task) throws IOException {
        RealInterceptorChain next = new RealInterceptorChain(interceptors, index + 1, task, downloader);
        Interceptor interceptor = interceptors.get(index);
        return interceptor.intercept(next);
    }
}
