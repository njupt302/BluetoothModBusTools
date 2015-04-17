package com.bluetooth.modbus.snrtools.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bluetooth.modbus.snrtools.R;
import com.bluetooth.modbus.snrtools.bean.SiriListItem;

public class DeviceListAdapter extends BaseAdapter {
	private ArrayList<SiriListItem> list;
	private LayoutInflater mInflater;

	public DeviceListAdapter(Context context, ArrayList<SiriListItem> list2) {
		list = list2;
		mInflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public int getItemViewType(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		SiriListItem item = list.get(position);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item, null);
			viewHolder = new ViewHolder(
					(View) convertView.findViewById(R.id.list_child),
					(TextView) convertView.findViewById(R.id.chat_msg),
					(TextView) convertView.findViewById(R.id.label));
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (item.isSiri()) {
			viewHolder.child.setBackgroundColor(Color.parseColor("#46E109"));
			viewHolder.label.setVisibility(View.VISIBLE);
		} else {
			viewHolder.child.setBackgroundColor(Color.WHITE);
			viewHolder.label.setVisibility(View.GONE);
		}
		viewHolder.msg.setText(item.getMessage());

		return convertView;
	}

	class ViewHolder {
		protected View child;
		protected TextView msg;
		protected TextView label;

		public ViewHolder(View child, TextView msg, TextView label) {
			this.child = child;
			this.msg = msg;
			this.label = label;
		}
	}
}
