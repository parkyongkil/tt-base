package tt.lang.string.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TBraceReplacer {

	private String srcString;
	private Map<String, String> map = new HashMap<String, String>();
	private static Pattern bracePattern = Pattern.compile("\\{(\\w+)\\}");

	public TBraceReplacer(String s) {
		this.srcString = s;
	}

	public String getSrcString() {
		return srcString;
	}

	public void setSrcString(String srcString) {
		this.srcString = srcString;
	}

	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}

	public String[] getKeys() {
		String[] sa = new String[map.size()];
		map.keySet().toArray(sa);
		return sa;
	}

	public String[] getValues() {
		String[] ka = getKeys();
		String[] va = new String[ka.length];
		for (int i = 0; i < ka.length; i++)
			va[i] = map.get(ka[i]);
		return va;
	}

	public void put(Object k, Object v) {
		map.put(k == null ? "" : k.toString(), v == null ? "" : v.toString());
	}

	public String replace() {
		StringBuilder sb = new StringBuilder();
		Matcher m = bracePattern.matcher(getSrcString());
		int lse = 0;
		while (m.find()) {
			String k = m.group(1);
			Object o = getMap().get(k);
			String v = o == null ? k : o.toString();
			if (v == null)
				v = m.group(0);
			int si = m.start();
			int se = m.end();
			sb.append(getSrcString().substring(lse, si));
			sb.append(v);
			lse = se;
		}
		sb.append(getSrcString().substring(lse, m.regionEnd()));
		return sb.toString();
	}
}
