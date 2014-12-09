package com.alcatel.master.networkmonitor.firewall;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FirewallDatabaseHelper extends SQLiteOpenHelper {
	
	private static final String FIREWALL_DB_NAME 	= "firewall.db";
	private static final int    FIREWALL_DB_VERSION = 1;
	
	public static final String  APP_TABLE 		= "app";	
	
	//app table column
	public static final int  APP_TABLE_PKGNAME	= 0;
	public static final int  APP_TABLE_MOBILE 	= 1;	
	public static final int  APP_TABLE_WIFI 	= 2;	
	
	
	private Context mContext;
	
	public FirewallDatabaseHelper(Context context) {
		super(context, FIREWALL_DB_NAME, null, FIREWALL_DB_VERSION);	
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createFirewallAppTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String path = mContext.getFilesDir().getAbsolutePath() + "/databases/"+FIREWALL_DB_NAME;
		File file = new File(path);
		file.delete();
		onCreate(db);		
	}	
	
	private void  createFirewallAppTable(SQLiteDatabase db) {
		String sql = "CREATE TABLE "+ APP_TABLE+"(pkg TEXT PRIMARY KEY NOT NULL UNIQUE, mobile INTEGER, wifi INTEGER)";			
		db.execSQL(sql);		
	}
}