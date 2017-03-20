/**   
* @Title: CardSelectActivity.java
* @Package com.authentication.activity
* @Description: TODO
* @author Weishun.Xu   
* @date 2016年3月24日 下午1:27:44
* @version V1.0   
*/
package com.ranglerz.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 *选择卡类型
 */
public class CardSelectActivity extends Activity implements OnClickListener{
	private Button btnCardBank;
	private Button btnCardOrder;
	private Button btnCardProg;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		Log.i("cy", "Enter function CardSelectActivity-onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cardselect);
		initView();
	}
	
	private void initView() {
		this.btnCardBank = (Button)findViewById(R.id.CardBank_ICCard);
		this.btnCardBank.setOnClickListener(this);
		
		this.btnCardOrder = (Button)findViewById(R.id.CardOrder_ICCard);
		this.btnCardOrder.setOnClickListener(this);
		
		this.btnCardProg = (Button)findViewById(R.id.CardProg_ICCard);
		this.btnCardProg.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.CardBank_ICCard://银行卡
			startActivity(new Intent(this,IcCardBankActivity.class));
			break;
		case R.id.CardOrder_ICCard://定制卡
			startActivity(new Intent(this, IcCardOrderActivity.class));
			break;
		case R.id.CardProg_ICCard://工程卡
			startActivity(new Intent(this,IcCardProgActivity.class));
			break;
		default:
			break;
		}
	}
}