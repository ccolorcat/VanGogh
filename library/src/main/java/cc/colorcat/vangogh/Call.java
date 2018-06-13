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

/**
 * Author: cxx
 * Date: 2017-07-11
 * GitHub: https://github.com/ccolorcat
 */
class Call implements Runnable {
    private final VanGogh vanGogh;

    Action action;
    List<Action> actions;
    Task task;
    Future<?> future;

    private int count;
    From from;
    Bitmap bitmap;
    Throwable cause;

    Call(VanGogh vanGogh, Action action) {
        this.vanGogh = vanGogh;
        this.action = action;
        this.task = action.task;
        this.count = vanGogh.maxTry;
    }

    void attach(Action action) {
        if (actions == null) {
            actions = new ArrayList<>(8);
        }
        actions.add(action);
    }

    void detach(Action action) {
        if (this.action == action) {
            this.action = null;
        } else if (this.actions != null) {
            this.actions.remove(action);
        }
    }

    String key() {
        return task.key();
    }

    boolean tryCancel() {
        return action == null
                && (actions == null || actions.isEmpty())
                && future != null
                && future.cancel(false);
    }

    boolean isCanceled() {
        return future != null && future.isCancelled();
    }

    @Override
    public void run() {
        try {
            Result result = getResultWithInterceptor();
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
            } else if (cause instanceof IOException && count > 0) {
                vanGogh.dispatcher.dispatchRetry(this);
            } else {
                vanGogh.dispatcher.dispatchFailed(this);
            }
        }
    }

    private Result getResultWithInterceptor() throws IOException {
        --count;
        List<Interceptor> users = vanGogh.interceptors;
        List<Interceptor> interceptors = new ArrayList<>(users.size() + 7);
        interceptors.addAll(users);
        interceptors.add(new QuickMemoryInterceptor(vanGogh.memoryCache));
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
}
