package rk.gcm.demo.server.service;

import javax.persistence.EntityManager;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service("auditLogger")
@Scope("singleton")
public class AuditLogger {

	public void loginSuccess(EntityManager em, String userSID) {
		//
	}

	public void loginFail(EntityManager em, String userSID) {
		//
	}

	public void logoutSuccess(String authToken, String userSID) {
		//
	}

	public void logoutFail(String authToken) {
		//
	}
}
