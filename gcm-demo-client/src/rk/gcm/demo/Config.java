package rk.gcm.demo;

public abstract class Config {

	public static final String GCM_SENDER_ID = "AIzaSyCqYzySQ_q6zA6_ohgX0ruB5DKR3MzcDzk";

	public static final String INTENT_SERVER_REGISTRATION_CALLBACK = "intent.rk.gcm.demo.Main.SIGNING_IN_LISTENER";

	public static final String EXTRA_REASON = "reason";

	public static final String EXTRA_ERROR_ID = "error-id";

	public static final String EXTRA_REASON_CONNECTION_ERROR = "not-connected";

	public static final String EXTRA_REASON_AUTH_FAILED = "auth-failed";

	public static final String EXTRA_REASON_AUTH_SUCCEED = "auth-succeed";

	public static final String EXTRA_REASON_LOGOUT = "logout";

	public static final long DEFAULT_BACKOFF = 30000;
}
