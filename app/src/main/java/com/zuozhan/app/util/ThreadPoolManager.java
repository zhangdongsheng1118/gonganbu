package com.zuozhan.app.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** 线程池 工具类 */
public class ThreadPoolManager {

    private static Handler mHandler = new Handler(Looper.getMainLooper());

    // Access net, File I/O
    private static Executor mTimeConsumingTaskExecutor =
            new ThreadPoolExecutor(
                    0, 3, 20L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());


    // 单线程池
    private static Executor mFIFOTaskExecutor = Executors.newSingleThreadExecutor();

    public static void runSubThread(Runnable task) {
        if (task == null) return;
        mTimeConsumingTaskExecutor.execute(task);
    }

    public static void runSubFIFOThread(Runnable task) {
        if (task == null) return;
        mFIFOTaskExecutor.execute(task);
    }

    public static void runOnUiThread(Runnable action) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            action.run();
        } else {
            mHandler.post(action);
        }
    }

    public static void postOnUiThread(Runnable action, long delayTime) {
        mHandler.postDelayed(action, delayTime);
    }
}
