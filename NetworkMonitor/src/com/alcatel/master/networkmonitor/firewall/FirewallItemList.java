package com.alcatel.master.networkmonitor.firewall;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FirewallItemList {	

	private static FirewallItemList mInstance = null;
	public  boolean m_bRooted;

	private List<Map<String, Object>> m_listItemData;	
	
	public static FirewallItemList getInstance() {
		if (mInstance == null) {
			mInstance = new FirewallItemList();			
		}
		return mInstance;
	}
	
	public FirewallItemList() {	
		
		m_listItemData = new ArrayList<Map<String, Object>>();
		m_bRooted = false;
	}
	
	public List<Map<String, Object>> getItemList()
	{
		return m_listItemData;
	}	
}
