package com.bluetooth.modbus.snrtools;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.bluetooth.modbus.snrtools.adapter.SelectAdapter;
import com.bluetooth.modbus.snrtools.bean.Parameter;
import com.bluetooth.modbus.snrtools.bean.Selector;
import com.bluetooth.modbus.snrtools.manager.ActivityManager;

public class SelectActivity extends BaseWriteParamActivity {

	private TextView mTvTitle;
	private ListView mLv;
	private SelectAdapter mAdapter;
	private int mPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_activity);
		initUI();
		initData();
	}

	private void initData() {
		mTvTitle.setText(getIntent().getStringExtra("title"));
		List<Selector> list = (List<Selector>) getIntent()
				.getSerializableExtra("list");
		if (list == null) {
			list = new ArrayList<Selector>();
		}
		mAdapter = new SelectAdapter(mContext, list);
		mLv.setAdapter(mAdapter);
		mLv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mPosition = position;
				Parameter p = (Parameter) getIntent().getSerializableExtra("param");
				if(p != null){
					p.valueIn = mAdapter.getItem(position).value;
					writeParameter(p);
				}
			}
		});
	}
	@Override
	public void reconnectSuccss() {
	}
	private void initUI() {
		mTvTitle = (TextView) findViewById(R.id.tvTitle);
		mLv = (ListView) findViewById(R.id.listView1);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN
				&& isOutOfBounds(this, event)) {
			ActivityManager.getInstances().finishActivity(this);
			return true;
		}
		return super.onTouchEvent(event);
	}

	private boolean isOutOfBounds(Activity context, MotionEvent event) {
		final int x = (int) event.getX();
		final int y = (int) event.getY();
		final int slop = ViewConfiguration.get(context)
				.getScaledWindowTouchSlop();
		final View decorView = context.getWindow().getDecorView();
		return (x < -slop) || (y < -slop)
				|| (x > (decorView.getWidth() + slop))
				|| (y > (decorView.getHeight() + slop));
	}

	@Override
	public void onSuccess() {
		Intent intent = new Intent();
		intent.putExtra("position", getIntent().getIntExtra("position", -1));
		intent.putExtra("selector", mAdapter.getItem(mPosition));
		setResult(RESULT_OK, intent);
		ActivityManager.getInstances().finishActivity(SelectActivity.this);
	}
}
