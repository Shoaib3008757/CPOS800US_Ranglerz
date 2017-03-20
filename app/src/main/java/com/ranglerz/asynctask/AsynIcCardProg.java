/**   
 * @Title: AsynIcCardProg.java
 * @Package com.authentication.asynctask
 * @Description: TODO
 * @author Weishun.Xu   
 * @date 2016年3月25日 上午10:58:03
 * @version V1.0   
 */
package com.ranglerz.asynctask;

import com.ranglerz.logic.IcCradProgAPI;
import com.ranglerz.logic.IcCradProgAPI.Result;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class AsynIcCardProg extends Handler {
	private static final int RESETCARD = 0x00;
	private static final int SELECTFILE = 0x01;
	private static final int READFILE = 0x02;
	private static final int WRITEFILE = 0x03;
	private static final int UPDATEFILE = 0x04;
	private static final int ERASEFILE = 0x05;
	private static final int MAKEMF = 0x06;
	private static final int MAKEDF = 0x07;
	private static final int MAKEBINARYFILE = 0x08;

	private static final int RESETCARD_SUCCESS = 0xa0;
	private static final int RESETCARD_FAILURE = 0xa1;
	private static final int SELECTFILE_SUCCESS = 0xa2;
	private static final int SELECTFILE_FAILURE = 0xa3;
	private static final int READFILE_SUCCESS = 0xa4;
	private static final int READFILE_FAILURE = 0xa5;
	private static final int WRITEFILE_SUCCESS = 0xa6;
	private static final int WRITEFILE_FAILURE = 0xa7;
	private static final int UPDATEFILE_SUCCESS = 0xa8;
	private static final int UPDATEFILE_FAILURE = 0xa9;
	private static final int ERASEFILE_SUCCESS = 0xaa;
	private static final int ERASEFILE_FAILURE = 0xab;
	private static final int MAKEMF_SUCCESS = 0xac;
	private static final int MAKEMF_FAILURE = 0xad;
	private static final int MAKEDF_SUCCESS = 0xae;
	private static final int MAKEDF_FAILURE = 0xaf;
	private static final int MAKEBINARYFILE_SUCCESS = 0xb0;
	private static final int MAKEBINARYFILE_FAILURE = 0xb1;

	private IcCradProgAPI icCradProgAPI;
	private Handler workerThreadHandler;

	public AsynIcCardProg(Looper looper) {
		Log.i("cy", "Enter function AsynIcCardProg-AsynIcCardProg()");
		this.workerThreadHandler = createHandler(looper);
		this.icCradProgAPI = new IcCradProgAPI();
	}

	protected Handler createHandler(Looper looper) {
		Log.i("cy", "Enter function AsynIcCardProg-createHandler()");
		return new WorkerHandler(looper);
	}

	public void resetCard(int flag) {
		Log.i("cy", "Enter function AsynIcCardProg-resetCard()");
		// this.workerThreadHandler.sendEmptyMessage(RESETCARD);
		this.workerThreadHandler.obtainMessage(RESETCARD, flag, -1)
				.sendToTarget();
	}

	public void eraseFile() {
		Log.i("cy", "Enter function AsynIcCardProg-eraseFile()");
		this.workerThreadHandler.obtainMessage(ERASEFILE).sendToTarget();
	}

	public void makeMF() {
		Log.i("cy", "Enter function AsynIcCardProg-makeMF()");
		this.workerThreadHandler.obtainMessage(MAKEMF).sendToTarget();
	}

	public void makeDF() {
		Log.i("cy", "Enter function AsynIcCardProg-makeDF()");
		this.workerThreadHandler.obtainMessage(MAKEDF).sendToTarget();
	}

	public void makeBinaryFile() {
		Log.i("cy", "Enter function AsynIcCardProg-makeBinaryFile()");
		this.workerThreadHandler.obtainMessage(MAKEBINARYFILE).sendToTarget();
	}

	public void selectFile() {
		Log.i("cy", "Enter function AsynIcCardProg-selectFile()");
		this.workerThreadHandler.obtainMessage(SELECTFILE).sendToTarget();
	}

	public void writeBinaryFile(byte[] buffer) {
		Log.i("cy", "Enter function AsynIcCardProg-writeBinaryFile()");
		this.workerThreadHandler.obtainMessage(WRITEFILE, buffer)
				.sendToTarget();
	}

	public void updateBinaryFile(byte[] buffer) {
		Log.i("cy", "Enter function AsynIcCardProg-updateBinaryFile()");
		this.workerThreadHandler.obtainMessage(UPDATEFILE, buffer)
				.sendToTarget();
	}

	public void readBinaryFile(int len) {
		Log.i("cy", "Enter function AsynIcCardProg-readBinaryFile()");
		this.workerThreadHandler.obtainMessage(READFILE, len, -1)
				.sendToTarget();
	}

	protected class WorkerHandler extends Handler {
		public WorkerHandler(Looper looper) {
			super(looper);
			Log.i("cy",
					"Enter function AsynIcCardProg-WorkerHandler-WorkerHandler()");
		}

		@Override
		public void handleMessage(Message msg) {
			Log.i("cy",
					"Enter function AsynIcCardProg-WorkerHandler-handleMessage()");
			int ret = -1;
			switch (msg.what) {
			case RESETCARD:
				ret = AsynIcCardProg.this.icCradProgAPI.resetCard(msg.arg1);
				if (Result.SUCCESS == ret) {
					AsynIcCardProg.this.obtainMessage(RESETCARD_SUCCESS, ret,
							-1).sendToTarget();
				} else {
					AsynIcCardProg.this.obtainMessage(RESETCARD_FAILURE, ret,
							-1).sendToTarget();
				}
				break;

			case ERASEFILE:
				byte[] efCmd = { 0x02, 0x00, 0x0D, (byte) 0x80, 0x0E, 0x00,
						0x00, 0x08, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
						(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
						(byte) 0xFF };
				byte[] efData = AsynIcCardProg.this.icCradProgAPI
						.operateICApdu(efCmd, efCmd.length);
				ret = Result.FAILURE;
				if (null == efData) {
					ret = Result.FAILURE;
				} else {
					ret = Result.SUCCESS;
				}

				if (Result.SUCCESS == ret) {
					AsynIcCardProg.this.obtainMessage(ERASEFILE_SUCCESS, ret,
							-1).sendToTarget();
				} else {
					AsynIcCardProg.this.obtainMessage(ERASEFILE_FAILURE, ret,
							-1).sendToTarget();
				}
				break;

			case MAKEMF:
				byte[] mfCmd = { 0x02, 0x00, 0x1D, (byte) 0x80, (byte) 0xE0,
						0x00, 0x00, 0x18, (byte) 0xFF, (byte) 0xFF,
						(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
						(byte) 0xFF, (byte) 0xFF, 0x0F, 0x01, 0x31, 0x50, 0x41,
						0x59, 0x2E, 0x53, 0x59, 0x53, 0x2E, 0x44, 0x44, 0x46,
						0x30, 0x31 };
				byte[] mfData = AsynIcCardProg.this.icCradProgAPI
						.operateICApdu(mfCmd, mfCmd.length);
				ret = Result.FAILURE;
				if (null == mfData) {
					ret = Result.FAILURE;
				} else {
					ret = Result.SUCCESS;
				}

				if (Result.SUCCESS == ret) {
					AsynIcCardProg.this.obtainMessage(MAKEMF_SUCCESS, ret, -1)
							.sendToTarget();
				} else {
					AsynIcCardProg.this.obtainMessage(MAKEMF_FAILURE, ret, -1)
							.sendToTarget();
				}
				break;

			case MAKEDF:
				byte[] dfCmd = { 0x02, 0x00, 0x12, (byte) 0x80, (byte) 0xE0,
						0x01, 0x00, 0x0D, 0x2F, 0x01, 0x0F, 0x00, (byte) 0xA0,
						0x00, 0x00, 0x00, 0x03, (byte) 0x86, (byte) 0x98, 0x07,
						0x01 };
				byte[] dfData = AsynIcCardProg.this.icCradProgAPI
						.operateICApdu(dfCmd, dfCmd.length);
				ret = Result.FAILURE;
				if (null == dfData) {
					ret = Result.FAILURE;
				} else {
					ret = Result.SUCCESS;
				}

				if (Result.SUCCESS == ret) {
					AsynIcCardProg.this.obtainMessage(MAKEDF_SUCCESS, ret, -1)
							.sendToTarget();
				} else {
					AsynIcCardProg.this.obtainMessage(MAKEDF_FAILURE, ret, -1)
							.sendToTarget();
				}
				break;

			case MAKEBINARYFILE:
				byte[] bfCmd = { 0x02, 0x00, 0x0C, (byte) 0x80, (byte) 0xE0,
						0x02, 0x00, 0x07, 0x00, 0x16, 0x00, 0x0F, 0x0F, 0x00,
						0x27 };
				byte[] bfData = AsynIcCardProg.this.icCradProgAPI
						.operateICApdu(bfCmd, bfCmd.length);
				ret = Result.FAILURE;
				if (null == bfData) {
					ret = Result.FAILURE;
				} else {
					ret = Result.SUCCESS;
				}

				if (Result.SUCCESS == ret) {
					AsynIcCardProg.this.obtainMessage(MAKEBINARYFILE_SUCCESS,
							ret, -1).sendToTarget();
				} else {
					AsynIcCardProg.this.obtainMessage(MAKEBINARYFILE_FAILURE,
							ret, -1).sendToTarget();
				}
				break;

			case SELECTFILE:
				byte[] sfCmd = { 0x02, 0x00, 0x07, 0x00, (byte) 0xA4, 0x02,
						0x00, 0x02, 0x00, 0x16 };
				byte[] sfData = AsynIcCardProg.this.icCradProgAPI
						.operateICApdu(sfCmd, sfCmd.length);
				ret = Result.FAILURE;
				if (null == sfData) {
					ret = Result.FAILURE;
				} else {
					ret = Result.SUCCESS;
				}

				if (Result.SUCCESS == ret) {
					AsynIcCardProg.this.obtainMessage(SELECTFILE_SUCCESS, ret,
							-1).sendToTarget();
				} else {
					AsynIcCardProg.this.obtainMessage(SELECTFILE_FAILURE, ret,
							-1).sendToTarget();
				}
				break;

			case WRITEFILE:
				byte[] wBuf = (byte[]) msg.obj;
				byte[] wfCmd = new byte[1 + 2 + 5 + wBuf.length];
				int wLenKT = 5 + wBuf.length;
				wfCmd[0] = 0x02;
				wfCmd[1] = 0x00;
				wfCmd[2] = (byte) wLenKT;
				wfCmd[3] = 0x00;
				wfCmd[4] = (byte) 0xD0;
				wfCmd[5] = (byte) 0x96;
				wfCmd[6] = 0x00;
				wfCmd[7] = (byte) wBuf.length;
				System.arraycopy(wBuf, 0, wfCmd, 7, wBuf.length);
				byte[] wfData = AsynIcCardProg.this.icCradProgAPI
						.operateICApdu(wfCmd, wfCmd.length);
				ret = Result.FAILURE;
				if (null == wfData) {
					ret = Result.FAILURE;
				} else {
					ret = Result.SUCCESS;
				}

				if (Result.SUCCESS == ret) {
					AsynIcCardProg.this.obtainMessage(WRITEFILE_SUCCESS, ret,
							-1).sendToTarget();
				} else {
					AsynIcCardProg.this.obtainMessage(WRITEFILE_FAILURE, ret,
							-1).sendToTarget();
				}
				break;

			case UPDATEFILE:
				byte[] uBuf = (byte[]) msg.obj;
				byte[] ufCmd = new byte[1 + 2 + 5 + uBuf.length];
				int uLenKT = 5 + uBuf.length;
				ufCmd[0] = 0x02;
				ufCmd[1] = 0x00;
				ufCmd[2] = (byte) uLenKT;
				ufCmd[3] = 0x00;
				ufCmd[4] = (byte) 0xD6;
				ufCmd[5] = (byte) 0x96;
				ufCmd[6] = 0x00;
				ufCmd[7] = (byte) uBuf.length;
				System.arraycopy(uBuf, 0, ufCmd, 7, uBuf.length);
				byte[] ufData = AsynIcCardProg.this.icCradProgAPI
						.operateICApdu(ufCmd, ufCmd.length);
				ret = Result.FAILURE;
				if (null == ufData) {
					ret = Result.FAILURE;
				} else {
					ret = Result.SUCCESS;
				}

				if (Result.SUCCESS == ret) {
					AsynIcCardProg.this.obtainMessage(UPDATEFILE_SUCCESS, ret,
							-1).sendToTarget();
				} else {
					AsynIcCardProg.this.obtainMessage(UPDATEFILE_FAILURE, ret,
							-1).sendToTarget();
				}
				break;

			case READFILE:
				int len = msg.arg1;
				byte[] rfCmd = { 0x02, 0x00, 0x05, 0x00, (byte) 0xB0,
						(byte) 0x96, 0x00, (byte) len };
				byte[] rfData = AsynIcCardProg.this.icCradProgAPI
						.operateICApdu(rfCmd, rfCmd.length);
				byte[] revData = new byte[len];
				if (null == rfData) {
					AsynIcCardProg.this.obtainMessage(READFILE_FAILURE)
							.sendToTarget();
				} else {
					System.arraycopy(rfData, 0, revData, 0, len);
					AsynIcCardProg.this
							.obtainMessage(READFILE_SUCCESS, revData)
							.sendToTarget();
				}
				break;

			default:
				break;
			}
		}
	}

	public interface OnResetCardListener {
		void onResetCardSuccess(int confirmationCode);

		void onResetCardFail(int confirmationCode);
	}

	private OnResetCardListener onResetCardListener;

	public void setOnResetCardListener(OnResetCardListener onResetCardListener) {
		Log.i("cy", "Enter function AsynIcCardProg-setOnResetCardListener()");
		this.onResetCardListener = onResetCardListener;
	}

	public interface OnEraseFileListener {
		void onEraseFileSuccess(int confirmationCode);

		void onEraseFileFail(int confirmationCode);
	}

	private OnEraseFileListener onEraseFileListener;

	public void setOnEraseFileListener(OnEraseFileListener onEraseFileListener) {
		Log.i("cy", "Enter function AsynIcCardProg-setOnEraseFileListener()");
		this.onEraseFileListener = onEraseFileListener;
	}

	public interface OnMakeMFListener {
		void onMakeMFSuccess(int confirmationCode);

		void onMakeMFFail(int confirmationCode);
	}

	private OnMakeMFListener onMakeMFListener;

	public void setOnMakeMFListener(OnMakeMFListener onMakeMFListener) {
		Log.i("cy", "Enter function AsynIcCardProg-setOnMakeMFListener()");
		this.onMakeMFListener = onMakeMFListener;
	}

	public interface OnMakeDFListener {
		void onMakeDFSuccess(int confirmationCode);

		void onMakeDFFail(int confirmationCode);
	}

	private OnMakeDFListener onMakeDFListener;

	public void setOnMakeDFListener(OnMakeDFListener onMakeDFListener) {
		Log.i("cy", "Enter function AsynIcCardProg-setOnMakeDFListener()");
		this.onMakeDFListener = onMakeDFListener;
	}

	public interface OnMakeBinaryFileListener {
		void onMakeBinaryFileSuccess(int confirmationCode);

		void onMakeBinaryFileFail(int confirmationCode);
	}

	private OnMakeBinaryFileListener onMakeBinaryFileListener;

	public void setOnMakeBinaryFileListener(
			OnMakeBinaryFileListener onMakeBinaryFileListener) {
		Log.i("cy",
				"Enter function AsynIcCardProg-setOnMakeBinaryFileListener()");
		this.onMakeBinaryFileListener = onMakeBinaryFileListener;
	}

	public interface OnSelectFileListener {
		void onSelectFileSuccess(int confirmationCode);

		void onSelectFileFail(int confirmationCode);
	}

	private OnSelectFileListener onSelectFileListener;

	public void setOnSelectFileListener(
			OnSelectFileListener onSelectFileListener) {
		Log.i("cy", "Enter function AsynIcCardProg-setOnSelectFileListener()");
		this.onSelectFileListener = onSelectFileListener;
	}

	public interface OnWriteFileListener {
		void onWriteFileSuccess(int confirmationCode);

		void onWriteFileFail(int confirmationCode);
	}

	private OnWriteFileListener onWriteFileListener;

	public void setOnWriteFileListener(OnWriteFileListener onWriteFileListener) {
		Log.i("cy", "Enter function AsynIcCardProg-setOnWriteFileListener()");
		this.onWriteFileListener = onWriteFileListener;
	}

	public interface OnUpdateFileListener {
		void onUpdateFileSuccess(int confirmationCode);

		void onUpdateFileFail(int confirmationCode);
	}

	private OnUpdateFileListener onUpdateFileListener;

	public void setOnUpdateFileListener(
			OnUpdateFileListener onUpdateFileListener) {
		Log.i("cy", "Enter function AsynIcCardProg-setOnUpdateFileListener()");
		this.onUpdateFileListener = onUpdateFileListener;
	}

	public interface OnReadFileListener {
		void onReadFileSuccess(byte[] buffer);

		void onReadFileFail(int confirmationCode);
	}

	private OnReadFileListener onReadFileListener;

	public void setOnReadFileListener(OnReadFileListener onReadFileListener) {
		Log.i("cy", "Enter function AsynIcCardProg-setOnReadFileListener()");
		this.onReadFileListener = onReadFileListener;
	}

	public void handleMessage(Message msg) {
		Log.i("cy", "Enter function AsynIcCardProg-handleMessage()");
		super.handleMessage(msg);
		switch (msg.what) {
		case RESETCARD_SUCCESS:
			if (null != this.onResetCardListener) {
				this.onResetCardListener.onResetCardSuccess(msg.arg1);
			}
			break;

		case RESETCARD_FAILURE:
			if (null != this.onResetCardListener) {
				this.onResetCardListener.onResetCardFail(msg.arg1);
			}
			break;

		case ERASEFILE_SUCCESS:
			if (null != this.onEraseFileListener) {
				this.onEraseFileListener.onEraseFileSuccess(msg.arg1);
			}
			break;

		case ERASEFILE_FAILURE:
			if (null != this.onEraseFileListener) {
				this.onEraseFileListener.onEraseFileFail(msg.arg1);
			}
			break;

		case MAKEMF_SUCCESS:
			if (null != this.onMakeMFListener) {
				this.onMakeMFListener.onMakeMFSuccess(msg.arg1);
			}
			break;

		case MAKEMF_FAILURE:
			if (null != this.onMakeMFListener) {
				this.onMakeMFListener.onMakeMFFail(msg.arg1);
			}
			break;

		case MAKEDF_SUCCESS:
			if (null != this.onMakeDFListener) {
				this.onMakeDFListener.onMakeDFSuccess(msg.arg1);
			}
			break;

		case MAKEDF_FAILURE:
			if (null != this.onMakeDFListener) {
				this.onMakeDFListener.onMakeDFSuccess(msg.arg1);
			}
			break;

		case MAKEBINARYFILE_SUCCESS:
			if (null != this.onMakeBinaryFileListener) {
				this.onMakeBinaryFileListener.onMakeBinaryFileSuccess(msg.arg1);
			}
			break;

		case MAKEBINARYFILE_FAILURE:
			if (null != this.onMakeBinaryFileListener) {
				this.onMakeBinaryFileListener.onMakeBinaryFileFail(msg.arg1);
			}
			break;

		case SELECTFILE_SUCCESS:
			if (null != this.onSelectFileListener) {
				this.onSelectFileListener.onSelectFileSuccess(msg.arg1);
			}
			break;

		case SELECTFILE_FAILURE:
			if (null != this.onSelectFileListener) {
				this.onSelectFileListener.onSelectFileFail(msg.arg1);
			}
			break;

		case WRITEFILE_SUCCESS:
			if (null != this.onWriteFileListener) {
				this.onWriteFileListener.onWriteFileSuccess(msg.arg1);
			}
			break;

		case WRITEFILE_FAILURE:
			if (null != this.onWriteFileListener) {
				this.onWriteFileListener.onWriteFileFail(msg.arg1);
			}
			break;

		case UPDATEFILE_SUCCESS:
			if (null != this.onUpdateFileListener) {
				this.onUpdateFileListener.onUpdateFileSuccess(msg.arg1);
			}
			break;

		case UPDATEFILE_FAILURE:
			if (null != this.onUpdateFileListener) {
				this.onUpdateFileListener.onUpdateFileFail(msg.arg1);
			}
			break;

		case READFILE_SUCCESS:
			if (null != this.onReadFileListener) {
				if (null != msg.obj) {
					this.onReadFileListener.onReadFileSuccess((byte[]) msg.obj);
				}
			}
			break;

		case READFILE_FAILURE:
			if (null != this.onReadFileListener) {
				this.onReadFileListener.onReadFileFail(msg.arg1);
			}
			break;

		default:
			break;
		}
	}
}