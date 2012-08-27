package rk.gcm.demo.server.service;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnit;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import rk.gcm.demo.server.model.AuthException;
import rk.gcm.demo.server.model.R;
import rk.gcm.demo.server.model.User;
import rk.gcm.demo.server.util.PersistenceHelper;

@Service("authManager")
@Scope("singleton")
public class AuthManager {
	
	@PersistenceUnit
	private EntityManagerFactory emf;
	
	@Resource(name = "userManager")
	private UserManager userManager;
	
	@Resource(name = "passwordManager")
	private PasswordManager passwordManager;

	@Resource(name = "authTokenGenerator")
	private AuthTokenGenerator authTokenGenerator;
	
	@Resource(name = "auditLogger")
	private AuditLogger auditLogger;

	public User authenticate(String username, String password, String regId) throws AuthException {
		User u;
		
		EntityManager em = null;
		EntityTransaction tx = null;
		
		try {
			em = emf.createEntityManager();
			
			tx = PersistenceHelper.begin(em);

			try {
				u = authenticate(em, username, password, regId);
			} catch (AuthException e) {
				tx.commit();
				
				throw e;
			}
			
			tx.commit();
			
			em.detach(u);
		} catch (RuntimeException e) {
			PersistenceHelper.rollback(tx);

			throw e;
		} finally {
			PersistenceHelper.close(em);
		}
		
		return u;
	}
	
	private User authenticate(EntityManager em, String username, String password, String regId) throws AuthException {
		User u;
		
		try {
			u = userManager.findUserForUpdate(em, username, R.user_status.NORMAL);
		} catch (RuntimeException e) {
			throw e;
		}
		
		if (u == null) {
			throw new AuthException("username can't be found");
		}
		
		String userSID = u.getSID();
		
		if (!passwordManager.verify(password, u.getPassword())) {
			auditLogger.loginFail(em, userSID);
			
			throw new AuthException("invalid password");
		}
		
		u.setAuthToken(authTokenGenerator.generateBase64());
		u.setRegId(regId);
		
		auditLogger.loginSuccess(em, userSID);
		
		return u;
	}

	public void unauthenticate(String sid, String authToken) throws AuthException {
		//
	}
}
