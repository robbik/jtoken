package jtoken.core.ntp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import jtoken.core.util.NtpUtils;

public class NtpPacket implements Serializable {

	private static final long serialVersionUID = -5074238496823436649L;

	private static final int MODE_INDEX = 0;
	private static final int MODE_SHIFT = 0;

	private static final int VERSION_INDEX = 0;
	private static final int VERSION_SHIFT = 3;

	private static final int LI_INDEX = 0;
	private static final int LI_SHIFT = 6;

	private static final int STRATUM_INDEX = 1;
	private static final int POLL_INDEX = 2;
	private static final int PRECISION_INDEX = 3;

	private static final int ROOT_DELAY_INDEX = 4;
	private static final int ROOT_DISPERSION_INDEX = 8;
	private static final int REFERENCE_ID_INDEX = 12;

	private static final int REFERENCE_TIMESTAMP_INDEX = 16;
	private static final int ORIGINATE_TIMESTAMP_INDEX = 24;
	private static final int RECEIVE_TIMESTAMP_INDEX = 32;
	private static final int TRANSMIT_TIMESTAMP_INDEX = 40;

	private transient final byte[] buf = new byte[48];

	public NtpPacket() {
		// do nothing
	}

	/***
	 * Returns mode as defined in RFC-1305 which is a 3-bit integer whose value
	 * is indicated by the MODE_xxx parameters.
	 * 
	 * @return mode as defined in RFC-1305.
	 */
	public int getMode() {
		return ((buf[MODE_INDEX] & 0xFF) >> MODE_SHIFT) & 0x7;
	}

	/***
	 * Set mode as defined in RFC-1305.
	 * 
	 * @param value
	 */
	public void setMode(int value) {
		buf[MODE_INDEX] = (byte) (buf[MODE_INDEX] & 0xF8 | value & 0x7);
	}

	/***
	 * Returns leap indicator as defined in RFC-1305 which is a two-bit code:
	 * 0=no warning 1=last minute has 61 seconds 2=last minute has 59 seconds
	 * 3=alarm condition (clock not synchronized)
	 * 
	 * @return leap indicator as defined in RFC-1305.
	 */
	public int getLeapIndicator() {
		return ((buf[LI_INDEX] & 0xFF) >> LI_SHIFT) & 0x3;
	}

	/***
	 * Set leap indicator as defined in RFC-1305.
	 * 
	 * @param value
	 *            leap indicator.
	 */
	public void setLeapIndicator(int value) {
		buf[LI_INDEX] = (byte) (buf[LI_INDEX] & 0x3F | ((value & 0x3) << LI_SHIFT));
	}

	/***
	 * Returns poll interval as defined in RFC-1305, which is an eight-bit
	 * signed integer indicating the maximum interval between successive
	 * messages, in seconds to the nearest power of two (e.g. value of six
	 * indicates an interval of 64 seconds. The values that can appear in this
	 * field range from NTP_MINPOLL to NTP_MAXPOLL inclusive.
	 * 
	 * @return poll interval as defined in RFC-1305.
	 */
	public int getPollInterval() {
		return buf[POLL_INDEX];
	}

	/***
	 * Set poll interval as defined in RFC-1305.
	 * 
	 * @param value
	 *            poll interval.
	 */
	public void setPollInterval(int value) {
		buf[POLL_INDEX] = (byte) (value & 0xFF);
	}

	/***
	 * Returns precision as defined in RFC-1305 encoded as an 8-bit signed
	 * integer (seconds to nearest power of two). Values normally range from -6
	 * to -20.
	 * 
	 * @return precision as defined in RFC-1305.
	 */
	public int getPrecision() {
		return buf[PRECISION_INDEX];
	}

	/***
	 * Set precision as defined in RFC-1305.
	 * 
	 * @param value
	 *            precision
	 */
	public void setPrecision(int value) {
		buf[PRECISION_INDEX] = (byte) (value & 0xFF);
	}

	/***
	 * Returns NTP version number as defined in RFC-1305.
	 * 
	 * @return NTP version number.
	 */
	public int getVersion() {
		return ((buf[VERSION_INDEX] & 0xFF) >> VERSION_SHIFT) & 0x7;
	}

	/***
	 * Set NTP version as defined in RFC-1305.
	 * 
	 * @param version
	 *            NTP version.
	 */
	public void setVersion(int version) {
		buf[VERSION_INDEX] = (byte) (buf[VERSION_INDEX] & 0xC7 | ((version & 0x7) << VERSION_SHIFT));
	}

	/***
	 * Returns Stratum as defined in RFC-1305, which indicates the stratum level
	 * of the local clock, with values defined as follows: 0=unspecified,
	 * 1=primary ref clock, and all others a secondary reference (via NTP).
	 * 
	 * @return Stratum level as defined in RFC-1305.
	 */
	public int getStratum() {
		return buf[STRATUM_INDEX] & 0xFF;
	}

	/***
	 * Set stratum level as defined in RFC-1305.
	 * 
	 * @param value
	 *            stratum level.
	 */
	public void setStratum(int value) {
		buf[STRATUM_INDEX] = (byte) (value & 0xFF);
	}

	/***
	 * Return root delay as defined in RFC-1305, which is the total roundtrip
	 * delay to the primary reference source, in seconds. Values can take
	 * positive and negative values, depending on clock precision and skew.
	 * 
	 * @return root delay as defined in RFC-1305.
	 */
	public int getRootDelay() {
		return NtpUtils.getInt(buf, ROOT_DELAY_INDEX);
	}

	/***
	 * Return root delay as defined in RFC-1305 in milliseconds, which is the
	 * total roundtrip delay to the primary reference source, in seconds. Values
	 * can take positive and negative values, depending on clock precision and
	 * skew.
	 * 
	 * @return root delay in milliseconds
	 */
	public double getRootDelayInMillisDouble() {
		return ((double) getRootDelay()) / 65.536;
	}

	/***
	 * Returns root dispersion as defined in RFC-1305.
	 * 
	 * @return root dispersion.
	 */
	public int getRootDispersion() {
		return NtpUtils.getInt(buf, ROOT_DISPERSION_INDEX);
	}

	/***
	 * Returns root dispersion (as defined in RFC-1305) in milliseconds.
	 * 
	 * @return root dispersion in milliseconds
	 */
	public long getRootDispersionInMillis() {
		return (getRootDispersion() * 1000L) / 65536L;
	}

	/***
	 * Returns root dispersion (as defined in RFC-1305) in milliseconds as
	 * double precision value.
	 * 
	 * @return root dispersion in milliseconds
	 */
	public double getRootDispersionInMillisDouble() {
		return ((double) getRootDispersion()) / 65.536;
	}

	/***
	 * Set reference clock identifier field with 32-bit unsigned integer value.
	 * See RFC-1305 for description.
	 * 
	 * @param refId
	 *            reference clock identifier.
	 */
	public void setReferenceId(int refId) {
		for (int i = 3; i >= 0; i--) {
			buf[REFERENCE_ID_INDEX + i] = (byte) (refId & 0xff);
			refId >>>= 8; // shift right one-byte
		}
	}

	/***
	 * Returns the reference id as defined in RFC-1305, which is a 32-bit
	 * integer whose value is dependent on several criteria.
	 * 
	 * @return the reference id as defined in RFC-1305.
	 */
	public int getReferenceId() {
		return NtpUtils.getInt(buf, REFERENCE_ID_INDEX);
	}

	/***
	 * Returns the reference id string. String cannot be null but value is
	 * dependent on the version of the NTP spec supported and stratum level.
	 * Value can be an empty string, clock type string, IP address, or a hex
	 * string.
	 * 
	 * @return the reference id string.
	 */
	public String getReferenceIdAsString() {
		int version = getVersion();
		int stratum = getStratum();

		if ((version == NtpConstants.VERSION_3)
				|| (version == NtpConstants.VERSION_4)) {
			if ((stratum == 0) || (stratum == 1)) {
				return NtpUtils.getUTF7String(buf, REFERENCE_ID_INDEX);
			}

			if (version == NtpConstants.VERSION_4) {
				return Integer.toHexString(getReferenceId());
			}
		}

		if (stratum >= 2) {
			return NtpUtils.getIPv4Address(buf, REFERENCE_ID_INDEX);
		}

		return Integer.toHexString(getReferenceId());
	}

	/***
	 * Returns the transmit timestamp as defined in RFC-1305.
	 * 
	 * @return the transmit timestamp as defined in RFC-1305. Never returns a
	 *         null object.
	 */
	public Timestamp getTransmitTime() {
		return NtpUtils.getTimestamp(buf, TRANSMIT_TIMESTAMP_INDEX);
	}

	/***
	 * Set transmit time with NTP timestamp. If <code>ts</code> is null then
	 * zero time is used.
	 * 
	 * @param ts
	 *            NTP timestamp
	 */
	public void setTransmitTime(Timestamp ts) {
		NtpUtils.setTimestamp(buf, TRANSMIT_TIMESTAMP_INDEX, ts);
	}

	/***
	 * Returns the originate time as defined in RFC-1305.
	 * 
	 * @return the originate time. Never returns null.
	 */
	public Timestamp getOriginateTime() {
		return NtpUtils.getTimestamp(buf, ORIGINATE_TIMESTAMP_INDEX);
	}

	/***
	 * Set originate timestamp given NTP TimeStamp object. If <code>ts</code> is
	 * null then zero time is used.
	 * 
	 * @param ts
	 *            NTP timestamp
	 */
	public void setOriginateTime(Timestamp ts) {
		NtpUtils.setTimestamp(buf, ORIGINATE_TIMESTAMP_INDEX, ts);
	}

	/***
	 * Returns the reference time as defined in RFC-1305.
	 * 
	 * @return the reference time as <code>TimeStamp</code> object. Never
	 *         returns null.
	 */
	public Timestamp getReferenceTime() {
		return NtpUtils.getTimestamp(buf, REFERENCE_TIMESTAMP_INDEX);
	}

	/***
	 * Set Reference time with NTP timestamp. If <code>ts</code> is null then
	 * zero time is used.
	 * 
	 * @param ts
	 *            NTP timestamp
	 */
	public void setReferenceTime(Timestamp ts) {
		NtpUtils.setTimestamp(buf, REFERENCE_TIMESTAMP_INDEX, ts);
	}

	/***
	 * Returns receive timestamp as defined in RFC-1305.
	 * 
	 * @return the receive time. Never returns null.
	 */
	public Timestamp getReceiveTime() {
		return NtpUtils.getTimestamp(buf, RECEIVE_TIMESTAMP_INDEX);
	}

	/***
	 * Set receive timestamp given NTP TimeStamp object. If <code>ts</code> is
	 * null then zero time is used.
	 * 
	 * @param ts
	 *            timestamp
	 */
	public void setReceiveTime(Timestamp ts) {
		NtpUtils.setTimestamp(buf, RECEIVE_TIMESTAMP_INDEX, ts);
	}

	private void readObject(ObjectInputStream in)
			throws ClassNotFoundException, IOException {
		int len = in.readInt();
		in.readFully(buf, 0, len);
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		int len = buf.length;
		out.writeInt(len);
		out.write(buf, 0, len);
	}
}
