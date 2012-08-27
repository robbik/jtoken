package rk.gcm.demo.server.service;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import rk.gcm.demo.server.util.Bytes;
import rk.gcm.demo.server.util.ObjectHelper;

@Service("passwordManager")
@Scope("singleton")
public class PasswordManager {
	
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
		
		return ObjectHelper.equals(Bytes.toString64(md.digest()), stored);
	}
	
	public String store(String entered) {
		MessageDigest md = getMessageDigest(DIGEST_ALGO);
		
		md.reset();
		md.update(entered.getBytes(UTF8));
		
		return Bytes.toString64(md.digest());
	}
}
