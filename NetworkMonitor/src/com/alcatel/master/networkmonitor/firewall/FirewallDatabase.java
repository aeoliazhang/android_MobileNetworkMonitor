package com.alcatel.master.networkmonitor.firewall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class FirewallDatabase {

	private FirewallDatabaseHelper m_Dbhelper = null;

	public FirewallDatabase(Context context) {
		m_Dbhelper = new FirewallDatabaseHelper(context);
	}

	public boolean setFireWallItemListData() {
		boolean bSuccess = false;
		SQLiteDatabase db = null;
		try {
			db = m_Dbhelper.getWritableDatabase();

			// clear
			String sql = String.format("delete from %s;",
					FirewallDatabaseHelper.APP_TABLE);
			db.execSQL(sql);

			// set data
			db.beginTransaction();
			try {
				List<Map<String, Object>> lstData = FirewallItemList
						.getInstance().getItemList();
				for (Map<String, Object> map : lstData) {
					if (map != null) {
						String pkgName = (String) map
								.get(FirewallItem.ITEM_PKG);
						boolean bMobileData = (Boolean) map
								.get(FirewallItem.ITEM_MOBILE_DATA);
						boolean bWifiData = (Boolean) map
								.get(FirewallItem.ITEM_WIFI_DATA);
						sql = String
								.format("insert into %s([pkg],[mobile],[wifi]) values('%s', '%d','%d');",
										FirewallDatabaseHelper.APP_TABLE,
										pkgName, boolean2int(bMobileData),
										boolean2int(bWifiData));
						db.execSQL(sql);

					}
				}

				db.setTransactionSuccessful();
				bSuccess = true;

			} catch (Exception e) {
				e.printStackTrace();
			}

			finally {

				db.endTransaction();
			}

		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.close();
			}
		}

		return bSuccess;
	}

	public List<Map<String, Object>> getFireWallItemListData() {

		List<Map<String, Object>> lstData = new ArrayList<Map<String, Object>>();

		SQLiteDatabase db = null;
		try {
			db = m_Dbhelper.getReadableDatabase();
			String strSql = "select * from " + FirewallDatabaseHelper.APP_TABLE;
			Cursor cursor = db.rawQuery(strSql, null);

			while (cursor != null && cursor.moveToNext()) {

				Map<String, Object> map = new HashMap<String, Object>();
				String strPkg = cursor
						.getString(FirewallDatabaseHelper.APP_TABLE_PKGNAME);

				int mobile = cursor
						.getInt(FirewallDatabaseHelper.APP_TABLE_MOBILE);

				int wifi = cursor.getInt(FirewallDatabaseHelper.APP_TABLE_WIFI);

				map.put(FirewallItem.ITEM_PKG, strPkg);
				map.put(FirewallItem.ITEM_MOBILE_DATA, int2boolean(mobile));
				map.put(FirewallItem.ITEM_WIFI_DATA, int2boolean(wifi));

				lstData.add(map);
			}
			cursor.close();

		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.close();
			}
		}
		return lstData;
	}

	private int boolean2int(boolean bool) {
		int nRes = 0;
		if (bool) {
			nRes = 1;
		}

		return nRes;
	}

	private boolean int2boolean(int number) {
		boolean bRes = false;
		if (number != 0) {
			bRes = true;
		}
		return bRes;
	}
}
