package com.ranglerz.activity;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ranglerz.logic.MagStripeCardAPI;
import com.ranglerz.utils.DataUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android_serialport_api.SerialPortManager;

public class MagStripeCardActivity extends Activity implements OnClickListener {
	private Button btnRead;
	private Button btnClear;
	private TextView one, two, three;

	private ExecutorService singleThreadExecutor;
	private boolean flag, isup = true;
	private ProgressDialog progressDialog;
	private MagStripeCardAPI api;
	private Timer mtimer;
	private byte[] buffer;

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
							Toast.makeText(getApplicationContext(),R.string.upGpioSuccess,
									Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}
	};

	private Runnable task = new Runnable() {
		@Override
		public void run() {
			while (flag) {
				buffer = api.readCard();
				if (buffer != null) {
					flag = false;
					cancleProgressDialog();
					handler.post(new Runnable() {

						@Override
						public void run() {
							String m = new String(buffer);
							String n = DataUtils.toHexString(buffer);
							if (n.equals("ee")) {
								Toast.makeText(getApplicationContext(),R.string.noData,
										Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(getApplicationContext(),
										"读取成功:数据" + m + "16进制数据：" + n,
										Toast.LENGTH_SHORT).show();
							}
							updateInfo(buffer);
							buffer = null;
						}
					});
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.magstripecard);
		initView();
	}

	private void initView() {
		api = new MagStripeCardAPI();
		singleThreadExecutor = Executors.newSingleThreadExecutor();

		btnRead = (Button) findViewById(R.id.CircleRead_MsCard);
		btnRead.setOnClickListener(this);

		btnClear = (Button) findViewById(R.id.Clear_MsCard);
		btnClear.setOnClickListener(this);

		one = (TextView) findViewById(R.id.one);
		two = (TextView) findViewById(R.id.two);
		three = (TextView) findViewById(R.id.three);

		mtimer = new Timer();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.CircleRead_MsCard:
			showProgressDialog("Now it is reading......");
			api.send();
			flag = true;
			singleThreadExecutor.execute(task);
			mtimer.schedule(new TimerTask() {

				@Override
				public void run() {
					handler.post(new Runnable() {

						@Override
						public void run() {
							if (flag) {
								flag = false;
								cancleProgressDialog();
								Toast.makeText(getApplicationContext(), R.string.readFailure,
										Toast.LENGTH_SHORT).show();
								buffer = null;
							}
						}
					});
				}
			}, 5000);
			break;
		case R.id.Clear_MsCard:
			ClearAll();
			break;
		default:
			break;
		}
	}

	private void ClearAll() {
		one.setText("");
		two.setText("");
		three.setText("");
	}

	private void showProgressDialog(String message) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(message);
		progressDialog.setCancelable(false);
		if (!progressDialog.isShowing()) {
			progressDialog.show();
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

	/**
	 * 截取字符串
	 */
	private void updateInfo(byte[] buffer) {
		String str = DataUtils.toHexString(buffer);
		boolean isN = str.contains("25");
		boolean isM = str.contains("3b");
		int one = str.indexOf("25");
		int two = str.indexOf("3b");
		if (isN) {// 有一轨数据
			this.one.setText(toStringHex1(str.substring(one + 2, two)));
		} else {// 没有有一轨数据
			this.one.setText("");
		}
		if (isM) {// 有二轨数据
			String newstr = str.substring(two + 2, str.length());// 新的数据
			boolean isO = newstr.contains("3b");// 判断是否有三轨数据
			int three = newstr.indexOf("3b");
			if (isO) {// 有三轨数据
				this.two.setText(toStringHex1(newstr.substring(0, three)));
				this.three.setText(toStringHex1(newstr.substring(three + 2,
						newstr.length())));
			} else {
				this.two.setText(toStringHex1(newstr));
			}
		} else {// 没有有二轨道或者三轨数据
			this.two.setText("");
		}
	}

	/**
	 * 把16进制转为utf8格式
	 * 
	 * @param s
	 * @return
	 */
	public static String toStringHex1(String s) {
		byte[] baKeyword = new byte[s.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
			try {
				baKeyword[i] = (byte) (0xff & Integer.parseInt(
						s.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			s = new String(baKeyword, "utf-8");// UTF-16le:Not
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return s;
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
		handler.removeCallbacks(task);
		SerialPortManager.getInstance().closeSerialPort(1);
		super.onDestroy();
	}
}