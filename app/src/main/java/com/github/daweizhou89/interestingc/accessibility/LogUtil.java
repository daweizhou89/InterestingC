package com.github.daweizhou89.interestingc.accessibility;

public class LogUtil {
	
    /** Tag for logging. */
    private static final String LOG_TAG = "accessibility";
	
    public static void v(String msg) {
    	android.util.Log.v(LOG_TAG, msg);
    }
    
    public static void e(String msg, Throwable throwable) {
    	android.util.Log.e(LOG_TAG, msg, throwable);
    }
    
    public static void e(Throwable throwable) {
    	e(null, throwable);
    }
}
