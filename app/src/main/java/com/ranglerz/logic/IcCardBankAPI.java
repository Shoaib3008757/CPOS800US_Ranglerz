/**   
 * @Title: IcCardBankAPI.java
 * @Package android_serialport_api
 * @Description: TODO
 * @author Weishun.Xu   
 * @date 2016年3月28日 上午8:42:35
 * @version V1.0   
 */
package com.ranglerz.logic;

import com.ranglerz.utils.DataUtils;

import android.util.Log;
import android_serialport_api.SerialPortManager;

public class IcCardBankAPI {
	public static class Result {
		public static final int SUCCESS = 1;
		public static final int FAILURE = 0;
		public Object resultInfo;
	}

	private byte[] buffer = new byte[1024];

	public IcCardBankAPI() {
		Log.i("cy", "Enter function IcCardBankAPI-IcCardBankAPI()");
	}

	public synchronized int resetCard(int flag) {
		Log.i("cy", "Enter function IcCardBankAPI-resetCard()");

		if (0 == flag) {
			byte[] calcCrc = { 0x00, 0x00, 0x00, 0x00 };
			SerialPortManager.getInstance().write(calcCrc);
		} else {
			byte[] calcCrc = { 0x01, 0x00, 0x00, 0x01 };
			SerialPortManager.getInstance().write(calcCrc);
		}

		int length = SerialPortManager.getInstance().read(buffer, 4000, 200);
		if (0 == length) {
			return Result.FAILURE;
		}

		byte[] recvData = new byte[length];
		System.arraycopy(buffer, 0, recvData, 0, length);

		printlog("IcCardBankAPI-resetCard()", recvData);

		if (0x03 == recvData[0] && 0x01 == recvData[1] && 0x01 == recvData[2]) {
			return Result.FAILURE;
		}
		return Result.SUCCESS;
	}

	/**
	 * 选卡（接触式）
	 * 
	 * @return
	 */
	public synchronized byte[] selectFile() {
		Log.i("cy", "Enter function IcCardBankAPI-selectFile()");

		byte[] toCalcLrc = { 0x02, 0x00, 0x13, 0x00, (byte) 0xa4, 0x04, 0x00,
				0x0e, 0x31, 0x50, 0x41, 0x59, (byte) 0x2E, 0x53, 0x59, 0x53,
				(byte) 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31 };
		int lenLrc = toCalcLrc.length;
		byte[] toCalcCrc = DataUtils.getFirstCmd(toCalcLrc, lenLrc);
		DataUtils.toHexString(toCalcCrc);
		SerialPortManager.getInstance().write(toCalcCrc);

		int length = SerialPortManager.getInstance().read(buffer, 4000, 200);
		// 正确的值至少5个字节，2个字节头，2个字节结果码，1个字节结束符
		if (5 > length) {
			return null;
		}

		byte[] recvData = new byte[length];
		System.arraycopy(buffer, 0, recvData, 0, length);

		printlog("IcCardBankAPI-selectFile()", recvData);

		short sw = (short) (recvData[length - 3] & 0xFF);
		if (0x90 == sw && 0x00 == recvData[length - 2]) {
			// return recvData;
			byte[] retData = new byte[length - 3];
			System.arraycopy(recvData, 2, retData, 0, retData.length);
			return retData;
		}
		return null;
	}

	/**
	 * 选卡（非接触式）
	 * 
	 * @return
	 */
	public synchronized byte[] selectFileContless() {
		Log.i("cy", "Enter function IcCardBankAPI-selectFileContless()");

		byte[] toCalcLrc = { 0x02, 0x00, 0x13, 0x00, (byte) 0xa4, 0x04, 0x00,
				0x0e, 0x32, 0x50, 0x41, 0x59, (byte) 0x2E, 0x53, 0x59, 0x53,
				(byte) 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31 };
		int lenLrc = toCalcLrc.length;
		byte[] toCalcCrc = DataUtils.getFirstCmd(toCalcLrc, lenLrc);
		DataUtils.toHexString(toCalcCrc);
		SerialPortManager.getInstance().write(toCalcCrc);

		int length = SerialPortManager.getInstance().read(buffer, 4000, 200);
		if (5 > length) {
			return null;
		}

		byte[] recvData = new byte[length];
		System.arraycopy(buffer, 0, recvData, 0, length);

		printlog("IcCardBankAPI-selectFileContless()", recvData);

		short sw = (short) (recvData[length - 3] & 0xFF);
		if (0x90 == sw && 0x00 == recvData[length - 2]) {
			byte[] retData = new byte[length - 3];
			System.arraycopy(recvData, 2, retData, 0, retData.length);
			return retData;
		}
		return null;
	}

	/**
	 * 选文件
	 * 
	 * @return 返回
	 */
	public synchronized byte[] selectFileNext() {
		Log.i("cy", "Enter function IcCardBankAPI-selectFileNext()");

		byte[] toCalcLrc = { 0x02, 0x00, 0x0D, 0x00, (byte) 0xA4, 0x04, 0x00,
				0x08, (byte) 0xA0, 0x00, 0x00, 0x03, 0x33, 0x01, 0x01, 0x01 };
		int lenLrc = toCalcLrc.length;
		byte[] toCalcCrc = DataUtils.getFirstCmd(toCalcLrc, lenLrc);
		DataUtils.toHexString(toCalcCrc);
		SerialPortManager.getInstance().write(toCalcCrc);

		int length = SerialPortManager.getInstance().read(buffer, 4000, 200);
		if (5 > length) {
			return null;
		}

		byte[] recvData = new byte[length];
		System.arraycopy(buffer, 0, recvData, 0, length);

		printlog("IcCardBankAPI-selectFileNext()", recvData);

		short sw = (short) (recvData[length - 3] & 0xFF);
		if (0x90 == sw && 0x00 == recvData[length - 2]) {
			byte[] retData = new byte[length - 3];
			System.arraycopy(recvData, 2, retData, 0, retData.length);
			return retData;
		} else {
			byte[] creditCardFir = { 0x02, 0x0D, 0x00, (byte) 0xA4, 0x04, 0x00,
					0x08, (byte) 0xA0, 0x00, 0x00, 0x03, 0x33, 0x01, 0x01, 0x02 };
			int lenFir = creditCardFir.length;
			byte[] creditCardSec = DataUtils.getFirstCmd(creditCardFir, lenFir);
			SerialPortManager.getInstance().write(creditCardSec);

			int lenCreditCard = SerialPortManager.getInstance().read(buffer,
					4000, 200);
			if (5 > lenCreditCard) {
				return null;
			}

			byte[] creditCardData = new byte[lenCreditCard];
			System.arraycopy(buffer, 0, creditCardData, 0, lenCreditCard);

			printlog("IcCardBankAPI-selectFileNext()", creditCardData);

			short swCreditCard = (short) (creditCardData[lenCreditCard - 3] & 0xFF);
			if (0x90 == swCreditCard
					&& 0x00 == creditCardData[lenCreditCard - 2]) {
				byte[] retData = new byte[lenCreditCard - 3];
				System.arraycopy(creditCardData, 2, retData, 0, retData.length);
				return retData;
			}
		}
		return null;
	}

	/**
	 * 读记录
	 * 
	 * @return
	 */
	public synchronized byte[] readRecord() {
		Log.i("cy", "Enter function IcCardBankAPI-readRecord()");

		byte[] toCalcLrc = { 0x02, 0x00, 0x05, 0x00, (byte) 0xB2, 0x01, 0x0C,
				0x00 };
		int lenLrc = toCalcLrc.length;
		byte[] toCalcCrc = DataUtils.getFirstCmd(toCalcLrc, lenLrc);
		DataUtils.toHexString(toCalcCrc);
		SerialPortManager.getInstance().write(toCalcCrc);

		int length = SerialPortManager.getInstance().read(buffer, 4000, 200);
		if (5 > length) {
			return null;
		}

		byte[] recvData = new byte[length];
		System.arraycopy(buffer, 0, recvData, 0, length);

		printlog("IcCardBankAPI-readRecord()", recvData);

		short sw = (short) (recvData[length - 3] & 0xFF);
		if (0x90 == sw && 0x00 == recvData[length - 2]) {
			byte[] retData = new byte[length - 3];
			System.arraycopy(recvData, 2, retData, 0, retData.length);
			return retData;
		}
		return null;
	}

	public synchronized byte[] readRecordNext() {
		Log.i("cy", "Enter function IcCardBankAPI-readRecordNext()");

		byte[] toCalcLrc = { 0x02, 0x00, 0x05, 0x00, (byte) 0xB2, 0x02, 0x0C,
				0x00 };
		int lenLrc = toCalcLrc.length;
		byte[] toCalcCrc = DataUtils.getFirstCmd(toCalcLrc, lenLrc);
		SerialPortManager.getInstance().write(toCalcCrc);

		int length = SerialPortManager.getInstance().read(buffer, 4000, 200);
		if (5 > length) {
			return null;
		}

		byte[] recvData = new byte[length];
		System.arraycopy(buffer, 0, recvData, 0, length);

		printlog("IcCardBankAPI-readRecordNext()", recvData);

		short sw = (short) (recvData[length - 3] & 0xFF);
		if (0x90 == sw && 0x00 == recvData[length - 2]) {
			byte[] retData = new byte[length - 3];
			System.arraycopy(recvData, 2, retData, 0, retData.length);
			return retData;
		}
		return null;
	}

	/**
	 * 读数据
	 * 
	 * @return
	 */
	public synchronized byte[] getData() {
		Log.i("cy", "Enter function IcCardBankAPI-getData()");

		byte[] toCalcLrc = { 0x02, 0x00, 0x05, (byte) 0x80, (byte) 0xCA,
				(byte) 0x9F, 0x79, 0x00 };
		int lenLrc = toCalcLrc.length;
		byte[] toCalcCrc = DataUtils.getFirstCmd(toCalcLrc, lenLrc);
		DataUtils.toHexString(toCalcCrc);
		SerialPortManager.getInstance().write(toCalcCrc);

		int length = SerialPortManager.getInstance().read(buffer, 4000, 200);
		if (5 > length) {
			return null;
		}

		byte[] recvData = new byte[length];
		System.arraycopy(buffer, 0, recvData, 0, length);

		printlog("IcCardBankAPI-getData()", recvData);

		short sw = (short) (recvData[length - 3] & 0xFF);
		if (0x90 == sw && 0x00 == recvData[length - 2]) {
			byte[] retData = new byte[length - 3];
			System.arraycopy(recvData, 2, retData, 0, retData.length);
			return retData;
		}
		return null;
	}

	private void printlog(String tag, byte[] toLog) {
		Log.i("cy", "Enter function IcCardBankAPI-printlog()");

		String toLogStr = DataUtils.toHexString(toLog);
		Log.i("cy", tag + "=" + toLogStr);
	}
}