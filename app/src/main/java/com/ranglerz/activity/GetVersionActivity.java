package com.ranglerz.activity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android_serialport_api.GetVersion;
import android_serialport_api.SerialPortManager;

public class GetVersionActivity extends Activity implements OnClickListener {
	private ExecutorService singleThreadExecutor;
	private boolean isup = true;

	private Button mBtApk, mBtStm, mBt3255;
	private TextView mTvApk, mTvStm, mTv32550;
	private GetVersion api;
	private ProgressDialog progressDialog;
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
							Toast.makeText(getApplicationContext(), "32550上电成功！",
									Toast.LENGTH_SHORT).show();
							mTv32550.setText(api.get32550Version());
							SerialPortManager.getInstance().closeSerialPort(1);
						}
					});
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_get_version);

		init();
	}

	private void init() {
		api = new GetVersion();
		singleThreadExecutor = Executors.newSingleThreadExecutor();

		mBtApk = (Button) findViewById(R.id.bt_getApk);
		mBtApk.setOnClickListener(this);

		mBtStm = (Button) findViewById(R.id.bt_getStmVersion);
		mBtStm.setOnClickListener(this);

		mBt3255 = (Button) findViewById(R.id.bt_get3255Version);
		mBt3255.setOnClickListener(this);

		mTvApk = (TextView) findViewById(R.id.tv_apkVersion);
		mTvStm = (TextView) findViewById(R.id.tv_stmVersion);
		mTv32550 = (TextView) findViewById(R.id.tv_32550Version);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_getApk:
			mTvApk.setText(api.getApkVersion(getApplicationContext()));
			break;
		case R.id.bt_getStmVersion:
			SerialPortManager.getInstance().openSerialPortPrinter();
			mTvStm.setText(api.getStm32Version());
			SerialPortManager.getInstance().closeSerialPort(2);
			break;
		case R.id.bt_get3255Version:
			SerialPortManager.getInstance().closeSerialPort(1);
			isup = true;
			showProgressDialog("正在上电。。。");
			SerialPortManager.getInstance().openSerialPort();
			singleThreadExecutor.execute(up);
			break;
		default:
			break;
		}
	}

	private void showProgressDialog(String str) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(str);
		progressDialog.setCanceledOnTouchOutside(false);
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
		SerialPortManager.getInstance().closeSerialPort(0);
		super.onDestroy();
	}
}