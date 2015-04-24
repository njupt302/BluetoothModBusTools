package com.bluetooth.modbus.snrtools;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.ab.http.AbHttpUtil;
import com.bluetooth.modbus.snrtools.adapter.DeviceListAdapter;
import com.bluetooth.modbus.snrtools.bean.SiriListItem;
import com.bluetooth.modbus.snrtools.manager.AppStaticVar;
import com.bluetooth.modbus.snrtools.uitls.AppUtil;
import com.bluetooth.modbus.snrtools.view.MyAlertDialog.MyAlertDialogListener;

public class SelectDeviceActivity extends BaseActivity {

	private static final String NO_DEVICE_CAN_CONNECT = "没有可以连接的设备";
	private ListView mListView;
	private ArrayList<SiriListItem> list;
	private DeviceListAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mAbHttpUtil = AbHttpUtil.getInstance(this);
		setTitleContent("选择设备");
		hideRightView(R.id.view2);
		setRightButtonContent("搜索", R.id.btnRight1);
		init();
		showRightView(R.id.rlMenu);
		if (AppUtil.checkBluetooth(mContext)) {
			searchDevice();
		}
	}

	@Override
	public void reconnectSuccss() {
		hideDialog();
		Intent intent = new Intent(mContext, MainActivity.class);
//		Intent intent = new Intent(mContext, SNRMainActivity.class);
		startActivity(intent);
	}

	@Override
	public void BackOnClick(View v) {
		switch (v.getId()) {
			case R.id.ivBack :
				onBackPressed();
		}
	}

	private void init() {
		list = new ArrayList<SiriListItem>();
		mAdapter = new DeviceListAdapter(this, list);
		mListView = (ListView) findViewById(R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setFastScrollEnabled(true);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				SiriListItem item = list.get(arg2);
				if(item == null){
					return;
				}
				String info = item.getMessage();
				if (NO_DEVICE_CAN_CONNECT.equals(info)) {
					return;
				}
				String address = info.substring((info.length() - 17)>0?info.length() - 17:0);
				String name = info.substring(0, (info.length() - 17)>0?info.length() - 17:0);
				AppStaticVar.mCurrentAddress = address;
				AppStaticVar.mCurrentName = name;

				showDialog("是否连接" + item.getMessage(),
						new MyAlertDialogListener() {

							@Override
							public void onClick(View view) {
								switch (view.getId()) {
									case R.id.btnCancel :
										AppStaticVar.mCurrentAddress = null;
										AppStaticVar.mCurrentName = null;
										hideDialog();
										break;
									case R.id.btnOk :
										setRightButtonContent("搜索",
												R.id.btnRight1);
										connectDevice(AppStaticVar.mCurrentAddress);
										break;
								}
							}
						});
			}
		});

		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);
	}

	@Override
	protected void rightButtonOnClick(int id) {
		switch (id) {
			case R.id.btnRight1 :
				if (AppUtil.checkBluetooth(mContext)) {
					searchDevice();
				}
				break;
		}
	}

	private void searchDevice() {
		if (AppStaticVar.mBtAdapter.isDiscovering()) {
			AppStaticVar.mBtAdapter.cancelDiscovery();
			setRightButtonContent("搜索", R.id.btnRight1);
		} else {
			showProgressDialog("设备搜索中...", true);
			list.clear();
			mAdapter.notifyDataSetChanged();

			Set<BluetoothDevice> pairedDevices = AppStaticVar.mBtAdapter
					.getBondedDevices();
			if (pairedDevices.size() > 0) {
				for (BluetoothDevice device : pairedDevices) {
					if (device.getName().toUpperCase(Locale.ENGLISH)
							.startsWith(Constans.DEVICE_NAME_START.toUpperCase(Locale.ENGLISH))) {
						list.add(new SiriListItem(device.getName() + "\n"+ device.getAddress(), true));
						mAdapter.notifyDataSetChanged();
						mListView.setSelection(list.size() - 1);
					}
				}
			} else {
				list.add(new SiriListItem(NO_DEVICE_CAN_CONNECT, true));
				mAdapter.notifyDataSetChanged();
				mListView.setSelection(list.size() - 1);
			}
			/* 开始搜索 */
			AppStaticVar.mBtAdapter.startDiscovery();
			setRightButtonContent("停止", R.id.btnRight1);
		}
	}

	@Override
	public void onBackPressed() {
		showDialog("是否要退出程序？", new MyAlertDialogListener() {
			@Override
			public void onClick(View view) {
				switch (view.getId()) {
					case R.id.btnCancel :
						hideDialog();
						break;
					case R.id.btnOk :
						AppUtil.closeBluetooth();
						finish();
						System.exit(0);
						break;
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		this.unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					if (device.getName().toUpperCase(Locale.ENGLISH)
							.startsWith(Constans.DEVICE_NAME_START.toUpperCase(Locale.ENGLISH))) {
						list.add(new SiriListItem(device.getName() + "\n"+ device.getAddress(), false));
						mAdapter.notifyDataSetChanged();
						mListView.setSelection(list.size() - 1);
					}
				}
			} else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
				if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON){
					if (AppUtil.checkBluetooth(mContext)) {
						searchDevice();
					}
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				hideProgressDialog();
				setProgressBarIndeterminateVisibility(false);
				if (mListView.getCount() == 0) {
					list.add(new SiriListItem("没有发现蓝牙设备", false));
					mAdapter.notifyDataSetChanged();
					mListView.setSelection(list.size() - 1);
				}
				setRightButtonContent("搜索", R.id.btnRight1);
			}
		}
	};
}
