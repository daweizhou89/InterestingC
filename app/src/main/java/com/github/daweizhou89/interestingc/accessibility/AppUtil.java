package com.github.daweizhou89.interestingc.accessibility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

public class AppUtil {

	/** max task count */
	private static final int MAX_TASK_COUNT = 200;
	/** max service count */
	private static final int MAX_SERVICE_COUNT = 100;
	
	/***
	 * Get all informations of running application. 
	 * @param context
	 * @return
	 */
	public static HashMap<String, RunningAppInfo> getRunningAppInfoMap(Context context, boolean onlyBackground) {
		final HashMap<String, RunningAppInfo> runningAppInfoMap = new HashMap<String, RunningAppInfo>();
		final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		
		// running services
		final List<RunningServiceInfo> runningServiceInfos = activityManager.getRunningServices(MAX_SERVICE_COUNT);
		for (RunningServiceInfo runningServiceInfo : runningServiceInfos) {
			ComponentName componentName = runningServiceInfo.service;
			final String packageName = componentName != null ? componentName.getPackageName() : null;
			if (packageName != null && !runningAppInfoMap.containsKey(packageName)) {
				runningAppInfoMap.put(packageName, new RunningAppInfo(packageName, true, isSystemApp(context, packageName)));
			}
		}
		
		if (!onlyBackground) {
			// running tasks
			final List<RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(MAX_TASK_COUNT);
			for (RunningTaskInfo runningTaskInfo : runningTaskInfos) {
				ComponentName componentName = runningTaskInfo.topActivity;
				final String packageName = componentName != null ? componentName.getPackageName() : null;
				if (packageName != null && !runningAppInfoMap.containsKey(packageName)) {
					runningAppInfoMap.put(packageName, new RunningAppInfo(packageName, false, isSystemApp(context, packageName)));
				}
			}
		}
		
		return runningAppInfoMap;
	}
	
	/***
	 * Get all informations of running application. 
	 * @param context
	 * @return
	 */
	public static List<RunningAppInfo> getRunningAppInfoList(Context context, boolean onlyBackground) {
		final List<RunningAppInfo> runningAppInfoList = new ArrayList<RunningAppInfo>();
		final HashMap<String, RunningAppInfo> runningAppInfoMap = getRunningAppInfoMap(context, onlyBackground);
		runningAppInfoList.addAll(runningAppInfoMap.values());
		return runningAppInfoList;
	}
	
	/***
	 * Running application's information.
	 * @author zhoudawei
	 *
	 */
	public static class RunningAppInfo {
		
		/** 包名 */
		public String packageName;
		/** 是否后台运行程序 */
		public boolean isRunningService;
		/** 是否系统应用 */
		public boolean isSystemApp;
		
		public RunningAppInfo(String packageName, boolean isRunningService) {
			super();
			this.packageName = packageName;
			this.isRunningService = isRunningService;
		}
		
		public RunningAppInfo(String packageName, boolean isRunningService, boolean isSystemApp) {
			super();
			this.packageName = packageName;
			this.isRunningService = isRunningService;
			this.isSystemApp = isSystemApp;
		}
		
	}
	
	
	public static boolean isSystemApp(Context context, String pkgName) {
		if (pkgName != null && context != null) {
			PackageInfo appPackageInfo = getAppPackageInfo(context, pkgName);
			if (appPackageInfo != null && appPackageInfo.applicationInfo != null) {
				ApplicationInfo applicationInfo = appPackageInfo.applicationInfo;
				return ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
						|| ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
			}
		}
		return false;
	}
	
	public static PackageInfo getAppPackageInfo(final Context context,
			final String packageName) {
		PackageInfo info = null;
		try {
			info = context.getPackageManager().getPackageInfo(packageName, 0);
		} catch (Exception e) {
			info = null;
			LogUtil.e(e);
		}
		return info;
	}
	
	/***
	 * Check the application running a service in background.
	 * @param context
	 * @param checkedPackageName
	 * @return
	 */
	public static boolean isRunningService(Context context, String checkedPackageName) {
		boolean runningService = false;
		final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		final List<RunningServiceInfo> runningServiceInfos = activityManager.getRunningServices(MAX_SERVICE_COUNT);
		for (RunningServiceInfo runningServiceInfo : runningServiceInfos) {
			ComponentName componentName = runningServiceInfo.service;
			final String packageName = componentName != null ? componentName.getPackageName() : null;
			if (packageName != null && packageName.equals(checkedPackageName)) {
				runningService = true;
				break;
			}
		}
		return runningService;
	}
}
