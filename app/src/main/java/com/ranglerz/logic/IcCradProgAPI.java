package com.ranglerz.logic;

import com.ranglerz.utils.DataUtils;

import android.util.Log;
import android_serialport_api.SerialPortManager;

public class IcCradProgAPI {
	public static class Result {
		public static final int SUCCESS = 1;

		public static final int FAILURE = 0;

		public static final int NONE = 3;

		public Object resultInfo;
	}

	private byte[] buffer = new byte[1024];

	public IcCradProgAPI() {
		Log.i("cy", "Enter function IcCradProgAPI-IcCradProgAPI()");
	}

	/**
	 * 复位ic卡
	 * 
	 * @param flag
	 *            卡片类型，0：接触卡 1：非接触卡
	 * @return 返回结果
	 */
	public synchronized int resetCard(int flag) {
		Log.i("cy", "Enter function IcCradProgAPI-resetCard()");
		if (0 == flag) {
			// byte[] calcCrc = { 0x00, 0x00, 0x00 };
			byte[] calcCrc = { 0x00, 0x00, 0x00, 0x00 };
			SerialPortManager.getInstance().write(calcCrc);
		} else {
			// byte[] calcCrc = { 0x01, 0x00, 0x01 };
			byte[] calcCrc = { 0x01, 0x00, 0x00, 0x01 };
			SerialPortManager.getInstance().write(calcCrc);
		}

		int length = SerialPortManager.getInstance().read(buffer, 300, 30);
		if (0 == length) {
			return Result.NONE;// 没读到
		}

		byte[] recvData = new byte[length];
		System.arraycopy(buffer, 0, recvData, 0, length);
		DataUtils.toHexString(recvData);
		if (0x03 == recvData[0] && 0x01 == recvData[1] && 0x01 == recvData[2]
				&& 0x03 == recvData[3]) {
			return Result.FAILURE;
		}
		return Result.SUCCESS;
	}

	/**
	 * ic卡通用接口
	 * 
	 * @param calcLrc
	 *            操作指令
	 * @param lenLrc
	 *            指令长度
	 * @return
	 */
	public synchronized byte[] operateICApdu(byte[] calcLrc, int lenLrc) {
		Log.i("cy", "Enter function IcCradProgAPI-operdateICApdu()");

		byte[] calcCrc = DataUtils.getFirstCmd(calcLrc, lenLrc);
		SerialPortManager.getInstance().write(calcCrc);

		int length = SerialPortManager.getInstance().read(buffer, 4000, 200);
		if (5 > length) {
			return null;
		}

		byte[] recvData = new byte[length];
		System.arraycopy(buffer, 0, recvData, 0, length);

		short sw = (short) (recvData[length - 3] & 0xFF);
		if (0x90 == sw && 0x00 == recvData[length - 2]) {
			byte[] retData = new byte[length - 3];
			System.arraycopy(recvData, 2, retData, 0, retData.length);
			return retData;
		}
		return null;
	}
}