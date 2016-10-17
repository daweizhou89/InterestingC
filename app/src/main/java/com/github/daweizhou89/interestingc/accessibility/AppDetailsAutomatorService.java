/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.github.daweizhou89.interestingc.accessibility;


import java.util.LinkedList;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.view.accessibility.AccessibilityEvent;

/**
 * This class demonstrates how an accessibility service can query
 * window content to improve the feedback given to the user.
 */
public class AppDetailsAutomatorService extends AccessibilityService {
	
	/** 广播接收 */
    private BroadcastReceiver mBroadcastReceiver;
    
    /** 管理类 */
    private AppDetailsAutomatorManager mForceStopManager;
    
    /** 语言 */
    private String mCurrentLocale;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onServiceConnected() {
    	LogUtil.v("onServiceConnected");
    	registerBroadCastReceiver();
    	mForceStopManager = new AppDetailsAutomatorManager(getApplicationContext());
    	mCurrentLocale= getResources().getConfiguration().locale.toString();
    }
   
    /**
     * Processes an AccessibilityEvent, by traversing the View's tree.
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    	
    	if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
    		return;
    	}
    	
    	if (mForceStopManager.isForceStopRequested()) {
    		mForceStopManager.onAccessibilityEvent(event);
    	}
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	final String newLocale = newConfig.locale.toString();
    	LogUtil.v("(onConfigurationChanged) Locale : " + newLocale);
    	if (mCurrentLocale == null || !mCurrentLocale.equals(newLocale)) {
    		LogUtil.v("onConfigurationChanged - location");
    		mCurrentLocale = newLocale;
    		mForceStopManager.initSettingsString();
    	}
    	super.onConfigurationChanged(newConfig);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onInterrupt() {
        /* do nothing */
    	LogUtil.v("onInterrupt");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Registers the phone state observing broadcast receiver.
     */
    private void registerBroadCastReceiver() {
    	if (mBroadcastReceiver == null) {
    		mBroadcastReceiver = new BroadcastReceiver() {
    			
    			@Override
    			public void onReceive(Context context, Intent intent) {
    				String action = intent.getAction();
    				if (Intent.ACTION_SCREEN_ON.equals(action)) {
    					LogUtil.v("ACTION_SCREEN_ON");
    				} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
    					LogUtil.v("ACTION_SCREEN_OFF");
    				} else if (Constant.ACTION_FORCE_STOP_REQUEST.equals(action)) {
    					LogUtil.v("ACTION_REQUEST");
    					String[] packageNames = intent.getStringArrayExtra(Constant.EXTRA_PACKAGE_NAMES);
    					mForceStopManager.setPackageNames(packageNames);
    				}
    			}
    		};
    		// Create a filter with the broadcast intents we are interested in.
    		IntentFilter filter = new IntentFilter();
    		filter.addAction(Intent.ACTION_SCREEN_ON);
    		filter.addAction(Intent.ACTION_SCREEN_OFF);
    		filter.addAction(Constant.ACTION_FORCE_STOP_REQUEST);
    		// Register for broadcasts of interest.
    		registerReceiver(mBroadcastReceiver, filter, null, null);
    	}
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
    	LogUtil.v("onDestroy");
    	if (mBroadcastReceiver != null) {
    		unregisterReceiver(mBroadcastReceiver);
    		mBroadcastReceiver = null;
    	}
    	super.onDestroy();
    }
}
