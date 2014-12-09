package com.alcatel.master.networkmonitor.ui;

import com.alcatel.master.networkmonitor.R;
import com.alcatel.master.networkmonitor.common.MonitorService;

import android.os.Bundle;
import android.os.IBinder;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

@TargetApi(11)
public class MainActivity extends Activity implements OnClickListener{

	TextView m_FluxButton;
	TextView m_AppListButton;
	TextView m_FirewallButton;
	TextView m_TitleView;
	
	private MonitorService m_monitorService;
	
	private int m_preButton = 0;
	//mzheng add
	@Override
	public void onDestroy(){
		super.onDestroy();
		WindowManager winManager = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		if(((NetworkMonitorApp)getApplication()).m_floatLayout != null){
			winManager.removeView(((NetworkMonitorApp)getApplication()).m_floatLayout);
			((NetworkMonitorApp)getApplication()).m_floatLayout = null;
		}		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		initMainView();
		
		bindService(new Intent(getApplicationContext(),MonitorService.class)
		,m_serviceConnect,BIND_AUTO_CREATE);
		//Intent intent = new Intent(MainActivity.this, MonitorService.class);
		//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		//startService(intent);               
	}

	ServiceConnection m_serviceConnect = new ServiceConnection(){
		public void onServiceConnected(ComponentName name, IBinder binder) {			
			m_monitorService = ((MonitorService.MyServiceBinder)binder).getService();
			((NetworkMonitorApp)getApplication()).m_monitorService = m_monitorService;		
		}
    	
    	public void onServiceDisconnected(ComponentName name){			
		} 
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	protected void onStart()
	{
		((NetworkMonitorApp)getApplication()).m_activityMain = this;//mzheng
		super.onStart();
	}
	
	//mzheng
	public void finish() {
	    super.finish();		
	    moveTaskToBack(true);
	} 

	protected void showSettingActivity(){
		Intent intentSettingBlock = new Intent(this,Setting.class);
		startActivity(intentSettingBlock);
	}
	protected void initMainView()
	{
		m_TitleView = (TextView)findViewById(R.id.textView_title);
		m_FluxButton = (TextView)findViewById(R.id.textView_flux);
		m_AppListButton = (TextView)findViewById(R.id.textView_statistics);
		m_FirewallButton = (TextView)findViewById(R.id.textView_fire_wall);
		m_FluxButton.setOnClickListener(this);
		m_AppListButton.setOnClickListener(this);
		m_FirewallButton.setOnClickListener(this);
		Button btnSetting = (Button)findViewById(R.id.button_setting);
		btnSetting.setOnClickListener(this);
		
		fluxBtnClick();
	}
	
	private void setMainBtnStatus(int nActiveBtnId) {
		m_preButton = nActiveBtnId;
		int nDrawable = nActiveBtnId == R.id.textView_flux?
				R.drawable.traffic_main_unchecked_pressed:R.drawable.traffic_main_unchecked;
		Drawable d = getResources().getDrawable(nDrawable);
		d.setBounds(0,0,d.getMinimumWidth(),d.getMinimumHeight());
		m_FluxButton.setCompoundDrawables(null, d, null, null);
		
		nDrawable = nActiveBtnId == R.id.textView_statistics?
				R.drawable.traffic_list_unchecked_pressed:R.drawable.traffic_list_unchecked;
		d = getResources().getDrawable(nDrawable);
		d.setBounds(0,0,d.getMinimumWidth(),d.getMinimumHeight());
		m_AppListButton.setCompoundDrawables(null, d, null, null);	
		
		nDrawable = nActiveBtnId == R.id.textView_fire_wall?
				R.drawable.traffic_tab_firewall_unchecked_pressed:R.drawable.traffic_tab_firewall_unchecked;
		d = getResources().getDrawable(nDrawable);
		d.setBounds(0,0,d.getMinimumWidth(),d.getMinimumHeight());
		m_FirewallButton.setCompoundDrawables(null, d, null, null);	
	}
	
	private void replaceFragment(Fragment fragment) {
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.containerBody,fragment);
		fragmentTransaction.commit();
	}
	
	private void fluxBtnClick() {
		if(m_preButton == R.id.textView_flux) {
			return;
		}

		setMainBtnStatus(R.id.textView_flux);
		
		m_TitleView.setText(R.string.flux_monitor);
		NetworkMonitorFragment fragment =new NetworkMonitorFragment();
		replaceFragment(fragment);
	}
	
	private void statisticsBtnClick() {
		if(m_preButton == R.id.textView_statistics) {
			return;
		}
		setMainBtnStatus(R.id.textView_statistics);
		
		m_TitleView.setText(R.string.statistic_list);
		NetworkFlusListFragment fragment =new NetworkFlusListFragment();
		replaceFragment(fragment);
	}
	
	private void firawallNetworkBtnClick() {
		if(m_preButton == R.id.textView_fire_wall) {
			return;
		}
		setMainBtnStatus(R.id.textView_fire_wall);
		
		m_TitleView.setText(R.string.fire_wall);
		NetworkFirewallFragment fragment =new NetworkFirewallFragment();
		replaceFragment(fragment);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
    	case R.id.textView_flux:
    		fluxBtnClick();
    		break;
    	case R.id.textView_fire_wall:
    		firawallNetworkBtnClick();
    		break;
    	case R.id.textView_statistics:
    		statisticsBtnClick();
    		break;
    	case R.id.button_setting:
    		showSettingActivity();
    		break;
    	}
		
	}
}
