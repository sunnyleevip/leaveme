package com.sunny.leaveme.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sunny.leaveme.R;
import com.sunny.leaveme.db.entity.ScheduleItem;
import com.sunny.leaveme.db.entity.ScheduleTime;

import java.util.ArrayList;

public class ScheduleItemActivity extends AppCompatActivity {

    private static final String TAG = "ScheduleItemActivity";

    private final static String STR_START_TIME = "Start Time  ";
    private final static String STR_END_TIME   = "End Time    ";
    private final static String STR_REPEAT     = "Repeat      ";
    private final static int INT_EDITTEXT_MAX_LEN = 2;

    //private Context mContext;

    private ArrayList<String> mListItems = new ArrayList<>();
    private ArrayAdapter<String> mAdapter;
    private ScheduleItem mScheduleItem;
    private int mPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_item);

        //mContext = this;

        Intent intent = getIntent();
        ScheduleItem scheduleItem = (ScheduleItem) intent.getSerializableExtra("schedule_item");
        if (scheduleItem != null) {
            mScheduleItem = scheduleItem;
            mPos = intent.getIntExtra("position", -1);
        } else {
            mScheduleItem = new ScheduleItem();
        }

        ListView listView = (ListView) findViewById(R.id.lvSettings);
        mAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                mListItems);
        updateListItem();
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int idx,
                                    long arg3) {
                if (mListItems.get(idx).startsWith(STR_START_TIME)) {
                    showSetTimeDialog(view, mScheduleItem.getStartTime());
                }
                if (mListItems.get(idx).startsWith(STR_END_TIME)) {
                    showSetTimeDialog(view, mScheduleItem.getEndTime());
                }
                if (mListItems.get(idx).startsWith(STR_REPEAT)) {
                    showSetRepeatDialog(view, mScheduleItem.getRepeatDays());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.schedule_item_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                returnSchedule();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateListItem() {
        for (int i = mListItems.size() - 1; i >= 0; --i) {
            mListItems.remove(i);
        }
        mListItems.add(STR_START_TIME + mScheduleItem.getStartTime().toString());
        mListItems.add(STR_END_TIME + mScheduleItem.getEndTime().toString());
        mListItems.add(STR_REPEAT + mScheduleItem.getRepeatDaysString());
    }

    private void showSetRepeatDialog(final View view, final boolean[] repeatDays) {
        Context context = view.getContext();

        final CharSequence[] items = {
                " Sunday ", " Monday ", " Tuesday ", " Wednesday ", " Thursday ", " Friday ", " Saturday "
        };
        final ArrayList<Integer> selectedItems = new ArrayList<>();
        for (int i = 0; i < repeatDays.length; ++i) {
            if (repeatDays[i]) {
                selectedItems.add(i);
            }
        }

        new AlertDialog.Builder(context).setTitle("Choose repeat days")
                .setMultiChoiceItems(items, repeatDays, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                        if (isChecked) {
                            selectedItems.add(indexSelected);
                        } else if (selectedItems.contains(indexSelected)) {
                            selectedItems.remove(Integer.valueOf(indexSelected));
                        }
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        boolean isRepeat = false;
                        for (int i = 0; i < selectedItems.size(); ++i) {
                            repeatDays[selectedItems.get(i)] = true;
                            isRepeat = true;
                        }
                        mScheduleItem.setRepeatDays(repeatDays);
                        mScheduleItem.setRepeat(isRepeat);
                        updateListItem();
                        mAdapter.notifyDataSetChanged();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // nothing to do
                    }
                }).create()
                .show();
    }

    private void showSetTimeDialog(final View view, final ScheduleTime scheduleTime) {
        Context context = view.getContext();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER);

        final EditText etHour = new EditText(context);
        etHour.setInputType(InputType.TYPE_CLASS_NUMBER);
        etHour.setFilters(new InputFilter[] {new InputFilter.LengthFilter(INT_EDITTEXT_MAX_LEN)});
        etHour.setHint("00");
        etHour.setGravity(Gravity.CENTER);
        layout.addView(etHour);

        final TextView tvColon = new TextView(context);
        tvColon.setText(":");
        tvColon.setGravity(Gravity.CENTER);
        layout.addView(tvColon);

        final EditText etMinute = new EditText(context);
        etMinute.setInputType(InputType.TYPE_CLASS_NUMBER);
        etMinute.setFilters(new InputFilter[] {new InputFilter.LengthFilter(INT_EDITTEXT_MAX_LEN)});
        etMinute.setHint("00");
        etMinute.setGravity(Gravity.CENTER);
        layout.addView(etMinute);

        new AlertDialog.Builder(context).setTitle("Time Setting")
                .setView(layout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int hour = 0;
                        if (etHour.getText().length() > 0) {
                            try {
                                hour = Integer.parseInt(etHour.getText().toString());
                            } catch(NumberFormatException nfe) {
                                Log.e(TAG, "Not number");
                                nfe.printStackTrace();
                            }
                        }

                        if ((hour < 0) || (hour > 23)) {
                            Toast.makeText(getApplicationContext(), "Hour should between 0-23.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        int minute = 0;
                        if (etMinute.getText().length() > 0) {
                            try {
                                minute = Integer.parseInt(etMinute.getText().toString());
                            } catch(NumberFormatException nfe) {
                                Log.e(TAG, "Not number");
                                nfe.printStackTrace();
                            }
                        }

                        if ((minute < 0) || (minute > 59)) {
                            Toast.makeText(getApplicationContext(), "Minute should between 0-59.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        scheduleTime.setHour(hour);
                        scheduleTime.setMinute(minute);
                        mScheduleItem.makeVaild();
                        Log.d(TAG, "Hour: " + scheduleTime.getHour() + ", minute: " + scheduleTime.getMinute());
                        updateListItem();
                        mAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void returnSchedule() {
        mScheduleItem.setAvailable(true);
        Log.d(TAG, "Repeat days: " + mScheduleItem.getRepeatDaysString());
        Intent intent = new Intent();
        intent.putExtra("schedule_item", mScheduleItem);
        intent.putExtra("position", mPos);
        setResult(RESULT_OK, intent);
        finish();
    }
}
