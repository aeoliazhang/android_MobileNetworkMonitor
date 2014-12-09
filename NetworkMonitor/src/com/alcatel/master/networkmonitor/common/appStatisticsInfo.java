/**
 * 
 */
package com.alcatel.master.networkmonitor.common;
import java.util.Calendar;
import android.graphics.drawable.Drawable;

/**
 * @author mzheng
 *
 */
public class appStatisticsInfo {
	//ͼ��  
	private Drawable icon;
	
	//������  
	private String name;
	
	//����Uid
	private int uid;
	
	//��������ֵ
	private long send;
	
	//��������ֵ
	private long receive;
	
	//���������
	private String netType;
	
	//��������ʱ��
	private Calendar linkDate;
	
	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public long getSend() {
		return send;
	}

	public void setSend(long send) {
		this.send = send;
	}

	public long getReceive() {
		return receive;
	}

	public void setReceive(long receive) {
		this.receive = receive;
	}

	public String getNetType() {
		return netType;
	}

	public void setNetType(String netType) {
		this.netType = netType;
	}

	public Calendar getLinkDate() {
		return linkDate;
	}

	public void setLinkDate(Calendar linkDate) {
		this.linkDate = linkDate;
	}
	
	public void setAll(appStatisticsInfo appInfo){
		this.icon = appInfo.icon;		
		this.name = appInfo.name;		
		this.uid = appInfo.uid;		
		this.send = appInfo.send;		
		this.receive = appInfo.receive;		
		this.netType = appInfo.netType;		
		this.linkDate = appInfo.linkDate;
	}
}
