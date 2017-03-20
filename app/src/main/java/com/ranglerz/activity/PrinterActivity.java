package com.ranglerz.activity;

import java.util.Timer;
import java.util.TimerTask;

import com.ranglerz.utils.DataUtils;
import com.ranglerz.utils.DataValidator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android_serialport_api.PrinterAPI;
import android_serialport_api.SerialPortManager;

public class PrinterActivity extends Activity implements OnClickListener,
		OnItemSelectedListener, OnCheckedChangeListener {

	private Button mBtPrint, mBtInit, mBtPrintBC, mBtPrintQC, mBtPrintPic;
	private EditText mEdPrintstr, mEdPrintBC, mEdPrintQC;
	private CheckBox mCbHeight, mCbWeight, mCbBold, mCbUnderLine;
	private Spinner mSAlign, mSBarcodeType, mSQrcodeType, mSFlash;
	private PrinterAPI api;

	private boolean mBoolTimesHeight = false;
	private boolean mBoolTimesWeight = false;
	private boolean mBoolTimesCrude = false;
	private boolean mBoolTimesUnderLine = false;
	private int mBarcodeType;
	private int mQrcodeType;
	private int mFlashImageType = 0;

	private ProgressDialog progressDialog;
	private Timer mtimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_printer);

		init();

		SerialPortManager.getInstance().openSerialPortPrinter();
		showProgressDialog(R.string.isopenGpio);
		mtimer.schedule(new TimerTask() {

			@Override
			public void run() {
				cancleProgressDialog();
			}
		}, 1000);
	}

	private void init() {
		api = new PrinterAPI();

		mCbHeight = (CheckBox) findViewById(R.id.timesHeight_check);
		mCbHeight.setOnCheckedChangeListener(this);
		mCbWeight = (CheckBox) findViewById(R.id.timesWeight_check);
		mCbWeight.setOnCheckedChangeListener(this);
		mCbBold = (CheckBox) findViewById(R.id.setBold_check);
		mCbBold.setOnCheckedChangeListener(this);
		mCbUnderLine = (CheckBox) findViewById(R.id.setUnderLine_check);
		mCbUnderLine.setOnCheckedChangeListener(this);

		mBtPrint = (Button) findViewById(R.id.paperPrint_printBtn);
		mBtPrint.setOnClickListener(this);
		mBtInit = (Button) findViewById(R.id.init_printBtn);
		mBtInit.setOnClickListener(this);
		mBtPrintBC = (Button) findViewById(R.id.barcode_printBtn);
		mBtPrintBC.setOnClickListener(this);
		mBtPrintQC = (Button) findViewById(R.id.qrcode_printBtn);
		mBtPrintQC.setOnClickListener(this);
		mBtPrintPic = (Button) findViewById(R.id.flashPic_printBtn);
		mBtPrintPic.setOnClickListener(this);

		mEdPrintstr = (EditText) findViewById(R.id.inputData_printBtn);
		mEdPrintBC = (EditText) findViewById(R.id.inputBarData_printBtn);
		mEdPrintQC = (EditText) findViewById(R.id.inputQrData_printBtn);

		mSAlign = (Spinner) findViewById(R.id.alignType_check);
		mSAlign.setOnItemSelectedListener(this);
		mSBarcodeType = (Spinner) findViewById(R.id.barcodeType_check);
		mSBarcodeType.setOnItemSelectedListener(this);
		mSQrcodeType = (Spinner) findViewById(R.id.qrcodeType_check);
		mSQrcodeType.setOnItemSelectedListener(this);
		mSFlash = (Spinner) findViewById(R.id.flashPicType_check);
		mSFlash.setOnItemSelectedListener(this);

		mtimer = new Timer();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.paperPrint_printBtn:// 打印手输入内容
			if (mEdPrintstr.getText().toString().length() == 0) {
				Toast.makeText(this, "Input can not be empty！",
						Toast.LENGTH_SHORT).show();
			} else {
				String str = mEdPrintstr.getText().toString();
				if (DataUtils.str2Hexstr(str).length() / 2 >= 2000) {
					Toast.makeText(
							this,
							"Current number of bytes:"
									+ DataUtils.str2Hexstr(str).length() / 2
									+ ",Can not be more than 1999",
							Toast.LENGTH_SHORT).show();
				} else {
					api.printPaper(str);
					Toast.makeText(this, str.length() + "", Toast.LENGTH_SHORT)
							.show();
				}
			}
			break;
		case R.id.init_printBtn:// 初始化
			api.initPrint();
			mCbHeight.setChecked(false);
			mCbWeight.setChecked(false);
			mCbBold.setChecked(false);
			mCbUnderLine.setChecked(false);
			break;
		case R.id.barcode_printBtn:// 打印条码
			printBarcode(mEdPrintBC.getText().toString(), mBarcodeType);
			break;
		case R.id.qrcode_printBtn:// 打印二维码
			printQrcode(mEdPrintQC.getText().toString(), mQrcodeType);
			break;
		case R.id.flashPic_printBtn:// 打印图片
			api.printFlashImage(mFlashImageType);
			break;
		default:
			break;
		}
	}

	/**
	 * 打印条码
	 * 
	 * @param str
	 *            打印的数据
	 * @param codeType
	 *            条码类型
	 */
	private void printBarcode(String str, int codeType) {
		if (str.length() == 0) {
			Toast.makeText(this, "Input can not be empty！", Toast.LENGTH_SHORT)
					.show();
			return;
		} else if (DataValidator.isIntege1(str) == false) {
			Toast.makeText(this, "Must be numeric！", Toast.LENGTH_SHORT).show();
			return;
		} else if (codeType == 0 && str.length() > 11) {
			Toast.makeText(this, "UPCA maximum length is 11 ！",
					Toast.LENGTH_SHORT).show();
			return;
		} else if (codeType == 1 && str.length() > 7) {
			Toast.makeText(this, "UPCE maximum length is 7 ！",
					Toast.LENGTH_SHORT).show();
			return;
		} else if (codeType == 4 && str.length() > 13) {
			Toast.makeText(this, "ITF14 maximum length is 13 ！",
					Toast.LENGTH_SHORT).show();
			return;
		} else if (codeType == 5 && str.length() > 12) {
			Toast.makeText(this, "EAN13/EAN8 maximum length is 12 ！！",
					Toast.LENGTH_SHORT).show();
			return;
		} else if (codeType == 5 && str.length() < 6) {
			Toast.makeText(this, "EAN13/EAN8 minimum length is 6 ！",
					Toast.LENGTH_SHORT).show();
			return;
		}

		api.printBarcode(str, codeType);
	}

	/**
	 * 打印二维码
	 * 
	 * @param str
	 *            打印的数据
	 * @param codeType
	 *            二维码类型
	 */
	private void printQrcode(String str, int codeType) {
		if (str.equals("")) {
			Toast.makeText(this, "Input can not be empty！", Toast.LENGTH_SHORT)
					.show();
		} else {
			api.printQrcode(str, codeType);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		switch (parent.getId()) {
		case R.id.alignType_check:// 对齐方式
			api.setAlighType(position);
			break;
		case R.id.barcodeType_check:// 条码类型
			mBarcodeType = position;
			break;
		case R.id.qrcodeType_check:// 二维码类型
			mQrcodeType = position;
			break;
		case R.id.flashPicType_check:// flash图片选择
			mFlashImageType = position;
			break;
		default:
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.timesHeight_check:// 倍高
			mBoolTimesHeight = isChecked;
			api.setKGCU(mBoolTimesWeight, mBoolTimesHeight, mBoolTimesCrude,
					mBoolTimesUnderLine);
			break;
		case R.id.timesWeight_check:// 倍宽
			mBoolTimesWeight = isChecked;
			api.setKGCU(mBoolTimesWeight, mBoolTimesHeight, mBoolTimesCrude,
					mBoolTimesUnderLine);
			break;
		case R.id.setBold_check:// 加粗
			mBoolTimesCrude = isChecked;
			api.setKGCU(mBoolTimesWeight, mBoolTimesHeight, mBoolTimesCrude,
					mBoolTimesUnderLine);
			break;
		case R.id.setUnderLine_check:// 下划线
			mBoolTimesUnderLine = isChecked;
			api.setKGCU(mBoolTimesWeight, mBoolTimesHeight, mBoolTimesCrude,
					mBoolTimesUnderLine);
			break;
		default:
			break;
		}
	}

	private void showProgressDialog(int resId) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getResources().getString(resId));
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
	protected void onDestroy() {
		SerialPortManager.getInstance().closeSerialPort(2);
		super.onDestroy();
	}
}