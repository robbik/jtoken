package jtoken.core.totp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import jtoken.core.internal.AlphaNumericUtils;

import org.junit.Test;

public class TOTPTest {

	@Test
	public void generateNumericTest() throws Exception {
		long window = 3000;

		TOTP totp = new TOTP(TOTP.ALGORITHM_HMAC_SHA256,
				"abcdefghijklmnopqrst".getBytes(), 0, window);

		String token1 = totp.generateNumeric(System.currentTimeMillis(), 8);
		assertEquals(token1.length(), 8);

		Thread.sleep(window + 100);

		String token2 = totp.generateNumeric(System.currentTimeMillis(), 8);
		assertEquals(token2.length(), 8);

		if (token1.equals(token2)) {
			fail("token1 equals with token2, value is " + token1);
		}

		System.out.println("[generateNumericTest] token1=" + token1
				+ "; token2=" + token2);
	}

	@Test
	public void generateAlphaNumericTest() throws Exception {
		long window = 1000;

		TOTP totp = new TOTP(TOTP.ALGORITHM_HMAC_SHA256,
				"abcdefghijklmnopqrst".getBytes(), 0, window);

		String token1 = totp
				.generateAlphaNumeric(System.currentTimeMillis(), 8);
		assertEquals(token1.length(), 8);

		Thread.sleep(window + 100);

		String token2 = totp
				.generateAlphaNumeric(System.currentTimeMillis(), 8);
		assertEquals(token2.length(), 8);

		if (token1.equals(token2)) {
			fail("token1 equals with token2, value is " + token1);
		}

		System.out.println("[generateAlphaNumericTest] token1=" + token1
				+ "; token2=" + token2);
	}

	@Test
	public void generateBinaryTest() throws Exception {
		long window = 1000;

		TOTP totp = new TOTP(TOTP.ALGORITHM_HMAC_SHA256,
				"abcdefghijklmnopqrst".getBytes(), 0, window);

		byte[] token1 = totp.generateBinary(System.currentTimeMillis(), 8);
		assertEquals(token1.length, 8);

		Thread.sleep(window + 100);

		byte[] token2 = totp.generateBinary(System.currentTimeMillis(), 8);
		assertEquals(token2.length, 8);

		String stoken1 = AlphaNumericUtils.bytesToString64(token1);
		String stoken2 = AlphaNumericUtils.bytesToString64(token2);

		if (stoken1.equals(stoken2)) {
			fail("token1 equals with token2");
		}

		System.out.println("[generateBinaryTest] token1=" + stoken1
				+ "; token2=" + stoken2);

	}
}
