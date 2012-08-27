package com.robbi.android.token.util;

import java.io.IOException;
import java.io.InputStream;

public abstract class IOHelper {

	public static void consume(InputStream in) {
		byte[] tmp = new byte[256];
		
		try {
			while (in.read(tmp) >= 0) {
				// do nothing
			}
		} catch (IOException e) {
			// do nothing
		}
	}
}
