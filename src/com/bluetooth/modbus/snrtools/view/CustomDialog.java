package com.bluetooth.modbus.snrtools.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bluetooth.modbus.snrtools.R;

/**
 * 自定义进度条
 * 
 * @author David
 */
public class CustomDialog extends AlertDialog
{
	private ProgressBar pb;
	private TextView text;
	private Context mContext;
	private String message = "";
	private String style = "circle";
	private boolean indeterminate = true;
	private int maxValue = 100;
	private int value = 0;
	private View contextView;

	private boolean isBlock;

	public static final int STYLE_RECTANGLE = 1;
	public static final int STYLE_CIRCLE = 2;

	public CustomDialog(Context context, int theme)
	{
		super(context, theme);
		mContext = context;
	}

	public CustomDialog(Context context)
	{
		super(context);
		mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if ("rectangle".equals(style))
		{
			contextView = buildContentView(STYLE_RECTANGLE);

		}
		else
		{
			contextView = buildContentView(STYLE_CIRCLE);
		}
		setContentView(contextView);

		WindowManager.LayoutParams lp=getWindow().getAttributes();
		lp.dimAmount=0.25f;
		lp.width = (int) (getWindow().getWindowManager().getDefaultDisplay().getWidth()*0.7);
		getWindow().setAttributes(lp);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		if (isBlock)
		{
			contextView.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{

					CustomDialog.this.dismiss();
				}
			});
		}
	}

	public void setMaxValue(int maxVal)
	{
		maxValue = maxVal;
	}

	public void setProgress(int val)
	{
		value = val;
		if (pb != null)
		{
			pb.setProgress(value);
		}
	}

	public int getValue()
	{
		return value;
	}

	public void setMessage(String message)
	{
		this.message = message;
		if (text != null)
		{
			text.setText(message);
		}
	}

	public void setIndeterminate(boolean isStatus)
	{
		this.indeterminate = isStatus;
	}

	public void setStyle(String style)
	{
		this.style = style;
	}

	private View buildContentView(int style)
	{
		switch (style)
		{
			default:
				View circle = LayoutInflater.from(mContext).inflate(R.layout.progressbar0, null);
				pb = (ProgressBar) circle.findViewById(R.id.progress);
				text = (TextView) circle.findViewById(R.id.message);
				text.setText(message);
				if (TextUtils.isEmpty(message))
				{
					text.setVisibility(View.GONE);
				}
				else
				{
					text.setVisibility(View.VISIBLE);
				}
				return circle;
		}

	}

	public void setText(String mes)
	{
		if (text != null)
		{
			if (TextUtils.isEmpty(mes))
			{
				text.setVisibility(View.GONE);
			}
			else
			{
				text.setVisibility(View.VISIBLE);
			}
			text.setText(mes);
		}
	}

	// 防止打开dialog的时候会出现异常
	public void show(boolean isBlock)
	{
		try
		{
			this.isBlock = isBlock;
			setCancelable(isBlock);
			super.show();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
