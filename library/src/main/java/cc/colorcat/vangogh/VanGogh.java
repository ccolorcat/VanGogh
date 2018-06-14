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

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Author: cxx
 * Date: 2017-07-06
 * GitHub: https://github.com/ccolorcat
 */
@SuppressWarnings("unused")
public class VanGogh {
    static final int CALL_BATCH_COMPLETE = 0x15;
    static final int ACTION_BATCH_RESUME = 0x16;

    @SuppressLint("StaticFieldLeak")
    private static volatile VanGogh singleton;

    private final Map<Object, Action> targetToAction;
    final Dispatcher dispatcher;
    final int maxTry;
    final int connectTimeOut;
    final int readTimeOut;

    final List<Interceptor> interceptors;
    final Downloader downloader;
    final int defaultFromPolicy;

    final Cache<Bitmap> memoryCache;
    final DiskCache diskCache;

    final Task.Options defaultOptions;
    final Context context;
    final boolean indicatorEnabled;

    final List<Transformation> transformations;

    final Drawable defaultLoading;
    final Drawable defaultError;

    final boolean fade;


    private VanGogh(Builder builder, Cache<Bitmap> memoryCache, DiskCache diskCache) {
        maxTry = builder.maxTry;
        connectTimeOut = builder.connectTimeOut;
        readTimeOut = builder.readTimeOut;
        interceptors = Utils.immutableList(builder.interceptors);
        downloader = builder.downloader;
        defaultFromPolicy = builder.defaultFromPolicy;
        defaultOptions = builder.defaultOptions;
        context = builder.context;
        indicatorEnabled = builder.indicatorEnabled;
        transformations = Utils.immutableList(builder.transformations);
        defaultLoading = builder.defaultLoading;
        defaultError = builder.defaultError;
        fade = builder.fade;
        this.memoryCache = memoryCache;
        this.diskCache = diskCache;
        this.targetToAction = new WeakHashMap<>();
        this.dispatcher = new Dispatcher(this, builder.executor, new MainHandler(this));
    }

    void cancelExistingAction(Object target) {
        Action action = targetToAction.remove(target);
        if (action != null) {
            action.cancel();
            dispatcher.dispatchCancel(action);
        }
    }

    void enqueueAndSubmit(Action action) {
        Object target = action.target();
        if (target != null && targetToAction.get(target) != action) {
            cancelExistingAction(target);
            targetToAction.put(target, action);
        }
        submit(action);
    }

    void submit(Action action) {
        dispatcher.dispatchSubmit(action);
    }

    void complete(Call call) {
        List<Action> actions = call.actions;
        if (!actions.isEmpty()) {
            Bitmap result = call.bitmap;
            From from = call.from;
            Throwable cause = call.cause;
            for (int i = 0, size = actions.size(); i < size; ++i) {
                deliverAction(result, from, cause, actions.get(i));
            }
        }
    }

    void resumeAction(Action action) {
        Bitmap bitmap = null;
        if ((action.task.fromPolicy() & From.MEMORY.policy) != 0) {
            bitmap = obtainFromMemoryCache(action.key);
        }
        if (bitmap != null) {
            deliverAction(bitmap, From.MEMORY, null, action);
        } else {
            enqueueAndSubmit(action);
        }
    }

    private void deliverAction(Bitmap result, From from, Throwable cause, Action action) {
        if (action.isCanceled()) {
            return;
        }
        targetToAction.remove(action.target());
        if (result != null) {
            action.complete(result, from);
        } else {
            action.error(cause);
        }
    }

    public void pauseTag(Object tag) {
        dispatcher.dispatchPauseTag(tag);
    }

    public void resumeTag(Object tag) {
        dispatcher.dispatchResumeTag(tag);
    }

    /**
     * Create a {@link Creator} using the specified path.
     *
     * @param uri May be a remote URL, file or android resource.
     * @see #load(Uri)
     * @see #load(File)
     * @see #load(int)
     */
    public Creator load(String uri) {
        return this.load(TextUtils.isEmpty(uri) ? Uri.EMPTY : Uri.parse(uri));
    }

    /**
     * Create a {@link Creator} using the specified drawable resource ID.
     *
     * @see #load(Uri)
     * @see #load(File)
     * @see #load(String)
     */
    public Creator load(@DrawableRes int resId) {
        return this.load(VanGogh.toUri(context, resId));
    }

    /**
     * Create a {@link Creator} using the specified image file.
     *
     * @see #load(Uri)
     * @see #load(String)
     * @see #load(int)
     */
    public Creator load(File file) {
        return this.load(file == null ? Uri.EMPTY : Uri.fromFile(file));
    }

    /**
     * Create a {@link Creator} using the specified uri.
     *
     * @see #load(String)
     * @see #load(File)
     * @see #load(int)
     */
    public Creator load(Uri uri) {
        Uri u = (uri == null ? Uri.EMPTY : uri);
        String stableKey = Utils.md5(u.toString());
        return new Creator(this, u, stableKey);
    }

    /**
     * Clear all pending tasks.
     */
    public void clear() {
//        dispatcher.clear();
    }

    /**
     * Clear all cached bitmaps from the memory.
     */
    public void releaseMemory() {
        memoryCache.clear();
    }

    @Nullable
    Bitmap obtainFromMemoryCache(String key) {
        return memoryCache.get(key);
    }


    /**
     * Set the global instance.
     * NOTE: This method must be called before calls to {@link #with}.
     */
    public static void setSingleton(VanGogh vanGogh) {
        synchronized (VanGogh.class) {
            if (singleton != null) {
                throw new IllegalStateException("Singleton instance already exists.");
            }
            singleton = vanGogh;
        }
    }

    /**
     * Get the global instance.
     * If the global instance is null which will be initialized with default.
     */
    public static VanGogh with(Context ctx) {
        if (singleton == null) {
            synchronized (VanGogh.class) {
                if (singleton == null) {
                    singleton = new Builder(ctx).build();
                }
            }
        }
        return singleton;
    }

    public static VanGogh get() {
        if (singleton == null) {
            throw new IllegalStateException("The singleton is null.");
        }
        return singleton;
    }

    public static Uri toUri(Context context, @DrawableRes int resId) {
        final Resources resources = context.getResources();
        return new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(resId))
                .appendPath(resources.getResourceTypeName(resId))
                .appendPath(resources.getResourceEntryName(resId))
                .build();
    }


    private static class MainHandler extends Handler {
        private final VanGogh vanGogh;

        MainHandler(VanGogh vanGogh) {
            super(Looper.getMainLooper());
            this.vanGogh = vanGogh;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VanGogh.CALL_BATCH_COMPLETE: {
                    @SuppressWarnings("unchecked")
                    List<Call> calls = (List<Call>) msg.obj;
                    for (int i = 0, size = calls.size(); i < size; ++i) {
                        vanGogh.complete(calls.get(i));
                    }
                    break;
                }
                case VanGogh.ACTION_BATCH_RESUME: {
                    @SuppressWarnings("unchecked")
                    List<Action> actions = (List<Action>) msg.obj;
                    for (int i = 0, size = actions.size(); i < size; ++i) {
                        Action action = actions.get(i);
                        vanGogh.resumeAction(action);
                    }
                    break;
                }
                default:
                    throw new AssertionError("Unknown handler message received: " + msg.what);
            }
        }
    }


    public static class Builder {
        private ExecutorService executor;
        private int maxTry;
        private int connectTimeOut;
        private int readTimeOut;

        private List<Interceptor> interceptors;
        private Downloader downloader;
        private int defaultFromPolicy;

        private long memoryCacheSize;
        private File cacheDirectory;
        private long diskCacheSize;

        private Task.Options defaultOptions;
        private Context context;
        private boolean indicatorEnabled;

        private List<Transformation> transformations;
        private boolean fade;

        private Drawable defaultLoading;
        private Drawable defaultError;

        public Builder(Context ctx) {
            maxTry = 1;
            connectTimeOut = 5000;
            readTimeOut = 5000;
            interceptors = new ArrayList<>(4);
            downloader = new HttpDownloader();
            defaultFromPolicy = From.ANY.policy;
            memoryCacheSize = Utils.calculateMemoryCacheSize(ctx);
            cacheDirectory = Utils.getCacheDirectory(ctx);
            diskCacheSize = (long) Math.min(50 * 1024 * 1024, cacheDirectory.getUsableSpace() * 0.1);
            defaultOptions = new Task.Options();
            context = ctx.getApplicationContext();
            indicatorEnabled = false;
            transformations = new ArrayList<>(4);
            fade = true;
        }

        /**
         * @param executor The executor service for loading images in the background.
         */
        public Builder executor(ExecutorService executor) {
            if (executor == null) {
                throw new NullPointerException("executor == null");
            }
            this.executor = executor;
            return this;
        }

        /**
         * @param maxTry The maximum number of retries.
         * @throws IllegalArgumentException if the maxTry less than 0.
         */
        public Builder maxTry(int maxTry) {
            if (maxTry < 0) {
                throw new IllegalArgumentException("maxTry < 0");
            }
            this.maxTry = maxTry;
            return this;
        }

        public Builder connectTimeOut(int timeOut) {
            if (timeOut < 0) {
                throw new IllegalArgumentException("timeOut < 0");
            }
            this.connectTimeOut = timeOut;
            return this;
        }

        public Builder readTimeOut(int timeOut) {
            if (timeOut < 0) {
                throw new IllegalArgumentException("timeOut < 0");
            }
            this.readTimeOut = timeOut;
            return this;
        }

        public Builder addInterceptor(Interceptor interceptor) {
            if (interceptor == null) {
                throw new NullPointerException("interceptor == null");
            }
            if (!interceptors.contains(interceptor)) {
                interceptors.add(interceptor);
            }
            return this;
        }

        /**
         * @param downloader The {@link Downloader} will be used for download images.
         * @throws NullPointerException if downloader is null
         * @see HttpDownloader
         */
        public Builder downloader(Downloader downloader) {
            if (downloader == null) {
                throw new NullPointerException("downloader == null");
            }
            this.downloader = downloader;
            return this;
        }

        /**
         * The default policy of image source.
         * Any source, <code>From.ANY.policy</code>
         * Memory and Disk, <code>From.MEMORY.policy | From.DISK.policy</code>
         * Memory and Network, <code>From.MEMORY.policy | From.NETWORK.policy</code>
         * ...
         *
         * @see From
         */
        public Builder defaultFromPolicy(int fromPolicy) {
            From.checkFromPolicy(fromPolicy);
            this.defaultFromPolicy = fromPolicy;
            return this;
        }

        public Builder memoryCacheSize(long sizeInByte) {
            if (sizeInByte <= 0L) {
                throw new IllegalArgumentException("sizeInByte <= 0");
            }
            this.memoryCacheSize = sizeInByte;
            return this;
        }

        public Builder diskCache(File directory) {
            if (directory == null) {
                throw new NullPointerException("directory == null");
            }
            this.cacheDirectory = directory;
            return this;
        }

        public Builder diskCacheSize(long sizeInByte) {
            if (sizeInByte <= 0L) {
                throw new IllegalArgumentException("sizeInByte <= 0");
            }
            this.diskCacheSize = sizeInByte;
            return this;
        }

        public Builder defaultOptions(Task.Options options) {
            if (options == null) {
                throw new NullPointerException("options == null");
            }
            defaultOptions = options;
            return this;
        }

        public Builder enableIndicator(boolean enabled) {
            this.indicatorEnabled = enabled;
            return this;
        }

        public Builder addTransformation(Transformation transformation) {
            if (transformation == null) {
                throw new NullPointerException("transformation == null");
            }
            if (!transformations.contains(transformation)) {
                transformations.add(transformation);
            }
            return this;
        }

        public Builder fade(boolean fade) {
            this.fade = fade;
            return this;
        }

        /**
         * The default drawable to be used while the image is being loaded.
         */
        public Builder defaultLoading(Drawable loading) {
            defaultLoading = loading;
            return this;
        }

        /**
         * The default drawable to be used while the image is being loaded.
         */
        public Builder defaultLoading(@DrawableRes int resId) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                defaultLoading = context.getDrawable(resId);
            } else {
                defaultLoading = context.getResources().getDrawable(resId);
            }
            return this;
        }

        /**
         * The default drawable to be used if the request image could not be loaded.
         */
        public Builder defaultError(Drawable error) {
            defaultError = error;
            return this;
        }

        /**
         * The default drawable to be used if the request image could not be loaded.
         */
        public Builder defaultError(@DrawableRes int resId) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                defaultError = context.getDrawable(resId);
            } else {
                defaultError = context.getResources().getDrawable(resId);
            }
            return this;
        }

        public Builder enableLog(boolean enabled) {
            LogUtils.init(enabled);
            return this;
        }

        public VanGogh build() {
            DiskCache diskCache = null;
            try {
                diskCache = DiskCache.open(cacheDirectory, diskCacheSize);
            } catch (IOException e) {
                LogUtils.e(e);
            }
            if (executor == null) {
                executor = new ThreadPoolExecutor(
                        4,
                        5,
                        10L,
                        TimeUnit.SECONDS,
                        new LinkedBlockingDeque<Runnable>(),
                        new ThreadPoolExecutor.DiscardOldestPolicy()
                );
            }
            return new VanGogh(this, new MemoryCache((int) memoryCacheSize), diskCache);
        }
    }
}
