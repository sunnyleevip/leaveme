package com.sunny.leaveme.activities;

import android.content.pm.PackageInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.sunny.leaveme.R;
import com.sunny.leaveme.activities.uihelper.ScheduleListAdapter;
import com.sunny.leaveme.activities.uihelper.WhitelistListAdapter;
import com.sunny.leaveme.db.DataHelper;
import com.sunny.leaveme.db.entity.WhitelistItem;

import java.util.ArrayList;
import java.util.List;

public class WhitelistActivity extends AppCompatActivity {

    private final static String TAG = "WhitelistActivity";

    private DataHelper mDataHelper;
    ArrayList<WhitelistItem> mWhitelistItems;
    private WhitelistListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whitelist);

        mDataHelper = new DataHelper(this);
        mWhitelistItems = mDataHelper.getAllWhitelistItems();
        updateUninstalledPackages();

        ListView listView = (ListView) findViewById(R.id.lvWhitelistItems);
        mAdapter = new WhitelistListAdapter(this,
                R.layout.listview_whitelist_row,
                mWhitelistItems,
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int position = (Integer)buttonView.getTag();
                        if (isChecked != mWhitelistItems.get(position).isAvailable()) {
                            Log.d(TAG, "onCheckedChanged at position: " + position + " checked: " + isChecked);
                            mWhitelistItems.get(position).setAvailable(isChecked);
                            mDataHelper.updateWhitelistItem(mWhitelistItems.get(position));
                        }
                    }
                });
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick pos:" + position + " id:" + id);
            }
        });
    }

    private void updateUninstalledPackages() {
        List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
        ArrayList<WhitelistItem> newWhitelistItems = new ArrayList<>();
        for (int i = 0; i < packages.size(); ++i) {
            PackageInfo packageInfo = packages.get(i);
            Log.d(TAG, "packageInfo.packageName: " + packageInfo.packageName);
            boolean isExist = false;
            for (WhitelistItem whitelistItem : mWhitelistItems) {
                if (whitelistItem.getAppName().equals(packageInfo.packageName)) {
                    isExist = true;
                    break;
                }
            }

            if (!isExist) {
                WhitelistItem whitelistItem = new WhitelistItem();
                whitelistItem.setAppName(packageInfo.packageName);
                Log.d(TAG, "packageInfo.packageName: " + packageInfo.packageName);
                Log.d(TAG, "whitelistItem.packageName: " + whitelistItem.getAppName());
                mWhitelistItems.add(whitelistItem);
                newWhitelistItems.add(whitelistItem);
            }
        }

        if (newWhitelistItems.size() > 0) {
            mDataHelper.insertWhitelistItems(newWhitelistItems);
        }
    }
}
