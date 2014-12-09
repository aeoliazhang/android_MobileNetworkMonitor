package com.alcatel.master.networkmonitor.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
public class BootReceiver extends BroadcastReceiver{
	static final String ACTION = "android.intent.action.BOOT_COMPLETED";
	
	@Override
	public void onReceive(Context context, Intent intent){
		if ((intent.getAction().equals(ACTION))) {
			Intent monitorIntent = new Intent(context, MonitorService.class);
			monitorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			context.startService(monitorIntent);
		}
	}
	
	public boolean isStart(String s) {
		if (Integer.parseInt(s) == 1) {
			return true;
		} else {
			return false;
		}
	}
}
