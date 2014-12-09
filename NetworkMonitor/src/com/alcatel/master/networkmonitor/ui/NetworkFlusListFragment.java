package com.alcatel.master.networkmonitor.ui;

import java.util.ArrayList;
import java.util.List;

import com.alcatel.master.networkmonitor.appstatistics.AppStatisticsItemList;
import com.alcatel.master.networkmonitor.common.*;
import com.alcatel.master.networkmonitor.R;

import android.app.Fragment;

import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.TargetApi;
import android.content.pm.*;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


@TargetApi(11)
public class NetworkFlusListFragment extends Fragment {
	
	private ArrayList<appStatisticsInfo> m_AppList = new ArrayList<appStatisticsInfo>();
	public View m_fragmentView = null;
	private ListView m_lstView;
	private static final int MSG_APP_STATISTIC_SCAN_FINISH = 0x0200;
	
	/*
	final Handler handler = new Handler(){
		public void handlerMessage(Message msg){
			switch(msg.what){
			case MSG_APP_STATISTIC_SCAN_FINISH:{
					AppStatisticsItemList appStatisticsList 
						= AppStatisticsItemList.getInstance();
					appStatisticsList.setItemList(m_AppList);
					showListview();
					break;
				}						
			default:
				break;
			}
		}
	};
	*/
	
	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_APP_STATISTIC_SCAN_FINISH: {
				AppStatisticsItemList appStatisticsList = AppStatisticsItemList.getInstance();
				appStatisticsList.setItemList(m_AppList);
				showListview();
				break;
			}

			default:
				break;
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);			
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);		
		m_fragmentView = inflater.inflate(R.layout.app_flux_list,container,false);				
		initAppListView();
		initAppStatisticsInfoDataList();
		//getList();
		return m_fragmentView;
	}	
	
	public void clearListView(){
		if(m_lstView != null){
			m_AppList.clear();
			((AppAdapter)m_lstView.getAdapter()).notifyDataSetChanged();
		}
	}
	
	public void initAppListView(){	
		m_lstView = (ListView)m_fragmentView.findViewById(R.id.listview_statistics_app);
		AppAdapter appAdapter=new AppAdapter();
		m_lstView.setAdapter(appAdapter);			
	}
	
	public void initAppStatisticsInfoDataList(){
		m_AppList.clear();
		AppStatisticsItemList list = AppStatisticsItemList.getInstance();
		int nCount = list.getLastUIAppCount();
		m_AppList.addAll(list.getItemList());		
		if (m_AppList.isEmpty() || m_AppList.size() != nCount) {
			showLoadingview();
			new Thread() {
				public void run() {
					getList();
					Message msg = new Message();
					msg.what = MSG_APP_STATISTIC_SCAN_FINISH;
					handler.sendMessage(msg);
				}
			}.start();
		} else {
			showListview();
		}
	}
	
	public void getList(){	
		m_AppList.clear();
        int nAppCount = 0;
		List<ApplicationInfo> applications = getActivity().getPackageManager().getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES); 
        for(int i=0;i < applications.size() ;i++) { 
			ApplicationInfo application = applications.get(i); 			
			int uid = application.uid;    
			if (application.processName.equals("system")
				|| application.processName.equals("com.android.phone")) {
			     continue;
			}
			if(monitoringEachApplicationSend(uid)==0 && 
					  monitoringEachApplicationReceive(uid)==0){
				  continue;
			}
			
			nAppCount++;
			
			appStatisticsInfo appInfo = new appStatisticsInfo();

			String appLabel = "";
			if(getActivity() != null)
				appLabel = application.loadLabel(getActivity().getPackageManager()).toString();			
			if(getActivity() != null)
				appInfo.setIcon(application.loadIcon(getActivity().getPackageManager()));
			appInfo.setName(appLabel); 
			appInfo.setUid(uid);			
			appInfo.setReceive(monitoringEachApplicationReceive(uid));
			appInfo.setSend(monitoringEachApplicationSend( uid)) ;
			if(getActivity() != null)
				m_AppList.add(appInfo);						
		}
		
        AppStatisticsItemList list = AppStatisticsItemList.getInstance();
        list.setLastUIAppCount(nAppCount);
		//sort
		sortAppByTotalFlow();
		//adjustListViewHeight();
		//((AppAdapter)m_lstView.getAdapter()).notifyDataSetChanged();
	}
	
	public void sortAppByTotalFlow(){
		int nSize = m_AppList.size();
		for(int i = 0 ; i < nSize - 1 ; i++){			
			long total_1 = m_AppList.get(i).getReceive() + m_AppList.get(i).getSend();
			for(int j = i + 1 ; j < nSize; j++){
				long total_2 = m_AppList.get(j).getReceive() + m_AppList.get(j).getSend();
				if(total_1 < total_2){
					appStatisticsInfo app_2 = new appStatisticsInfo();
					app_2.setIcon(m_AppList.get(j).getIcon());
					app_2.setName(m_AppList.get(j).getName());
					app_2.setUid(m_AppList.get(j).getUid());
					app_2.setReceive(m_AppList.get(j).getReceive());
					app_2.setSend(m_AppList.get(j).getSend());
					app_2.setNetType(m_AppList.get(j).getNetType());
					app_2.setLinkDate(m_AppList.get(j).getLinkDate());
					m_AppList.get(j).setAll(m_AppList.get(i));	
					m_AppList.get(i).setAll(app_2);
					
					total_1 = m_AppList.get(i).getReceive() + m_AppList.get(i).getSend();
				}				
			}			
		}
	}
	
	public void adjustListViewHeight(){
		int itemHeight = 0;
		int totalHeight = 0;
		AppAdapter adapter = (AppAdapter)m_lstView.getAdapter();
		View listItem = adapter.getView(0, null, m_lstView);    
	    listItem.measure(0, 0); //计算子项View 的宽高    
	    itemHeight = listItem.getMeasuredHeight();
	    totalHeight = itemHeight * m_AppList.size()
	    		+ m_lstView.getDividerHeight() * (m_AppList.size() - 1);
	    ViewGroup.LayoutParams params = m_lstView.getLayoutParams();
	    params.height = totalHeight;
	    m_lstView.setLayoutParams(params);
	}
	
	public static long monitoringEachApplicationReceive(int uid) {
		long   receive=TrafficStats.getUidRxBytes(uid);
		if(receive==-1)receive=0;
	  return receive;
	}
	
    public static long monitoringEachApplicationSend(int uid) {
    	long   send=TrafficStats.getUidTxBytes(uid);
		if(send==-1)send=0;
		return send;
	}
	
	public class AppAdapter extends BaseAdapter {    	
    	
		public int getCount() {
			// TODO Auto-generated method stub
			return m_AppList.size();
		}
		
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return m_AppList.get(position);
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;			
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {								
			//View v=convertView;			
			final appStatisticsInfo appUnit = m_AppList.get(position);
			if(convertView==null)
			{
        		//LayoutInflater vi=(LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        		convertView = LayoutInflater.from(getActivity().getApplicationContext()).inflate
					    (R.layout.statistics_applist_item,null);
				//v=vi.inflate(R.layout.statistics_applist_item, null);
			}
			
        	TextView appName=(TextView)convertView.findViewById(R.id.app_name);        	
        	TextView appSend=(TextView)convertView.findViewById(R.id.app_upflow_value);
        	TextView appRecieve=(TextView)convertView.findViewById(R.id.app_dwflow_value);
        	TextView appTotal=(TextView)convertView.findViewById(R.id.app_totalflow);
        	ImageView appIcon=(ImageView)convertView.findViewById(R.id.app_icon);
        	if(appName!=null){
        		//ViewGroup.LayoutParams params = appName.getLayoutParams();
        	    //params.width = m_lstView.getWidth()/2;
        	   // appName.setLayoutParams(params);
        		appName.setText(appUnit.getName());
        	}
        	if(appIcon!=null)
        		appIcon.setImageDrawable(appUnit.getIcon());
        	if(appSend!=null)        		
        		appSend.setText(UsageMonitorHelper.ConvertTrafficToString(appUnit.getSend()));        		
        	if(appRecieve!=null)
        		appRecieve.setText(UsageMonitorHelper.ConvertTrafficToString(appUnit.getReceive()));
        	if(appTotal!=null)
        		appTotal.setText(UsageMonitorHelper.ConvertTrafficToString(appUnit.getReceive()+appUnit.getSend()));
			return convertView;			
		}
    }
	
	private void showLoadingview() {
		Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
				m_fragmentView.getContext(), R.anim.firewall_loading_anim);
		ImageView loadingImage = (ImageView)m_fragmentView
				.findViewById(R.id.applist_loading_image);
		loadingImage.startAnimation(hyperspaceJumpAnimation);
	}
	
	private void showListview() {		
		((AppAdapter)m_lstView.getAdapter()).notifyDataSetChanged();
		this.m_fragmentView.findViewById(R.id.app_list_layout_loading)
				.setVisibility(View.INVISIBLE);
		this.m_fragmentView.findViewById(R.id.listview_statistics_app)
				.setVisibility(View.VISIBLE);
	}	
}
