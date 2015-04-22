package com.bluetooth.modbus.snrtools;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.TextView;

import com.ab.util.AbAppUtil;
import com.bluetooth.modbus.snrtools.manager.ActivityManager;

/**
 * wechat
 * 
 * @author donal
 * 
 */
public class Welcome extends BaseActivity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final View view = View.inflate(this, R.layout.welcome_page, null);
		setContentView(view);
		((TextView)findViewById(R.id.tvVersion)).setText(AbAppUtil.getPackageInfo(this).versionName);
		AlphaAnimation aa = new AlphaAnimation(0.3f, 1.0f);
		aa.setDuration(1000);
		view.startAnimation(aa);
		aa.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation arg0) {
				redirectTo();
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
			}

		});
	}
	@Override
	public void reconnectSuccss() {
	}
	private void redirectTo() {
		Intent intent = new Intent(this, SelectDeviceActivity.class);
		startActivity(intent);
		ActivityManager.getInstances().finishActivity(this);
	}
}
