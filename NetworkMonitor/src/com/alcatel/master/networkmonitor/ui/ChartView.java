package com.alcatel.master.networkmonitor.ui;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.alcatel.master.networkmonitor.common.DayUsageItem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class ChartView extends View {
	private int m_nViewHeight = 0;
	private Calendar m_caStart = Calendar.getInstance();
    private Calendar m_caEnd = Calendar.getInstance();
    private float m_maxValue = 0;
	
	Paint mPaint;
	Paint mPaintForData;
	Paint mDatePaint;
	ChartData mData = new ChartData();
	SimpleDateFormat mFormat = new SimpleDateFormat("MM.dd");

	public ChartView(Context context) {
		super(context);
		init();	
	}
	
	public ChartView(Context context, AttributeSet attrs){
        super(context, attrs);
        init();	
    }

    public ChartView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        init();	
    }
    
    public void setStartAndEndDate(Calendar caStart,Calendar caEnd) {
    	m_caStart.setTime(caStart.getTime()); 
    	m_caEnd.setTime(caEnd.getTime());
    }

	
	protected void init()
	{
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setARGB(255, 204, 204, 204);
		mPaint.setTextAlign(Paint.Align.CENTER);
		
		mDatePaint = new Paint();
		mDatePaint.setStyle(Paint.Style.STROKE);
		mDatePaint.setARGB(255, 57, 190, 0);
		mDatePaint.setTextAlign(Paint.Align.CENTER);
		
		
		mPaintForData = new Paint();
		mPaintForData.setARGB(200, 10, 200, 10);
		mPaintForData.setStyle(Paint.Style.FILL);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
	
	@Override
	protected void onSizeChanged(int w,int h,int oldw,int oldh) {
		m_nViewHeight = h;
		//m_nViewWidth = mData.mScaleWidth*(mData.mScaleHCount+1);
		super.onSizeChanged(w, m_nViewHeight, oldw, oldh);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
		ViewGroup.LayoutParams lp = this.getLayoutParams();
		lp.width = mData.mScaleWidth*(mData.mScaleVCount+1);
		lp.height = m_nViewHeight;
		setLayoutParams(lp);
		
		int totalHeight = m_nViewHeight;
		int textHeight = mData.mTextHeight;
		int bottomHeight = (int)((float)textHeight * 1.5);
		bottomHeight += (totalHeight-bottomHeight)%4;
		int scaleHeight = (totalHeight - bottomHeight)/4;
		
		int scaleWidth = mData.mScaleWidth;
		int scaleHCount = mData.mScaleHCount;
		int totalWidth = scaleWidth*(mData.mScaleVCount+1);
		int scaleVCount = mData.mScaleVCount;
		
		
        
		for(int i = 1;i<=scaleVCount;++i)
		{
			canvas.drawLine((float)scaleWidth*i, (float)0, (float)scaleWidth*i, (float)totalHeight - bottomHeight, mPaint);
			Date date = mData.mDateLst.elementAt(i-1);
			String str = mFormat.format(date);
			canvas.drawText(str, (float)scaleWidth*i, (float)(totalHeight-4), mDatePaint);	
		}
		
		for(int i = 1;i<=scaleHCount;++i)
		{
			if(scaleVCount == 0)
			{
				break;
			}
			canvas.drawLine((float)0, (float)scaleHeight*i, (float)totalWidth, (float)scaleHeight*i, mPaint);
		}
		
		calMaxValue();
		
		float bot = totalHeight - bottomHeight;
		float width = scaleWidth / 5;
		for(int i = 0;i < scaleVCount;++i)
		{
			//if(mData.mDatas[i] <= 0)
			//	continue;
			float top = 0;
			if(i < mData.mDatas.size())
				top = bot * (m_maxValue - mData.mDatas.get(i).dayUsage) / m_maxValue;			
			else
				top = bot;
			float left = scaleWidth * (i+1) - width/2;
			float right = scaleWidth * (i+1) + width/2;
			canvas.drawRect(left, top, right, bot, mPaintForData);
			if(isShowDataText(i)) {
				String strData = formatData(mData.mDatas.get(i).dayUsage);
				//canvas.drawText(String.format("%.2f%s", mData.mDatas[i],"MB"), (float)scaleWidth*(i+1), top, mPaint);
				canvas.drawText(strData, (float)scaleWidth*(i+1), top, mPaint);
			}
		}	
	}
	
	private String formatData(long lData)
	{
		double baseB = 1.00;
		double baseKB = 1024.00;
		double baseMB = 1024 * 1024.00;
		double baseGB = 1024 * 1024 * 1024.00;
		
		String strSize = "";
		if(lData < baseKB){
			DecimalFormat df = new DecimalFormat("0.##B");
			strSize = df.format(lData/baseB).toString();
		}else if(lData >= baseKB && lData < baseMB){
			DecimalFormat df = new DecimalFormat("0.##K");
			strSize = df.format(lData/baseKB).toString();
		}else if(lData >= baseMB && lData < baseGB){
			DecimalFormat df = new DecimalFormat("0.##M");
			strSize = df.format(lData/baseMB).toString();
		}else{
			DecimalFormat df = new DecimalFormat("0.##G");
			strSize = df.format(lData/baseGB).toString();
		}
		
		return strSize;
	} 


	
	private boolean isShowDataText(int index) {
		Calendar ca = Calendar.getInstance();
		Calendar caNow = Calendar.getInstance();
		ca.setTime(m_caStart.getTime());
		ca.add(Calendar.DATE, index);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date dateNow = caNow.getTime();
		String strNow = dateFormat.format(dateNow);
		Date dateCa = ca.getTime();
		String strCa = dateFormat.format(dateCa);
		if(strCa.compareToIgnoreCase(strNow) <= 0) {
			return true;
		}
		
		return false;
	}
	
	private void calMaxValue() {
		m_maxValue = 0.0f;
		for(int i = 0; mData.mDatas != null && i < mData.mDatas.size();i++) {
			if(m_maxValue < mData.mDatas.get(i).dayUsage) {
				m_maxValue = mData.mDatas.get(i).dayUsage;
			}
		}
		m_maxValue = m_maxValue * 1.2f;
	}
	
	public void setData(ChartData data) {
		mData = data;
	}
	
	public class ChartData// extends Object
	{
		public ChartData()
		{
		}
		int mTextHeight = 10;
		int mScaleWidth = 40;
		int mScaleVCount = 0;
		int mScaleHCount = 4;
		public Vector<Date> mDateLst = new Vector<Date>();
		List<DayUsageItem> mDatas;

		public void calDate() {	  
			Calendar caStart = Calendar.getInstance();
		    Calendar caEnd = Calendar.getInstance();
		    caStart.setTime(m_caStart.getTime());
		    caEnd.setTime(m_caEnd.getTime());
	
		    mDateLst.clear();
		    long lEnd = caEnd.getTimeInMillis();
		    while(true) {
		    	long lStart = caStart.getTimeInMillis();
		    	if(lStart <= lEnd) {
		    		mDateLst.add(caStart.getTime());
		    	}else{
		    		break;
		    	}
		    	caStart.add(Calendar.DATE, 1);
		    }
		    mScaleVCount = mDateLst.size();
		}
	};
}
