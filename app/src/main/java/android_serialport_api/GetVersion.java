package android_serialport_api;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class GetVersion {
	private byte[] stm32 = { (byte) 0xca, (byte) 0xdf, (byte) 0x04, 0x36, 0x01,
			(byte) 0xe3 };
	private byte[] v32550 = { 0x09, 0x00, 0x00, 0x09 };
	private byte[] buffer = new byte[1024];
	private byte[] data = new byte[1024];

	/**
	 * ��ȡapk�汾��
	 * 
	 * @param context
	 * @return apk�汾��
	 */
	public String getApkVersion(Context context) {
		String appVersion = null;
		PackageManager manager = context.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(context.getPackageName(),
					0);
			appVersion = info.versionName; // �汾��
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return appVersion;
	}

	/**
	 * ��ȡstm32���صİ汾��
	 * 
	 * @return �汾��
	 */
	public String getStm32Version() {
		SerialPortManager.getInstance().write(stm32);
		String strdata = "";
		int length = SerialPortManager.getInstance().read(buffer, 3000, 100);
		if (length > 0) {
			byte[] getData = new byte[length];
			System.arraycopy(buffer, 0, getData, 0, length);
			strdata = new String(getData);
		}
		return strdata;
	}

	/**
	 * ��ȡ32550���صİ汾��
	 * 
	 * @return �汾��
	 */
	public String get32550Version() {
		SerialPortManager.getInstance().write(v32550);
		String strdata = "";
		int length = SerialPortManager.getInstance().read(data, 3000, 100);
		if (length > 0) {
			byte[] getData = new byte[length];
			System.arraycopy(data, 0, getData, 0, length);
			strdata = new String(getData);
		}
		return strdata;
	}
}