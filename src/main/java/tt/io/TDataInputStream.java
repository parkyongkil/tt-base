package tt.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import tt.io.annotation.AtByte;
import tt.io.annotation.AtCharBoolean;
import tt.io.annotation.AtLPad;
import tt.io.annotation.AtSize;
import tt.io.annotation.AtUnsignedInt;
import tt.io.annotation.AtUnuse;

public class TDataInputStream extends DataInputStream {

	private Charset charset;
	private boolean debug = false;
	private int oi;
	protected volatile byte[] buffer = new byte[64];

	public TDataInputStream(InputStream in, Charset charset) {
		super(in);
		this.charset = charset;
	}

	public int readLength() throws IOException {
		return readUnsignedShort();
	}

	public boolean readCharBoolean() throws IOException {
		return in.read() != '0';
	}

	public long readUnsignedInt() throws IOException {
		in.read(buffer, 0, 4);
		long n1 = ((long) buffer[0] & 255) << 0x18;
		long n2 = ((long) buffer[1] & 255) << 0x10;
		long n3 = ((long) buffer[2] & 255) << 0x08;
		long n4 = ((long) buffer[3] & 255) << 0x00;
		return n1 + n2 + n3 + n4;
	}

	public String readNString(int n) throws IOException {
		if (n > buffer.length)
			buffer = new byte[n];
		in.read(buffer, 0, n);
		return new String(buffer, 0, n, charset).trim();
	}

	public int readNInt(int n) throws IOException {
		String s = readNString(n).trim();
		return NumberUtils.toInt(s);
	}

	public Date readDate() throws IOException {
		long v = readUnsignedInt();
		return v == 0 ? null : new Date(v);
	}

	@SuppressWarnings("unchecked")
	public <X> X readObject(Class<?> c) throws Exception {
		return (X) readObject(c, null, null);
	}

	public Object readObject(Object o) throws Exception {
		return o == null ? null : readObject(o.getClass(), o, null);
	}

	protected Object readObject(Class<?> c, Object o, Field f) throws Exception {
		AtUnuse atUnused = f == null ? null : f.getAnnotation(AtUnuse.class);
		if (atUnused != null)
			return o;
		Class<?> rc = null;
		try {
			if (c.isPrimitive()) {
				if (int.class.isAssignableFrom(c)) {
					AtByte a1 = f == null ? null : f.getAnnotation(AtByte.class);
					AtLPad a2 = f == null ? null : f.getAnnotation(AtLPad.class);
					if (a1 != null) {
						rc = byte.class;
						return in.read();
					} else if (a2 != null) {
						rc = String.class;
						return readNInt(a2.value());
					} else {
						rc = int.class;
						return o = readInt();
					}
				}
				if (float.class.isAssignableFrom(c)) {
					rc = float.class;
					return o = readFloat();
				}
				if (short.class.isAssignableFrom(c)) {
					rc = short.class;
					return o = readShort();
				}
				if (long.class.isAssignableFrom(c)) {
					AtUnsignedInt a1 = f == null ? null : f.getAnnotation(AtUnsignedInt.class);
					if (a1 != null) {
						rc = int.class;
						return o = readUnsignedInt();
					} else {
						return o = readLong();
					}
				}
				if (double.class.isAssignableFrom(c)) {
					rc = double.class;
					return o = readDouble();
				}
				if (char.class.isAssignableFrom(c)) {
					rc = char.class;
					return o = (char) in.read();
				}
				if (byte.class.isAssignableFrom(c)) {
					rc = byte.class;
					return o = readByte();
				}
				if (boolean.class.isAssignableFrom(c)) {
					rc = boolean.class;
					AtCharBoolean a1 = f == null ? null : f.getAnnotation(AtCharBoolean.class);
					return o = a1 == null ? readBoolean() : readCharBoolean();
				}
				throw new IOException(
						String.format("%s is unsupported primitive type%s.", c.getCanonicalName(), f == null ? ""
								: String.format(" at %s.%s", f.getDeclaringClass().getCanonicalName(), f.getName())));
			}
			if (c.isArray()) {
				AtSize a1 = f == null ? null : f.getAnnotation(AtSize.class);
				if (a1 == null)
					throw new IOException(String.format("%s should be annotated%s.", c.getCanonicalName(), f == null
							? "" : String.format(" at %s.%s", f.getDeclaringClass().getCanonicalName(), f.getName())));
				Class<?> cs = c.getComponentType();
				if (cs.isPrimitive()) {
					if (byte[].class.isAssignableFrom(c)) {
						byte[] b = new byte[a1.value()];
						in.read(b, 0, b.length);
						return b;
					}
					throw new IOException(
							String.format("%s is unsupported primitive array[] type%s.", c.getCanonicalName(),
									f == null ? ""
											: String.format(" at %s.%s", f.getDeclaringClass().getCanonicalName(),
													f.getName())));
				} else {
					int n = a1.value();
					o = Array.newInstance(cs, n);
					for (int i = 0; i < n; i++) {
						Object o1 = readObject(cs, Array.get(o, i), null);
						Array.set(o, i, o1);
					}
					return o;
				}
			}
			if (String.class.isAssignableFrom(c)) {
				rc = String.class;
				AtSize a1 = f == null ? null : f.getAnnotation(AtSize.class);
				if (a1 == null)
					throw new IOException(String.format("%s should be annotated%s.", c.getCanonicalName(), f == null
							? "" : String.format(" at %s.%s", f.getDeclaringClass().getCanonicalName(), f.getName())));
				return o = readNString(a1.value());
			}
			if (Date.class.isAssignableFrom(c)) {
				rc = Date.class;
				return o = readDate();
			}
			if (List.class.isAssignableFrom(c)) {
				if (f == null)
					throw new IOException(String.format("%s should be annotated%s.", c.getCanonicalName(), f == null
							? "" : String.format(" at %s.%s", f.getDeclaringClass().getCanonicalName(), f.getName())));
				Class<?> lc = (Class<?>) ((((ParameterizedType) f.getGenericType()).getActualTypeArguments())[0]);
				AtSize a1 = f.getDeclaredAnnotation(AtSize.class);
				int n = a1 != null ? a1.value() : readLength();
				List<Object> l = new ArrayList<Object>(n);
				for (int i = 0; i < n; i++)
					l.add(readObject(lc, null, null));
				return o = l;
			}
			if (Number.class.isAssignableFrom(c)) {
				if (Integer.class.isAssignableFrom(c))
					return o = readInt();
				throw new IOException(String.format("%s is unsupported class type%s.", c.getCanonicalName(), f == null
						? "" : String.format(" at %s.%s", f.getDeclaringClass().getCanonicalName(), f.getName())));
			}
			if (TData.class.isAssignableFrom(c)) {
				return o = readObjectFields(c, o);
			}
			throw new IOException(String.format("%s is unsupported class type%s.", c.getCanonicalName(), f == null ? ""
					: String.format(" at %s.%s", f.getDeclaringClass().getCanonicalName(), f.getName())));

		} catch (Exception e) {
			throw e;
		} finally {
			if (debug)
				System.out
						.println(String.format("O(%4d) %-10s %s=%s", oi++, rc == null ? "" : rc.getSimpleName(),
								f == null ? c.getSimpleName()
										: String.format("%s.%s", f.getDeclaringClass().getSimpleName(), f.getName()),
								o));
		}
	}

	protected Object readObjectFields(Class<?> c, Object o) throws Exception {
		if (o == null)
			o = c.newInstance();
		Field[] fa = c.getDeclaredFields();
		for (Field f : fa)
			if (!Modifier.isStatic(f.getModifiers())) {
				boolean b = f.isAccessible();
				if (!b)
					f.setAccessible(true);
				f.set(o, readObject(f.getType(), f.get(o), f));
				if (!b)
					f.setAccessible(false);
			}
		return o;
	}

	public Charset getCharset() {
		return charset;
	}
}
