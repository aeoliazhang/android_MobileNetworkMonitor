package com.alcatel.master.networkmonitor.common;

import java.util.Date;
import java.util.List;

import android.content.Context;

public class NetworkMonitorConfig {

	//
	private static NetworkMonitorConfig mInstance = null;
	private NetworkMonitorSettings mSettings = null;
	private UsageDatabaseAdapter mDBUsage= null;
	// get instance
	public static NetworkMonitorConfig getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new NetworkMonitorConfig();
			mInstance.initConfig(context);
		}
		return mInstance;
	}

	// initialize configure include settings and data
	private void initConfig(Context context) {
		initSettings(context);
		initUsageDB(context);
	}
	
	// settings
	private void initSettings(Context context) {
		mSettings = new NetworkMonitorSettings(context);
	}
	
	//usage database
	private void initUsageDB(Context context){
		mDBUsage = new UsageDatabaseAdapter(context);
		mDBUsage.open();
	}

	// service state setting
	public void setServiceOnState(boolean bOn) {
		mSettings.setServiceOnState(bOn);
	}

	public boolean getServiceOnState() {
		return mSettings.getServiceOnState();
	}
	
	//month package value
	public int getMonthPackage () {
		return mSettings.getMonthPackage();
	}

	public void setMonthPackage (int nValue) {
		mSettings.setMonthPackage(nValue);
	}
	
	//manual adjust flux
	public void getManualAdjustFlux(ManualAdjustItem manualItem) {
		mSettings.getManualAdjustFlux(manualItem);
	}
	
	public void setManualAdjustFlux(ManualAdjustItem manualItem) {
		mSettings.setManualAdjustFlux(manualItem);
	}
	
	public void clearManualAdjustFlux() {
		mSettings.clearManualAdjustFlux();
	}
	//start day
	public int getStartDay() {
		return mSettings.getStartDay();
	}
	
	public void setStartDay(int nStartDay) {
		mSettings.setStartDay(nStartDay);
	}
	
	//exceed on
	public boolean getExceedOnState() {
		return mSettings.getExceedOnState();
	}
	
	public void setExceedOnState(boolean bOn)
	{
		mSettings.setExceedOnState(bOn);
	}
	
	//month warn
	public int getMonthWarnValue() {
		return mSettings.getMonthWarnValue();
	}
	
	public void setMonthWarnValue(int nValue) {
		mSettings.setMonthWarnValue(nValue);
	}
	
	//day warn
	public int getDayWarnValue() {
		return mSettings.getDayWarnValue();
	}
	
	public void setDayWarnValue(int nValue) {
		mSettings.setDayWarnValue(nValue);
	}
		
	//one day mobile total
	public long getOneDayMobileUsage(Date date){
		long oneDayUsage = mDBUsage.getProFlowUp(1, date) + mDBUsage.getProFlowDown(1, date);
		return oneDayUsage;
	}
	
	//days mobile(receive/transfer/total)
	public List<DayUsageItem> getDaysMobileUsage(Date startDate,Date endDate){
		List<DayUsageItem> dayUsageList =  mDBUsage.getMobileDaysUsageList(startDate,endDate);		
		return dayUsageList;
	}	
	
	//clear database 
	public void clearDBUsage(){
		mDBUsage.clear();
	}
	
	public void closeDBUsage(){		
		mDBUsage.close();
	}
}
