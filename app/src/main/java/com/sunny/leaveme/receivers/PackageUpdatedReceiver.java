package com.sunny.leaveme.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.util.Log;

import com.sunny.leaveme.ActionStr;
import com.sunny.leaveme.db.DataHelper;
import com.sunny.leaveme.db.entity.WhitelistItem;

import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.GET_ACTIVITIES;

public class PackageUpdatedReceiver extends BroadcastReceiver {
    private static final String TAG = "PackageUpdatedReceiver";

    public PackageUpdatedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ActionStr.ACTION_CHECK_PACKAGE_UPDATE)) {
            updateUninstalledPackages(context);
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

    private void updateUninstalledPackages(Context context) {
        DataHelper dataHelper = new DataHelper(context);
        List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(GET_ACTIVITIES);
        ArrayList<WhitelistItem> whitelistItems = dataHelper.getAllWhitelistItems();
        ArrayList<WhitelistItem> newWhitelistItems = new ArrayList<>();
        for (int i = 0; i < packages.size(); ++i) {
            PackageInfo packageInfo = packages.get(i);
            if (packageInfo.activities != null) {
                Log.d(TAG, "packageInfo.packageName: " + packageInfo.packageName);
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
