package com.ranglerz.activity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ranglerz.asynctask.AsynIcCardProg;
import com.ranglerz.asynctask.AsynIcCardProg.OnEraseFileListener;
import com.ranglerz.asynctask.AsynIcCardProg.OnMakeBinaryFileListener;
import com.ranglerz.asynctask.AsynIcCardProg.OnMakeDFListener;
import com.ranglerz.asynctask.AsynIcCardProg.OnMakeMFListener;
import com.ranglerz.asynctask.AsynIcCardProg.OnReadFileListener;
import com.ranglerz.asynctask.AsynIcCardProg.OnResetCardListener;
import com.ranglerz.asynctask.AsynIcCardProg.OnSelectFileListener;
import com.ranglerz.asynctask.AsynIcCardProg.OnUpdateFileListener;
import com.ranglerz.asynctask.AsynIcCardProg.OnWriteFileListener;
import com.ranglerz.utils.DataUtils;
import com.ranglerz.utils.ToastUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android_serialport_api.SerialPortManager;

/**
 * 工程卡
 */
public class IcCardProgActivity extends Activity implements OnClickListener {
	private boolean isup;
	private ExecutorService singleThreadExecutor;

	private int flag = 0;
	private int success_num = 0;
	private int faile_num = 0;
	private AsynIcCardProg asynIcCardProg;
	private MyApplication application;
	private Button continu;
	private TextView success, faile;
	/**
	 * 复位IC卡
	 */
	private Button btnResetCard;
	/**
	 * 删除文件
	 */
	private Button btnEraseFile;
	/**
	 * 新建主目录
	 */
	private Button btnMakeMF;
	/**
	 * 新建子目录
	 */
	private Button btnMakeDF;
	/**
	 * 新建文件夹
	 */
	private Button btnMakeBinFile;
	/**
	 * 选文件
	 */
	private Button btnSelectFile;
	/**
	 * 读文件
	 */
	private Button btnReadFile;
	/**
	 * 写文件
	 */
	private Button btnWriteFile;
	/**
	 * 写文件
	 */
	private Button btnUpdateFile;
	/**
	 * 清屏
	 */
	private Button btnClear;
	private EditText edTxtReadFile;
	private EditText edTxtWriteFile;
	private EditText edTxtUpdateFile;
	private ProgressDialog progressDialog;
	private int readLength = 0;

	private Handler handler = new Handler();

	private Runnable up = new Runnable() {

		@Override
		public void run() {
			while (isup) {
				if (SerialPortManager.getInstance().isUpGpio()) {
					isup = false;
					handler.post(new Runnable() {

						@Override
						public void run() {
							cancleProgressDialog();
							Toast.makeText(getApplicationContext(), R.string.upGpioSuccess,
									Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.iccardprog);
		initView();
		initData();
	}

	private void initView() {
		this.btnResetCard = (Button) findViewById(R.id.ResetCard_ICCard);
		this.btnResetCard.setOnClickListener(this);

		this.btnSelectFile = (Button) findViewById(R.id.SelectFile_ICCard);
		this.btnSelectFile.setOnClickListener(this);

		this.btnReadFile = (Button) findViewById(R.id.ReadFile_ICCard);
		this.btnReadFile.setOnClickListener(this);

		this.btnWriteFile = (Button) findViewById(R.id.WriteFile_ICCard);
		this.btnWriteFile.setOnClickListener(this);

		this.btnUpdateFile = (Button) findViewById(R.id.UpdateFile_ICCard);
		this.btnUpdateFile.setOnClickListener(this);

		this.btnEraseFile = (Button) findViewById(R.id.EraseFile_ICCard);
		this.btnEraseFile.setOnClickListener(this);

		this.btnMakeMF = (Button) findViewById(R.id.MakeMF_ICCard);
		this.btnMakeMF.setOnClickListener(this);

		this.btnMakeDF = (Button) findViewById(R.id.MakeDF_ICCard);
		this.btnMakeDF.setOnClickListener(this);

		this.btnMakeBinFile = (Button) findViewById(R.id.MakeBinFile_ICCard);
		this.btnMakeBinFile.setOnClickListener(this);

		this.btnClear = (Button) findViewById(R.id.Clear_ICCard);
		this.btnClear.setOnClickListener(this);

		this.edTxtReadFile = (EditText) findViewById(R.id.ReadData_ICCard);
		this.edTxtWriteFile = (EditText) findViewById(R.id.WriteData_ICCard);
		this.edTxtUpdateFile = (EditText) findViewById(R.id.UpdateData_ICCard);

		continu = (Button) findViewById(R.id.bt_continu);
		continu.setOnClickListener(this);

		success = (TextView) findViewById(R.id.tv_success);
		faile = (TextView) findViewById(R.id.tv_faile);

		singleThreadExecutor=Executors.newSingleThreadExecutor();
	}

	private void initData() {
		application = (MyApplication) getApplicationContext();
		asynIcCardProg = new AsynIcCardProg(application.getHandlerThread()
				.getLooper());

		asynIcCardProg.setOnResetCardListener(new OnResetCardListener() {

			@Override
			public void onResetCardSuccess(int confirmationCode) {
				cancleProgressDialog();
				success.setText(success_num++ + "");
				if (flag == 1) {
					if (!DataUtils.isContactless) {
						asynIcCardProg.resetCard(0);
					} else {
						asynIcCardProg.resetCard(1);
					}
				}
			}

			@Override
			public void onResetCardFail(int confirmationCode) {
				cancleProgressDialog();
				faile.setText(faile_num++ + "");
				if (flag == 1) {
					if (!DataUtils.isContactless) {
						asynIcCardProg.resetCard(0);
					} else {
						asynIcCardProg.resetCard(1);
					}
				}
			}
		});

		asynIcCardProg.setOnEraseFileListener(new OnEraseFileListener() {

			@Override
			public void onEraseFileSuccess(int confirmationCode) {
				cancleProgressDialog();
				ToastUtil.showToast(IcCardProgActivity.this, "Delete file success");
			}

			@Override
			public void onEraseFileFail(int confirmationCode) {
				cancleProgressDialog();
				ToastUtil.showToast(IcCardProgActivity.this, "Delete file failed");
			}
		});

		asynIcCardProg.setOnMakeMFListener(new OnMakeMFListener() {

			@Override
			public void onMakeMFSuccess(int confirmationCode) {
				cancleProgressDialog();
				ToastUtil.showToast(IcCardProgActivity.this, "New home directory success");
			}

			@Override
			public void onMakeMFFail(int confirmationCode) {
				cancleProgressDialog();
				ToastUtil.showToast(IcCardProgActivity.this, "Failed to create a new home directory ");
			}
		});

		asynIcCardProg.setOnMakeDFListener(new OnMakeDFListener() {

			@Override
			public void onMakeDFSuccess(int confirmationCode) {
				cancleProgressDialog();
				ToastUtil.showToast(IcCardProgActivity.this, "Delete sub files successfully");
			}

			@Override
			public void onMakeDFFail(int confirmationCode) {
				cancleProgressDialog();
				ToastUtil.showToast(IcCardProgActivity.this, "Delete sub file failed ");
			}
		});

		asynIcCardProg
				.setOnMakeBinaryFileListener(new OnMakeBinaryFileListener() {

					@Override
					public void onMakeBinaryFileSuccess(int confirmationCode) {
						cancleProgressDialog();
						ToastUtil.showToast(IcCardProgActivity.this, "New file success");
					}

					@Override
					public void onMakeBinaryFileFail(int confirmationCode) {
						cancleProgressDialog();
						ToastUtil.showToast(IcCardProgActivity.this, "New file failed");
					}
				});

		asynIcCardProg.setOnSelectFileListener(new OnSelectFileListener() {

			@Override
			public void onSelectFileSuccess(int confirmationCode) {
				cancleProgressDialog();
				ToastUtil.showToast(IcCardProgActivity.this, "Selected file success");
			}

			@Override
			public void onSelectFileFail(int confirmationCode) {
				cancleProgressDialog();
				ToastUtil.showToast(IcCardProgActivity.this, "Selected file failed");
			}
		});

		asynIcCardProg.setOnWriteFileListener(new OnWriteFileListener() {

			@Override
			public void onWriteFileSuccess(int confirmationCode) {
				cancleProgressDialog();
				ToastUtil.showToast(IcCardProgActivity.this, "Write file success");
			}

			@Override
			public void onWriteFileFail(int confirmationCode) {
				cancleProgressDialog();
				ToastUtil.showToast(IcCardProgActivity.this, "Write file failed");
			}
		});

		asynIcCardProg.setOnUpdateFileListener(new OnUpdateFileListener() {

			@Override
			public void onUpdateFileSuccess(int confirmationCode) {
				cancleProgressDialog();
				ToastUtil.showToast(IcCardProgActivity.this, "Update file successfully");
			}

			@Override
			public void onUpdateFileFail(int confirmationCode) {
				cancleProgressDialog();
				ToastUtil.showToast(IcCardProgActivity.this, "Update file failed");
			}
		});

		asynIcCardProg.setOnReadFileListener(new OnReadFileListener() {

			@Override
			public void onReadFileSuccess(byte[] buffer) {
				cancleProgressDialog();
				ToastUtil.showToast(IcCardProgActivity.this, "Read the document successfully");
				IcCardProgActivity.this.edTxtReadFile
						.setText(new String(buffer));
			}

			@Override
			public void onReadFileFail(int confirmationCode) {
				cancleProgressDialog();
				ToastUtil.showToast(IcCardProgActivity.this, "Failed to read file");
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ResetCard_ICCard:// 复位IC卡
			showProgressDialog("Now it is reseting......");
			if (!DataUtils.isContactless) {
				this.asynIcCardProg.resetCard(0);
			} else {
				this.asynIcCardProg.resetCard(1);
			}
			break;
		case R.id.EraseFile_ICCard:// 删除文件
			showProgressDialog("Now it is erasing......");
			asynIcCardProg.eraseFile();
			break;
		case R.id.MakeMF_ICCard:// 新建主目录
			showProgressDialog("Now it is makemf......");
			asynIcCardProg.makeMF();
			break;
		case R.id.MakeDF_ICCard:// 新建子目录
			showProgressDialog("Now it is makedf......");
			asynIcCardProg.makeDF();
			break;
		case R.id.MakeBinFile_ICCard:// 新建文件夹
			showProgressDialog("Now it is makeFile......");
			asynIcCardProg.makeBinaryFile();
			break;
		case R.id.SelectFile_ICCard:// 选文件
			showProgressDialog("Now it is selecting......");
			asynIcCardProg.selectFile();
			break;
		case R.id.WriteFile_ICCard:// 写文件
			showProgressDialog("Now it is writing......");
			String strWrite = edTxtWriteFile.getText().toString();
			byte[] toWrite = strWrite.getBytes();
			readLength = toWrite.length;
			asynIcCardProg.writeBinaryFile(toWrite);
			break;
		case R.id.UpdateFile_ICCard:// 更新文件
			showProgressDialog("Now it is updating......");
			String strUpdate = edTxtUpdateFile.getText().toString();
			byte[] toUpdate = strUpdate.getBytes();
			readLength = toUpdate.length;
			asynIcCardProg.updateBinaryFile(toUpdate);
			break;
		case R.id.ReadFile_ICCard:// 读文件
			showProgressDialog("Now it is reading......");
			asynIcCardProg.readBinaryFile(readLength);
			break;
		case R.id.Clear_ICCard:// 清屏
			clear();
			break;
		case R.id.bt_continu:
			showProgressDialog("Now it is reseting......");
			flag = 1;
			if (!DataUtils.isContactless) {
				this.asynIcCardProg.resetCard(0);
			} else {
				this.asynIcCardProg.resetCard(1);
			}
			break;
		default:
			break;
		}
	}

	private void clear() {
		this.edTxtWriteFile.setText("");
		this.edTxtUpdateFile.setText("");
		this.edTxtReadFile.setText("");
	}

	private void showProgressDialog(String message) {
		this.progressDialog = new ProgressDialog(this);
		this.progressDialog.setMessage(message);
		if (!this.progressDialog.isShowing()) {
			this.progressDialog.show();
		}
	}
	
	private void showProgressDialog(int resId) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getResources().getString(resId)); // 通过ID获取对应的值
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();
	}

	private void cancleProgressDialog() {
		if (this.progressDialog != null && this.progressDialog.isShowing()) {
			this.progressDialog.cancel();
			this.progressDialog = null;
		}
	}

	@Override
	protected void onResume() {
		SerialPortManager.getInstance().closeSerialPort(1);
		isup = true;
		showProgressDialog(R.string.isopenGpio);
		SerialPortManager.getInstance().openSerialPort();
		singleThreadExecutor.execute(up);
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		flag = 0;
		SerialPortManager.getInstance().closeSerialPort(1);
		super.onDestroy();
	}
}