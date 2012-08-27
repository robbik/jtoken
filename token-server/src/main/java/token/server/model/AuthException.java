package token.server.model;

public class AuthException extends Exception {

	private static final long serialVersionUID = 9014883254708738699L;

	public AuthException() {
		super();
	}

	public AuthException(String message, Throwable cause) {
		super(message, cause);
	}

	public AuthException(String message) {
		super(message);
	}

	public AuthException(Throwable cause) {
		super(cause);
	}
}
