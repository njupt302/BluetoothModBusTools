package com.bluetooth.modbus.snrtools.manager;

import com.bluetooth.modbus.snrtools.bean.Parameter;

public class DealParam
{
//	String param = msg.substring(6, 10);
//	System.out.println("参数"+ paramCountLabel++ +"--语言==" + Integer.parseInt(param, 16));
//	parameter = new Parameter();
//	parameter.address = "0000";
//	parameter.count = "0001";
//	parameter.name = "语言";
//	parameter.type = 1;
//	parameter.valueIn = Integer.parseInt(param, 16);
//	switch ((Integer) parameter.valueIn) {
//	case 0:
//		parameter.value = "简体中文";
//		break;
//	case 1:
//		parameter.value = "English";
//		break;
//	}
//	selectorList = new ArrayList<Selector>();
//
//	selector = new Selector();
//	selector.name = "简体中文";
//	selector.value = "0000";
//	selectorList.add(selector);
//
//	selector = new Selector();
//	selector.name = "English";
//	selector.value = "0001";
//	selectorList.add(selector);
//
//	parameter.selectors = selectorList;
//	mList.add(parameter);
	public static Parameter dealSelect(String result){
		Parameter p = new Parameter();
		return p;
	}

}
