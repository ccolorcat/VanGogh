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
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Author: cxx
 * Date: 2018-07-10
 * GitHub: https://github.com/ccolorcat
 */
class Dispatcher {
    static final int ACTION_SUBMIT = 100;
    static final int ACTION_CANCEL = 101;
    static final int CALL_COMPLETE = 102;
    static final int CALL_FAILED = 103;
    static final int CALL_RETRY = 104;
    static final int CALL_DELAY_NEXT_BATCH = 105;
    static final int TAG_PAUSE = 106;
    static final int TAG_RESUME = 107;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final VanGogh vanGogh;
    private final ExecutorService executor;
    private final Map<String, Call> keyToCall = new LinkedHashMap<>();
    private final List<Call> batch = new ArrayList<>(4);
    private final Set<Object> pausedTags = new HashSet<>();
    private final SparseArray<Task> pausedTask = new SparseArray<>();

    Dispatcher(VanGogh vanGogh, ExecutorService executor) {
        this.vanGogh = vanGogh;
        this.executor = executor;
    }

    void dispatchSubmit(Task task) {
        task.onPreExecute();
        if (pausedTags.contains(task.tag)) {
            pausedTask.put(task.target.uniqueCode(), task);
            return;
        }
        Call call = keyToCall.get(task.key);
        if (call != null) {
            call.attach(task);
        }
    }

    void dispatchCancel(Task task) {

    }

    void dispatchSuccess(Call call) {

    }

    void dispatchRetry(Call call) {

    }

    void dispatchFailed(Call call) {

    }

    private void performResumeTag(Object obj) {

    }

    private void performPauseTag(Object obj) {

    }

    private void performBatchComplete() {


    }

    private void performError(Call call) {

    }

    private void performRetry(Call call) {

    }

    private void performComplete(Call call) {

    }

    private void performCancel(Task task) {

    }

    private void performSubmit(Task task) {
    }


    void pause() {
//        pause = true;
    }

    void resume() {
//        pause = false;
//        synchronized (waiting) {
//            promoteTask();
//        }
    }

    void clear() {
//        Utils.checkMain();
//        synchronized (waiting) {
//            waiting.clear();
//            tasks.clear();
//        }
    }

//    void enqueue(Task task) {
//        Utils.checkMain();
//        if (!tasks.contains(task) && tasks.offer(task)) {
//            task.onPreExecute();
//            Call call = new Call(vanGogh, task);
//            synchronized (waiting) {
//                if (!waiting.contains(call) && waiting.offer(call)) {
//                    promoteTask();
//                }
//            }
//        }
//    }

//    private void promoteTask() {
//        Call call;
//        while (!pause && running.size() < vanGogh.maxRunning && (call = pollWaiting()) != null) {
//            if (running.add(call)) {
//                executor.submit(new AsyncCall(call));
//            }
//        }
//        LogUtils.i("Dispatcher", "waiting tasks = " + tasks.size()
//                + "\n waiting calls = " + waiting.size()
//                + "\n running calls = " + running.size());
//    }
//
//    private Call pollWaiting() {
//        return vanGogh.mostRecentFirst ? waiting.pollLast() : waiting.pollFirst();
//    }

//    private void completeCall(final Call call, final Result result, final Exception cause) {
//        if ((result != null) == (cause != null)) {
//            throw new IllegalStateException("dispatcher reporting error.");
//        }
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                String stableKey = call.task().stableKey();
//                Iterator<Task> iterator = tasks.descendingIterator();
//                while (iterator.hasNext()) {
//                    Task task = iterator.next();
//                    if (stableKey.equals(task.stableKey())) {
//                        task.onPostResult(result, cause);
//                        iterator.remove();
//                    }
//                }
//            }
//        });
//    }

//    private class AsyncCall implements Runnable {
//        private Call call;
//
//        private AsyncCall(Call call) {
//            this.call = call;
//        }
//
//        @Override
//        public void run() {
//            Result result = null;
//            Exception cause = null;
//            try {
//                result = call.execute();
//            } catch (IOException e) {
//                LogUtils.e(e);
//                cause = e;
//            } catch (IndexOutOfBoundsException e) {
//                LogUtils.e(e);
//                cause = new UnsupportedOperationException("unsupported uri: " + call.task().uri());
//            } finally {
//                synchronized (waiting) {
//                    running.remove(call);
//                    if (result != null || call.getAndIncrement() >= vanGogh.maxTry) {
//                        completeCall(call, result, cause);
//                    } else if (!waiting.contains(call)) {
//                        waiting.offer(call);
//                    }
//                    promoteTask();
//                }
//            }
//        }
//    }

    private static class DispatcherHandler extends Handler {
        private final Dispatcher dispatcher;

        private DispatcherHandler(Looper looper, Dispatcher dispatcher) {
            super(looper);
            this.dispatcher = dispatcher;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ACTION_SUBMIT: {
                    Task task = (Task) msg.obj;
                    dispatcher.performSubmit(task);
                    break;
                }
                case ACTION_CANCEL: {
                    Task task= (Task) msg.obj;
                    dispatcher.performCancel(task);
                    break;
                }
                case CALL_COMPLETE: {
                    Call call = (Call) msg.obj;
                    dispatcher.performComplete(call);
                    break;
                }
                case CALL_RETRY: {
                    Call call = (Call) msg.obj;
                    dispatcher.performRetry(call);
                    break;
                }
                case CALL_FAILED: {
                    Call call = (Call) msg.obj;
                    dispatcher.performError(call);
                    break;
                }
                case CALL_DELAY_NEXT_BATCH: {
                    dispatcher.performBatchComplete();
                    break;
                }
                case TAG_PAUSE: {
                    dispatcher.performPauseTag(msg.obj);
                    break;
                }
                case TAG_RESUME: {
                    dispatcher.performResumeTag(msg.obj);
                    break;
                }
                default:
                    throw new AssertionError("Illegal message received: " + msg.what);
            }
        }
    }


}
