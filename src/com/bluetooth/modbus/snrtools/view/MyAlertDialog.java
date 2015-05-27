package com.bluetooth.modbus.snrtools.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.bluetooth.modbus.snrtools.R;

public class MyAlertDialog extends Dialog implements android.view.View.OnClickListener
{

	Context context;
	private String title_;
	private String msg_;
	private String okButtonContent, cancelButtonContent, oddOkButtonContent;
	private int okButtonContentColor, cancelButtonContentColor, oddOkButtonContentColor;
	private int type_;
	public static int TYPE_ONE = 1;
	public static int TYPE_TWO = 2;
	public final static int BUTTON_OK = 20;
	public final static int BUTTON_CANCEL = 21;
	public final static int BUTTON_ODD_OK = 22;
	private TextView msgView;
	private MyAlertDialogListener listener_;
	private String oneOkText = "";
	private String twoOkText = "";
	private String twoCancelText = "";

	public interface MyAlertDialogListener
	{
		public void onClick(View view);
	}

	public MyAlertDialog(Context context, String title, String msg, int type, MyAlertDialogListener listener)
	{
		super(context, R.style.CustomAlertDialog);
		this.context = context;
		title_ = title;
		msg_ = msg;
		type_ = type;
		listener_ = listener;
	}
	
	public void setListener(MyAlertDialogListener listener){
		listener_ = listener;
	}

	public MyAlertDialog(Context context, String title, int type, MyAlertDialogListener listener)
	{
		super(context, R.style.CustomAlertDialog);
		this.context = context;
		title_ = title;
		type_ = type;
		listener_ = listener;
	}

	public void setMessage(String msg)
	{
		msg_ = msg;
		if (msgView != null)
		{
			msgView.setText(msg_);
		}
	}

	/**
	 * 设置按钮内容
	 * 
	 * @author chencheng
	 * @date 2015-2-9下午3:58:33
	 * @param content
	 * @param button
	 *            {@link #BUTTON_OK},{@link #BUTTON_CANCEL},
	 *            {@link #BUTTON_ODD_OK}
	 * @return void
	 */
	public void setButtonContent(String content, int button)
	{
		switch (button)
		{
			case BUTTON_OK:
				okButtonContent = content;
				break;
			case BUTTON_CANCEL:
				cancelButtonContent = content;
				break;
			case BUTTON_ODD_OK:
				oddOkButtonContent = content;
				break;
		}
	}

	/**
	 * 设置按钮内容颜色
	 * 
	 * @author chencheng
	 * @date 2015-2-9下午3:58:33
	 * @param color
	 * @param button
	 *            {@link #BUTTON_OK},{@link #BUTTON_CANCEL},
	 *            {@link #BUTTON_ODD_OK}
	 * @return void
	 */
	public void setButtonContentColor(int color, int button)
	{
		switch (button)
		{
			case BUTTON_OK:
				okButtonContentColor = color;
				break;
			case BUTTON_CANCEL:
				cancelButtonContentColor = color;
				break;
			case BUTTON_ODD_OK:
				oddOkButtonContentColor = color;
				break;
		}
	}

	public void setOneOkText(String text)
	{
		oneOkText = text;
	}

	public void setTwoOkText(String text)
	{
		twoOkText = text;
	}

	public void setTwoCancelText(String text)
	{
		twoCancelText = text;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.alert_layout_new);
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.dimAmount = 0.25f;
		getWindow().setAttributes(lp);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		//dimAmount在0.0f和1.0f之间，0.0f完全不暗，1.0f全暗
		initView();
	}

	private void initView()
	{
		// TextView tipView = (TextView) findViewById(R.id.zl_strmainmenu);
		// TextPaint tpaint = tipView.getPaint();
		// tpaint.setFakeBoldText(true);
		// tipView.setText(title_);
		msgView = (TextView) findViewById(R.id.message);
		if (msg_ != null)
		{
			msgView.setText(msg_);
		}

		TextView okButton = (TextView) findViewById(R.id.btnOk);
		if (twoOkText != null && !"".equalsIgnoreCase(twoOkText))
		{
			okButton.setText(twoOkText);
		}
		TextView cancelButton = (TextView) findViewById(R.id.btnCancel);
		if (twoCancelText != null && !"".equalsIgnoreCase(twoCancelText))
		{
			cancelButton.setText(twoCancelText);
		}
		TextView oddOkButton = (TextView) findViewById(R.id.btnOkOne);
		if (oneOkText != null && !"".equalsIgnoreCase(oneOkText))
		{
			oddOkButton.setText(oneOkText);
		}
		if (okButtonContent != null)
		{
			okButton.setText(okButtonContent);
		}
		if (cancelButtonContent != null)
		{
			cancelButton.setText(cancelButtonContent);
		}
		if (oddOkButtonContent != null)
		{
			oddOkButton.setText(oddOkButtonContent);
		}

		if (okButtonContentColor != 0)
		{
			okButton.setTextColor(okButtonContentColor);
		}
		if (cancelButtonContentColor != 0)
		{
			cancelButton.setTextColor(cancelButtonContentColor);
		}
		if (oddOkButtonContentColor != 0)
		{
			oddOkButton.setTextColor(oddOkButtonContentColor);
		}
		View divider = (View) findViewById(R.id.id_dialog_divider);

		if (type_ == TYPE_ONE)
		{
			oddOkButton.setVisibility(View.VISIBLE);
			okButton.setVisibility(View.GONE);
			cancelButton.setVisibility(View.GONE);
			divider.setVisibility(View.GONE);
			oddOkButton.setOnClickListener(this);
		}
		else if (type_ == TYPE_TWO)
		{
			okButton.setOnClickListener(this);
			cancelButton.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v)
	{
		dismiss();
		if(listener_ == null){
			return;
		}
		listener_.onClick(v);

	}

}