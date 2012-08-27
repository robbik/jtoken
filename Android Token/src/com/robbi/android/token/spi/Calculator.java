package com.robbi.android.token.spi;

import android.content.Context;
import android.os.Bundle;

public interface Calculator {
	
	public static final String EXTRA_NOW = "now";
	
	public static final String EXTRA_TOKEN_WINDOW = "token_window";
	
	public static final String EXTRA_TOKEN_SEED = "seed";
	
	public static final String EXTRA_PIN = "pin";
	
	public static final String EXTRA_CHALLENGE = "challenge";
	
	String calculate1(Context context, Bundle extras) throws Exception;
	
	String calculate2(Context context, Bundle extras) throws Exception;
}
