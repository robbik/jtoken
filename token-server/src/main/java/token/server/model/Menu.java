package token.server.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import ognl.Ognl;
import token.server.util.ObjectHelper;

@Entity(name = "Menu")
@Table(name = "tbl_menu")
@SequenceGenerator(name = "MenuSEQ", sequenceName = "seq_menu")
@Access(AccessType.FIELD)
@NamedQueries({
	@NamedQuery(
			name = "findMenuChilds",
			query = "SELECT OBJECT(o) FROM Menu o WHERE (o.parentId IS NOT NULL) AND (o.parentId = :parentId)"
	),
	@NamedQuery(
			name = "findMenuRoots",
			query = "SELECT OBJECT(o) FROM Menu o WHERE o.parentId IS NULL"
	)
})
public class Menu {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MenuSEQ")
	private Long id;

	@Basic
	@Column(name = "parent_id", nullable = true)
	private Long parentId;
	
	@Basic
	@Column(name = "bean_data", length = 2048)
	private String beanData;

	@Basic
	@Column(name = "text_id", length = 120)
	private String textId;
	
	@Transient
	private transient Object cbeanData;
	
	public Long getId() {
		return id;
	}
	
	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public String getBeanData() {
		return beanData;
	}

	public void setBeanData(String beanData) {
		if (!ObjectHelper.equals(this.beanData, beanData)) {
			this.beanData = beanData;
			
			cbeanData = null;
		}
	}

	public String getTextId() {
		return textId;
	}

	public void setTextId(String textId) {
		this.textId = textId;
	}
	
	public void optimize() throws Exception {
		if ((cbeanData == null) && (beanData != null)) {
			cbeanData = Ognl.parseExpression(beanData);
		}
	}
	
	public Map<String, Object> createBeanData() throws Exception {
		optimize();
		
		Map<String, Object> root = new HashMap<String, Object>();
		
		if (cbeanData != null) {
			Ognl.getValue(cbeanData, root);
		}
		
		return root;
	}

	public void applyBeanData(Object root) throws Exception {
		optimize();
		
		if (cbeanData != null) {
			Ognl.getValue(cbeanData, root);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		
		if (o instanceof Menu) {
			return ObjectHelper.equals(id, ((Menu) o).id);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return id == null ? 0 : id.hashCode();
	}
}
