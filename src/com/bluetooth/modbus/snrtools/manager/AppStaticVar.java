package com.bluetooth.modbus.snrtools.manager;

import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;

import com.bluetooth.modbus.snrtools.MyObservable;
import com.bluetooth.modbus.snrtools.bean.Parameter;

public class AppStaticVar {
	public final static Byte[] locks = new Byte[0]; 
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
	/** 密码等级1查看参数个数*/
	public static int PASSWORD_LEVEAL1_COUNT = 0;
	/** 密码等级2查看参数个数*/
	public static int PASSWORD_LEVEAL2_COUNT = 0;
	/** 密码等级3查看参数个数*/
	public static int PASSWORD_LEVEAL3_COUNT = 0;
	/** 密码等级4查看参数个数*/
	public static int PASSWORD_LEVEAL4_COUNT= 0;
	/** 密码等级5查看参数个数*/
	public static int PASSWORD_LEVEAL5_COUNT = 0;
	/** 参数列表*/
	public static List<Parameter> mParamList;
	/** 正向总量参数位置*/
	public static int ZXZLPosition = -1;
	/** 反向总量参数位置*/
	public static int FXZLPosition = -1;
	/** 是否是主动断开*/
	public static boolean isExit = false;
	/** 用于通知密码页面开始发送指令*/
	public static MyObservable mObservable;
}
