package token.server.service;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import token.server.util.Bytes;
import token.server.util.ObjectHelper;

@Service("passwordManager")
@Scope("singleton")
public class PasswordManager {
	
	private static final Logger log = LoggerFactory.getLogger(PasswordManager.class);
	
	private static final Charset UTF8;
	
	private static final String DIGEST_ALGO;
	
	static {
		UTF8 = Charset.forName("UTF-8");
		
		MessageDigest md = getMessageDigest("SHA-256", "SHA-1", "MD5");
		if (md == null) {
			throw new Error(
					"at least one of following hash algorithms must be supported: SHA-256, SHA-1, MD5");
		}
		
		DIGEST_ALGO = md.getAlgorithm();
		
		log.info("Password manager is using " + DIGEST_ALGO + " hash algorithm");
	}
	
	private static MessageDigest getMessageDigest(String ... algs) {
		for (String alg : algs) {
			try {
				return MessageDigest.getInstance(alg);
			} catch (NoSuchAlgorithmException e) {
				// do nothing
			}
		}
		
		return null;
	}

	public boolean verify(String entered, String stored) {
		MessageDigest md = getMessageDigest(DIGEST_ALGO);
		
		md.reset();
		md.update(entered.getBytes(UTF8));
		
		entered = Bytes.toString64(md.digest());
		
		System.out.println("verify: " + entered + " ? " + stored);
		
		return ObjectHelper.equals(entered, stored);
	}
	
	public String store(String entered) {
		MessageDigest md = getMessageDigest(DIGEST_ALGO);
		
		md.reset();
		md.update(entered.getBytes(UTF8));
		
		return Bytes.toString64(md.digest());
	}
}
