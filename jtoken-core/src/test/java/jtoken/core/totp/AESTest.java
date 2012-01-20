package jtoken.core.totp;

import static org.junit.Assert.assertEquals;
import jtoken.core.totp.AES;
import jtoken.core.util.HexUtils;

import org.junit.Test;

public class AESTest {

	@Test
	public void testAes128Encrypt1() throws Exception {
		byte[] in = HexUtils.hexToBytes("3243f6a8885a308d313198a2e0370734");
		byte[] key = HexUtils.hexToBytes("2b7e151628aed2a6abf7158809cf4f3c");

		AES aes = new AES();
		aes.init(key, null, true, true);

		byte[] out = aes.doFinal(in);
		String hout = HexUtils.bytesToHex(out);
		
		assertEquals("3925841d02dc09fbdc118597196a0b32", hout);
	}

	@Test
	public void testAes128Decrypt1() throws Exception {
		byte[] in = HexUtils.hexToBytes("3925841d02dc09fbdc118597196a0b32");
		byte[] key = HexUtils.hexToBytes("2b7e151628aed2a6abf7158809cf4f3c");

		AES aes = new AES();
		aes.init(key, null, true, false);

		byte[] out = aes.doFinal(in);
		String hout = HexUtils.bytesToHex(out);
		
		assertEquals("3243f6a8885a308d313198a2e0370734", hout);
	}

	@Test
	public void testAes128Encrypt2() throws Exception {
		byte[] in = HexUtils.hexToBytes("00112233445566778899aabbccddeeff");
		byte[] key = HexUtils.hexToBytes("000102030405060708090a0b0c0d0e0f");

		AES aes = new AES();
		aes.init(key, null, true, true);

		byte[] out = aes.doFinal(in);
		String hout = HexUtils.bytesToHex(out);
		
		assertEquals("69c4e0d86a7b0430d8cdb78070b4c55a", hout);
	}

	@Test
	public void testAes128Decrypt2() throws Exception {
		byte[] in = HexUtils.hexToBytes("69c4e0d86a7b0430d8cdb78070b4c55a");
		byte[] key = HexUtils.hexToBytes("000102030405060708090a0b0c0d0e0f");

		AES aes = new AES();
		aes.init(key, null, true, false);

		byte[] out = aes.doFinal(in);
		String hout = HexUtils.bytesToHex(out);
		
		assertEquals("00112233445566778899aabbccddeeff", hout);
	}

	@Test
	public void testAes192Encrypt1() throws Exception {
		byte[] in = HexUtils.hexToBytes("00112233445566778899aabbccddeeff");
		byte[] key = HexUtils.hexToBytes("000102030405060708090a0b0c0d0e0f1011121314151617");

		AES aes = new AES();
		aes.init(key, null, true, true);

		byte[] out = aes.doFinal(in);
		String hout = HexUtils.bytesToHex(out);
		
		assertEquals("dda97ca4864cdfe06eaf70a0ec0d7191", hout);
	}

	@Test
	public void testAes192Decrypt1() throws Exception {
		byte[] in = HexUtils.hexToBytes("dda97ca4864cdfe06eaf70a0ec0d7191");
		byte[] key = HexUtils.hexToBytes("000102030405060708090a0b0c0d0e0f1011121314151617");

		AES aes = new AES();
		aes.init(key, null, true, false);

		byte[] out = aes.doFinal(in);
		String hout = HexUtils.bytesToHex(out);
		
		assertEquals("00112233445566778899aabbccddeeff", hout);
	}

	@Test
	public void testAes256Encrypt1() throws Exception {
		byte[] in = HexUtils.hexToBytes("00112233445566778899aabbccddeeff");
		byte[] key = HexUtils.hexToBytes("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f");

		AES aes = new AES();
		aes.init(key, null, true, true);

		byte[] out = aes.doFinal(in);
		String hout = HexUtils.bytesToHex(out);
		
		assertEquals("8ea2b7ca516745bfeafc49904b496089", hout);
	}

	@Test
	public void testAes256Decrypt1() throws Exception {
		byte[] in = HexUtils.hexToBytes("8ea2b7ca516745bfeafc49904b496089");
		byte[] key = HexUtils.hexToBytes("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f");

		AES aes = new AES();
		aes.init(key, null, true, false);

		byte[] out = aes.doFinal(in);
		String hout = HexUtils.bytesToHex(out);
		
		assertEquals("00112233445566778899aabbccddeeff", hout);
	}
}
