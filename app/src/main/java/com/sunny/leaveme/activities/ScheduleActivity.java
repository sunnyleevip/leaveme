package com.sunny.leaveme.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;

import com.sunny.leaveme.activities.uihelper.ScheduleListAdapter;
import com.sunny.leaveme.db.DataHelper;
import com.sunny.leaveme.R;
import com.sunny.leaveme.db.entity.ScheduleItem;
import com.sunny.leaveme.services.ManagerService;

import java.util.ArrayList;

public class ScheduleActivity extends AppCompatActivity {
    private final static String TAG = "ScheduleActivity";
    private final static String ACTION_UPDATE_VIEW = "com.sunny.leaveme.ACTION_UPDATE_VIEW";
    private final static int REQUEST_CODE_CREATE = 101;
    private final static int REQUEST_CODE_MODIFY = 102;

    private static Context mContext = null;
    private LocalBroadcastManager mLocalBroadcastManager;

    private ScheduleListAdapter mAdapter;

    private DataHelper mDataHelper;
    private ArrayList<ScheduleItem> mScheduleItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_schedule);

        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.fab_add);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScheduleItem();
            }
        });

        mDataHelper = new DataHelper(this);
        mScheduleItems = mDataHelper.getAllScheduleItems();
        for (int i = 0; i < mScheduleItems.size(); ++i) {
            Log.d(TAG, "pos: " + i + " id: " + mScheduleItems.get(i).getId() + " available:" + mScheduleItems.get(i).isAvailable());
        }

        ListView listView = (ListView) findViewById(R.id.lvTimeList);
        mAdapter = new ScheduleListAdapter(this,
                R.layout.listview_schedule_row,
                mScheduleItems,
                new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int position = (Integer)buttonView.getTag();
                        if (isChecked != mScheduleItems.get(position).isAvailable()) {
                            Log.d(TAG, "onCheckedChanged at position: " + position + " checked: " + isChecked);
                            mScheduleItems.get(position).setAvailable(isChecked);
                            updateItem(position, mScheduleItems.get(position));
                        }
                    }
                });
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startScheduleItem(position);
            }
        });
        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.setHeaderTitle("Delete?");
                menu.add(0, 0, 0, "OK");
                menu.add(0, 1, 0, "Cancel");
            }
        });

        Intent intent = new Intent(mContext, ManagerService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.registerReceiver(mViewUpdateBroadcastReceiver, new IntentFilter(ACTION_UPDATE_VIEW));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDataHelper.close();
        unbindService(mServiceConnection);
        mLocalBroadcastManager.unregisterReceiver(mViewUpdateBroadcastReceiver);
    }

    private BroadcastReceiver mViewUpdateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "mViewUpdateBroadcastReceiver.onReceive");
            if (intent.getAction().equals(ACTION_UPDATE_VIEW)) {
                int id = intent.getIntExtra("id", -1);
                if (id >= 0) {
                    Log.d(TAG, "Update view id: " + id);
                    for (int i = 0; i < mScheduleItems.size(); ++i) {
                        if (mScheduleItems.get(i).getId() == id) {
                            mScheduleItems.get(i).setAvailable(false);
                            mAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult requestCode:" + requestCode + ", resultCode: " + resultCode);
        if (resultCode == RESULT_OK) {
            ScheduleItem scheduleItem;
            switch (requestCode) {
                case REQUEST_CODE_CREATE:
                    scheduleItem = (ScheduleItem) data.getSerializableExtra("schedule_item");
                    Log.d(TAG, "start: " + scheduleItem.getStartTime().toString()
                            + ", end: " + scheduleItem.getEndTime().toString()
                            + " " + scheduleItem.getRepeatDaysString());
                    addItem(scheduleItem);
                    break;
                case REQUEST_CODE_MODIFY:
                    int pos = data.getIntExtra("position", -1);
                    scheduleItem = (ScheduleItem) data.getSerializableExtra("schedule_item");
                    Log.d(TAG, "start: " + scheduleItem.getStartTime().toString()
                            + ", end: " + scheduleItem.getEndTime().toString()
                            + " " + scheduleItem.getRepeatDaysString());
                    updateItem(pos, scheduleItem);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int pos=(int)info.id;

        switch(item.getItemId())
        {
            case 0:
                removeItem(pos);
                break;
            case 1:
                Log.d(TAG, "Not delete");
                break;
            default:
                Log.e(TAG, "Cannot be here");
                break;
        }
        return super.onContextItemSelected(item);
    }

    public void addItem(ScheduleItem scheduleItem) {
        int id = mDataHelper.insertScheduleItem(scheduleItem);
        scheduleItem.setId(id);
        mScheduleItems.add(scheduleItem);
        mAdapter.notifyDataSetChanged();
        if (scheduleItem.isAvailable()) {
            addAlarm(scheduleItem);
        }
    }

    public void updateItem(int position, ScheduleItem scheduleItem) {
        Log.d(TAG, "update id: " + scheduleItem.getId());
        mScheduleItems.set(position, scheduleItem);
        mAdapter.notifyDataSetChanged();
        mDataHelper.updateScheduleItem(scheduleItem);
        if (scheduleItem.isAvailable()) {
            updateAlarm(scheduleItem);
        } else {
            cancelAlarm(position);
        }
    }

    public void removeItem(int position) {
        cancelAlarm(position);
        mDataHelper.deleteScheduleItem(mScheduleItems.get(position));
        mScheduleItems.remove(position);
        mAdapter.notifyDataSetChanged();
    }

    private void startScheduleItem() {
        Intent intent = new Intent(mContext, ScheduleItemActivity.class);
        startActivityForResult(intent, REQUEST_CODE_CREATE);
    }

    private void startScheduleItem(int position) {
        Intent intent = new Intent(mContext, ScheduleItemActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("schedule_item", mScheduleItems.get(position));
        startActivityForResult(intent, REQUEST_CODE_MODIFY);
    }

    //private static final String ACTION_UPDATE_ALARM = "com.sunny.leaveme.UPDATE_ALARM";
    private ManagerService mManagerService;
    private void addAlarm(ScheduleItem scheduleItem) {
        if (mManagerService != null) {
            mManagerService.addAlarm(scheduleItem);
        }
    }

    private void updateAlarm(ScheduleItem scheduleItem) {
        if (mManagerService != null) {
            mManagerService.updateAlarm(scheduleItem);
        }
    }

    private void cancelAlarm(int position) {
        if (mManagerService != null) {
            mManagerService.cancelAlarm(mScheduleItems.get(position).getId());
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {       //connect Service
            mManagerService = ((ManagerService.ManagerServiceBinder)(service)).getService();
            if (mManagerService != null) {
                Log.d(TAG, "connected");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {                 //disconnect Service
            mManagerService = null;
        }
    };
}
