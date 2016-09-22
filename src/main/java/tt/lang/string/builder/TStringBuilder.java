package tt.lang.string.builder;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.time.FastDateFormat;

import tt.io.TData;
import tt.io.annotation.AtUnuse;

public class TStringBuilder {

	private static FastDateFormat df = FastDateFormat.getInstance("yyyy/MM/dd HH:mm:ss");
	private static String nls = "";
	private static String ns = "\n";
	private static String ds = "  ";
	private static String cs = ", ";
	private static String cns = ",\n";
	private static String a1s = "[";
	private static String a2s = "]";
	private static String ads = ": ";
	private static String o1s = "{";
	private static String o2s = "}";
	private static String ods = ": ";

	private StringBuilder sb;
	private boolean appendSuperClassAtLast;

	public TStringBuilder() {
		this.sb = new StringBuilder();
	}

	public TStringBuilder(StringBuilder sb) {
		this.sb = sb;
	}

	public boolean isAppendSuperClassAtLast() {
		return appendSuperClassAtLast;
	}

	public void setAppendSuperClassAtLast(boolean appendSuperClassAtLast) {
		this.appendSuperClassAtLast = appendSuperClassAtLast;
	}

	public void write(Object o) {
		if (o == null)
			sb.append(nls);
		else
			write(o.getClass(), o, "", new ArrayList<Object>());
	}

	public void write(Object o, String d) {
		if (o == null)
			sb.append(nls);
		else
			write(o.getClass(), o, d, new ArrayList<Object>());
	}

	@SuppressWarnings({ "rawtypes" })
	public void write(Class<?> c, Object o, String d, ArrayList<Object> p) {
		try {
			if (o == null) {
				sb.append(nls);
				return;
			}
			if (p.contains(o)) {
				sb.append("REF");
				return;
			}
			if (c.isPrimitive()) {
				sb.append(o);
				return;
			}
			if (c.isArray()) {
				writeArray(c, o, d, p);
				return;
			}
			if (c.isInterface()) {
				sb.append(o);
				return;
			}
			if (o instanceof CharSequence) {
				sb.append("'").append(o).append("'");
				return;
			}
			if (o instanceof Number) {
				sb.append(o);
				return;
			}
			if (o instanceof Date) {
				sb.append(df.format((Date) o));
				return;
			}
			if (o instanceof Calendar) {
				sb.append(df.format(((Calendar) o).getTime()));
				return;
			}
			if (o instanceof Collection) {
				writeCollection((Collection) o, d, p);
				return;
			}
			if (o instanceof Iterable) {
				ArrayList<Object> l = new ArrayList<Object>();
				Iterable t = (Iterable) o;
				for (Object to : t)
					l.add(to);
				writeCollection(l, d, p);
				return;
			}
			if (o instanceof Map) {
				writeMap((Map) o, d, p);
				return;
			}
			if (c == Object.class) {
				sb.append(o);
				return;
			}
			writeFields(c, o, d, p);
		} catch (Exception e) {
			sb.append(String.format("ERR: (%s[%s] %s)", c, o, e.getMessage()));
			e.printStackTrace(System.err);
		}
	}

	@SuppressWarnings("rawtypes")
	private void writeArray(Class c, Object o, String d, ArrayList<Object> p) {
		Class cc = c.getComponentType();
		int n = Array.getLength(o);
		boolean b = n > 1;
		String nd = d + ds;
		String cd = b ? cns + nd : cs;
		sb.append(a1s);
		if (b)
			sb.append(ns).append(nd);
		for (int i = 0; i < n; i++) {
			if (i > 0)
				sb.append(cd);
			sb.append(i).append(ads);
			@SuppressWarnings("unchecked")
			ArrayList<Object> u = (ArrayList<Object>) p.clone();
			u.add(o);
			write(cc, Array.get(o, i), nd, u);
		}
		sb.append(a2s);
	}

	@SuppressWarnings("rawtypes")
	private void writeCollection(Collection l, String d, ArrayList<Object> p) {
		boolean b = l.size() > 1;
		String nd = d + ds;
		String cd = b ? cns + nd : cs;
		sb.append(a1s);
		if (b)
			sb.append(ns).append(nd);
		int i = 0;
		for (Object lo : l) {
			if (i > 0)
				sb.append(cd);
			sb.append(i++).append(ads);
			@SuppressWarnings("unchecked")
			ArrayList<Object> u = (ArrayList<Object>) p.clone();
			u.add(l);
			write(lo == null ? null : lo.getClass(), lo, nd, u);
		}
		sb.append(a2s);
	}

	@SuppressWarnings("rawtypes")
	private void writeMap(Map m, String d, ArrayList<Object> p) {
		boolean b = m.size() > 1;
		String nd = d + ds;
		String cd = b ? cns + nd : cs;
		sb.append(o1s);
		if (b)
			sb.append(ns).append(nd);
		Set set = m.keySet();
		int i = 0;
		for (Object so : set) {
			if (i > 0)
				sb.append(cd);
			sb.append(i++).append(".").append(so).append(ods);
			@SuppressWarnings("unchecked")
			ArrayList<Object> u = (ArrayList<Object>) p.clone();
			u.add(m);
			write(so == null ? null : so.getClass(), m.get(so), nd, u);
		}
		sb.append(o2s);
	}

	@SuppressWarnings("rawtypes")
	private void writeFields(Class c, Object o, String d, ArrayList<Object> p) {
		try {
			List<Field> l = new ArrayList<Field>();
			listFields(l, c);
			boolean b = l.size() > 1;
			String nd = d + ds;
			String cd = b ? cns + nd : cs;
			sb.append(o1s);
			if (b)
				sb.append(ns).append(nd);
			int i = 0;
			for (Field f : l) {
				AtUnuse a = f.getDeclaredAnnotation(AtUnuse.class);
				if (a == null || !a.print())
					continue;
				if (i > 0)
					sb.append(cd);
				sb.append(i++).append(".").append(f.getName()).append(ods);
				@SuppressWarnings("unchecked")
				ArrayList<Object> u = (ArrayList<Object>) p.clone();
				u.add(o);
				boolean q = f.isAccessible();
				if (!q)
					f.setAccessible(true);
				Object t = f.get(o);
				if (!q)
					f.setAccessible(false);
				write(f.getType(), t, nd, u);
			}
			sb.append(o2s);
			return;
		} catch (Exception e) {
			sb.append(e.getMessage());
		}
	}

	@SuppressWarnings("rawtypes")
	private void listFields(List<Field> l, Class c) {
		Class sc = c.getSuperclass();
		if (appendSuperClassAtLast == false) {
			if (sc != null && !sc.equals(TData.class) && !sc.equals(Object.class))
				listFields(l, sc);
		}
		Field[] fa = c.getDeclaredFields();
		for (Field f : fa)
			if (!Modifier.isStatic(f.getModifiers()))
				l.add(f);
		if (appendSuperClassAtLast) {
			if (sc != null && !sc.equals(TData.class) && !sc.isInterface())
				listFields(l, sc);
		}
	}

	public String buildString() {
		return sb == null ? "" : sb.toString();
	}
}
