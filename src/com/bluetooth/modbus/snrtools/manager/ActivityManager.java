package com.bluetooth.modbus.snrtools.manager;

import java.util.Stack;

import android.app.Activity;

public class ActivityManager {

	private static ActivityManager mActivityManager;
	
	private Stack<Activity> mActivitys;

	public static ActivityManager getInstances() {
		if (mActivityManager == null) {
			mActivityManager = new ActivityManager();
		}
		return mActivityManager;
	}

	public void addActivity(Activity activity){
		if(mActivitys == null){
			mActivitys = new Stack<Activity>();
		}
		mActivitys.add(activity);
	}

	public void finishActivity(Activity activity) {
		if(activity != null){
			activity.finish();
			mActivitys.remove(activity);
		}
	}
}
