package android_serialport_api;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import com.ranglerz.utils.DataUtils;

public class PrinterAPI {
	private int maxLen = 0;
	private int maxLength = 250;
	private byte[] buffer = new byte[50 * 1024];// 定义字节的数组 存储打印机状态
	private static final int MAXPDLENGTH = 0x255;// 一次最大打印数据255
	private byte[] maxPrintData = new byte[MAXPDLENGTH];// 一次最大打印数据

	private byte[] print_command = { 0x0A };// 打印命令
	private byte[] initPrinter_command = { 0x1B, 0x40 };// 初始化打印机
	private byte[] set_command = { 0x1B, 0x21, 0x00 };// 设置
	private byte[] setUnderLine_command = { 0x1B, 0x2D, 0x00 };// 无下划线
	private byte[] setAlignType_command = { 0x1B, 0x61, 0x00 };// 默认设置左对齐
	private byte[] printFlashPic_command = { 0x1C, 0x2D, 0x00 };// 打印flash图片
	private byte[] printQrcode_command = { 0x1D, 0x5A, 0x00 };// 默认Qr码
	private byte[] printFont_command = { 0x1B, 0x4D, 0x00 };// 默认字体
	private byte[] printDouble_command = { 0x1B, 0x47, 0x00 };// 默认不双重打印
	private byte[] line_spacing = { 0x1B, 0x33, 0x00 };// 默认行间距0;
	private byte[] word_spacing = { 0x1B, 0x20, 0x00 };// 默认字间距

	/**
	 * 打印机状态
	 */
	public class PrinterStatus {
		public boolean isPaper = false;
		public boolean isHot = false;
		public boolean isPrint = false;
	}

	/**
	 * 开启模块，上电
	 */
	public synchronized void openPrint() {
		SerialPortManager.getInstance().openSerialPortPrinter();
	}

	/**
	 * 关闭模块，下电
	 */
	public synchronized void closePrint() {
		SerialPortManager.getInstance().closeSerialPort(2);
	}

	/**
	 * 函数说明：初始化打印机
	 */
	public synchronized void initPrint() {
		SerialPortManager.getInstance().write(topackage(initPrinter_command));
	}

	/**
	 * 函数说明：打印走纸
	 */
	public synchronized void doPrintPaper() {
		SerialPortManager.getInstance().write(topackage(print_command));
	}

	/**
	 * 函数说明：打印一维条码
	 * 
	 * @param str
	 *            打印的一维条码数据
	 * @param mBarcodeType
	 *            一维条码类型选择
	 * @return 0：失败 1：成功
	 */
	public void printBarcode(String str, int mBarcodeType) {
		try {
			byte[] bytes = str.getBytes("GBK");
			byte[] realBytes = new byte[bytes.length + 4];
			realBytes[0] = 0x1D;
			realBytes[1] = 0x6B;
			realBytes[2] = (byte) mBarcodeType;
			realBytes[3] = (byte) bytes.length;
			byte[] tmpBytes = DataUtils.hexStringTobyte(DataUtils
					.str2Hexstr(str));
			for (int i = 0; i < bytes.length; i++)
				realBytes[4 + i] = tmpBytes[i];
			SerialPortManager.getInstance().write(topackage(realBytes));
			doPrintPaper();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 函数说明：打印二维码
	 * 
	 * @param str
	 *            二维码数据
	 * @param codeType
	 *            二维码类型
	 * @return 0：失败 1：成功
	 */
	public synchronized int printQrcode(String str, int codeType) {
		// 发二维码类型
		printQrcode_command[2] = (byte) codeType;
		SerialPortManager.getInstance().write(topackage(printQrcode_command));

		// 打印二维码
		try {
			byte[] bytes = str.getBytes("GBK");
			byte[] realBytes = new byte[bytes.length + 4];
			realBytes[0] = 0x1B;
			realBytes[1] = 0x5A;
			realBytes[2] = 0x00;
			realBytes[3] = (byte) bytes.length;
			byte[] tmpBytes = DataUtils.hexStringTobyte(DataUtils
					.str2Hexstr(str));
			for (int i = 0; i < bytes.length; i++)
				realBytes[4 + i] = tmpBytes[i];
			SerialPortManager.getInstance().write(topackage(realBytes));
			doPrintPaper();
			doPrintPaper();
			doPrintPaper();
			doPrintPaper();
			doPrintPaper();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	/**
	 * 函数说明：打印Flash中图片
	 * 
	 * @param imageType
	 *            打印Flash图片类型
	 */
	public synchronized void printFlashImage(int imageType) {
		printFlashPic_command[2] = (byte) imageType;
		SerialPortManager.getInstance().write(topackage(printFlashPic_command));
		doPrintPaper();
		doPrintPaper();
		doPrintPaper();
		doPrintPaper();
		doPrintPaper();
	}

	/**
	 * 函数说明： 手动输入打印
	 * 
	 * @param str
	 *            要打印的数据
	 */
	public synchronized void printPaper(String str) {
		String mstr = DataUtils.str2Hexstr(str);
		int len = mstr.length() / 2;// 字节长度
		if (len > maxLength) {// 如果字节大于250
			int n = len / 250 + 1;// 分几组发下去
			String newstr;
			for (int i = 0; i < n; i++) {
				if (i != n - 1) {
					newstr = mstr.substring(500 * i, 500 * (i + 1));
					byte[] data = DataUtils.hexStringTobyte(newstr);
					// byte[] newdata = new byte[data.length + 1];
					// System.arraycopy(data, 0, newdata, 0, data.length);
					// newdata[newdata.length - 1] = (byte) 0x0a;
					SerialPortManager.getInstance().write(topackage(data));
				} else {
					newstr = mstr.substring(500 * i, mstr.length());
					byte[] data = DataUtils.hexStringTobyte(newstr);
					byte[] newdata = new byte[data.length + 1];
					System.arraycopy(data, 0, newdata, 0, data.length);
					newdata[newdata.length - 1] = (byte) 0x0a;

					SerialPortManager.getInstance().write(topackage(data));
				}
			}
		} else {
			String newstr = DataUtils.str2Hexstr(str);
			byte[] data = DataUtils.hexStringTobyte(newstr);
			byte[] newdata = new byte[data.length + 1];
			System.arraycopy(data, 0, newdata, 0, data.length);
			newdata[newdata.length - 1] = (byte) 0x0a;

			SerialPortManager.getInstance().write(topackage(newdata));
		}
		doPrintPaper();
		doPrintPaper();
		doPrintPaper();
		doPrintPaper();
		doPrintPaper();
	}

	/**
	 * 设置下划线
	 * 
	 * @param underline
	 *            下划线类型
	 */
	public synchronized void setUnderLine(int underline) {
		setUnderLine_command[2] = (byte) underline;
		SerialPortManager.getInstance().write(topackage(setUnderLine_command));
	}

	/**
	 * 函数说明：设置对齐方式
	 * 
	 * @param alignType
	 *            对齐类型
	 */
	public synchronized void setAlighType(int alignType) {
		setAlignType_command[2] = (byte) alignType;
		SerialPortManager.getInstance().write(topackage(setAlignType_command));
	}

	/**
	 * 设置宽高粗下划线
	 * 
	 * @param wide
	 *            宽
	 * @param high
	 *            高
	 * @param crude
	 *            粗
	 * @param underLine
	 *            下划线
	 */
	public synchronized void setKGCU(boolean wide, boolean high, boolean crude,
			boolean underLine) {
		int intWide, intHigh, intCrude, inUnderLine;
		int total;
		if (wide) {
			intWide = 100000;
		} else {
			intWide = 0;
		}
		if (high) {
			intHigh = 10000;
		} else {
			intHigh = 0;
		}
		if (crude) {
			intCrude = 1000;
		} else {
			intCrude = 0;
		}
		if (underLine) {
			inUnderLine = 10000000;
		} else {
			inUnderLine = 0;
		}
		total = intWide + intHigh + intCrude + inUnderLine;
		BigInteger src = new BigInteger(total + "", 2);// 转换为BigInteger类型
		String ten = src.toString();
		int m = Integer.parseInt(ten);
		set_command[2] = (byte) m;
		SerialPortManager.getInstance().write(topackage(set_command));
		DataUtils.toHexString(topackage(set_command));
	}

	/**
	 * 函数说明：设置双重打印
	 * 
	 * @param tag
	 *            是否双重打印
	 */
	public synchronized void setDouble(boolean tag) {
		if (tag) {
			printDouble_command[2] = (byte) 1;
			SerialPortManager.getInstance().write(
					topackage(printDouble_command));
		} else {
			SerialPortManager.getInstance().write(
					topackage(printDouble_command));
		}
	}

	/**
	 * 设置行间距
	 * 
	 * @param space
	 *            行间距单位，每单位0.125mm,[0,127]
	 */
	public synchronized void setLineSpace(int space) {
		line_spacing[2] = (byte) space;
		SerialPortManager.getInstance().write(topackage(line_spacing));
	}

	/**
	 * 设置字间距
	 * 
	 * @param space
	 *            字间距单位,space=[0,127]
	 */
	public synchronized void setWordSpace(int space) {
		word_spacing[2] = (byte) space;
		SerialPortManager.getInstance().write(topackage(word_spacing));
	}

	/**
	 * 函数说明：设置字体
	 * 
	 * @param tag
	 *            字体类型
	 */
	public synchronized void setFont(int fontType) {
		printFont_command[2] = (byte) fontType;
		SerialPortManager.getInstance().write(topackage(printFont_command));
	}

	/**
	 * 函数说明：实时获取打印机状态
	 * 
	 * @return 返回打印机状态
	 */
	public synchronized PrinterStatus getPrinterStatus() {
		maxLen = SerialPortManager.getInstance().readFixedLength(buffer, 1000,
				-1);
		PrinterStatus status = new PrinterStatus();
		maxLen = maxLen - 1;
		if (maxLen < 0)
			maxLen = 0;
		if ((buffer[maxLen] & 0x10) == 16)
			status.isPrint = true;// 打印
		else
			status.isPrint = false;// 未打印
		if ((buffer[maxLen] & 0x04) == 4)
			status.isHot = true;// 过热
		else
			status.isHot = false;// 正常
		if ((buffer[maxLen] & 0x01) == 1)
			status.isPaper = true;// 缺纸
		else
			status.isPaper = false;// 有纸
		return status;
	}

	/**
	 * 封装指令并返回
	 * 
	 * @param old
	 *            原始指令
	 * @return 新的指令
	 */
	private byte[] topackage(byte[] old) {
		int len = old.length;
		byte[] heade = { (byte) 0xCA, (byte) 0xDF, (byte) 0x00, (byte) 0x35 };
		byte[] cmd = new byte[heade.length + 2 + len + 1];
		System.arraycopy(heade, 0, cmd, 0, heade.length);
		cmd[heade.length] = (byte) 0;
		cmd[heade.length + 1] = (byte) len;
		System.arraycopy(old, 0, cmd, heade.length + 2, len);
		cmd[cmd.length - 1] = (byte) 0xE3;
		return cmd;
	}
}