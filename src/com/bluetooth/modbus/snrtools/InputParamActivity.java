package com.bluetooth.modbus.snrtools;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.TextView;

import com.bluetooth.modbus.snrtools.bean.Parameter;
import com.bluetooth.modbus.snrtools.manager.ActivityManager;

public class InputParamActivity extends BaseWriteParamActivity
{

	private TextView mTvTitle;
	private EditText mEtParam;
	private Parameter p;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.input_param_activity);
		p = (Parameter) getIntent().getSerializableExtra("param");
		initUI();
	}

	private void initUI()
	{
		mTvTitle = (TextView) findViewById(R.id.tvTitle);
		mEtParam = (EditText) findViewById(R.id.editText1);
		mTvTitle.setText(getIntent().getStringExtra("title"));
		mEtParam.setHint(getIntent().getStringExtra("value") + "(" + getResources().getString(R.string.string_hint1)
				+ p.minValue + "~" + p.maxValue + ")");
		// if (p.type == 1) {
		// mEtParam.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
		// } else if (p.type == 2) {
		// mEtParam.setInputType(EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
		// mEtParam.setKeyListener(new DigitsKeyListener(false, true));
		// } else if (p.type == 3) {
		// mEtParam.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
		// }
	}

	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.button2:
				if (TextUtils.isEmpty(mEtParam.getText().toString().trim()))
				{
					showToast(getResources().getString(R.string.string_tips_msg8));
					return;
				}
				if (p != null)
				{
					// TODO 根据输入的数据类型，转换成对应的十六进制字符串
					double valueIn = 0;
					valueIn = Double.parseDouble(mEtParam.getText().toString().trim());
					if (valueIn > p.maxValue)
					{
						showToast(getResources().getString(R.string.string_tips_msg9) + p.maxValue + "!");
						return;
					}
					if (valueIn < p.minValue)
					{
						showToast(getResources().getString(R.string.string_tips_msg10) + p.minValue + "!");
						return;
					}
					p.valueIn = Integer.toHexString((int) (valueIn * Math.pow(10, p.point)));
					if (p.type == 4 && p.valueIn.toString().length() == 8)
					{
						p.valueIn = p.valueIn.toString().substring(4, 8);
					}
					writeParameter(p);
				}
				break;
		}
	}

	@Override
	public void reconnectSuccss()
	{
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (event.getAction() == MotionEvent.ACTION_DOWN && isOutOfBounds(this, event))
		{
			ActivityManager.getInstances().finishActivity(this);
			return true;
		}
		return super.onTouchEvent(event);
	}

	private boolean isOutOfBounds(Activity context, MotionEvent event)
	{
		final int x = (int) event.getX();
		final int y = (int) event.getY();
		final int slop = ViewConfiguration.get(context).getScaledWindowTouchSlop();
		final View decorView = context.getWindow().getDecorView();
		return (x < -slop) || (y < -slop) || (x > (decorView.getWidth() + slop))
				|| (y > (decorView.getHeight() + slop));
	}

	@Override
	public void onSuccess()
	{
		Intent intent = new Intent();
		intent.putExtra("position", getIntent().getIntExtra("position", -1));
		intent.putExtra("value", mEtParam.getText().toString());
		setResult(RESULT_OK, intent);
		ActivityManager.getInstances().finishActivity(InputParamActivity.this);
	}
}
