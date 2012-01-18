package jtoken.core.totp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TOTPTest {

	@Test
	public void generateNumericTest() throws Exception {
		long window = 3000;

		TOTP totp = TOTP.createHmacSHA256("abcdefghijklmnopqrst".getBytes(), 0,
				window);

		String token1 = totp.generateNumeric(System.currentTimeMillis(), 8);
		assertEquals(token1.length(), 8);

		Thread.sleep(window + 100);

		String token2 = totp.generateNumeric(System.currentTimeMillis(), 8);
		assertEquals(token2.length(), 8);

		if (token1.equals(token2)) {
			throw new AssertionError("token1 equals with token2, value is "
					+ token1);
		}
	}

	@Test
	public void generateAlphaNumericTest() throws Exception {
		long window = 1000;

		TOTP totp = TOTP.createHmacSHA256("abcdefghijklmnopqrst".getBytes(), 0,
				window);

		String token1 = totp.generateAlphaNumeric(System.currentTimeMillis(), 8);
		assertEquals(token1.length(), 8);

		Thread.sleep(window + 100);

		String token2 = totp.generateAlphaNumeric(System.currentTimeMillis(), 8);
		assertEquals(token2.length(), 8);

		if (token1.equals(token2)) {
			throw new AssertionError("token1 equals with token2, value is "
					+ token1);
		}
		
		System.out.println("[generateAlphaNumericTest] token1=" + token1 + "; token2=" + token2);
		
	}
}
