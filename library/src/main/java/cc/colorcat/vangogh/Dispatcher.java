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
import java.util.Iterator;
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
    private final Handler handler;

    private final VanGogh vanGogh;
    private final ExecutorService executor;
    private final Map<String, Call> keyToCall = new LinkedHashMap<>();
    private final Set<Object> pausedTags = new HashSet<>();
    private final SparseArray<Task> pausedTasks = new SparseArray<>();

    Dispatcher(VanGogh vanGogh, ExecutorService executor) {
        this.vanGogh = vanGogh;
        this.executor = executor;
        this.handler = vanGogh.handler;
    }

    void dispatchSubmit(Task task) {
        if (pausedTags.contains(task.tag)) {
            pausedTasks.put(task.target.uniqueCode(), task);
            return;
        }
        task.onPreExecute();
        Call call = keyToCall.get(task.key);
        if (call != null) {
            call.attach(task);
        } else {
            call = new Call(vanGogh, task);
            keyToCall.put(call.key, call);
            call.future = executor.submit(call);
        }
    }

    void dispatchCancel(Task task) {
        final String key = task.key;
        Call call = keyToCall.get(key);
        if (call != null) {
            call.detach(task);
            if (call.tryCancel()) {
                keyToCall.remove(key);
            }
        }
        if (pausedTags.contains(task.tag)) {
            pausedTasks.remove(task.target.uniqueCode());
        }
    }

    void dispatchSuccess(Call call) {
        keyToCall.remove(call.key);
        batch(call);
    }

    void dispatchRetry(Call call) {
        if (call.isCanceled()) {
            return;
        }
        call.future = executor.submit(call);
    }

    void dispatchFailed(Call call) {
        keyToCall.remove(call.key);
        batch(call);
    }

    void dispatchPauseTag(Object tag) {
        if (!pausedTags.add(tag)) {
            return;
        }
        Iterator<Call> iterator = keyToCall.values().iterator();
        while (iterator.hasNext()) {
            Call call = iterator.next();
            if (call.tasks.isEmpty()) {
                continue;
            }
            for (int i = 0, size = call.tasks.size(); i < size; ++i) {
                Task task = call.tasks.get(i);
                if (!tag.equals(task.tag)) {
                    continue;
                }
                call.detach(task);
                pausedTasks.put(task.target.uniqueCode(), task);
            }
            if (call.tryCancel()) {
                iterator.remove();
            }
        }
    }

    void dispatchResumeTag(Object tag) {
        if (!pausedTags.remove(tag)) {
            return;
        }
        List<Task> resumed = null;
        for (int i = 0; i < pausedTasks.size(); ++i) {
            Task task = pausedTasks.valueAt(i);
            if (tag.equals(task.tag)) {
                if (resumed == null) {
                    resumed = new ArrayList<>(8);
                }
                resumed.add(task);
                pausedTasks.removeAt(i);
            }
        }
        if (resumed != null) {
            handler.sendMessage(handler.obtainMessage(VanGogh.TASK_BATCH_RESUME, resumed));
        }
    }

    private void performPauseTag(Object tag) {
        if (!pausedTags.add(tag)) {
            return;
        }
        Iterator<Call> iterator = keyToCall.values().iterator();
        while (iterator.hasNext()) {
            Call call = iterator.next();
            if (call.tasks.isEmpty()) {
                continue;
            }
            for (int i = 0, size = call.tasks.size(); i < size; ++i) {
                Task task = call.tasks.get(i);
                if (!tag.equals(task.tag)) {
                    continue;
                }
                call.detach(task);
                pausedTasks.put(task.target.uniqueCode(), task);
            }
            if (call.tryCancel()) {
                iterator.remove();
            }
        }
    }

    private void batch(Call call) {
        if (call.isCanceled()) {
            return;
        }
        if (call.bitmap != null) {
            call.bitmap.prepareToDraw();
        }

    }

    private void performResumeTag(Object tag) {
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
