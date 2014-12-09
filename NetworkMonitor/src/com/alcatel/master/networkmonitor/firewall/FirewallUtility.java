package com.alcatel.master.networkmonitor.firewall;

import java.io.DataOutputStream;
import java.util.List;
import java.util.Map;

public class FirewallUtility {
	//
	private final static String WIFI_OPTIONS[] = { "tiwlan+", "wlan+", "eth+" };
	private final static String MOBILE_OPTIONS[] = { "rmnet+", "pdp+", "ppp+",
			"uwbr+", "wimax+" };

	//
	public static boolean isRooted() {

		boolean bRooted = true;
		Process process = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes("id" + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();

		} catch (Exception e) {
			e.printStackTrace();
			bRooted = false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {

			}
		}
		return bRooted;

	}

	public static boolean runRootCommand(String command) {
		boolean bSuccess = false;
		Process process = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(command + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
			if(process.exitValue() == 0)
			{
				bSuccess = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {

			}
		}
		return bSuccess;
	}

	public static boolean SetFirewall(List<Map<String, Object>> lstData) {
		boolean bSucess = true;
		StringBuilder script = new StringBuilder();
		
		script.append("iptables -F || exit\n");
		script.append("iptables -X || exit\n");

		for (Map<String, Object> map : lstData) {

			if (map != null) {
				int uid = (Integer) map.get(FirewallItem.ITEM_UID);
				boolean bMobileData = (Boolean) map
						.get(FirewallItem.ITEM_MOBILE_DATA);
				boolean bWifiData = (Boolean) map
						.get(FirewallItem.ITEM_WIFI_DATA);

				if (!bMobileData && !bWifiData) {
					script.append("iptables -A OUTPUT "
							+ " -m owner --uid-owner " + uid + " -j REJECT"
							+ " || exit\n");
				}

				if (!bMobileData) {
					for (final String option : MOBILE_OPTIONS) {
						script.append("iptables -A OUTPUT " + " -o " + option
								+ " -m owner --uid-owner " + uid + " -j REJECT"
								+ " || exit\n");
					}
				}

				if (!bWifiData) {
					for (final String option : WIFI_OPTIONS) {
						script.append("iptables -A OUTPUT " + " -o " + option
								+ " -m owner --uid-owner " + uid + " -j REJECT"
								+ " || exit\n");
					}
				}

			}

		}

		bSucess = runRootCommand(script.toString());

		return bSucess;
	}

	
}
