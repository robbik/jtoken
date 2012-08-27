package com.robbi.android.token;


public abstract class C {

	public static final String PACKAGE_NAME = "com.robbi.android.token";
	
	public static abstract class ntp {
		
		public static final String DEFAULT_HOST = "pool.ntp.org";
		
		public static final int DEFAULT_TIMEOUT = 20;
	}

	public static abstract class sync {
		
		public static final String TAG = "com.robbi.android.token#sync";
	}
	
	public static abstract class shared_preferences {
		
		public static final String NTP_HOST = "ntp-host";
		
		public static final String NTP_TIMEOUT = "ntp-timeout";
		
		public static final String EMAIL_ADDRESS = "email-address";
		
		public static final String PIN = "pin";
		
		public static final String SEED = "seed";
		
		public static final String SYNC_LAST_UPDATE = "sync-last-update";
		
		public static final String CLOCK_OFFSET = "clock-offset";
		
		public static final String BACKOFF_MS = "sync-backoff-ms";
		
		public static final String DATA_VERSION = "version";
		
		public static final String TOTP_WINDOW = "token-window";
	}
	
	public static abstract class server_uri {
		
		public static final String SIGN_IN = "https://www..com/accounts/sign-in";

		public static final String SIGN_UP = "https://www..com/accounts/sign-up";

		public static final String CHANGE_PASSWORD = "https://www..com/accounts/change-password";
		
		public static final String SYNC = "https://www..com/sync";
	}
	
	public static abstract class intent {
		
		public static final String EXTRA_COMPATIBILITY = "compatibility";

		public static final String EXTRA_APPLICATION = "app";

		public static final String EXTRA_EMAIL_ADDRESS = "email-address";
		
		public static final String EXTRA_CRYPT_ALGO = "crypt-algo";
		
		public static final String EXTRA_TOKEN_TYPE = "token-type";
		
		public static final String EXTRA_SEED = "seed";

		public static final String COMPATIBILITY_NONE = "None";
	}
}
