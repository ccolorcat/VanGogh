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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author: cxx
 * Date: 2017-07-11
 * GitHub: https://github.com/ccolorcat
 */
class Call implements Runnable {
    private final VanGogh vanGogh;
    private final AtomicInteger count;

    final String key;
    final List<Task> tasks = new ArrayList<>(4);
    Future future;
    Result result;
    Bitmap bitmap;
    From from;
    Throwable cause;

    Call(VanGogh vanGogh, Task task) {
        this.vanGogh = vanGogh;
        this.count = new AtomicInteger(vanGogh.maxTry);
        this.key = task.key();
        this.tasks.add(task);
    }

    void attach(Task task) {
        tasks.add(task);
    }

    void detach(Task task) {
        tasks.remove(task);
    }

    boolean tryCancel() {
        return tasks.isEmpty()
                && future != null
                && future.cancel(false);
    }

    boolean isCanceled() {
        return future != null && future.isCancelled();
    }

    @Override
    public void run() {
        final Task task = tasks.get(0);
        try {
            Result result = getResultWithInterceptor(task);
            bitmap = result.bitmap();
            from = result.from();
        } catch (IOException e) {
            cause = e;
        } catch (IndexOutOfBoundsException e) {
            cause = new UnsupportedOperationException("unsupported uri: " + task.uri());
        } catch (OutOfMemoryError e) {
            cause = e;
        } catch (Exception e) {
            cause = e;
        } finally {
            if (bitmap != null) {
                vanGogh.dispatcher.dispatchSuccess(this);
            } else if (cause instanceof IOException && count.get() > 0) {
                vanGogh.dispatcher.dispatchRetry(this);
            } else {
                vanGogh.dispatcher.dispatchFailed(this);
            }
            if (cause != null) {
                LogUtils.e(cause);
            }
        }
    }

    private Result getResultWithInterceptor(Task task) throws IOException {
        count.decrementAndGet();
        List<Interceptor> users = vanGogh.interceptors;
        List<Interceptor> interceptors = new ArrayList<>(users.size() + 7);
        interceptors.addAll(users);
        interceptors.add(new KeyMemoryCacheInterceptor(vanGogh.memoryCache));
        interceptors.add(new TransformInterceptor());
        interceptors.add(new StableKeyMemoryCacheInterceptor(vanGogh.memoryCache));
        interceptors.add(new StreamInterceptor());
        interceptors.add(new ContentInterceptor(vanGogh.context));
        if (vanGogh.diskCache != null) {
            interceptors.add(new DiskCacheInterceptor(vanGogh.diskCache));
        }
        interceptors.add(new NetworkInterceptor());
        Interceptor.Chain chain = new RealInterceptorChain(interceptors, 0, task, vanGogh.downloader.clone());
        return chain.proceed(task);
    }
}
