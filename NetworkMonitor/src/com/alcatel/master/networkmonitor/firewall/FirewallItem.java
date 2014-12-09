package com.alcatel.master.networkmonitor.firewall;

import android.graphics.drawable.Drawable;

public class FirewallItem {
	public static final String ITEM_UID = "uid";	
	public static final String ITEM_PKG = "pkg";	
	public static final String ITEM_ICON = "icon";
	public static final String ITEM_LABLE = "lable";
	public static final String ITEM_MOBILE_DATA = "mobile data";
	public static final String ITEM_WIFI_DATA = "wifi data";	
	
	public int		 m_uid;
	public String    m_pkg;
	public Drawable  m_icon;
	public String    m_lable;
	public boolean   m_mobileData;	
	public boolean   m_wifiData;
}