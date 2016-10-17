package com.github.daweizhou89.interestingc.accessibility;

import java.util.LinkedList;
import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/***
 * 
 * @author zhoudawei
 *
 */
public class AppDetailsAutomatorManager {

	/**  */
	private static final int MSG_PERFORM_STOP = 0x1;
	
	/**  */
	private static ComponentName sSettingsComponentName;
	
	/**  */
	private Context mContext;
	
	/** text: com.android.settings:string/finish_application */
	private String mFinishApplication;
	/** text: com.android.settings:string/force_stop */
	private String mForceStop;
	/** text: com.android.settings:string/clear_user_data_text */
	private String mClearUserDataText;
	/** text: com.android.settings:string/uninstall_text */
	private String mUninstallText;
	/** text: com.android.settings:string/dlg_ok */
	private String mDlgOk;
	/** text: com.android.settings:string/dlg_cancel */
	private String mDlgCancel;
	
    /** 包名 */
    LinkedList<String> mPackageNames = new LinkedList<String>();
	
	/**  */
	private boolean mForceStopRequested;
	
	/**  */
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_PERFORM_STOP:
				if (mPackageNames.isEmpty()) {
					forceStopFinished();
					return;
				}
				String packageName = mPackageNames.poll();
				IntentUtil.startApplicationDetailsSettings(mContext, packageName);
				break;
			}
		}
	};
	
	public AppDetailsAutomatorManager(Context context) {
		mContext = context;
		sSettingsComponentName = AppDetailsAutomatorUtil.getSettingsComponentName(context);
		initSettingsString();
	}
	
	public void initSettingsString() {
		mFinishApplication = getSettingsString("finish_application");
		mForceStop = getSettingsString("force_stop");
		mClearUserDataText = getSettingsString("clear_user_data_text");
		mUninstallText = getSettingsString("uninstall_text");
		mDlgOk = getSettingsString("dlg_ok");
		mDlgCancel = getSettingsString("dlg_cancel");
	}
	
	public boolean isForceStopRequested() {
		return mForceStopRequested;
	}
	
	public void onAccessibilityEvent(AccessibilityEvent event) {
		
		if (sSettingsComponentName == null || !isSettings(event)) {
			LogUtil.v("No Settings application installed.");
			return;
		}

		AccessibilityNodeInfo source = event.getSource();
		if (source == null) {
			LogUtil.v("source = null");
			return;
		}
		
		LogUtil.v("source : " + source.getClassName() + ", " + source.getText());
		
		try {
			if (isAppDetail(event)) {
				handleAppDetail(source);
			} else if (isAlertDialog(event)) {
				handleAlertDialog(source);
			}
		} finally {
			source.recycle();
		}
	}
	
	private boolean isAlertDialog(AccessibilityEvent event) {
		return "android.app.AlertDialog".equals(event.getClassName()) || event.getClassName().toString().endsWith("AlertDialog");
	}
	
	private boolean isAppDetail(AccessibilityEvent event) {
		return sSettingsComponentName.getClassName().equals(event.getClassName());
	}
	
	private boolean isSettings(AccessibilityEvent event) {
		return sSettingsComponentName.getPackageName().equals(event.getPackageName());
	}
	
	private void handleAppDetail(AccessibilityNodeInfo source) {
		AccessibilityNodeInfo forceStopNodeInfo = null;
		forceStopNodeInfo = AppDetailsAutomatorUtil.getAccessibilityNodeInfo(source, Constant.FORCE_STOP_STRING_RES_NAME);
		if (forceStopNodeInfo == null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				forceStopNodeInfo = AppDetailsAutomatorUtil.getAccessibilityNodeInfo(source, Constant.FORCE_STOP_STRING_RIGHT_BOTTON);
			} else {
				forceStopNodeInfo = AppDetailsAutomatorUtil.getAccessibilityNodeInfo(source, Constant.FORCE_STOP_STRING_LEFT_BOTTON);
			}
		}
		if (forceStopNodeInfo == null) {
			forceStopNodeInfo = AppDetailsAutomatorUtil.findAccessibilityNodeInfo(source, mForceStop);
		}
		if (forceStopNodeInfo == null) {
			forceStopNodeInfo = AppDetailsAutomatorUtil.findAccessibilityNodeInfo(source, mFinishApplication);
		}
		boolean performClick = false;
		if (forceStopNodeInfo != null) {
			LogUtil.v("forceStopNodeInfo : " + forceStopNodeInfo.getText());
			AppDetailsAutomatorUtil.checkVisibleToUserTimeOut(forceStopNodeInfo);
			if (AppDetailsAutomatorUtil.isVisibleToUser(forceStopNodeInfo)) {
				AppDetailsAutomatorUtil.performClickAction(forceStopNodeInfo);
				performClick = true;
			}
			forceStopNodeInfo.recycle();
		}
		
		if (!performClick) {
			// 队列为空则返回
			if (mPackageNames.isEmpty()) {
				LogUtil.v("AccessibilityService.GLOBAL_ACTION_BACK");
				source.performAction(AccessibilityService.GLOBAL_ACTION_BACK);
			}
			// 关闭设置项界面
			mHandler.sendEmptyMessage(MSG_PERFORM_STOP);
		}
		
	}
	
	private void handleAlertDialog(AccessibilityNodeInfo source) {
		AccessibilityNodeInfo okNodeInfo = null;
		okNodeInfo = AppDetailsAutomatorUtil.getAccessibilityNodeInfo(source, Constant.OK_STRING_RES_NAME);
		if (okNodeInfo == null) {
			okNodeInfo = AppDetailsAutomatorUtil.findAccessibilityNodeInfo(source, mDlgOk);
		}
		if (okNodeInfo != null) {
			LogUtil.v("okNodeInfo : " + okNodeInfo.getText());
			AppDetailsAutomatorUtil.checkVisibleToUserTimeOut(okNodeInfo);
			AppDetailsAutomatorUtil.performClickAction(okNodeInfo);
			AppDetailsAutomatorUtil.checkInvisibleToUserTimeOut(okNodeInfo);
			okNodeInfo.recycle();
		}
	}
	
	private String getSettingsString(String stringResName) {
		LogUtil.v("(getSettingsString) Get text of " + stringResName);
		String stringRes = null;
		try {
			final String settingsPackageName = sSettingsComponentName.getPackageName();
			final Resources resources = mContext.getPackageManager().getResourcesForApplication(settingsPackageName);
			int stringResId = resources.getIdentifier(stringResName, "string", settingsPackageName);
			if (stringResId > 0) {
				String str = resources.getString(stringResId);
				stringRes = str;
			} else {
				LogUtil.v("(getSettingsString) Not found : " + stringResName);
			}
		} catch (Exception e) {
			// TODO nothing
		}
		LogUtil.v(stringResName + " : " + stringRes);
		return stringRes;
	}
    
    public void setPackageNames(String[] packageNames) {
    	mPackageNames.clear();
    	if (packageNames != null && packageNames.length > 0) {
    		for (String packageName : packageNames) {
    			mPackageNames.add(packageName);
    		}
		}
    	requestForceStop();
    }
    
	private void requestForceStop() {
		mForceStopRequested = true;
		mHandler.sendEmptyMessage(MSG_PERFORM_STOP);
	}
	
	private void forceStopFinished() {
		mForceStopRequested = false;
		Intent intent = new Intent(Constant.ACTION_FORCE_STOP_FINISHED);
		mContext.sendBroadcast(intent);
		// 关闭设置窗口
	}
}
