
package com.alcatel.master.networkmonitor.common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UsageDatabaseAdapter {
	
	private SQLiteDatabase m_SQLiteDatabase = null;	
	private Context m_context = null;	
	private DatabaseHelper m_DatabaseHelper = null;
	
	private static String DATABASE_NAME = "Usage.db";	
	private static String TABLE_NAME = "Usage";
	private final static String TABLE_ID = "FlowID";
	private final static String TABLE_UPF = "UpFlow";
	private final static String TABLE_DPF = "DownFlow";
	private final static String TABLE_TIME = "Time";
	private final static String TABLE_WEB = "WebType";
	private static int DB_VERSION = 1;
	private final static String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME 
			+ " (" + TABLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ TABLE_UPF + " Long," + TABLE_DPF + " Long,"
			+ TABLE_WEB + " INTEGER," + TABLE_TIME + " DATE)";
	
	private static class DatabaseHelper extends SQLiteOpenHelper{
		DatabaseHelper(Context context){
			super(context,DATABASE_NAME,null,DB_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db){
			db.execSQL(CREATE_TABLE);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
			db.execSQL("DROP TABLE IF EXISTS notes");
			onCreate(db);
		}
	}
	
	public UsageDatabaseAdapter(Context context){
		m_context = context;				
	}
	
	public void open() throws SQLException{
		m_DatabaseHelper = new DatabaseHelper(m_context);
		m_SQLiteDatabase = m_DatabaseHelper.getWritableDatabase();
	}
	
	public void close(){
		m_DatabaseHelper.close();
	}
	
	public void insertData(long upFlow,long downFlow,int webType,Date date){		
		SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd");
		String dataString = time.format(date);
		String insertData = " INSERT INTO " + TABLE_NAME + " ("
		+ TABLE_UPF + ", " + TABLE_DPF + "," + TABLE_WEB + ","
				+ TABLE_TIME + " ) values(" + upFlow + ", "
				+ downFlow + "," + webType + "," + "datetime('" + dataString
				+ "'));";
		m_SQLiteDatabase.execSQL(insertData);		
	}
	
	public void updateData(long upFlow,long downFlow,int webType,Date date){
		SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = time.format(date);
		String sqlUpdateString = " UPDATE " + TABLE_NAME + " SET " + TABLE_UPF
				+ "=" + upFlow + " , " + TABLE_DPF + "=" + downFlow
				+ " WHERE " + TABLE_WEB + "=" + webType + " and "
				+ TABLE_TIME + " like '" + dateString + "%'"; 
		m_SQLiteDatabase.execSQL(sqlUpdateString);			
	}
	
	public Cursor Check(int netType,Date date){
		SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = time.format(date);
		Cursor cursor = m_SQLiteDatabase.query(TABLE_NAME, new String[]{
				TABLE_UPF + " As upPro",TABLE_DPF + " As dwPro"},
				TABLE_WEB + "=" + netType + " and " + TABLE_TIME + " like '"
				+ dateString + "%'",null,null,null,null,null);
		return cursor;			
	}

/****************** Day up/down/All day Flow Statistics ******************/	
	public Cursor fetchDayFlow(int year,int month,int day,int netType){
		StringBuffer date = new StringBuffer();
		date.append(String.valueOf(year) + "-");
		if(month < 10){
			date.append("0" + String.valueOf(month) + "-");
		}else{
			date.append(String.valueOf(month) + "-");
		}
		
		if(day < 10){
			date.append("0" + String.valueOf(day));
		}else{
			date.append(String.valueOf(day));
		}
		
		Cursor cursor = m_SQLiteDatabase.query(TABLE_NAME,new String[]{
				"sum(" + TABLE_UPF + ") As sumUp",
				"sum(" + TABLE_DPF + ") As sumDw"},
				TABLE_WEB + "=" + netType + " and "
				+ TABLE_TIME + " like '" + date.toString() + "%'", 
				null, null, null, null, null);
		return cursor;			
	}	
	
	public long getProFlowUp(int netType,Date date){
		Cursor cursor = Check(netType,date);
		long upFlow = 0;
		if(cursor.moveToNext()){
			int up = cursor.getColumnIndex("upPro");
			upFlow = cursor.getLong(up);
		}	
		cursor.close();
		return upFlow;
	}
	
	public long getProFlowDown(int netType,Date date){
		Cursor cursor = Check(netType,date);
		long downFlow = 0;
		if(cursor.moveToNext()){
			int down = cursor.getColumnIndex("dwPro");
			downFlow = cursor.getLong(down);
		}
		cursor.close();
		return downFlow;
	}
	
	public long caculateDayFlow(int year,int month,int day,int netType){
		Cursor cursor = fetchDayFlow(year,month,day,netType);
		long sum = 0;
		if(cursor != null){
			if(cursor.moveToFirst()){
				do{
					int upColum = cursor.getColumnIndex("sumUp");
					int dwColum = cursor.getColumnIndex("sumDw");
					sum = cursor.getLong(upColum) + cursor.getLong(dwColum);
				}while(cursor.moveToNext());				
			}
		}
		cursor.close();
		return sum;
	}
	
	public long calculateDayFlowUp(int year, int month, int day, int netType) {
		Cursor calCurso = fetchDayFlow(year, month, day, netType);
		long sum = 0;
		if (calCurso != null) {
			if (calCurso.moveToFirst()) {
				do {
					int upColumn = calCurso.getColumnIndex("sumUp");
					sum = calCurso.getLong(upColumn);
				} while (calCurso.moveToNext());
			}
		}
		calCurso.close();
		return sum;
	}
	
	public long calculateDayFlowDw(int year, int month, int day, int netType) {
		Cursor calCurso = fetchDayFlow(year, month, day, netType);
		long sum = 0;
		if (calCurso != null) {
			if (calCurso.moveToFirst()) {
				do {
					int dwColumn = calCurso.getColumnIndex("sumDw");
					sum = calCurso.getLong(dwColumn);
				} while (calCurso.moveToNext());
			}
		}
		calCurso.close();
		return sum;
	}
	
	
/****************** Week up/down/One Week Flow Statistics ******************/	
	public long weekUpFlow(int netType){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
		long weekFlowUp = 0;
		for(int i = 0 ; i < 7 ; i++){
			int y = calendar.get(Calendar.YEAR);
			int m = calendar.get(Calendar.MONTH) + 1;
			int d = calendar.get(Calendar.DAY_OF_MONTH);
			weekFlowUp += calculateDayFlowUp(y,m,d,netType);
			calendar.add(Calendar.DAY_OF_WEEK, 1);
		}
		return weekFlowUp;				
	}
	
	public long weekDwFlow(int netType){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
		long weekFlowDw = 0;
		for(int i = 0 ; i < 7 ; i++){
			int y = calendar.get(Calendar.YEAR);
			int m = calendar.get(Calendar.MONTH) + 1;
			int d = calendar.get(Calendar.DAY_OF_MONTH);
			weekFlowDw += calculateDayFlowDw(y,m,d,netType);
			calendar.add(Calendar.DAY_OF_WEEK, 1);
		}
		return weekFlowDw;
	}
	
/****************** Month up/down/One Month Flow Statistics ******************/	
	public Cursor fetchmonthFlow(int year,int month,int netType){
		StringBuffer date = new StringBuffer();
		date.append(String.valueOf(year) + "-");
		if(month < 10){
			date.append("0" + String.valueOf(month) + "-");
		}else{
			date.append(String.valueOf(month) + "-");
		}			
		
		Cursor cursor = m_SQLiteDatabase.query(TABLE_NAME,new String[]{
				"sum(" + TABLE_UPF + ") As monthUp",
				"sum(" + TABLE_DPF + ") As monthDw"},
				TABLE_WEB + "=" + netType + " and "
				+ TABLE_TIME + " like '" + date.toString() + "%'", 
				null, null, null, null, null);
		return cursor;
	}
	
	public long calculateMonthFlowUp(int year,int month,int netType){
		Cursor cursor = fetchmonthFlow(year,month,netType);
		long monthUp = 0;
		if(cursor != null){
			if(cursor.moveToFirst()){
				do{
					int upColumn = cursor.getColumnIndex("monthUp");
					monthUp += cursor.getLong(upColumn);
				}while(cursor.moveToNext());
			}
		}
		return monthUp;
	}
	
	public long calculateMonthFlowDw(int year,int month,int netType){
		Cursor cursor = fetchmonthFlow(year,month,netType);
		long monthDw = 0;
		if(cursor != null){
			if(cursor.moveToFirst()){
				do{
					int dwColumn = cursor.getColumnIndex("monthDw");
					monthDw += cursor.getLong(dwColumn);
				}while(cursor.moveToNext());
			}
		}
		return monthDw;
	}
	
	public long calculateMonthFlow(int year,int month,int netType){
		Cursor cursor = fetchmonthFlow(year,month,netType);
		long monthFlow = 0;
		if(cursor != null){
			if(cursor.moveToFirst()){
				do{
					int upColumn = cursor.getColumnIndex("monthUp");
					int dwColumn = cursor.getColumnIndex("monthDw");
					monthFlow += cursor.getLong(upColumn) + cursor.getLong(dwColumn);
				}while(cursor.moveToNext());
			}
		}
		return monthFlow;
	}
	
	public void deleteAll(){
		m_SQLiteDatabase.execSQL("DROP TABLE" + TABLE_NAME);
	}
	
	public void clear(){
		m_SQLiteDatabase.delete(TABLE_NAME, null, null);
	}
		
	public List<DayUsageItem> getMobileDaysUsageList(Date startDate,Date endDate){
		List<DayUsageItem> dayUsageList = new ArrayList<DayUsageItem>();
		
		Calendar curCalendar = Calendar.getInstance();  
		SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd");
		String curDateString = time.format(curCalendar.getTime());
		
		Calendar calendar = Calendar.getInstance();		
		calendar.setTime(startDate);
		
		/*
		int daysofMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		int nowDay = curCalendar.get(Calendar.DATE);
		int nowMonth = curCalendar.get(Calendar.MONTH) + 1;
		int nowYear = curCalendar.get(Calendar.YEAR);
		int startMonth = nowMonth,startYear = nowYear;
		if(startDay > nowDay){
			if(nowMonth == 1){
				startMonth = 12;
				startYear = nowYear - 1;
			}else
				startMonth -= 1;
		}
		calendar.set(Calendar.YEAR, startYear);
		calendar.set(Calendar.MONTH, startMonth - 1);
		calendar.set(Calendar.DATE, startDay);
		*/
		long daysFlowDw = 0,daysFlowUp = 0,daysFlow = 0;
		//for(int i = 0 ; i < daysofMonth ; i++){
		while(true){
			DayUsageItem item = new DayUsageItem();
			int y = calendar.get(Calendar.YEAR);
			int m = calendar.get(Calendar.MONTH) + 1;
			int d = calendar.get(Calendar.DAY_OF_MONTH);
			daysFlowDw = calculateDayFlowDw(y,m,d,1);
			daysFlowUp = calculateDayFlowUp(y, m, d, 1);
			daysFlow = caculateDayFlow(y, m, d, 1);
			Date date = calendar.getTime();			
			
			item.date = date;
			item.dayMobileRx = daysFlowDw;
			item.dayMobileTx = daysFlowUp;
			item.dayUsage = daysFlow;					
			
			dayUsageList.add(item);

			SimpleDateFormat time_1 = new SimpleDateFormat("yyyy-MM-dd");
			String dateString = time_1.format(date);
			
			SimpleDateFormat time_2 = new SimpleDateFormat("yyyy-MM-dd");
			String endDateString = time_2.format(endDate);
			
			if(curDateString.compareToIgnoreCase(dateString) == 0 
					|| endDateString.compareToIgnoreCase(dateString) == 0)
			//if(date.equals(curCalendar.getTime()) || date.equals(endDate))
				break;
			else
				calendar.add(Calendar.DAY_OF_YEAR, 1);			
		}
		
		return dayUsageList;
	}
}
