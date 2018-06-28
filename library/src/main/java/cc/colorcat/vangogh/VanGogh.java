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
import android.support.annotation.MainThread;
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
    static final int CALL_BATCH_COMPLETE = 0x15;
    static final int ACTION_BATCH_RESUME = 0x16;

    @SuppressLint("StaticFieldLeak")
    private static volatile VanGogh singleton;

    private final Map<Object, Action> targetUniqueToAction;
    final Dispatcher dispatcher;

    final Context context;

    final List<Interceptor> interceptors;
    final Downloader downloader;
    final int connectTimeOut;
    final int readTimeOut;
    final int fromPolicy;
    final int maxTry;

    final Cache<Bitmap> memoryCache;
    final DiskCache diskCache;

    final List<Transformation> transformations;
    final Drawable placeholder;
    final Drawable error;
    final Task.Options options;
    final boolean indicatorEnabled;
    final boolean fade;

    private VanGogh(Builder builder, Cache<Bitmap> memoryCache, DiskCache diskCache) {
        this.context = builder.context;
        this.interceptors = Utils.immutableList(builder.interceptors);
        this.downloader = builder.downloader;
        this.connectTimeOut = builder.connectTimeOut;
        this.readTimeOut = builder.readTimeOut;
        this.fromPolicy = builder.fromPolicy;
        this.maxTry = builder.maxTry;
        this.memoryCache = memoryCache;
        this.diskCache = diskCache;
        this.transformations = Utils.immutableList(builder.transformations);
        this.placeholder = builder.placeholder;
        this.error = builder.error;
        this.options = builder.options;
        this.indicatorEnabled = builder.indicatorEnabled;
        this.fade = builder.fade;
        this.targetUniqueToAction = new WeakHashMap<>();
        this.dispatcher = new Dispatcher(this, builder.executor, new MainHandler(this));
    }

    @Nullable
    Bitmap obtainFromMemoryCache(String key) {
        return memoryCache.get(key);
    }

    void cancelExistingAction(Object targetUnique) {
        Action action = targetUniqueToAction.remove(targetUnique);
        if (action != null) {
            action.cancel();
            dispatcher.dispatchCancel(action);
        }
    }

    @MainThread
    void enqueueAndSubmit(Action action) {
        Object targetUnique = action.targetUnique();
        if (targetUnique != null && targetUniqueToAction.get(targetUnique) != action) {
            cancelExistingAction(targetUnique);
            targetUniqueToAction.put(targetUnique, action);
        }
        submit(action);
    }

    @MainThread
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

    @MainThread
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
        targetUniqueToAction.remove(action.targetUnique());
        if (result != null) {
            action.onSuccess(result, from);
        } else {
            action.onFailed(cause);
        }
    }

    public void pauseTag(Object tag) {
        dispatcher.dispatchPauseTag(tag);
    }

    public void resumeTag(Object tag) {
        dispatcher.dispatchResumeTag(tag);
    }

    public void cancelTag(Object tag) {
        Utils.checkMain();
        for (Iterator<Action> i = targetUniqueToAction.values().iterator(); i.hasNext(); ) {
            Action action = i.next();
            if (action.tag.equals(tag)) {
                i.remove();
                action.cancel();
                dispatcher.dispatchCancel(action);
            }
        }
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
        String stableKey = Utils.createStableKey(u);
        return new Creator(this, u, stableKey);
    }

    /**
     * Clear all cached bitmaps from the memory.
     */
    public void clearMemoryCache() {
        memoryCache.clear();
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

        private Context context;

        private List<Interceptor> interceptors;
        private Downloader downloader;
        private int connectTimeOut;
        private int readTimeOut;
        private int fromPolicy;
        private int maxTry;


        private long memoryCacheSize;
        private File cacheDirectory;
        private long diskCacheSize;

        private List<Transformation> transformations;
        private Drawable placeholder;
        private Drawable error;
        private Task.Options options;
        private boolean indicatorEnabled;
        private boolean fade;


        public Builder(Context ctx) {
            context = ctx.getApplicationContext();
            interceptors = new ArrayList<>(4);
            downloader = new HttpDownloader();
            connectTimeOut = 5000;
            readTimeOut = 5000;
            fromPolicy = From.ANY.policy;
            maxTry = 1;
            memoryCacheSize = Utils.calculateMemoryCacheSize(ctx);
            cacheDirectory = Utils.getCacheDirectory(ctx);
            diskCacheSize = (long) Math.min(50 * 1024 * 1024, cacheDirectory.getUsableSpace() * 0.1);
            transformations = new ArrayList<>(4);
            options = new Task.Options();
            indicatorEnabled = false;
            fade = true;
        }

        /**
         * @param executor The executor service for placeholder images in the background.
         */
        public Builder executor(ExecutorService executor) {
            if (executor == null) {
                throw new NullPointerException("executor == null");
            }
            this.executor = executor;
            return this;
        }

        public Builder addInterceptor(Interceptor interceptor) {
            if (interceptor == null) {
                throw new NullPointerException("interceptor == null");
            }
            if (!this.interceptors.contains(interceptor)) {
                this.interceptors.add(interceptor);
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

        /**
         * The default policy of image source.
         * Any source, <code>From.ANY.policy</code>
         * Memory and Disk, <code>From.MEMORY.policy | From.DISK.policy</code>
         * Memory and Network, <code>From.MEMORY.policy | From.NETWORK.policy</code>
         * ...
         *
         * @see From
         */
        public Builder fromPolicy(int fromPolicy) {
            From.checkFromPolicy(fromPolicy);
            this.fromPolicy = fromPolicy;
            return this;
        }

        /**
         * @param maxTry The maximum number of retries.
         * @throws IllegalArgumentException if the maxTry <= 0.
         */
        public Builder maxTry(int maxTry) {
            if (maxTry <= 0) {
                throw new IllegalArgumentException("maxTry <= 0");
            }
            this.maxTry = maxTry;
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

        public Builder addTransformation(Transformation transformation) {
            if (transformation == null) {
                throw new NullPointerException("transformation == null");
            }
            if (!this.transformations.contains(transformation)) {
                this.transformations.add(transformation);
            }
            return this;
        }

        /**
         * The default drawable to be used while the image is being loaded.
         */
        public Builder placeholder(Drawable placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        /**
         * The default drawable to be used while the image is being loaded.
         */
        public Builder placeholder(@DrawableRes int resId) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                placeholder = context.getDrawable(resId);
            } else {
                placeholder = context.getResources().getDrawable(resId);
            }
            return this;
        }

        /**
         * The default drawable to be used if the request image could not be loaded.
         */
        public Builder error(Drawable error) {
            this.error = error;
            return this;
        }

        /**
         * The default drawable to be used if the request image could not be loaded.
         */
        public Builder error(@DrawableRes int resId) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                error = context.getDrawable(resId);
            } else {
                error = context.getResources().getDrawable(resId);
            }
            return this;
        }

        public Builder options(Task.Options options) {
            if (options == null) {
                throw new NullPointerException("options == null");
            }
            this.options = options;
            return this;
        }

        public Builder indicator(boolean enabled) {
            this.indicatorEnabled = enabled;
            return this;
        }

        public Builder fade(boolean fade) {
            this.fade = fade;
            return this;
        }

        public Builder log(boolean enabled) {
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
