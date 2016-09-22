package tt.util.cfg;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CfgReader {

	private static CfgReader instance;

	private String cfgUrl = "http://download.tygem.com/mobile/down_korea/service/cfg/TygemMobile.cfg";
	private HashMap<String, List<List<String>>> cfgData;

	public static CfgReader getInstance() throws Exception {
		if (instance == null) {
			instance = new CfgReader();
			instance.readCfg();
		}
		return instance;
	}

	public List<List<String>> getCfg(String key) {
		return cfgData.get(key);
	}

	private HashMap<String, List<List<String>>> readCfg() throws Exception {
		if (cfgData == null) {
			cfgData = new HashMap<String, List<List<String>>>();
			URL url = new URL(cfgUrl);
			InputStream is = url.openStream();
			InputStreamReader isr = new InputStreamReader(is, "euc-kr");
			BufferedReader br = new BufferedReader(isr);
			Pattern p1 = Pattern.compile("^/(\\w+)\\s*\\[([^\\[\\]]*)\\].*$");
			while (br.ready()) {
				String s1 = br.readLine();
				if (s1 == null) {
					break;
				}
				if ((s1 = s1.trim()).length() == 0) {
					continue;
				}
				Matcher m1 = p1.matcher(s1);
				if (m1.matches() == false) {
					continue;
				}
				String key = m1.group(1);
				List<List<String>> items = cfgData.get(key);
				if (items == null) {
					items = new ArrayList<List<String>>();
					cfgData.put(key, items);
				}
				s1 = s1.substring(m1.end(1)).trim();
				String[] sa1 = s1.replaceAll("\\s*\\[(.*)\\]\\s*", "$1").split("\\s*\\]\\s*\\[\\s*");
				List<String> values = new ArrayList<String>(sa1.length);
				for (String s2 : sa1) {
					values.add(s2);
				}
				items.add(values);
			}
		}
		return cfgData;
	}
}
