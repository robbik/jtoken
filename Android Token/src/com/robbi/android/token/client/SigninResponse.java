package com.robbi.android.token.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SigninResponse {
	
	public String sid;
	
	public String email;
	
	public String authToken;
	
	private SigninResponse() {
		// do nothing
	}
	
	public static SigninResponse parse(InputStreamReader in) throws IOException {
		SigninResponse result = new SigninResponse();
		
		BufferedReader r = new BufferedReader(in);
		
		String line;
		
		while ((line = r.readLine()) != null) {
			if (line.startsWith("auth=")) {
				result.authToken = line.substring(5);
			} else if (line.startsWith("sid=")) {
				result.sid = line.substring(4);
			}
		}
		
		return result;
	}
}
