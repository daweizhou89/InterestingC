package com.github.daweizhou89.interestingc.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.daweizhou89.interestingc.accessibility.AppUtil;
import com.github.daweizhou89.interestingc.accessibility.LogUtil;
import com.github.daweizhou89.interestingc.databinding.AppInfoItemBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.github.daweizhou89.interestingc.R;

/**
 *
 * Created by zhoudawei on 16/8/2.
 */
public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppInfoItemVH> {

    private List<AppUtil.RunningAppInfo> mRunningAppInfos;

    private HashSet<String> mSelectedApp = new HashSet<>();

    private LayoutInflater mLayoutInflater;

    private PackageManager mPackageManager;

    public AppListAdapter(Context context, List<AppUtil.RunningAppInfo> runningAppInfos) {
        mLayoutInflater = LayoutInflater.from(context);
        mRunningAppInfos = runningAppInfos;
        mPackageManager = context.getPackageManager();
    }

    public String[] getSelectedPackages() {
        String[] packages = null;
        final int selectedCount = mSelectedApp.size();
        if (selectedCount > 0) {
            packages = new String[selectedCount];
            int i = 0;
            for (String packageName : mSelectedApp) {
                packages[i] = packageName;
                ++i;
            }
        }
        return packages;
    }

    public void selectAll() {
        if (mRunningAppInfos == null) {
            return;
        }
        mSelectedApp.clear();
        for (AppUtil.RunningAppInfo appInfo : mRunningAppInfos) {
            mSelectedApp.add(appInfo.packageName);
        }
    }

    @Override
    public AppInfoItemVH onCreateViewHolder(ViewGroup parent, int viewType) {
        AppInfoItemBinding binding = DataBindingUtil.inflate(mLayoutInflater, R.layout.app_info_item, parent, false);
        return new AppInfoItemVH(binding);
    }

    @Override
    public void onBindViewHolder(AppInfoItemVH holder, int position) {
        holder.bindView(position);
    }

    @Override
    public int getItemCount() {
        if (mRunningAppInfos == null) {
            return 0;
        }
        return mRunningAppInfos.size();
    }

    public class AppInfoItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        AppInfoItemBinding binding;

        public AppInfoItemVH(AppInfoItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
        }

        public void bindView(int position) {
            final String packageName = mRunningAppInfos.get(position).packageName;
            ApplicationInfo applicationInfo = null;
            try {
                applicationInfo = mPackageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e) {
                LogUtil.e(e);
            }
            if (applicationInfo != null) {
                binding.appIcon.setImageDrawable(applicationInfo.loadIcon(mPackageManager));
                binding.appName.setText(applicationInfo.loadLabel(mPackageManager));
            } else {
                binding.appIcon.setImageDrawable(null);
                binding.appName.setText(null);
            }
            binding.appSelected.setChecked(mSelectedApp.contains(packageName));
            binding.getRoot().setTag(R.id.tag_position, position);
        }

        @Override
        public void onClick(View v) {
            final int position = (Integer) v.getTag(R.id.tag_position);
            AppUtil.RunningAppInfo appInfo = mRunningAppInfos.get(position);
            if (mSelectedApp.contains(appInfo.packageName)) {
                mSelectedApp.remove(appInfo.packageName);
            } else {
                mSelectedApp.add(appInfo.packageName);
            }
            notifyItemChanged(position);
        }

    }

}
