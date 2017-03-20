package android_serialport_api;

public class BarCodeAPI {
	private byte[] startCode = { (byte) 0xca, (byte) 0xdf, (byte) 0x02,
			(byte) 0x36, (byte) 0x00, (byte) 0xe3 };

	/**
	 * ¿ªÊ¼É¨Ãè
	 */
	public void startScanner() {
		SerialPortManager.getInstance().write(startCode);
	}
}