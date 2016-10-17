package com.github.daweizhou89.interestingc.accessibility;

public interface Constant {

	/** Action */
	public static final String ACTION_FORCE_STOP_REQUEST = "com.android.accessibilityservice.FORCE_STOP_REQUEST";
	
	/** Action */
	public static final String ACTION_FORCE_STOP_FINISHED = "com.android.accessibilityservice.ACTION_FORCE_STOP_FINISHED";
	
	/** Extra */
	public static final String EXTRA_ACTION = "action";
	
	/** 关闭单个 */
	public static final int ACTION_REQUEST_FORCE_STOP = 1 << 0;
	
	/** Extra */
	public static final String EXTRA_PACKAGE_NAMES = "package_names";
	
	/** VERSION CODE: JELLY_BEAN */
	public static final int BUILD_VERSION_CODES_JELLY_BEAN = 16;
	
	/** VERSION CODE: JELLY_BEAN_MR2 */
	public static final int BUILD_VERSION_CODES_JELLY_BEAN_MR2 = 18;
	
	/**  */
	public static final long TIME_OUT_PERFORM_CLICK = 1500;
	
	/** task to ignore */
	public static final String[] IGNORE_PROCESSES_LIST = new String[] {
		"system",
		"com.google.process.gapps",
		"com.google.android.gms"
	};
	
	/**  */
	public static final String FORCE_STOP_STRING_RES_NAME = "com.android.settings:id/force_stop_button";

	/**  */
	public static final String FORCE_STOP_STRING_LEFT_BOTTON = "com.android.settings:id/left_button";

	/**  */
	public static final String FORCE_STOP_STRING_RIGHT_BOTTON = "com.android.settings:id/right_button";
	
	/**  */
	public static final String OK_STRING_RES_NAME = "android:id/button1";
}
