package jtoken.core.ntp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jtoken.core.ntp.NtpConstants;
import jtoken.core.ntp.NtpPacket;
import jtoken.core.ntp.NtpTime;
import jtoken.core.ntp.Timestamp;

import org.junit.Test;

public class NtpPacketTest {

	@Test
	public void testOriginateTime() throws Exception {
		NtpPacket p1 = new NtpPacket();

		Timestamp ts = Timestamp.currentTimestamp();
		p1.setOriginateTime(ts);

		assertEquals(ts, p1.getOriginateTime());
	}

	@Test
	public void testNtpLoopback() throws Exception {
		final NtpPacket p1 = new NtpPacket();
		p1.setMode(NtpConstants.MODE_CLIENT);
		p1.setVersion(NtpConstants.VERSION_3);

		p1.setOriginateTime(Timestamp.currentTimestamp());

		// send and receive
		final CountDownLatch latch = new CountDownLatch(1);

		// send
		new Thread(new Runnable() {

			public void run() {
				p1.setReceiveTime(Timestamp.currentTimestamp());
				p1.setTransmitTime(Timestamp.currentTimestamp());

				latch.countDown();
			}
		}).start();

		// receive
		assertTrue(latch.await(5, TimeUnit.SECONDS));
		long returnTime = System.currentTimeMillis();

		NtpTime time = NtpTime.valueOf(p1, returnTime);
		assertTrue(time.getDelay() < 10);
		assertTrue(time.getOffset() < 10);
	}
}
