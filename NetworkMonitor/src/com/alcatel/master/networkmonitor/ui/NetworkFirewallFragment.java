package com.alcatel.master.networkmonitor.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.alcatel.master.networkmonitor.R;
import com.alcatel.master.networkmonitor.firewall.FirewallDatabase;
import com.alcatel.master.networkmonitor.firewall.FirewallItem;
import com.alcatel.master.networkmonitor.firewall.FirewallItemList;
import com.alcatel.master.networkmonitor.firewall.FirewallMessage;
import com.alcatel.master.networkmonitor.firewall.FirewallUtilityThread;
import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

@SuppressLint({ "HandlerLeak", "NewApi" })
public class NetworkFirewallFragment extends Fragment implements
		OnClickListener {

	public View m_fragmentView = null;
	private TextView m_mobileBtn;
	private TextView m_wifiBtn;
	private boolean m_bMobileDataOn = false;
	private boolean m_bWifiDataOn = false;
	private ListView m_listView;
	private List<Map<String, Object>> m_listItemData;
	private List<Map<String, Object>> m_listItemDataBeforeModify = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> m_savedData;
	private FirewallDatabase m_db;
	private boolean m_bInitializing = false;

	private boolean m_bRooted = false;
	private UiHandler m_uiHandler = new UiHandler();
	private FirewallUtilityThread m_firewallThread = new FirewallUtilityThread(
			m_uiHandler);
	private volatile boolean m_bDestroyed = false;

	// ---------------------
	// class UiHandler
	// ---------------------
	private class UiHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {

			case FirewallMessage.MSG_SCANAPP_FINISH: {
				m_bInitializing = false;
				showListview();
				break;
			}

			case FirewallMessage.MSG_ROOT_CHECK: {
				m_bRooted = (Boolean) msg.obj;
				FirewallItemList list = FirewallItemList.getInstance();
				list.m_bRooted = m_bRooted;
				break;
			}

			case FirewallMessage.MSG_ROOT_EXCU: {
				boolean bSuccess = (Boolean) msg.obj;
				if (!bSuccess) {
					loadDataBeforeModify();
					showSetFirewallError();
				}
				break;
			}
			default:
				break;
			}

		}
	}

	// ---------------------
	// class ViewHolder
	// ---------------------
	private class ViewHolder {
		public ImageView icon;
		public TextView lable;
		public ImageView mobile;
		public ImageView wifi;
	}

	// ---------------------
	// class FirewallListAdapter
	// ---------------------
	private class FirewallListAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public FirewallListAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return m_listItemData.size();
		}

		@Override
		public Object getItem(int arg0) {
			return arg0;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			if (convertView == null) {

				holder = new ViewHolder();

				convertView = mInflater.inflate(R.layout.firewall_item, null);
				holder.icon = (ImageView) convertView
						.findViewById(R.id.firewall_item_icon);
				holder.lable = (TextView) convertView
						.findViewById(R.id.firewall_item_label);
				holder.mobile = (ImageView) convertView
						.findViewById(R.id.firewall_item_mobile);
				holder.wifi = (ImageView) convertView
						.findViewById(R.id.firewall_item_wifi);
				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			//
			holder.icon.setImageDrawable((Drawable) m_listItemData
					.get(position).get(FirewallItem.ITEM_ICON));

			holder.lable.setText((String) m_listItemData.get(position).get(
					FirewallItem.ITEM_LABLE));

			//
			Drawable image = null;
			int nResId = 0;

			boolean bMobileData = (Boolean) m_listItemData.get(position).get(
					FirewallItem.ITEM_MOBILE_DATA);

			if (bMobileData) {
				image = getResources().getDrawable(R.drawable.mobile_data_on);
				nResId = R.drawable.firewall_left_allowed;

			} else {
				image = getResources().getDrawable(R.drawable.mobile_data_off);
				nResId = R.drawable.firewall_left_forbidden;

			}

			holder.mobile.setBackgroundResource(nResId);
			holder.mobile.setImageDrawable(image);

			boolean bWifiData = (Boolean) m_listItemData.get(position).get(
					FirewallItem.ITEM_WIFI_DATA);

			if (bWifiData) {

				image = getResources().getDrawable(R.drawable.wifi_data_on);
				nResId = R.drawable.firewall_right_allowed;

			} else {

				image = getResources().getDrawable(R.drawable.wifi_data_off);
				nResId = R.drawable.firewall_right_forbidden;
			}

			holder.wifi.setBackgroundResource(nResId);
			holder.wifi.setImageDrawable(image);

			holder.mobile.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					saveDataBeforeModify();
					setItemMobileStatus(v);
					getMobileBtnStatus();
					setMobileBtnStatus();
					setFirewallRules();
				}
			});

			holder.wifi.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					saveDataBeforeModify();
					setItemWifiStatus(v);
					getWifiBtnStatus();
					setWifiBtnStatus();
					setFirewallRules();

				}
			});

			return convertView;
		}
	}

	// ---------------------
	// class NetworkFirewallFragment
	// ---------------------
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FirewallItemList list = FirewallItemList.getInstance();
		m_listItemData = list.getItemList();
		m_firewallThread.start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		m_bDestroyed = true;
		saveData();
		m_firewallThread.m_firewallHandler.getLooper().quit();
	}

	public View getView() {
		return this.m_fragmentView;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		m_fragmentView = inflater.inflate(R.layout.firewall_network, container,
				false);
		initFragment();
		initListview();
		initItemList();
		return m_fragmentView;
	}

	private void initFragment() {
		m_mobileBtn = (TextView) m_fragmentView
				.findViewById(R.id.firewall_button_mobile);
		m_wifiBtn = (TextView) m_fragmentView
				.findViewById(R.id.firewall_button_wifi);
		m_mobileBtn.setOnClickListener(this);
		m_wifiBtn.setOnClickListener(this);
		m_db = new FirewallDatabase(m_fragmentView.getContext());
	}

	private void initListview() {

		m_listView = (ListView) this.m_fragmentView
				.findViewById(R.id.firewall_app_list);
		FirewallListAdapter adapter = new FirewallListAdapter(this.getView()
				.getContext());
		m_listView.setAdapter(adapter);
	}

	private void initItemList() {

		if (m_listItemData.isEmpty()) {
			m_bInitializing = true;
			showLoadingview();
			isRooted();

			new Thread() {
				public void run() {

					loadSavedApps();
					scanApps();
					if (!m_bDestroyed) {
						Message msg = new Message();
						msg.what = FirewallMessage.MSG_SCANAPP_FINISH;
						m_uiHandler.sendMessage(msg);
					}
				}
			}.start();

		} else {
			FirewallItemList list = FirewallItemList.getInstance();
			m_bRooted = list.m_bRooted;
			showListview();
		}

	}

	private void addItemToList(FirewallItem item) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		if (item != null) {
			map.put(FirewallItem.ITEM_UID, item.m_uid);
			map.put(FirewallItem.ITEM_PKG, item.m_pkg);
			map.put(FirewallItem.ITEM_ICON, item.m_icon);
			map.put(FirewallItem.ITEM_LABLE, item.m_lable);
			map.put(FirewallItem.ITEM_MOBILE_DATA, item.m_mobileData);
			map.put(FirewallItem.ITEM_WIFI_DATA, item.m_wifiData);

			m_listItemData.add(map);
		}
	}

	private void scanApps() {

		PackageManager pm = this.getView().getContext().getPackageManager();
		List<ApplicationInfo> listApp = pm.getInstalledApplications(0);

		for (ApplicationInfo appInfo : listApp) {

			if (PackageManager.PERMISSION_GRANTED == pm.checkPermission(
					Manifest.permission.INTERNET, appInfo.packageName)
					&& (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {

				FirewallItem itemInfo = new FirewallItem();
				itemInfo.m_uid = appInfo.uid;
				itemInfo.m_pkg = appInfo.packageName;
				itemInfo.m_icon = pm.getApplicationIcon(appInfo);
				itemInfo.m_lable = appInfo.loadLabel(pm).toString();

				if (!isAppSaved(appInfo.packageName, itemInfo)) {

					itemInfo.m_mobileData = true;
					itemInfo.m_wifiData = true;

					m_bMobileDataOn = true;
					m_bWifiDataOn = true;
				}

				addItemToList(itemInfo);
			}
		}
	}

	private void showLoadingview() {
		Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
				m_fragmentView.getContext(), R.anim.firewall_loading_anim);
		ImageView loadingImage = (ImageView) m_fragmentView
				.findViewById(R.id.loading_image);
		loadingImage.startAnimation(hyperspaceJumpAnimation);
	}

	private void showListview() {
		((FirewallListAdapter) this.m_listView.getAdapter())
				.notifyDataSetChanged();
		this.m_fragmentView.findViewById(R.id.firewall_layout_loading)
				.setVisibility(View.INVISIBLE);
		this.m_fragmentView.findViewById(R.id.firewall_layout_list)
				.setVisibility(View.VISIBLE);

		getMobileBtnStatus();
		setMobileBtnStatus();
		getWifiBtnStatus();
		setWifiBtnStatus();
	}

	@SuppressLint("NewApi")
	public void onClick(View v) {
		if (!m_bInitializing) {

			switch (v.getId()) {
			case R.id.firewall_button_mobile: {
				saveDataBeforeModify();
				m_bMobileDataOn = !m_bMobileDataOn;
				setMobileBtnStatus();
				setAllMobileStatus(m_bMobileDataOn);
				setFirewallRules();

				break;
			}

			case R.id.firewall_button_wifi: {

				saveDataBeforeModify();
				m_bWifiDataOn = !m_bWifiDataOn;
				setWifiBtnStatus();
				setAllWifiStatus(m_bWifiDataOn);
				setFirewallRules();
				break;
			}

			default:
				break;
			}
		}
	}

	private void setMobileBtnStatus() {

		Drawable draw = null;
		if (m_bMobileDataOn) {
			draw = getResources().getDrawable(R.drawable.mobile_data_on);
			m_mobileBtn.setTextColor(getResources().getColor(
					R.color.color_green));
		} else {
			draw = getResources().getDrawable(R.drawable.mobile_data_off);
			m_mobileBtn.setTextColor(getResources()
					.getColor(R.color.color_gray));
		}

		draw.setBounds(0, 0, draw.getMinimumWidth(), draw.getMinimumHeight());
		m_mobileBtn.setCompoundDrawables(null, draw, null, null);

	}

	private void setWifiBtnStatus() {

		Drawable draw = null;
		if (m_bWifiDataOn) {
			draw = getResources().getDrawable(R.drawable.wifi_data_on);
			m_wifiBtn
					.setTextColor(getResources().getColor(R.color.color_green));
		} else {
			draw = getResources().getDrawable(R.drawable.wifi_data_off);
			m_wifiBtn.setTextColor(getResources().getColor(R.color.color_gray));
		}

		draw.setBounds(0, 0, draw.getMinimumWidth(), draw.getMinimumHeight());
		m_wifiBtn.setCompoundDrawables(null, draw, null, null);
	}

	private void loadSavedApps() {
		m_savedData = m_db.getFireWallItemListData();
	}

	private boolean saveData() {
		return m_db.setFireWallItemListData();
	}

	private boolean isAppSaved(String pkg, FirewallItem item) {
		boolean bRes = false;
		for (Map<String, Object> map : m_savedData) {

			if (map != null) {

				String pkgName = (String) map.get(FirewallItem.ITEM_PKG);
				if (pkgName.equals(pkg)) {

					item.m_mobileData = (Boolean) map
							.get(FirewallItem.ITEM_MOBILE_DATA);

					item.m_wifiData = (Boolean) map
							.get(FirewallItem.ITEM_WIFI_DATA);

					if (item.m_mobileData)
						m_bMobileDataOn = true;

					if (item.m_wifiData)
						m_bWifiDataOn = true;

					bRes = true;
					break;
				}
			}
		}

		return bRes;
	}

	private void getMobileBtnStatus() {
		m_bMobileDataOn = false;
		for (Map<String, Object> map : m_listItemData) {

			if (map != null) {

				m_bMobileDataOn = (Boolean) map
						.get(FirewallItem.ITEM_MOBILE_DATA);

				if (m_bMobileDataOn)
					break;
			}
		}
	}

	private void getWifiBtnStatus() {
		m_bWifiDataOn = false;
		for (Map<String, Object> map : m_listItemData) {

			if (map != null) {

				m_bWifiDataOn = (Boolean) map.get(FirewallItem.ITEM_WIFI_DATA);

				if (m_bWifiDataOn)
					break;
			}
		}
	}

	private void setAllMobileStatus(boolean bStatus) {
		for (Map<String, Object> map : m_listItemData) {

			if (map != null) {
				map.put(FirewallItem.ITEM_MOBILE_DATA, bStatus);
			}
		}

		((FirewallListAdapter) this.m_listView.getAdapter())
				.notifyDataSetChanged();
	}

	private void setAllWifiStatus(boolean bStatus) {
		for (Map<String, Object> map : m_listItemData) {

			if (map != null) {
				map.put(FirewallItem.ITEM_WIFI_DATA, bStatus);
			}
		}

		((FirewallListAdapter) this.m_listView.getAdapter())
				.notifyDataSetChanged();
	}

	private void setItemMobileStatus(View v) {
		int position = m_listView.getPositionForView(v);

		Map<String, Object> data = m_listItemData.get(position);
		if (data != null) {
			boolean bMobileData = (Boolean) data
					.get(FirewallItem.ITEM_MOBILE_DATA);
			data.put(FirewallItem.ITEM_MOBILE_DATA, !bMobileData);

			((FirewallListAdapter) this.m_listView.getAdapter())
					.notifyDataSetChanged();
		}
	}

	private void setItemWifiStatus(View v) {
		int position = m_listView.getPositionForView(v);

		Map<String, Object> data = m_listItemData.get(position);
		if (data != null) {
			boolean bWifiData = (Boolean) data.get(FirewallItem.ITEM_WIFI_DATA);
			data.put(FirewallItem.ITEM_WIFI_DATA, !bWifiData);

			((FirewallListAdapter) this.m_listView.getAdapter())
					.notifyDataSetChanged();
		}
	}

	private void setFirewallRules() {

		if (m_bRooted) {
			Message msg = new Message();
			msg.what = FirewallMessage.MSG_ROOT_EXCU;
			msg.obj = m_listItemData;
			m_firewallThread.m_firewallHandler.sendMessage(msg);

		} else {
			loadDataBeforeModify();
			AlertDialogCustom.Builder builder = new AlertDialogCustom.Builder(
					this.getActivity());
			builder.setTitle(R.string.firewall_no_root_title);
			builder.setMessage(R.string.firewall_no_root_msg);
			AlertDialogCustom dialog = builder.create();
			dialog.setOwnerActivity(this.getActivity());
			dialog.show();
		}

	}

	private void isRooted() {
		Message msg = new Message();
		msg.what = FirewallMessage.MSG_ROOT_CHECK;
		m_firewallThread.m_firewallHandler.sendMessage(msg);
	}

	private void saveDataBeforeModify() {
		m_listItemDataBeforeModify.clear();
		for (Map<String, Object> src : m_listItemData) {
			Map<String, Object> dst = new HashMap<String, Object>(src);
			m_listItemDataBeforeModify.add(dst);
		}

	}

	private void loadDataBeforeModify() {
		m_listItemData.clear();
		for (Map<String, Object> src : m_listItemDataBeforeModify) {
			Map<String, Object> dst = new HashMap<String, Object>(src);
			m_listItemData.add(dst);
		}
		((FirewallListAdapter) this.m_listView.getAdapter())
				.notifyDataSetChanged();
	}
	
	private void showSetFirewallError()
	{
		AlertDialogCustom.Builder builder = new AlertDialogCustom.Builder(
				this.getActivity());
		builder.setTitle(R.string.fire_wall);
		builder.setMessage(R.string.firewall_set_rule_failed);
		AlertDialogCustom dialog = builder.create();
		dialog.setOwnerActivity(this.getActivity());
		dialog.show();
	}
}
