package com.bluetooth.modbus.snrtools.bean;

import java.io.Serializable;
import java.util.List;

public class Parameter implements Serializable{
	private static final long serialVersionUID = 2556675806486384300L;
	public String address;
	public String count;
	public String name;
	/** 显示给用户的值*/
	public String value;
	/** 寄存器内的值*/
	public Object valueIn;
	/** 1-int,2-real,3-uint,4-short*/
	public int type;
	public int point;
	public List<Selector> selectors;
	public boolean isGroupTitle = false;
	public String groupTitle = "";
	public double maxValue;
	public double minValue;
	
	public Parameter()
	{
	}
	
	public Parameter(boolean isGroupTitle,String groupTitle)
	{
		this.isGroupTitle = isGroupTitle;
		this.groupTitle = groupTitle;
	}
}
