package com.ranglerz.logic;

import com.ranglerz.utils.DataUtils;

import android.util.Log;
import android_serialport_api.SerialPortManager;

public class IcCardOrderAPI {
	public static class Result {
		public static final int SUCCESS = 1;
		public static final int FAILURE = 0;
		public Object resultInfo;
	}

	private byte[] buffer = new byte[1024];

	public IcCardOrderAPI() {
	}

	/*
	 * IC卡通用操作接口
	 */
	public synchronized byte[] operateICApdu(byte[] calcLrc, int lenLrc) {
		Log.i("cy", "Enter function IcCardOrderAPI-operdateICApdu()");

		if (3 == lenLrc) {
			SerialPortManager.getInstance().write(calcLrc);

			int length = SerialPortManager.getInstance()
					.read(buffer, 4000, 200);
			if (0 == length) {
				return null;
			}

			byte[] recvData = new byte[length];
			System.arraycopy(buffer, 0, recvData, 0, length);
			printlog("IcCradProgAPI-resetCard()", recvData);

			if (0x03 == recvData[0] && 0x01 == recvData[1]
					&& 0x01 == recvData[2]) {
				return null;
			}

			return recvData;

		} else {
			// 前2字节的头和长度改为放在此处封装
			byte[] operCmd = new byte[calcLrc.length + 2];
			operCmd[0] = 0x02; // kt2001q命令字
			operCmd[1] = (byte) calcLrc.length; // kt2001q数据长度
			System.arraycopy(calcLrc, 0, operCmd, 2, calcLrc.length); // iso整个命令包

			// byte[] calcCrc = DataUtils.getFirstCmd(calcLrc, lenLrc);
			byte[] calcCrc = DataUtils.getFirstCmd(operCmd, operCmd.length); // lrc校验和

			SerialPortManager.getInstance().write(calcCrc);

			int length = SerialPortManager.getInstance()
					.read(buffer, 4000, 200);
			if (5 > length) {
				return null;
			}

			byte[] recvData = new byte[length];
			System.arraycopy(buffer, 0, recvData, 0, length);
			printlog("IcCradProgAPI-operdateICApdu()", recvData);

			// short sw = (short) (recvData[length - 3] & 0xFF);
			// if(0x90 == sw && 0x00 == recvData[length - 2])
			// //注释掉此判断，不管正确与否都返回值
			// {
			byte[] retData = new byte[length - 3];
			System.arraycopy(recvData, 2, retData, 0, retData.length);
			return retData;
			// }

			// return null;
		}

	}

	private void printlog(String tag, byte[] toLog) {
		Log.i("cy", "Enter function IcCardOrderAPI-printlog()");

		String toLogStr = DataUtils.toHexString(toLog);
		Log.i("cy", tag + "=" + toLogStr);

	}
}