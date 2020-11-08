package com.postpc.elhalso.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Utility class to manage multi-threading operations
 */
public class ThreadingHelper {
    private static Executor bgExecutor = Executors.newCachedThreadPool();

    public static void runAsyncInBackground(Runnable block) {
        bgExecutor.execute(block);
    }

    public static void runAsyncInMainThread(Runnable block) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(block);
    }
}