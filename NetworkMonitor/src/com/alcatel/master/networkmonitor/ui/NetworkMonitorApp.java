package com.alcatel.master.networkmonitor.ui;

import com.alcatel.master.networkmonitor.common.MonitorService;
import com.alcatel.master.networkmonitor.common.NetworkMonitorConfig;

import android.app.*;
import android.view.*;
public class NetworkMonitorApp extends Application {
	private WindowManager.LayoutParams m_wmParams 
		= new WindowManager.LayoutParams();
	public NetworkFloatView m_floatLayout = null;	
	public int m_statusHeight = 0;
	public WindowManager.LayoutParams getNetAppWm(){
		return m_wmParams;
	}
	public MainActivity m_activityMain = null;
	public MonitorService m_monitorService = null;
	@Override
	public void onCreate() {
		super.onCreate();
		NetworkMonitorConfig.getInstance(getApplicationContext());
	}
	
}
