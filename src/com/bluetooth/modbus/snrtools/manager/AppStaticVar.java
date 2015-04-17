package com.bluetooth.modbus.snrtools.manager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;

public class AppStaticVar {
	/** 取得默认的蓝牙适配器 */
	public static BluetoothAdapter mBtAdapter;
	/** 当前连接的蓝牙地址*/
	public static String mCurrentAddress;
	/** 当前连接的蓝牙名称*/
	public static String mCurrentName;
	/** 蓝牙socket*/
	public static BluetoothSocket mSocket;
	/** 密码等级*/
	public static int PASSWORD_LEVEAL = -1;
	
}
