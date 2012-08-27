package token.server.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import token.server.R;
import token.server.model.AuthException;
import token.server.model.User;
import token.server.util.JpaHelper;

@Service("authManager")
@Scope("singleton")
public class AuthManager {
	
	private static final long AUTH_TOKEN_VALID_PERIOD = TimeUnit.DAYS.toMillis(7);

	@Resource(name = "userManager")
	private UserManager userManager;
	
	@Resource(name = "passwordManager")
	private PasswordManager passwordManager;

	@Resource(name = "authTokenGenerator")
	private AuthTokenGenerator authTokenGenerator;
	
	@Resource(name = "auditLogger")
	private AuditLogger auditLogger;
	
	public User authenticate(String username, String password) throws AuthException {
		User u;
		
		EntityManager em = null;
		EntityTransaction tx = null;
		
		try {
			em = JpaHelper.createEntityManager();
			tx = JpaHelper.begin(em);

			try {
				u = authenticate(em, username, password);
			} catch (AuthException e) {
				tx.commit();
				
				throw e;
			}
			
			em.flush();
			tx.commit();
			
			em.detach(u);
		} catch (RuntimeException e) {
			JpaHelper.rollback(tx);
			throw e;
		} finally {
			JpaHelper.close(em);
		}
		
		return u;
	}

	public User deauthenticate(String authToken) throws AuthException {
		User u;
		
		EntityManager em = null;
		EntityTransaction tx = null;
		
		try {
			em = JpaHelper.createEntityManager();
			tx = JpaHelper.begin(em);

			try {
				u = deauthenticate(em, authToken);
			} catch (AuthException e) {
				tx.commit();
				throw e;
			}

			em.flush();
			tx.commit();
			
			em.detach(u);
		} catch (RuntimeException e) {
			JpaHelper.rollback(tx);
			throw e;
		} finally {
			JpaHelper.close(em);
		}
		
		return u;
	}

	public User changePassword(String username, String password, String newpassword) throws AuthException {
		User u;
		
		EntityManager em = null;
		EntityTransaction tx = null;
		
		try {
			em = JpaHelper.createEntityManager();
			tx = JpaHelper.begin(em);

			try {
				u = changePassword(em, username, password, newpassword);
			} catch (AuthException e) {
				tx.commit();
				throw e;
			}

			em.flush();
			tx.commit();
			
			em.detach(u);
		} catch (RuntimeException e) {
			JpaHelper.rollback(tx);
			throw e;
		} finally {
			JpaHelper.close(em);
		}
		
		return u;
	}
	
	public User authenticate(EntityManager em, String username, String password) throws AuthException {
		User u;
		
		try {
			u = userManager.findByUsernameAndStatus(em, username, R.user_status.NORMAL);
		} catch (RuntimeException e) {
			throw e;
		}
		
		if (u == null) {
			auditLogger.loginFail(em, null, "username " + username + " can't be found");
			
			throw new AuthException("username" + username + " can't be found");
		}
		
		Long userId = u.getId();
		
		if (!passwordManager.verify(password, u.getPassword())) {
			auditLogger.loginFail(em, userId, "invalid password");
			
			throw new AuthException("invalid password");
		}
		
		u.setAuthToken(authTokenGenerator.generateBase64());
		u.setExpiresTime(System.currentTimeMillis() + AUTH_TOKEN_VALID_PERIOD);
		
		auditLogger.loginSuccess(em, userId);
		
		return u;
	}

	public User deauthenticate(EntityManager em, String authToken) throws AuthException {
		User u;
		
		try {
			u = userManager.findByAuthToken(em, authToken);
		} catch (RuntimeException e) {
			throw e;
		}
		
		if (u == null) {
			auditLogger.logoutFail(authToken, "auth-token can't be found");
			throw new AuthException("auth-token can't be found");
		}
		
		u.setAuthToken(null);
		u.setExpiresTime(0);
		
		auditLogger.logoutSuccess(authToken, u.getId());
		
		return u;
	}

	public User changePassword(EntityManager em, String username, String password, String newpassword) throws AuthException {
		User u;
		
		try {
			u = userManager.findByUsernameAndStatus(em, username, R.user_status.NORMAL);
		} catch (RuntimeException e) {
			throw e;
		}
		
		if (u == null) {
			auditLogger.changePasswordFail(null, "username " + username + " can't be found");
			
			throw new AuthException("username " + username + " can't be found");
		}
		
		u.setPassword(passwordManager.store(newpassword));
		
		auditLogger.changePasswordSuccess(u.getId());
		
		return u;
	}

	public User verifyAuthToken(EntityManager em, String authToken) {
		TypedQuery<User> q = em.createNamedQuery("findByAuthToken", User.class);
		
		q.setParameter("authToken", authToken);
		q.setParameter("now", System.currentTimeMillis());
		
		List<User> l = q.getResultList();
		
		if (l.isEmpty()) {
			return null;
		} else {
			User user = l.get(0);
			user.setExpiresTime(System.currentTimeMillis() + AUTH_TOKEN_VALID_PERIOD);
			
			return user;
		}
	}
}
