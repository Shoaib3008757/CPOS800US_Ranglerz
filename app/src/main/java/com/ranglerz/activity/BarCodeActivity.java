package com.ranglerz.activity;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ranglerz.utils.DataUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android_serialport_api.BarCodeAPI;
import android_serialport_api.SerialPortManager;

public class BarCodeActivity extends Activity implements OnClickListener {
	private String longstr;
	private BarCodeAPI api;
	private Button scan_button;
	private EditText str, line;
	private ProgressDialog progressDialog;
	private ExecutorService singleThreadExecutor;
	private boolean flag = true;
	private Timer mtimer;
	private Handler mhandler;
	private byte[] buffer = new byte[1024];
	private String strdata = "";
	private int n;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.barcode_activity);
		initView();
		
		SerialPortManager.getInstance().openSerialPortPrinter();
		showProgressDialog(R.string.isopenGpio);
		mtimer.schedule(new TimerTask() {

			@Override
			public void run() {
				cancleProgressDialog();
			}
		}, 2000);
	}

	private void initView() {
		scan_button = (Button) findViewById(R.id.scan);
		scan_button.setOnClickListener(this);
		str = (EditText) findViewById(R.id.et_str);
		line = (EditText) findViewById(R.id.et_line);
		line.setSelection(line.length());
		singleThreadExecutor = Executors.newSingleThreadExecutor();

		mhandler = new Handler();
		mtimer = new Timer();
		api = new BarCodeAPI();
		longstr = "";
		n = 1;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.scan:
			showProgressDialog("Scanning...");
			singleThreadExecutor.execute(send);
			flag = true;
			singleThreadExecutor.execute(getdata);
			break;
		default:
			break;
		}
	}

	/**
	 * ����ɨ��ָ��
	 */
	Runnable send = new Runnable() {

		@Override
		public void run() {
			api.startScanner();
			mtimer.schedule(new TimerTask() {

				@Override
				public void run() {
					flag = false;
					cancleProgressDialog();
				}
			}, 3000);
		}
	};

	/**
	 * ���շ��ص����
	 */
	Runnable getdata = new Runnable() {

		@Override
		public void run() {
			while (flag) {
				int length = SerialPortManager.getInstance().read(buffer, 3000,
						100);
				if (length > 0) {
					byte[] getData = new byte[length];
					System.arraycopy(buffer, 0, getData, 0, length);
					strdata = DataUtils.hexStr2Str(DataUtils
							.toHexString(getData));
					flag = false;
					cancleProgressDialog();
					mhandler.post(new Runnable() {

						@Override
						public void run() {
							str.setText(strdata);
							longstr = "No" + n + ":" + strdata + "\r\n"
									+ longstr;
							line.setText(longstr);
							n++;
						}
					});
				}
			}
		}
	};

	private void showProgressDialog(int resId) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getResources().getString(resId)); // ͨ��ID��ȡ��Ӧ��ֵ
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();
	}

	private void cancleProgressDialog() {
		if (null != progressDialog && progressDialog.isShowing()) {
			progressDialog.cancel();
			progressDialog = null;
		}
	}
	
	private void showProgressDialog(String message) {
		Log.i("cy",
				"Enter function IcCardBankActivity-showProgressDialog(String)");

		this.progressDialog = new ProgressDialog(this);
		this.progressDialog.setMessage(message);
		if (!this.progressDialog.isShowing()) {
			this.progressDialog.show();
		}
	}

	@Override
	protected void onDestroy() {
		singleThreadExecutor.shutdownNow();
		SerialPortManager.getInstance().closeSerialPort(2);
		super.onDestroy();
	}
}