package com.bluetooth.modbus.snrtools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ab.http.AbFileHttpResponseListener;
import com.ab.http.AbHttpUtil;
import com.ab.util.AbAppUtil;
import com.ab.util.AbFileUtil;
import com.ab.view.progress.AbHorizontalProgressBar;
import com.bluetooth.modbus.snrtools.bean.ZFLJDW;
import com.bluetooth.modbus.snrtools.manager.AppStaticVar;
import com.bluetooth.modbus.snrtools.uitls.AppUtil;
import com.bluetooth.modbus.snrtools.uitls.ModbusUtils;
import com.bluetooth.modbus.snrtools.uitls.NumberBytes;
import com.bluetooth.modbus.snrtools.view.NoFocuseTextview;

public class SNRMainActivity extends BaseActivity {

//	private Handler mHandler;
	private Thread mThread;
	private TextView mParam1, mParam2, mParam3, mParam4, mParam5, mParam6,
			mParam7;
	private NoFocuseTextview mTvAlarm;
	private View mViewMore;
	private boolean isPause = false;
	private boolean isSetting = false;
	private PopupWindow mPop;
	private AbHorizontalProgressBar mAbProgressBar;
	// 最大100
	private int max = 100;
	private int progress = 0;
	private TextView numberText, maxText;
	private AlertDialog mAlertDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.snr_main_activity);
		mAbHttpUtil = AbHttpUtil.getInstance(this);
		initUI();
		setTitleContent(AppStaticVar.mCurrentName);
		setRightButtonContent(getResources().getString(R.string.string_settings), R.id.btnRight1);
		hideRightView(R.id.view2);
		hideRightView(R.id.btnRight1);
		showRightView(R.id.rlMenu);
		initHandler();
	}

	@Override
	public void rightButtonOnClick(int id) {
		switch (id) {
			case R.id.btnRight1 :
				isPause = true;
				isSetting = true;
				showProgressDialog(getResources().getString(R.string.string_progressmsg1));
				break;
			case R.id.rlMenu :
				 showMenu(findViewById(id));
				break;
		}
	}

	private void startReadParam() {
		mThread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (!isPause) {
					ModbusUtils.readStatus(mContext.getClass().getSimpleName(),
							mInnerHandler);
				}
			}
		});
		mThread.start();
	}
	
	public void onClick(View v){
		switch(v.getId()){
			case R.id.btnMore:
				if(mViewMore.getVisibility() == View.VISIBLE){
					mViewMore.setVisibility(View.GONE);
					((Button)v).setText(getResources().getString(R.string.string_more));
				}else{
					mViewMore.setVisibility(View.VISIBLE);
					((Button)v).setText(getResources().getString(R.string.string_shouqi));
				}
				break;

			case R.id.textView1 :// 新功能
				hideMenu();
				showDialogOne(getResources().getString(R.string.string_menu_msg1), null);
				break;
			case R.id.textView2 :// 关于
				hideMenu();
				showDialogOne(getResources().getString(R.string.string_menu_msg2), null);
				break;
			case R.id.textView3 :// 版本更新
				hideMenu();
				downloadXml();
				break;
			case R.id.textView4 :// 退出
				hideMenu();
				exitApp();
				break;
			case R.id.textView5:// 清除缓存
				hideMenu();
				AbFileUtil.deleteFile(new File(AbFileUtil.getFileDownloadDir(mContext)));
				AbFileUtil.deleteFile(new File(Constans.Directory.DOWNLOAD));
				break;
		
		}
	}
	
	private void showMenu(View v) {
		if (mPop == null) {
			View contentView = View.inflate(this, R.layout.main_menu, null);
			mPop = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			mPop.setBackgroundDrawable(new BitmapDrawable());
			mPop.setOutsideTouchable(true);
			mPop.setFocusable(true);
		}
		mPop.showAsDropDown(v, R.dimen.menu_x, 20);
	}

	private void hideMenu() {
		if (mPop != null && mPop.isShowing()) {
			mPop.dismiss();
		}
	}

	private void downloadXml() {
		String url = "http://www.sinier.com.cn/download/version.xml";
		mAbHttpUtil.get(url, new AbFileHttpResponseListener(url) {
			// 获取数据成功会调用这里
			@Override
			public void onSuccess(int statusCode, File file) {
				int version = 0;
				String url = "";
				String md5 = "";
				XmlPullParser xpp = Xml.newPullParser();
				try {
					xpp.setInput(new FileInputStream(file), "utf-8");

					int eventType = xpp.getEventType();
					while (eventType != XmlPullParser.END_DOCUMENT) {
						switch (eventType) {
							case XmlPullParser.START_TAG :
								if ("version".equals(xpp.getName())) {
									try {
										version = Integer.parseInt(xpp.nextText());
									} catch (NumberFormatException e1) {
										e1.printStackTrace();
										showToast(getResources().getString(R.string.string_error_msg1));
									}
								}
								if ("url".equals(xpp.getName())) {
									url = xpp.nextText();
								}
								if ("MD5".equals(xpp.getName())) {
									md5 = xpp.nextText();
								}
								break;
							default :
								break;
						}
						eventType = xpp.next();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				PackageManager manager;
				PackageInfo info = null;
				manager = getPackageManager();
				try {
					info = manager.getPackageInfo(getPackageName(), 0);
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
				if (version != info.versionCode) {
					String fileName = url.substring(url.lastIndexOf("/") + 1);
					File apk = new File(Constans.Directory.DOWNLOAD + fileName);
					if (md5.equals(AppUtil.getFileMD5(apk))) {
//						Intent intent = new Intent(Intent.ACTION_VIEW);
//						intent.setDataAndType(Uri.fromFile(apk),
//								"application/vnd.android.package-archive");
//						startActivity(intent);
						AbAppUtil.installApk(mContext, apk);
						return;
					}
					try {
						if (!apk.getParentFile().exists()) {
							apk.getParentFile().mkdirs();
						}
						apk.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
					mAbHttpUtil.get(url, new AbFileHttpResponseListener(apk) {
						public void onSuccess(int statusCode, File file) {
//							Intent intent = new Intent(Intent.ACTION_VIEW);
//							intent.setDataAndType(Uri.fromFile(file),
//									"application/vnd.android.package-archive");
//							startActivity(intent);
							AbAppUtil.installApk(mContext, file);
						};

						// 开始执行前
						@Override
						public void onStart() {
							// 打开进度框
							View v = LayoutInflater.from(mContext).inflate(
									R.layout.progress_bar_horizontal, null,
									false);
							mAbProgressBar = (AbHorizontalProgressBar) v
									.findViewById(R.id.horizontalProgressBar);
							numberText = (TextView) v.findViewById(R.id.numberText);
							maxText = (TextView) v.findViewById(R.id.maxText);

							maxText.setText(progress + "/"+ String.valueOf(max)+"%");
							mAbProgressBar.setMax(max);
							mAbProgressBar.setProgress(progress);

							mAlertDialog = showDialog(getResources().getString(R.string.string_progressmsg2), v);
						}

						// 失败，调用
						@Override
						public void onFailure(int statusCode, String content,
								Throwable error) {
							showToast(error.getMessage());
						}

						// 下载进度
						@Override
						public void onProgress(long bytesWritten, long totalSize) {
							if (totalSize / max == 0) {
								onFinish();
								showToast(getResources().getString(R.string.string_error_msg2));
								return;
							}
							maxText.setText(bytesWritten / (totalSize / max)
									+ "/" + max+"%");
							mAbProgressBar
									.setProgress((int) (bytesWritten / (totalSize / max)));
						}

						// 完成后调用，失败，成功
						public void onFinish() {
							// 下载完成取消进度框
							if (mAlertDialog != null) {
								mAlertDialog.cancel();
								mAlertDialog = null;
							}

						};
					});
				} else {
					showToast(getResources().getString(R.string.string_tips_msg1));
				}

			}

			// 开始执行前
			@Override
			public void onStart() {
				// 打开进度框
				View v = LayoutInflater.from(mContext).inflate(
						R.layout.progress_bar_horizontal, null, false);
				mAbProgressBar = (AbHorizontalProgressBar) v
						.findViewById(R.id.horizontalProgressBar);
				numberText = (TextView) v.findViewById(R.id.numberText);
				maxText = (TextView) v.findViewById(R.id.maxText);

				maxText.setText(progress + "/" + String.valueOf(max)+"%");
				mAbProgressBar.setMax(max);
				mAbProgressBar.setProgress(progress);

				mAlertDialog = showDialog(getResources().getString(R.string.string_progressmsg2), v);
			}

			// 失败，调用
			@Override
			public void onFailure(int statusCode, String content,Throwable error) {
				showToast(error.getMessage());
			}

			// 下载进度
			@Override
			public void onProgress(long bytesWritten, long totalSize) {
				if (totalSize / max == 0) {
					onFinish();
					showToast(getResources().getString(R.string.string_error_msg2));
					return;
				}
				maxText.setText(bytesWritten / (totalSize / max) + "/" + max+"%");
				mAbProgressBar.setProgress((int) (bytesWritten / (totalSize / max)));
			}

			// 完成后调用，失败，成功
			public void onFinish() {
				// 下载完成取消进度框
				if (mAlertDialog != null) {
					mAlertDialog.cancel();
					mAlertDialog = null;
				}
			};
		});
	}


	private void initUI() {
		mParam1 = (TextView) findViewById(R.id.param1);
		mParam2 = (TextView) findViewById(R.id.param2);
		mParam3 = (TextView) findViewById(R.id.param3);
		mParam4 = (TextView) findViewById(R.id.param4);
		mParam5 = (TextView) findViewById(R.id.param5);
		mParam6 = (TextView) findViewById(R.id.param6);
		mParam7 = (TextView) findViewById(R.id.param7);
		mViewMore = findViewById(R.id.llMore);
		mTvAlarm = (NoFocuseTextview) findViewById(R.id.tvAlarm);
		mTvAlarm.setVisibility(View.GONE);
		mTvAlarm.startAnimation(AnimationUtils.loadAnimation(mContext,
				R.anim.anim_alpha));
	}

	private void hasAlarm(String s) {
		if (mTvAlarm.getVisibility() != View.VISIBLE) {
			mTvAlarm.setVisibility(View.VISIBLE);
		}
		if (!mTvAlarm.getText().toString().contains(s)) {
			mTvAlarm.setText(mTvAlarm.getText() + " " + s);
		}
	}

	private void hasNoAlarm(String s) {
		mTvAlarm.setText(mTvAlarm.getText().toString().replace(" " + s, ""));
		if (TextUtils.isEmpty(mTvAlarm.getText().toString().trim())) {
			mTvAlarm.setVisibility(View.GONE);
		}
	}

	private String getSsllDw(String s) {
		System.out.println("瞬时流量单位====" + s);
		String dw = "";
		s = s.replace("0", "");
		if ("".equals(s)) {
			dw = "L/h";
		} else if ("1".equals(s)) {
			dw = "L/m";
		} else if ("2".equals(s)) {
			dw = "L/s";
		} else if ("3".equals(s)) {
			dw = "m³/h";
		} else if ("4".equals(s)) {
			dw = "m³/m";
		} else if ("5".equals(s)) {
			dw = "m³/s";
		}
		return dw;
	}

	private ZFLJDW getZFDw(String s) {
		System.out.println("正反累积单位====" + s);
		ZFLJDW dw = null;
		s = s.replace("0", "");
		if ("".equals(s)) {
			dw = new ZFLJDW("m³", 3);
		} else if ("1".equals(s)) {
			dw = new ZFLJDW("m³", 2);
		} else if ("2".equals(s)) {
			dw = new ZFLJDW("m³", 1);
		} else if ("3".equals(s)) {
			dw = new ZFLJDW("m³", 0);
		} else if ("4".equals(s)) {
			dw = new ZFLJDW("L", 3);
		} else if ("5".equals(s)) {
			dw = new ZFLJDW("L", 2);
		} else if ("6".equals(s)) {
			dw = new ZFLJDW("L", 1);
		} else if ("7".equals(s)) {
			dw = new ZFLJDW("L", 0);
		}
		return dw;
	}

	private void dealReturnMsg(String msg) {
		if (msg.length() != ModbusUtils.MSG_STATUS_COUNT) {
			return;
		}
		int paramIndex = 0;
		// 瞬时流量浮点值
		String ssllL = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		String ssllH = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("瞬时流量==" + NumberBytes.hexStrToFloat(ssllH + ssllL));
		// 瞬时流速浮点值
		String sslsL = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		String sslsH = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("瞬时流速==" + NumberBytes.hexStrToFloat(sslsH + sslsL));
		String sslsT = NumberBytes.hexStrToFloat(sslsH + sslsL) + " m/s";
		mParam2.setText(sslsT);
		// 流量百分比浮点值
		String llbfbL = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		String llbfbH = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("流量百分比=="
				+ NumberBytes.hexStrToFloat(llbfbH + llbfbL));
		String llbfbT = NumberBytes.hexStrToFloat(llbfbH + llbfbL) + " %";
		mParam3.setText(llbfbT);
		// 流体电导比浮点值
		String ltddbL = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		String ltddbH = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("流体电导比=="
				+ NumberBytes.hexStrToFloat(ltddbH + ltddbL));
		String ltddbT = NumberBytes.hexStrToFloat(ltddbH + ltddbL) + " %";
		mParam4.setText(ltddbT);
		// 正向累积数值整数值
		String zxljintL = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		String zxljintH = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		long zxljLong = Long.parseLong(zxljintH + zxljintL, 16);
		System.out.println("正向累积数值整数值=="
				+ Long.parseLong(zxljintH + zxljintL, 16));
		// 正向累积数值小数值
		String zxljfloatL = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		String zxljfloatH = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("正向累积数值小数值=="
				+ NumberBytes.hexStrToFloat(zxljfloatH + zxljfloatL));
		float zxljFloat = NumberBytes.hexStrToFloat(zxljfloatH
				+ zxljfloatL);
		// 反向累积数值整数值
		String fxljintL = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		String fxljintH = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("反向累积数值整数值=="
				+ Long.parseLong(fxljintH + fxljintL, 16));
		long fxljLong = Long.parseLong(fxljintH + fxljintL, 16);
		// 反向累积数值小数值
		String fxljfloatL = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		String fxljfloatH = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("反向累积数值小数值=="
				+ NumberBytes.hexStrToFloat(fxljfloatH + fxljfloatL));
		float fxljFloat = NumberBytes.hexStrToFloat(fxljfloatH+ fxljfloatL);

		// 正反向累积差值整数值
		String zfljintL = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		String zfljintH = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("正反向累积差值整数值=="
				+ Long.parseLong(zfljintH + zfljintL, 16));
		long zfljLong = Long.parseLong(zfljintH + zfljintL, 16);
		// 正反向累积差值小数值
		String zfljfloatL = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		String zfljfloatH = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("正反向累积差值小数值=="
				+ NumberBytes.hexStrToFloat(zfljfloatH + zfljfloatL));
		float zfljFloat = NumberBytes.hexStrToFloat(zfljfloatH+ zfljfloatL);

		String sslldw = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("瞬时流量单位==" + sslldw);
		String ssllT = NumberBytes.hexStrToFloat(ssllH + ssllL) + " "
				+ getSsllDw(sslldw);
		mParam1.setText(ssllT);
		// 正向，反向累积单位
		String ljdw = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("正向，反向累积单位==" + ljdw);
		ZFLJDW zfljdw = getZFDw(ljdw);
		if(zfljdw == null){
			zfljdw = new ZFLJDW("", 3);
		}
		
//		String zxljT = zxljIntString
//				+ zxljFloatString.substring(zxljFloatString.indexOf(".")) + " "
//				+ getZFDw(ljdw);
		String zxljT = zxljLong + (zfljdw.point==0?"":String.format("%."+zfljdw.point+"f", zxljFloat).substring(String.format("%."+zfljdw.point+"f", zxljFloat).indexOf(".")))
				+ zfljdw.dw;
		mParam5.setText(zxljT);
		String fxljt = fxljLong + (zfljdw.point==0?"":String.format("%."+zfljdw.point+"f", fxljFloat).substring(String.format("%."+zfljdw.point+"f", fxljFloat).indexOf(".")))
				+ zfljdw.dw;
//		String fxljt = fxljIntString
//				+ fxljFloatString.substring(fxljFloatString.indexOf(".")) + " "
//				+ getZFDw(ljdw);
		mParam6.setText(fxljt);

		String zfljt = zfljLong + (zfljdw.point==0?"":String.format("%."+zfljdw.point+"f", zfljFloat).substring(String.format("%."+zfljdw.point+"f", zfljFloat).indexOf(".")))
				+ zfljdw.dw;
//		String zfljt = zfljIntString
//				+ zfljFloatString.substring(zfljFloatString.indexOf(".")) + " "
//				+ getZFDw(ljdw);
		mParam7.setText(zfljt);

		// mParam7.setText(String.format(
		// "%.3f",
		// Long.parseLong(fxljintH + fxljintL, 16)
		// + NumberBytes.hexStrToFloat(fxljfloatH + fxljfloatL)
		// - Long.parseLong(fxljintH + fxljintL, 16)
		// - NumberBytes.hexStrToFloat(fxljfloatH + fxljfloatL))
		// + " " + getZFDw(ljdw));

		// 流量上限报警
		String llsxbj = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("流量上限报警==" + llsxbj);
		if (Long.parseLong(llsxbj, 16) == 1) {
			hasAlarm(getResources().getString(R.string.string_alarm_llsx));
		} else {
			hasNoAlarm(getResources().getString(R.string.string_alarm_llsx));
		}
		// 流量下限报警
		String llxxbj = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("流量下限报警==" + llxxbj);
		if (Long.parseLong(llxxbj, 16) == 1) {
			hasAlarm(getResources().getString(R.string.string_alarm_llxx));
		} else {
			hasNoAlarm(getResources().getString(R.string.string_alarm_llxx));
		}
		// 励磁异常报警
		String lcbj = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("励磁异常报警==" + lcbj);
		if (Long.parseLong(lcbj, 16) == 1) {
			hasAlarm(getResources().getString(R.string.string_alarm_lcyc));
		} else {
			hasNoAlarm(getResources().getString(R.string.string_alarm_lcyc));
		}
		// 空管报警
		String kgbj = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("空管报警==" + kgbj);
		if (Long.parseLong(kgbj, 16) == 1) {
			hasAlarm(getResources().getString(R.string.string_alarm_kgbj));
		} else {
			hasNoAlarm(getResources().getString(R.string.string_alarm_kgbj));
		}

	}
	
	@Override
	public void handleMessage(Activity activity, Message msg, String name)
	{
		super.handleMessage(activity, msg, name);

		switch (msg.what) {
			case Constans.CONTACT_START :
				System.out.println(name+"开始读取数据=====");
				break;
			case Constans.NO_DEVICE_CONNECTED :
				System.out.println(name+"连接失败=====");
				if (isPause) {
					AppStaticVar.mObservable.notifyObservers();
				} else {
					showConnectDevice();
				}
				break;
			case Constans.DEVICE_RETURN_MSG :
				System.out.println(name+"收到数据=====" + msg.obj.toString());
				dealReturnMsg(msg.obj.toString());
				if (isPause) {
					AppStaticVar.mObservable.notifyObservers();
				} else {
					startReadParam();
				}
				break;
			case Constans.CONNECT_IS_CLOSED :
				System.out.println(name+"连接关闭=====");
				isPause = true;
				showConnectDevice();
			case Constans.ERROR_START :
				System.out.println(name+"接收数据错误=====");
				if (isPause) {
					AppStaticVar.mObservable.notifyObservers();
				} else {
					startReadParam();
				}
				break;
			case Constans.TIME_OUT :
				System.out.println(name+"连接超时=====");
				if (mThread != null && !mThread.isInterrupted()) {
					mThread.interrupt();
				}
				showToast(getResources().getString(R.string.string_error_msg3));
				startReadParam();
				break;
		}
	
	}

	private void initHandler() {
		mInnerHandler = new InnerHandler(this, "主页面");
	}

	@Override
	public void reconnectSuccss() {
		isPause = false;
		startReadParam();
	}

	@Override
	protected void onResume() {
		isPause = false;
		startReadParam();
		super.onResume();
	}

	@Override
	protected void onPause() {
		isPause = true;
		super.onPause();
	}

}
