package com.sunny.leaveme.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sunny.leaveme.common.ActionStr;
import com.sunny.leaveme.R;
import com.sunny.leaveme.activities.uihelper.WhitelistListedAdapter;
import com.sunny.leaveme.db.DataHelper;
import com.sunny.leaveme.db.entity.WhitelistItem;

import java.util.ArrayList;

public class ScreenBlockerActivity extends AppCompatActivity {
    private final static String TAG = "ScreenBlockerActivity";
    private Context mContext = null;
    private LocalBroadcastManager mLocalBroadcastManager;

    private DataHelper mDataHelper;
    ArrayList<WhitelistItem> mWhitelistItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_screen_blocker);

        mDataHelper = new DataHelper(this);
        mWhitelistItems = mDataHelper.getAvailableWhitelistItems();

        ListView listView = (ListView) findViewById(R.id.lvWhitelistedItems);
        WhitelistListedAdapter adapter = new WhitelistListedAdapter(this,
                R.layout.listview_whitelisted_row,
                mWhitelistItems);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick pos:" + position + " id:" + id);

                stopScreenBlockerAndKeepReason();

                Intent launchIntent = new Intent();
                launchIntent.setComponent(new ComponentName(mWhitelistItems.get(position).getAppName(),
                        mWhitelistItems.get(position).getAppActivity()));
                startActivity(launchIntent);
            }
        });

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.registerReceiver(mStopBroadcastReceiver,
                new IntentFilter(ActionStr.ACTION_STOP_SCREEN_BLOCKER));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocalBroadcastManager.unregisterReceiver(mStopBroadcastReceiver);
        mDataHelper.close();
    }

    BroadcastReceiver mStopBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "mStopBroadcastReceiver.onReceive");
            if (intent.getAction().equals(ActionStr.ACTION_STOP_SCREEN_BLOCKER)) {
                ((Activity)mContext).finish();
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.screen_locker_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "BACK");
            finish();
        }
        return true;
    }

    private void startSettings() {
        Intent intent = new Intent(mContext, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        finish();
    }

    private void stopScreenBlockerAndKeepReason() {
        Intent intent = new Intent(ActionStr.ACTION_STOP_MONITOR_AND_KEEP_REASON);
        mLocalBroadcastManager.sendBroadcast(intent);
    }
}
