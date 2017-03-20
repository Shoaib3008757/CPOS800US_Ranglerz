package android_serialport_api;

import android.util.Log;

import com.ranglerz.utils.DataUtils;
import com.google.common.primitives.Bytes;

class PacketHeader {
	byte[] header = new byte[] { (byte) 0xCA, (byte) 0xDF, 0x00, 0x35 };
	int len;
	byte[] data;
	byte[] ed = new byte[] { (byte) 0xE3 };

	public PacketHeader(byte[] data, int id) {
		this.data = data;
		this.header[2] = (byte) id;
		this.len = this.data.length;
	}

	public byte[] getPacket() {
		byte[] packet = Bytes.concat(header, new byte[] { (byte) len }, data,
				ed);
		Log.i("whw", "packet=" + DataUtils.toHexString(packet));
		return packet;
	}
}