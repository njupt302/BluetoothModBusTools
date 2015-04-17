package com.bluetooth.modbus.snrtools.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewDebug.ExportedProperty;
import android.widget.TextView;

public class NoFocuseTextview extends TextView {

	public NoFocuseTextview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public NoFocuseTextview(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NoFocuseTextview(Context context) {
		super(context);
	}

	@Override
	@ExportedProperty(category = "focus")
	public boolean isFocused() {
		return true;
	}
	
	@Override
	protected void onFocusChanged(boolean focused, int direction,
			Rect previouslyFocusedRect) {
	}
}
