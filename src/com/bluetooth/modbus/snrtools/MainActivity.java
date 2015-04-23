package com.bluetooth.modbus.snrtools;

import java.io.IOException;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.ab.util.AbViewUtil;
import com.bluetooth.modbus.snrtools.manager.AppStaticVar;

public class MainActivity extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main_activity);

		Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Resusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab
		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, SNRMainActivity.class);
		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("SNRMainActivity").setIndicator("监视", res.getDrawable(R.drawable.ic_launcher)).setContent(intent);
		tabHost.addTab(spec);
		// Do the same for the other tabs
		intent = new Intent().setClass(this, CheckPasswordActivity.class);
		spec = tabHost.newTabSpec("CheckPasswordActivity").setIndicator("参数", res.getDrawable(R.drawable.ic_launcher)).setContent(intent);
		tabHost.addTab(spec);
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {

			@Override
			public void onTabChanged(String tabId) {
				if ("CheckPasswordActivity".equals(tabId)) {
					AppStaticVar.mObservable.notifyObservers("showProgress");
				}
			}
		});
		int count = tabHost.getTabWidget().getChildCount();//TabHost中有一个getTabWidget()的方法
		  for (int i = 0; i < count; i++) {
		   View view = tabHost.getTabWidget().getChildTabViewAt(i);   
		   view.getLayoutParams().height = 80; //tabWidget.getChildAt(i)
		   final TextView tv = (TextView) view.findViewById(android.R.id.title);
		   tv.setTextSize(20);
//		   tv.setTextColor(this.getResources().getColorStateList(
//		     android.R.color.white));
		   view.getLayoutParams().height = (int) AbViewUtil.dip2px(this, 40); 

		  }
	}

	@Override
	protected void onDestroy() {
		AppStaticVar.isExit = true;
		AppStaticVar.mCurrentAddress = null;
		AppStaticVar.mCurrentName = null;
		if (AppStaticVar.mSocket != null) {
			try {
				AppStaticVar.mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			AppStaticVar.mSocket = null;
		}
		super.onDestroy();
	}
}
