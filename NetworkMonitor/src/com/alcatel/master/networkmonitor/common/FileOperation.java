
package com.alcatel.master.networkmonitor.common;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class FileOperation {
	Context m_context;
	private InputStream m_is;
	private OutputStream m_os;
	
	private String Text_of_output;
	private String Text_of_input;
	private String m_file;
	
	public FileOperation(Context context){
		m_context = context;
	}
	
	public String getFileContent(String fileName){
		m_os = null;
		m_file = fileName;
		try{
			m_os = m_context.openFileOutput(m_file,Context.MODE_APPEND);
		}catch(FileNotFoundException e){
			
		}catch(IOException e){
			
		}finally{
			try{
				m_os.close();
			}catch(IOException e){
				
			}
		}
		
		m_is = null;
		try{
			m_is = m_context.openFileInput(m_file);
			byte[] readbytes = new byte[m_is.available()];
			int length = m_is.read(readbytes);
			Text_of_output = new String(readbytes);
			return Text_of_output;
		}catch(FileNotFoundException e){
			return null;
		}catch(IOException e){
			return null;
		}finally{
			try{
				m_is.close();
			}catch(IOException e){
				return null;
			}
		}
	}
	
	public void SaveFileContent(String fileName,String settingContent){
		m_os = null;
		m_file = fileName;
		try{
			m_os = m_context.openFileOutput(m_file, Context.MODE_PRIVATE);
			Log.v("file_context",m_context.toString());
			Text_of_input = settingContent;
			m_os.write(Text_of_input.getBytes());
		}catch(FileNotFoundException e){
			//NoteDebug("文件打开失败" + e);
		}catch(IOException e){
			//NoteDebug("文件写入失败" + e);
		}finally{
			try{
				m_os.close();
				//NoteDebug("设置成功");
			}catch(IOException e){
				//NoteDebug("文件关闭失败" + e);
			}
		}
	}
	
	private void NoteDebug(String showString) {		
		Toast.makeText(m_context, showString, Toast.LENGTH_SHORT).show();
	}
}
