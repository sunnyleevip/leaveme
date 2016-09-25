package com.sunny.leaveme.activities;

import android.content.pm.PackageInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.sunny.leaveme.R;
import com.sunny.leaveme.db.DataHelper;
import com.sunny.leaveme.db.entity.WhitelistItem;

import java.util.ArrayList;
import java.util.List;

public class WhitelistActivity extends AppCompatActivity {

    private final static String TAG = "WhitelistActivity";

    private DataHelper mDataHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whitelist);

        mDataHelper = new DataHelper(this);
        updateUninstalledPackages();
    }

    private void updateUninstalledPackages() {
        ArrayList<WhitelistItem> whitelistItems = mDataHelper.getAllWhitelistItems();
        List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packages.size(); ++i) {
            PackageInfo packageInfo = packages.get(i);
            Log.d(TAG, "packageInfo.packageName == " + packageInfo.packageName);
        }
    }
}
