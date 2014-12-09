package com.alcatel.master.networkmonitor.ui;

import com.alcatel.master.networkmonitor.R;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class SettingAutoAdjustContent extends Activity implements OnClickListener {	
	
	int m_AutoAdjustPeriodIndex = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_auto_adjust_content_page);
		initView();
	}	
	
	protected void initView(){
		final CharSequence[] items = {
				getResources().getString(R.string.setting_auto_adjust_period_3),
				getResources().getString(R.string.setting_auto_adjust_period_week),
				getResources().getString(R.string.setting_auto_adjust_period_everyday),
				getResources().getString(R.string.setting_auto_adjust_period_close)}; 
		TextView periodLable = (TextView)findViewById(R.id.setting_auto_adjust_days_value);
		periodLable.setText(items[m_AutoAdjustPeriodIndex]);
		
		View daysAutoAdjustView = findViewById(R.id.setting_auto_adjust_days_panel);
		daysAutoAdjustView.setOnClickListener(this);
		
		View smsAutoAdjustView = findViewById(R.id.setting_auto_adjust_sms_panel);
		smsAutoAdjustView.setOnClickListener(this);
	}
	
	protected void showDaysAutoAdjust(){		
		final CharSequence[] items = {
				getResources().getString(R.string.setting_auto_adjust_period_3),
				getResources().getString(R.string.setting_auto_adjust_period_week),
				getResources().getString(R.string.setting_auto_adjust_period_everyday),
				getResources().getString(R.string.setting_auto_adjust_period_close)}; 
		AlertDialogCustom.Builder builder = new AlertDialogCustom.Builder(this);
		builder.setTitle(R.string.setting_altert_auto_adjust_period_title);
		
		builder.setSingleSelectosItems(items,(int)m_AutoAdjustPeriodIndex,new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int item) {  							
				m_AutoAdjustPeriodIndex = item;
				TextView periodLable = (TextView)findViewById(R.id.setting_auto_adjust_days_value);
				periodLable.setText(items[item]);
				dialog.dismiss();
		   }  
		}); 
						
		builder.setNegativeButton(R.string.common_button_cancel, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//TODO Auto-generated method stub				
				dialog.cancel();
			}
		});
		
		AlertDialogCustom dialog = builder.create();		
		dialog.setOwnerActivity(this);
		dialog.show(); 
	}	
	
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.setting_auto_adjust_days_panel:
			showDaysAutoAdjust();
			break;
		case R.id.setting_auto_adjust_sms_panel:
			break;
		}
	}
}
