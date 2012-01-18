package jtoken.core.ntp;

import java.util.Date;

public class Timestamp {

	/**
	 * baseline NTP time if bit-0=0 -> 7-Feb-2036 @ 06:28:16 UTC
	 */
	private static final long msb0baseTime = 2085978496000L;

	/**
	 * baseline NTP time if bit-0=1 -> 1-Jan-1900 @ 01:00:00 UTC
	 */
	private static final long msb1baseTime = -2208988800000L;

	private final long ntpTime;

	public Timestamp() {
		this(dateToNtp(System.currentTimeMillis()));
	}

	public Timestamp(long ntpTime) {
		this.ntpTime = ntpTime;
	}

	/***
	 * Constructs a newly allocated NTP timestamp object that represents the
	 * Java Date argument.
	 * 
	 * @param date
	 *            - the Date to be represented by the Timestamp object.
	 */
	public Timestamp(Date date) {
		ntpTime = (date == null) ? 0 : dateToNtp(date.getTime());
	}

	public long getSeconds() {
		return (ntpTime >>> 32) & 0xffffffffL;
	}

	public long getFraction() {
		return ntpTime & 0xffffffffL;
	}

	public long timeInMillisValue() {
		return ntpToDate(ntpTime);
	}

	public Date dateValue() {
		return new Date(ntpToDate(ntpTime));
	}

	public long ntpValue() {
		return ntpTime;
	}

	@Override
	public int hashCode() {
		return (int) (ntpTime ^ (ntpTime >>> 32));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}

		if (obj instanceof Timestamp) {
			return ntpTime == ((Timestamp) obj).ntpTime;
		}

		return false;
	}

	/***
	 * Converts Java time to 64-bit NTP time representation.
	 * 
	 * @param t
	 *            Java time
	 * @return NTP timestamp representation of Java time value.
	 */
	public static long dateToNtp(long t) {
		boolean useBase1 = t < msb0baseTime; // time < Feb-2036

		long baseTime;
		if (useBase1) {
			baseTime = t - msb1baseTime; // dates <= Feb-2036
		} else {
			// if base0 needed for dates >= Feb-2036
			baseTime = t - msb0baseTime;
		}

		long seconds = baseTime / 1000;
		long fraction = ((baseTime % 1000) * 0x100000000L) / 1000;

		if (useBase1) {
			seconds |= 0x80000000L; // set high-order bit if msb1baseTime 1900
									// used
		}

		long time = seconds << 32 | fraction;
		return time;
	}

	/***
	 * Convert 64-bit NTP timestamp to Java standard time.
	 * 
	 * Note that java time (milliseconds) by definition has less precision then
	 * NTP time (picoseconds) so converting NTP timestamp to java time and back
	 * to NTP timestamp loses precision. For example, Tue, Dec 17 2002
	 * 09:07:24.810 EST is represented by a single Java-based time value of
	 * f22cd1fc8a, but its NTP equivalent are all values ranging from
	 * c1a9ae1c.cf5c28f5 to c1a9ae1c.cf9db22c.
	 * 
	 * @param ntpTimeValue
	 * @return the number of milliseconds since January 1, 1970, 00:00:00 GMT
	 *         represented by this NTP timestamp value.
	 */
	public static long ntpToDate(long ntpTimeValue) {
		long seconds = (ntpTimeValue >>> 32) & 0xffffffffL; // high-order
															// 32-bits
		long fraction = ntpTimeValue & 0xffffffffL; // low-order 32-bits

		// Use round-off on fractional part to preserve going to lower precision
		fraction = Math.round(1000D * fraction / 0x100000000L);

		/*
		 * If the most significant bit (MSB) on the seconds field is set we use
		 * a different time base. The following text is a quote from RFC-2030
		 * (SNTP v4):
		 * 
		 * If bit 0 is set, the UTC time is in the range 1968-2036 and UTC time
		 * is reckoned from 0h 0m 0s UTC on 1 January 1900. If bit 0 is not set,
		 * the time is in the range 2036-2104 and UTC time is reckoned from 6h
		 * 28m 16s UTC on 7 February 2036.
		 */
		long msb = seconds & 0x80000000L;
		if (msb == 0) {
			// use base: 7-Feb-2036 @ 06:28:16 UTC
			return msb0baseTime + (seconds * 1000) + fraction;
		} else {
			// use base: 1-Jan-1900 @ 01:00:00 UTC
			return msb1baseTime + (seconds * 1000) + fraction;
		}
	}
	
	public static Timestamp currentTimestamp() {
		return new Timestamp(dateToNtp(System.currentTimeMillis()));
	}
}
