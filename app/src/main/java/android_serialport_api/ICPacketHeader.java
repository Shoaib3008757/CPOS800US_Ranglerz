package android_serialport_api;

import android.util.Log;

import com.ranglerz.utils.DataUtils;
import com.google.common.primitives.Bytes;

public class ICPacketHeader {
		byte[] header = new byte[] { 0x02  };
		int len;
		byte[] data;
		byte[] ed = new byte[] { (byte) 0xb5 };
		public ICPacketHeader(byte[] data) {
			this.data = data;
			this.len = this.data.length;
		}
		
		public byte[] getPacket() {
			byte[] packet = Bytes.concat(header,new byte[] { (byte) len },
					data, ed);
			Log.i("whw", "packet=" + DataUtils.toHexString(packet));
			return packet;
		}
	}