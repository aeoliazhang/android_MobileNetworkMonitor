package com.alcatel.master.networkmonitor.ui;

import com.alcatel.master.networkmonitor.common.MonitorService;
import com.alcatel.master.networkmonitor.common.NetworkMonitorConfig;
import com.alcatel.master.networkmonitor.R;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.graphics.Rect;

public class Setting extends Activity implements OnClickListener{	
/******monitor setting file name start*******/
	/*private String m_fileServiceOn = "ServiceOn";
	private String m_fileMonthPackage = "MonthPackage";
	private String m_fileStartDay = "StartDay";
	private String m_fileExceedOn = "ExceedOn";
	private String m_fileMonthWarn = "MonthWar";
	private String m_fileDayWarn = "DayWarn";*/	
	
	private MonitorService m_monitorService;
	
/******monitor setting data start******/
	private String m_strOn = "1";
	private String m_strOff = "0";
	
/******float view start******/
	private WindowManager m_windowManager;  
	private WindowManager.LayoutParams m_wmParam;  
	//private NetworkFloatView m_floatLayout = ((NetworkMonitorApp)getApplication()).m_floatLayout; 
	
	private Button m_ServiceOnBtn;
	private Button m_ExceedOnBtn;
	private LayoutInflater m_Inflater;	
	private View m_MonthPackageAlertInputView;	
	private View m_dayPromptAlertInputView;
	private View m_monthSurplusAlertView;
	
	ServiceConnection m_serviceConnect = new ServiceConnection(){
		public void onServiceConnected(ComponentName name, IBinder binder) {			
			m_monitorService = ((MonitorService.MyServiceBinder)binder).getService();			
		}
    	
    	public void onServiceDisconnected(ComponentName name){			
		} 
	};
	
	public int dipToPx(float dipValue){
		int pxValue = (int)(dipValue * this.getResources().getDisplayMetrics().density + 0.5f); 
		return pxValue;
	}
	
	public int pxToDip(float pxValue){
		int dipValue = (int)(pxValue/this.getResources().getDisplayMetrics().density + 0.5f);
		return dipValue;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);	
		m_ServiceOnBtn = (Button)findViewById(R.id.service_switchbtn);
		m_ExceedOnBtn = (Button)findViewById(R.id.exceed_prompt_switchbtn);		
		m_Inflater = LayoutInflater.from(this);
				
		initMonitorSettingState();		
		initView();	
		
		m_windowManager = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		
		bindService(new Intent(getApplicationContext(),MonitorService.class)
		,m_serviceConnect,BIND_AUTO_CREATE);
	}
	
	@Override  
	public void onDestroy(){  
		super.onDestroy();  	 
		//m_windowManager.removeView(m_floatLayout);  
	} 
	
	protected boolean strToBoolean(String onOff){
		boolean bOn = false;
		if(onOff.equals(m_strOn)){
			bOn = true;
		}
		return bOn;
	}	
	
	protected void initMonitorSettingState(){
		//m_bServiceOn = getServiceOnState();
		//m_nMonthPackageValue = getMonthPakcage();		
		//m_nStartDay = getStartDay();
		//m_bExceedOn = getExceedState();
		//m_nDayPromptValue = getDayPromptValue();
		//m_nMonthSurplusValue = getMonthSurplusValue();
		
		boolean bServiceOn = NetworkMonitorConfig.getInstance(getApplicationContext()).getServiceOnState();
		int nMonthPackageValue = NetworkMonitorConfig.getInstance(getApplicationContext()).getMonthPackage();
		int nStartDay = NetworkMonitorConfig.getInstance(getApplicationContext()).getStartDay();
		boolean bExceedOn = NetworkMonitorConfig.getInstance(getApplicationContext()).getExceedOnState();
		int nDayPromptValue = NetworkMonitorConfig.getInstance(getApplicationContext()).getDayWarnValue();
		int nMonthSurplusValue = NetworkMonitorConfig.getInstance(getApplicationContext()).getMonthWarnValue();
		setServiceSwitchState(bServiceOn);
		setExceedSwitchState(bExceedOn);
		
		TextView monthPackageValueLabel = (TextView)findViewById(R.id.month_packge_value);
		if(nMonthPackageValue <= 0)
			monthPackageValueLabel.setText(R.string.not_setting);
		else
			monthPackageValueLabel.setText(String.valueOf(nMonthPackageValue) + "M");					
		
		TextView startDayValueLabel = (TextView)findViewById(R.id.start_day_value);
		if(nStartDay <= 0)
			startDayValueLabel.setText(R.string.not_setting);
		else
			startDayValueLabel.setText(String.valueOf(nStartDay) + "ÈÕ");
			
		TextView monthSurplusValueLabel = (TextView)findViewById(R.id.month_surplus_warning_value);
		if(nMonthSurplusValue <= 0)
			monthSurplusValueLabel.setText(R.string.not_setting);
		else
			monthSurplusValueLabel.setText(String.valueOf(nMonthSurplusValue) + " M");
			
		TextView dayPromptValueLabel = (TextView)findViewById(R.id.day_volume_prompt_value);
		if(nDayPromptValue <= 0)
			dayPromptValueLabel.setText(R.string.not_setting);
		else
			dayPromptValueLabel.setText(String.valueOf(nDayPromptValue) + " M");
	}	

	protected void initView(){
		
		View itemServiceSwitchBtn = findViewById(R.id.service_switchbtn);
		itemServiceSwitchBtn.setOnClickListener(this);
			
		View itemExceedSwitchBtn = findViewById(R.id.exceed_prompt_switchbtn);
		itemExceedSwitchBtn.setOnClickListener(this);			
		
		View itemServiceTrigger = findViewById(R.id.layout_setting_service_trigger);
		itemServiceTrigger.setOnClickListener(this);
		
		View itemMonthPackage = findViewById(R.id.layout_setting_month_packgage);
		itemMonthPackage.setOnClickListener(this);
		
		View itemStartDay = findViewById(R.id.layout_setting_start_day);
		itemStartDay.setOnClickListener(this);
		
		//View itemAutoAdjust = findViewById(R.id.layout_setting_auto_adjust);
		//itemAutoAdjust.setOnClickListener(this);
		
		View itemFloatWin = findViewById(R.id.layout_setting_float_window);
		itemFloatWin.setOnClickListener(this);
		
		View itemExceedPrompt = findViewById(R.id.layout_setting_exceed_prompt);
		itemExceedPrompt.setOnClickListener(this);
		
		View itemMonthSurplus = findViewById(R.id.layout_setting_month_surplus_warning);
		itemMonthSurplus.setOnClickListener(this);
		
		View itemDayPrompt = findViewById(R.id.layout_setting_day_volum_prompt);
		itemDayPrompt.setOnClickListener(this);
		
		View itemClearUsage = findViewById(R.id.layout_setting_clear_usage);
		itemClearUsage.setOnClickListener(this);
	}	
	
/*****************Service trigger control start********************/	
	protected void showTriggerOpPrompt(){
		AlertDialogCustom.Builder builder = new AlertDialogCustom.Builder(this);		
		builder.setTitle(R.string.setting_altert_trigger_title);
		builder.setMessage(R.string.setting_altert_trigger_service_close);
		builder.setNeutralButton(R.string.common_button_confirm, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {			
				boolean bServiceOn = NetworkMonitorConfig.getInstance(getApplicationContext()).getServiceOnState();
				setServiceSwitchState(!bServiceOn);	
				if(bServiceOn == true)
					setExceedSwitchState(false);
				dialog.dismiss();
			}
		});
		builder.setPositiveButton(R.string.common_button_cancel, new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int id) {  
				dialog.cancel();  
			}  
		});  

		AlertDialogCustom dialog = builder.create();
		dialog.setOwnerActivity(this);
		dialog.show();
	}
	
	protected void setServiceSwitchState(boolean bServiceOn){
		if(bServiceOn)
			m_ServiceOnBtn.setBackgroundResource(R.drawable.switch_on_normal);		
		else
			m_ServiceOnBtn.setBackgroundResource(R.drawable.switch_off_normal);
		NetworkMonitorConfig.getInstance(getApplicationContext()).setServiceOnState(bServiceOn);
		//changeServiceTriggerState(bServiceOn);
	}
	
	/*protected void changeServiceTriggerState(boolean bServiceOn){
		FileOperation fileOp = new FileOperation(this);
		m_bServiceOn = bServiceOn;
		if(bServiceOn)
			fileOp.SaveFileContent(m_fileServiceOn,m_strOn);
		else
			fileOp.SaveFileContent(m_fileServiceOn,m_strOff);
	}*/	

/*****************Month package setting start********************/
	protected void showMonthPackageSettingAlert(){
		AlertDialogCustom.Builder builder = new AlertDialogCustom.Builder(this);
		builder.setTitle(R.string.setting_label_month_package);
		m_MonthPackageAlertInputView = m_Inflater.inflate(R.layout.alert_dialog_custom_inputedit_view, null);			
		TextView inputViewTitle = (TextView)m_MonthPackageAlertInputView.findViewById(R.id.InputViewTitle);
		inputViewTitle.setText(R.string.setting_altert_month_package_message);
		TextView rightText = (TextView)m_MonthPackageAlertInputView.findViewById(R.id.text_right);
		rightText.setText(R.string.volume_unit_M);			
		EditText view = (EditText)m_MonthPackageAlertInputView.findViewById(R.id.InputEdit);
		
		int nMonthPackageValue = NetworkMonitorConfig.getInstance(getApplicationContext()).getMonthPackage();
		if(nMonthPackageValue == -1)
			view.setText("");
		else
			view.setText(String.valueOf(nMonthPackageValue));
				
		builder.setContentView(m_MonthPackageAlertInputView);
		builder.setNeutralButton(R.string.common_button_confirm, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				EditText view = (EditText)m_MonthPackageAlertInputView.findViewById(R.id.InputEdit);								
				String strValue = view.getText().toString();
				setMonthPackage(strValue);
				dialog.dismiss();
			}
		});
		
		builder.setPositiveButton(R.string.common_button_cancel, new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int id) {  
				dialog.cancel();  
			}  
		});  

		AlertDialogCustom dialog = builder.create();
		dialog.setOwnerActivity(this);
		dialog.show();
	}
	
	protected void setMonthPackage(String strValue){		
		TextView monthPackageValueLable = (TextView)findViewById(R.id.month_packge_value);
		String str;	
		int nMonthPackageValue = -1;
		if(strValue.length() == 0){
			str = getString(R.string.not_setting);			
			setExceedSwitchState(false);
		}
		else{	
			nMonthPackageValue = Integer.parseInt(strValue);
			if(nMonthPackageValue <= 0){				
				str = getString(R.string.not_setting);				
				setExceedSwitchState(false);
			}						
			else{
				str = strValue + "M";
				/*
				if(nMonthPackageValue == 0){					
					setMonthSurplus(0);					
					setDayPrompt(0);					
				}
				*/				
				setExceedSwitchState(true);
			}
		}
		/*FileOperation fileOp = new FileOperation(this);
		fileOp.SaveFileContent(m_fileMonthPackage, strValue);*/
		NetworkMonitorConfig.getInstance(getApplicationContext()).setMonthPackage(nMonthPackageValue);
		monthPackageValueLable.setText(str);
	}
	
/*****************Start day setting start********************/	
	protected void showDaySelectorAlert(){
		final CharSequence[] items = {
				getResources().getString(R.string.setting_start_day_1),getResources().getString(R.string.setting_start_day_2),
				getResources().getString(R.string.setting_start_day_3),getResources().getString(R.string.setting_start_day_4),
				getResources().getString(R.string.setting_start_day_5),getResources().getString(R.string.setting_start_day_6),
				getResources().getString(R.string.setting_start_day_7),getResources().getString(R.string.setting_start_day_8),
				getResources().getString(R.string.setting_start_day_9),getResources().getString(R.string.setting_start_day_10),
				getResources().getString(R.string.setting_start_day_11),getResources().getString(R.string.setting_start_day_12),
				getResources().getString(R.string.setting_start_day_13),getResources().getString(R.string.setting_start_day_14),
				getResources().getString(R.string.setting_start_day_15),getResources().getString(R.string.setting_start_day_16),
				getResources().getString(R.string.setting_start_day_17),getResources().getString(R.string.setting_start_day_18),
				getResources().getString(R.string.setting_start_day_19),getResources().getString(R.string.setting_start_day_20),
				getResources().getString(R.string.setting_start_day_21),getResources().getString(R.string.setting_start_day_22),
				getResources().getString(R.string.setting_start_day_23),getResources().getString(R.string.setting_start_day_24),
				getResources().getString(R.string.setting_start_day_25),getResources().getString(R.string.setting_start_day_26),
				getResources().getString(R.string.setting_start_day_27),getResources().getString(R.string.setting_start_day_28),
				getResources().getString(R.string.setting_start_day_29),getResources().getString(R.string.setting_start_day_30),
				getResources().getString(R.string.setting_start_day_31)}; 
		AlertDialogCustom.Builder builder = new AlertDialogCustom.Builder(this);
		builder.setTitle(R.string.setting_label_start_day);
		
		int nStartDay = NetworkMonitorConfig.getInstance(getApplicationContext()).getStartDay();
		builder.setSingleSelectosItems(items,nStartDay - 1,new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int item) {
				setStartDay(items[item],item + 1);
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
		WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
		int nScreenWidth = this.getResources().getDisplayMetrics().widthPixels;
		int nScreenHeight = this.getResources().getDisplayMetrics().heightPixels;		
		params.height = nScreenHeight*3/4;		
		if(nScreenHeight > nScreenWidth){
			params.width = nScreenWidth - 20;
		}
		else{
			params.width = nScreenWidth*4/5;
		}			
		dialog.getWindow().setAttributes(params);		
		dialog.setOwnerActivity(this);
		dialog.show(); 
	}
	
	protected void setStartDay(CharSequence dayValue,int nStartDay){
		TextView startValueLable = (TextView)findViewById(R.id.start_day_value);				
		startValueLable.setText(dayValue);
		
		/*FileOperation fileOp = new FileOperation(this);
		fileOp.SaveFileContent(m_fileStartDay,String.valueOf(m_nStartDay));*/
		int nPreStartDay = NetworkMonitorConfig.getInstance(getApplicationContext()).getStartDay();
		NetworkMonitorConfig.getInstance(getApplicationContext()).setStartDay(nStartDay);
		if(nPreStartDay != nStartDay) {
			NetworkMonitorConfig.getInstance(getApplicationContext()).clearManualAdjustFlux();
		}
	}

/*****************Auto adjust period setting start********************/
	protected void showAutoAdjustActivity(){
		//Intent intentAutoAdjustContent = new Intent(this,SettingAutoAdjustContent.class);
		//startActivity(intentAutoAdjustContent);
	}

/*****************Float View setting Start********************/	
	protected void showFloatViewSetting(){		
		
		((NetworkMonitorApp)getApplication()).m_floatLayout
			= new NetworkFloatView(getApplicationContext(),((NetworkMonitorApp)getApplication()).m_activityMain);
		//((NetworkMonitorApp)getApplication()).m_floatLayout.setBackgroundResource(R.drawable.ic_launcher);
		m_windowManager=(WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);  
		m_wmParam = ((NetworkMonitorApp)getApplication()).getNetAppWm();  
 
		m_wmParam.type = WindowManager.LayoutParams.TYPE_PHONE;
		m_wmParam.format = PixelFormat.RGBA_8888;		
		m_wmParam.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		m_wmParam.flags = m_wmParam.flags | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;  
		m_wmParam.flags = m_wmParam.flags | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;       		       
		m_wmParam.gravity=Gravity.LEFT|Gravity.TOP;
		m_wmParam.x=0;  
		m_wmParam.y=0;  
         
		m_wmParam.width=200;  
		m_wmParam.height=40;
		Rect frame = new Rect();  
		this.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);		
		((NetworkMonitorApp)getApplication()).m_statusHeight = frame.top;;
				
		m_windowManager.addView(((NetworkMonitorApp)getApplication()).m_floatLayout, m_wmParam);	
	}
	
/*****************Exceed state setting start********************/	
	protected void onExceedStateChange(boolean bExceedOn){		
		setExceedSwitchState(bExceedOn);		
	}
	
	protected void setExceedSwitchState(boolean bExceedOn){
		/*FileOperation fileOp = new FileOperation(this);
		if(m_bExceedOn)
			fileOp.SaveFileContent(m_fileExceedOn, m_strOn);
		else
			fileOp.SaveFileContent(m_fileExceedOn, m_strOff);*/
		NetworkMonitorConfig.getInstance(getApplicationContext()).setExceedOnState(bExceedOn);
			
		if(bExceedOn)
			m_ExceedOnBtn.setBackgroundResource(R.drawable.switch_on_normal);		
		else
			m_ExceedOnBtn.setBackgroundResource(R.drawable.switch_off_normal);
		
		View monthSurplusView = findViewById(R.id.layout_setting_month_surplus_warning);		
		monthSurplusView.setEnabled(bExceedOn);
		View monthSurpluslabel = findViewById(R.id.month_surplus_warning_label);
		monthSurpluslabel.setEnabled(bExceedOn);
		View monthSurplusValue = findViewById(R.id.month_surplus_warning_value);
		monthSurplusValue.setEnabled(bExceedOn);
		
		View dayVolumePromptView = findViewById(R.id.layout_setting_day_volum_prompt);
		dayVolumePromptView.setEnabled(bExceedOn);	
		View dayVolumePromptlabel = findViewById(R.id.day_volume_prompt_label);
		dayVolumePromptlabel.setEnabled(bExceedOn);
		View dayVolumePromptValue = findViewById(R.id.day_volume_prompt_value);
		dayVolumePromptValue.setEnabled(bExceedOn);
	}
	
/*****************Month surplus setting start********************/	
	protected void showMonthSurplusAlert(){
		AlertDialogCustom.Builder builder = new AlertDialogCustom.Builder(this);
		builder.setTitle(R.string.setting_label_month_surplus_warning);
		m_monthSurplusAlertView = m_Inflater.inflate(R.layout.alert_dialog_custom_seekbar_view, null);
		SeekBar barView = (SeekBar)m_monthSurplusAlertView.findViewById(R.id.alert_seekview_seekbar);
		initSeekBarAlert(barView);		
		barView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
				// TODO Auto-generated method stub
				TextView seekBarValue = (TextView)m_monthSurplusAlertView.findViewById(R.id.alert_seekview_value);
				int nMax = seekBar.getMax();
				int percent = (progress*100)/nMax;
				String thumbValue=String.valueOf(percent) + "%" + "(" + String.valueOf(progress) + " M)";
				seekBarValue.setText(thumbValue);
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}					
		});		
		
		builder.setContentView(m_monthSurplusAlertView);
		builder.setNeutralButton(R.string.common_button_confirm, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				SeekBar view = (SeekBar)m_monthSurplusAlertView.findViewById(R.id.alert_seekview_seekbar);								
				int value = view.getProgress();
				setMonthSurplus(value);
				dialog.dismiss();
			}
		});
		
		builder.setPositiveButton(R.string.common_button_cancel, new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int id) {  
				dialog.cancel();  
			}  
		});  

		AlertDialogCustom dialog = builder.create();
		dialog.setOwnerActivity(this);
		dialog.show();
	}
	
	protected void initSeekBarAlert(SeekBar barView){				
		TextView seekbarMessage = (TextView)m_monthSurplusAlertView.findViewById(R.id.alert_seekview_message);
		seekbarMessage.setText(R.string.setting_altert_month_surplus_message);				
		int nMonthPackageValue = NetworkMonitorConfig.getInstance(getApplicationContext()).getMonthPackage();
		if(nMonthPackageValue != -1)
			barView.setMax(nMonthPackageValue);
		else
			barView.setMax(100);
		
		int nMonthSurplusValue = NetworkMonitorConfig.getInstance(getApplicationContext()).getMonthWarnValue();
		barView.setProgress(nMonthSurplusValue);
		TextView seekBarValue = (TextView)m_monthSurplusAlertView.findViewById(R.id.alert_seekview_value);
		int percent = (nMonthSurplusValue*100)/barView.getMax();
		String thumbValue=String.valueOf(percent) + "%" + "(" + String.valueOf(nMonthSurplusValue) + " M)";
		seekBarValue.setText(thumbValue);
	}
	
	protected void setMonthSurplus(int surplusvalue){
		/*FileOperation fileOp = new FileOperation(this);
		fileOp.SaveFileContent(m_fileMonthWarn, String.valueOf(m_nMonthSurplusValue));*/
		NetworkMonitorConfig.getInstance(getApplicationContext()).setMonthWarnValue(surplusvalue);
		
		TextView monthSurplusValueLabel = (TextView)findViewById(R.id.month_surplus_warning_value);
		if(surplusvalue <= 0)
			monthSurplusValueLabel.setText(R.string.not_setting);
		else{
			String thumbValue;		
			thumbValue=String.valueOf(surplusvalue) + " M";
			monthSurplusValueLabel.setText(thumbValue);
		}		
	}
	
/*****************Day use volume prompt setting start********************/	
	protected void showDayPromptAlert(){
		AlertDialogCustom.Builder builder = new AlertDialogCustom.Builder(this);
		builder.setTitle(R.string.setting_label_day_prompt);
		m_dayPromptAlertInputView = m_Inflater.inflate(R.layout.alert_dialog_custom_inputedit_view, null);			
		
		TextView inputViewTitle = (TextView)m_dayPromptAlertInputView.findViewById(R.id.InputViewTitle);
		inputViewTitle.setText(R.string.setting_altert_day_prompt_message);
		
		TextView rightText = (TextView)m_dayPromptAlertInputView.findViewById(R.id.text_right);
		rightText.setText(R.string.volume_unit_M);
		
		int nDayPromptValue = NetworkMonitorConfig.getInstance(getApplicationContext()).getDayWarnValue();
		EditText view = (EditText)m_dayPromptAlertInputView.findViewById(R.id.InputEdit);
		view.setText(String.valueOf(nDayPromptValue));
		
		builder.setContentView(m_dayPromptAlertInputView);
		builder.setNeutralButton(R.string.common_button_confirm, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				EditText view = (EditText)m_dayPromptAlertInputView.findViewById(R.id.InputEdit);								
				String strValue = view.getText().toString();
				int nValue = Integer.parseInt(strValue);
				setDayPrompt(nValue);
				dialog.dismiss();
			}
		});
		
		builder.setPositiveButton(R.string.common_button_cancel, new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int id) {  
				dialog.cancel();  
			}  
		});  

		AlertDialogCustom dialog = builder.create();
		dialog.setOwnerActivity(this);
		dialog.show();
	}
	
	protected void setDayPrompt(int dayValue){
		/*FileOperation fileOp = new FileOperation(this);
		fileOp.SaveFileContent(m_fileDayWarn, String.valueOf(m_nDayPromptValue));*/		
		NetworkMonitorConfig.getInstance(getApplicationContext()).setDayWarnValue(dayValue);
		
		TextView dayPromptValueLabel = (TextView)findViewById(R.id.day_volume_prompt_value);
		if(dayValue <= 0)
			dayPromptValueLabel.setText(R.string.not_setting);
		else			
			dayPromptValueLabel.setText(String.valueOf(dayValue) + " M");		
	}
	
/*****************Clear usage setting start********************/	
	protected void showClearUsagePrompt(){
		AlertDialogCustom.Builder builder = new AlertDialogCustom.Builder(this);
		builder.setTitle(R.string.setting_altert_clear_usage_title);
		builder.setMessage(R.string.setting_altert_clear_usage_message);
		builder.setNeutralButton(R.string.common_button_confirm, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {				
				clearUsage();				
				dialog.dismiss();
			}
		});
		builder.setPositiveButton(R.string.common_button_cancel, new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int id) {  
				dialog.cancel();  
			}  
		});  

		AlertDialogCustom dialog = builder.create();
		dialog.setOwnerActivity(this);
		dialog.show();
	}
	
	protected void clearUsage(){
		NetworkMonitorConfig.getInstance(getApplicationContext()).clearDBUsage();
		NetworkMonitorConfig.getInstance(getApplicationContext()).clearManualAdjustFlux();
	}
/*****************Clear usage setting end********************/
	
	protected void showToast(String strToast){
		Toast toast = Toast.makeText(getApplicationContext(),strToast,Toast.LENGTH_SHORT);
		toast.show();
	}
	
	public void onClick(View v){
		switch(v.getId()){	
		case R.id.service_switchbtn:				
		case R.id.layout_setting_service_trigger:
			boolean bServiceOn = NetworkMonitorConfig.getInstance(getApplicationContext()).getServiceOnState(); 
			if(bServiceOn)				
				showTriggerOpPrompt();
			else{				
				setServiceSwitchState(!bServiceOn);
			}			
			break;
		case R.id.layout_setting_month_packgage:
			showMonthPackageSettingAlert();
			break;
		case R.id.layout_setting_start_day:
			showDaySelectorAlert();
			break;
		case R.id.layout_setting_auto_adjust:
			showAutoAdjustActivity();
			break;
		case R.id.layout_setting_float_window:
			if(((NetworkMonitorApp)getApplication()).m_floatLayout == null)
				showFloatViewSetting();
			else{
				WindowManager winManager = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
				winManager.removeView(((NetworkMonitorApp)getApplication()).m_floatLayout);				
				((NetworkMonitorApp)getApplication()).m_floatLayout = null;
			}
			break;
			
		case R.id.exceed_prompt_switchbtn:
		case R.id.layout_setting_exceed_prompt:			
			boolean bExceedOn = NetworkMonitorConfig.getInstance(getApplicationContext()).getExceedOnState();
			onExceedStateChange(!bExceedOn);
			break;
		case R.id.layout_setting_month_surplus_warning:
			int nMonthPackageValue = NetworkMonitorConfig.getInstance(getApplicationContext()).getMonthPackage();
			if(nMonthPackageValue == -1)
				showToast(getString(R.string.setting_toast_setMonthPackge_first));  
			else if(nMonthPackageValue == 0)
				showToast(getString(R.string.setting_toast_noNetworkPackage));
			else
				showMonthSurplusAlert();
			break;
		case R.id.layout_setting_day_volum_prompt:
			int nMonthSurplusValue = NetworkMonitorConfig.getInstance(getApplicationContext()).getMonthWarnValue();
			if(nMonthSurplusValue <= 0)
				showToast(getString(R.string.setting_toast_noNetworkPackage));
			else
				showDayPromptAlert();
			break;
		case R.id.layout_setting_clear_usage:
			showClearUsagePrompt();
			break;
		default:
			break;
		}
	}
	
	/*****Get Monitor Data start******/
	/*protected boolean getServiceOnState(){
		FileOperation fileOp = new FileOperation(this);
		String serviceOn = fileOp.getFileContent(m_fileServiceOn);
		return strToBoolean(serviceOn);
	}*/
	
	/*protected int getMonthPakcage(){
		FileOperation fileOp = new FileOperation(this);
		String strPackage = fileOp.getFileContent(m_fileMonthPackage);
		int value = -1;
		if(!strPackage.equals("")){
			value = Integer.parseInt(strPackage);
		}
		return value;
	}*/
	
	/*protected int getStartDay(){
		FileOperation fileOp = new FileOperation(this);
		String strStartDay = fileOp.getFileContent(m_fileStartDay);
		int value = 0;
		if(!strStartDay.equals("")){
			value = Integer.parseInt(strStartDay);
		}
		return value;
	}*/
	
	//protected boolean getExceedState(){
		//FileOperation fileOp = new FileOperation(this);
		//String exceedOn = fileOp.getFileContent(m_fileExceedOn);
		//return strToBoolean(exceedOn);
		
		/*
		boolean bRet =true;
		if(getMonthPakcage() == -1)
			bRet = false;
		return bRet;
		*/		
	//}
	
	/*protected int getMonthSurplusValue(){
		FileOperation fileOp = new FileOperation(this);
		String strStartDay = fileOp.getFileContent(m_fileMonthWarn);
		int value = 0;
		if(!strStartDay.equals("")){
			value = Integer.parseInt(strStartDay);
		}
		return value;		
	}*/
	
	/*protected int getDayPromptValue(){
		FileOperation fileOp = new FileOperation(this);
		String strStartDay = fileOp.getFileContent(m_fileDayWarn);
		int value = 0;
		if(!strStartDay.equals("")){
			value = Integer.parseInt(strStartDay);
		}
		return value;	
	}*/
/*****Get Monitor Data end  ******/
}

