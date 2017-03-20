package android_serialport_api;


import com.ranglerz.utils.DataUtils;

import android.os.SystemClock;
import android.util.Log;

/**
 * @author Administrator
 *
 */
public class CPUAPI {
	public static final byte[] CMD_SELECT_14443 = {0x01,0x00,0x01};
	public static final byte[] CMD_SELECT_PPOC = {0x00,(byte) 0xA4,0x04,0x00,0x0E,0x32,0x50,0x41,0x59,0x2E,0x53,0x59,0x53,0x2E,0x44,0x44,0x46,0x30,0x31 };
	public static final byte[] CMD_READ_PPOC = {0x00,(byte)0xB2,0x01,0x0C,0x00};

	//private static final byte[] SWITCH_COMMAND = "D&C00040104".getBytes();
	private static final byte[] CONFIGURATION_READER_MODE = "c05060102\r\n"
			.getBytes();
	private static final byte[] CONFIGURATION_PROTOCOL_MODE = "c05060c1001\r\n"
			.getBytes();
	private static final byte[] SET_CHECK_CODE = "c05060c04c1\r\n".getBytes();
	//private static final byte[] FIND = "f26\r\n".getBytes();
	//private static final String COLLISION_SELECT_CARD = "f9320\r\n";
	//private static final String SELECT = "f9370";
	//private static final byte[] RESET = "fE051\r\n".getBytes();
	private static final byte[] GET_CHALLENGE = "0084000008\r\n".getBytes();

	private String TAG ="rfid";
	private static final String ENTER = "\r\n";
	
	private static final String NO_RESPONSE = "No response from card.";

	private byte[] mBuffer = new byte[1024];

	public boolean selectCardType(byte[] cmd) {
		SerialPortManager.getInstance().write(cmd);
		Log.i(TAG, "selectCardType hex=" + new String(cmd));
		int length = SerialPortManager.getInstance().read(mBuffer, 150, 10);
		Log.i("whw", "selectCardType length="+length+",mBuffer="+ DataUtils.toHexString(mBuffer));
		SystemClock.sleep(200);
		return true;
	}
	/**
	 * 函数说明：切换指令
	 * @return
	 */
	/*public boolean switchStatus() {
		sendCommand(SWITCH_COMMAND);
		Log.i("whw", "SWITCH_COMMAND hex=" + new String(SWITCH_COMMAND));
		SystemClock.sleep(200);
		SerialPortManager.switchRFID = true;
		return true;
	}*/

	/**
	 * 函数说明：配置读卡器模式
	 * @return 返回  RF carrier on! ISO/IEC14443 TYPE A, 106KBPS.设置成功。无返回则通讯异常
	 */
	/*public String configurationReaderMode() {
		int length = receive(CONFIGURATION_READER_MODE, mBuffer);
		String receiveData = new String(mBuffer, 0, length);
		Log.i("whw", "configurationReaderMode   str=" + receiveData);
		if (length > 0
				&& receiveData.startsWith("RF carrier on! ISO/IEC14443 TYPE A, 106KBPS.")) {
			return receiveData;
		}
		return "";
	}*/

	/**
	 * 函数说明：根据卡片类型配置读卡协议模式（副载波 速率 调制方式）
	 * @return 正确 返回 0x01（01为写入的值 再次读出的结果 可以判断写入是否成功）
	 */
	/*public String configurationProtocolMode() {
		int length = receive(CONFIGURATION_PROTOCOL_MODE, mBuffer);
		String receiveData = new String(mBuffer, 0, length);
		Log.i("whw", "configurationProtocolMode   str=" + receiveData);
		if (length > 0 && receiveData.startsWith("0x01")) {
			return receiveData;
		}
		return "";
	}*/

	/**
	 * 函数说明：设置读卡器的校验码方式 自动接收超时判别
	 * @return 正确 返回 0xc1（c1为写入的值 再次读出的结果 可以判断写入是否成功）
	 */
	public String setCheckCode() {
		int length = sendRecv(SET_CHECK_CODE, mBuffer);
		String receiveData = new String(mBuffer, 0, length);
		Log.i("whw", "setCheckCode   str=" + receiveData);
		if (length > 0 && receiveData.startsWith("0xc1")) {
			return receiveData;
		}
		return "";
	}

	/**
	 * 函数说明：寻卡（按照标准规定的通讯协议格式，使用标准定义的目录命令进行寻卡）
	 * @return 返回值根据不同卡类型判断  无返回值寻卡失败
	 */
	/*public String findCard() {
		int length = sendRecv(FIND, mBuffer);
		String receiveData = new String(mBuffer, 0, length).trim();
		Log.i("whw", "findCard   str=" + receiveData);
		if (length > 0) {
			if(!receiveData.startsWith(NO_RESPONSE)){
				return receiveData;
			}
		}
		return "";
	}*/
	
	/**
	 * 函数说明：碰撞选卡
	 * @return 正确返回卡号 ，反之无返回值
	 */
	/*public String collisionSecectCard(){
		int length = sendRecv(COLLISION_SELECT_CARD.getBytes(), mBuffer);
		String receiveData = new String(mBuffer, 0, length).trim();
		Log.i("whw", "CollisionSecectCard   str=" + receiveData);
		if (length > 0) {
			if(!receiveData.startsWith(NO_RESPONSE)){
				return receiveData;
			}
		}
		return "";
	}*/
	
	/**
	 * 函数说明：选卡
	 * @param cardNum 获取的卡号
	 * @return 成功有返回值 ，反之失败
	 */
	/*public String selectCard(String cardNum){
		byte[] command = (SELECT+cardNum+ENTER).getBytes();
		int length = sendRecv(command, mBuffer);
		String receiveData = new String(mBuffer, 0, length).trim();
		Log.i("whw", "selectCard   str=" + receiveData);
		if (length > 0) {
			if(!receiveData.startsWith(NO_RESPONSE)){
				return receiveData;
			}
		}
		return "";
	}*/
	
	/**
	 * 函数说明：复位卡片
	 * @return 成功有返回值 ，反之失败
	 */
	/*public boolean  reset(){
		int length = sendRecv(RESET, mBuffer);
		String receiveData = new String(mBuffer, 0, length).trim();
		Log.i("whw", "reset   str=" + receiveData);
		if (length > 0) {
			if(!receiveData.startsWith(NO_RESPONSE)){
				return true;
			}
		}
		return false;
	}*/
	
	/**
	 * 函数说明：获取随机数
	 * @return 成功返回随机数
	 */
	public String getChallenge(){
		int length = sendRecv(GET_CHALLENGE, mBuffer);
		String receiveData = new String(mBuffer, 0, length).trim();
		Log.i("whw", "getChallenge   str=" + receiveData);
		if (length > 0) {
			if(!receiveData.startsWith(NO_RESPONSE)){
				return receiveData;
			}
		}
		return "";
	}

	public boolean selectIBANFile() {
		byte[] command1 = new byte[] {0x00,(byte) 0xA4,0x04,0x00,0x08,(byte) 0xA0,0x00,0x00,0x03,0x33,0x01,0x01,0x01};
		byte[] command2 = new byte[] {0x00,(byte) 0xA4,0x04,0x00,0x08,(byte) 0xA0,0x00,0x00,0x03,0x33,0x01,0x01,0x02};
		byte[] command3 = new byte[] {0x00,(byte) 0xA4,0x04,0x00,0x08,(byte) 0xA0,0x00,0x00,0x03,0x33,0x01,0x01,0x03};
		byte[] command4 = new byte[] {0x00,(byte) 0xA4,0x04,0x00,0x08,(byte) 0xA0,0x00,0x00,0x03,0x33,0x01,0x01,0x06};
		
		int len = sendRecv(CMD_SELECT_PPOC, mBuffer);
		if(len>0){
			for(int i=0;i<4;i++){
				byte[] command = null;
				if(i==0){
					command = command1;
				}else if(i==1)
				{
					command = command2;
				}
				else if(i==2)
				{
					command = command3;
				}
				else if(i==3)
				{
					command = command4;
				}
				int length = sendRecv(command, mBuffer);
				if(length>0)
				{
					byte[] buffer = new byte[length];
					System.arraycopy(mBuffer, 0, buffer, 0, length);
					if((buffer[length-2]==0x90)&&(buffer[length-1]==0x00))
					{
						return true;
					}
				}
				Log.i("whw", "recv buf=" + DataUtils.toHexString(mBuffer));
			}
			return true;
		}
		return false;	
	}
	
	public String readIBAN()
	{
		if(selectIBANFile())
		{
			int length2 = sendRecv(CMD_READ_PPOC, mBuffer);
			byte[] command = new byte[length2];
			System.arraycopy(mBuffer, 0, command, 0, length2);
			Log.i("whw", "recv buf=" + DataUtils.toHexString(command));
			return DataUtils.toHexString(command);
		}
		return "";
	}
	/*private void sendCommand(byte[] command) {
		ICPacketHeader pHeader = new ICPacketHeader(command);
		SerialPortManager.getInstance().write(pHeader.getPacket());
	}*/
	
	/*private int receive(byte[] command, byte[] buffer) {
		int length = -1;
		if (!SerialPortManager.switchRFID) {
			switchStatus();
		}
		ICPacketHeader pHeader = new ICPacketHeader(command);
		SerialPortManager.getInstance().write(pHeader.getPacket());
		Log.i("whw", "command hex=" + new String(command));
		length = SerialPortManager.getInstance().read(buffer, 150, 10);
		return length;
	}*/
	
	private int sendRecv(byte[] command, byte[] buffer) {
		int length = -1;
		ICPacketHeader pHeader = new ICPacketHeader(command);
		SerialPortManager.getInstance().write(pHeader.getPacket());
		length = SerialPortManager.getInstance().read(buffer, 150, 10);
		Log.i("whw", "packet=" + DataUtils.toHexString(buffer));
		return length;
	}
}
