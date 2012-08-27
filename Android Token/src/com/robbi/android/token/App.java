package com.robbi.android.token;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Settings.Secure;

import com.robbi.android.token.util.AlphaNumericHelper;

public class App extends Application {
	
	private static final Uri GOOGLE_SERVICES_URI = Uri.parse("content://com.google.android.gsf.gservices");
	
	private static final String GOOGLE_SERVICES_ANDROID_ID = "android_id";
	
	private String deviceId = null;

	public String getDeviceId() {
		String deviceId = this.deviceId;
		
		if (deviceId == null) {
			deviceId = getDeviceId(this);
			this.deviceId = deviceId;
		}
		
		return deviceId;
	}

	public static String getDeviceId(Context context) {
		String raw = ""; 
		
		raw += ":" + Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		
		Cursor cursor = context.getContentResolver().query(GOOGLE_SERVICES_URI, null, null, new String[] { GOOGLE_SERVICES_ANDROID_ID }, null);
		if (cursor.moveToFirst()) {
			raw += ":" + cursor.getString(1);
		}

		// sha-256
		MessageDigest digest;
		
	    try {
	        digest = MessageDigest.getInstance("SHA-256");
	    } catch (NoSuchAlgorithmException e) {
	    	throw new UnsupportedOperationException("unsupported hash algorithm: SHA-256");
	    }
		
        try {
			digest.update(raw.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new UnsupportedOperationException("unsupported encoding: UTF-8");
		}

        return AlphaNumericHelper.bytesToString64(digest.digest());
	}
}
