package com.bluetooth.modbus.snrtools;

import android.content.Context;
import android.os.Environment;

public class Constans {
	
	public static Context mApplicationContext;
	
	/** 指定型号名称起始字段*/
	public final static String DEVICE_NAME_START = "Sinier";

	/** 循环读取的参数的数目*/
	public final static int READ_PARAM_COUNT = 6;

	/** 连接设备中*/
	public final static int CONNECTING_DEVICE = 0X10001;
	/** 设备连接成功*/
	public final static int CONNECT_DEVICE_SUCCESS = 0X10002;
	/** 设备连接失败*/
	public final static int CONNECT_DEVICE_FAILED = 0X10003;
	/** 设备未连接*/
	public final static int NO_DEVICE_CONNECTED = 0X10004;
	/** 设备返回信息*/
	public final static int DEVICE_RETURN_MSG = 0X10005;
	/** 连接已经关闭*/
	public final static int CONNECT_IS_CLOSED = 0X10006;
	/** 连接堵塞*/
	public final static int CONNECT_IS_JIM = 0X10007;
	/** 通讯开始*/
	public final static int CONTACT_START = 0X10008;
	/** 不合法的返回信息*/
	public final static int ERROR_START = 0X10009;
	/** 连接超时*/
	public final static int TIME_OUT = 0X1000A;
	
	public static class PasswordLevel{
		/** 等级1，基本参数密码 可设置1-17参数，出厂密码521*/
		public static long LEVEL_1 = 521;
		/** 等级2，高级参数密码 可设置1-32参数，出厂密码3210*/
		public static long LEVEL_2 = 3210;
		/** 等级3，传感器参数密码 可设置1-55参数，出厂密码6108*/
		public static long LEVEL_3 = 6108;
		/** 等级4，转换器密码 可设置1-60参数，出厂密码97206*/
		public static long LEVEL_4 = 97206;
		/** 等级5，超级密码 可设置1-60参数，固定值270427*/
		public static long LEVEL_5 = 270427;
		/** 等级6，总量清零密码,出厂密码5210*/
		public static long LEVEL_6 = 5210;
	}
	
	public static class Directory{
		public static final String DOWNLOAD = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Sinier/update/";
		public static final String LOG = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Sinier/log/";
	}
}
