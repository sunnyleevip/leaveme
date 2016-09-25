package com.sunny.leaveme.activities.uihelper;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.sunny.leaveme.R;
import com.sunny.leaveme.db.entity.WhitelistItem;

import java.util.ArrayList;

/**
 * Created by Sunny Li on 2016/9/25.
 * WhitelistListAdapter for customize list view in Whitelist Activity
 */
public class WhitelistListAdapter extends ArrayAdapter<WhitelistItem> {
    private Context context;
    private int layoutResourceId;
    private ArrayList<WhitelistItem> data;
    private OnCheckedChangeListener mSwitchOnCheckedChangeListener;

    public WhitelistListAdapter(Context context, int layoutResourceId, ArrayList<WhitelistItem> data
            , OnCheckedChangeListener onCheckedChangeListener) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.mSwitchOnCheckedChangeListener = onCheckedChangeListener;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        WhitelistItemHolder holder;

        if(convertView == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);

            holder = new WhitelistItemHolder();
            holder.tvTitle = (TextView)convertView.findViewById(R.id.whitelist_item_main);
            holder.swAvailable = (Switch)convertView.findViewById(R.id.whitelist_item_switch_available);
            holder.swAvailable.setEnabled(true);
            holder.swAvailable.setTag(position);
            holder.swAvailable.setOnCheckedChangeListener(mSwitchOnCheckedChangeListener);
            convertView.setTag(holder);
        } else {
            holder = (WhitelistItemHolder)convertView.getTag();
            holder.swAvailable.setTag(position);
        }

        WhitelistItem whitelistItem = data.get(position);
        holder.swAvailable.setChecked(whitelistItem.isAvailable());
        String[] splittedNames = whitelistItem.getAppName().split("\\.");
        holder.tvTitle.setText(splittedNames[splittedNames.length - 1]);

        return convertView;
    }

    private static class WhitelistItemHolder {
        TextView tvTitle;
        Switch swAvailable;
    }
}
