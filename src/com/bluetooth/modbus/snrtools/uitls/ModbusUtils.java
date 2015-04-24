package com.bluetooth.modbus.snrtools.uitls;

import java.util.HashMap;

import android.os.Handler;

import com.bluetooth.modbus.snrtools.bean.CommandRead;
import com.bluetooth.modbus.snrtools.bean.CommandWrite;
import com.bluetooth.modbus.snrtools.bean.Parameter;

public class ModbusUtils {
	private static final int TIME_OUT = 10000;
	public static int MSG_STATUS_COUNT = 0;
	public static int MSG_PARAM_COUNT = 0;

	public static void readStatus(String className,Handler handler) {
		CommandRead read = new CommandRead();
		read.setDeviceId("01");
		read.setCommandNo("03");
		read.setStartAddressH("10");
		read.setStartAddressL("10");
		read.setCountH("00");
		read.setCountL("1A");
		MSG_STATUS_COUNT = Integer.parseInt("001A", 16)*4+10;
		AppUtil.modbusWrite( className,handler, read,TIME_OUT);
	}

	public static void readParameter(String className,Handler handler) {
		CommandRead read = new CommandRead();
		read.setDeviceId("01");
		read.setCommandNo("03");
		read.setStartAddressH("00");
		read.setStartAddressL("00");
		read.setCountH("00");
		read.setCountL("48");
		MSG_PARAM_COUNT = Integer.parseInt("0048", 16)*4+10;
		AppUtil.modbusWrite( className,handler, read,TIME_OUT);
	}

	public static void writeParameter(String className,Handler handler, Parameter param) {
		CommandWrite write = new CommandWrite();
		write.setDeviceId("01");
		write.setCommandNo("10");
		write.setStartAddressH(param.address.substring(0, 2));
		write.setStartAddressL(param.address.substring(2, 4));
		write.setCountH(param.count.substring(0, 2));
		write.setCountL(param.count.substring(2, 4));
		int byteCount = Integer.parseInt(param.count, 16) * 2;
		write.setByteCount(NumberBytes.padLeft(Integer.toHexString(byteCount),
				2, '0'));
		String content = NumberBytes.padLeft(param.valueIn.toString(),
				byteCount * 2, '0');
		if (byteCount == 4) {
			content = content.substring(4, 8) + content.substring(0, 4);
		}
		HashMap<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < byteCount; i++) {
			map.put(i + "", content.substring(i * 2, (i + 1) * 2));
		}
		write.setContentMap(map);
		AppUtil.modbusWrite( className,handler, write,TIME_OUT);
	}

	public static void write2Device(String className,Handler handler) {
		CommandWrite write = new CommandWrite();
		write.setDeviceId("01");
		write.setCommandNo("10");
		write.setStartAddressH("00");
		write.setStartAddressL("48");
		write.setCountH("00");
		write.setCountL("01");
		write.setByteCount("02");
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("0", "00");
		map.put("1", "01");
		write.setContentMap(map);
		AppUtil.modbusWrite( className,handler, write,TIME_OUT);
	}

	/**
	 * 总量清零
	 * @param className
	 * @param handler
	 */
	public static void clearZL(String className,Handler handler) {
		CommandWrite write = new CommandWrite();
		write.setDeviceId("01");
		write.setCommandNo("10");
		write.setStartAddressH("00");
		write.setStartAddressL("48");
		write.setCountH("00");
		write.setCountL("01");
		write.setByteCount("02");
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("0", "00");
		map.put("1", "02");
		write.setContentMap(map);
		AppUtil.modbusWrite( className,handler, write,TIME_OUT);
	}
	
	/**
	 * 正向总量写入标志
	 * @param className
	 * @param handler
	 */
	public static void inputZXZL(String className,Handler handler) {
		CommandWrite write = new CommandWrite();
		write.setDeviceId("01");
		write.setCommandNo("10");
		write.setStartAddressH("00");
		write.setStartAddressL("48");
		write.setCountH("00");
		write.setCountL("01");
		write.setByteCount("02");
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("0", "00");
		map.put("1", "03");
		write.setContentMap(map);
		AppUtil.modbusWrite( className,handler, write,TIME_OUT);
	}
	
	/**
	 * 反向总量写入标志
	 * @param className
	 * @param handler
	 */
	public static void inputFXZL(String className,Handler handler) {
		CommandWrite write = new CommandWrite();
		write.setDeviceId("01");
		write.setCommandNo("10");
		write.setStartAddressH("00");
		write.setStartAddressL("48");
		write.setCountH("00");
		write.setCountL("01");
		write.setByteCount("02");
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("0", "00");
		map.put("1", "04");
		write.setContentMap(map);
		AppUtil.modbusWrite( className,handler, write,TIME_OUT);
	}
}
