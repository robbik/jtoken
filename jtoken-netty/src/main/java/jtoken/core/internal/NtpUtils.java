package jtoken.core.internal;

import jtoken.core.ntp.Timestamp;

public abstract class NtpUtils {

	public static int getInt(byte[] buf, int index) {
		return (int) (buf[index] & 0xFF) << 24
				| (int) (buf[index + 1] & 0xFF) << 16
				| (int) (buf[index + 2]) << 8
				| (int) (buf[index + 3] & 0xFF);
	}

	public static long getLong(byte[] buf, int index) {
		return (long) (buf[index] & 0xFF) << 56
				| (long) (buf[index + 1] & 0xFF) << 48
				| (long) (buf[index + 2] & 0xFF) << 40
				| (long) (buf[index + 3] & 0xFF) << 32
				| (long) (buf[index + 4] & 0xFF) << 24
				| (long) (buf[index + 5] & 0xFF) << 16
				| (long) (buf[index + 6] & 0xFF) << 8
				| (long) (buf[index + 7] & 0xFF);
	}

	public static String getIPv4Address(byte[] buf, int index) {
		return String.valueOf(buf[index] & 0xFF).concat(".")
				.concat(String.valueOf(buf[index + 1] & 0xFF)).concat(".")
				.concat(String.valueOf(buf[index + 2] & 0xFF)).concat(".")
				.concat(String.valueOf(buf[index + 3] & 0xFF));
	}

	public static String getUTF7String(byte[] buf, int index) {
		char[] chars = new char[4];
		chars[0] = (char) (buf[index] & 0xFF);
		chars[1] = (char) (buf[index + 1] & 0xFF);
		chars[2] = (char) (buf[index + 2] & 0xFF);
		chars[3] = (char) (buf[index + 3] & 0xFF);

		return new String(chars);
	}

	public static Timestamp getTimestamp(byte[] buf, int index) {
		return new Timestamp(getLong(buf, index));
	}

	/***
	 * Sets the NTP timestamp at the given array index.
	 * 
	 * @param index
	 *            index into the byte array.
	 * @param t
	 *            TimeStamp.
	 */
	public static void setTimestamp(byte[] buf, int index, Timestamp t) {
		long ntpTime = (t == null) ? 0 : t.ntpValue();
		// copy 64-bits from Long value into 8 x 8-bit bytes of array
		// one byte at a time shifting 8-bits for each position.
		for (int i = 7; i >= 0; i--) {
			buf[index + i] = (byte) (ntpTime & 0xFF);
			ntpTime >>>= 8; // shift to next byte
		}
		// buf[index] |= 0x80; // only set if 1900 baseline....
	}
}
