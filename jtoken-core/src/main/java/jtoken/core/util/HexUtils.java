package jtoken.core.util;

public abstract class HexUtils {

	private static final char[] B2H = "0123456789abcdef".toCharArray();

	private static int halfHexToByte(char cc) {
		if ((cc >= '0') && (cc <= '9')) {
			return cc - '0';
		} else if ((cc >= 'A') && (cc <= 'F')) {
			return (cc - 'A') + 10;
		} else if ((cc >= 'a') && (cc <= 'f')) {
			return (cc - 'a') + 10;
		} else {
			throw new NumberFormatException(cc + " is not hex number");
		}
	}

	public static byte[] hexToBytes(String hexstr) {
		hexstr = hexstr.replace(" ", "");

		int length = hexstr.length();
		if ((length & 1) != 0) {
			++length;
			hexstr = "0".concat(hexstr);
		}

		byte[] b = new byte[length >> 1];
		for (int i = 0, j = 0; i < length; i += 2, ++j) {
			b[j] = (byte) ((halfHexToByte(hexstr.charAt(i)) << 4) | halfHexToByte(hexstr
					.charAt(i + 1)));
		}

		return b;
	}

	public static int[] hexToBytesAsInts(String hexstr) {
		hexstr = hexstr.replace(" ", "");

		int length = hexstr.length();
		if ((length & 1) != 0) {
			++length;
			hexstr = "0".concat(hexstr);
		}

		int[] b = new int[length >> 1];
		for (int i = 0, j = 0; i < length; i += 2, ++j) {
			b[j] = (halfHexToByte(hexstr.charAt(i)) << 4)
					| halfHexToByte(hexstr.charAt(i + 1));
		}

		return b;
	}

	public static String bytesToHex(byte[] b) {
		int blen = b.length;
		char[] cc = new char[blen << 1];

		for (int i = 0, j = 0; i < blen; ++i, j += 2) {
			cc[j] = B2H[(b[i] & 0xF0) >> 4];
			cc[j + 1] = B2H[b[i] & 0x0F];
		}

		return new String(cc);
	}

	public static String bytesToHex(byte[] b, int offset) {
		int blen = b.length;
		char[] cc = new char[(blen - offset) << 1];

		for (int i = offset, j = 0; i < blen; ++i, j += 2) {
			cc[j] = B2H[(b[i] & 0xF0) >> 4];
			cc[j + 1] = B2H[b[i] & 0x0F];
		}

		return new String(cc);
	}

	public static String bytesToHexSpaceDelim(int[] ib) {
		int blen = ib.length;
		char[] cc = new char[blen * 3];

		for (int i = 0, j = 0; i < blen; ++i, j += 3) {
			cc[j] = B2H[(ib[i] & 0xF0) >> 4];
			cc[j + 1] = B2H[ib[i] & 0x0F];
			cc[j + 2] = ' ';
		}

		return new String(cc);
	}

	public static String bytesToHex(int[] ib) {
		int blen = ib.length;
		char[] cc = new char[blen << 1];

		for (int i = 0, j = 0; i < blen; ++i, j += 2) {
			cc[j] = B2H[(ib[i] & 0xF0) >> 4];
			cc[j + 1] = B2H[ib[i] & 0x0F];
		}

		return new String(cc);
	}
}
