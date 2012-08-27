package com.robbi.android.token.client;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.robbi.android.token.C;
import com.robbi.android.token.util.IOHelper;

public abstract class ServerHelper {
	
	private static final String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=UTF-8";
	
	public static SignupResponse signup(SignupRequest req) throws HttpException, IOException {
		int rc;
		String rm;
		
		SignupResponse resp = null;
		
		HttpURLConnection conn = (HttpURLConnection) new URL(C.server_uri.SIGN_UP).openConnection();
		
		try {
			byte[] body = req.toPostBody().getBytes("UTF-8");
			
			conn.setDefaultUseCaches(false);
			conn.setUseCaches(false);
			
			conn.setDoOutput(true);
			conn.setFixedLengthStreamingMode(body.length);
			
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", DEFAULT_CONTENT_TYPE);
			conn.setRequestProperty("Content-Length", String.valueOf(body.length));
			
			OutputStream out = conn.getOutputStream();
			out.write(body);
			out.close();
			
			rc = conn.getResponseCode();
			rm = conn.getResponseMessage();
			
			if (rc == HttpURLConnection.HTTP_OK) {
				resp = SignupResponse.parse(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			}
		} finally {
			try {
				conn.disconnect();
			} catch (Throwable t) {
				// do nothing
			}
		}
		
		if (rc != HttpURLConnection.HTTP_OK) {
			throw new HttpException(rc, rm);
		}
		
		return resp;
	}
	
	public static SigninResponse signin(SigninRequest req) throws HttpException, IOException {
		int rc;
		String rm;
		
		SigninResponse resp = null;
		
		HttpURLConnection conn = (HttpURLConnection) new URL(C.server_uri.SIGN_IN).openConnection();
		
		try {
			byte[] body = req.toPostBody().getBytes("UTF-8");
			
			conn.setDefaultUseCaches(false);
			conn.setUseCaches(false);
			
			conn.setDoOutput(true);
			conn.setFixedLengthStreamingMode(body.length);
			
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", DEFAULT_CONTENT_TYPE);
			conn.setRequestProperty("Content-Length", String.valueOf(body.length));
			
			OutputStream out = conn.getOutputStream();
			out.write(body);
			out.close();
			
			rc = conn.getResponseCode();
			rm = conn.getResponseMessage();
			
			if (rc == HttpURLConnection.HTTP_OK) {
				resp = SigninResponse.parse(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				
				resp.email = req.email;
			}
		} finally {
			try {
				conn.disconnect();
			} catch (Throwable t) {
				// do nothing
			}
		}
		
		if (rc != HttpURLConnection.HTTP_OK) {
			throw new HttpException(rc, rm);
		}
		
		return resp;
	}
	
	public static void changePassword(ChangePasswordRequest req) throws HttpException, IOException {
		int rc;
		String rm;
		
		HttpURLConnection conn = (HttpURLConnection) new URL(C.server_uri.CHANGE_PASSWORD).openConnection();
		
		try {
			byte[] body = req.toPostBody().getBytes("UTF-8");
			
			conn.setDefaultUseCaches(false);
			conn.setUseCaches(false);
			
			conn.setDoOutput(true);
			conn.setFixedLengthStreamingMode(body.length);
			
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Authorization", req.authToken);
			conn.setRequestProperty("Content-Type", DEFAULT_CONTENT_TYPE);
			conn.setRequestProperty("Content-Length", String.valueOf(body.length));
			
			OutputStream out = conn.getOutputStream();
			out.write(body);
			out.close();
			
			rc = conn.getResponseCode();
			rm = conn.getResponseMessage();
			
			IOHelper.consume(conn.getInputStream());
		} finally {
			try {
				conn.disconnect();
			} catch (Throwable t) {
				// do nothing
			}
		}
		
		if (rc != HttpURLConnection.HTTP_OK) {
			throw new HttpException(rc, rm);
		}
	}

	public static SyncResponse sync(SyncRequest req) throws HttpException, IOException {
		int rc;
		String rm;
		
		SyncResponse resp = null;
		
		HttpURLConnection conn = (HttpURLConnection) new URL(C.server_uri.SYNC).openConnection();
		
		try {
			byte[] body = req.toPostBody().getBytes("UTF-8");
			
			conn.setDefaultUseCaches(false);
			conn.setUseCaches(false);
			
			conn.setDoOutput(true);
			conn.setFixedLengthStreamingMode(body.length);
			
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Authorization", req.authToken);
			conn.setRequestProperty("Content-Type", DEFAULT_CONTENT_TYPE);
			conn.setRequestProperty("Content-Length", String.valueOf(body.length));
			
			OutputStream out = conn.getOutputStream();
			out.write(body);
			out.close();
			
			rc = conn.getResponseCode();
			rm = conn.getResponseMessage();
			
			if (rc == HttpURLConnection.HTTP_OK) {
				resp = SyncResponse.parse(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			}
		} finally {
			try {
				conn.disconnect();
			} catch (Throwable t) {
				// do nothing
			}
		}
		
		if (rc != HttpURLConnection.HTTP_OK) {
			throw new HttpException(rc, rm);
		}
		
		return resp;
	}
}
