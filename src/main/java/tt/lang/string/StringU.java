package tt.lang.string;

import org.apache.commons.lang3.StringUtils;

import tt.lang.string.builder.TStringBuilder;

public class StringU {

	public static String leftPad(String s, int n, char c) {
		if (s == null)
			s = "";
		while (s.length() < n)
			s = c + s;
		return s;
	}

	public static boolean isBlank(String s) {
		return StringUtils.isBlank(s);
	}

	public static String toString(Object o) {
		TStringBuilder sb = new TStringBuilder();
		sb.write(o);
		return sb.buildString();
	}
}
