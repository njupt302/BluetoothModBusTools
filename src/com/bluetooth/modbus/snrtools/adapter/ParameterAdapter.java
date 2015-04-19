package com.bluetooth.modbus.snrtools.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bluetooth.modbus.snrtools.R;
import com.bluetooth.modbus.snrtools.bean.Parameter;

public class ParameterAdapter extends BaseAdapter
{

	private Context mContext;
	private List<Parameter> mList;

	public ParameterAdapter(Context context, List<Parameter> list)
	{
		this.mContext = context;
		this.mList = list;
	}

	@Override
	public int getCount()
	{
		return mList.size();
	}

	@Override
	public Parameter getItem(int position)
	{
		return mList.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder;
		if (convertView == null)
		{
			holder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.parameter_item, null);
			holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			holder.tvValue = (TextView) convertView.findViewById(R.id.tvValue);
			holder.tvGroupName = (TextView) convertView.findViewById(R.id.tvGroupName);
			holder.ll = convertView.findViewById(R.id.ll);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.tvName.setText(mList.get(position).name);
		holder.tvValue.setText(mList.get(position).value);
		if (mList.get(position).isGroupTitle)
		{
			holder.tvGroupName.setVisibility(View.VISIBLE);
			holder.ll.setVisibility(View.GONE);
			holder.tvGroupName.setText(mList.get(position).groupTitle);
		}
		else
		{
			holder.tvGroupName.setVisibility(View.GONE);
			holder.ll.setVisibility(View.VISIBLE);
		}
		return convertView;
	}

	static class ViewHolder
	{
		TextView tvName;
		TextView tvValue;
		TextView tvGroupName;
		View ll;
	}
}
