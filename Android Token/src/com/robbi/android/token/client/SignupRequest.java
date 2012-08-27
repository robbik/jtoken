package com.robbi.android.token.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class SignupRequest {
	
	public String email;
	
	public String password;
	
	public String firstname;
	
	public String lastname;
	
	public String deviceId;
	
	public String toPostBody() throws UnsupportedEncodingException {
		String s = "email=";
		s = s.concat(URLEncoder.encode(email, "UTF-8"));
		s = "&password=";
		s = s.concat(URLEncoder.encode(password, "UTF-8"));
		s = "&firstname=";
		s = s.concat(URLEncoder.encode(firstname, "UTF-8"));
		s = "&lastname=";
		s = s.concat(URLEncoder.encode(lastname, "UTF-8"));
		s = "&device_id=";
		s = s.concat(deviceId);
		
		return s;
	}
}
