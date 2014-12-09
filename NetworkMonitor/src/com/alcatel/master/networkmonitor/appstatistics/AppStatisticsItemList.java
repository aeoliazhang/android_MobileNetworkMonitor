package com.alcatel.master.networkmonitor.appstatistics;

import java.util.ArrayList;
import java.util.List;

import com.alcatel.master.networkmonitor.common.appStatisticsInfo;

public class AppStatisticsItemList {
	private static AppStatisticsItemList mInstance = null;
	private List<appStatisticsInfo> mAppStatisticsData = new ArrayList<appStatisticsInfo>();
	private int mlastUICount = 0;
	public static AppStatisticsItemList getInstance(){
		if(mInstance == null)
			mInstance = new AppStatisticsItemList();
		return mInstance;
	}
	
	public AppStatisticsItemList(){
		
	}
	
	public void setLastUIAppCount(int nCount){
		mlastUICount = nCount;
	}
	
	public int getLastUIAppCount(){
		return mlastUICount;
	}
	
	public List<appStatisticsInfo> getItemList(){
		return mAppStatisticsData;
	}
	
	public void setItemList(List<appStatisticsInfo> appDataList){
		mAppStatisticsData.clear();
		mAppStatisticsData.addAll(appDataList);
	}
}
