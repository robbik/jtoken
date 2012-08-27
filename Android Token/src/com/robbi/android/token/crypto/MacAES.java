package com.robbi.android.token.crypto;

import java.util.Arrays;

import com.robbi.android.token.util.AlphaNumericHelper;

public class MacAES {

	private static final byte[] ZERO = new byte[16];

	private static final byte[] Rb = new byte[16];

	private byte[] k1;
	private byte[] k2;

	private AES aes;

	static {
		Arrays.fill(ZERO, (byte) 0);
		Arrays.fill(Rb, (byte) 0);

		Rb[15] = (byte) 0x87;
	}

	public MacAES() {
		aes = new AES();
	}

	@Override
	protected void finalize() {
		destroy();
	}

	private void expandKey(byte[] key) {
		byte[] l = aes.doFinal(ZERO);
		int llen = l.length;

		byte[] k1 = new byte[llen];
		byte[] k2 = new byte[llen];

		shl1(l, k1);
		if ((l[0] & 0x80) != 0) {
			xor(k1, Rb, k1);
		}

		shl1(k1, k2);
		if ((k1[0] & 0x80) != 0) {
			xor(k2, Rb, k2);
		}

		this.k1 = k1;
		this.k2 = k2;

		Arrays.fill(l, (byte) 0);
	}

	private static void shl1(byte[] in, byte[] out) {
		byte msb = 0;

		for (int i = in.length - 1; i >= 0; --i) {
			int x = in[i] & 0xFF;

			out[i] = (byte) (((x << 1) & 0xFF) | msb);
			msb = (byte) ((x & 0x80) >> 7);
		}
	}

	private static void xor(byte[] in1, byte[] in2, byte[] out) {
		for (int i = 0; i < 16; i++) {
			out[i] = (byte) ((in1[i] & 0xFF) ^ (in2[i] & 0xFF));
		}
	}

	private static void sliceAndPadBlock(byte[] in, int offset, byte[] out) {
		int len = in.length - offset;
		if (len < 16) {
			Arrays.fill(out, (byte) 0);
			out[len] = (byte) 0x80;
		} else {
			len = 16;
		}

		System.arraycopy(in, offset, out, 0, len);
	}

	public void init(byte[] key, boolean wipe) {
		aes.init(key, null, false, true);
		expandKey(key);

		if (wipe) {
			Arrays.fill(key, (byte) 0);
		}
	}

	public byte[] doFinal(byte[] in) {
		int len = in.length;

		int n;
		boolean complete;

		if (len == 0) {
			n = 1;
			complete = false;
		} else {
			n = AlphaNumericHelper.ceildiv16(len);
			complete = ((len & 0x0F) == 0);
		}

		byte[][] blocks = new byte[n][16];
		for (int i = 0; i < n; ++i) {
			sliceAndPadBlock(in, i << 4, blocks[i]);
		}

		if (complete) {
			xor(blocks[n - 1], k1, blocks[n - 1]);
		} else {
			xor(blocks[n - 1], k2, blocks[n - 1]);
		}

		byte[] x = new byte[16];
		byte[] y = new byte[16];
		byte[] tmp;

		System.arraycopy(ZERO, 0, x, 0, 16);

		for (int i = 0, maxi = n - 1; i < maxi; ++i) {
			xor(x, blocks[i], y);

			tmp = aes.doFinal(y);
			System.arraycopy(tmp, 0, x, 0, 16);

			Arrays.fill(tmp, (byte) 0);
		}

		xor(blocks[n - 1], x, y);

		tmp = aes.doFinal(y);
		Arrays.fill(x, (byte) 0);
		Arrays.fill(y, (byte) 0);

		return tmp;
	}

	public void destroy() {
		Arrays.fill(k1, (byte) 0);
		Arrays.fill(k2, (byte) 0);

		aes.destroy();
	}
}
