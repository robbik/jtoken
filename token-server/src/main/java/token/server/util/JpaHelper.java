package token.server.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public abstract class JpaHelper {
	
	private static final String DEFAULT_PERSISTENT_UNIT_NAME = "mobile-banking-server";
	
	private static volatile EntityManagerFactory emf;
	
	public static EntityManager createEntityManager() {
		return createEntityManager(DEFAULT_PERSISTENT_UNIT_NAME);
	}
	
	public static EntityManager createEntityManager(String persistentUnitName) {
		if (emf == null) {
			emf = Persistence.createEntityManagerFactory(persistentUnitName);
		}
		
		return emf.createEntityManager();
	}
	
	public static EntityTransaction begin(EntityManager em) {
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		
		return tx;
	}

	public static void rollback(EntityTransaction tx) {
		if (tx != null) {
			try {
				tx.rollback();
			} catch (Throwable t) {
				// do nothing
			}
		}
	}

	public static void close(EntityManager em) {
		if (em != null) {
			try {
				em.close();
			} catch (Throwable t) {
				// do nothing
			}
		}
	}
}
