package token.server.service;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import token.server.R;
import token.server.model.User;
import token.server.model.UserAccount;
import token.server.util.ObjectHelper;

@Service("userManager")
@Scope("singleton")
public class UserManager {
	
	public UserAccount findAccountByAccountNumber(User u, String accountNumber) {
		Set<UserAccount> accounts = u.getAccounts();
		
		if (accounts == null) {
			return null;
		}
		
		for (UserAccount ua : accounts) {
			if (ObjectHelper.equals(ua.getAccountNumber(), accountNumber)) {
				return ua;
			}
		}
		
		return null;
	}

	public User findByUsernameAndStatus(EntityManager em, String username, int status) {
		TypedQuery<User> q = em.createNamedQuery("findByUsernameAndStatus", User.class);
		
		q.setParameter("username", username);
		q.setParameter("status", R.user_status.NORMAL);
		
		List<User> l = q.getResultList();
		
		if (l.isEmpty()) {
			return null;
		} else {
			return l.get(0);
		}
	}
	
	public User findByAuthToken(EntityManager em, String authToken) {
		TypedQuery<User> q = em.createNamedQuery("findByAuthToken", User.class);
		
		q.setParameter("authToken", authToken);
		q.setParameter("now", System.currentTimeMillis());
		
		List<User> l = q.getResultList();
		
		if (l.isEmpty()) {
			return null;
		} else {
			return l.get(0);
		}
	}
}
