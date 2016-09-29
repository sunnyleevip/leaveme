package com.sunny.leaveme.activities.uihelper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
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
 * WhitelistedListAdapter for customize list view in Screen Blocker Activity
 */
public class WhitelistListedAdapter extends ArrayAdapter<WhitelistItem> {
    private Context context;
    private int layoutResourceId;
    private ArrayList<WhitelistItem> data;

    public WhitelistListedAdapter(Context context, int layoutResourceId,
                                  ArrayList<WhitelistItem> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        WhitelistItemHolder holder;

        if(convertView == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);

            holder = new WhitelistItemHolder();
            holder.tvTitle = (TextView)convertView.findViewById(R.id.whitelisted_item_main);
            convertView.setTag(holder);
        } else {
            holder = (WhitelistItemHolder)convertView.getTag();
        }

        WhitelistItem whitelistItem = data.get(position);
        holder.tvTitle.setText(whitelistItem.getAppLabel());
        PackageManager pm = context.getPackageManager();
        try {
            Drawable icon = pm.getApplicationIcon(whitelistItem.getAppName());
            holder.tvTitle.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        } catch (NameNotFoundException ex) {
            ex.printStackTrace();
        }

        return convertView;
    }

    private static class WhitelistItemHolder {
        TextView tvTitle;
    }
}
