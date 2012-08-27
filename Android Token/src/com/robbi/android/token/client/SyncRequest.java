package com.robbi.android.token.client;

import java.io.UnsupportedEncodingException;

public class SyncRequest {
	
	public String authToken;
	
	public long version;
	
	public String deviceId;
	
	public String toPostBody() throws UnsupportedEncodingException {
		String s = "device_id=";
		s = s.concat(deviceId);
		s = s.concat("&version=");
		s = s.concat(String.valueOf(version));
		
		return s;
	}
}
