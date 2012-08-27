package com.robbi.android.token.spi;

import com.robbi.android.token.util.Crypto;
import com.robbi.android.token.util.Hex;

import android.content.Context;
import android.os.Bundle;

public class MOTP implements Calculator {

	public String calculate1(Context context, Bundle extras) throws Exception {
		byte[] seedb = extras.getByteArray(EXTRA_TOKEN_SEED);
		
		String secret = new String(seedb, "UTF-8");
		
		String epoch = String.valueOf(extras.getLong(EXTRA_NOW));
		epoch = epoch.substring(0, epoch.length() - 4);
		
		String pin = extras.getString(EXTRA_PIN);
		
		return Hex.asHex(Crypto.md5(epoch + secret + pin)).substring(0, 6);
	}

	public String calculate2(Context context, Bundle extras) throws Exception {
		throw new UnsupportedOperationException();
	}
}
