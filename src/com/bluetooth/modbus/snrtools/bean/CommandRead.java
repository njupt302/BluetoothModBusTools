package com.bluetooth.modbus.snrtools.bean;

public class CommandRead {

	private String deviceId;
	private String commandNo;
	private String startAddressH;
	private String startAddressL;
	private String countH;
	private String countL;
	
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getCommandNo() {
		return commandNo;
	}
	public void setCommandNo(String commandNo) {
		this.commandNo = commandNo;
	}
	public String getStartAddressH() {
		return startAddressH;
	}
	public void setStartAddressH(String startAddressH) {
		this.startAddressH = startAddressH;
	}
	public String getStartAddressL() {
		return startAddressL;
	}
	public void setStartAddressL(String startAddressL) {
		this.startAddressL = startAddressL;
	}
	public String getCountH() {
		return countH;
	}
	public void setCountH(String countH) {
		this.countH = countH;
	}
	public String getCountL() {
		return countL;
	}
	public void setCountL(String countL) {
		this.countL = countL;
	}
	
}
