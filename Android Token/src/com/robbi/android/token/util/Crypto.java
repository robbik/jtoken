package com.robbi.android.token.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.robbi.android.token.crypto.AES;

public abstract class Crypto {
	
	public static byte[] decrypt(byte[] cipher, String key) {
	    byte[] hkey = sha256(key);
		return decrypt(cipher, hkey);
	}
	
	public static byte[] decrypt(byte[] cipher, byte[] hkey) {
		AES aes = new AES();
		aes.init(hkey, null, false, false);
		
		return aes.doFinal(cipher);
	}
	
	public static byte[] encrypt(byte[] plain, String key) {
	    byte[] hkey = sha256(key);
		return encrypt(plain, hkey);
	}
	
	public static byte[] encrypt(byte[] plain, byte[] hkey) {
		AES aes = new AES();
		aes.init(hkey, null, false, true);
		
		return aes.doFinal(plain);
	}

	public static byte[] sha256(String key) {
		MessageDigest digest;
		
	    try {
	        digest = MessageDigest.getInstance("SHA-256");
	    } catch (NoSuchAlgorithmException e) {
	    	throw new UnsupportedOperationException("unsupported hash algorithm: SHA-256");
	    }

	    try {
			digest.update(key.getBytes("UTF-8"));
	    } catch (UnsupportedEncodingException e) {
	    	throw new UnsupportedOperationException("unsupported encoding: UTF-8");
	    }
		
		return digest.digest();
	}

	public static byte[] md5(String key) {
		MessageDigest digest;
		
	    try {
	        digest = MessageDigest.getInstance("MD5");
	    } catch (NoSuchAlgorithmException e) {
	    	throw new UnsupportedOperationException("unsupported hash algorithm: MD5");
	    }

	    try {
			digest.update(key.getBytes("UTF-8"));
	    } catch (UnsupportedEncodingException e) {
	    	throw new UnsupportedOperationException("unsupported encoding: UTF-8");
	    }
		
		return digest.digest();
	}
}
