package com.sunny.leaveme.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.sunny.leaveme.ActionStr;
import com.sunny.leaveme.db.DataHelper;
import com.sunny.leaveme.db.entity.WhitelistItem;

import java.util.ArrayList;
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
            }
        }
    }

    private BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "mLocalBroadcastReceiver.onReceive");
            if (intent.getAction().equals(ActionStr.ACTION_CHECK_PACKAGE_UPDATE)) {
                updateUninstalledPackages();
            }
        }
    };

    private void updateUninstalledPackages() {
        DataHelper dataHelper = new DataHelper(mContext);
        PackageManager pm = mContext.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        ArrayList<WhitelistItem> whitelistItems = dataHelper.getAllWhitelistItems();
        ArrayList<WhitelistItem> newWhitelistItems = new ArrayList<>();
        for (int i = 0; i < packages.size(); ++i) {
            PackageInfo packageInfo = packages.get(i);
            ActivityInfo[] activities = null;
            try {
                synchronized(PackageUpdateService.class) {
                    pm = mContext.getPackageManager();
                    activities = pm.getPackageInfo(packageInfo.packageName, GET_ACTIVITIES).activities;
                }
            } catch (PackageManager.NameNotFoundException ex) {
                Log.e(TAG, "Name not found: " + packageInfo.packageName);
                ex.printStackTrace();
            }

            if (activities != null) {
                //Log.d(TAG, "packageInfo.packageName: " + packageInfo.packageName);
                boolean isExist = false;
                for (WhitelistItem whitelistItem : whitelistItems) {
                    if (whitelistItem.getAppName().equals(packageInfo.packageName)) {
                        isExist = true;
                        break;
                    }
                }

                if (!isExist) {
                    WhitelistItem whitelistItem = new WhitelistItem();
                    whitelistItem.setAppName(packageInfo.packageName);
                    newWhitelistItems.add(whitelistItem);
                }
            }
        }

        if (newWhitelistItems.size() > 0) {
            dataHelper.insertWhitelistItems(newWhitelistItems);
        }

        dataHelper.close();
    }
}
