package com.robbi.android.token.totp;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.robbi.android.token.crypto.MacAES;
import com.robbi.android.token.util.AlphaNumericHelper;

public class TOTP {

	private static final int MAX_NUMERIC_DIGITS = 15;

	private static final int MAX_NUMERIC_OFFSET = 7;

	private static final int MAX_ALPHANUMERIC_DIGITS = 25;

	private static final int MAX_BINARY_DIGITS = 16;

	private static final int MAX_CHALLENGE_LENGTH = 99;

	private static final long[] DIGITS_POWER;

	private static final String NUMERIC_PADDER;

	private static final String ALPHANUMERIC_PADDER;

	static {
		DIGITS_POWER = new long[MAX_NUMERIC_DIGITS + 1];

		long value = 1;

		for (int i = 0, maxI = DIGITS_POWER.length; i < maxI; ++i) {
			DIGITS_POWER[i] = value;
			value *= 10;
		}

		char[] cbuf = new char[MAX_NUMERIC_DIGITS];
		Arrays.fill(cbuf, '0');

		NUMERIC_PADDER = new String(cbuf);

		cbuf = new char[MAX_ALPHANUMERIC_DIGITS];
		Arrays.fill(cbuf, '0');

		ALPHANUMERIC_PADDER = new String(cbuf);
	}

	private final MacAES mac;

	private long t0;

	private final long window;

	public TOTP(byte[] key, long t0, long window, boolean wipe) {
		if (t0 < 0) {
			throw new IllegalArgumentException(
					"t0 must equals or greater than zero");
		}

		this.t0 = t0;

		if (window <= 0) {
			throw new IllegalArgumentException("window must greater than zero");
		}

		this.window = window;

		mac = new MacAES();
		mac.init(key, wipe);
	}

	@Override
	protected void finalize() {
		destroy();
	}
	
	private byte[] generate(long time, int digits, byte[] challenge,
			int maxDigits) throws NoSuchAlgorithmException, InvalidKeyException {

		if (time < t0) {
			throw new IllegalArgumentException(
					"time must equals or greater than " + t0);
		}

		if (digits <= 0) {
			throw new IllegalArgumentException("digits must greater than zero");
		}
		if (digits > maxDigits) {
			throw new IllegalArgumentException(
					"digits must equals or less than " + maxDigits);
		}

		byte[] t;

		if (challenge == null) {
			t = AlphaNumericHelper.numericToBytes((time - t0) / window);
		} else {
			int challengeLength = challenge.length;
			if ((challengeLength == 0)
					|| (challengeLength > MAX_CHALLENGE_LENGTH)) {
				throw new IllegalArgumentException("challenge must contains 1-"
						+ MAX_CHALLENGE_LENGTH + " characters");
			}

			byte[] tmp = AlphaNumericHelper.numericToBytes((time - t0) / window);

			t = new byte[8 + challengeLength];
			System.arraycopy(tmp, 0, t, 0, 8);
			System.arraycopy(challenge, 0, t, 8, challengeLength);
		}

		return mac.doFinal(t);
	}
	
	public void adjustT0(long delta) {
		t0 += delta;
	}

	public String generateNumeric(long time, int digits, byte[] challenge) {
		byte[] hash;

		try {
			hash = generate(time, digits, challenge, MAX_NUMERIC_DIGITS);
		} catch (InvalidKeyException e) {
			throw new RuntimeException("state is not consistent", e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("state is not consistent", e);
		}

		int hlen = hash.length;
		int offset;

		if (hlen == digits) {
			offset = 0;
		} else {
			offset = hash[hlen - 1] & MAX_NUMERIC_OFFSET;
		}

		long otp = AlphaNumericHelper.bytesToLong(hash, offset)
				% DIGITS_POWER[digits];

		String result = Long.toString(otp);

		int len = result.length();
		if (len < digits) {
			return NUMERIC_PADDER.substring(len, digits).concat(result);
		}

		return result;
	}

	public String generateNumeric(long time, int digits) {
		return generateNumeric(time, digits, null);
	}

	public String generateAlphaNumeric(long time, int digits, byte[] challenge) {
		byte[] hash;

		try {
			hash = generate(time, digits, challenge, MAX_ALPHANUMERIC_DIGITS);
		} catch (InvalidKeyException e) {
			throw new RuntimeException("state is not consistent", e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("state is not consistent", e);
		}

		int hlen = hash.length, bdigits = ((digits * 5) + 7) / 8;
		int boffset;

		if (hlen == bdigits) {
			boffset = 0;
		} else {
			boffset = hash[hlen - 1] & (hlen - bdigits);
		}

		String result = AlphaNumericHelper.bytesToString32(hash, boffset,
				bdigits);

		int len = result.length();
		if (len > digits) {
			return result.substring(len - digits);
		}

		if (len < digits) {
			return ALPHANUMERIC_PADDER.substring(len, digits).concat(result);
		}

		return result;
	}

	public String generateAlphaNumeric(long time, int digits) {
		return generateAlphaNumeric(time, digits, null);
	}

	public byte[] generateBinary(long time, int digits, byte[] challenge) {
		byte[] hash;

		try {
			hash = generate(time, digits, challenge, MAX_BINARY_DIGITS);
		} catch (InvalidKeyException e) {
			throw new RuntimeException("state is not consistent", e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("state is not consistent", e);
		}

		int hlen = hash.length;
		if (hlen == digits) {
			return hash;
		}

		int offset = hash[hlen - 1] & (hlen - digits);

		byte[] result = new byte[digits];
		System.arraycopy(hash, offset, result, 0, digits);

		return result;
	}

	public byte[] generateBinary(long time, int digits) {
		return generateBinary(time, digits, null);
	}

	public void destroy() {
		mac.destroy();
	}
}
