package rk.experiment.c2dm.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import rk.experiment.c2dm.ssl.AllowAllHostnameVerifier;

public abstract class ServerRegistrar {
	
	private static final String UTF8 = "UTF-8";
	
	private static final String SERVER_URL = "http://localhost:8080/accounts/androidLogin";
	
	private static final String UNREGISTER_URL = "http://localhost:8080/android/unregister";
	
	private static final String PARAM_USERNAME = "username";
	
	private static final String PARAM_PASSWORD = "password";
	
	private static final String PARAM_REGISTRATION_ID = "gcm_registration_id";
	
	private static final int DEFAULT_BACKOFF_MS = 3000;
	
	private static final int MAX_BACKOFF_MS = (int) TimeUnit.MINUTES.toMillis(5);
	
	private static final Random rand = new Random();
	
	public static boolean register(String username, String password, String registrationId, int maxRetry) throws IOException {
		HttpsURLConnection.setDefaultHostnameVerifier(new AllowAllHostnameVerifier());
		
		URL url;
		
		try {
			url = new URL(SERVER_URL);
		} catch (MalformedURLException e) {
			throw e;
		}
		
		int backoffTimeMs = DEFAULT_BACKOFF_MS;
		
		while (maxRetry > 0) {
			int responseCode;
			
			try {
				String post;
				
				post = PARAM_USERNAME + "=" + URLEncoder.encode(username, UTF8);
				post += "&" + PARAM_PASSWORD + "=" + URLEncoder.encode(password, UTF8);
				post += "&" + PARAM_REGISTRATION_ID + "=" + registrationId;
				
				byte[] bytesPost = post.getBytes(UTF8);
				
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
				
				conn.setDoOutput(true);
				conn.setUseCaches(false);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
				conn.setRequestProperty("Content-Length", Integer.toString(bytesPost.length));

				OutputStream out = conn.getOutputStream();
				out.write(bytesPost);
				out.close();

				responseCode = conn.getResponseCode();
			} catch (IOException e) {
				responseCode = 503;
			}

			if ((responseCode >= 100) && (responseCode < 300)) {
				return true;
			} else if (responseCode == 304) {
				return true;
			} else if ((responseCode >= 500) && (responseCode <= 599)) {
				// retry
			} else {
				return false;
			}
			
			int nextAttempt = (backoffTimeMs / 2) + rand.nextInt(backoffTimeMs);
			
			try {
				Thread.sleep(nextAttempt);
			} catch (InterruptedException e) {
				return false;
			}
			
			if (backoffTimeMs < MAX_BACKOFF_MS) {
				backoffTimeMs *= 2;
			}
			
			--maxRetry;
		}
		
		throw new ConnectException();
	}

	public static boolean unregister(String authToken, int maxRetry) {
		URL url;
		
		try {
			url = new URL(UNREGISTER_URL);
		} catch (MalformedURLException e) {
			return false;
		}
		
		int backoffTimeMs = DEFAULT_BACKOFF_MS;
		
		while (maxRetry > 0) {
			int responseCode;
			
			try {
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
				
				conn.setDoOutput(true);
				conn.setUseCaches(false);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
				conn.setRequestProperty("Content-Length", "0");
				conn.setRequestProperty("Auth", authToken);
	
				conn.getOutputStream().close();
				
				responseCode = conn.getResponseCode();
			} catch (IOException e) {
				responseCode = 503;
			}
			
			if ((responseCode >= 100) && (responseCode < 300)) {
				return true;
			} else if (responseCode == 304) {
				return true;
			} else if ((responseCode >= 500) && (responseCode <= 599)) {
				// retry
			} else {
				return false;
			}
			
			int nextAttempt = (backoffTimeMs / 2) + rand.nextInt(backoffTimeMs);
			
			try {
				Thread.sleep(nextAttempt);
			} catch (InterruptedException e) {
				return false;
			}
			
			if (backoffTimeMs < MAX_BACKOFF_MS) {
				backoffTimeMs *= 2;
			}
			
			--maxRetry;
		}
		
		return false;
	}
}
