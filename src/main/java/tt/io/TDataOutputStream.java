package tt.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import tt.io.annotation.AtByte;
import tt.io.annotation.AtLPad;
import tt.io.annotation.AtSize;
import tt.io.annotation.AtUnsignedInt;
import tt.io.annotation.AtUnuse;

public class TDataOutputStream extends DataOutputStream {

	private Charset charset;

	public TDataOutputStream(OutputStream out, Charset charset) {
		super(out);
		this.charset = charset;
	}

	public void writeLength(int n) throws IOException {
		writeUnsignedShort(n);
	}

	public void writeCharBoolean(boolean b) throws IOException {
		out.write(b ? '1' : '0');
	}

	public void writeUnsignedShort(int n) throws IOException {
		out.write((int) (n >>> 0x08) & 255);
		out.write((int) (n >>> 0x00) & 255);
	}

	public void writeUnsignedInt(long n) throws IOException {
		out.write((int) (n >>> 0x18) & 255);
		out.write((int) (n >>> 0x10) & 255);
		out.write((int) (n >>> 0x08) & 255);
		out.write((int) (n >>> 0x00) & 255);
	}

	public void writeNString(int n, Object o) throws IOException {
		String s = o == null ? null : String.valueOf(o);
		byte[] b = s == null ? null : s.getBytes(charset);
		int j = b.length;
		if (j > n)
			throw new IOException(String.format("Data length(%d)[%s] has been exeeded the limit(%d)", j, s, n));
		if (j < n) {
			out.write(b, 0, j);
			int z = n - j;
			while (z-- > 0)
				out.write(0);
		} else {
			out.write(b, 0, n);
		}
	}

	public void writeNInt(int n, int i, char p) throws IOException {
		String s = String.valueOf(i);
		s = StringUtils.leftPad(s, n, p);
		writeNString(i, s);
	}

	public void writeDate(Date d) throws IOException {
		writeUnsignedInt(d == null ? 0 : d.getTime());
	}

	public void writeObject(Object o) throws Exception {
		Class<?> c = o.getClass();
		writeObject(c, o, null);
	}

	public void writeObject(Class<?> c, Object o, Field f) throws Exception {
		AtUnuse atUnused = f == null ? null : f.getAnnotation(AtUnuse.class);
		if (atUnused != null)
			return;
		try {
			if (c.isPrimitive()) {
				if (o == null)
					o = c.newInstance();
				if (int.class.isAssignableFrom(c)) {
					AtByte a1 = f == null ? null : f.getAnnotation(AtByte.class);
					AtLPad a2 = f == null ? null : f.getAnnotation(AtLPad.class);
					if (a1 != null) {
						out.write(((Number) o).intValue());
						return;
					} else if (a2 != null) {
						writeNInt(a2.value(), ((Number) o).intValue(), a2.pad());
						return;
					} else {
						writeInt(((Number) o).intValue());
						return;
					}
				}
				if (float.class.isAssignableFrom(c)) {
					writeFloat(((Number) o).floatValue());
					return;
				}
				if (short.class.isAssignableFrom(c)) {
					writeShort(((Number) o).shortValue());
					return;
				}
				if (long.class.isAssignableFrom(c)) {
					AtUnsignedInt a1 = f == null ? null : f.getAnnotation(AtUnsignedInt.class);
					if (a1 != null) {
						writeUnsignedInt(((Number) o).longValue());
					} else {
						writeLong(((Number) o).longValue());
					}
					return;
				}
				if (double.class.isAssignableFrom(c)) {
					writeDouble(((Number) o).doubleValue());
					return;
				}
				if (char.class.isAssignableFrom(c)) {
					out.write(((Character) o).charValue());
					return;
				}
				if (byte.class.isAssignableFrom(c)) {
					out.write(((Byte) o).byteValue());
					return;
				}
				if (boolean.class.isAssignableFrom(c)) {
					writeCharBoolean(((Boolean) o).booleanValue());
				}
				throw new IOException(
						String.format("%s is unsupported primitive type%s.", c.getCanonicalName(), f == null ? ""
								: String.format(" at %s.%s", f.getDeclaringClass().getCanonicalName(), f.getName())));
			}
			if (c.isArray()) {
				AtSize a1 = f == null ? null : f.getAnnotation(AtSize.class);
				if (a1 == null)
					throw new IOException(String.format("%s must be annotated%s.", c.getCanonicalName(), f == null ? ""
							: String.format(" at %s.%s", f.getDeclaringClass().getCanonicalName(), f.getName())));
				Class<?> cs = c.getComponentType();
				if (cs.isPrimitive()) {
					int n = a1.value();
					if (byte[].class.isAssignableFrom(c)) {
						byte[] b = o == null ? new byte[n] : (byte[]) o;
						if (b.length != n)
							throw new IOException(String.format("%s must be %d size of array but %d%s.",
									c.getCanonicalName(), n, b.length,
									f == null ? ""
											: String.format(" at %s.%s", f.getDeclaringClass().getCanonicalName(),
													f.getName())));
						out.write(b, 0, b.length);
						return;
					}
					throw new IOException(
							String.format("%s is unsupported primitive array[] type%s.", c.getCanonicalName(),
									f == null ? ""
											: String.format(" at %s.%s", f.getDeclaringClass().getCanonicalName(),
													f.getName())));
				} else {
					int n = a1.value();
					if (o == null)
						o = Array.newInstance(cs, n);
					else if (Array.getLength(o) != n)
						throw new IOException(String.format("%s must be %d size of array but %d%s.",
								c.getCanonicalName(), n, Array.getLength(o),
								f == null ? ""
										: String.format(" at %s.%s", f.getDeclaringClass().getCanonicalName(),
												f.getName())));
					for (int i = 0; i < n; i++)
						writeObject(cs, Array.get(o, i), null);
					return;
				}
			}
			if (String.class.isAssignableFrom(c)) {
				AtSize a1 = f == null ? null : f.getAnnotation(AtSize.class);
				if (a1 != null) {
					writeNString(a1.value(), o);
				} else {
					throw new IOException(String.format("%s should be annotated%s.", c.getCanonicalName(), f == null
							? "" : String.format(" at %s.%s", f.getDeclaringClass().getCanonicalName(), f.getName())));
				}
				return;
			}
			if (Date.class.isAssignableFrom(c)) {
				writeDate((Date) o);
				return;
			}
			if (List.class.isAssignableFrom(c)) {
				if (f == null)
					throw new IOException(String.format("%s should be annotated%s.", c.getCanonicalName(), f == null
							? "" : String.format(" at %s.%s", f.getDeclaringClass().getCanonicalName(), f.getName())));
				Class<?> lc = (Class<?>) ((((ParameterizedType) f.getGenericType()).getActualTypeArguments())[0]);
				List<?> l = (List<?>) o;
				AtSize a1 = f.getDeclaredAnnotation(AtSize.class);
				if (a1 == null) {
					if (l == null || l.size() == 0) {
						writeLength(0);
					} else {
						writeLength(l.size());
						for (Object lo : l)
							writeObject(lc, lo, null);
					}
				} else {
					int n = a1.value();
					int j = l == null ? 0 : l.size();
					for (int i = 0; i < n; i++) {
						Object lo = j > n ? l.get(i) : null;
						writeObject(lc, lo, null);
					}
				}
				return;
			}
			if (Number.class.isAssignableFrom(c) || c.isArray()) {
				if (Integer.class.isAssignableFrom(c)) {
					Integer n = (Integer) o;
					this.writeInt(n == null ? 0 : n.intValue());
				} else
					throw new IOException(String.format("%s is unsupported class type%s.", c.getCanonicalName(),
							f == null ? ""
									: String.format(" at %s.%s", f.getDeclaringClass().getCanonicalName(),
											f.getName())));
				return;
			}
			writeObjectFields(c, o);
		} catch (

		Exception e) {
			throw e;
		}
	}

	/**
	 * @param c
	 * @param o
	 * @throws Exception
	 */
	public void writeObjectFields(Class<?> c, Object o) throws Exception {
		if (o == null)
			o = c.newInstance();
		Field[] fa = c.getDeclaredFields();
		AccessibleObject.setAccessible(fa, true);
		for (Field f : fa)
			if (!Modifier.isStatic(f.getModifiers()))
				writeObject(f.getType(), f.get(o), f);
	}

	public Charset getCharset() {
		return charset;
	}
}
