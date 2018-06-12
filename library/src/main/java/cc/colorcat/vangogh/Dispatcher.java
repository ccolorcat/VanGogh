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
import java.util.LinkedHashMap;
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
    private final Deque<Call> waiting = new LinkedList<>();
    //    private final Set<Call> running = new HashSet<>();
    private final Map<String, Call> running = new WeakHashMap<>();
    private final Map<String, Call> callMap = new LinkedHashMap<>();

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

    void dispatchSubmit(Action<?> action) {
//        dispatchHandler.dispatchMessage(dispatchHandler.obtainMessage(REQUEST_SUBMIT, call));
    }

    void dispatchCancel(Action<?> action) {
//        dispatchHandler.dispatchMessage(dispatchHandler.obtainMessage(REQUEST_CANCEL, call));
    }

    void dispatchSuccess(Call call) {
//        dispatchHandler.dispatchMessage(dispatchHandler.obtainMessage(REQUEST_SUCCESS, call));
    }

    void dispatchFailed(Call call) {
//        dispatchHandler.dispatchMessage(dispatchHandler.obtainMessage(REQUEST_FAILED, call));
    }

    void dispatchRetry(Call call) {

    }

    void performSubmit(Action<?> action) {
        Call call = callMap.get(action.taskKey());
        if (call != null) {
            call.attach(action);
        } else {
            call = new Call(vanGogh, action);
            call.future = executor.submit(call);
            callMap.put(action.taskKey(), call);
        }
    }

    void performCancel(Action<?> action) {
        final String key = action.taskKey();
        Call call = callMap.get(key);
        if (call != null) {
            call.detach(action);
            if (call.cancel()) {
                callMap.remove(key);
            }
        }
    }

    void performSuccess(Call call) {
//        if (call.isCanceled()) return;
//        if (running.remove(call.task().uriKey()) == call) {
//            final String key = call.task().taskKey();
//            boolean batch = false;
//            Iterator<Call> iterator = waiting.iterator();
//            while (iterator.hasNext()) {
//                Call realCall = iterator.next();
//                if (realCall.task().taskKey().equals(key)) {
//                    batch = true;
//                    iterator.remove();
//                }
//            }
//            deliver(call, true, batch);
//        } else {
//            LogUtils.e("performSuccess, but call recycled.");
//        }
//        promoteCall();
    }

    void performFailed(Call call) {
//        if (call.isCanceled()) return;
//        if (running.remove(call.task().uriKey()) == call) {
//            if (call.shouldRetry()) {
//                reEnqueueWaiting(call);
//            } else {
//                deliver(call, false, false);
//            }
//        } else {
//            LogUtils.e("performFailed and call recycled.");
//        }
//        promoteCall();
    }

    private void deliver(Call call, boolean success, boolean batch) {
        int what = !success ? VanGogh.DELIVER_FAILED : (batch ? VanGogh.DELIVER_SUCCESS_MULTIPLE : VanGogh.DELIVER_SUCCESS_SINGLE);
        mainHandler.dispatchMessage(mainHandler.obtainMessage(what, call));
    }

    private void promoteCall() {
//        Call call;
//        while (!pause && running.size() < vanGogh.maxRunning && (call = pollWaiting()) != null) {
//            String key = call.task().uriKey();
//            if (!running.containsKey(key)) {
//                running.put(key, call);
//                call.future = executor.submit(call);
//            } else {
//                reEnqueueWaiting(call);
//            }
//        }
    }

    private Call pollWaiting() {
        return vanGogh.mostRecentFirst ? waiting.pollLast() : waiting.pollFirst();
    }

    private void reEnqueueWaiting(Call call) {
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
            Call call = (Call) msg.obj;
            switch (msg.what) {
                case REQUEST_SUBMIT:
//                    dispatcher.performSubmit(call);
                    break;
                case REQUEST_CANCEL:
//                    dispatcher.performCancel(call);
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
