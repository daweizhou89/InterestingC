package com.github.daweizhou89.interestingc.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.github.daweizhou89.interestingc.R;
import com.github.daweizhou89.interestingc.accessibility.AppUtil;
import com.github.daweizhou89.interestingc.accessibility.Constant;
import com.github.daweizhou89.interestingc.accessibility.IntentUtil;
import com.github.daweizhou89.interestingc.adapter.AppListAdapter;
import com.github.daweizhou89.interestingc.databinding.ActivityRunningAppListBinding;

import java.util.List;

public class RunningAppListActivity extends BaseActivity implements View.OnClickListener {

    private AppListAdapter mAppListAdapter;

    ActivityRunningAppListBinding mActivityBinding;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.ACTION_FORCE_STOP_FINISHED.equals(intent.getAction())) {
                changeState("清理完成");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityBinding = DataBindingUtil.setContentView(this, R.layout.activity_running_app_list);
        initView();
        IntentFilter intentFilter = new IntentFilter(Constant.ACTION_FORCE_STOP_FINISHED);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void initView() {
        initRecycleView();
        mActivityBinding.stateText.setOnClickListener(this);
        mActivityBinding.cleanOk.setOnClickListener(this);
    }

    private void initRecycleView() {
        mActivityBinding.swipeRefreshLayout.setColorSchemeResources(R.color.swipe_color_1,
                R.color.swipe_color_2,
                R.color.swipe_color_3,
                R.color.swipe_color_4);
        mActivityBinding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tryLoadTasks();
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mActivityBinding.appList.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(Constant.ACTION_FORCE_STOP_REQUEST);
        intent.putExtra(Constant.EXTRA_PACKAGE_NAMES, new String[0]);
        sendBroadcast(intent);
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    private void tryLoadTasks() {
        if (AppUtil.isRunningService(this, getPackageName())) {
            changeState("加载中...");
            mActivityBinding.stateText.setOnClickListener(null);
            new MyAsyncTask().execute();
        } else {
            IntentUtil.startAccessibilitySettings(this);
        }
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.state_text:
                tryLoadTasks();
                break;
            case R.id.clean_ok:
                String[] packages = mAppListAdapter.getSelectedPackages();
                if (packages != null) {
                    mActivityBinding.appListContainer.setVisibility(View.INVISIBLE);
                    changeState("清理中");
                    IntentUtil.sendForceStopBroadcast(this, packages);
                } else {
                    Toast.makeText(this, "勾选清理项...", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void changeState(String state) {
        if (TextUtils.isEmpty(state)) {
            mActivityBinding.stateText.setVisibility(View.GONE);
        } else {
            mActivityBinding.stateText.setVisibility(View.VISIBLE);
            mActivityBinding.stateText.setOnClickListener(this);
            mActivityBinding.stateText.setText(state);
        }
    }

    private class MyAsyncTask extends AsyncTask<Void, Void, List<AppUtil.RunningAppInfo>> {

        @Override
        protected List<AppUtil.RunningAppInfo> doInBackground(Void... params) {
            List<AppUtil.RunningAppInfo> appInfos = AppUtil.getRunningAppInfoList(getApplicationContext(), true);
            if (appInfos.size() > 0) {
                for (int i = appInfos.size() - 1; i >= 0; i--) {
                    if (getPackageName().equals(appInfos.get(i).packageName)) {
                        appInfos.remove(i);
                        continue;
                    }
                    if (AppUtil.isSystemApp(getApplicationContext(), appInfos.get(i).packageName)) {
                        appInfos.remove(i);
                        continue;
                    }
                }
            }
            return appInfos;
        }

        @Override
        protected void onPostExecute(List<AppUtil.RunningAppInfo> result) {

            if (result == null || result.isEmpty()) {
                changeState("无后台程序可清理...");
                return;
            }

            mAppListAdapter = new AppListAdapter(getApplicationContext(), result);
            mAppListAdapter.selectAll();
            mActivityBinding.appList.setAdapter(mAppListAdapter);
            mActivityBinding.appListContainer.setVisibility(View.VISIBLE);
            mActivityBinding.swipeRefreshLayout.setRefreshing(false);
        }
    }

}
