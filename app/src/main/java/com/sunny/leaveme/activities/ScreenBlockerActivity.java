package com.sunny.leaveme.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
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
import android.view.View.OnClickListener;
import android.widget.Button;

import com.sunny.leaveme.ActionStr;
import com.sunny.leaveme.R;


public class ScreenBlockerActivity extends AppCompatActivity {
    private final static String TAG = "ScreenBlockerActivity";
    private Context mContext = null;
    private LocalBroadcastManager mLocalBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_screen_blocker);
        initView();

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.registerReceiver(mStopBroadcastReceiver,
                new IntentFilter(ActionStr.ACTION_STOP_SCREEN_BLOCKER));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocalBroadcastManager.unregisterReceiver(mStopBroadcastReceiver);
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
        }
        return true;
    }

    private void initView() {
        Button btExit = (Button) findViewById(R.id.exit_button);
        btExit.setOnClickListener(new mClickListener());
    }

    class mClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.exit_button:
                    Log.d(TAG, "Exit");
                    finish();
                    break;
                default:
                    break;
            }
        }
    }

    private void startSettings() {
        Intent intent = new Intent(mContext, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        finish();
    }
}
