
package com.alcatel.master.networkmonitor.ui;
import java.math.BigDecimal;
import java.util.List;
import java.util.Timer;
import com.alcatel.master.networkmonitor.R;
import com.alcatel.master.networkmonitor.common.MonitorService;
import android.os.Handler;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.*;
public class NetworkFloatView extends LinearLayout{
	private float m_fTouchStartX;
	private float m_fTouchStartY;
	private float m_mouseX;
	private float m_mouseY;
	private Handler handler = new Handler();
	
	private WindowManager m_wm =
			(WindowManager)getContext().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);  
	private WindowManager.LayoutParams wmParams = 
			((NetworkMonitorApp)getContext().getApplicationContext()).getNetAppWm(); 
	private MainActivity m_activityMain;
	
	public Timer m_timer;
	public int m_opType;
	
	public NetworkFloatView(Context context,MainActivity activity){
		super(context);
		this.m_activityMain = activity;	
		View view = View.inflate(context, R.layout.float_window, null);
		this.addView(view);	
		handler.post(thread);
	}		
	
	Runnable thread = new Runnable(){		
		@Override
		public void run(){
			TextView speedTextView = (TextView)findViewById(R.id.speed);	
			long speed = ((NetworkMonitorApp)getContext().getApplicationContext()).m_monitorService.getNetSpeed();			
			String strSpeed ;
			if(speed == 0){
				strSpeed = ((NetworkMonitorApp)getContext().getApplicationContext()).m_monitorService.getMonthUsage() + " M";
			}else				
				strSpeed = ConvertTrafficToString(speed);			
			speedTextView.setText(strSpeed);
			handler.postDelayed(thread, 500);
		}
	};
	
	public static String ConvertTrafficToString(long traffic){
		BigDecimal trafficKB;
		BigDecimal trafficMB;
		BigDecimal trafficGB;
		
		BigDecimal temp = new BigDecimal(traffic);
		BigDecimal divide = new BigDecimal(1024);
		if(temp.compareTo(divide) > 0){			
			trafficKB = temp.divide(divide, 2, 1);
			if(trafficKB.compareTo(divide) > 0){
				trafficMB = trafficKB.divide(divide,2,1);
				if(trafficMB.compareTo(divide) > 0){
					trafficGB = trafficMB.divide(divide,2,1);
					return trafficGB.doubleValue() + " GB/s";
				}else{
					return trafficMB.doubleValue() + " MB/s";
				}
			}else{
				return trafficKB.doubleValue() + " KB/s";
			}
		}else{
			return temp.doubleValue() + " B/s";
		}
		
	}
		
	@Override  
	public boolean onTouchEvent(MotionEvent event) {  
		m_mouseX = event.getRawX();     
		m_mouseY = event.getRawY()-((NetworkMonitorApp)getContext().getApplicationContext()).m_statusHeight;   	
		switch (event.getAction()) {  
			case MotionEvent.ACTION_DOWN:  		
				m_fTouchStartX =  event.getX();    
				m_fTouchStartY =  event.getY();				
				m_opType = MotionEvent.ACTION_DOWN;	
				showActivity();
				break;
				
			case MotionEvent.ACTION_MOVE:               
				m_opType = MotionEvent.ACTION_MOVE;
			    updateViewPosition();  
			    break;  
				
			case MotionEvent.ACTION_UP:  
				m_opType = MotionEvent.ACTION_UP;
				updateViewPosition();  
				m_fTouchStartX=0;
				m_fTouchStartY=0;  
			    break;  
		 }
		 return true;
	}

	private void updateViewPosition(){		 
		wmParams.x=(int)(m_mouseX-m_fTouchStartX);  
		wmParams.y=(int)(m_mouseY-m_fTouchStartY); 			
		int nScreenWidth = this.getResources().getDisplayMetrics().widthPixels;
		int nScreenHeight = this.getResources().getDisplayMetrics().heightPixels;		
		int nRightX = wmParams.x + wmParams.width/2; 
		int nButtomY = wmParams.y + wmParams.height 
				+ ((NetworkMonitorApp)getContext().getApplicationContext()).m_statusHeight;
		if((wmParams.x >= 0 && nRightX <= nScreenWidth)&&
				(wmParams.y >= 0 && nButtomY <= nScreenHeight))		
			m_wm.updateViewLayout(this, wmParams);      
	}
	
	@SuppressLint("NewApi")
	private void showActivity(){
		ActivityManager am =
				(ActivityManager)getContext().getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> info = am.getRunningTasks(5); 
		for(int i = 0 ; i < info.size() ; i++){
			ComponentName name = info.get(i).baseActivity;
			if(name == null){
				continue;				
			}else if(name.getPackageName().equals("com.alcatel.master.networkmonitor")){
				int id = info.get(i).id;
				am.moveTaskToFront(id,0);
				break;
			}
		}		
	}
	
	protected void updateUI(){
		
	}
}
