package com.robbi.android.token.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SignupResponse {
	
	public String email;
	
	public String firstname;
	
	public String lastname;
	
	public String authToken;

	private SignupResponse() {
		// do nothing
	}
	
	public static SignupResponse parse(InputStreamReader in) throws IOException {
		SignupResponse result = new SignupResponse();
		
		BufferedReader r = new BufferedReader(in);
		
		String line;
		
		while ((line = r.readLine()) != null) {
			if (line.startsWith("auth=")) {
				result.authToken = line.substring(5);
			} else if (line.startsWith("email=")) {
				result.email = line.substring(6);
			} else if (line.startsWith("firstname=")) {
				result.firstname = line.substring(10);
			} else if (line.startsWith("lastname=")) {
				result.lastname = line.substring(9);
			}
		}
		
		return result;
	}
}
