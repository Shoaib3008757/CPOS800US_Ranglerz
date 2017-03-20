package com.ranglerz.activity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ranglerz.asynctask.AsynIcCardOrder;
import com.ranglerz.asynctask.AsynIcCardOrder.OnSendOperCmdListener;
import com.ranglerz.utils.ToastUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android_serialport_api.SerialPortManager;

/**
 * 定制卡
 */
public class IcCardOrderActivity extends Activity implements OnClickListener {
	private boolean isup;
	private ExecutorService singleThreadExecutor;

	private EditText edTxtOperCmd;
	private Button btnOperCmd;;
	private Button btnClear;
	private TextView txtRetInfo;

	private ProgressDialog progressDialog;
	private MyApplication application;
	private AsynIcCardOrder asynIcCardOrder;

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
							Toast.makeText(getApplicationContext(), "上电成功！",
									Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.iccardorder);
		initView();
		initData();
	}

	private void initView() {
		edTxtOperCmd = (EditText) findViewById(R.id.OperCmd_ICCard);

		btnOperCmd = (Button) findViewById(R.id.SendOperCmd_ICCard);
		btnOperCmd.setOnClickListener(this);

		this.btnClear = (Button) findViewById(R.id.ClearOrder_ICCard);
		this.btnClear.setOnClickListener(this);

		this.txtRetInfo = (TextView) findViewById(R.id.ReturnInfo_ICCard);

		singleThreadExecutor = Executors.newSingleThreadExecutor();
	}

	private void initData() {
		application = (MyApplication) getApplicationContext();
		asynIcCardOrder = new AsynIcCardOrder(application.getHandlerThread()
				.getLooper());

		asynIcCardOrder.setOnSendOperCmdListener(new OnSendOperCmdListener() {

			@Override
			public void onSendOperCmdSuccess(String result) {
				cancleProgressDialog();
				ToastUtil.showToast(IcCardOrderActivity.this, "Gets the return value successfully");
				IcCardOrderActivity.this.txtRetInfo.setText(result);
			}

			@Override
			public void onSendOperCmdFail(int resultCode) {
				cancleProgressDialog();
				ToastUtil.showToast(IcCardOrderActivity.this, "Gets the return value failed");
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.SendOperCmd_ICCard:
			showProgressDialog("Now, sending the command...");
			if (TextUtils.isEmpty(this.edTxtOperCmd.getText())) {
				ToastUtil.showToast(this, "Input can not be empty ");
				cancleProgressDialog();
				return;
			}
			if (!isHexStrNumber(this.edTxtOperCmd.getText().toString())) {
				ToastUtil.showToast(this, "Input is not valid");
				cancleProgressDialog();
				return;
			}

			final byte[] tempCmd = hexToByteArray(this.edTxtOperCmd.getText()
					.toString());

			byte[] operCmd = new byte[tempCmd.length + 2];
			if (1 == tempCmd.length) {
				if (0x00 == tempCmd[0]) {
					operCmd[0] = 0x00;
					operCmd[1] = 0x00;
					operCmd[2] = 0x00;
				} else if (0x01 == tempCmd[0]) {
					operCmd[0] = 0x01;
					operCmd[1] = 0x00;
					operCmd[2] = 0x01;
				} else {
					ToastUtil.showToast(this,
							"Please Input the right command!!!");
					return;
				}
				this.asynIcCardOrder.sendOperCmd(operCmd);
			} else {
				this.asynIcCardOrder.sendOperCmd(tempCmd);
			}
			break;
		case R.id.ClearOrder_ICCard:
			clear();
			break;
		default:
			break;
		}
	}

	public static boolean isHexStrNumber(String s) {
		Matcher m = Pattern.compile("^[0-9A-Fa-f]+$").matcher(s);
		return m.matches();
	}

	static public byte[] hexToByteArray(String inHex)// hex字符串转字节数组
	{
		int hexlen = inHex.length();
		byte[] result;
		if (isOdd(hexlen) == 1) {// 奇数
			hexlen++;
			result = new byte[(hexlen / 2)];
			inHex = "0" + inHex;
		} else {// 偶数
			result = new byte[(hexlen / 2)];
		}

		int j = 0;
		for (int i = 0; i < hexlen; i += 2) {
			result[j] = hexToByte(inHex.substring(i, i + 2));
			j++;
		}
		return result;
	}

	static public int isOdd(int num) {
		return num & 0x1;
	}

	static public byte hexToByte(String inHex)// Hex字符串转byte
	{
		return (byte) Integer.parseInt(inHex, 16);
	}

	private void clear() {
		this.txtRetInfo.setText("");
		this.edTxtOperCmd.setText("");
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
		if (null != this.progressDialog && this.progressDialog.isShowing()) {
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
		SerialPortManager.getInstance().closeSerialPort(1);
		super.onDestroy();
	}
}