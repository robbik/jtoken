package token.server.model;

import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import token.server.util.ObjectHelper;

@Entity(name = "User")
@Table(name = "tbl_user")
@SequenceGenerator(name = "UserSEQ", sequenceName = "seq_user")
@Access(AccessType.FIELD)
@NamedQueries({
	@NamedQuery(
			name = "findByUsernameAndStatus",
			query = "SELECT OBJECT(o) FROM User o WHERE o.username = :username AND o.status = :status"),
	@NamedQuery(
			name = "findByAuthToken",
			query = "SELECT OBJECT(o) FROM User o WHERE o.authToken = :authToken AND o.expiresTime > :now")
})
public class User {
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "UserSEQ")
	private Long id;
	
	@Basic
	@Column(name = "username", length = 120, nullable = false)
	private String username;
	
	@Basic
	@Column(name = "passwd", length = 120, nullable = false)
	private String password;
	
	@Basic
	@Column(name = "firstname", length = 120, nullable = false)
	private String firstname;
	
	@Basic
	@Column(name = "lastname", length = 120, nullable = true)
	private String lastname;
	
	@Basic
	@Column(name = "status")
	private int status;
	
	@Basic
	@Column(name = "auth_token", length = 90, nullable = true)
	private String authToken;
	
	@Basic
	@Column(name = "expires_time", nullable = false)
	private long expiresTime;
	
	@Basic
	@Column(name = "device_id", length = 120, nullable = true)
	private String deviceId;
	
	@OneToMany(orphanRemoval = true)
	@JoinColumn(name = "user_id")
	private Set<UserAccount> accounts;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public String getAuthToken() {
		return authToken;
	}
	
	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}
	
	public long getExpiresTime() {
		return expiresTime;
	}

	public void setExpiresTime(long expiresTime) {
		this.expiresTime = expiresTime;
	}
	
	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	public Set<UserAccount> getAccounts() {
		return accounts;
	}

	public void setAccounts(Set<UserAccount> accounts) {
		this.accounts = accounts;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		
		if (o instanceof User) {
			return ObjectHelper.equals(id, ((User) o).id);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return id == null ? 0 : id.hashCode();
	}
}
