package com.ranglerz.activity;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.hiklife.rfidapi.*;

public class HKUHFActivity extends BaseUHFActivity {

	public RadioCtrl myRadio;

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
				InventoryEvent info = (InventoryEvent) msg.obj;
				ShowEPC(info.GetFlagID());
				break;

			case MSG_DISMISS_CONNECT_WAIT_SHOW:
				prgDlg.dismiss();
				int returnValue = (Integer) msg.obj;
				switch (returnValue) {
				case 0:
					Toast.makeText(activity,
							activity.getText(R.string.info_connect_success),
							Toast.LENGTH_SHORT).show();

					break;

				case -1:
					Toast.makeText(activity,
							activity.getText(R.string.info_connect_fail),
							Toast.LENGTH_SHORT).show();
					break;

				case -2:
					Toast.makeText(activity,
							activity.getText(R.string.info_antenna_fail),
							Toast.LENGTH_SHORT).show();
					break;
				}

				break;
			case INFO_INV_SUCCESS:
				Toast.makeText(HKUHFActivity.this,
						getText(R.string.info_inv_success), Toast.LENGTH_SHORT)
						.show();
				break;
			case INFO_INV_FAIL:
				Toast.makeText(HKUHFActivity.this,
						getText(R.string.info_inv_fail), Toast.LENGTH_SHORT)
						.show();
				break;
			case INFO_STOPINV_SUCCESS:
				Toast.makeText(HKUHFActivity.this,
						getText(R.string.info_stopinv_success),
						Toast.LENGTH_SHORT).show();
				break;
			case INFO_STOPINV_FAIL:
				Toast.makeText(HKUHFActivity.this,
						getText(R.string.info_stopinv_fail), Toast.LENGTH_SHORT)
						.show();
				break;
			case INFO_DISCONNECT_SUCCESS:
				buttonInv.setClickable(false);
				Toast.makeText(HKUHFActivity.this,
						getText(R.string.info_disconnect_success),
						Toast.LENGTH_SHORT).show();
				break;
			case INFO_DISCONNECT_FAIL:
				Toast.makeText(HKUHFActivity.this,
						getText(R.string.info_disconnect_fail),
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	private Handler hMsg = new StartHander(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_uhf);
		myRadio = new RadioCtrl();
		myRadio.setInventoryEventListener(new OnInventoryEventListener() {
			public void RadioInventory(InventoryEvent event) {
				Message msg = new Message();
				msg.obj = event;
				msg.what = MSG_SHOW_EPC_INFO;
				hMsg.sendMessage(msg);
			}
		});

		// 获取后续需要经常操作的窗口控件
		txtCount = (TextView) findViewById(R.id.txtCount);
		txtTimes = (TextView) findViewById(R.id.txtTimes);
		setting  = (Button)findViewById(R.id.setting_params);
		setting.setVisibility(View.GONE);
		buttonConnect = (ToggleButton) findViewById(R.id.togBtn_open);
		buttonInv = (ToggleButton) findViewById(R.id.togBtn_inv);
		final FragmentManager fragmentManager = getFragmentManager();
		objFragment = (TaglistFragment) fragmentManager
				.findFragmentById(R.id.fragment_taglist);

		buttonConnect.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				buttonConnect.setClickable(false);
				if (isChecked) {
					if (prgDlg != null) {
						prgDlg.show();
					} else {
						prgDlg = ProgressDialog.show(HKUHFActivity.this,
								getResources().getString(R.string.str_conn), getResources().getString(R.string.str_do_conn), true, false);
					}

					// 开启一个线程来处理RFID设备的连接
					new Thread() {
						@Override
						public void run() {
							Message closemsg = new Message();
							closemsg.obj = (Object) connectRadio();
							;
							closemsg.what = MSG_DISMISS_CONNECT_WAIT_SHOW;
							hMsg.sendMessage(closemsg);
						}
					}.start();
				} else {
					if(!isOnPause){
						pool.execute(new Runnable() {
							@Override
							public void run() {
								DisconnectRadio();
							}
						});
					}
				}

				buttonConnect.setClickable(true);
			}
		});

		// connectRadio();

		buttonInv.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				buttonInv.setClickable(false);
				if (isChecked) {
					Inv();
				} else {
					if(!isOnPause){
						stop();
					}
				}

				buttonInv.setClickable(true);
			}
		});

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
	 * 连接RFID模块
	 */
	public int connectRadio() {
		int returnValue = 0;
		try {
			if (myRadio.ConnectRadio() == ctrlOperateResult.OK) {
				// 配置天线参数
				AntennaPortConfiguration config = new AntennaPortConfiguration();
				config.dwellTime = 200;
				config.numberInventoryCycles = 8192;
				config.powerLevel = 300;

				// 配置单化算法为动态Q值，以及通话选择为Session1，翻转关闭减少标签的上报次数
				SingulationAlgorithmParms parm = new SingulationAlgorithmParms();
				parm.toggleTarget = 0;
				parm.maxQValue = 15;
				parm.minQValue = 0;
				parm.qValue = 7;
				parm.startQValue = 7;
				parm.repeatUntilNoTags = 0;
				parm.thresholdMultiplier = 4;
				parm.retryCount = 0;
				parm.singulationAlgorithmType = SingulationAlgorithm.DYNAMICQ;
				try {
					if (myRadio.SetAntennaPortConfiguration(0, config) == ctrlOperateResult.OK
							&& myRadio.SetCurrentLinkProfile(1) == ctrlOperateResult.OK
							&& myRadio.SetTagGroupSession(Session.S1) == ctrlOperateResult.OK
							&& myRadio.SetCurrentSingulationAlgorithm(parm) == ctrlOperateResult.OK) {
						buttonInv.setClickable(true);
						returnValue = 0;
					} else {
						returnValue = -2;
					}
				} catch (radioBusyException e) {
					returnValue = -2;
				}
			} else {
				returnValue = -1;
			}

		} catch (radioBusyException e) {
			e.printStackTrace();
		}

		return returnValue;
	}

	/**
	 * 断开RFID模块的连接
	 */
	public void DisconnectRadio() {
		try {
			if (myRadio.DisconnectRadio() == ctrlOperateResult.OK) {
				buttonInv.setClickable(false);
				hMsg.sendEmptyMessage(INFO_DISCONNECT_SUCCESS);
			} else {
				hMsg.sendEmptyMessage(INFO_DISCONNECT_FAIL);
			}
		} catch (radioBusyException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 开启盘点操作
	 */
	public void Inv() {
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
		pool.execute(new Runnable() {

			@Override
			public void run() {
				// 对于手持机只能使用周期性盘点，不然cpu资源将被耗尽
				try {
					if (myRadio.StartInventory(0, 200) == ctrlOperateResult.OK) {
						buttonConnect.setClickable(false);
						hMsg.sendEmptyMessage(INFO_INV_SUCCESS);
					} else {
						hMsg.sendEmptyMessage(INFO_INV_FAIL);
					}
				} catch (radioBusyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
	}

	/**
	 * 停止盘点操作
	 */
	public void stop() {
		Log.i("whw", "stop()$$$$$$$$$$$$$$$");
		buttonConnect.setClickable(true);
		// pool.execute(new Runnable() {
		//
		// @Override
		// public void run() {
		if (myRadio.StopInventory() == ctrlOperateResult.OK) {
			hMsg.sendEmptyMessage(INFO_STOPINV_SUCCESS);
		} else {
			hMsg.sendEmptyMessage(INFO_STOPINV_FAIL);
		}

		// }
		// });

	}

	@Override
	protected void onResume() {
		super.onResume();
		isOnPause = false;
	}

	private boolean isOnPause;
	@Override
	protected void onPause() {
		Log.i("whw", "onPause()$$$$$$$$$$$$$$$");
		isOnPause = true;
		if (buttonInv.isChecked()) {
			buttonInv.setChecked(false);
			stop();
		}
		if (buttonConnect.isChecked()) {
			buttonConnect.setChecked(false);
			while(myRadio.isBusy()){
				SystemClock.sleep(10);
			}
			try {
				myRadio.DisconnectRadio();
			} catch (radioBusyException e) {
				e.printStackTrace();
				Log.i("whw", "DisconnectRadio   radioBusyException%%%%%%%%%");
			}
		}
		SystemClock.sleep(1000);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

}
