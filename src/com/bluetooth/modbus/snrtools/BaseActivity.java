package com.bluetooth.modbus.snrtools;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ab.http.AbHttpUtil;
import com.bluetooth.modbus.snrtools.manager.ActivityManager;
import com.bluetooth.modbus.snrtools.manager.AppStaticVar;
import com.bluetooth.modbus.snrtools.thread.ConnectThread;
import com.bluetooth.modbus.snrtools.uitls.AppUtil;
import com.bluetooth.modbus.snrtools.view.CustomDialog;
import com.bluetooth.modbus.snrtools.view.MyAlertDialog;
import com.bluetooth.modbus.snrtools.view.MyAlertDialog.MyAlertDialogListener;

public abstract class BaseActivity extends Activity {

	public Context mContext;
	private CustomDialog mCustomDialog;
	private MyAlertDialog mDialog, mDialogOne;
	public AbHttpUtil mAbHttpUtil;
	public InnerHandler mInnerHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		ActivityManager.getInstances().addActivity(this);
		mContext = this;
		AppStaticVar.isExit = false;
	}

	public void BackOnClick(View v) {
		switch (v.getId()) {
		case R.id.ivBack:
			ActivityManager.getInstances().finishActivity(this);
		}
	}

	public void BtnRight(View v) {
		rightButtonOnClick(v.getId());
	}

	public void showToast(String msg) {
		Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 所有右侧按钮的触发事件(xml布局文件中必须引入base_title.xml) rlMenu--菜单按钮id
	 * ；btnRight1--文字按钮id；view2--右侧分割线id
	 * 
	 * @author: cchen
	 * @time: 2014-12-26 上午10:44:17
	 * @param id
	 *            按钮的id
	 */
	protected void rightButtonOnClick(int id) {
	}

	/**
	 * 隐藏右侧按钮或者分割线(xml布局文件中必须引入base_title.xml)
	 * 
	 * @author: cchen
	 * @time: 2014-12-26 上午10:47:06
	 * @param id
	 *            需要隐藏的view的id
	 */
	public void hideRightView(int id) {
		View v = findViewById(id);
		if (v != null) {
			v.setVisibility(View.GONE);
		}
	}

	/**
	 * 展示右侧按钮或者分割线(xml布局文件中必须引入base_title.xml)
	 * 
	 * @author: cchen
	 * @time: 2014-12-26 上午10:47:06
	 * @param id
	 *            需要隐藏的view的id
	 */
	public void showRightView(int id) {
		View v = findViewById(id);
		if (v != null) {
			v.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 设置title的内容(xml布局文件中必须引入base_title.xml)
	 * 
	 * @author: cchen
	 * @time: 2014-12-26 上午10:49:52
	 * @param content
	 */
	public void setTitleContent(String content) {
		View v = findViewById(R.id.tvTitle);
		if (v != null && v instanceof TextView) {
			((TextView) v).setText(content);
		}
	}

	/**
	 * 设置右侧button的内容(xml布局文件中必须引入base_title.xml)
	 * 
	 * @author: cchen
	 * @time: 2014-12-26 上午10:49:52
	 * @param content
	 * @param id
	 *            需要设置的button的id
	 */
	public void setRightButtonContent(String content, int id) {
		View v = findViewById(id);
		if (v != null && v instanceof Button) {
			((Button) v).setText(content);
		}
	}

	public void showDialogOne(String msg, MyAlertDialogListener listener) {
		mDialogOne = new MyAlertDialog(this, "", msg, MyAlertDialog.TYPE_ONE, listener);
		mDialogOne.setMessage(msg);
		mDialogOne.setListener(listener);
		mDialogOne.show();
	}

	public void hideDialogOne() {
		if (mDialogOne != null && mDialogOne.isShowing()) {
			mDialogOne.dismiss();
			mDialogOne = null;
		}
	}

	public void hideDialog() {
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
			mDialog = null;
		}
	}

	public void showDialog(String msg, MyAlertDialogListener listener) {
		mDialog = new MyAlertDialog(this, "", msg, MyAlertDialog.TYPE_TWO, null);
		mDialog.setButtonContent(getResources().getString(R.string.string_cancel), MyAlertDialog.BUTTON_CANCEL);
		mDialog.setButtonContent(getResources().getString(R.string.string_ok), MyAlertDialog.BUTTON_OK);
		mDialog.setListener(listener);
		mDialog.setMessage(msg);
		mDialog.show();
	}

	public void showDialog(String msg, String cancelText, String okText, MyAlertDialogListener listener) {
		mDialog = new MyAlertDialog(this, "", msg, MyAlertDialog.TYPE_TWO, null);
		mDialog.setButtonContent(cancelText, MyAlertDialog.BUTTON_CANCEL);
		mDialog.setButtonContent(okText, MyAlertDialog.BUTTON_OK);
		mDialog.setListener(listener);
		mDialog.setMessage(msg);
		mDialog.show();
	}

	public void showProgressDialog() {
		showProgressDialog(null, false);
	}

	public void showProgressDialog(String msg) {
		showProgressDialog(msg, false);
	}

	public void showProgressDialog(boolean isCancel) {
		showProgressDialog(null, isCancel);
	}

	public void showProgressDialog(String msg, boolean isCancel) {
		if (mCustomDialog == null) {
			mCustomDialog = new CustomDialog(mContext);
		}
		if (!TextUtils.isEmpty(msg)) {
			mCustomDialog.setMessage(msg);
		} else {
			mCustomDialog.setTitle(getResources().getString(R.string.string_loading));
		}
		mCustomDialog.show(isCancel);
	}

	public abstract void reconnectSuccss();

	private AlertDialog dialog;

	public void showConnectDevice() {
		if (AppStaticVar.isExit) {
			return;
		}
		showDialogOne(getResources().getString(R.string.string_error_msg12), new MyAlertDialogListener() {

			@Override
			public void onClick(View view) {
				switch (view.getId()) {
				case R.id.btnOkOne:
					// finish();
					Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					System.exit(0);
					break;
				// case R.id.btnCancel :
				// hideDialog();
				// break;
				// case R.id.btnOk :
				// connectDevice(AppStaticVar.mCurrentAddress);
				// break;
				}
			}
		});

	}

	public void connectDevice(String address) {
		if (!TextUtils.isEmpty(address)) {
			AppStaticVar.mBtAdapter.cancelDiscovery();
			final BluetoothDevice device = AppStaticVar.mBtAdapter.getRemoteDevice(address);
			ConnectThread connectThread = new ConnectThread(device, new Handler() {
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case Constans.CONNECTING_DEVICE:
						System.out.println("=====开始连接=====");
						showProgressDialog(msg.obj.toString());
						break;
					case Constans.CONNECT_DEVICE_SUCCESS:
						System.out.println("=====连接成功=====");
						hideProgressDialog();
						reconnectSuccss();
						break;
					case Constans.CONNECT_DEVICE_FAILED:
						System.out.println("=====连接失败=====");
						hideProgressDialog();
						showToast(msg.obj.toString());
						break;
					}
				}
			});
			connectThread.start();
		} else {
			Toast.makeText(mContext, getResources().getString(R.string.string_error_msg13), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 描述：对话框dialog （确认，取消）.
	 * 
	 * @param title
	 *            对话框标题内容
	 * @param view
	 *            对话框提示内容
	 * @param mOkOnClickListener
	 *            点击确认按钮的事件监听
	 * @return the alert dialog
	 */
	public AlertDialog showDialog(String title, View view, DialogInterface.OnClickListener mOkOnClickListener) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle(title);
		builder.setView(view);
		builder.setPositiveButton(getResources().getString(R.string.string_ok), mOkOnClickListener);
		builder.setNegativeButton(getResources().getString(R.string.string_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog mAlertDialog = builder.create();
		mAlertDialog.show();
		return mAlertDialog;
	}

	/**
	 * 描述：对话框dialog （无按钮）.
	 * 
	 * @param title
	 *            对话框标题内容
	 * @param view
	 *            对话框提示内容
	 * @return the alert dialog
	 */
	public AlertDialog showDialog(String title, View view) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle(title);
		builder.setView(view);
		builder.create();
		AlertDialog mAlertDialog = builder.create();
		mAlertDialog.show();
		return mAlertDialog;
	}

	public void hideProgressDialog() {
		if (mCustomDialog != null && mCustomDialog.isShowing()) {
			mCustomDialog.dismiss();
			mCustomDialog = null;
		}
	}

	@Override
	protected void onPause() {
		hideProgressDialog();
		hideDialog();
		hideDialogOne();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		hideProgressDialog();
		hideDialog();
		hideDialogOne();
		super.onDestroy();
	}

	public void exitApp() {

		showDialog(getResources().getString(R.string.string_exit_app), new MyAlertDialogListener() {
			@Override
			public void onClick(View view) {
				switch (view.getId()) {
				case R.id.btnCancel:
					hideDialog();
					break;
				case R.id.btnOk:
					AppUtil.closeBluetooth();
					ActivityManager.getInstances().finishAll();
					System.exit(0);
					break;
				}
			}
		});

	}

	public void handleMessage(Activity activity, Message msg, String name) {

	}

	static class InnerHandler extends Handler {
		private WeakReference<Activity> wr;
		private String name;

		public InnerHandler(Activity activity, String name) {
			wr = new WeakReference<Activity>(activity);
			this.name = name;
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Activity a = wr.get();
			if (a == null) {
				return;
			}
			((BaseActivity) a).handleMessage(a, msg, name);
		}
	}
}
