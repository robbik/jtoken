package rk.gcm.demo.server.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import rk.gcm.demo.server.util.ObjectHelper;

@Entity(name = "User")
@Table(name = "user")
public class User {
	
	@Id
	@Column(name = "sid", length = 90, nullable = false, updatable = false)
	private String sid;
	
	@Basic
	@Column(name = "username", length = 120, nullable = false)
	private String username;
	
	@Basic
	@Column(name = "passwd", length = 120, nullable = false)
	private String password;
	
	@Basic
	@Column(name = "status")
	private int status;
	
	@Basic
	@Column(name = "auth_token", length = 90, nullable = false, unique = true)
	private String authToken;
	
	@Basic
	@Column(name = "reg_id", length = 90, nullable = false, unique = true)
	private String regId;
	
	public String getSID() {
		return sid;
	}
	
	public void setSID(String sid) {
		this.sid = sid;
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
	
	public String getRegId() {
		return regId;
	}
	
	public void setRegId(String regId) {
		this.regId = regId;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		
		if (o instanceof User) {
			return ObjectHelper.equals(sid, ((User) o).sid);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return sid == null ? 0 : sid.hashCode();
	}
}
