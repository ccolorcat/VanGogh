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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
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

    private final Handler mainHandler;
    private final DispatcherThread dispatcherThread;
    private final DispatcherHandler handler;
    private final ExecutorService executor;
    private final Map<String, Call> callMap = new LinkedHashMap<>();
    private final List<Call> batch = new ArrayList<>(4);
    private final Set<Object> pausedTags = new HashSet<>();
    private final Map<Object, Action> pausedActions = new WeakHashMap<>(); // target to action

    private final VanGogh vanGogh;

    Dispatcher(VanGogh vanGogh, ExecutorService executor, Handler mainHandler) {
        this.vanGogh = vanGogh;
        this.executor = executor;
        this.mainHandler = mainHandler;
        this.dispatcherThread = new DispatcherThread();
        this.dispatcherThread.start();
        this.handler = new DispatcherHandler(dispatcherThread.getLooper(), this);
    }

    void dispatchSubmit(Action action) {
        action.prepare();
        handler.sendMessage(handler.obtainMessage(ACTION_SUBMIT, action));
    }

    void dispatchCancel(Action action) {
        handler.sendMessage(handler.obtainMessage(ACTION_CANCEL, action));
    }

    void dispatchSuccess(Call call) {
        handler.sendMessage(handler.obtainMessage(CALL_COMPLETE, call));
    }

    void dispatchFailed(Call call) {
        handler.sendMessage(handler.obtainMessage(CALL_FAILED, call));
    }

    void dispatchRetry(Call call) {
        handler.sendMessage(handler.obtainMessage(CALL_RETRY, call));
    }

    void dispatchPauseTag(Object tag) {
        handler.sendMessage(handler.obtainMessage(TAG_PAUSE, tag));
    }

    void dispatchResumeTag(Object tag) {
        handler.sendMessage(handler.obtainMessage(TAG_RESUME, tag));
    }

    private void performSubmit(Action action) {
        if (pausedTags.contains(action.tag)) {
            pausedActions.put(action.target(), action);
            return;
        }
        Call call = callMap.get(action.key);
        if (call != null) {
            call.attach(action);
        } else {
            call = new Call(vanGogh, action);
            callMap.put(action.key, call);
            call.future = executor.submit(call);
        }
    }

    private void performCancel(Action action) {
        final String key = action.key;
        Call call = callMap.get(key);
        if (call != null) {
            call.detach(action);
            if (call.tryCancel()) {
                callMap.remove(key);
            }
        }
        if (pausedTags.contains(action.tag)) {
            pausedActions.remove(action.target());
        }
    }

    private void performComplete(Call call) {
        callMap.remove(call.key());
        batch(call);
    }

    private void performError(Call call) {
        callMap.remove(call.key());
        batch(call);
    }

    private void performRetry(Call call) {
        if (call.isCanceled()) {
            return;
        }
        call.future = executor.submit(call);
    }

    private void performPauseTag(Object tag) {
        if (!pausedTags.add(tag)) {
            return;
        }
        Iterator<Call> iterator = callMap.values().iterator();
        while (iterator.hasNext()) {
            Call call = iterator.next();
            if (call.actions.isEmpty()) {
                continue;
            }
            for (int i = 0, size = call.actions.size(); i < size; ++i) {
                Action action = call.actions.get(i);
                if (!action.tag.equals(tag)) {
                    continue;
                }
                call.detach(action);
                pausedActions.put(action.target(), action);
            }
            if (call.tryCancel()) {
                iterator.remove();
            }
        }
    }

    private void performResumeTag(Object tag) {
        if (!pausedTags.remove(tag)) {
            return;
        }
        List<Action> resumed = null;
        for (Iterator<Action> i = pausedActions.values().iterator(); i.hasNext(); ) {
            Action action = i.next();
            if (action.tag.equals(tag)) {
                if (resumed == null) {
                    resumed = new ArrayList<>();
                }
                resumed.add(action);
                i.remove();
            }
        }
        if (resumed != null) {
            mainHandler.sendMessage(mainHandler.obtainMessage(VanGogh.ACTION_BATCH_RESUME, resumed));
        }
    }

    private void performBatchComplete() {
        List<Call> copy = new ArrayList<>(batch);
        batch.clear();
        mainHandler.sendMessage(mainHandler.obtainMessage(VanGogh.CALL_BATCH_COMPLETE, copy));
    }

    private void batch(Call call) {
        if (call.isCanceled()) {
            return;
        }
        if (call.bitmap != null) {
            call.bitmap.prepareToDraw();
        }
        batch.add(call);
        if (!handler.hasMessages(CALL_DELAY_NEXT_BATCH)) {
            handler.sendEmptyMessageDelayed(CALL_DELAY_NEXT_BATCH, 200);
        }
    }


    private static class DispatcherThread extends HandlerThread {
        DispatcherThread() {
            super("Dispatcher", Process.THREAD_PRIORITY_BACKGROUND);
        }
    }


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
                    Action action = (Action) msg.obj;
                    dispatcher.performSubmit(action);
                    break;
                }
                case ACTION_CANCEL: {
                    Action action = (Action) msg.obj;
                    dispatcher.performCancel(action);
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
