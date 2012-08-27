package jtoken.core.ntp;

public final class NtpTime {

	private long delay;

	private long offset;

	private boolean delayIsSet;

	private boolean offsetIsSet;

	private NtpTime() {
		// do nothing
	}

	public boolean hasDelay() {
		return delayIsSet;
	}

	public boolean hasOffset() {
		return offsetIsSet;
	}

	public long getDelay() {
		if (!delayIsSet) {
			throw new UnsupportedOperationException(
					"this ntp-time has no delay");
		}

		return delay;
	}

	public long getOffset() {
		if (!offsetIsSet) {
			throw new UnsupportedOperationException(
					"this ntp-time has no offset");
		}

		return offset;
	}

	@Override
	public String toString() {
		String s = super.toString().concat(" [");

		if (delayIsSet && offsetIsSet) {
			s += "delay=" + delay + ", offset=" + offset;
		} else if (delayIsSet) {
			s += "delay=" + delay;
		} else if (offsetIsSet) {
			s += "offset=" + offset;
		}

		return s.concat("]");
	}

	private void compute(NtpPacket packet, long t4) {
		Timestamp time1 = packet.getOriginateTime();
		long t1 = time1.timeInMillisValue();

		// Receive Time is time request received by server (t2)
		Timestamp time2 = packet.getReceiveTime();
		long t2 = time2.timeInMillisValue();

		// Transmit time is time reply sent by server (t3)
		Timestamp time3 = packet.getTransmitTime();
		long t3 = time3.timeInMillisValue();

		delayIsSet = false;
		offsetIsSet = false;

		/*
		 * Round-trip network delay and local clock offset (or time drift) is
		 * calculated according to this standard NTP equation:
		 * 
		 * LocalClockOffset = ((ReceiveTimestamp - OriginateTimestamp) +
		 * (TransmitTimestamp - DestinationTimestamp)) / 2
		 * 
		 * equations from RFC-1305 (NTPv3) roundtrip delay = (t4 - t1) - (t3 -
		 * t2) local clock offset = ((t2 - t1) + (t3 - t4)) / 2
		 * 
		 * It takes into account network delays and assumes that they are
		 * symmetrical.
		 * 
		 * Note the typo in SNTP RFCs 1769/2030 which state that the delay is
		 * (T4 - T1) - (T2 - T3) with the "T2" and "T3" switched.
		 */
		if (time1.ntpValue() == 0) {
			// without originate time cannot determine when packet went out
			// might be via a broadcast NTP packet...
			if (time3.ntpValue() != 0) {
				offset = t3 - t4;
				offsetIsSet = true;
			}
			// else, ERROR: cannot compute delay/offset
		} else if ((time2.ntpValue() == 0)
				|| (time3.ntpValue() == 0)) {

			if (t4 < t1) {
				throw new AssertionError("return-time < originate-time");
			}

			// without receive or xmit time cannot figure out processing
			// time
			// so delay is simply the network travel time
			delay = t4 - t1;
			delayIsSet = true;

			// TODO: is offset still valid if rcvNtpTime=0 || xmitNtpTime=0 ???
			// Could always hash origNtpTime (sendTime) but if host doesn't set
			// it
			// then it's an malformed ntp host anyway and we don't care?
			// If server is in broadcast mode then we never send out a query in
			// first place...
			if (time2.ntpValue() != 0) {
				// xmitTime is 0 just use rcv time
				offset = t2 - t1;
			} else if (time3.ntpValue() != 0) {
				// rcvTime is 0 just use xmitTime time
				offset = t3 - t4;
			}

			offsetIsSet = true;
		} else {
			// assert xmitTime >= rcvTime: difference typically < 1ms
			if (t3 < t2) {
				throw new AssertionError(
						"malformed NTP packet, transmit-time < receive-time");
			}

			// assert returnTime >= origTime: network delay could not be
			// negative
			if (t4 < t1) {
				throw new AssertionError("return-time < originate-time");
			}

			long troundtrip = t4 - t1;

			// subtract processing time from round-trip network delay
			long tprocessing = t3 - t2;

			// in normal cases the processing delta is less than
			// the total roundtrip network travel time.
			if (tprocessing <= troundtrip) {
				troundtrip -= tprocessing; // delay = (t4 - t1) - (t3 - t2)
			} else {
				// if delta - delayValue == 1 ms then it's a round-off error
				// e.g. delay=3ms, processing=4ms
				if (tprocessing - troundtrip == 1) {
					// delayValue == 0 -> local clock saw no tick change but
					// destination clock did
					if (troundtrip != 0) {
						troundtrip = 0;
					}
				}
			}

			offset = ((t2 - t1) + (t3 - t4) - troundtrip) / 2;

			delayIsSet = true;
			offsetIsSet = true;
		}
	}

	public static NtpTime valueOf(NtpPacket packet, long returnTime) {
		NtpTime o = new NtpTime();
		o.compute(packet, returnTime);

		return o;
	}
}
