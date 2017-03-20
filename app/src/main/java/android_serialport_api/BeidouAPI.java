package android_serialport_api;

import java.util.ArrayList;
import java.util.List;

import com.ranglerz.utils.LooperBuffer;
import com.google.code.microlog4android.Logger;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

public class BeidouAPI {

	private static final byte[] OPEN_COMMAND = "D&C00040106".getBytes();
	private static final byte[] CLOSE_COMMAND = "D&C00040105".getBytes();

	/* ������������ */

	/**
	 * ģ������������
	 */
	public static final byte[] COLD_START_COMMAND = "$PMTK103*30\r\n"
			.getBytes();

	/**
	 * ģ������������
	 */
	public static final byte[] WARM_START_COMMAND = "$PMTK102*31\r\n"
			.getBytes();

	/**
	 * ģ������������
	 */
	public static final byte[] HOT_START_COMMAND = "$PMTK101*32\r\n".getBytes();

	/* ģʽ�л����� */

	/**
	 * ʹ�ô�GPS ģʽ����
	 */
	public static final byte[] GPS_MODE_OPERATION_COMMAND = "$PMTK353,1,0,0,0,0*2A\r\n"
			.getBytes();

	/**
	 * ʹ��BDS+GPS ����ģʽ����
	 */
	public static final byte[] GPS_BDS_MODE_OPERATION_COMMAND = "$PMTK353,1,0,0,0,1*2B\r\n"
			.getBytes();

	/**
	 * ʹ�ô�BDS ģʽ����
	 */
	public static final byte[] BDS_MODE_OPERATION_COMMAND = "$PMTK353,0,0,0,0,1*2A\r\n"
			.getBytes();

	/* ��估��Ƶ�ȿ������� */

	/**
	 * ģ��ֻ���GLL ��䣬Ƶ��Ϊ1HZ
	 */
	public static final byte[] GLL_STATEMENT_MODEL_COMMAND = "$PMTK314,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0*29\r\n"
			.getBytes();

	/**
	 * ģ��ֻ���RMC ��䣬Ƶ��Ϊ1HZ
	 */
	public static final byte[] RMC_STATEMENT_MODEL_COMMAND = "$PMTK314,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0*29\r\n"
			.getBytes();

	/**
	 * ģ��ֻ���VTG ��䣬Ƶ��Ϊ1HZ
	 */
	public static final byte[] VTG_STATEMENT_MODEL_COMMAND = "$PMTK314,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0*29\r\n"
			.getBytes();

	/**
	 * ģ��ֻ���GGA ��䣬Ƶ��Ϊ1HZ
	 */
	public static final byte[] GGA_STATEMENT_MODEL_COMMAND = "$PMTK314,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0*29\r\n"
			.getBytes();

	/**
	 * ģ��ֻ���GSA ��䣬Ƶ��Ϊ1HZ
	 */
	public static final byte[] GSA_STATEMENT_MODEL_COMMAND = "$PMTK314,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0*29\r\n"
			.getBytes();

	/**
	 * ģ��ֻ���GSV ��䣬Ƶ��Ϊ1HZ
	 */
	public static final byte[] GSV_STATEMENT_MODEL_COMMAND = "$PMTK314,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0*29\r\n"
			.getBytes();

	/**
	 * ģ��ֻ���ZDA ��䣬Ƶ��Ϊ1HZ
	 */
	public static final byte[] ZDA_STATEMENT_MODEL_COMMAND = "$PMTK314,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0*29\r\n"
			.getBytes();

	/**
	 * ģ��ָ�Ϊ���RMC��GGA��GSA��GSV��5 �����һ�Σ���GLL��VTG��ZDA
	 */
	public static final byte[] RMC_GGA_GSA_GSV_STATEMENT_MODEL_COMMAND = "$PMTK314,1,1,1,1,1,5,0,0,0,0,0,0,0,0,0,0,0,1,0*2D\r\n"
			.getBytes();

	public void receive(byte[] command) {
		SerialPortManager.getInstance().write(command);
	}

	private boolean isOpen;

	public boolean isOpen() {
		return isOpen;
	}

	OnOpenListener onOpenListener;
	OnCloseListener onCloseListener;

	public void setOnOpenListener(OnOpenListener onOpenListener) {
		this.onOpenListener = onOpenListener;
	}

	public void setOnCloseListener(OnCloseListener onCloseListener) {
		this.onCloseListener = onCloseListener;
	}

	public interface OnOpenListener {
		void openSuccess();

		void openFail();
	}

	public interface OnCloseListener {
		void closeSuccess();

		void closeFail();
	}

	/**
	 * ����˵�����򿪱���ģ��
	 */
	public void open() {
		SerialPortManager.getInstance().write(OPEN_COMMAND);
		SystemClock.sleep(10);
		isOpen = true;
		SerialPortManager.getInstance().setLoopBuffer(looperBuffer);
		looperBuffer.start();
	}

	/**
	 * ����˵�����رձ���ģ��
	 */
	public void close() {
		SerialPortManager.getInstance().write(CLOSE_COMMAND);
		SystemClock.sleep(10);
		isOpen = true;
		SerialPortManager.getInstance().setLoopBuffer(null);
		looperBuffer.isStop = true;
	}

	public static final String GPS_Mode_Header = "$GP";
	public static final String BeiDou_GPS_Mode_Header = "$GN";
	public static final String BeiDou_Mode_Header = "$BD";

	/**
	 * ��GPS ģʽ
	 */
	public static final int MODE_GPS = 1;
	/**
	 * GPS+BDS ����ģʽ
	 */
	public static final int MODE_GPS_BEIDOU = 2;
	/**
	 * ��BDS ģʽ
	 */
	public static final int MODE_BEIDOU = 3;

	public static final int GGA = 1;
	public static final int GLL = 2;
	public static final int GSA = 3;
	public static final int GSV = 4;
	public static final int RMC = 5;
	public static final int VTG = 6;
	public static final int OPEN = 7;
	public static final int CLOSE = 8;

	private OnUpdateDataListener listerer;

	public void setOnUpdateDataListener(OnUpdateDataListener listerer) {
		this.listerer = listerer;
	}

	public void removeOnUpdateDataListener() {
		this.listerer = null;
	}

	public interface OnUpdateDataListener {
		/**
		 * ���������ΪGlobal Positioning System Fix Data��GGA��GPS ��λ��Ϣ
		 * @param mode GPS+BDS����ģʽ   ��  ��GPSģʽ ��  ģʽ ��BDSģʽ
		 * @param UTC  ʱ��
		 * @param Latitude γ��ddmm.mmmm���ȷָ�ʽ��ǰ��λ��������0��
		 * @param Indicator_N_S γ��N����γ����S����γ��
		 * @param Longitude ����dddmm.mmmm���ȷָ�ʽ��ǰ��λ��������0��
		 * @param Indicator_E_W ����E����������W��������
		 * @param PositionFixIndicator GPS ״̬��0=δ��λ��1=�ǲ�ֶ�λ��2=��ֶ�λ��3=��ЧPPS��6=���ڹ���
		 * @param satellitesUsed ����ʹ�õ�����������00 - 12����ǰ��λ��������0��
		 * @param HDOP HDOP ˮƽ�������ӣ�0.5 - 99.9��
		 * @param MSLAltitude ���θ߶ȣ�-9999.9 - 99999.9��
		 * @param units1   ������������Դ��ˮ׼��ĸ߶�
		 * @param GeoidSeparation   ���ʱ�䣨�����һ�ν��յ�����źſ�ʼ��������������ǲ�ֶ�λ��Ϊ�գ�
		 * @param units2     ���վID ��0000 - 1023��ǰ��λ��������0��������ǲ�ֶ�λ��Ϊ�գ�
		 * @param AgeOfDiffCorr  Null fields when DGPS is not used
		 * @param DiffRefStationID
		 */
		public void GGA(int mode, String UTC, String Latitude,
				String Indicator_N_S, String Longitude, String Indicator_E_W,
				String PositionFixIndicator, String satellitesUsed,
				String HDOP, String MSLAltitude, String units1,
				String GeoidSeparation, String units2, String AgeOfDiffCorr,
				String DiffRefStationID);

		/**
		 * ���������ΪGeographic Position��GLL������λ��Ϣ
		 * @param mode GPS+BDS����ģʽ   ��  ��GPSģʽ ��  ģʽ ��BDSģʽ
		 * @param Latitude γ��ddmm.mmmm���ȷָ�ʽ��ǰ��λ��������0��
		 * @param Indicator_N_S γ��N����γ����S����γ��
		 * @param Longitude ����dddmm.mmmm���ȷָ�ʽ��ǰ��λ��������0��
		 * @param Indicator_E_W ����E����������W��������
		 * @param UTC UTC ʱ�䣬hhmmss.sss ��ʽ
		 * @param location ״̬��A=��λ��V=δ��λ
		 */
		public void GLL(int mode, String Latitude, String Indicator_N_S,
				String Longitude, String Indicator_E_W, String UTC,
				String location);

		/**
		 * ���������ΪGPS DOP and Active Satellites��GSA����ǰ������Ϣ
		 * @param mode GPS+BDS����ģʽ   ��  ��GPSģʽ ��  ģʽ ��BDSģʽ
		 * @param mode1 ��λģʽ��A=�Զ��ֶ�2D/3D��M=�ֶ�2D/3D
		 * @param mode2 ��λ���ͣ�1=δ��λ��2=2D ��λ��3=3D ��λ
		 * @param IDOfSatelliteUsedList PRN �루α��������룩����1-12�ŵ�����ʹ�õ�����PRN ���ţ�00����ǰ��λ��������0��
		 * @param PDOP �ۺ�λ�þ������ӣ�0.5 - 99.9��
		 * @param HDOP ˮƽ�������ӣ�0.5 - 99.9��
		 * @param VDOP ��ֱ�������ӣ�0.5 - 99.9��
		 */
		public void GSA(int mode, String mode1, String mode2,
				List<String> IDOfSatelliteUsedList, String PDOP, String HDOP,
				String VDOP);

		/**
		 * ���������ΪGPS Satellites in View��GSV���ɼ�������Ϣ
		 * @param mode GPS+BDS����ģʽ   ��  ��GPSģʽ ��  ģʽ ��BDSģʽ
		 * @param TotalNumberOfMessage ����GSV ��������Ŀ��1 - 3��
		 * @param MessageNumber ����GSV ����Ǳ���GSV ���ĵڼ�����1 - 3��
		 * @param SatelliteInView ��ǰ�ɼ�����������00 - 12����ǰ��λ��������0��
		 * @param satelliteList
		 */
		public void GSV(int mode, String TotalNumberOfMessage,
				String MessageNumber, String SatelliteInView,
				List<Satellite> satelliteList);

		/**
		 * ���������ΪRecommended Minimum SpecificGPS/TRANSIT Data��RMC���Ƽ���С��λ��Ϣ
		 * @param mode GPS+BDS����ģʽ   ��  ��GPSģʽ ��  ģʽ ��BDSģʽ
		 * @param UTC UTC ʱ�䣬hhmmss.sss ��ʽ
		 * @param Status ״̬��A=��λ��V=δ��λ
		 * @param Latitude γ��ddmm.mmmm���ȷָ�ʽ��ǰ��λ��������0��
		 * @param Indicator_N_S γ��N����γ����S����γ��
		 * @param Longitude ����dddmm.mmmm���ȷָ�ʽ��ǰ��λ��������0��
		 * @param Indicator_E_W ����E����������W��������
		 * @param SpeedOverGround �ٶȣ��ڣ�Knots
		 * @param CourseOverGround ��λ�ǣ���
		 * @param Date UTC ���ڣ�DDMMYY ��ʽ
		 * @param MagneticVariation ��ƫ�ǣ���000 - 180���ȣ�ǰ��λ��������0��
		 * @param VariationSense ��ƫ�Ƿ���E=��W=��
		 * @param Mode 
		 * A:autonomous  
		 * D:DPGS
		 * E:DR
		 * N:data not valid
		 * R:coarse position
		 * S:simulator
		 */
		public void RMC(int mode, String UTC, String Status, String Latitude,
				String Indicator_N_S, String Longitude, String Indicator_E_W,
				String SpeedOverGround, String CourseOverGround, String Date,
				String MagneticVariation, String VariationSense, String Mode);

		/**
		 * ���������ΪTrack Made Good and Ground Speed��VTG�������ٶ���Ϣ
		 * @param mode GPS+BDS����ģʽ   ��  ��GPSģʽ ��  ģʽ ��BDSģʽ
		 * @param CourseOverGround1 �˶��Ƕȣ�000 - 359����ǰ��λ��������0��
		 * @param Reference1 T=�汱����ϵ
		 * @param CourseOverGround2 �˶��Ƕȣ�000 - 359����ǰ��λ��������0��
		 * @param Reference2 M=�ű�����ϵ
		 * @param SpeedOverGround1 ˮƽ�˶��ٶȣ�0.00����ǰ��λ��������0��
		 * @param Units1 N=�ڣ�Knots
		 * @param SpeedOverGround2 ˮƽ�˶��ٶȣ�0.00����ǰ��λ��������0��
		 * @param Units2 K=����/ʱ��km/h
		 * @param Mode 
		 * A:autonomous
		 * D:DGPS
		 * E:DR
		 * N:data not valid
		 * R:coarse position
		 * S:simulator
		 */
		public void VTG(int mode, String CourseOverGround1, String Reference1,
				String CourseOverGround2, String Reference2,
				String SpeedOverGround1, String Units1,
				String SpeedOverGround2, String Units2,String Mode);
	}

	public class Satellite {
		/**
		 * PRN �루α��������룩��01 - 32����ǰ��λ��������0��
		 */
		String SatelliteID;
		/**
		 * �������ǣ�00 - 90���ȣ�ǰ��λ��������0��
		 */
		String Elevation;
		/**
		 * ���Ƿ�λ�ǣ�00 - 359���ȣ�ǰ��λ��������0��
		 */
		String Azimuth;
		/**
		 * ����ȣ�00��99��dbHz
		 */
		String SNR;

		public String getSatelliteID() {
			return SatelliteID;
		}

		public void setSatelliteID(String satelliteID) {
			SatelliteID = satelliteID;
		}

		public String getElevation() {
			return Elevation;
		}

		public void setElevation(String elevation) {
			Elevation = elevation;
		}

		public String getAzimuth() {
			return Azimuth;
		}

		public void setAzimuth(String azimuth) {
			Azimuth = azimuth;
		}

		public String getSNR() {
			return SNR;
		}

		public void setSNR(String sNR) {
			SNR = sNR;
		}

	}

	private LoopBufferBeidou looperBuffer = new LoopBufferBeidou();

	public class LoopBufferBeidou extends Thread implements LooperBuffer {
		private byte[] mBuffer = new byte[1024 * 50];
		private int packetStartIndex = 0;
		private int dataLength = 0;
		private boolean isStop = false;

		@Override
		public synchronized void add(byte[] buf) {
			System.arraycopy(buf, 0, mBuffer, dataLength, buf.length);
			dataLength += buf.length;
		}

		@Override
		public synchronized byte[] getFullPacket() {
			byte[] temp = null;
			if (dataLength > 0 && mBuffer[0] == '$'
					&& mBuffer[dataLength - 5] == '*') {
				temp = new byte[dataLength];
				System.arraycopy(mBuffer, 0, temp, 0, dataLength);
				dataLength = 0;
			}
			return temp;
		}

		@Override
		public void run() {
			while (!isStop) {
				byte[] data = getFullPacket();
				if (data != null) {
					String dataStr = new String(data);
					String[] strs = dataStr.split("\r\n");
					for (int i = 0; i < strs.length; i++) {
						 Log.i("whw", "strs=" + strs[i]);
						if (strs[i].startsWith(GPS_Mode_Header)
								|| strs[i].startsWith(BeiDou_GPS_Mode_Header)
								|| strs[i].startsWith(BeiDou_Mode_Header)) {
							String[] items = strs[i].split(",");
							if (items[0].endsWith("GGA")) {
								mWorkerThreadHandler.obtainMessage(GGA, items)
										.sendToTarget();
							} else if (items[0].endsWith("GLL")) {
								mWorkerThreadHandler.obtainMessage(GLL, items)
										.sendToTarget();
							} else if (items[0].endsWith("GSA")) {
								mWorkerThreadHandler.obtainMessage(GSA, items)
										.sendToTarget();
							} else if (items[0].endsWith("GSV")) {
								logger.debug(strs[i]);
								mWorkerThreadHandler.obtainMessage(GSV, items)
										.sendToTarget();
							} else if (items[0].endsWith("RMC")) {
								mWorkerThreadHandler.obtainMessage(RMC, items)
										.sendToTarget();
							} else if (items[0].endsWith("VTG")) {
								mWorkerThreadHandler.obtainMessage(VTG, items)
										.sendToTarget();
							}
						}
					}
				}
				SystemClock.sleep(10);
			}
		}
	}

	private Handler mWorkerThreadHandler;

	Logger logger;

	public BeidouAPI(Logger logger) {
		this.logger = logger;
		mWorkerThreadHandler = new WorkerHandler();
	}

	protected class WorkerHandler extends Handler {

		private String getString(String srcStr) {
			int index = srcStr.indexOf("*");
			if (index >0) {
				return srcStr.substring(0, index);
			} else {
				return "";
			}
		}

		@Override
		public void handleMessage(Message msg) {
			if (listerer == null) {
				return;
			}
			switch (msg.what) {
			case GGA:
				String[] ggaStr = (String[]) msg.obj;
				listerer.GGA(
						ggaStr[0].startsWith(GPS_Mode_Header) ? MODE_GPS
								: (ggaStr[0].startsWith(BeiDou_GPS_Mode_Header) ? MODE_GPS_BEIDOU
										: MODE_BEIDOU), ggaStr[1], ggaStr[2],
						ggaStr[3], ggaStr[4], ggaStr[5], ggaStr[6], ggaStr[7],
						ggaStr[8], ggaStr[9], ggaStr[10], ggaStr[11],
						ggaStr[12], ggaStr[13], getString(ggaStr[14]));
				break;
			case GLL:
				String[] gllStr = (String[]) msg.obj;
				// Log.i("whw", "gllStr="+gllStr);
				listerer.GLL(
						gllStr[0].startsWith(GPS_Mode_Header) ? MODE_GPS
								: (gllStr[0].startsWith(BeiDou_GPS_Mode_Header) ? MODE_GPS_BEIDOU
										: MODE_BEIDOU), gllStr[1], gllStr[2],
						gllStr[3], gllStr[4], gllStr[5], gllStr[6]);
				break;
			case GSA:
				String[] gsaStr = (String[]) msg.obj;
				List<String> list = new ArrayList<String>();
				for (int i = 3; i < gsaStr.length - 3; i++) {
					list.add(gsaStr[i]);
				}
				listerer.GSA(
						gsaStr[0].startsWith(GPS_Mode_Header) ? MODE_GPS
								: (gsaStr[0].startsWith(BeiDou_GPS_Mode_Header) ? MODE_GPS_BEIDOU
										: MODE_BEIDOU), gsaStr[1], gsaStr[2],
						list, gsaStr[gsaStr.length - 3],
						gsaStr[gsaStr.length - 2],
						getString(gsaStr[gsaStr.length - 1]));
				break;
			case GSV:
				String[] gsvStr = (String[]) msg.obj;
				List<Satellite> satelliteList = new ArrayList<Satellite>();
				Satellite satellite = null;
				for (int i = 0; i < (gsvStr.length - 5) / 4; i++) {
					satellite = new Satellite();
					satellite.setSatelliteID(gsvStr[3 + i * 4 + 1]);
					satellite.setElevation(gsvStr[3 + i * 4 + 2]);
					satellite.setAzimuth(gsvStr[3 + i * 4 + 3]);
					satellite.setSNR(gsvStr[3 + i * 4 + 4]);
					satelliteList.add(satellite);
				}
				listerer.GSV(
						gsvStr[0].startsWith(GPS_Mode_Header) ? MODE_GPS
								: (gsvStr[0].startsWith(BeiDou_GPS_Mode_Header) ? MODE_GPS_BEIDOU
										: MODE_BEIDOU), gsvStr[1], gsvStr[2],
						satelliteList.size() == 0 ? getString(gsvStr[3])
								: gsvStr[3], satelliteList);
				break;
			case RMC:
				String[] rmcStr = (String[]) msg.obj;
				listerer.RMC(
						rmcStr[0].startsWith(GPS_Mode_Header) ? MODE_GPS
								: (rmcStr[0].startsWith(BeiDou_GPS_Mode_Header) ? MODE_GPS_BEIDOU
										: MODE_BEIDOU), rmcStr[1], rmcStr[2],
						rmcStr[3], rmcStr[4], rmcStr[5], rmcStr[6], rmcStr[7],
						rmcStr[8], rmcStr[9], rmcStr[10], rmcStr[11],
						getString(rmcStr[12]));
				break;
			case VTG:
				String[] vtgStr = (String[]) msg.obj;
				listerer.VTG(
						vtgStr[0].startsWith(GPS_Mode_Header) ? MODE_GPS
								: (vtgStr[0].startsWith(BeiDou_GPS_Mode_Header) ? MODE_GPS_BEIDOU
										: MODE_BEIDOU), vtgStr[1], vtgStr[2],
						vtgStr[3], vtgStr[4], vtgStr[5], vtgStr[6], vtgStr[7],
						vtgStr[8], getString(vtgStr[9]));
				break;
			case OPEN:
				if (onOpenListener != null) {
					if ((Boolean) msg.obj) {
						onOpenListener.openSuccess();
					} else {
						onOpenListener.openFail();
					}
				}
				break;
			case CLOSE:
				if (onCloseListener != null) {
					if ((Boolean) msg.obj) {
						onCloseListener.closeSuccess();
					} else {
						onCloseListener.closeFail();
					}
				}
				break;
			default:
				break;
			}
		}
	}

}
