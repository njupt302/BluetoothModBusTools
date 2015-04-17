package com.bluetooth.modbus.snrtools;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.bluetooth.modbus.snrtools.bean.Parameter;
import com.bluetooth.modbus.snrtools.uitls.ModbusUtils;

public abstract class BaseWriteParamActivity extends BaseActivity {

	private Handler mHandler, mHandler1;
	private Thread mThread;
	public  int RECONNECT_TIME = 3, RECONNECT_TIME1 = 3;
	private Parameter mParameter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initHandler();
	}

	public abstract void onSuccess();

	public void writeParameter(Parameter param) {
		this.mParameter = param;
		if (this.mParameter != null) {
			startWriteParam();
		}
	}

	private void startWriteParam() {
		mThread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (RECONNECT_TIME > 0) {
					ModbusUtils.writeParameter(mContext.getClass().getSimpleName(),mHandler, mParameter);
					System.out.println("===RECONNECT_TIME===" + RECONNECT_TIME);
					RECONNECT_TIME--;
				}
			}
		});
		mThread.start();
	}

	private void startWrite2Device() {
		mThread = new Thread(new Runnable() {

			@Override
			public void run() {
				ModbusUtils.write2Device(mContext.getClass().getSimpleName(),mHandler1);
			}
		});
		mThread.start();
	}

	private void initHandler() {
		mHandler1 = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case Constans.CONTACT_START :
						System.out.println("=====写入参数开始读取数据");
						break;
					case Constans.NO_DEVICE_CONNECTED :
						System.out.println("=====写入参数没有设备连接");
						showToast("没有设备连接，请重新选择设备!");
						break;
					case Constans.DEVICE_RETURN_MSG :
						hideProgressDialog();
						System.out.println("写入参数收到的数据====="
								+ msg.obj.toString());
						if (msg.obj.toString().length() != 16) {
							return;
						}
						onSuccess();
						break;
					case Constans.CONNECT_IS_CLOSED :
						System.out.println("=====写入参数连接断开");
						showConnectDevice();
						showToast("连接已经断开!");
						break;
					case Constans.ERROR_START :
						System.out.println("=====写入参数接收错误");
						startWriteParam();
						break;
					case Constans.CONNECT_IS_JIM :
						showToast(msg.toString());
						break;
					case Constans.TIME_OUT :
						System.out.println("=====写入参数连接超时");
						if (RECONNECT_TIME1 > 0) {
							if (mThread != null && !mThread.isInterrupted()) {
								mThread.interrupt();
							}
							startWrite2Device();
							RECONNECT_TIME1--;
						} else {
							hideProgressDialog();
							showToast("写入参数超时！");
						}
						break;
				}
			}
		};

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case Constans.CONTACT_START :
						showProgressDialog("与设备通讯中...");
						System.out.println("=====参数开始读取数据");
						break;
					case Constans.NO_DEVICE_CONNECTED :
						System.out.println("=====参数没有设备连接");
						showToast("没有设备连接，请重新选择设备!");
						break;
					case Constans.DEVICE_RETURN_MSG :
						System.out.println("参数收到的数据=====" + msg.obj.toString());
						if (msg.obj.toString().length() != 16) {
							return;
						}
						startWrite2Device();
						break;
					case Constans.CONNECT_IS_CLOSED :
						System.out.println("=====参数连接断开");
						showConnectDevice();
						showToast("连接已经断开!");
						break;
					case Constans.ERROR_START :
						System.out.println("=====参数接收错误");
						startWriteParam();
						break;
					case Constans.CONNECT_IS_JIM :
						showToast(msg.toString());
						break;
					case Constans.TIME_OUT :

						if (RECONNECT_TIME1 > 0) {
							if (mThread != null && !mThread.isInterrupted()) {
								mThread.interrupt();
							}
							startWriteParam();
							RECONNECT_TIME1--;
						} else {
							hideProgressDialog();
							showToast("写入参数超时！");
						}
						break;
				}
			}
		};
	}
}
