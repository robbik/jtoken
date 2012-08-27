package com.robbi.android.token.util;

import java.math.BigInteger;

public abstract class Hex {
	
	private static final String HEX = "0123456789abcdef";

	public static String asHex(byte[] bytes) {
		int length = bytes.length;
		
		char[] chars = new char[length * 2];
		
		for (int i = 0, j = 0; i < length; ++i) {
			chars[j++] = HEX.charAt((i & 0xF0) >> 4);
			chars[j++] = HEX.charAt(i & 0x0F);
		}
		
		return new String(chars);
	}
	
	public static byte[] asBytes(String hex) {
        // Adding one byte to get the right conversion
        // Values starting with "0" can be converted
        byte[] bArray = new BigInteger("10" + hex,16).toByteArray();

        // Copy all the REAL bytes, not the "first"
        byte[] ret = new byte[bArray.length - 1];
        
        for (int i = 0; i < ret.length; i++) {
            ret[i] = bArray[i+1];
        }
        
        return ret;
    }
}
