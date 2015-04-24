package com.bluetooth.modbus.snrtools;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.bluetooth.modbus.snrtools.bean.ZFLJDW;
import com.bluetooth.modbus.snrtools.manager.AppStaticVar;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.snr_main_activity);
		initUI();
		setTitleContent(AppStaticVar.mCurrentName);
		setRightButtonContent("设置", R.id.btnRight1);
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
				showProgressDialog("设备通讯中,请稍后...");
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
					((Button)v).setText("更多");
				}else{
					mViewMore.setVisibility(View.VISIBLE);
					((Button)v).setText("收起");
				}
				break;
		}
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
			hasAlarm("流量上限");
		} else {
			hasNoAlarm("流量上限");
		}
		// 流量下限报警
		String llxxbj = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("流量下限报警==" + llxxbj);
		if (Long.parseLong(llxxbj, 16) == 1) {
			hasAlarm("流量下限");
		} else {
			hasNoAlarm("流量下限");
		}
		// 励磁异常报警
		String lcbj = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("励磁异常报警==" + lcbj);
		if (Long.parseLong(lcbj, 16) == 1) {
			hasAlarm("励磁异常");
		} else {
			hasNoAlarm("励磁异常");
		}
		// 空管报警
		String kgbj = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("空管报警==" + kgbj);
		if (Long.parseLong(kgbj, 16) == 1) {
			hasAlarm("空管报警");
		} else {
			hasNoAlarm("空管报警");
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
				showToast("连接设备超时!");
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
