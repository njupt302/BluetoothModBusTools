package com.bluetooth.modbus.snrtools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.ab.util.AbAppUtil;
import com.bluetooth.modbus.snrtools.adapter.ParameterAdapter;
import com.bluetooth.modbus.snrtools.bean.Parameter;
import com.bluetooth.modbus.snrtools.bean.Selector;
import com.bluetooth.modbus.snrtools.manager.AppStaticVar;
import com.bluetooth.modbus.snrtools.uitls.ModbusUtils;
import com.bluetooth.modbus.snrtools.uitls.NumberBytes;

public class CheckPasswordActivity extends BaseActivity implements Observer {
	private Handler mHandler;
	private Thread mThread;
	private EditText editText1, editText2;
	private List<Parameter> mList;
	private int reconnectCount = 3;
	private boolean isClear = false;
	private View mViewCheckPsd, mViewSetParam;

	private ListView mListview;
	private ParameterAdapter mAdapter;
	private List<Parameter> mDataList;
	private final static int SELECT_PARAM = 0x100001;
	private final static int INPUT_PARAM = 0x100002;
	/** 显示参数的数量 */
	private int mCount = 0;
	private boolean flag = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mList = new ArrayList<Parameter>();
		setContentView(R.layout.check_pass_activity);
		setTitleContent("密码校验");
		hideRightView(R.id.btnRight1);
		hideRightView(R.id.view2);
		initHandler();
		editText1 = (EditText) findViewById(R.id.editText1);
		editText2 = (EditText) findViewById(R.id.editText2);
		mViewCheckPsd = findViewById(R.id.checkpsd);
		mViewSetParam = findViewById(R.id.setparam);
		mViewSetParam.setVisibility(View.GONE);
		initUI();
		setListeners();
		AppStaticVar.mObservable.addObserver(this);
		showProgressDialog("与设备通讯中...");
//		startReadParam();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button2:
			if (AppStaticVar.mParamList == null || AppStaticVar.mParamList.size() == 0) {
				showToast("获取参数失败,请返回重试!");
				return;
			}
			if (TextUtils.isEmpty(editText1.getText().toString().trim())) {
				showToast("请输入密码!");
				return;
			}
			checkPsw(Long.parseLong(editText1.getText().toString().trim()));
			if (AppStaticVar.PASSWORD_LEVEAL != -1) {
				mViewCheckPsd.setVisibility(View.GONE);
				mViewSetParam.setVisibility(View.VISIBLE);
				setTitleContent("参数设置");
				switch (AppStaticVar.PASSWORD_LEVEAL) {
				case 1:// 可以设置1-24
					mCount = AppStaticVar.PASSWORD_LEVEAL1_COUNT;
					break;
				case 2:// 可以设置1-25
					mCount = AppStaticVar.PASSWORD_LEVEAL2_COUNT;
					break;
				case 3:// 可以设置1-38
					mCount = AppStaticVar.PASSWORD_LEVEAL3_COUNT;
					break;
				case 4:// 可以设置1-60
					mCount = AppStaticVar.PASSWORD_LEVEAL4_COUNT;
					break;
				case 5:// // 超级密码
					mCount = mList.size();
					break;
				}
				mDataList.addAll(mList.subList(0, mCount));
				editText1.setText("");
				AbAppUtil.closeSoftInput(mContext);
				// Intent intent = new
				// Intent(mContext,ParamSettingActivity.class);
				// startActivity(intent);
				// finish();
			} else {
				Toast.makeText(mContext, "密码不正确!", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.button3:
			if (TextUtils.isEmpty(editText2.getText().toString().trim())) {
				showToast("请输入密码!");
				return;
			}
			if (Constans.PasswordLevel.LEVEL_6 == Long.parseLong(editText2.getText().toString())) {
				isClear = true;
				editText2.setText("");
				ModbusUtils.clearZL("总量清零", mHandler);
			} else {
				Toast.makeText(mContext, "总量清零密码不正确!", Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

	private void startReadParam() {
		mThread = new Thread(new Runnable() {

			@Override
			public void run() {
				ModbusUtils.readParameter(mContext.getClass().getSimpleName(), mHandler);
			}
		});
		mThread.start();
	}

	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case Constans.CONTACT_START:
					showProgressDialog("与设备通讯中...");
					System.out.println("=====参数开始读取数据");
					break;
				case Constans.NO_DEVICE_CONNECTED:
					System.out.println("=====参数没有设备连接");
					break;
				case Constans.DEVICE_RETURN_MSG:
					hideProgressDialog();
					System.out.println("参数收到的数据=====" + msg.obj.toString());

					if (isClear) {
						isClear = false;
						if (msg.obj.toString().length() != 16) {
							showToast("总量清零失败，请重试!");
							return;
						}
						showToast("总量清零成功!");
					} else {
						dealReturnMsg(msg.obj.toString());
					}
					break;
				case Constans.CONNECT_IS_CLOSED:
					System.out.println("=====参数连接断开");
					showConnectDevice();
					break;
				case Constans.ERROR_START:
					System.out.println("=====参数接收错误");
					if (reconnectCount > 0) {
						if (mThread != null && !mThread.isInterrupted()) {
							mThread.interrupt();
						}
						startReadParam();
						reconnectCount--;
					} else {
						hideProgressDialog();
						showToast("读取数据超时！");
					}
					break;
				case Constans.TIME_OUT:
					System.out.println("主页面连接超时=====");
					if (reconnectCount > 0) {
						if (mThread != null && !mThread.isInterrupted()) {
							mThread.interrupt();
						}
						startReadParam();
						reconnectCount--;
					} else {
						hideProgressDialog();
						showToast("读取数据超时！");
					}
					break;
				}
			}
		};
	}

	private void dealReturnMsg(String msg) {
		if (msg.length() != ModbusUtils.MSG_PARAM_COUNT) {
			return;
		}
		mList.clear();
		Parameter parameter = null;
		ArrayList<Selector> selectorList = null;
		Selector selector = null;
		int paramIndex = 0;
		int paramCountLabel = 1;
		String param1,param2,param3 = "";
		/********************************** 基本参数 **************************************/
		/********************************** 参数1--语言 **************************************/
		mList.add(new Parameter(true, "基本参数"));
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0000";
		String param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--语言==" + Integer.parseInt(param, 16));
		parameter.count = "0001";
		parameter.name = "语言";
		parameter.type = 1;
		parameter.valueIn = Integer.parseInt(param, 16);
		switch ((Integer) parameter.valueIn) {
		case 0:
			parameter.value = "简体中文";
			break;
		case 1:
			parameter.value = "English";
			break;
		}
		selectorList = new ArrayList<Selector>();

		selector = new Selector();
		selector.name = "简体中文";
		selector.value = "0000";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "English";
		selector.value = "0001";
		selectorList.add(selector);

		parameter.selectors = selectorList;
		mList.add(parameter);
		/********************************** 参数2--流量单位 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0001";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量单位 ==" + Integer.parseInt(param, 16));
		parameter.count = "0001";//		parameter.count = "0001";
		parameter.name = "流量单位";
		parameter.type = 1;
		parameter.valueIn = Integer.parseInt(param, 16);
		switch ((Integer) parameter.valueIn) {
		case 0:
			parameter.value = "L/h";
			break;
		case 1:
			parameter.value = "L/mim";
			break;
		case 2:
			parameter.value = "L/s";
			break;
		case 3:
			parameter.value = "m³/h";
			break;
		case 4:
			parameter.value = "m³/min";
			break;
		case 5:
			parameter.value = "m³/s";
			break;
		}
		selectorList = new ArrayList<Selector>();

		selector = new Selector();
		selector.name = "L/h";
		selector.value = "0000";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "L/min";
		selector.value = "0001";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "L/s";
		selector.value = "0002";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "m³/h";
		selector.value = "0003";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "m³/min";
		selector.value = "0004";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "m³/s";
		selector.value = "0005";
		selectorList.add(selector);

		parameter.selectors = selectorList;
		mList.add(parameter);

		/********************************** 参数3--仪表量程设置 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0002";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		param1 = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--仪表量程设置==" + NumberBytes.hexStrToLong(param1+param));
		parameter.count = "0002";
		parameter.name = "仪表量程设置";
		parameter.type = 3;
		parameter.maxValue = 99999;
		parameter.minValue = 0;
		parameter.valueIn = NumberBytes.hexStrToLong(param1+param);
		parameter.value = NumberBytes.hexStrToLong(param1+param) + "";
		mList.add(parameter);
		/********************************** 参数4--流量方向择项 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0004";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量方向择项==" + Integer.parseInt(param, 16));
		parameter.count = "0001";
		parameter.name = "流量方向择项";
		parameter.type = 1;
		parameter.valueIn = Integer.parseInt(param, 16);
		switch ((Integer) parameter.valueIn) {
		case 0:
			parameter.value = "正向";
			break;
		case 1:
			parameter.value = "反向";
			break;
		}
		selectorList = new ArrayList<Selector>();

		selector = new Selector();
		selector.name = "正向";
		selector.value = "0000";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "反向";
		selector.value = "0001";
		selectorList.add(selector);

		parameter.selectors = selectorList;
		mList.add(parameter);
		/********************************** 参数5--反向输出允许 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0005";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--反向输出允许==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "反向输出允许";
		parameter.type = 1;
		parameter.valueIn = Integer.parseInt(param, 16);
		switch ((Integer) parameter.valueIn) {
		case 0:
			parameter.value = "允许";
			break;
		case 1:
			parameter.value = "禁止";
			break;
		}
		selectorList = new ArrayList<Selector>();

		selector = new Selector();
		selector.name = "允许";
		selector.value = "0000";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "禁止";
		selector.value = "0001";
		selectorList.add(selector);

		parameter.selectors = selectorList;
		mList.add(parameter);

		/********************************** 参数6--流量积算单位 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0006";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量积算单位==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "流量积算单位";
		parameter.type = 1;
		parameter.valueIn = Integer.parseInt(param, 16);
		switch ((Integer) parameter.valueIn) {
		case 0:
			parameter.value = "0.001m3";
			break;
		case 1:
			parameter.value = "0.01m3";
			break;
		case 2:
			parameter.value = "0.1m3";
			break;
		case 3:
			parameter.value = "1m3";
			break;
		case 4:
			parameter.value = "0.001L";
			break;
		case 5:
			parameter.value = "0.01L";
			break;
		case 6:
			parameter.value = "0.1L";
			break;
		case 7:
			parameter.value = "1L";
			break;
		}
		selectorList = new ArrayList<Selector>();

		selector = new Selector();
		selector.name = "0.001m3";
		selector.value = "0000";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "0.01m3";
		selector.value = "0001";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "0.1m3";
		selector.value = "0002";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "1m3";
		selector.value = "0003";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "0.001L";
		selector.value = "0004";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "0.01L";
		selector.value = "0005";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "0.1L";
		selector.value = "0006";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "1L";
		selector.value = "0007";
		selectorList.add(selector);

		parameter.selectors = selectorList;
		mList.add(parameter);
		/********************************** 参数7--测量阻尼时间 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0007";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--测量阻尼时间 ==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "测量阻尼时间";
		parameter.type = 1;
		parameter.valueIn = Integer.parseInt(param, 16);
		switch ((Integer) parameter.valueIn) {
		case 0:
			parameter.value = "1s";
			break;
		case 1:
			parameter.value = "2s";
			break;
		case 2:
			parameter.value = "3s";
			break;
		case 3:
			parameter.value = "4s";
			break;
		case 4:
			parameter.value = "6s";
			break;
		case 5:
			parameter.value = "8s";
			break;
		case 6:
			parameter.value = "10s";
			break;
		case 7:
			parameter.value = "15s";
			break;
		case 8:
			parameter.value = "30s";
			break;
		case 9:
			parameter.value = "50s";
			break;
		}
		selectorList = new ArrayList<Selector>();

		selector = new Selector();
		selector.name = "1s";
		selector.value = "0000";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "2s";
		selector.value = "0001";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "3s";
		selector.value = "0002";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "4s";
		selector.value = "0003";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "6s";
		selector.value = "0004";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "8s";
		selector.value = "0005";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "10s";
		selector.value = "0006";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "15s";
		selector.value = "0007";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "30s";
		selector.value = "0008";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "50s";
		selector.value = "0009";
		selectorList.add(selector);

		parameter.selectors = selectorList;
		mList.add(parameter);
		/********************************** 参数8--小信号切除点 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0007";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--小信号切除点==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "小信号切除点";
		parameter.type = 2;
		parameter.point = 2;
		parameter.maxValue = 99.99;
		parameter.minValue = 0.0;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn) + "%";
		mList.add(parameter);
		/********************************** 参数9--脉冲输出方式 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0009";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--脉冲输出方式==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "脉冲输出方式";
		parameter.type = 1;
		parameter.valueIn = Integer.parseInt(param, 16);
		switch ((Integer) parameter.valueIn) {
		case 0:
			parameter.value = "频率";
			break;
		case 1:
			parameter.value = "脉冲";
			break;
		}
		selectorList = new ArrayList<Selector>();

		selector = new Selector();
		selector.name = "频率";
		selector.value = "0000";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "脉冲";
		selector.value = "0001";
		selectorList.add(selector);

		parameter.selectors = selectorList;
		mList.add(parameter);
		/********************************** 参数10--脉冲单位当量 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "000A";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--脉冲单位当量==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "脉冲单位当量";
		parameter.type = 1;
		parameter.valueIn = Integer.parseInt(param, 16);
		switch ((Integer) parameter.valueIn) {
		case 0:
			parameter.value = "1.0m³/cp";
			break;
		case 1:
			parameter.value = "0.1m³/cp";
			break;
		case 2:
			parameter.value = "0.01m³/cp";
			break;
		case 3:
			parameter.value = "0.001m³/cp";
			break;
		case 4:
			parameter.value = "1.0L/cp";
			break;
		case 5:
			parameter.value = "0.1L/cp";
			break;
		case 6:
			parameter.value = "0.01L/cp";
			break;
		case 7:
			parameter.value = "0.001L/cp";
			break;
		}
		selectorList = new ArrayList<Selector>();

		selector = new Selector();
		selector.name = "1.0m³/cp";
		selector.value = "0000";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "0.1m³/cp";
		selector.value = "0001";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "0.01m³/cp";
		selector.value = "0002";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "0.001m³/cp";
		selector.value = "0003";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "1.0L/cp";
		selector.value = "0004";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "0.1L/cp";
		selector.value = "0005";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "0.01L/cp";
		selector.value = "0006";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "0.001L/cp";
		selector.value = "0007";
		selectorList.add(selector);

		parameter.selectors = selectorList;
		mList.add(parameter);
		/********************************** 参数11--脉冲宽度时间 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "000B";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--脉冲宽度时间==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "脉冲宽度时间";
		parameter.type = 1;
		parameter.valueIn = Integer.parseInt(param, 16);
		switch ((Integer) parameter.valueIn) {
		case 0:
			parameter.value = "4ms";
			break;
		case 1:
			parameter.value = "8ms";
			break;
		case 2:
			parameter.value = "20ms";
			break;
		case 3:
			parameter.value = "30ms";
			break;
		case 4:
			parameter.value = "40ms";
			break;
		case 5:
			parameter.value = "80ms";
			break;
		case 6:
			parameter.value = "100ms";
			break;
		case 7:
			parameter.value = "150ms";
			break;
		case 8:
			parameter.value = "200ms";
			break;
		case 9:
			parameter.value = "400ms";
			break;
		}
		selectorList = new ArrayList<Selector>();

		selector = new Selector();
		selector.name = "4ms";
		selector.value = "0000";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "8ms";
		selector.value = "0001";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "20ms";
		selector.value = "0002";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "30ms";
		selector.value = "0003";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "40ms";
		selector.value = "0004";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "80ms";
		selector.value = "0005";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "100ms";
		selector.value = "0006";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "150ms";
		selector.value = "0007";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "200ms";
		selector.value = "0008";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "400ms";
		selector.value = "0009";
		selectorList.add(selector);

		parameter.selectors = selectorList;
		mList.add(parameter);
		/********************************** 参数12--频率输出范围 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "000C";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--频率输出范围==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "频率输出范围";
		parameter.type = 1;
		parameter.maxValue = 5999;
		parameter.minValue = 0;
		parameter.valueIn = Long.parseLong(param, 16);
		parameter.value = Long.parseLong(param, 16) + "Hz";
		mList.add(parameter);
		/********************************** 参数13--流量零点修正 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "000D";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量零点修正==" + Integer.parseInt(param, 16));
		parameter.count = "0001";
		parameter.name = "流量零点修正";
		parameter.type = 4;
		parameter.maxValue = 9999;
		parameter.minValue = -9999;
		parameter.valueIn = Long.parseLong(param, 16) < 0x8000 ? Long.parseLong(param, 16) : Long.parseLong(param, 16) - 65536;
		parameter.value = (Long.parseLong(param, 16) < 0x8000 ? Long.parseLong(param, 16) : Long.parseLong(param, 16) - 65536) + "";
		mList.add(parameter);
		/********************************** 参数14--背光保持时间 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//	parameter.address = "000E";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--背光保持时间==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "背光保持时间";
		parameter.type = 1;
		parameter.valueIn = Integer.parseInt(param, 16);
		switch ((Integer) parameter.valueIn) {
		case 0:
			parameter.value = "15s";
			break;
		case 1:
			parameter.value = "30s";
			break;
		case 2:
			parameter.value = "60s";
			break;
		case 3:
			parameter.value = "120s";
			break;
		case 4:
			parameter.value = "180s";
			break;
		case 5:
			parameter.value = "300s";
			break;
		case 6:
			parameter.value = "常亮";
			break;
		}
		selectorList = new ArrayList<Selector>();

		selector = new Selector();
		selector.name = "15s";
		selector.value = "0000";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "30s";
		selector.value = "0001";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "60s";
		selector.value = "0002";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "120s";
		selector.value = "0003";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "180s";
		selector.value = "0004";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "300s";
		selector.value = "0005";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "常亮";
		selector.value = "0006";
		selectorList.add(selector);

		parameter.selectors = selectorList;
		mList.add(parameter);
		/********************************** 参数15--通讯地址 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "000F";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--通讯地址==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "通讯地址";
		parameter.type = 1;
		parameter.maxValue = 247;
		parameter.minValue = 1;
		parameter.valueIn = Long.parseLong(param, 16);
		parameter.value = Long.parseLong(param, 16) + "";
		mList.add(parameter);
		/********************************** 参数16--通讯速率 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0010";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--通讯速率==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "通讯速率";
		parameter.type = 1;
		parameter.valueIn = Integer.parseInt(param, 16);
		selectorList = new ArrayList<Selector>();
		for (int i = 0; i < 8; i++) {
			if ((Integer) parameter.valueIn == i) {
				parameter.value = 300 * (int) Math.pow(2, i) + "bps";
			}
			selector = new Selector();
			selector.name = 300 * (int) Math.pow(2, i) + "bps";
			selector.value = "000" + i;
			selectorList.add(selector);
		}
		parameter.selectors = selectorList;
		mList.add(parameter);
		/********************************** 参数17--设备位号 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0011";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--设备位号==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "设备位号";
		parameter.type = 1;
		parameter.maxValue = 9999;
		parameter.minValue = 1;
		parameter.valueIn = Long.parseLong(param, 16);
		parameter.value = Long.parseLong(param, 16) + "";
		mList.add(parameter);
		AppStaticVar.PASSWORD_LEVEAL1_COUNT = mList.size();
		/********************************** 高级参数 **************************************/
		/********************************** 参数18--测量管道口径 **************************************/
		mList.add(new Parameter(true, "高级参数"));
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0012";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--测量管道口径==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "测量管道口径";
		parameter.type = 1;
		parameter.valueIn = Integer.parseInt(param, 16);
		switch ((Integer) parameter.valueIn) {
		case 0:
			parameter.value = "3mm";
			break;
		case 1:
			parameter.value = "6mm";
			break;
		case 2:
			parameter.value = "10mm";
			break;
		case 3:
			parameter.value = "15mm";
			break;
		case 4:
			parameter.value = "20mm";
			break;
		case 5:
			parameter.value = "25mm";
			break;
		case 6:
			parameter.value = "32mm";
			break;
		case 7:
			parameter.value = "40mm";
			break;
		case 8:
			parameter.value = "50mm";
			break;
		case 9:
			parameter.value = "65mm";
			break;
		case 10:
			parameter.value = "80mm";
			break;
		case 11:
			parameter.value = "100mm";
			break;
		case 12:
			parameter.value = "125mm";
			break;
		case 13:
			parameter.value = "150mm";
			break;
		case 14:
			parameter.value = "200mm";
			break;
		case 15:
			parameter.value = "250mm";
			break;
		case 16:
			parameter.value = "300mm";
			break;
		case 17:
			parameter.value = "350mm";
			break;
		case 18:
			parameter.value = "400mm";
			break;
		case 19:
			parameter.value = "450mm";
			break;
		case 20:
			parameter.value = "500mm";
			break;
		case 21:
			parameter.value = "600mm";
			break;
		case 22:
			parameter.value = "700mm";
			break;
		case 23:
			parameter.value = "800mm";
			break;
		case 24:
			parameter.value = "900mm";
			break;
		case 25:
			parameter.value = "1000mm";
			break;
		case 26:
			parameter.value = "1200mm";
			break;
		case 27:
			parameter.value = "1400mm";
			break;
		case 28:
			parameter.value = "1600mm";
			break;
		case 29:
			parameter.value = "1800mm";
			break;
		case 30:
			parameter.value = "2000mm";
			break;
		case 31:
			parameter.value = "2200mm";
			break;
		case 32:
			parameter.value = "2400mm";
			break;
		case 33:
			parameter.value = "2500mm";
			break;
		case 34:
			parameter.value = "2600mm";
			break;
		case 35:
			parameter.value = "2800mm";
			break;
		case 36:
			parameter.value = "3000mm";
			break;
		}
		selectorList = new ArrayList<Selector>();
		selector = new Selector();
		selector.name = "3mm";
		selector.value = "0000";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "6mm";
		selector.value = "0001";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "10mm";
		selector.value = "0002";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "15mm";
		selector.value = "0003";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "20mm";
		selector.value = "0004";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "25mm";
		selector.value = "0005";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "32mm";
		selector.value = "0006";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "40mm";
		selector.value = "0007";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "50mm";
		selector.value = "0008";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "65mm";
		selector.value = "0009";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "80mm";
		selector.value = "000A";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "100mm";
		selector.value = "000B";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "125mm";
		selector.value = "000C";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "150mm";
		selector.value = "000D";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "200mm";
		selector.value = "000E";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "250mm";
		selector.value = "000F";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "300mm";
		selector.value = "0010";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "350mm";
		selector.value = "0011";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "400mm";
		selector.value = "0012";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "450mm";
		selector.value = "0013";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "500mm";
		selector.value = "0014";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "600mm";
		selector.value = "0015";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "700mm";
		selector.value = "0016";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "800mm";
		selector.value = "0017";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "900mm";
		selector.value = "0018";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "1000mm";
		selector.value = "0019";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "1200mm";
		selector.value = "001A";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "1400mm";
		selector.value = "001B";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "1600mm";
		selector.value = "001C";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "1800mm";
		selector.value = "001D";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "2000mm";
		selector.value = "001E";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "2200mm";
		selector.value = "001F";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "2400mm";
		selector.value = "0020";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "2500mm";
		selector.value = "0021";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "2600mm";
		selector.value = "0022";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "2800mm";
		selector.value = "0023";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "3000mm";
		selector.value = "0024";
		selectorList.add(selector);

		parameter.selectors = selectorList;
		mList.add(parameter);
		/********************************** 参数19--允许切除显示 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0013";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--允许切除显示==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "允许切除显示";
		parameter.type = 1;
		parameter.valueIn = Integer.parseInt(param, 16);
		switch ((Integer) parameter.valueIn) {
		case 0:
			parameter.value = "允许";
			break;
		case 1:
			parameter.value = "禁止";
			break;
		}
		selectorList = new ArrayList<Selector>();

		selector = new Selector();
		selector.name = "允许";
		selector.value = "0000";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "禁止";
		selector.value = "0001";
		selectorList.add(selector);

		parameter.selectors = selectorList;
		mList.add(parameter);
		/********************************** 参数20--传感器系数值 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0014";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--传感器系数值==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "传感器系数值";
		parameter.type = 2;
		parameter.point = 4;
		parameter.maxValue = 5.9999;
		parameter.minValue = 0;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn) + "%";
		mList.add(parameter);
		/********************************** 参数21--空管报警允许 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0015";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--空管报警允许==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "空管报警允许";
		parameter.type = 1;
		parameter.valueIn = Integer.parseInt(param, 16);
		switch ((Integer) parameter.valueIn) {
		case 0:
			parameter.value = "允许";
			break;
		case 1:
			parameter.value = "禁止";
			break;
		}
		selectorList = new ArrayList<Selector>();

		selector = new Selector();
		selector.name = "允许";
		selector.value = "0000";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "禁止";
		selector.value = "0001";
		selectorList.add(selector);

		parameter.selectors = selectorList;
		mList.add(parameter);
		/********************************** 参数22--空管报警阈值 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0016";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--空管报警阈值==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "空管报警阈值";
		parameter.type = 2;
		parameter.point = 2;
		parameter.maxValue = 599.99;
		parameter.minValue = 0;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn) + "%";
		mList.add(parameter);
		/********************************** 参数23--流量上限报警允许 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0017";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量上限报警允许==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "流量上限报警允许";
		parameter.type = 1;
		parameter.valueIn = Integer.parseInt(param, 16);
		switch ((Integer) parameter.valueIn) {
		case 0:
			parameter.value = "允许";
			break;
		case 1:
			parameter.value = "禁止";
			break;
		}
		selectorList = new ArrayList<Selector>();

		selector = new Selector();
		selector.name = "允许";
		selector.value = "0000";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "禁止";
		selector.value = "0001";
		selectorList.add(selector);

		parameter.selectors = selectorList;
		mList.add(parameter);
		/********************************** 参数24--流量上限报警数值 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0018";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量上限报警数值==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "流量上限报警数值";
		parameter.type = 2;
		parameter.point = 2;
		parameter.maxValue = 599.99;
		parameter.minValue = 0;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn) + "%";
		mList.add(parameter);
		/********************************** 参数25--流量下限报警允许 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0019";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量下限报警允许==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "流量下限报警允许";
		parameter.type = 1;
		parameter.valueIn = Integer.parseInt(param, 16);
		switch ((Integer) parameter.valueIn) {
		case 0:
			parameter.value = "允许";
			break;
		case 1:
			parameter.value = "禁止";
			break;
		}
		selectorList = new ArrayList<Selector>();

		selector = new Selector();
		selector.name = "允许";
		selector.value = "0000";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "禁止";
		selector.value = "0001";
		selectorList.add(selector);

		parameter.selectors = selectorList;
		mList.add(parameter);
		/********************************** 参数26--流量下限报警数值 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "001A";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量下限报警数值==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "流量下限报警数值";
		parameter.type = 2;
		parameter.point = 2;
		parameter.maxValue = 599.99;
		parameter.minValue = 0;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn) + "%";
		mList.add(parameter);
		/********************************** 参数27--励磁报警允许 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "001B";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--励磁报警允许==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "励磁报警允许";
		parameter.type = 1;
		parameter.valueIn = Integer.parseInt(param, 16);
		switch ((Integer) parameter.valueIn) {
		case 0:
			parameter.value = "允许";
			break;
		case 1:
			parameter.value = "禁止";
			break;
		}
		selectorList = new ArrayList<Selector>();

		selector = new Selector();
		selector.name = "允许";
		selector.value = "0000";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "禁止";
		selector.value = "0001";
		selectorList.add(selector);

		parameter.selectors = selectorList;
		mList.add(parameter);
		/********************************** 参数28--正向总量 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "001C";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		param1 = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--正向总量==" + NumberBytes.hexStrToLong(param1+param));
		parameter.count = "0002";
		parameter.name = "正向总量";
		parameter.type = 3;
		parameter.maxValue = 999999999;
		parameter.minValue = 0;
		parameter.valueIn = NumberBytes.hexStrToLong(param1+param);
		parameter.value = NumberBytes.hexStrToLong(param1+param) + "";
		AppStaticVar.ZXZLPosition = mList.size();
		mList.add(parameter);
		/********************************** 参数29--反向总量 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "001E";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		param1 = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--反向总量==" + NumberBytes.hexStrToLong(param1+param));
		parameter.count = "0002";
		parameter.name = "反向总量";
		parameter.type = 3;
		parameter.maxValue = 999999999;
		parameter.minValue = 0;
		parameter.valueIn = NumberBytes.hexStrToLong(param1+param);
		parameter.value = NumberBytes.hexStrToLong(param1+param) + "";
		AppStaticVar.FXZLPosition = mList.size();
		mList.add(parameter);
		/********************************** 参数30--基本参数密码 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0020";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		param1 = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--基本参数密码==" + NumberBytes.hexStrToLong(param1+param));
		parameter.count = "0002";
		parameter.name = "基本参数密码";
		parameter.type = 3;
		parameter.maxValue = 999999;
		parameter.minValue = 0;
		parameter.valueIn = NumberBytes.hexStrToLong(param1+param);
		parameter.value = NumberBytes.hexStrToLong(param1+param) + "";
		Constans.PasswordLevel.LEVEL_1 = NumberBytes.hexStrToLong(param1+param);
		mList.add(parameter);
		/********************************** 参数31--高级参数密码 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0022";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		param1 = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--高级参数密码==" + NumberBytes.hexStrToLong(param1+param));
		parameter.count = "0002";
		parameter.name = "高级参数密码";
		parameter.maxValue = 999999;
		parameter.minValue = 0;
		parameter.type = 3;
		parameter.valueIn = NumberBytes.hexStrToLong(param1+param);
		parameter.value = NumberBytes.hexStrToLong(param1+param) + "";
		Constans.PasswordLevel.LEVEL_2 = NumberBytes.hexStrToLong(param1+param);
		mList.add(parameter);
		/********************************** 参数32--总量清零密码 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0024";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		param1 = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--总量清零密码==" + NumberBytes.hexStrToLong(param1+param));
		parameter.count = "0002";
		parameter.maxValue = 999999;
		parameter.minValue = 0;
		parameter.name = "总量清零密码";
		parameter.type = 3;
		parameter.valueIn = NumberBytes.hexStrToLong(param1+param);
		parameter.value = NumberBytes.hexStrToLong(param1+param) + "";
		Constans.PasswordLevel.LEVEL_6 = NumberBytes.hexStrToLong(param1+param);
		mList.add(parameter);
		AppStaticVar.PASSWORD_LEVEAL2_COUNT = mList.size();
		/********************************** 传感器参数 **************************************/
		/********************************** 参数33--流量修正允许 **************************************/
		mList.add(new Parameter(true, "传感器参数"));
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0026";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量修正允许==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "流量修正允许";
		parameter.type = 1;
		parameter.valueIn = Integer.parseInt(param, 16);
		switch ((Integer) parameter.valueIn) {
		case 0:
			parameter.value = "允许";
			break;
		case 1:
			parameter.value = "禁止";
			break;
		}
		selectorList = new ArrayList<Selector>();

		selector = new Selector();
		selector.name = "允许";
		selector.value = "0000";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "禁止";
		selector.value = "0001";
		selectorList.add(selector);

		parameter.selectors = selectorList;
		mList.add(parameter);
		/********************************** 参数34--流量修正点1 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0027";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量修正点1==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "流量修正点1";
		parameter.type = 2;
		parameter.point = 3;
		parameter.maxValue = 15;
		parameter.minValue = 0;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn) + "m/s";
		mList.add(parameter);
		/********************************** 参数35--流量修正值1 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0028";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量修正值1==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "流量修正值1";
		parameter.type = 2;
		parameter.point = 4;
		parameter.maxValue = 1.9999;
		parameter.minValue = 0;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn);
		mList.add(parameter);
		/********************************** 参数36--流量修正点2 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0029";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量修正点2==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "流量修正点2";
		parameter.type = 2;
		parameter.point = 3;
		parameter.maxValue = 15;
		parameter.minValue = 0;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn) + "m/s";
		mList.add(parameter);
		/********************************** 参数37--流量修正值2 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "002A";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量修正值2==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "流量修正值2";
		parameter.type = 2;
		parameter.point = 4;
		parameter.maxValue = 1.9999;
		parameter.minValue = 0;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn);
		mList.add(parameter);
		/********************************** 参数38--流量修正点3 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "002B";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量修正点3==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "流量修正点3";
		parameter.type = 2;
		parameter.point = 3;
		parameter.maxValue = 15;
		parameter.minValue = 0;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn) + "m/s";
		mList.add(parameter);
		/********************************** 参数39--流量修正值3 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "002C";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量修正值3==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "流量修正值3";
		parameter.type = 2;
		parameter.point = 4;
		parameter.maxValue = 1.9999;
		parameter.minValue = 0;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn);
		mList.add(parameter);
		/********************************** 参数40--流量修正点4 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "002D";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量修正点4==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "流量修正点4";
		parameter.type = 2;
		parameter.point = 3;
		parameter.maxValue = 15;
		parameter.minValue = 0;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn) + "m/s";
		mList.add(parameter);
		/********************************** 参数41--流量修正值4 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "002E";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量修正值4==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "流量修正值4";
		parameter.type = 2;
		parameter.point = 4;
		parameter.maxValue = 1.9999;
		parameter.minValue = 0;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn);
		mList.add(parameter);
		/********************************** 参数42--流量修正点5 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "002F";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量修正点5==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "流量修正点5";
		parameter.type = 2;
		parameter.point = 3;
		parameter.maxValue = 15;
		parameter.minValue = 0;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn) + "m/s";
		mList.add(parameter);
		/********************************** 参数43--流量修正值5 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0030";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量修正值5==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "流量修正值5";
		parameter.type = 2;
		parameter.point = 4;
		parameter.maxValue = 1.9999;
		parameter.minValue = 0;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn);
		mList.add(parameter);
		/********************************** 参数44--流量修正点6 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0031";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量修正点6==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "流量修正点6";
		parameter.type = 2;
		parameter.point = 3;
		parameter.maxValue = 15;
		parameter.minValue = 0;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn) + "m/s";
		mList.add(parameter);
		/********************************** 参数45--流量修正值6 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0032";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量修正值6==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "流量修正值6";
		parameter.type = 2;
		parameter.point = 4;
		parameter.maxValue = 1.9999;
		parameter.minValue = 0;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn);
		mList.add(parameter);
		/********************************** 参数46--流量修正点7 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0033";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量修正点7==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "流量修正点7";
		parameter.type = 2;
		parameter.point = 3;
		parameter.maxValue = 15;
		parameter.minValue = 0;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn) + "m/s";
		mList.add(parameter);
		/********************************** 参数47--流量修正值7 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0034";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量修正值7==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "流量修正值7";
		parameter.type = 2;
		parameter.point = 4;
		parameter.maxValue = 1.9999;
		parameter.minValue = 0;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn);
		mList.add(parameter);
		/********************************** 参数48--流量修正点8 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0035";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--流量修正点8==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "流量修正点8";
		parameter.type = 2;
		parameter.point = 3;
		parameter.maxValue = 15;
		parameter.minValue = 0;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn) + "m/s";
		mList.add(parameter);
		/********************************** 参数49--励磁方式选择 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0036";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--励磁方式选择==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "励磁方式选择";
		parameter.type = 1;
		parameter.valueIn = Integer.parseInt(param, 16);
		switch ((Integer) parameter.valueIn) {
		case 0:
			parameter.value = "1/16工频";
			break;
		case 1:
			parameter.value = "1/20工频";
			break;
		case 2:
			parameter.value = "1/25工频";
			break;
		}
		selectorList = new ArrayList<Selector>();

		selector = new Selector();
		selector.name = "1/16工频";
		selector.value = "0000";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "1/20工频";
		selector.value = "0001";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "1/25工频";
		selector.value = "0002";
		selectorList.add(selector);

		parameter.selectors = selectorList;
		mList.add(parameter);
		/********************************** 参数50--励磁电流 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0037";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--励磁电流==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "励磁电流";
		parameter.type = 1;
		parameter.valueIn = Integer.parseInt(param, 16);
		switch ((Integer) parameter.valueIn) {
		case 0:
			parameter.value = "100mA";
			break;
		case 1:
			parameter.value = "250mA";
			break;
		case 2:
			parameter.value = "300mA";
			break;
		case 3:
			parameter.value = "500mA";
			break;
		}
		selectorList = new ArrayList<Selector>();

		selector = new Selector();
		selector.name = "100mA";
		selector.value = "0000";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "250mA";
		selector.value = "0001";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "350mA";
		selector.value = "0002";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "500mA";
		selector.value = "0003";
		selectorList.add(selector);
		parameter.selectors = selectorList;
		mList.add(parameter);
		/********************************** 参数51--尖峰抑制允许 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0038";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--尖峰抑制允许==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "尖峰抑制允许";
		parameter.type = 1;
		parameter.valueIn = Integer.parseInt(param, 16);
		switch ((Integer) parameter.valueIn) {
		case 0:
			parameter.value = "允许";
			break;
		case 1:
			parameter.value = "禁止";
			break;
		}
		selectorList = new ArrayList<Selector>();

		selector = new Selector();
		selector.name = "允许";
		selector.value = "0000";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "禁止";
		selector.value = "0001";
		selectorList.add(selector);

		parameter.selectors = selectorList;
		mList.add(parameter);
		/********************************** 参数52--尖峰抑制系数 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0039";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--尖峰抑制系数==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "尖峰抑制系数";
		parameter.type = 1;
		parameter.valueIn = Integer.parseInt(param, 16);
		switch ((Integer) parameter.valueIn) {
		case 0:
			parameter.value = "0.010m/s";
			break;
		case 1:
			parameter.value = "0.020m/s";
			break;
		case 2:
			parameter.value = "0.030m/s";
			break;
		case 3:
			parameter.value = "0.050m/s";
			break;
		case 4:
			parameter.value = "0.080m/s";
			break;
		case 5:
			parameter.value = "0.100m/s";
			break;
		case 6:
			parameter.value = "0.200m/s";
			break;
		case 7:
			parameter.value = "0.300m/s";
			break;
		case 8:
			parameter.value = "0.500m/s";
			break;
		case 9:
			parameter.value = "0.800m/s";
			break;
		}
		selectorList = new ArrayList<Selector>();

		selector = new Selector();
		selector.name = "0.010m/s";
		selector.value = "0000";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "0.020m/s";
		selector.value = "0001";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "0.030m/s";
		selector.value = "0002";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "0.050m/s";
		selector.value = "0003";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "0.080m/s";
		selector.value = "0004";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "0.100m/s";
		selector.value = "0005";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "0.200m/s";
		selector.value = "0006";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "0.300m/s";
		selector.value = "0007";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "0.500m/s";
		selector.value = "0008";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "0.800m/s";
		selector.value = "0009";
		selectorList.add(selector);

		parameter.selectors = selectorList;
		mList.add(parameter);
		/********************************** 参数53--尖峰抑制时间 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "003A";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--尖峰抑制时间==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "尖峰抑制时间";
		parameter.type = 1;
		parameter.valueIn = Integer.parseInt(param, 16);
		switch ((Integer) parameter.valueIn) {
		case 0:
			parameter.value = "400ms";
			break;
		case 1:
			parameter.value = "600ms";
			break;
		case 2:
			parameter.value = "800ms";
			break;
		case 3:
			parameter.value = "1000ms";
			break;
		case 4:
			parameter.value = "2500ms";
			break;
		}
		selectorList = new ArrayList<Selector>();

		selector = new Selector();
		selector.name = "400ms";
		selector.value = "0000";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "600ms";
		selector.value = "0001";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "800ms";
		selector.value = "0002";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "1000ms";
		selector.value = "0003";
		selectorList.add(selector);

		selector = new Selector();
		selector.name = "2500ms";
		selector.value = "0004";
		selectorList.add(selector);

		parameter.selectors = selectorList;
		mList.add(parameter);
		/********************************** 参数54--传感器编码 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "003B";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		param1 = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--传感器编码==" + NumberBytes.hexStrToLong(param1+param));
		parameter.count = "0002";
		parameter.name = "传感器编码";
		parameter.type = 3;
		parameter.maxValue = 99999999;
		parameter.minValue = 0;
		parameter.valueIn = NumberBytes.hexStrToLong(param1+param);
		parameter.value = NumberBytes.hexStrToLong(param1+param) + "";
		mList.add(parameter);
		/********************************** 参数55--传感器参数密码 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "003D";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		param1 = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--传感器参数密码==" + NumberBytes.hexStrToLong(param1+param));
		parameter.count = "0002";
		parameter.name = "传感器参数密码";
		parameter.type = 3;
		parameter.maxValue = 999999;
		parameter.minValue = 0;
		parameter.valueIn = NumberBytes.hexStrToLong(param1+param);
		parameter.value = NumberBytes.hexStrToLong(param1+param) + "";
		Constans.PasswordLevel.LEVEL_3 = NumberBytes.hexStrToLong(param1+param);
		mList.add(parameter);
		AppStaticVar.PASSWORD_LEVEAL3_COUNT = mList.size();
		/********************************** 转换器参数 **************************************/
		/********************************** 参数56--转换器标定系数 **************************************/
		mList.add(new Parameter(true, "转换器参数"));
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "003F";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--转换器标定系数==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "转换器标定系数";
		parameter.type = 2;
		parameter.point = 4;
		parameter.maxValue = 5.9999;
		parameter.minValue = 0;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn);
		mList.add(parameter);
		/********************************** 参数57--转换器标定零点 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//	parameter.address = "0040";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--转换器标定零点==" + Integer.parseInt(param, 16));
		parameter.count = "0001";
		parameter.name = "转换器标定零点";
		parameter.type = 4;
		parameter.point = 4;
		parameter.maxValue = 9999;
		parameter.minValue = -9999;
		parameter.valueIn = Long.parseLong(param, 16) < 0x8000 ? Long.parseLong(param, 16) / Math.pow(10, parameter.point) : (Long.parseLong(param, 16) - 65536)
				/ Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn);
		mList.add(parameter);
		/********************************** 参数58--空管检测零点 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0041";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--空管检测零点 ==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "空管检测零点 ";
		parameter.type = 1;
		parameter.maxValue = 59999;
		parameter.minValue = 0;
		parameter.point = 5;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn);
		mList.add(parameter);
		/********************************** 参数59--电流零点修正 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0042";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--电流零点修正==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "电流零点修正";
		parameter.type = 2;
		parameter.point = 4;
		parameter.maxValue = 1.9999;
		parameter.minValue = 0;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn);
		mList.add(parameter);
		/********************************** 参数60--电流满度修正 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0043";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--电流满度修正==" + Long.parseLong(param, 16));
		parameter.count = "0001";
		parameter.name = "电流满度修正";
		parameter.type = 2;
		parameter.point = 4;
		parameter.maxValue = 3.9999;
		parameter.minValue = 0;
		parameter.valueIn = Long.parseLong(param, 16) / Math.pow(10, parameter.point);
		parameter.value = String.format("%." + parameter.point + "f", (Double) parameter.valueIn);
		mList.add(parameter);
		/********************************** 参数61--仪表编码 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0044";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		param1 = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--仪表编码==" + NumberBytes.hexStrToLong(param1+param));
		parameter.count = "0002";
		parameter.name = "仪表编码";
		parameter.type = 3;
		parameter.maxValue = 99999999;
		parameter.minValue = 0;
		parameter.valueIn = NumberBytes.hexStrToLong(param1+param);
		parameter.value = NumberBytes.hexStrToLong(param1+param) + "";
		mList.add(parameter);
		/********************************** 参数62--转换器密码 **************************************/
		parameter = new Parameter();
		parameter.address = NumberBytes.padLeft(Integer.toHexString(paramIndex), 4, '0');//parameter.address = "0046";
		param = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		param1 = msg.substring(6+4*paramIndex++, 6+4*paramIndex);
		System.out.println("参数"+ paramCountLabel++ +"--转换器密码==" + NumberBytes.hexStrToLong(param1+param));
		parameter.count = "0002";
		parameter.name = "转换器密码";
		parameter.type = 3;
		parameter.maxValue = 999999;
		parameter.minValue = 0;
		parameter.valueIn = NumberBytes.hexStrToLong(param1+param);
		parameter.value = NumberBytes.hexStrToLong(param1+param) + "";
		Constans.PasswordLevel.LEVEL_4 = NumberBytes.hexStrToLong(param1+param);
		mList.add(parameter);
		AppStaticVar.PASSWORD_LEVEAL4_COUNT = mList.size();
		AppStaticVar.PASSWORD_LEVEAL5_COUNT = mList.size();
		AppStaticVar.mParamList = mList;
		mAdapter.notifyDataSetChanged();
	}

	private void checkPsw(long psw) {
		if (Constans.PasswordLevel.LEVEL_1 == psw) {
			AppStaticVar.PASSWORD_LEVEAL = 1;
		} else if (Constans.PasswordLevel.LEVEL_2 == psw) {
			AppStaticVar.PASSWORD_LEVEAL = 2;
		} else if (Constans.PasswordLevel.LEVEL_3 == psw) {
			AppStaticVar.PASSWORD_LEVEAL = 3;
		} else if (Constans.PasswordLevel.LEVEL_4 == psw) {
			AppStaticVar.PASSWORD_LEVEAL = 4;
		} else if (Constans.PasswordLevel.LEVEL_5 == psw) {
			AppStaticVar.PASSWORD_LEVEAL = 5;
		} else {
			AppStaticVar.PASSWORD_LEVEAL = -1;
		}
	}

	@Override
	protected void onResume() {
		// mViewCheckPsd.setVisibility(View.VISIBLE);
		// setTitleContent("密码校验");
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (!flag) {
			mViewCheckPsd.setVisibility(View.VISIBLE);
			setTitleContent("密码校验");
			mViewSetParam.setVisibility(View.GONE);
		}
		super.onPause();
	}

	@Override
	public void reconnectSuccss() {
		reconnectCount = 3;
		startReadParam();
	}

	private void setListeners() {

		mDataList = new ArrayList<Parameter>();
		mAdapter = new ParameterAdapter(mContext, mDataList);
		mListview.setAdapter(mAdapter);
		mListview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (mAdapter.getItem(position).isGroupTitle) {
					return;
				}
				flag = true;
				Intent intent = new Intent();
				if (mAdapter.getItem(position).selectors != null) {
					intent.setClass(mContext, SelectActivity.class);
					intent.putExtra("position", position);
					intent.putExtra("title", mAdapter.getItem(position).name);
					intent.putExtra("value", mAdapter.getItem(position).value);
					intent.putExtra("valueIn", mAdapter.getItem(position).valueIn.toString());
					intent.putExtra("list", (Serializable) mAdapter.getItem(position).selectors);
					intent.putExtra("param", mAdapter.getItem(position));
					startActivityForResult(intent, SELECT_PARAM);
				} else {
					intent.setClass(mContext, InputParamActivity.class);
					intent.putExtra("position", position);
					intent.putExtra("title", mAdapter.getItem(position).name);
					intent.putExtra("value", mAdapter.getItem(position).value);
					intent.putExtra("valueIn", mAdapter.getItem(position).valueIn.toString());
					intent.putExtra("param", mAdapter.getItem(position));
					startActivityForResult(intent, INPUT_PARAM);
				}
			}
		});
	}

	private void initUI() {
		mListview = (ListView) findViewById(R.id.listView1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		System.out.println("==========onActivityResult============");
		flag = false;
		hideProgressDialog();
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PARAM) {
				int position = data.getIntExtra("position", -1);
				Selector selector = (Selector) data.getSerializableExtra("selector");
				if (position != -1) {
					mAdapter.getItem(position).valueIn = selector.value;
					mAdapter.getItem(position).value = selector.name;
					mAdapter.notifyDataSetChanged();
				}
			} else if (requestCode == INPUT_PARAM) {
				int position = data.getIntExtra("position", -1);
				String value = data.getStringExtra("value");
				if (position != -1) {
					mAdapter.getItem(position).valueIn = value;
					mAdapter.getItem(position).value = value;
					mAdapter.notifyDataSetChanged();
				}
			}
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		if (data != null && "showProgress".equals(data.toString())) {
			System.out.println("-------");
			showProgressDialog("与设备通讯中...");
		} else {
			startReadParam();
		}
	}

	@Override
	protected void onDestroy() {
		AppStaticVar.mObservable.deleteObserver(this);
		AppStaticVar.PASSWORD_LEVEAL = -1;
		super.onDestroy();
	}
}
