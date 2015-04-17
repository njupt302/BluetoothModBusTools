package com.bluetooth.modbus.snrtools.bean;

import java.util.HashMap;

public class CommandWrite extends CommandRead {

	private String byteCount;
	private HashMap<String, String> contentMap;
	public String getByteCount() {
		return byteCount;
	}
	public void setByteCount(String byteCount) {
		this.byteCount = byteCount;
	}
	public HashMap<String, String> getContentMap() {
		return contentMap;
	}
	public void setContentMap(HashMap<String, String> contentMap) {
		this.contentMap = contentMap;
	}
	
}
