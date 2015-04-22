package com.bluetooth.modbus.snrtools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.bluetooth.modbus.snrtools.adapter.ParameterAdapter;
import com.bluetooth.modbus.snrtools.bean.Parameter;
import com.bluetooth.modbus.snrtools.bean.Selector;
import com.bluetooth.modbus.snrtools.manager.AppStaticVar;

public class CopyOfParamSettingActivity extends BaseActivity {

	private ListView mListview;
	private ParameterAdapter mAdapter;
	private List<Parameter> mList;
	private List<Parameter> mDataList;
	private final static int SELECT_PARAM = 0x100001;
	private final static int INPUT_PARAM = 0x100002;
	/** 显示参数的数量 */
	private int mCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_param_activity);
		if (AppStaticVar.PASSWORD_LEVEAL == 0) {
			setTitleContent("查看参数");
		} else {
			setTitleContent("设置参数");
		}
		// mList = (List<Parameter>) getIntent().getSerializableExtra("list");
		mList = AppStaticVar.mParamList;
		hideRightView(R.id.btnRight1);
		hideRightView(R.id.view2);
		initUI();
		setListeners();
	}

	@Override
	public void reconnectSuccss() {
	}

	private void setListeners() {
		switch (AppStaticVar.PASSWORD_LEVEAL) {
			case 1 :// 可以设置1-24
				mCount = AppStaticVar.PASSWORD_LEVEAL1_COUNT;
				break;
			case 2 :// 可以设置1-25
				mCount = AppStaticVar.PASSWORD_LEVEAL2_COUNT;
				break;
			case 3 :// 可以设置1-38
				mCount = AppStaticVar.PASSWORD_LEVEAL3_COUNT;
				break;
			case 4 :// 可以设置1-60

				mCount = AppStaticVar.PASSWORD_LEVEAL4_COUNT;
				break;
			case 5 :// // 超级密码
				mCount = mList.size();
				break;
		}
		mDataList = new ArrayList<Parameter>();
		mDataList.addAll(mList.subList(0, mCount));
		mAdapter = new ParameterAdapter(mContext, mDataList);
		mListview.setAdapter(mAdapter);
		mListview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mAdapter.getItem(position).isGroupTitle) {
					return;
				}
				Intent intent = new Intent();
				if (mAdapter.getItem(position).selectors != null) {
					intent.setClass(mContext, SelectActivity.class);
					intent.putExtra("position", position);
					intent.putExtra("title", mAdapter.getItem(position).name);
					intent.putExtra("value", mAdapter.getItem(position).value);
					intent.putExtra("valueIn",
							mAdapter.getItem(position).valueIn.toString());
					intent.putExtra("list",
							(Serializable) mAdapter.getItem(position).selectors);
					intent.putExtra("param", mAdapter.getItem(position));
					startActivityForResult(intent, SELECT_PARAM);
				} else {
					intent.setClass(mContext, InputParamActivity.class);
					intent.putExtra("position", position);
					intent.putExtra("title", mAdapter.getItem(position).name);
					intent.putExtra("value", mAdapter.getItem(position).value);
					intent.putExtra("valueIn",
							mAdapter.getItem(position).valueIn.toString());
					intent.putExtra("param", mAdapter.getItem(position));
					startActivityForResult(intent, INPUT_PARAM);
				}
			}
		});
	}

	private void initUI() {
		mListview = (ListView) findViewById(R.id.listView1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PARAM) {
				int position = data.getIntExtra("position", -1);
				Selector selector = (Selector) data
						.getSerializableExtra("selector");
				if (position != -1) {
					mAdapter.getItem(position).valueIn = selector.value;
					mAdapter.getItem(position).value = selector.name;
					mAdapter.notifyDataSetChanged();
				}
			} else if (requestCode == INPUT_PARAM) {
				int position = data.getIntExtra("position", -1);
				String value = data.getStringExtra("value");
				if (position != -1) {
					mAdapter.getItem(position).valueIn = value;
					mAdapter.getItem(position).value = value;
					mAdapter.notifyDataSetChanged();
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		AppStaticVar.PASSWORD_LEVEAL = -1;
		super.onDestroy();
	}
}
