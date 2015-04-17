package com.bluetooth.modbus.snrtools.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

public class MyProgressDialog extends ProgressDialog {

	public MyProgressDialog(Context context, int theme) {
		super(context, theme);
	}

	public MyProgressDialog(Context context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
}
