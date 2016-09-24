package com.sunny.leaveme.activities;

import android.content.pm.PackageInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.sunny.leaveme.R;

import java.util.List;

public class WhitelistActivity extends AppCompatActivity {

    private final static String TAG = "WhitelistActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whitelist);

        updateUninstalledPackages();
    }

    private void updateUninstalledPackages() {
        List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packages.size(); ++i) {
            PackageInfo packageInfo = packages.get(i);
            Log.d(TAG, "packageInfo.packageName == " + packageInfo.packageName);
        }
    }
}
