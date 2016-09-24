package com.sunny.leaveme.activities.uihelper;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.sunny.leaveme.R;
import com.sunny.leaveme.db.entity.ScheduleItem;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/9/11.
 * ScheduleListAdapter for customize list view in Schedule Activity
 */
public class ScheduleListAdapter extends ArrayAdapter<ScheduleItem> {
    private Context context;
    private int layoutResourceId;
    private ArrayList<ScheduleItem> data;
    private OnCheckedChangeListener mSwitchOnCheckedChangeListener;

    public ScheduleListAdapter(Context context, int layoutResourceId, ArrayList<ScheduleItem> data
            , OnCheckedChangeListener onCheckedChangeListener) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.mSwitchOnCheckedChangeListener = onCheckedChangeListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ScheduleItemHolder holder;

        if(convertView == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);

            holder = new ScheduleItemHolder();
            holder.tvTitle = (TextView)convertView.findViewById(R.id.schedule_item_main);
            holder.tvSubtitle = (TextView)convertView.findViewById(R.id.schedule_item_sub);
            holder.swAvailable = (Switch)convertView.findViewById(R.id.switch_available);
            holder.swAvailable.setEnabled(true);
            holder.swAvailable.setTag(position);
            holder.swAvailable.setOnCheckedChangeListener(mSwitchOnCheckedChangeListener);
            convertView.setTag(holder);
        } else {
            holder = (ScheduleItemHolder)convertView.getTag();
            holder.swAvailable.setTag(position);
        }

        ScheduleItem scheduleItem = data.get(position);
        holder.swAvailable.setChecked(scheduleItem.isAvailable());
        holder.tvTitle.setText("From " + scheduleItem.getStartTime().toString()
                + " to " + scheduleItem.getEndTime().toString());
        holder.tvSubtitle.setText(scheduleItem.getRepeatDaysString());

        return convertView;
    }

    private static class ScheduleItemHolder {
        public TextView tvTitle;
        public TextView tvSubtitle;
        public Switch swAvailable;
    }
}
