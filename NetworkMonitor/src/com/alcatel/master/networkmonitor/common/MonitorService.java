package com.alcatel.master.networkmonitor.common;

import android.app.Service;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.TrafficStats;
import java.lang.Runnable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.alcatel.master.networkmonitor.R;
import com.alcatel.master.networkmonitor.ui.AlertDialogCustom;

public class MonitorService extends Service {
	private MyServiceBinder binder = new MyServiceBinder(); 	
	
	private UsageDatabaseAdapter dbAdapter;
	private Handler handler = new Handler();
	private long now_MobileRx = 0 , now_MobileTx = 0;
	private long old_MobileRx = 0 ,old_MobileTx = 0;
	private long dur_MobileRx = 0,dur_MobileTx = 0;
	private long all_dbMobileRx= 0 ,all_dbMobileTx= 0;
	private long totalRx = 0 , totalTx = 0;	 
	private long m_mStartTime = 0;
	private long m_netSpeed = 0;
	static int count = 12; //what's this?	
	
	private double m_doubleMonthUse = 0;
	private int mMonthPackage = 0;
	private int mMonthWarnValue = 0;
	private int mDayWarnValue = 0;
	private ManualAdjustItem mManualItem = new ManualAdjustItem();
	private int mPopMonth = -1;
	private int mPopDate = 0;
	
	private Calendar mToday = Calendar.getInstance();
	private AlertDialogCustom.Builder mbuilder;
	private AlertDialogCustom mDialog;
	@Override
	public void onCreate(){
		old_MobileRx = TrafficStats.getMobileRxBytes();
		old_MobileTx = TrafficStats.getMobileTxBytes();
		m_mStartTime = System.nanoTime();
		
		totalRx = TrafficStats.getTotalRxBytes();
		totalTx = TrafficStats.getTotalTxBytes();	
		
		mMonthPackage = NetworkMonitorConfig.getInstance(getApplicationContext()).getMonthPackage();
		mMonthWarnValue = NetworkMonitorConfig.getInstance(getApplicationContext()).getMonthWarnValue();
		mDayWarnValue = NetworkMonitorConfig.getInstance(getApplicationContext()).getDayWarnValue();		
		NetworkMonitorConfig.getInstance(getApplicationContext()).getManualAdjustFlux(mManualItem);
		
		createWarningAlert();
		handler.post(thread);
		super.onCreate();
	}	
	
	Runnable thread = new Runnable(){
		
		@Override
		public void run(){
			Calendar now = Calendar.getInstance();
			if(mToday.get(Calendar.DATE) != now.get(Calendar.DATE)){
				mToday = Calendar.getInstance();
			}
			
			boolean bServiceOn = NetworkMonitorConfig.getInstance(getApplicationContext()).getServiceOnState();
			if(bServiceOn){
				updateUsageDatabase();
				if(IsMonthUsageOver() 
						|| IsDayUsageOver()){
					
					showWarningAlert();
				}
			}			
			handler.postDelayed(thread, 500);
		}		
	};
	
	protected void updateUsageDatabase(){
		dbAdapter = new UsageDatabaseAdapter(MonitorService.this);			
		
		now_MobileRx = TrafficStats.getMobileRxBytes();
		now_MobileTx = TrafficStats.getMobileTxBytes();
		
		totalRx = TrafficStats.getTotalRxBytes();
		totalTx = TrafficStats.getTotalTxBytes();
		
		if(now_MobileRx != -1 && now_MobileTx != -1){
			dur_MobileRx = now_MobileRx - old_MobileRx;
			dur_MobileTx = now_MobileTx - old_MobileTx;
			old_MobileRx = now_MobileRx;
			old_MobileTx = now_MobileTx;
			now_MobileRx = 0;
			now_MobileTx = 0;
			//mBroadcaseIntent.putExtra("MobileRx", dur_MobileRx);
			//mBroadcaseIntent.putExtra("MobileTx", dur_MobileTx);
		}else{
			//mBroadcaseIntent.putExtra("MobileRx", "No");
			//mBroadcaseIntent.putExtra("MobileTx", "NO");
		}
		
		all_dbMobileRx += dur_MobileRx;
		all_dbMobileTx += dur_MobileTx;
		
		long temp = System.nanoTime();
		long seconds = (temp - m_mStartTime)/(1000*1000*1000);
		if(seconds != 0){
			m_netSpeed = (dur_MobileRx + dur_MobileTx)/seconds;			
			m_mStartTime = temp;
		}	
		
		if(count == 12){
			Date date = new Date();
			if(all_dbMobileRx != 0 || all_dbMobileTx != 0){
				dbAdapter.open();
				Cursor checkMobile = dbAdapter.Check(1, date);//1 为 GPRS流量类型
				if(checkMobile.moveToNext()){
					long up = dbAdapter.getProFlowUp(1, date);
					long dw = dbAdapter.getProFlowDown(1, date);
					all_dbMobileTx += up ;
					all_dbMobileRx += dw ;
					dbAdapter.updateData(all_dbMobileTx, all_dbMobileRx, 1, date);	
				}else{						
					dbAdapter.insertData(all_dbMobileTx, all_dbMobileRx, 1, date);							
				}
				dbAdapter.close();										
				Intent mBroadcaseIntent = new Intent("com.alcatel.master.networkmonitor.broadcast");
				if(mBroadcaseIntent != null){
					long dateusage = all_dbMobileRx + all_dbMobileTx;
					mBroadcaseIntent.putExtra("DateUsage", dateusage);
					sendBroadcast(mBroadcaseIntent);
				}
				all_dbMobileRx=0;
				all_dbMobileTx=0;	
			}	
			count = 1;
		}																				
		count++;		
	}
	
	public void getStartAndEndDate(Calendar startCalendar,Calendar endCalendar){
		int startDay = 
				NetworkMonitorConfig.getInstance(getApplicationContext()).getStartDay();		
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
		
		int startMonthDays = startCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		if(startDay > 1){						
			int endMonthDays = endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			int endDay = startDay - 1;
			if(endDay > endMonthDays)
				endDay = endMonthDays;
			endCalendar.set(startYear,startMonth + 1,endDay);
		}
		else if(startDay == 1){
			endCalendar.set(startYear,startMonth,startMonthDays);
		}
		Date endDate = endCalendar.getTime();	
		endCalendar.setTime(endDate);
	}
	
	protected boolean IsMonthUsageOver(){
		boolean bOver = false;				
		m_doubleMonthUse = 0;
		int nMonthWarnValue = NetworkMonitorConfig.getInstance(getApplicationContext()).getMonthWarnValue();
		int nMonthPackageValue = NetworkMonitorConfig.getInstance(getApplicationContext()).getMonthPackage();
		ManualAdjustItem manuItem = new ManualAdjustItem();			
		NetworkMonitorConfig.getInstance(getApplicationContext()).getManualAdjustFlux(manuItem);
		double manuAdjustMonthUsage = convertB2M(manuItem.lManualFlux.longValue());
		double manuAdjustMonthAlreadUsed = convertB2M(manuItem.lAlreadyUsedValue.longValue());
		if(manuAdjustMonthUsage != 0){
			m_doubleMonthUse = m_doubleMonthUse - manuAdjustMonthAlreadUsed + manuAdjustMonthUsage;
			//doubleMonthUse = manuAdjustMonthUsage;
		}else{
			dbAdapter.open();
			Calendar startCalendar = Calendar.getInstance();
			Calendar endCalendar = Calendar.getInstance();
			getStartAndEndDate(startCalendar,endCalendar);
			Date startDate = startCalendar.getTime();
			Date endDate = endCalendar.getTime();						
			
			List<DayUsageItem> monthDayUsageList = dbAdapter.getMobileDaysUsageList(startDate,endDate);
			long monthUsage = 0;
			for(int i = 0 ; i < monthDayUsageList.size() ; i++){
				monthUsage += monthDayUsageList.get(i).dayUsage;
			}
			
			m_doubleMonthUse = convertB2M(monthUsage);			
			dbAdapter.close();
		}			
				
		Calendar calCur = Calendar.getInstance();
		if(IsMonthPacakgeOver(m_doubleMonthUse,manuItem,calCur)
				|| IsMonthWarnOver(m_doubleMonthUse,manuItem,calCur)){
			mPopMonth = calCur.get(Calendar.MONTH);				
			bOver = true;
		}
		mManualItem.lManualFlux = manuItem.lManualFlux;
		mManualItem.lAlreadyUsedValue = manuItem.lAlreadyUsedValue;	
		mMonthPackage = nMonthPackageValue;
		mMonthWarnValue = nMonthWarnValue;
					
		return bOver;
	}	
	
	protected boolean IsMonthPacakgeOver(double doubleMonthUse,ManualAdjustItem manuItem,Calendar calCur){
		boolean bOver = false;		
		int nMonthPackageValue = NetworkMonitorConfig.getInstance(getApplicationContext()).getMonthPackage();
		if(nMonthPackageValue > 0 
				&& doubleMonthUse > nMonthPackageValue 
				&& ( mPopMonth != calCur.get(Calendar.MONTH)
				     || mMonthPackage != nMonthPackageValue
				     || mManualItem.lManualFlux.longValue() != manuItem.lManualFlux.longValue())){
			bOver = true;
		}		
		return bOver;
	}
	
	protected boolean IsMonthWarnOver(double doubleMonthUse,ManualAdjustItem manuItem,Calendar calCur){
		boolean bOver = false;
		boolean bExceedOn = NetworkMonitorConfig.getInstance(getApplicationContext()).getExceedOnState();
		if(bExceedOn){
			int nMonthWarnValue = NetworkMonitorConfig.getInstance(getApplicationContext()).getMonthWarnValue();
			if(nMonthWarnValue > 0 
					&& doubleMonthUse > nMonthWarnValue 
					&& ( mPopMonth != calCur.get(Calendar.MONTH)
					     || mMonthWarnValue != nMonthWarnValue
					     || mManualItem.lManualFlux.longValue() != manuItem.lManualFlux.longValue())){
				bOver = true;
			}			
		}
		
		return bOver;
	}
	
	protected boolean IsDayUsageOver(){		
		boolean bOver = false;
		boolean bExceedOn = NetworkMonitorConfig.getInstance(getApplicationContext()).getExceedOnState();
		if(bExceedOn){
			double doubleDayUse = 0;
			Date date = new Date();
			dbAdapter.open();
			long dbDayUsage = dbAdapter.getProFlowDown(1, date) + dbAdapter.getProFlowUp(1, date);
			dbDayUsage += all_dbMobileRx + all_dbMobileTx;		
			dbAdapter.close();		
			int nDayValue = NetworkMonitorConfig.getInstance(getApplicationContext()).getDayWarnValue();
			if(nDayValue > 0){
				doubleDayUse = convertB2M(dbDayUsage);		
				Calendar calCur = Calendar.getInstance();		
				if(doubleDayUse >= nDayValue && 
						(mPopDate != calCur.get(Calendar.DATE)
						|| mDayWarnValue != nDayValue)){
					mPopDate = calCur.get(Calendar.DATE);				
					bOver = true;
					//showWarningAlert();			
				}
				
				mDayWarnValue = nDayValue;
			}
		}			
		return bOver;
	}	
	
	public double convertB2M(long value){
		double douValue = 0;
		BigDecimal trafficKB;
		BigDecimal trafficMB;			
		BigDecimal temp = new BigDecimal(value);
		BigDecimal divide = new BigDecimal(1024);
		trafficKB = temp.divide(divide, 2, 1);
		if(trafficKB.compareTo(divide) > 0){
			trafficMB = trafficKB.divide(divide,2,1);
			douValue = trafficMB.doubleValue();							
		}
		return douValue;
	}
	
	protected void createWarningAlert(){
		mbuilder = new AlertDialogCustom.Builder(this);
		mbuilder.setTitle(R.string.setting_altert_usage_warning_prompt_title);
		mbuilder.setMessage(R.string.setting_altert_usage_warning_prompt_message);
		mbuilder.setNeutralButton(R.string.common_button_confirm, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {						
				dialog.dismiss();
			}
		});			
		
		mDialog = mbuilder.create();
		mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
	}
	
	protected void showWarningAlert(){
		if(mDialog != null){
			if(!mDialog.isShowing())
				mDialog.show();
		}
	}
	
	public double getMonthUsage(){
		return m_doubleMonthUse;
	}
	
	public long getNetSpeed(){
		return m_netSpeed;
	}
	
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	public class MyServiceBinder extends Binder{	
		public MonitorService getService(){
			return MonitorService.this;
		}
	}
}
