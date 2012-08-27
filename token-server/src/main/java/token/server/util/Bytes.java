package token.server.util;

import java.util.UUID;

public abstract class Bytes {

	private static final char[] AN64 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01"
			.toCharArray();

	private static final char[] AN32 = "0123456789abcdefghijklmnpqrstuvwxyz"
			.toCharArray();
	
	public static void longToBytes(long l, byte[] b, int offset) {
		b[offset] = (byte) ((l >> 56) & 0xFF);
		b[offset + 1] = (byte) ((l >> 48) & 0xFF);
		b[offset + 2] = (byte) ((l >> 40) & 0xFF);
		b[offset + 3] = (byte) ((l >> 32) & 0xFF);
		b[offset + 4] = (byte) ((l >> 24) & 0xFF);
		b[offset + 5] = (byte) ((l >> 16) & 0xFF);
		b[offset + 6] = (byte) ((l >> 8) & 0xFF);
		b[offset + 7] = (byte) (l & 0xFF);
	}
	
	public static void intToBytes(int i, byte[] b, int offset) {
		b[offset + 0] = (byte) ((i >> 24) & 0xFF);
		b[offset + 1] = (byte) ((i >> 16) & 0xFF);
		b[offset + 2] = (byte) ((i >> 8) & 0xFF);
		b[offset + 3] = (byte) (i & 0xFF);
	}

	public static byte[] asBytes(UUID uuid) {
		byte[] b = new byte[16];

		longToBytes(uuid.getMostSignificantBits(), b, 0);
		longToBytes(uuid.getLeastSignificantBits(), b, 8);

		return b;
	}

	public static int ceildiv16(int a) {
		int val = a >> 4;
		int rem = a & 0x0F;

		if (rem > 0) {
			return val + 1;
		} else {
			return val;
		}
	}

	public static byte[] numericToBytes(long value) {
		byte[] b = new byte[8];

		b[0] = (byte) ((value >> 56) & 0xFF);
		b[1] = (byte) ((value >> 48) & 0xFF);
		b[2] = (byte) ((value >> 40) & 0xFF);
		b[3] = (byte) ((value >> 32) & 0xFF);
		b[4] = (byte) ((value >> 24) & 0xFF);
		b[5] = (byte) ((value >> 16) & 0xFF);
		b[6] = (byte) ((value >> 8) & 0xFF);
		b[7] = (byte) (value & 0xFF);

		return b;
	}

	public static long bytesToLong(byte[] b, int start) {
		int len = b.length;
		if (start + 7 < len) {
			return ((long) (b[start] & 0x7F) << 56)
					| ((long) (b[start + 1] & 0xFF) << 48)
					| ((long) (b[start + 2] & 0xFF) << 40)
					| ((long) (b[start + 3] & 0xFF) << 32)
					| ((long) (b[start + 4] & 0xFF) << 24)
					| ((long) (b[start + 5] & 0xFF) << 16)
					| ((long) (b[start + 6] & 0xFF) << 8)
					| (long) (b[start + 7] & 0xFF);
		}

		long value = 0;
		for (int i = len - 1; i >= start; --i) {
			value = (value << 8) | (long) (b[i] & 0xFF);
		}

		return value;
	}

	public static String bytesToString32(byte[] bytes) {
		return bytesToString32(bytes, 0, bytes.length);
	}

	public static String bytesToString32(byte[] bytes, int offset, int length) {
		if (length + offset > bytes.length) {
			length = bytes.length - offset;
		}

		int i = offset, index = 0, digit = 0;
		int currByte, nextByte;

		StringBuffer base32 = new StringBuffer((length + 7) * 8 / 5);

		while (i < length) {
			currByte = (bytes[i] >= 0) ? bytes[i] : (bytes[i] + 256); // unsign

			/* Is the current digit going to span a byte boundary? */
			if (index > 3) {
				if ((i + 1) < bytes.length) {
					nextByte = (bytes[i + 1] >= 0) ? bytes[i + 1]
							: (bytes[i + 1] + 256);
				} else {
					nextByte = 0;
				}

				digit = currByte & (0xFF >> index);
				index = (index + 5) % 8;
				digit <<= index;
				digit |= nextByte >> (8 - index);
				i++;
			} else {
				digit = (currByte >> (8 - (index + 5))) & 0x1F;
				index = (index + 5) % 8;

				if (index == 0) {
					i++;
				}
			}

			base32.append(AN32[digit]);
		}

		return base32.toString();
	}

	public static String toString64(byte[] bytes) {
		return bytesToString64(bytes, 0, bytes.length);
	}

	public static String bytesToString64(byte[] bytes, int offset, int length) {
		int oDataLen = (length * 4 + 2) / 3; // output length without padding
		int oLen = ((length + 2) / 3) * 4; // output length including padding

		char[] out = new char[oLen];
		int i = offset;
		int end = offset + length;
		int op = 0;

		if (end > bytes.length) {
			end = bytes.length;
		}

		while (i < end) {
			int i0 = bytes[i++] & 0xff;
			int i1 = i < end ? bytes[i++] & 0xff : 0;
			int i2 = i < end ? bytes[i++] & 0xff : 0;
			int o0 = i0 >>> 2;
			int o1 = ((i0 & 3) << 4) | (i1 >>> 4);
			int o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
			int o3 = i2 & 0x3F;

			out[op++] = AN64[o0];
			out[op++] = AN64[o1];

			if (op < oDataLen) {
				out[op] = AN64[o2];
				++op;
			}

			if (op < oDataLen) {
				out[op] = AN64[o3];
				++op;
			}
		}

		return String.valueOf(out, 0, op);
	}
}
