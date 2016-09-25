package com.sunny.leaveme.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.sunny.leaveme.R;
import com.sunny.leaveme.activities.uihelper.WhitelistListAdapter;
import com.sunny.leaveme.db.DataHelper;
import com.sunny.leaveme.db.entity.WhitelistItem;

import java.util.ArrayList;

public class WhitelistActivity extends AppCompatActivity {

    private final static String TAG = "WhitelistActivity";

    private DataHelper mDataHelper;
    ArrayList<WhitelistItem> mWhitelistItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whitelist);

        mDataHelper = new DataHelper(this);
        mWhitelistItems = mDataHelper.getAllWhitelistItems();

        ListView listView = (ListView) findViewById(R.id.lvWhitelistItems);
        WhitelistListAdapter adapter = new WhitelistListAdapter(this,
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
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick pos:" + position + " id:" + id);
            }
        });
    }

    @Override
    public void onDestroy() {
        mDataHelper.close();
    }
}
