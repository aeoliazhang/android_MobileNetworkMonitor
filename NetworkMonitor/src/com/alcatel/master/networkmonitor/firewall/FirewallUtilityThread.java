package com.alcatel.master.networkmonitor.firewall;

import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;


@SuppressLint("HandlerLeak")
public class FirewallUtilityThread extends Thread {

	public Handler m_firewallHandler;
	public Handler m_uiHandler;

	public FirewallUtilityThread(Handler handler) {
		m_uiHandler = handler;
	}
	
	@Override
	public void run() {

		Looper.prepare();

		m_firewallHandler = new Handler() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {

				case FirewallMessage.MSG_ROOT_CHECK: {
					Message msgIsRoot = new Message();
					msgIsRoot.what = FirewallMessage.MSG_ROOT_CHECK;
					msgIsRoot.obj = FirewallUtility.isRooted();
					m_uiHandler.sendMessage(msgIsRoot);
					break;
				}
					
				case FirewallMessage.MSG_ROOT_EXCU: {
					Message msgExcu = new Message();
					msgExcu.what = FirewallMessage.MSG_ROOT_EXCU;	
					List<Map<String, Object>> list = (List<Map<String, Object>>) msg.obj;					
					msgExcu.obj = FirewallUtility.SetFirewall(list);
					m_uiHandler.sendMessage(msgExcu);
					break;	
				}

				default:
					break;

				}
			}
		};

		Looper.loop();
	}
}
