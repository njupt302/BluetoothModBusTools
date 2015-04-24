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
	
	public void finishAll() {
		if(mActivitys != null){
			for(int i=0;mActivitys.size()>0;){
				mActivitys.get(0).finish();
				mActivitys.remove(0);
			}
		}
	}
}
