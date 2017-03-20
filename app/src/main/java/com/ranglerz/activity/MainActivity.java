package com.ranglerz.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.ranglerz.utils.DataUtils;
import com.ranglerz.utils.ToastUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	private GridView gridview;
	private Button set, check;
	private Dialog dialog;

	private String[] baudrateValue;
	private String[] keys;
	private int[] icons;

	private SharedPreferences preferences;

	private HashMap<String, Boolean> features = new HashMap<String, Boolean>();
	private ArrayList<Integer> indexList = new ArrayList<Integer>();
	private List<HashMap<String, Object>> menuList = new ArrayList<HashMap<String, Object>>();

	private SimpleAdapter menuAdapter;

	public void setOrientation() {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		int nowWidth = dm.widthPixels; // 当前分辨率 宽度
		int nowHeigth = dm.heightPixels; // 当前分辨率高度
		float density = dm.density; // 屏幕密度（像素比例：0.75/1.0/1.5/2.0）
		int densityDPI = dm.densityDpi; // 屏幕密度（每寸像素：120/160/240/320）
		Log.i("whw", "width=" + nowWidth + "   height=" + nowHeigth
				+ "  density=" + density + "  densityDPI=" + densityDPI);
		if (nowWidth == 1024 && (nowHeigth == 552 || nowHeigth == 600)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			Log.i("whw", "LANDSCAPE width=" + nowWidth + "   height="
					+ nowHeigth);
		} else if (nowWidth == 480 && nowHeigth == 800) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			Log.i("whw", "PORTRAIT width=" + nowWidth + "   height="
					+ nowHeigth);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i("whw", "onCreate  =" + this.toString());
		super.onCreate(savedInstanceState);
		setOrientation();
		setContentView(R.layout.main_activity);
		initView();
		initData();
	}

	private void initView() {
		// spinner = (Spinner) findViewById(R.id.baudrate_spinner);
		gridview = (GridView) findViewById(R.id.gridview);
		gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				showActivity(((TextView) view.findViewById(R.id.menu_name))
						.getText().toString());
				Log.i("whw", "onItemClick  position=" + position);

			}
		});

		set = (Button) findViewById(R.id.set_menu);
		set.setOnClickListener(this);

		check = (Button) findViewById(R.id.check_version);
		check.setOnClickListener(this);

		// spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
		//
		// @Override
		// public void onItemSelected(AdapterView<?> parent, View view,
		// int position, long id) {
		// Log.i("whw", "baudrateValue[position])="
		// + baudrateValue[position]);
		// SerialPortManager.getInstance().setBaudrate(
		// Integer.parseInt(baudrateValue[position]));
		// }
		//
		// @Override
		// public void onNothingSelected(AdapterView<?> parent) {
		//
		// }
		// });
	}

	private void initData() {
		baudrateValue = getResources().getStringArray(R.array.baudrates_value);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, baudrateValue);
		// spinner.setAdapter(adapter);
		keys = getResources().getStringArray(R.array.features);
		// if (isZh()) {
		icons = new int[] { R.drawable.fingerprint, R.drawable.barcode,
				R.drawable.hx, R.drawable.ic, R.drawable.cpu,
				R.drawable.printer, R.drawable.magstripecard };
		// } else {
		// icons = new int[] { R.drawable.fingerprint, R.drawable.barcode,
		// R.drawable.m1, R.drawable.rfid15693, R.drawable.hx,
		// R.drawable.hk, R.drawable.ic, R.drawable.barcode };
		// }
		preferences = getSharedPreferences("features", MODE_PRIVATE);
		getFeatures();
		showFeatures();
	}

	private void showActivity(String str) {
		int position = -1;
		for (int i = 0; i < keys.length; i++) {
			if (keys[i].equals(str)) {
				position = i;
				break;
			}
		}
		switch (position) {
		case 0:// 指纹
			startActivity(new Intent(this, FingerprintActivity.class));
			break;
		case 1:// 条码
			startActivity(new Intent(this, BarCodeActivity.class));
			break;
		case 2:// 超高频
			startActivity(new Intent(this, UHFActivity.class));
			break;
		case 3:// 接触
			DataUtils.isContactless = false;
			startActivity(new Intent(this, CardSelectActivity.class));
			break;
		case 4:// 非接触
			DataUtils.isContactless = true;
			startActivity(new Intent(this, CardSelectActivity.class));
			break;
		case 5:// 打印机
			startActivity(new Intent(this, PrinterActivity.class));
			break;
		case 6:// 磁条卡
			startActivity(new Intent(this, MagStripeCardActivity.class));
			break;
		default:
			break;
		}
	}

	private void setMenuValue() {
		Editor editor = preferences.edit();
		for (int i = 0; i < keys.length; i++) {
			editor.putBoolean(keys[i], features.get(keys[i]));
		}
		editor.commit();
	}

	private void getFeatures() {
		checkLanguageChanage();
		for (int i = 0; i < keys.length; i++) {
			features.put(keys[i], preferences.getBoolean(keys[i], false));
		}

		indexList.clear();
		for (String key : features.keySet()) {
			Log.i("whw", key + "=" + features.get(key));
			if (features.get(key)) {
				for (int i = 0; i < keys.length; i++) {
					if (keys[i].equals(key)) {
						indexList.add(i);
						break;
					}
				}
			}
		}
		Collections.sort(indexList);
		Log.i("whw", "indexList=" + indexList.size());
		menuList.clear();
		for (Integer value : indexList) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("menuIcon", icons[value]);
			map.put("menuName", keys[value]);
			menuList.add(map);
		}

	}

	private void showFeatures() {
		menuAdapter = new SimpleAdapter(this, menuList, R.layout.menu_item,
				new String[] { "menuIcon", "menuName" }, new int[] {
						R.id.menu_icon, R.id.menu_name });
		gridview.setAdapter(menuAdapter);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.set_menu:
			showDialog();
			break;
		case R.id.check_version:
			startActivity(new Intent(getApplicationContext(),
					GetVersionActivity.class));
			break;
		default:
			break;
		}

	}

	/**
	 * 展示功能对话框
	 */
	private void showDialog() {
		dialog = new Dialog(this, R.style.MyDialog);
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.menu_dialog, null);
		ListView listView = (ListView) view.findViewById(R.id.menu_list);
		MyAdapter adapter = new MyAdapter(this);
		adapter.setString(keys);
		listView.setAdapter(adapter);
		Button apply = (Button) view.findViewById(R.id.apply_dialog);
		apply.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setMenuValue();
				getFeatures();
				showFeatures();
				dialog.cancel();
			}
		});
		Button cancle = (Button) view.findViewById(R.id.cancle_dialog);
		cancle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});
		dialog.setContentView(view);
		dialog.show();
	}

	private class MyAdapter extends BaseAdapter {
		private String[] strs;
		private LayoutInflater mInflater;

		public MyAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		public void setString(String[] strs) {
			this.strs = strs;
		}

		@Override
		public int getCount() {
			if (strs == null) {
				return 0;
			}
			return strs.length;
		}

		@Override
		public Object getItem(int position) {
			return strs[position];
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater
						.inflate(R.layout.menu_dialog_item, null);
				holder = new ViewHolder();
				holder.name = (TextView) convertView
						.findViewById(R.id.feature_name);
				holder.checkBox = (CheckBox) convertView
						.findViewById(R.id.check_box);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.name.setText(strs[position]);
			holder.checkBox.setOnCheckedChangeListener(null);
			if (holder.checkBox.isChecked() != features.get(strs[position])) {
				holder.checkBox.setChecked(features.get(strs[position]));
			}
			Log.i("whw",
					"features.get(strs[position])="
							+ features.get(strs[position]) + "   "
							+ strs[position]);
			holder.checkBox
					.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							Log.i("whw", "strs[position]=" + isChecked + "   "
									+ strs[position]);
							features.put(strs[position], isChecked);
						}
					});
			return convertView;
		}

		public final class ViewHolder {
			public TextView name;
			public CheckBox checkBox;
		}

	}

	private long lastBackTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			long currentBackTime = System.currentTimeMillis();
			if (currentBackTime - lastBackTime > 2000) {
				ToastUtil.showToast(this, R.string.exit_toast);
				lastBackTime = currentBackTime;
			} else {
				android.os.Process.killProcess(android.os.Process.myPid());
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onRestart() {
		Log.i("whw", "onRestart  =" + this.toString());
		super.onRestart();
	}

	@Override
	protected void onResume() {
		Log.i("whw", "onResume  =" + this.toString());
		super.onResume();
	}

	@Override
	protected void onStart() {
		Log.i("whw", "onStart  =" + this.toString());
		super.onStart();
	}

	@Override
	protected void onStop() {
		Log.i("whw", "onStop  =" + this.toString());
		super.onStop();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private boolean isZh() {
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		if (language.endsWith("zh"))
			return true;
		else
			return false;
	}

	private void checkLanguageChanage() {
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		String str = preferences.getString("language", "");
		if (!language.equals(str)) {
			preferences.edit().clear().putString("language", language).commit();
		}
	}
}