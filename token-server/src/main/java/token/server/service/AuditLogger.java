package token.server.service;

import javax.persistence.EntityManager;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service("auditLogger")
@Scope("singleton")
public class AuditLogger {

	public void loginSuccess(EntityManager em, Long userId) {
		//
	}

	public void loginFail(EntityManager em, Long userId, String message) {
		//
	}

	public void logoutSuccess(String authToken, Long userId) {
		//
	}

	public void logoutFail(String authToken, String message) {
		//
	}

	public void changePasswordSuccess(Long userId) {
		//
	}

	public void changePasswordFail(Long userId, String message) {
		//
	}
}
