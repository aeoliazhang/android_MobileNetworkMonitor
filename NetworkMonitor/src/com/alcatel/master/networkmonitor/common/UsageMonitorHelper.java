/**
 * 
 */
package com.alcatel.master.networkmonitor.common;

import android.net.TrafficStats;
import java.math.BigDecimal;
/**
 * @author mzheng
 *
 */
public class UsageMonitorHelper {
	
	public UsageMonitorHelper(){		
	}
	
	//Inquiry Phone's total flow
	public static long Traffic_Total(){
		long recive = TrafficStats.getTotalRxBytes();
		long send = TrafficStats.getTotalTxBytes();
		long total = recive + send;
		return total;
	}
	
	public static long Traffic_MRecive(){
		return TrafficStats.getMobileRxBytes();
	}
	
	public static long Traffic_MSend(){
		return TrafficStats.getMobileTxBytes();
	}
	
	public static long Traffic_WifiRecive(){
		return TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes();
	}
	
	public static long Traffic_WifiSend(){
		return TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes();
	}
	
	public static long Traffic_AppRecive(int uid){
		return TrafficStats.getUidRxBytes(uid);
	}
	
	public static long Traffic_AppSend(int uid){
		return TrafficStats.getUidTxBytes(uid);
	}
	
	public static String ConvertTrafficToString(long traffic){
		BigDecimal trafficKB;
		BigDecimal trafficMB;
		BigDecimal trafficGB;
		
		BigDecimal temp = new BigDecimal(traffic);
		BigDecimal divide = new BigDecimal(1024);
		trafficKB = temp.divide(divide, 2, 1);
		if(trafficKB.compareTo(divide) > 0){
			trafficMB = trafficKB.divide(divide,2,1);
			if(trafficMB.compareTo(divide) > 0){
				trafficGB = trafficMB.divide(divide,2,1);
				return trafficGB.doubleValue() + " GB";
			}else{
				return trafficMB.doubleValue() + " MB";
			}
		}else{
			return trafficKB.doubleValue() + " KB";
		}
	}
}
