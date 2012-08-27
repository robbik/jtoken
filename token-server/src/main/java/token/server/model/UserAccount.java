package token.server.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import token.server.util.ObjectHelper;

@Entity(name = "UserAccount")
@Table(name = "tbl_user_account")
@Access(AccessType.FIELD)
@JsonIgnoreProperties({ "userId" })
public class UserAccount {

	@Id
	@Column(name = "account_number", length = 30, nullable = false, updatable = false)
	private String accountNumber;
	
	@Basic
	@Column(name = "card_number", length = 90, nullable = false, updatable = false)
	private String cardNumber;
	
	@Basic
	@Column(name = "user_id", nullable = false, updatable = false)
	private Long userId;

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	
	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		
		if (o instanceof UserAccount) {
			return ObjectHelper.equals(accountNumber, ((UserAccount) o).accountNumber);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return accountNumber == null ? 0 : accountNumber.hashCode();
	}
}
