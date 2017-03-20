package com.ranglerz.activity;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import com.ranglerz.utils.DataUtils;
import com.ranglerz.utils.ToastUtil;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android_serialport_api.SerialPortManager;
import android_serialport_api.UHFHXAPI;

public class UHFActivity extends BaseUHFActivity {
	
	private boolean isStop;

	UHFHXAPI api;

	/**
	 * 用于集中处理显示等事件信息的静态类
	 * 
	 * @author chenshanjing
	 * 
	 */
	class StartHander extends Handler {
		WeakReference<Activity> mActivityRef;

		StartHander(Activity activity) {
			mActivityRef = new WeakReference<Activity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			Activity activity = mActivityRef.get();
			if (activity == null) {
				return;
			}

			switch (msg.what) {
			case MSG_SHOW_EPC_INFO:
				ShowEPC((String) msg.obj);
				break;

			case MSG_DISMISS_CONNECT_WAIT_SHOW:
				prgDlg.dismiss();
				if ((Boolean) msg.obj) {
					Toast.makeText(activity,
							activity.getText(R.string.info_connect_success),
							Toast.LENGTH_SHORT).show();
					setting.setEnabled(true);
					buttonInv.setClickable(true);
				} else {
					Toast.makeText(activity,
							activity.getText(R.string.info_connect_fail),
							Toast.LENGTH_SHORT).show();
				}
				break;
			case INVENTORY_OVER:
				ToastUtil
						.showToast(UHFActivity.this, R.string.inventory_over);
				break;

			}
		}
	};

	private Handler hMsg = new StartHander(this);
	private ProgressDialog progressDialog;
	private Timer mtimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_uhf);
		api = new UHFHXAPI();
		txtCount = (TextView) findViewById(R.id.txtCount);
		txtTimes = (TextView) findViewById(R.id.txtTimes);
		setting  = (Button)findViewById(R.id.setting_params);
		setting.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				UHFDialogFragment dialog = new UHFDialogFragment();
				dialog.show(getFragmentManager(), "corewise");
			}
		});
		buttonInv = (ToggleButton) findViewById(R.id.togBtn_inv);
		buttonInv.setClickable(true);
		final FragmentManager fragmentManager = getFragmentManager();
		objFragment = (TaglistFragment) fragmentManager
				.findFragmentById(R.id.fragment_taglist);

		buttonInv.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				buttonInv.setClickable(false);
				if (isChecked) {
					isStop = false;
					Inv();
					setting.setEnabled(false);
				} else {
					isStop = true;
					setting.setEnabled(true);
				}

				buttonInv.setClickable(true);
			}
		});
		
		mtimer=new Timer();
		progressDialog=new ProgressDialog(this);
		
		SerialPortManager.getInstance().openSerialPortPrinter();
		showProgressDialog("正在上电...");
		mtimer.schedule(new TimerTask() {

			@Override
			public void run() {
				cancleProgressDialog();
			}
		}, 1500);
	}

	/**
	 * 显示搜索得到的标签信息
	 * 
	 * @param activity
	 * @param flagID
	 */
	public static void ShowEPC(String flagID) {
		if(mediaPlayer == null){
			return;
		}
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.seekTo(0);
		} else {
			mediaPlayer.start();
		}
		if (!tagInfoList.contains(flagID)) {
			number.put(flagID, 1);
			tagCount++;
			tagInfoList.add(flagID);
			objFragment.addItem(flagID);

			try {
				txtCount.setText(String.format("%d", tagCount));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			int num = number.get(flagID);
			number.put(flagID, ++num);
			Log.i("whw", "flagID=" + flagID + "   num=" + num);
		}
		objFragment.myadapter.notifyDataSetChanged();
		tagTimes++;
		try {
			txtTimes.setText(String.format("%d", tagTimes));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 开启盘点操作
	 */
	public void Inv() {
		pool.execute(task);
		tagInfoList.clear();
		tagCount = 0;
		tagTimes = 0;
		objFragment.clearItem();

		try {
			txtCount.setText(String.format("%d", tagCount));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			txtTimes.setText(String.format("%d", tagTimes));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Runnable task = new Runnable() {

		@Override
		public void run() {
			api.startAutoRead(0x22, new byte[] { 0x00, 0x01 },
					new UHFHXAPI.AutoRead() {

						@Override
						public void timeout() {
							Log.i("whw", "timeout");
						}

						@Override
						public void start() {
							Log.i("whw", "start");
						}

						@Override
						public void processing(byte[] data) {
							String epc = DataUtils.toHexString(data).substring(
									4);
							hMsg.obtainMessage(MSG_SHOW_EPC_INFO, epc)
									.sendToTarget();
							Log.i("whw", "data=" + epc);
						}

						@Override
						public void end() {
							Log.i("whw", "end");
							Log.i("whw", "isStop="+isStop);
							if (!isStop) {
								pool.execute(task);
							} else {
								hMsg.sendEmptyMessage(INVENTORY_OVER);
							}
						}
					});
		}
	};
	
	private void showProgressDialog(String str) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(str);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();
	}

	private void cancleProgressDialog() 
	{
		if (null != progressDialog && progressDialog.isShowing()) 
		{
			progressDialog.cancel();
			progressDialog = null;
		}
	}

	@Override
	protected void onPause() {
		isStop = true;
		if (buttonInv.isChecked()) {
			buttonInv.setChecked(false);
			buttonInv.setClickable(true);
		}
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		SerialPortManager.getInstance().closeSerialPort(2);
		super.onDestroy();
	}
}