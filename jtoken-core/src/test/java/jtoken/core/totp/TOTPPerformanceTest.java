package jtoken.core.totp;

import static org.junit.Assert.assertEquals;
import jtoken.core.totp.Totp;

import org.junit.Test;

public class TotpPerformanceTest {

	@Test
	public void generateNumericTest() throws Exception {
		long window = 3000;

		Totp totp = new Totp("abcdefghijklmnopqrstuvwxyz012345".getBytes(), 0,
				window, true);

		long dtBegin = System.currentTimeMillis();
		int nops = 1000000;

		for (int i = nops; i > 0; --i) {
			String token1 = totp
					.generateNumeric(System.currentTimeMillis(), 15);
			assertEquals(token1.length(), 15);
		}

		long dtEnd = System.currentTimeMillis();

		System.out.println("[generateNumericTest] elapsed (ms)="
				+ (dtEnd - dtBegin) + ", tpms=" + (nops / (dtEnd - dtBegin)));
	}

	@Test
	public void generateAlphaNumericTest() throws Exception {
		long window = 3000;

		Totp totp = new Totp("abcdefghijklmnopqrstuvwxyz012345".getBytes(), 0,
				window, true);

		long dtBegin = System.currentTimeMillis();
		int nops = 1000000;

		for (int i = nops; i > 0; --i) {
			String token1 = totp.generateAlphaNumeric(System
					.currentTimeMillis(), 25);
			assertEquals(token1.length(), 25);
		}

		long dtEnd = System.currentTimeMillis();

		System.out.println("[generateAlphaNumericTest] elapsed (ms)="
				+ (dtEnd - dtBegin) + ", tpms=" + (nops / (dtEnd - dtBegin)));
	}

	@Test
	public void generateBinaryTest() throws Exception {
		long window = 3000;

		Totp totp = new Totp("abcdefghijklmnopqrstuvwxyz012345".getBytes(), 0,
				window, true);

		long dtBegin = System.currentTimeMillis();
		int nops = 1000000;

		for (int i = nops; i > 0; --i) {
			byte[] token1 = totp.generateBinary(System.currentTimeMillis(), 16);
			assertEquals(token1.length, 16);
		}

		long dtEnd = System.currentTimeMillis();

		System.out.println("[generateBinaryTest] elapsed (ms)="
				+ (dtEnd - dtBegin) + ", tpms=" + (nops / (dtEnd - dtBegin)));
	}
}
