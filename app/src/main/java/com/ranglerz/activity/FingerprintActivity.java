package com.ranglerz.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.ranglerz.asynctask.AsyncFingerprint;
import com.ranglerz.asynctask.AsyncFingerprint.OnEmptyListener;
import com.ranglerz.asynctask.AsyncFingerprint.OnCalibrationListener;
import com.ranglerz.utils.ToastUtil;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android_serialport_api.FingerprintAPI;
import android_serialport_api.SerialPortManager;

public class FingerprintActivity extends BaseActivity implements
		OnClickListener {
	/**
	 * 注册指纹
	 */
	private Button btnRegister;
	/**
	 * 验证指纹
	 */
	private Button btnValidate;
	/**
	 * 连续注册指纹
	 */
	private Button btnRegister2;
	/**
	 * 连续验证指纹
	 */
	private Button btnValidate2;
	/**
	 * 清空指纹
	 */
	private Button btnClear;
	/**
	 * 校准
	 */
	private Button btnCalibration;
	private Button btnBack;
	/**
	 * 指纹图
	 */
	private ImageView imgFinger;

	private byte[] model;

	private MyApplication application;
	private ProgressDialog progressDialog;
	private AsyncFingerprint asyncFingerprint;
	private Timer mtimer;

	private String rootPath = Environment.getExternalStorageDirectory()
			.getAbsolutePath();

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			Log.i("cy",
					"Enter function FingerprintActivity-Handler-handleMessage()");
			super.handleMessage(msg);

			switch (msg.what) {
			case AsyncFingerprint.SHOW_PROGRESSDIALOG:
				cancleProgressDialog();
				showProgressDialog((Integer) msg.obj);
				break;

			case AsyncFingerprint.SHOW_FINGER_IMAGE:
				showFingerImage(msg.arg1, (byte[]) msg.obj);
				break;

			case AsyncFingerprint.SHOW_FINGER_MODEL:
				FingerprintActivity.this.model = (byte[]) msg.obj;
				if (null != FingerprintActivity.this.model) {
					Log.i("cy", "The length of Finger model is "
							+ FingerprintActivity.this.model.length);
				}
				cancleProgressDialog();
				ToastUtil.showToast(FingerprintActivity.this, "pageId = "
						+ msg.arg1 + "  store = " + msg.arg2);
				break;

			case AsyncFingerprint.REGISTER_SUCCESS:
				cancleProgressDialog();
				if (null != msg.obj) {
					Integer id = (Integer) msg.obj;
					ToastUtil.showToast(FingerprintActivity.this,
							getString(R.string.register_success)
									+ "  pageId = " + id);
				} else {
					ToastUtil.showToast(FingerprintActivity.this,
							R.string.register_success);
				}
				break;

			case AsyncFingerprint.REGISTER_FAIL:
				cancleProgressDialog();
				ToastUtil.showToast(FingerprintActivity.this,
						R.string.register_fail);
				break;

			case AsyncFingerprint.VALIDATE_RESULT1:
				cancleProgressDialog();
				showValidateResult((Boolean) msg.obj);
				break;

			case AsyncFingerprint.VALIDATE_RESULT2:
				cancleProgressDialog();
				Integer ret = (Integer) msg.obj;
				if (-1 != ret) {
					ToastUtil.showToast(FingerprintActivity.this,
							getString(R.string.verifying_through) + "  pageId="
									+ ret);
				} else {
					showValidateResult(false);
				}
				break;

			case AsyncFingerprint.UP_IMAGE_RESULT:
				cancleProgressDialog();
				ToastUtil
						.showToast(FingerprintActivity.this, (Integer) msg.obj);
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fingerprint);
		initView();
		initData();

		SerialPortManager.getInstance().openSerialPortPrinter();
		showProgressDialog(R.string.isopenGpio);
		mtimer.schedule(new TimerTask() {

			@Override
			public void run() {
				cancleProgressDialog();
			}
		}, 1500);
	}

	private void initView() {
		btnRegister = (Button) findViewById(R.id.Register_Finger);
		btnRegister.setOnClickListener(this);

		btnValidate = (Button) findViewById(R.id.Validate_Finger);
		btnValidate.setOnClickListener(this);

		btnRegister2 = (Button) findViewById(R.id.Register2_Finger);
		btnRegister2.setOnClickListener(this);

		btnValidate2 = (Button) findViewById(R.id.Validate2_Finger);
		btnValidate2.setOnClickListener(this);

		btnClear = (Button) findViewById(R.id.ClearFlash_Finger);
		btnClear.setOnClickListener(this);

		btnCalibration = (Button) findViewById(R.id.Calibration_Finger);
		btnCalibration.setOnClickListener(this);

		btnBack = (Button) findViewById(R.id.BackReg_Finger);
		btnBack.setOnClickListener(this);

		imgFinger = (ImageView) findViewById(R.id.Image_Finger);

		mtimer = new Timer();
	}

	private void initData() {
		application = (MyApplication) this.getApplicationContext();
		asyncFingerprint = new AsyncFingerprint(application.getHandlerThread()
				.getLooper(), handler);

		asyncFingerprint
				.setFingerprintType(FingerprintAPI.SMALL_FINGERPRINT_SIZE);

		asyncFingerprint.setOnEmptyListener(new OnEmptyListener() {

			@Override
			public void onEmptySuccess() {
				ToastUtil.showToast(FingerprintActivity.this,
						R.string.clear_flash_success);
			}

			@Override
			public void onEmptyFail() {
				ToastUtil.showToast(FingerprintActivity.this,
						R.string.clear_flash_fail);
			}
		});

		asyncFingerprint.setOnCalibrationListener(new OnCalibrationListener() {

			@Override
			public void onCalibrationSuccess() {
				ToastUtil.showToast(FingerprintActivity.this,
						R.string.calibration_success);
			}

			@Override
			public void onCalibrationFail() {
				ToastUtil.showToast(FingerprintActivity.this,
						R.string.calibration_fail);
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.Register_Finger:// 注册指纹
			asyncFingerprint.register();
			break;
		case R.id.Validate_Finger:// 验证指纹
			if (model != null) {
				asyncFingerprint.validate(model);
			} else {
				ToastUtil.showToast(FingerprintActivity.this,
						R.string.first_register);
			}
			break;
		case R.id.Register2_Finger:// 连续注册指纹
			asyncFingerprint.register2();
			break;
		case R.id.Validate2_Finger:// 连续验证指纹
			asyncFingerprint.validate2();
			break;
		case R.id.Calibration_Finger:// 校准
			asyncFingerprint.PS_Calibration();
			break;
		case R.id.ClearFlash_Finger:// 清空指纹库
			asyncFingerprint.PS_Empty();
			break;
		case R.id.BackReg_Finger:// 退出
			finish();
			break;
		default:
			break;
		}
	}

	private void showValidateResult(boolean matchResult) {
		if (matchResult) {
			ToastUtil.showToast(FingerprintActivity.this,
					R.string.verifying_through);
		} else {
			ToastUtil.showToast(FingerprintActivity.this,
					R.string.verifying_fail);
		}
	}

	@SuppressWarnings("deprecation")
	private void showFingerImage(int fingerType, byte[] data) {
		Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
		if (image.getHeight() != 360) {
			// ToastUtil.showToast(FingerprintActivity.this,"WIDTH=" +
			// image.getWidth() + "  HEIGHT=" + image.getHeight());
		}
		imgFinger.setBackgroundDrawable(new BitmapDrawable(image));
		writeToFile(data);
	}

	private void writeToFile(byte[] data) {
		Log.i("cy", "Enter function FingerprintActivity-writeToFile()");
		String dir = rootPath + File.separator + "fingerprint_image";
		File dirPath = new File(dir);
		if (!dirPath.exists()) {
			dirPath.mkdir();
		}

		String filePath = dir + "/" + System.currentTimeMillis() + ".bmp";
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
		}

		FileOutputStream fos = null;
		try {
			file.createNewFile();
			fos = new FileOutputStream(file);
			fos.write(data);
			fos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != fos) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void showProgressDialog(int resId) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getResources().getString(resId)); // 通过ID获取对应的值
		progressDialog.setCanceledOnTouchOutside(false);

		progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (KeyEvent.KEYCODE_BACK == keyCode) {
					asyncFingerprint.setStop(true);
				}
				return false;
			}
		});
		progressDialog.show();
	}

	private void cancleProgressDialog() {
		if (null != progressDialog && progressDialog.isShowing()) {
			progressDialog.cancel();
			progressDialog = null;
		}
	}

	@Override
	protected void onDestroy() {
		cancleProgressDialog();
		handler.removeCallbacksAndMessages(null);
		SerialPortManager.getInstance().closeSerialPort(2);
		super.onDestroy();
	}
}