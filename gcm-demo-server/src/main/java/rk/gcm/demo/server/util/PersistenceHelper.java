package rk.gcm.demo.server.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public abstract class PersistenceHelper {
	
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
