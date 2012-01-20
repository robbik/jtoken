package jtoken.core.totp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import jtoken.core.totp.Totp;
import jtoken.core.util.HexUtils;

import org.junit.Test;

public class TotpTest {

	@Test
	public void generateNumericTest() throws Exception {
		long window = 3000;

		Totp totp = new Totp("abcdefghijklmnopqrstuvwxyz012345".getBytes(), 0,
				window, true);

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

		Totp totp = new Totp("abcdefghijklmnopqrstuvwxyz012345".getBytes(), 0,
				window, true);

		String token1 = totp.generateAlphaNumeric(System.currentTimeMillis(),
				25);

		Thread.sleep(window + 100);

		String token2 = totp.generateAlphaNumeric(System.currentTimeMillis(),
				25);

		if (token1.equals(token2)) {
			fail("token1 equals with token2, value is " + token1);
		}

		System.out.println("[generateAlphaNumericTest] token1=" + token1
				+ "; token2=" + token2);

		assertEquals(25, token1.length());
		assertEquals(25, token2.length());
	}

	@Test
	public void generateBinaryTest() throws Exception {
		long window = 1000;

		Totp totp = new Totp("abcdefghijklmnopqrstuvwxyz012345".getBytes(), 0,
				window, true);

		byte[] token1 = totp.generateBinary(System.currentTimeMillis(), 16);
		assertEquals(token1.length, 16);

		Thread.sleep(window + 100);

		byte[] token2 = totp.generateBinary(System.currentTimeMillis(), 16);
		assertEquals(token2.length, 16);

		String stoken1 = HexUtils.bytesToHex(token1);
		String stoken2 = HexUtils.bytesToHex(token2);

		if (stoken1.equals(stoken2)) {
			fail("token1 equals with token2");
		}

		System.out.println("[generateBinaryTest] token1=" + stoken1
				+ "; token2=" + stoken2);

	}
}
