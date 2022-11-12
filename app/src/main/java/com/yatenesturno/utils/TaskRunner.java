package com.yatenesturno.utils;

import android.os.Handler;
import android.os.Looper;

import com.yatenesturno.R;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Asynk task executor thread for fetching data
 */
public class TaskRunner {
    private final Executor executor =
            new ThreadPoolExecutor(5, 128, 1,
                    TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    private final Handler handler = new Handler(Looper.getMainLooper());

    public <E> void executeAsync(Callable<E> callable, Callback<E> callback) {
        executor.execute(() -> {
            final E result;
            try {
                result = callable.call();
                handler.post(() -> callback.onComplete(result));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public interface Callback<R> {
        void onComplete(R result);
    }
}