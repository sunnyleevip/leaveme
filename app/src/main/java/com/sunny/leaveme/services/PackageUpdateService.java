package com.sunny.leaveme.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.sunny.leaveme.ActionStr;
import com.sunny.leaveme.db.DataHelper;
import com.sunny.leaveme.db.entity.WhitelistItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.pm.PackageManager.GET_ACTIVITIES;

public class PackageUpdateService extends Service implements Runnable {
    private static final String TAG = "PackageUpdateService";
    private Context mContext;
    private PackageUpdateReceiver mPackageUpdateReceiver;
    private LocalBroadcastManager mLocalBroadcastManager;

    @Override
    public void onCreate() {
        mContext = this;

        mPackageUpdateReceiver = new PackageUpdateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addAction(Intent.ACTION_PACKAGE_RESTARTED);
        filter.addAction(Intent.ACTION_PACKAGE_DATA_CLEARED);
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        this.registerReceiver(mPackageUpdateReceiver, filter);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActionStr.ACTION_START_MONITOR);
        intentFilter.addAction(ActionStr.ACTION_STOP_MONITOR);
        intentFilter.addAction(ActionStr.ACTION_UPDATE_LIGHT_SWITCH_VALUE);
        mLocalBroadcastManager.registerReceiver(mLocalBroadcastReceiver, intentFilter);

        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "PackageUpdateService onDestroy");
        mLocalBroadcastManager.unregisterReceiver(mLocalBroadcastReceiver);
        unregisterReceiver(mPackageUpdateReceiver);
    }

    @Override
    public void run() {
        updateUninstalledPackages();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class PackageUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                Intent i= new Intent(context, PackageUpdateService.class);
                context.startService(i);
            }

            if ((intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED))
                    || (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED))
                    || (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED))
                    || (intent.getAction().equals(Intent.ACTION_PACKAGE_CHANGED))
                    || (intent.getAction().equals(Intent.ACTION_PACKAGE_DATA_CLEARED))
                    || (intent.getAction().equals(Intent.ACTION_PACKAGE_RESTARTED))) {
                Log.d(TAG, "Package changed");

                Thread thread = new Thread((PackageUpdateService)mContext);
                thread.start();
            }
        }
    }

    private BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "mLocalBroadcastReceiver.onReceive");
            if (intent.getAction().equals(ActionStr.ACTION_CHECK_PACKAGE_UPDATE)) {
                Thread thread = new Thread((PackageUpdateService)mContext);
                thread.start();
            }
        }
    };

    private void updateUninstalledPackages() {
        DataHelper dataHelper = new DataHelper(mContext);
        PackageManager pm = mContext.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm
                .queryIntentActivities(mainIntent, PackageManager.MATCH_DEFAULT_ONLY);
        Collections.sort(resolveInfos,new ResolveInfo.DisplayNameComparator(pm));

        ArrayList<WhitelistItem> whitelistItems = dataHelper.getAllWhitelistItems();
        ArrayList<WhitelistItem> newWhitelistItems = new ArrayList<>();

        for (ResolveInfo resolveInfo : resolveInfos) {
            String activityName = resolveInfo.activityInfo.name;
            String pkgName = resolveInfo.activityInfo.packageName;
            String appLabel = (String) resolveInfo.loadLabel(pm);
            Drawable icon = resolveInfo.loadIcon(pm);
            Intent launchIntent = new Intent();
            launchIntent.setComponent(new ComponentName(pkgName,
                    activityName));
            Log.d(TAG, appLabel + " activityName---" + activityName
                    + " pkgName---" + pkgName);

            boolean isExist = false;
            for (WhitelistItem whitelistItem : whitelistItems) {
                if (whitelistItem.getAppName().equals(pkgName)) {
                    isExist = true;
                    break;
                }
            }

            if (!isExist) {
                WhitelistItem whitelistItem = new WhitelistItem();
                whitelistItem.setAppName(pkgName);
                newWhitelistItems.add(whitelistItem);
            }
        }

        if (newWhitelistItems.size() > 0) {
            dataHelper.insertWhitelistItems(newWhitelistItems);
        }

        dataHelper.close();
    }
}
