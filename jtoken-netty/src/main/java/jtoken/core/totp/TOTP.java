package jtoken.core.totp;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import jtoken.core.internal.AlphaNumericUtils;

public class TOTP {

	private static final int MAX_DIGITS = 15;

	private static final long[] DIGITS_POWER;

	private static final String PADDER;

	static {
		DIGITS_POWER = new long[MAX_DIGITS + 1];

		long value = 1;

		for (int i = 0, maxI = DIGITS_POWER.length; i < maxI; ++i) {
			DIGITS_POWER[i] = value;
			value *= 10;
		}

		char[] cbuf = new char[MAX_DIGITS];
		Arrays.fill(cbuf, '0');

		PADDER = new String(cbuf);
	}

	private final Mac hmac;

	private final long t0;

	private final long window;

	public TOTP(String algorithm, byte[] key, long t0, long window) {
		try {
			this.hmac = Mac.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("algorithm " + algorithm
					+ " is not supported");
		}

		try {
			hmac.init(new SecretKeySpec(key, "RAW"));
		} catch (InvalidKeyException e) {
			throw new IllegalArgumentException("key is not valid");
		}

		if (t0 < 0) {
			throw new IllegalArgumentException(
					"t0 must equals or greater than zero");
		}

		this.t0 = t0;

		if (window <= 0) {
			throw new IllegalArgumentException("window must greater than zero");
		}

		this.window = window;
	}

	public String generateNumeric(long time, int digits) {
		if (time < t0) {
			throw new IllegalArgumentException(
					"time must equals or greater than " + t0);
		}

		if (digits <= 0) {
			throw new IllegalArgumentException("digits must greater than zero");
		}
		if (digits > MAX_DIGITS) {
			throw new IllegalArgumentException(
					"digits must equals or less than " + MAX_DIGITS);
		}

		byte[] t = AlphaNumericUtils.numericToBytes((time - t0) / window);
		byte[] hash = hmac.doFinal(t);

		int offset = hash[hash.length - 1] & 0x0F;

		long otp = AlphaNumericUtils.bytesToLong(hash, offset)
				% DIGITS_POWER[digits];

		String result = Long.toString(otp);

		int len = result.length();
		if (len < digits) {
			return PADDER.substring(len, digits).concat(result);
		}

		return result;
	}

	public String generateAlphaNumeric(long time, int digits) {
		if (time < t0) {
			throw new IllegalArgumentException(
					"time must equals or greater than " + t0);
		}

		if (digits <= 0) {
			throw new IllegalArgumentException("digits must greater than zero");
		}
		if (digits > MAX_DIGITS) {
			throw new IllegalArgumentException(
					"digits must equals or less than " + MAX_DIGITS);
		}

		byte[] t = AlphaNumericUtils.numericToBytes((time - t0) / window);
		byte[] hash = hmac.doFinal(t);

		int offset = hash[hash.length - 1] & 0x0F;
		String result = AlphaNumericUtils.bytesToString32(hash).substring(offset);

		int len = result.length();
		if (len < digits) {
			return PADDER.substring(len, digits).concat(result);
		}

		return result.substring(len - digits);
	}

	public static TOTP createHmacMD5(byte[] key, long t0, long window) {
		return new TOTP("HmacMD5", key, t0, window);
	}

	public static TOTP createHmacSHA1(byte[] key, long t0, long window) {
		return new TOTP("HmacSHA1", key, t0, window);
	}

	public static TOTP createHmacSHA128(byte[] key, long t0, long window) {
		return new TOTP("HmacSHA128", key, t0, window);
	}

	public static TOTP createHmacSHA256(byte[] key, long t0, long window) {
		return new TOTP("HmacSHA256", key, t0, window);
	}

	public static TOTP createHmacSHA512(byte[] key, long t0, long window) {
		return new TOTP("HmacSHA512", key, t0, window);
	}
}
