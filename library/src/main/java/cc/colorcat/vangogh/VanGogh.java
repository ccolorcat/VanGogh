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
import java.util.Iterator;
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
    static final int DELIVER_SUCCESS_SINGLE = 0x12;
    static final int DELIVER_SUCCESS_MULTIPLE = 0x13;
    static final int DELIVER_FAILED = 0x14;

    @SuppressLint("StaticFieldLeak")
    private static volatile VanGogh singleton;

    private Map<Object, Action> targetToAction = new WeakHashMap<>();

    final Dispatcher dispatcher;
    final boolean mostRecentFirst;
    final int maxRunning;
    final int retryCount;
    final int connectTimeOut;
    final int readTimeOut;

    final List<Interceptor> interceptors;
    final Downloader downloader;
    final int defaultFromPolicy;

    final Cache<Bitmap> memoryCache;
    final DiskCache diskCache;

    final Task.Options defaultOptions;
    final Context context;
    final boolean debugColor;

    final List<Transformation> transformations;

    final Drawable defaultLoading;
    final Drawable defaultError;

    final boolean fade;

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

    public static Uri toUri(Resources resources, @DrawableRes int resId) {
        return new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(resId))
                .appendPath(resources.getResourceTypeName(resId))
                .appendPath(resources.getResourceEntryName(resId))
                .build();
    }

    private VanGogh(Builder builder, Cache<Bitmap> memoryCache, DiskCache diskCache) {
        mostRecentFirst = builder.mostRecentFirst;
        maxRunning = builder.maxRunning;
        retryCount = builder.retryCount;
        connectTimeOut = builder.connectTimeOut;
        readTimeOut = builder.readTimeOut;
        interceptors = Utils.immutableList(builder.interceptors);
        downloader = builder.downloader;
        defaultFromPolicy = builder.defaultFromPolicy;
        defaultOptions = builder.defaultOptions;
        context = builder.context;
        debugColor = builder.debug;
        transformations = Utils.immutableList(builder.transformations);
        defaultLoading = builder.defaultLoading;
        defaultError = builder.defaultError;
        fade = builder.fade;
        this.memoryCache = memoryCache;
        this.diskCache = diskCache;
        this.dispatcher = new Dispatcher(this, builder.executor, null); // todo
    }

    void cancelExistingCall(Object target) {
//        Call call = targetToAction.remove(target);
//        if (call != null) {
//            call.cancel();
//            dispatcher.dispatchCancel(call);
//        }
    }

    void enqueueAndSubmit(Action action) {
//        Object target = call.action.target();
//        if (target != null && targetToAction.get(target) != call) {
//            cancelExistingCall(target);
//            targetToAction.put(target, call);
//        }
//        submit(call);
    }

    void submit(Call call) {
//        dispatcher.dispatchSubmit(call);
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
        return this.load(VanGogh.toUri(resources(), resId));
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
     * Pause all tasks.
     *
     * @see #resume()
     */
    public void pause() {
        dispatcher.pause();
    }

    /**
     * Resume all tasks.
     *
     * @see #pause()
     */
    public void resume() {
        dispatcher.resume();
    }

    /**
     * Clear all pending tasks.
     */
    public void clear() {
        dispatcher.clear();
    }

    /**
     * Clear all cached bitmaps from the memory.
     */
    public void releaseMemory() {
        memoryCache.clear();
    }

    void enqueue(Task task) {
//        dispatcher.enqueue(task);
    }

    Resources resources() {
        return context.getResources();
    }

    Resources.Theme theme() {
        return context.getTheme();
    }

    @Nullable
    Bitmap obtainFromMemoryCache(String key) {
        return memoryCache.get(key);
    }


    static class DeliverHandler extends Handler {
        private final Map<Object, Call> targetToCall;

        DeliverHandler(Map<Object, Call> targetToCall) {
            super(Looper.getMainLooper());
            this.targetToCall = targetToCall;
        }

        @Override
        public void handleMessage(Message msg) {
//            Call obj = (Call) msg.obj;
//            final String key = obj.task().taskKey();
//            final Result result = obj.result;
//            switch (msg.what) {
//                case DELIVER_SUCCESS_SINGLE:
//                    Call call = targetToCall.remove(obj.action.target());
//                    if (call != null) {
//                        call.action.complete(result.bitmap(), result.from());
//                    }
//                    break;
//                case DELIVER_SUCCESS_MULTIPLE:
//                    Iterator<Map.Entry<Object, Call>> iterator = targetToCall.entrySet().iterator();
//                    while (iterator.hasNext()) {
//                        Map.Entry<Object, Call> entry = iterator.next();
//                        Call value = entry.getValue();
//                        if (key.equals(value.task().taskKey())) {
//                            value.action.complete(result.bitmap(), result.from());
//                            iterator.remove();
//                        }
//                    }
//                    break;
//                case DELIVER_FAILED:
//                    Call failed = targetToCall.remove(key);
//                    if (failed != null) {
//                        failed.action.error(obj.cause);
//                    }
//                    break;
//                default:
//                    throw new IllegalArgumentException("received illegal code " + msg.what);
//            }
        }
    }


    public static class Builder {
        private ExecutorService executor;
        private boolean mostRecentFirst;
        private int maxRunning;
        private int retryCount;
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
        private boolean debug;

        private List<Transformation> transformations;
        private boolean fade;

        private Drawable defaultLoading;
        private Drawable defaultError;

        public Builder(Context ctx) {
            mostRecentFirst = true;
            maxRunning = 4;
            retryCount = 1;
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
            debug = false;
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
         * @param mostRecentFirst LIFO if true, otherwise FIFO, the default is true.
         */
        public Builder taskPolicy(boolean mostRecentFirst) {
            this.mostRecentFirst = mostRecentFirst;
            return this;
        }

        /**
         * @param maxRunning The maximum number of concurrent tasks.
         * @throws IllegalArgumentException If the maxRunning less than 1 or greater than 10.
         */
        public Builder maxRunning(int maxRunning) {
            if (maxRunning < 1 || maxRunning > 10) {
                throw new IllegalArgumentException("maxRunning[1, 10] = " + maxRunning);
            }
            this.maxRunning = maxRunning;
            return this;
        }

        /**
         * @param retryCount The maximum number of retries.
         * @throws IllegalArgumentException if the retryCount less than 0.
         */
        public Builder retryCount(int retryCount) {
            if (retryCount < 0) {
                throw new IllegalArgumentException("retryCount < 0");
            }
            this.retryCount = retryCount;
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

        public Builder debug(boolean debug) {
            this.debug = debug;
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
                        maxRunning,
                        Math.min(maxRunning + (maxRunning >> 1), 10),
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
