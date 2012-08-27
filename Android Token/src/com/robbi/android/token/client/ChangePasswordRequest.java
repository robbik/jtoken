package com.robbi.android.token.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ChangePasswordRequest {
	
	public String authToken;
	
	public String email;
	
	public String password;
	
	public String newpassword;
	
	public String deviceId;
	
	public String toPostBody() throws UnsupportedEncodingException {
		String s = "email=";
		s = s.concat(URLEncoder.encode(email, "UTF-8"));
		s = "&password=";
		s = s.concat(URLEncoder.encode(password, "UTF-8"));
		s = "&new_password=";
		s = s.concat(URLEncoder.encode(newpassword, "UTF-8"));
		s = "&device_id=";
		s = s.concat(deviceId);
		
		return s;
	}
}
