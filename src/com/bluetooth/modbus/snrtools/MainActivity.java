package com.bluetooth.modbus.snrtools;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class MainActivity extends TabActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Resusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab
		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, SNRMainActivity.class);
		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("SNRMainActivity").setIndicator("º‡ ”",
		res.getDrawable(R.drawable.ic_launcher))
		.setContent(intent);
		tabHost.addTab(spec);
		// Do the same for the other tabs
		intent = new Intent().setClass(this, CheckPasswordActivity.class);
		spec = tabHost.newTabSpec("CheckPasswordActivity").setIndicator("≤Œ ˝",
		res.getDrawable(R.drawable.ic_launcher))
		.setContent(intent);
		tabHost.addTab(spec);
	}

}
