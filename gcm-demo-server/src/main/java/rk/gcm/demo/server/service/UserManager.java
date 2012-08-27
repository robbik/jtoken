package rk.gcm.demo.server.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceUnit;
import javax.persistence.TypedQuery;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import rk.gcm.demo.server.model.R;
import rk.gcm.demo.server.model.User;
import rk.gcm.demo.server.util.ObjectHelper;
import rk.gcm.demo.server.util.PersistenceHelper;

@Service("userManager")
@Scope("singleton")
public class UserManager {
	
	@PersistenceUnit
	private EntityManagerFactory emf;

	public User findUserForUpdate(String username, int status) {
		User u;
		
		EntityManager em = null;
		EntityTransaction tx = null;
		
		try {
			em = emf.createEntityManager();
			
			tx = PersistenceHelper.begin(em);

			u = findUserForUpdate(em, username, status);
			
			tx.commit();
		} catch (RuntimeException e) {
			PersistenceHelper.rollback(tx);
			throw e;
		} finally {
			PersistenceHelper.close(em);
		}
		
		return u;
	}
	
	public User findUserForUpdate(EntityManager em, String username, int status) {
		TypedQuery<User> q = em.createQuery(
				"SELECT OBJECT(o) FROM User o WHERE o.username = :username AND o.status = :status", User.class);
		
		q.setParameter("username", username);
		q.setParameter("status", R.user_status.NORMAL);
		
		q.setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT);
		
		List<User> l = q.getResultList();
		
		if (l.isEmpty()) {
			return null;
		} else {
			return l.get(0);
		}
	}

	public void updateAuthTokenAndGCM(String sid, String authToken, String gcm) {
		EntityManager em = null;
		EntityTransaction tx = null;
		
		try {
			em = emf.createEntityManager();
			
			tx = em.getTransaction();
			tx.begin();
			
			TypedQuery<User> q = em.createQuery(
					"UPDATE User SET authToken = :authToken, gcm = :gcm WHERE sid = :sid", User.class);
			
			q.setParameter("sid", sid);
			q.setParameter("gcm", gcm);
			q.setParameter("authToken", authToken);
			
			q.executeUpdate();
			
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null) {
				try {
					tx.rollback();
				} catch (Throwable t) {
					// do nothing
				}
			}
			
			throw e;
		} finally {
			if (em != null) {
				try {
					em.close();
				} catch (Throwable t) {
					// do nothing
				}
			}
		}
	}

	public boolean compareAndUpdateAuthTokenAndGCM(String sid, String cauthToken, String cgcm, String authToken, String gcm) {
		EntityManager em = null;
		EntityTransaction tx = null;
		
		boolean updated = false;
		
		try {
			em = emf.createEntityManager();
			
			tx = em.getTransaction();
			tx.begin();
			
			User u = em.find(User.class, sid);
			if (u != null) {
				if (ObjectHelper.equals(cauthToken, u.getAuthToken()) && ObjectHelper.equals(cgcm, u.getRegId())) {
					updated = true;
					
					u.setAuthToken(authToken);
					u.setRegId(gcm);
				}
			}
			
			em.flush();
			
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null) {
				try {
					tx.rollback();
				} catch (Throwable t) {
					// do nothing
				}
			}
			
			throw e;
		} finally {
			if (em != null) {
				try {
					em.close();
				} catch (Throwable t) {
					// do nothing
				}
			}
		}
		
		return updated;
	}

	public User findAuthToken(String authToken) {
		User result;
		
		EntityManager em = null;
		
		try {
			em = emf.createEntityManager();
			
			TypedQuery<User> q = em.createQuery(
					"SELECT OBJECT(o) FROM User o WHERE o.authToken = :authToken", User.class);
			
			q.setParameter("authToken", authToken);
			
			List<User> l = q.getResultList();
			
			if (l.isEmpty()) {
				result = null;
			} else {
				result = l.get(0);
			}
		} finally {
			if (em != null) {
				try {
					em.close();
				} catch (Throwable t) {
					// do nothing
				}
			}
		}
		
		return result;
	}
	
	public User findAuthTokenOrFail(String authToken) throws EntityNotFoundException {
		User u = findAuthToken(authToken);
		
		if (u == null) {
			throw new EntityNotFoundException("User {authToken=" + authToken + "}");
		}
		
		return u;
	}
}
