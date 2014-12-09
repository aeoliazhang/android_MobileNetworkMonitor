package com.alcatel.master.networkmonitor.common;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;

public class NetworkMonitorSettings {
	
	private static final String SETTING_FILE = "NetworkMonitor";
	private static final String ITEM_SERVICE_ON_STATE = "ServiceOn";
	private static final String ITEM_MONTH_PACKAGE = "MonthPackage";
	private static final String ITEM_START_DAY = "StartDay";
	private static final String ITEM_EXCEED_ON = "ExceedOn";
	private static final String ITEM_MONTH_WARN = "MonthWarn";
	private static final String ITEM_DAY_WARN = "DayWarn";
	private static final String ITEM_MANUAL_ADJUST_VALUE = "ManualAdjustValue";
	private static final String ITEM_ALREADY_USED_FLUX = "AlreadyUsedFlux";
	private static final String ITEM_FLUX_ADJUST_TIME = "AdjustTime";
	
	
	private Context mContext = null;
	private boolean mIsServiceOn = true;
	private int mMonthPackageValue = -1;
	private int mStartDay = 1;
	private boolean mIsExceedOn = false;
	private int mMonthWarn = 0;
	private int mDayWarn = 0;
	private long mManualAdjustValue = 0;
	private long mAlreadyUsedValue = 0;
	private long mAdjustTime = 0;
	
	public NetworkMonitorSettings(Context context)
	{
		mContext = context;
		loadSettings();
	}
	
	public void setServiceOnState(boolean bOn)
	{
		mIsServiceOn = bOn;
		saveSettings();
	}
	
	//manual adjust flux
	public void getManualAdjustFlux(ManualAdjustItem manualItem) {
		manualItem.lManualFlux = mManualAdjustValue;
		manualItem.lAlreadyUsedValue = mAlreadyUsedValue;
		manualItem.lAdjustTime = mAdjustTime;
	}
	
	public void setManualAdjustFlux(ManualAdjustItem manualItem) {
		mManualAdjustValue = manualItem.lManualFlux.longValue();
		mAlreadyUsedValue = manualItem.lAlreadyUsedValue.longValue();
		mAdjustTime = manualItem.lAdjustTime.longValue();
		saveSettings();
	}
	
	public void clearManualAdjustFlux() {
		mManualAdjustValue = 0;
		mAlreadyUsedValue = 0;
		mAdjustTime = 0;
		saveSettings();
	}
	
	public boolean getServiceOnState () {
		return mIsServiceOn;
	}
	
	public int getMonthPackage() {
		return mMonthPackageValue;
	}
	
	public void setMonthPackage(int nValue) {
		mMonthPackageValue = nValue;
		saveSettings();
	}
	
	public int getStartDay() {
		return mStartDay;
	}
	
	public void setStartDay(int nStartDay) {
		mStartDay = nStartDay;
		saveSettings();
	}
	
	public void setExceedOnState(boolean bOn)
	{
		mIsExceedOn = bOn;
		saveSettings();
	}
	
	public boolean getExceedOnState() {
		return mIsExceedOn;
	}
	
	public int getMonthWarnValue() {
		return mMonthWarn;
	}
	
	public void setMonthWarnValue(int nValue) {
		mMonthWarn = nValue;
		saveSettings();
	}
	
	public int getDayWarnValue() {
		return mDayWarn;
	}
	
	public void setDayWarnValue(int nValue) {
		mDayWarn = nValue;
		saveSettings();
	}
	
	
	private void loadSettings()
	{
		SharedPreferences sp = mContext.getSharedPreferences(SETTING_FILE, Context.MODE_PRIVATE);
		mIsServiceOn = sp.getBoolean(ITEM_SERVICE_ON_STATE, true);
		mMonthPackageValue = sp.getInt(ITEM_MONTH_PACKAGE, -1);
		mStartDay = sp.getInt(ITEM_START_DAY, 1);
		mIsExceedOn = sp.getBoolean(ITEM_EXCEED_ON, false);
		mMonthWarn = sp.getInt(ITEM_MONTH_WARN, 0);
		mDayWarn = sp.getInt(ITEM_DAY_WARN, 0);
		mManualAdjustValue = sp.getLong(ITEM_MANUAL_ADJUST_VALUE, 0);
		mAlreadyUsedValue = sp.getLong(ITEM_ALREADY_USED_FLUX, 0);
		mAdjustTime = sp.getLong(ITEM_FLUX_ADJUST_TIME, 0);
	}
	
	private void saveSettings()
	{
		SharedPreferences sp = mContext.getSharedPreferences(SETTING_FILE, Context.MODE_PRIVATE);
		Editor edt = sp.edit();
		edt.putBoolean(ITEM_SERVICE_ON_STATE, mIsServiceOn);
		edt.putInt(ITEM_MONTH_PACKAGE, mMonthPackageValue);
		edt.putInt(ITEM_START_DAY, mStartDay);
		edt.putBoolean(ITEM_EXCEED_ON, mIsExceedOn);
		edt.putInt(ITEM_MONTH_WARN, mMonthWarn);
		edt.putInt(ITEM_DAY_WARN, mDayWarn);
		edt.putLong(ITEM_MANUAL_ADJUST_VALUE, mManualAdjustValue);
		edt.putLong(ITEM_ALREADY_USED_FLUX, mAlreadyUsedValue);
		edt.putLong(ITEM_FLUX_ADJUST_TIME,mAdjustTime);
		edt.commit();	
	}
}
