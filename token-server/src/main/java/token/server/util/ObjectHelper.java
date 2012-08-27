package token.server.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.Arrays;

public abstract class ObjectHelper {
	
	public static Long tryLongValueOf(String s) {
		if (s == null) {
			return null;
		}
		
		try {
			return Long.valueOf(s);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static BigDecimal tryBigDecimalValueOf(String s) {
		if (s == null) {
			return null;
		}
		
		try {
			return new BigDecimal(s);
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean isArray(Object a) {
		return a.getClass().isArray();
	}
	
	public static void assertNotNull(Object a, String message) {
		if (a == null) {
			throw new NullPointerException(message);
		}
	}

	public static boolean equals(byte[] a, byte[] b) {
		if (a == b) {
			return true;
		}

		if ((a == null) || (b == null)) {
			return false;
		}

		return Arrays.equals(a, b);
	}

	public static boolean equals(Object a, Object b) {
		if (a == b) {
			return true;
		}

		if ((a == null) || (b == null)) {
			return false;
		}

		return a.equals(b);
	}
	
	public static Class<?> getPrimitiveType(Class<?> type) {
		if (Integer.class.equals(type)) {
			return int.class;
		} else if (Long.class.equals(type)) {
			return long.class;
		} else if (Short.class.equals(type)) {
			return short.class;
		} else if (Byte.class.equals(type)) {
			return byte.class;
		} else if (Float.class.equals(type)) {
			return float.class;
		} else if (Double.class.equals(type)) {
			return double.class;
		} else if (Void.class.equals(type)) {
			return void.class;
		} else if (Boolean.class.equals(type)) {
			return boolean.class;
		} else {
			return null;
		}
	}

	public static <T> T coalesce(T... values) {
		for (int i = 0, len = values.length; i < len; ++i) {
			T value = values[i];

			if (value != null) {
				return value;
			}
		}

		return null;
	}

	private static byte[] readBytes(ObjectInputStream in) throws IOException {
		int length = in.readInt();

		byte[] bytes = new byte[length];
		in.readFully(bytes, 0, length);

		return bytes;
	}

	private static void writeBytes(byte[] bytes, ObjectOutputStream out)
			throws IOException {
		out.writeInt(bytes.length);
		out.write(bytes);
	}

	public static void writeObject(Object o, ObjectOutputStream out)
			throws IOException {
		if (o instanceof byte[]) {
			out.write('b');
			out.write(']');
			writeBytes((byte[]) o, out);
		} else if (o instanceof char[]) {
			out.write('c');
			out.write(']');
			writeBytes(String.valueOf((char[]) o).getBytes("UTF-8"), out);
		} else if (boolean.class.isInstance(o)) {
			out.write('b');
			out.write('o');
			out.writeBoolean(boolean.class.cast(o));
		} else if (byte.class.isInstance(o)) {
			out.write('b');
			out.write(' ');
			out.writeByte(byte.class.cast(o));
		} else if (short.class.isInstance(o)) {
			out.write('s');
			out.write(' ');
			out.writeShort(short.class.cast(o));
		} else if (char.class.isInstance(o)) {
			out.write('c');
			out.write(' ');
			out.writeChar(char.class.cast(o));
		} else if (int.class.isInstance(o)) {
			out.write('i');
			out.write(' ');
			out.writeInt(int.class.cast(o));
		} else if (long.class.isInstance(o)) {
			out.write('l');
			out.write(' ');
			out.writeLong(long.class.cast(o));
		} else if (float.class.isInstance(o)) {
			out.write('f');
			out.write(' ');
			out.writeFloat(float.class.cast(o));
		} else if (double.class.isInstance(o)) {
			out.write('d');
			out.write(' ');
			out.writeDouble(double.class.cast(o));
		} else if (Boolean.class.isInstance(o)) {
			out.write('B');
			out.write('O');
			out.writeBoolean(Boolean.class.cast(o).booleanValue());
		} else if (Byte.class.isInstance(o)) {
			out.write('B');
			out.write(' ');
			out.writeByte(Byte.class.cast(o).byteValue());
		} else if (Short.class.isInstance(o)) {
			out.write('S');
			out.write(' ');
			out.writeShort(Short.class.cast(o).shortValue());
		} else if (Character.class.isInstance(o)) {
			out.write('C');
			out.write(' ');
			out.writeChar(Character.class.cast(o).charValue());
		} else if (Integer.class.isInstance(o)) {
			out.write('I');
			out.write(' ');
			out.writeInt(Integer.class.cast(o).intValue());
		} else if (Long.class.isInstance(o)) {
			out.write('L');
			out.write(' ');
			out.writeLong(Long.class.cast(o).longValue());
		} else if (Float.class.isInstance(o)) {
			out.write('F');
			out.write(' ');
			out.writeFloat(Float.class.cast(o).floatValue());
		} else if (Double.class.isInstance(o)) {
			out.write('D');
			out.write(' ');
			out.writeDouble(Double.class.cast(o).doubleValue());
		} else if (String.class.isInstance(o)) {
			out.write('T');
			out.write(' ');
			out.writeUTF(String.class.cast(o));
		} else {
			out.write('J');
			out.write('O');
			out.writeObject(o);
		}
	}

	public static Object readObject(ObjectInputStream in)
			throws IOException, ClassNotFoundException {

		char h1 = (char) in.readUnsignedByte();
		char h2 = (char) in.readUnsignedByte();

		if ((h1 == 'b') && (h2 == ']')) {
			return readBytes(in);
		}
		if ((h1 == 'c') && (h2 == ']')) {
			return new String(readBytes(in), "UTF-8").toCharArray();
		}

		if ((h1 == 'b') && (h2 == 'o')) {
			return in.readBoolean();
		}
		if ((h1 == 'b') && (h2 == ' ')) {
			return in.readByte();
		}
		if ((h1 == 's') && (h2 == ' ')) {
			return in.readShort();
		}
		if ((h1 == 'c') && (h2 == ' ')) {
			return in.readChar();
		}
		if ((h1 == 'i') && (h2 == ' ')) {
			return in.readInt();
		}
		if ((h1 == 'l') && (h2 == ' ')) {
			return in.readLong();
		}
		if ((h1 == 'f') && (h2 == ' ')) {
			return in.readFloat();
		}
		if ((h1 == 'd') && (h2 == ' ')) {
			return in.readDouble();
		}

		if ((h1 == 'B') && (h2 == 'O')) {
			return Boolean.valueOf(in.readBoolean());
		}
		if ((h1 == 'B') && (h2 == ' ')) {
			return Byte.valueOf(in.readByte());
		}
		if ((h1 == 'S') && (h2 == ' ')) {
			return Short.valueOf(in.readShort());
		}
		if ((h1 == 'C') && (h2 == ' ')) {
			return Character.valueOf(in.readChar());
		}
		if ((h1 == 'I') && (h2 == ' ')) {
			return Integer.valueOf(in.readInt());
		}
		if ((h1 == 'L') && (h2 == ' ')) {
			return Long.valueOf(in.readLong());
		}
		if ((h1 == 'F') && (h2 == ' ')) {
			return Float.valueOf(in.readFloat());
		}
		if ((h1 == 'D') && (h2 == ' ')) {
			return Double.valueOf(in.readDouble());
		}

		if ((h1 == 'T') && (h2 == ' ')) {
			return in.readUTF();
		}
		if ((h1 == 'J') && (h2 == 'O')) {
			return in.readObject();
		}

		throw new IOException("unknown content " + h1 + h2);
	}

	public static byte[] toBytes(Object o) {
		if (o == null) {
			return null;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			ObjectOutputStream oos = new ObjectOutputStream(out);
			writeObject(o, oos);

			oos.flush();
		} catch (Throwable t) {
			// do nothing
		}

		return out.toByteArray();
	}

	public static Object fromBytes(byte[] bytes) {
		if (bytes == null) {
			return null;
		}

		try {
			ObjectInputStream ois = new ObjectInputStream(
					new ByteArrayInputStream(bytes));

			return readObject(ois);
		} catch (Throwable t) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T tryCast(Object o, Class<T> type) {
		if (o == null) {
			return null;
		}
		
		if (type.isInstance(o)) {
			return (T) o;
		}
		
		return null;
	}
}
