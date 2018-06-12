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

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;

/**
 * Author: cxx
 * Date: 2018-07-10
 * GitHub: https://github.com/ccolorcat
 */
class Dispatcher {
    private static final int REQUEST_SUBMIT = 100;
    static final int REQUEST_CANCEL = 101;
    static final int REQUEST_SUCCESS = 102;
    static final int REQUEST_FAILED = 103;

    private final Handler mainHandler;
    private final DispatchThread dispatchThread;
    private final DispatchHandler dispatchHandler;
    private final ExecutorService executor;
    //    private final Deque<Task> tasks = new LinkedList<>();
    private final Deque<RealCall> waiting = new LinkedList<>();
    //    private final Set<RealCall> running = new HashSet<>();
    private final Map<String, RealCall> running = new WeakHashMap<>();

    private final VanGogh vanGogh;
    private volatile boolean pause = false;

    Dispatcher(VanGogh vanGogh, ExecutorService executor, Handler mainHandler) {
        this.vanGogh = vanGogh;
        this.executor = executor;
        this.mainHandler = mainHandler;
        this.dispatchThread = new DispatchThread();
        this.dispatchThread.start();
        this.dispatchHandler = new DispatchHandler(dispatchThread.getLooper(), this);
    }

    void dispatchSubmit(RealCall call) {
        dispatchHandler.dispatchMessage(dispatchHandler.obtainMessage(REQUEST_SUBMIT, call));
    }

    void dispatchCancel(RealCall call) {
        dispatchHandler.dispatchMessage(dispatchHandler.obtainMessage(REQUEST_CANCEL, call));
    }

    void dispatchSuccess(RealCall call) {
        dispatchHandler.dispatchMessage(dispatchHandler.obtainMessage(REQUEST_SUCCESS, call));
    }

    void dispatchFailed(RealCall call) {
        dispatchHandler.dispatchMessage(dispatchHandler.obtainMessage(REQUEST_FAILED, call));
    }

    void performSubmit(RealCall call) {
        if (waiting.offer(call)) {
            promoteCall();
        }
    }

    void performCancel(RealCall call) {
        waiting.remove(call);
        running.remove(call.task().stableKey());
        promoteCall();
    }

    void performSuccess(RealCall call) {
        if (call.isCanceled()) return;
        if (running.remove(call.task().stableKey()) == call) {
            final String key = call.task().key();
            boolean batch = false;
            Iterator<RealCall> iterator = waiting.iterator();
            while (iterator.hasNext()) {
                RealCall realCall = iterator.next();
                if (realCall.task().key().equals(key)) {
                    batch = true;
                    iterator.remove();
                }
            }
            deliver(call, true, batch);
        } else {
            LogUtils.e("performSuccess, but call recycled.");
        }
        promoteCall();
    }

    void performFailed(RealCall call) {
        if (call.isCanceled()) return;
        if (running.remove(call.task().stableKey()) == call) {
            if (call.shouldRetry()) {
                reEnqueueWaiting(call);
            } else {
                deliver(call, false, false);
            }
        } else {
            LogUtils.e("performFailed and call recycled.");
        }
        promoteCall();
    }

    private void deliver(RealCall call, boolean success, boolean batch) {
        int what = !success ? VanGogh.DELIVER_FAILED : (batch ? VanGogh.DELIVER_SUCCESS_MULTIPLE : VanGogh.DELIVER_SUCCESS_SINGLE);
        mainHandler.dispatchMessage(mainHandler.obtainMessage(what, call));
    }

    private void promoteCall() {
        RealCall call;
        while (!pause && running.size() < vanGogh.maxRunning && (call = pollWaiting()) != null) {
            String key = call.task().stableKey();
            if (!running.containsKey(key)) {
                running.put(key, call);
                call.future = executor.submit(call);
            } else {
                reEnqueueWaiting(call);
            }
        }
    }

    private RealCall pollWaiting() {
        return vanGogh.mostRecentFirst ? waiting.pollLast() : waiting.pollFirst();
    }

    private void reEnqueueWaiting(RealCall call) {
        if (vanGogh.mostRecentFirst) {
            waiting.offerFirst(call);
        } else {
            waiting.offerLast(call);
        }
    }

    void pause() {
        pause = true;
    }

    void resume() {
        pause = false;
    }

    void clear() {
    }

    private static class DispatchThread extends HandlerThread {
        DispatchThread() {
            super("Dispatcher", Process.THREAD_PRIORITY_BACKGROUND);
        }
    }

    private static class DispatchHandler extends Handler {
        private final Dispatcher dispatcher;

        DispatchHandler(Looper looper, Dispatcher dispatcher) {
            super(looper);
            this.dispatcher = dispatcher;
        }

        @Override
        public void handleMessage(Message msg) {
            RealCall call = (RealCall) msg.obj;
            switch (msg.what) {
                case REQUEST_SUBMIT:
                    dispatcher.performSubmit(call);
                    break;
                case REQUEST_CANCEL:
                    dispatcher.performCancel(call);
                    break;
                case REQUEST_SUCCESS:
                    dispatcher.performSuccess(call);
                    break;
                case REQUEST_FAILED:
                    dispatcher.performFailed(call);
                    break;
                default:
                    throw new IllegalArgumentException("received illegal code " + msg.what);
            }
        }
    }
}
