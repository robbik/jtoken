package com.robbi.android.token.client;

public class HttpException extends Exception {

	private static final long serialVersionUID = -3641080930337708555L;
	
	private final int statusCode;

	public HttpException(int statusCode) {
		super();
		
		this.statusCode = statusCode;
	}

	public HttpException(int statusCode, String statusMessage) {
		super(statusMessage);
		
		this.statusCode = statusCode;
	}
	
	public int getStatusCode() {
		return statusCode;
	}
}
