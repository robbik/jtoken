package token.server.service;

import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import token.server.R;
import token.server.model.AuthException;
import token.server.model.User;
import token.server.model.UserAccount;
import token.server.te.AsyncCallback;
import token.server.te.TESender;
import token.server.util.JpaHelper;

@Service("asyncHelloManager")
@Scope("singleton")
public class AsyncHelloManager {
	
	@Resource(name = "authManager")
	private AuthManager authManager;
	
	@Resource(name = "userManager")
	private UserManager userManager;
	
	@Resource(name = "menuManager")
	private MenuManager menuManager;
	
	@Resource(name = "TESender")
	private TESender sender;
	
	public void hello(AsyncCallback callback, String authToken, Long menuId, String accountNumber, String customerId) throws AuthException, Exception {
		EntityManager em = null;
		EntityTransaction tx = null;
		
		try {
			em = JpaHelper.createEntityManager();
			tx = JpaHelper.begin(em);

			hello(em, callback, authToken, menuId, accountNumber, customerId);
			
			em.flush();
			tx.commit();
		} catch (Exception e) {
			JpaHelper.rollback(tx);
			throw e;
		} finally {
			JpaHelper.close(em);
		}
	}
	
	public void hello(EntityManager em, AsyncCallback callback, String authToken, Long menuId, String accountNumber, String customerId) throws AuthException, Exception {
		User u = authManager.verifyAuthToken(em, authToken);
		if (u == null) {
			throw new AuthException("auth-token can't be found");
		}
		
		UserAccount ua = userManager.findAccountByAccountNumber(u, accountNumber);
		if (ua == null) {
			throw new IllegalArgumentException("user " + u.getId() + " has no account " + accountNumber);
		}
		
		// create hello bean and apply menu's bean-data
		Map<String, Object> bean = menuManager.createBeanData(em, menuId);
		
		// complete inquiry bean
		Date now = new Date();
		
		bean.put(R.message.transaction_date, now);
		
		// send hello bean to back-end
		sender.send(callback, bean, false);
	}
}
