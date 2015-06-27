package com.bluetooth.modbus.snrtools.common;


import android.app.Application;
import android.bluetooth.BluetoothAdapter;

import com.bluetooth.modbus.snrtools.Constans;
import com.bluetooth.modbus.snrtools.MyObservable;
import com.bluetooth.modbus.snrtools.manager.AppStaticVar;
/**
 * wechat
 *
 * @author donal
 *
 */
public class SNRApplication extends Application {
	
	public void onCreate() {
		CrashHandler catchHandler = CrashHandler.getInstance();
		catchHandler.init(this);
		AppStaticVar.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		AppStaticVar.mObservable = new MyObservable();
		Constans.mApplicationContext = getApplicationContext();
	}
	
}
