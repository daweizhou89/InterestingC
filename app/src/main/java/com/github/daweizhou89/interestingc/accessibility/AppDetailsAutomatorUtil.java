package com.github.daweizhou89.interestingc.accessibility;

import java.util.List;


import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;

public class AppDetailsAutomatorUtil {
	
	/***
	 * Find a matched node by tracing the tree of nodes.
	 * @param source
	 * @param text
	 * @return
	 */
	public static AccessibilityNodeInfo findAccessibilityNodeInfo(AccessibilityNodeInfo source, String text) {
		AccessibilityNodeInfo accessibilityNodeInfo = null;
		if (TextUtils.isEmpty(text)) {
			return accessibilityNodeInfo;
		}
		
		for (int i = 0; i < source.getChildCount(); i++) {
			AccessibilityNodeInfo compareNode = source.getChild(i);
			if (compareNode != null && compareNode.getText() != null) {
				
				LogUtil.v("(findAccessibilityNodeInfo) completeNode : " + compareNode.getClassName() + ", " + compareNode.getText());
				
				if (text.equals(compareNode.getText())) {
					LogUtil.v("(findAccessibilityNodeInfo) Find node : " + compareNode.getClassName() + ", " + compareNode.getText());
					accessibilityNodeInfo = compareNode;
				}
				if (accessibilityNodeInfo == null) {
					accessibilityNodeInfo = findAccessibilityNodeInfo(compareNode, text);
				}
				if (accessibilityNodeInfo == null) {
					compareNode.recycle();
				} else {
					break;
				}
			}
		}
		
		return accessibilityNodeInfo;
	}
	
	/***
	 * Find a matched node by the method, {@link AccessibilityNodeInfo#findAccessibilityNodeInfosByViewId}.
	 * @param accessibilityNodeInfo
	 * @param stringResName
	 * @return
	 */
	@TargetApi(Constant.BUILD_VERSION_CODES_JELLY_BEAN_MR2)
	public static AccessibilityNodeInfo getAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo, String stringResName) {
		AccessibilityNodeInfo foundNodeInfo = null;
		List<AccessibilityNodeInfo> foundNodeInfos;
		if (Build.VERSION.SDK_INT >= Constant.BUILD_VERSION_CODES_JELLY_BEAN_MR2) {
			LogUtil.v("(getAccessibilityNodeInfo) Searching for " + stringResName);
			foundNodeInfos = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(stringResName);
			if ((foundNodeInfos != null) && (!foundNodeInfos.isEmpty())) {
				foundNodeInfo = (AccessibilityNodeInfo)foundNodeInfos.get(0);
				LogUtil.v("(getAccessibilityNodeInfo) Found " + foundNodeInfos.size() + ", [0] " + foundNodeInfo.getText());
			} else {
				LogUtil.v("(getAccessibilityNodeInfo) Not found : " + stringResName);
			}
			while (foundNodeInfos.size() > 1) {
				((AccessibilityNodeInfo)foundNodeInfos.remove(-1 + foundNodeInfos.size())).recycle();
			}
		}
		return foundNodeInfo;
	}
	
	/***
	 * Check the node whether it is visible.
	 * @param accessibilityNodeInfo
	 * @return
	 */
    @TargetApi(Constant.BUILD_VERSION_CODES_JELLY_BEAN)
    public static boolean isVisibleToUser(AccessibilityNodeInfo accessibilityNodeInfo) {
    	if (accessibilityNodeInfo == null) {
    		return false;
    	}
      return accessibilityNodeInfo.isEnabled() && accessibilityNodeInfo.isVisibleToUser();
    }
    
    /***
     * 
     * @param accessibilityNodeInfo
     * @return
     */
    @TargetApi(Constant.BUILD_VERSION_CODES_JELLY_BEAN)
    public static boolean performClickAction(AccessibilityNodeInfo accessibilityNodeInfo) {
    	if (!isVisibleToUser(accessibilityNodeInfo)) {
    		return false;
    	}
    	CharSequence nodeInfoText = accessibilityNodeInfo.getText();
    	if (!accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
    		LogUtil.v("(performAction) Failed to click " + nodeInfoText);
    		return false;
    	}

    	LogUtil.v("(performAction) Clicked " + nodeInfoText);
    	return true;
    }
    
    public static void checkVisibleToUserTimeOut (AccessibilityNodeInfo accessibilityNodeInfo) {
    	final long currentTime = System.currentTimeMillis();
		while (!isVisibleToUser(accessibilityNodeInfo) && System.currentTimeMillis() - currentTime < Constant.TIME_OUT_PERFORM_CLICK) {
			// check node's visibility by time out
		}
    }

	public static void checkInvisibleToUserTimeOut (AccessibilityNodeInfo accessibilityNodeInfo) {
		final long currentTime = System.currentTimeMillis();
		while (isVisibleToUser(accessibilityNodeInfo) && System.currentTimeMillis() - currentTime < Constant.TIME_OUT_PERFORM_CLICK) {
			// check node's visibility by time out
		}
	}
	
    /***
     * Check the current system version whether support the function of automator.
     * @return
     */
    public static final boolean isSystemSupportAutomator() {
    	return Build.VERSION.SDK_INT >= Constant.BUILD_VERSION_CODES_JELLY_BEAN;
    }
    
    /***
     * Check 
     * @param context
     * @param processName
     * @return
     */
    public static boolean ignoreTask(Context context, String processName) {
		for (String systemimPortanceProcesses : Constant.IGNORE_PROCESSES_LIST) {
			if (systemimPortanceProcesses.equals(processName)) {
				return true;
			}
		}
		if (processName != null && processName.startsWith(context.getPackageName())) {
			return true;
		}
		return false;
	}
    
    public static ComponentName getSettingsComponentName(Context context) {
		return getComponentName(context, new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.fromParts("package", context.getPackageName(), null)));
	}

	public static ComponentName getComponentName(Context context, Intent intent) {
		PackageManager packageManager = context.getPackageManager();
		ComponentName resolvedComponentName = intent.resolveActivity(packageManager);
		try {
			ActivityInfo activityInfo = packageManager.getActivityInfo(resolvedComponentName, 0);
			if (activityInfo.targetActivity != null) {
				return new ComponentName(resolvedComponentName.getPackageName(), activityInfo.targetActivity);
			}
		} catch (PackageManager.NameNotFoundException e) {
			// TODO nothing
		}
		return resolvedComponentName;
	}
}
