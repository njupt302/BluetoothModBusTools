package com.bluetooth.modbus.snrtools.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bluetooth.modbus.snrtools.R;
import com.bluetooth.modbus.snrtools.bean.Selector;

public class SelectAdapter extends BaseAdapter {

	private Context mContext;
	private List<Selector> list;

	public SelectAdapter(Context context, List<Selector> list) {
		this.mContext = context;
		this.list = list;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Selector getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.select_item, null);
			holder.tv = (TextView) convertView.findViewById(R.id.textView1);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.tv.setText(list.get(position).name);
		return convertView;
	}

	static class ViewHolder {
		TextView tv;
	}
}
