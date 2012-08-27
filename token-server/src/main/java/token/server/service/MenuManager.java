package token.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import token.server.model.Menu;

@Service("menuManager")
@Scope("singleton")
public class MenuManager {

	public Map<String, Object> createBeanData(EntityManager em, Long leafMenuId) {
		List<Menu> menus = listToRoot(em, leafMenuId);
		if (menus.isEmpty()) {
			throw new IllegalArgumentException("menu " + leafMenuId + " can't be found");
		}
		
		Map<String, Object> bean = null;
		
		Menu track = null;
		Menu createFrom = null;
		
		// create bean data
		try {
			for (Menu m : menus) {
				track = m;
				
				bean = m.createBeanData();
				
				if (bean != null) {
					createFrom = m;
					break;
				}
			}
		} catch (Throwable t) {
			throw new RuntimeException("unable to create bean-data from menu " + track, t);
		}
		
		track = null;
		
		if (createFrom == null) {
			throw new RuntimeException("unable to create bean-data from menus " + menus);
		}

		// apply bean data
		try {
			for (Menu m : menus) {
				track = m;
				
				if (!createFrom.equals(m)) { // prevent duplicate apply
					m.applyBeanData(bean);
				}
			}
		} catch (Throwable t) {
			throw new RuntimeException("unable to apply bean-data with menu " + track, t);
		}
		
		track = null;
		
		// return the result
		return bean;
	}

	public void applyBeanData(EntityManager em, Long leafMenuId, Object bean) {
		List<Menu> menus = listToRoot(em, leafMenuId);
		if (menus.isEmpty()) {
			throw new IllegalArgumentException("menu " + leafMenuId + " can't be found");
		}
		
		Menu track = null;

		// apply bean data
		try {
			for (Menu m : menus) {
				track = m;
				
				m.applyBeanData(bean);
			}
		} catch (Throwable t) {
			throw new RuntimeException("unable to apply bean-data with menu " + track, t);
		}
		
		track = null;
	}

	public List<Menu> listToRoot(EntityManager em, Long menuId) {
		List<Menu> menus = new ArrayList<Menu>();
		
		Menu e = find(em, menuId);
		if (e != null) {
			menus.add(e);
			
			while ((e = findParent(em, e)) != null) {
				menus.add(e);
				
				menuId = e.getId();
			}
		}
		
		return menus;
	}

	public List<Menu> findRoots(EntityManager em) {
		return em.createNamedQuery("findMenuRoots", Menu.class).getResultList();
	}

	public List<Menu> findChildren(EntityManager em, Long menuId) {
		TypedQuery<Menu> q = em.createNamedQuery("findMenuChilds", Menu.class);
		q.setParameter("parentId", menuId);
		
		return q.getResultList();
	}

	public Menu findParent(EntityManager em, Menu menu) {
		if (menu == null) {
			return null;
		}
		
		Long parentId = menu.getParentId();
		if (parentId == null) {
			return null;
		}
		
		return em.find(Menu.class, parentId);
	}

	public Menu find(EntityManager em, Long menuId) {
		return em.find(Menu.class, menuId);
	}
}
