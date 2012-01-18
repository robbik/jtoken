package jtoken.core.totp;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import jtoken.core.internal.AlphaNumericUtils;

public class TOTP {

	public static final String ALGORITHM_HMAC_MD5 = "HmacMD5";

	public static final String ALGORITHM_HMAC_SHA1 = "HmacSHA1";

	public static final String ALGORITHM_HMAC_SHA244 = "HmacSHA244";

	public static final String ALGORITHM_HMAC_SHA256 = "HmacSHA256";

	public static final String ALGORITHM_HMAC_SHA512 = "HmacSHA512";

	private static final int MAX_NUMERIC_DIGITS = 15;

	private static final int MAX_ALPHANUMERIC_DIGITS = 128;

	private static final int MAX_BINARY_DIGITS = 60;

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

	private final String algorithm;

	private final byte[] key;

	private final long t0;

	private final long window;

	public TOTP(String algorithm, byte[] key, long t0, long window) {
		Mac hmac;

		try {
			hmac = Mac.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("algorithm " + algorithm
					+ " is not supported", e);
		}

		this.algorithm = algorithm;

		try {
			hmac.init(new SecretKeySpec(key, "RAW"));
		} catch (InvalidKeyException e) {
			throw new IllegalArgumentException("key is not valid", e);
		}

		this.key = (byte[]) key.clone();

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
			t = AlphaNumericUtils.numericToBytes((time - t0) / window);
		} else {
			int challengeLength = challenge.length;
			if ((challengeLength == 0)
					|| (challengeLength > MAX_CHALLENGE_LENGTH)) {
				throw new IllegalArgumentException("challenge must contains 1-"
						+ MAX_CHALLENGE_LENGTH + " characters");
			}

			byte[] tmp = AlphaNumericUtils.numericToBytes((time - t0) / window);

			t = new byte[8 + challengeLength];
			System.arraycopy(tmp, 0, t, 0, 8);
			System.arraycopy(challenge, 0, t, 8, challengeLength);
		}

		Mac hmac = Mac.getInstance(algorithm);
		hmac.init(new SecretKeySpec(key, "RAW"));

		return hmac.doFinal(t);
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

		int offset = hash[hash.length - 1] & 0x0F;

		long otp = AlphaNumericUtils.bytesToLong(hash, offset)
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

		int offset = hash[hash.length - 1] & 0x0F;
		String result = AlphaNumericUtils.bytesToString64(hash, offset,
				hash.length);

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

		int len = hash.length;
		int offset = hash[len - 1] & 0x0F;

		byte[] result = new byte[digits];
		if (digits + offset <= len) {
			System.arraycopy(hash, offset, result, 0, digits);
		} else {
			int diff = (digits + offset) - len;

			Arrays.fill(result, 0, diff, (byte) 0);
			System.arraycopy(hash, offset, result, 0, digits - diff);
		}

		return result;
	}

	public byte[] generateBinary(long time, int digits) {
		return generateBinary(time, digits, null);
	}

	public void destroy() {
		Arrays.fill(key, (byte) 0);
	}
}
