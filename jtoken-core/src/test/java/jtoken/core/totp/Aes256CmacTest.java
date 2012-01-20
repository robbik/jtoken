package jtoken.core.totp;

import static org.junit.Assert.assertEquals;
import jtoken.core.totp.MacAES;
import jtoken.core.util.HexUtils;

import org.junit.Test;

public class Aes256CmacTest {

	@Test
	public void testEmpty() {
		MacAES mac = new MacAES();
		mac.init(HexUtils.hexToBytes("2b7e1516 28aed2a6 abf71588 09cf4f3c"),
				true);

		byte[] out = mac.doFinal(HexUtils.hexToBytes(""));
		String sout = HexUtils.bytesToHex(out);

		assertEquals("bb1d6929e95937287fa37d129b756746", sout);
	}

	@Test
	public void testLenIs16() {
		MacAES mac = new MacAES();
		mac.init(HexUtils.hexToBytes("2b7e1516 28aed2a6 abf71588 09cf4f3c"),
				true);

		byte[] out = mac.doFinal(HexUtils
				.hexToBytes("6bc1bee2 2e409f96 e93d7e11 7393172a"));
		String sout = HexUtils.bytesToHex(out);

		assertEquals("070a16b46b4d4144f79bdd9dd04a287c", sout);
	}

	@Test
	public void testLenIs40() {
		MacAES mac = new MacAES();
		mac.init(HexUtils.hexToBytes("2b7e1516 28aed2a6 abf71588 09cf4f3c"),
				true);

		byte[] out = mac.doFinal(HexUtils
				.hexToBytes("6bc1bee2 2e409f96 e93d7e11 "
						+ "7393172a ae2d8a57 1e03ac9c 9eb76fac "
						+ "45af8e51 30c81c46 a35ce411"));
		String sout = HexUtils.bytesToHex(out);

		assertEquals("dfa66747de9ae63030ca32611497c827", sout);
	}

	@Test
	public void testLenIs64() {
		MacAES mac = new MacAES();
		mac.init(HexUtils.hexToBytes("2b7e1516 28aed2a6 abf71588 09cf4f3c"),
				true);

		byte[] out = mac.doFinal(HexUtils
				.hexToBytes("6bc1bee2 2e409f96 e93d7e11 7393172a"
						+ "ae2d8a57 1e03ac9c 9eb76fac 45af8e51"
						+ "30c81c46 a35ce411 e5fbc119 1a0a52ef"
						+ "f69f2445 df4f9b17 ad2b417b e66c3710"));
		String sout = HexUtils.bytesToHex(out);

		assertEquals("51f0bebf7e3b9d92fc49741779363cfe", sout);
	}
}
