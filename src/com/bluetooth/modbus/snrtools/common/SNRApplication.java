package com.bluetooth.modbus.snrtools.common;


import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.os.Handler;

import com.bluetooth.modbus.snrtools.manager.AppStaticVar;
/**
 * wechat
 *
 * @author donal
 *
 */
public class SNRApplication extends Application {
	
	private Handler mHandler;
	
	public void onCreate() {
		CrashHandler catchHandler = CrashHandler.getInstance();
		catchHandler.init(this);
		AppStaticVar.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
//		initHandler();
	}
	
	public Handler getHandler(){
		return this.mHandler;
	}
	
//	private void initHandler() {
//		mHandler = new Handler() {
//			@Override
//			public void handleMessage(Message msg) {
//				switch (msg.what) {
//					case Constans.CONNECTING_DEVICE :
//						showProgressDialog(msg.obj.toString());
//						break;
//					case Constans.CONNECT_DEVICE_SUCCESS :
//						hideProgressDialog();
//						Intent intent = new Intent(mContext,
//								SNRMainActivity.class);
//						startActivity(intent);
//						break;
//					case Constans.CONNECT_DEVICE_FAILED :
//						hideProgressDialog();
//						showToast(msg.obj.toString());
//						break;
//				}
//			}
//		};
//	}
}
