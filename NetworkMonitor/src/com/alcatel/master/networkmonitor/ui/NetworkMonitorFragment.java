package com.alcatel.master.networkmonitor.ui;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.alcatel.master.networkmonitor.R;
import com.alcatel.master.networkmonitor.common.DayUsageItem;
import com.alcatel.master.networkmonitor.common.ManualAdjustItem;
import com.alcatel.master.networkmonitor.common.NetworkMonitorConfig;
import com.alcatel.master.networkmonitor.ui.ChartView.ChartData;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

@TargetApi(11)
public class NetworkMonitorFragment extends Fragment implements OnClickListener{
	private float WARNING_PERCENT = 0.2f;
	private Handler m_handler = null;
	private long m_lPackage = 0;
	private long m_lTodayUsage = 0;
	private long m_lTotalUsage = 0;
	
	public View m_fragmentView = null;
	public ChartView m_chartView = null;
	private TextView m_tvTodayUsage = null;
	private TextView m_tvThisMonthUsage = null;
	private TextView m_tvThisMonthLeave = null;
	private TextView m_tvUsageTip = null;
	private TextView m_tvPercent = null;
	private Button m_btnSet = null;
	private HorizontalScrollView m_hScrollView = null;
	private FrameLayout m_loadingLayout = null;
	private FrameLayout m_pageLayout = null;
	private Calendar m_caStart = Calendar.getInstance();
    private Calendar m_caEnd = Calendar.getInstance();
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		m_fragmentView = inflater.inflate(R.layout.flux_monitor,container,false);
		initMainView();
		return m_fragmentView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//m_hScrollView.setScrollX(1000);
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		//calStartAndEndCalendar(m_caStart,m_caEnd);
		
		//refleshUIStatus();
		//refleshChartView();
		startLoadingThread();
	}
	
    @SuppressLint("HandlerLeak")
	private void startLoadingThread() {
    	m_handler= new Handler() {
			@SuppressLint("NewApi")
			public void handleMessage(Message msg) {
	        	refleshUIStatus();
				m_loadingLayout.setVisibility(View.GONE);
				m_pageLayout.setVisibility(View.VISIBLE);
				//m_hScrollView.setScrollX(1000);
			}
		};
		m_pageLayout.setVisibility(View.GONE);
		m_loadingLayout.setVisibility(View.VISIBLE);
		showLoadingview();
		Thread thread = new Thread(new LoadingRunnable());  
        thread.start();
    }
    
    private class LoadingRunnable implements Runnable{ 
        
        public LoadingRunnable(){ 
        } 
         
        @Override 
        public void run() {
        	/*calStartAndEndCalendar(m_caStart,m_caEnd);
        	calPackage();
        	calTodayUsage();
        	calThisMonthUsedUsage();
        	refleshChartView();*/
        	calUIData();
            m_handler.sendEmptyMessage(0);
        } 
    };
    
    public void calUIData() {
    	calStartAndEndCalendar(m_caStart,m_caEnd);
    	autoClearManualAdjustFlux();
    	calPackage();
    	calTodayUsage();
    	calThisMonthUsedUsage();
    	refleshChartView();
    }
    
    private void showLoadingview() {
		Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
				m_fragmentView.getContext(), R.anim.firewall_loading_anim);
		ImageView loadingImage = (ImageView) m_fragmentView
				.findViewById(R.id.flux_loading_image);
		loadingImage.startAnimation(hyperspaceJumpAnimation);
	}
	
	private void autoClearManualAdjustFlux() {
		ManualAdjustItem manualItem = new ManualAdjustItem();
		NetworkMonitorConfig.getInstance(NetworkMonitorFragment.this.getActivity())
			.getManualAdjustFlux(manualItem);
		
		SimpleDateFormat mFormat = new SimpleDateFormat("yyyyMMdd");
		String strSetDate = mFormat.format(manualItem.lAdjustTime);
		String strStartDate = mFormat.format(m_caStart.getTimeInMillis());
		String strEndDate = mFormat.format(m_caEnd.getTimeInMillis());
		if(!(strSetDate.compareTo(strStartDate) >= 0 && strSetDate.compareTo(strEndDate) <= 0)) {		
			NetworkMonitorConfig.getInstance(this.getActivity().getApplicationContext()).
				clearManualAdjustFlux();
		}
	}
	
	private void updateButton() {
		if(m_lPackage == 0) {
			m_btnSet.setText(R.string.set_package);
		}else{
			m_btnSet.setText(R.string.calibration);
		}
	}
	
	private void refleshUIStatus() {
		//update calibration button 
		updateButton();
		//update today usage
		long lTodayUsage = m_lTodayUsage;
		m_tvTodayUsage.setText(formatData(lTodayUsage));
		
		//update this month usage
		long lTotalUsage = m_lTotalUsage;
		m_tvThisMonthUsage.setText(formatData(lTotalUsage));
		
		//update this month leave
		long nMonthPackage = m_lPackage;
		if(nMonthPackage == 0) {
			//not set
			m_tvThisMonthLeave.setTextColor(getResources().getColor(R.color.color_red)); 
			m_tvThisMonthLeave.setText("-");
		}else {
			long lLeaveUsage = nMonthPackage - lTotalUsage;
			
			if(lLeaveUsage <= 0) {
				m_tvThisMonthLeave.setTextColor(getResources().getColor(R.color.color_red));
				String strValue = formatData(lLeaveUsage * -1);
				strValue = "-" + strValue;
				m_tvThisMonthLeave.setText(strValue);
			}else if(lLeaveUsage < nMonthPackage * WARNING_PERCENT) {//warn
				m_tvThisMonthLeave.setTextColor(getResources().getColor(R.color.color_yellow));
				m_tvThisMonthLeave.setText(formatData(lLeaveUsage));
			}else{//rich
				m_tvThisMonthLeave.setTextColor(getResources().getColor(R.color.color_green));
				m_tvThisMonthLeave.setText(formatData(lLeaveUsage));
			}
		}
		
		//update the usage description
		updateUsageDescription();
		//update the percent
		updatePercent();
	}
	
	private void updatePercent() {
		long nMonthPackage = m_lPackage;
		if(nMonthPackage == 0) {
			//not set
			m_tvPercent.setTextColor(getResources().getColor(R.color.color_red));
			m_tvPercent.setText("0%");
		}else{
			long lTotalUsage = m_lTotalUsage;
			long lLeaveUsage = nMonthPackage - lTotalUsage;
			//over month package
			if(lLeaveUsage <= 0) {
				m_tvPercent.setTextColor(getResources().getColor(R.color.color_red));
				m_tvPercent.setText("0%");
			}else if(lLeaveUsage < nMonthPackage * WARNING_PERCENT) {//warn
				float fPercent = ((lLeaveUsage * 1.00f) / nMonthPackage);
				DecimalFormat df = new DecimalFormat("0%");
				String strPercent = df.format(fPercent).toString();
				m_tvPercent.setTextColor(getResources().getColor(R.color.color_yellow));
				m_tvPercent.setText(strPercent);
			}else{//rich
				float fPercent = ((lLeaveUsage * 1.00f) / nMonthPackage);
				DecimalFormat df = new DecimalFormat("0%");
				String strPercent = df.format(fPercent).toString();
				m_tvPercent.setTextColor(getResources().getColor(R.color.color_green));
				m_tvPercent.setText(strPercent);
			}
		}
	}
	
	private void updateUsageDescription() {
		boolean bStart = NetworkMonitorConfig.getInstance(this.getActivity().getApplicationContext()).getServiceOnState();
		if(bStart == false) {
			m_tvUsageTip.setTextColor(getResources().getColor(R.color.color_red));
			setUsageTipDrawable(R.drawable.danger);
			m_tvUsageTip.setText(R.string.net_setting_service_not_started);
		}else{
			long nMonthPackage = m_lPackage;
			if(nMonthPackage == 0) {
				//not set
				m_tvUsageTip.setTextColor(getResources().getColor(R.color.color_red));
				setUsageTipDrawable(R.drawable.danger);
				m_tvUsageTip.setText(R.string.net_tip_noset);
			}else{
				long lTotalUsage = m_lTotalUsage;
				long lLeaveUsage = nMonthPackage - lTotalUsage;
				//over month package
				if(lLeaveUsage <= 0) {
					m_tvUsageTip.setTextColor(getResources().getColor(R.color.color_red));
					setUsageTipDrawable(R.drawable.danger);
					m_tvUsageTip.setText(R.string.net_tip_over);
				}else if(lLeaveUsage < nMonthPackage * WARNING_PERCENT) {//warn
					m_tvUsageTip.setTextColor(getResources().getColor(R.color.color_yellow));
					setUsageTipDrawable(R.drawable.warn);
					m_tvUsageTip.setText(R.string.net_tip_warn);
				}else{//rich
					m_tvUsageTip.setTextColor(getResources().getColor(R.color.color_green));
					setUsageTipDrawable(R.drawable.safe);
					m_tvUsageTip.setText(R.string.net_tip_enough);
				}
			}
		}
	}
	
	private void calTodayUsage() {
		Calendar caNow = Calendar.getInstance();
		m_lTodayUsage = NetworkMonitorConfig.getInstance(this.getActivity().getApplicationContext()).
			getOneDayMobileUsage(caNow.getTime());
	}
	
	private void calPackage() {
		long nMonthPackage = NetworkMonitorConfig.getInstance(this.getActivity().getApplicationContext()).getMonthPackage();
		if(nMonthPackage == 0 || nMonthPackage == -1) {
			nMonthPackage = 0;
		}
		
		m_lPackage = nMonthPackage * 1024 * 1024;//unit M
	}
	
	private void calThisMonthUsedUsage() {
		List<DayUsageItem> dayUsageList = NetworkMonitorConfig.getInstance(this.getActivity().getApplicationContext()).
				getDaysMobileUsage(m_caStart.getTime(), m_caEnd.getTime());
		long lTotalUsage = 0;
		for(int i = 0;dayUsageList != null && i < dayUsageList.size();i++) {
			lTotalUsage +=  dayUsageList.get(i).dayUsage;
		}
		//carry out manual set
		ManualAdjustItem manualItem = new ManualAdjustItem();
		NetworkMonitorConfig.getInstance(this.getActivity().getApplicationContext()).
			getManualAdjustFlux(manualItem);
		if(manualItem.lManualFlux != 0){
			lTotalUsage = lTotalUsage - manualItem.lAlreadyUsedValue + manualItem.lManualFlux;
		}
		m_lTotalUsage = lTotalUsage;
	}
	
	private void setUsageTipDrawable(int nDrawableId) {
		Drawable drawable= getResources().getDrawable(nDrawableId);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		m_tvUsageTip.setCompoundDrawables(drawable,null,null,null);
	}
	 
	protected void initMainView()
	{
		Button btncali = (Button)m_fragmentView.findViewById(R.id.button_calibration);
		btncali.setOnClickListener(this);
		m_chartView = (ChartView)m_fragmentView.findViewById(R.id.layout_chart);
		m_tvTodayUsage = (TextView)m_fragmentView.findViewById(R.id.today_useage);
		m_tvThisMonthUsage = (TextView)m_fragmentView.findViewById(R.id.this_month_usaged);
		m_tvThisMonthLeave = (TextView)m_fragmentView.findViewById(R.id.this_month_leave);
		m_tvUsageTip = (TextView)m_fragmentView.findViewById(R.id.text_usege_tip);
		m_tvPercent = (TextView)m_fragmentView.findViewById(R.id.textview_percent);
		
		m_hScrollView = (HorizontalScrollView)m_fragmentView.findViewById(R.id.horizontalScrollView_chart_view);
		
		m_loadingLayout = (FrameLayout)m_fragmentView.findViewById(R.id.flux_layout_loading);
		m_pageLayout = (FrameLayout)m_fragmentView.findViewById(R.id.flux_layout);
		
		m_btnSet = (Button)m_fragmentView.findViewById(R.id.button_calibration);
	}
	
	private void initChartDateString(ChartData data) {
		data.calDate();
	}
	
	private void initChartData(ChartData data) {
		data.mDatas = NetworkMonitorConfig.getInstance(this.getActivity().getApplicationContext()).
				getDaysMobileUsage(m_caStart.getTime(), m_caEnd.getTime());
	}

	public void refleshChartView()
	{
		m_chartView.setStartAndEndDate(m_caStart, m_caEnd);
		ChartData data = m_chartView.new ChartData();
		initChartDateString(data);
		initChartData(data);
		m_chartView.setData(data);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
    	case R.id.button_calibration:
    		setBtnClick();
    		break;
    	}	
	}
	
	private void setBtnClick() {
		long nMonthPackage = m_lPackage;
		if(nMonthPackage == 0) {
			showMonthPackageSettingAlert();
		}else{
			showFluxManualCalibrationAlert();
		}
	}
	
	protected void showFluxManualCalibrationAlert(){
		AlertDialogCustom.Builder builder = new AlertDialogCustom.Builder(this.getActivity());
		builder.setTitle(R.string.net_adjust_flux_title);
		final View adjustAlertInputView = this.getActivity().getLayoutInflater().inflate(R.layout.alert_dialog_custom_inputedit_view, null);			
		TextView inputViewTitle = (TextView)adjustAlertInputView.findViewById(R.id.InputViewTitle);
		inputViewTitle.setText(R.string.net_adjust_flux_input_label);
		TextView rightText = (TextView)adjustAlertInputView.findViewById(R.id.text_right);
		rightText.setText(R.string.volume_unit_M);	
		final EditText inputView = (EditText)adjustAlertInputView.findViewById(R.id.InputEdit);
		inputView.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
		
		long lTotalUsage = m_lTotalUsage;
		final double baseMB = 1024 * 1024.00;
		DecimalFormat df = new DecimalFormat("0.##");
		String strSize = df.format(lTotalUsage/baseMB).toString();
		if(strSize.compareToIgnoreCase("0") != 0) {
			inputView.setText(strSize);
		}
		
			
		m_tvThisMonthUsage.setText(formatData(lTotalUsage));
				
		builder.setContentView(adjustAlertInputView);
		builder.setNeutralButton(R.string.common_button_confirm, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub								
				String strValue = inputView.getText().toString();
				if(strValue == null ||strValue.length() == 0) {
					strValue = "0";
				}
				long nAdjustValue = (long) ((Float.parseFloat(strValue)) * baseMB);
				
				long nAlreadyUsedValue = 0;
				List<DayUsageItem> dayUsageList = NetworkMonitorConfig.getInstance(NetworkMonitorFragment.this.getActivity().getApplicationContext()).
						getDaysMobileUsage(m_caStart.getTime(), m_caEnd.getTime());
				for(int i = 0;dayUsageList != null && i < dayUsageList.size();i++) {
					nAlreadyUsedValue +=  dayUsageList.get(i).dayUsage;
				}
				
				ManualAdjustItem manualItem = new ManualAdjustItem();
				manualItem.lManualFlux = nAdjustValue;
				manualItem.lAlreadyUsedValue = nAlreadyUsedValue;
				manualItem.lAdjustTime = new Date().getTime();
				NetworkMonitorConfig.getInstance(NetworkMonitorFragment.this.getActivity()).
					setManualAdjustFlux(manualItem);
				calUIData();
				refleshUIStatus();
				dialog.dismiss();
			}
		});
		
		builder.setPositiveButton(R.string.common_button_cancel, new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int id) {  
				dialog.cancel();  
			}  
		});  

		AlertDialogCustom dialog = builder.create();
		dialog.setOwnerActivity(this.getActivity());
		dialog.show();
	}
	
	protected void showMonthPackageSettingAlert(){
		AlertDialogCustom.Builder builder = new AlertDialogCustom.Builder(this.getActivity());
		builder.setTitle(R.string.setting_label_month_package);
		final View monthPackageAlertInputView = this.getActivity().getLayoutInflater().inflate(R.layout.alert_dialog_custom_inputedit_view, null);			
		TextView inputViewTitle = (TextView)monthPackageAlertInputView.findViewById(R.id.InputViewTitle);
		inputViewTitle.setText(R.string.setting_altert_month_package_message);
		TextView rightText = (TextView)monthPackageAlertInputView.findViewById(R.id.text_right);
		rightText.setText(R.string.volume_unit_M);			
				
		builder.setContentView(monthPackageAlertInputView);
		builder.setNeutralButton(R.string.common_button_confirm, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				EditText view = (EditText)monthPackageAlertInputView.findViewById(R.id.InputEdit);								
				String strValue = view.getText().toString();
				int nMonthPackageValue = Integer.parseInt(strValue);
				NetworkMonitorConfig.getInstance(NetworkMonitorFragment.this.getActivity()).
					setMonthPackage(nMonthPackageValue);
				calUIData();
				refleshUIStatus();
				dialog.dismiss();
			}
		});
		
		builder.setPositiveButton(R.string.common_button_cancel, new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int id) {  
				dialog.cancel();  
			}  
		});  

		AlertDialogCustom dialog = builder.create();
		dialog.setOwnerActivity(this.getActivity());
		dialog.show();
	}
	
	private void calStartAndEndCalendar(Calendar startCalendar,Calendar endCalendar) {
		
		int startDay = 
				NetworkMonitorConfig.getInstance(this.getActivity()).getStartDay();		
		//month = actual month - 1
		Calendar curCalendar = Calendar.getInstance(); 
		int nowDay = curCalendar.get(Calendar.DATE);
		int nowMonth = curCalendar.get(Calendar.MONTH);
		int nowYear = curCalendar.get(Calendar.YEAR);
		int startMonth = nowMonth,startYear = nowYear;
		if(startDay > nowDay){
			if(nowMonth == 0){
				startMonth = 11;
				startYear = nowYear - 1;
			}else
				startMonth -= 1;
		}	
		startCalendar.set(Calendar.YEAR, startYear);
		startCalendar.set(Calendar.MONTH, startMonth);
		startCalendar.set(Calendar.DATE, startDay);
		if(startMonth != startCalendar.get(Calendar.MONTH))
			startCalendar.set(Calendar.DATE, 1);		
		//Date startDate = startCalendar.getTime();
		
		int startMonthDays = startCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		if(startDay > 1){						
			int endMonthDays = endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			int endDay = startDay - 1;
			if(endDay > endMonthDays)
				endDay = endMonthDays;
			endCalendar.set(startYear,startMonth + 1,endDay);
		}
		else if(startDay == 1){
			//endCalendar.set(startYear,startMonth,startMonthDays);
			endCalendar.set(Calendar.YEAR, startYear);
			endCalendar.set(Calendar.MONTH, startMonth);
			endCalendar.set(Calendar.DATE, startMonthDays);
		}
		Date endDate = endCalendar.getTime();	
		endCalendar.setTime(endDate);
		/*
		Calendar caCur = Calendar.getInstance();
		//test
		//caCur.set(2013,2,2);
		//mData.mBalanceSheetDay = 30;
		//test
	    int nDay=caCur.get(Calendar.DATE);
	    caStart.setTime(caCur.getTime());
	    caEnd.setTime(caCur.getTime());
	    if(nDay >= m_nBalanceSheetDay) {
	    	caStart.set(caCur.get(Calendar.YEAR), caCur.get(Calendar.MONTH), m_nBalanceSheetDay);
	    	int nEndMonth = caEnd.get(Calendar.MONTH);
	    	int nMonthChangedCount = 0;
	    	while(true) {	
	    		int nEndDay = caEnd.get(Calendar.DATE);
	    		if(nEndDay == m_nBalanceSheetDay && nEndMonth != caEnd.get(Calendar.MONTH)) {
	    			caEnd.add(Calendar.DATE, -1);
	    			break;	
	    		}
	    		if(nEndMonth != caEnd.get(Calendar.MONTH)) {
	    			nEndMonth = caEnd.get(Calendar.MONTH);
	    			nMonthChangedCount++;
	    		}
	    		if(nMonthChangedCount == 2) {//changed 2
	    			caEnd.add(Calendar.DATE, -1);
	    			break;
	    		}
	    		caEnd.add(Calendar.DATE, 1);
	    	}
	    	
	    }else{
	    	int nStartMonth = caStart.get(Calendar.MONTH);
	    	while(true) {
	    		 int nStartDay = caStart.get(Calendar.DATE);
	    		 if(nStartDay == m_nBalanceSheetDay) {
	    			 break;
	    		 }
	    		 if(nStartDay <  m_nBalanceSheetDay && nStartMonth != caStart.get(Calendar.MONTH)) {
	    			 caStart.add(Calendar.DATE, 1);
	    			 break;
	    		 }
	    		 caStart.add(Calendar.DATE, -1);
	    	}
	    	
	    	int nEndMonth = caEnd.get(Calendar.MONTH);
	    	while(true) {
	    		int nEndDay = caEnd.get(Calendar.DATE);
	    		if(nEndDay == m_nBalanceSheetDay) {
	    			caEnd.add(Calendar.DATE, -1);
	    			break;	
	    		}
	    		if(nEndDay != m_nBalanceSheetDay && nEndMonth != caEnd.get(Calendar.MONTH)) {
	    			caEnd.add(Calendar.DATE, -1);
	    			break;
	    		}
	    		caEnd.add(Calendar.DATE, 1);
	    	}
	    }
	    */
	}
	
	
	private String formatData(long lData)
	{
		double baseB = 1.00;
		double baseKB = 1024.00;
		double baseMB = 1024 * 1024.00;
		double baseGB = 1024 * 1024 * 1024.00;
		
		String strSize = "";
		if(lData < baseKB){
			DecimalFormat df = new DecimalFormat("0.##B");
			strSize = df.format(lData/baseB).toString();
		}else if(lData >= baseKB && lData < baseMB){
			DecimalFormat df = new DecimalFormat("0.##K");
			strSize = df.format(lData/baseKB).toString();
		}else if(lData >= baseMB && lData < baseGB){
			DecimalFormat df = new DecimalFormat("0.##M");
			strSize = df.format(lData/baseMB).toString();
		}else{
			DecimalFormat df = new DecimalFormat("0.##G");
			strSize = df.format(lData/baseGB).toString();
		}
		
		return strSize;
	}  

}
