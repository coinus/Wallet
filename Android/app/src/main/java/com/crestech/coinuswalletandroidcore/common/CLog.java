package com.crestech.coinuswalletandroidcore.common;

import android.util.Log;

public class CLog {

    static final String TAG = "com.crestech.coinuswalletandroidcore";
    static boolean DEBUG_MODE = true;

    /**
     * Log Level Error
     * @param message
     */
    public static void e(String message) {
        if (DEBUG_MODE) { Log.e(TAG, buildLogMsg(message)); }
    }

    public static void w(String message) {
        if (DEBUG_MODE) { Log.w(TAG, buildLogMsg(message)); }
    }

    public static void i(String message) {
        if (DEBUG_MODE) { Log.i(TAG, buildLogMsg(message)); }
    }

    public static void d(String message) {
        if (DEBUG_MODE) { Log.d(TAG, buildLogMsg(message)); }
    }

    public static void v(String message) {
        if (DEBUG_MODE) { Log.v(TAG, buildLogMsg(message)); }
    }


    private static String buildLogMsg(String message) {

        StackTraceElement ste = Thread.currentThread().getStackTrace()[4];

        StringBuilder sb = new StringBuilder();

        sb.append("[");
        sb.append(ste.getFileName().replace(".java", ""));
        sb.append("::");
        sb.append(ste.getMethodName());
        sb.append("] ");
        sb.append("(");
        sb.append(Thread.currentThread().getName());
        sb.append(") ");
        sb.append(message);

        return sb.toString();
    }
}
