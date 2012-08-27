package token.server;

public abstract class R {

	public abstract static class user_status {

		public static final int NORMAL = 0;

		public static final int LOCKED = 1;
	}

	public abstract static class message {

		public static final String transaction_date = "transaction_date";
		
		public static final String correlation = "correlation";
	}
}
