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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author: cxx
 * Date: 2017-07-11
 * GitHub: https://github.com/ccolorcat
 */
class RealCall implements Runnable {
    private final VanGogh vanGogh;
    private final Task task;
    private AtomicInteger count = new AtomicInteger(0);

    RealCall(VanGogh vanGogh, Task task) {
        this.vanGogh = vanGogh;
        this.task = task;
    }

    public int getCount() {
        return count.get();
    }

    public int getAndIncrement() {
        return count.getAndIncrement();
    }

    public Task task() {
        return task;
    }

    public Result execute() throws IOException {
        return getResultWithInterceptor();
    }

    @Override
    public void run() {
        
    }

    private Result getResultWithInterceptor() throws IOException {
        List<Interceptor> users = vanGogh.interceptors;
        List<Interceptor> interceptors = new ArrayList<>(users.size() + 7);
        interceptors.addAll(users);
        if (vanGogh.indicatorEnabled) {
            interceptors.add(new WatermarkInterceptor());
        }
        interceptors.add(new TransformInterceptor());
        interceptors.add(new MemoryCacheInterceptor(vanGogh.memoryCache));
        interceptors.add(new StreamInterceptor());
        interceptors.add(new ContentInterceptor(vanGogh.context));
        if (vanGogh.diskCache != null) {
            interceptors.add(new DiskCacheInterceptor(vanGogh.diskCache));
        }
        interceptors.add(new NetworkInterceptor());
        Interceptor.Chain chain = new RealInterceptorChain(interceptors, 0, task, vanGogh.downloader.clone());
        return chain.proceed(task);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RealCall realCall = (RealCall) o;

        if (task.fromPolicy() != realCall.task.fromPolicy()) return false;
        return task.stableKey().equals(realCall.task.stableKey());
    }

    @Override
    public int hashCode() {
        int result = task.stableKey().hashCode();
        result = 31 * result + task.fromPolicy();
        return result;
    }

    @Override
    public String toString() {
        return "RealCall{" +
                "vanGogh=" + vanGogh +
                ", task=" + task +
                ", count=" + count +
                '}';
    }
}
