package jtoken.core.totp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TOTPPerformanceTest {

	@Test
	public void generateNumericTest() throws Exception {
		long window = 3000;

		TOTP totp = new TOTP(TOTP.ALGORITHM_HMAC_SHA512,
				"abcdefghijklmnopqrst".getBytes(), 0, window);

		long dtBegin = System.currentTimeMillis();
		int nops = 1000000;

		for (int i = nops; i > 0; --i) {
			String token1 = totp.generateNumeric(System.currentTimeMillis(), 8);
			assertEquals(token1.length(), 8);
		}

		long dtEnd = System.currentTimeMillis();

		System.out.println("[generateNumericTest] elapsed (ms)="
				+ (dtEnd - dtBegin) + ", tpms=" + (nops / (dtEnd - dtBegin)));
	}

	@Test
	public void generateAlphaNumericTest() throws Exception {
		long window = 3000;

		TOTP totp = new TOTP(TOTP.ALGORITHM_HMAC_SHA512,
				"abcdefghijklmnopqrst".getBytes(), 0, window);

		long dtBegin = System.currentTimeMillis();
		int nops = 1000000;

		for (int i = nops; i > 0; --i) {
			String token1 = totp.generateAlphaNumeric(
					System.currentTimeMillis(), 8);
			assertEquals(token1.length(), 8);
		}

		long dtEnd = System.currentTimeMillis();

		System.out.println("[generateAlphaNumericTest] elapsed (ms)="
				+ (dtEnd - dtBegin) + ", tpms=" + (nops / (dtEnd - dtBegin)));
	}

	@Test
	public void generateBinaryTest() throws Exception {
		long window = 3000;

		TOTP totp = new TOTP(TOTP.ALGORITHM_HMAC_SHA512,
				"abcdefghijklmnopqrst".getBytes(), 0, window);

		long dtBegin = System.currentTimeMillis();
		int nops = 1000000;

		for (int i = nops; i > 0; --i) {
			byte[] token1 = totp.generateBinary(System.currentTimeMillis(), 8);
			assertEquals(token1.length, 8);
		}

		long dtEnd = System.currentTimeMillis();

		System.out.println("[generateBinaryTest] elapsed (ms)="
				+ (dtEnd - dtBegin) + ", tpms=" + (nops / (dtEnd - dtBegin)));
	}
}
