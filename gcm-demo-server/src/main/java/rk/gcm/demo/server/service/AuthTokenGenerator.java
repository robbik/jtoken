package rk.gcm.demo.server.service;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import rk.gcm.demo.server.util.Bytes;

@Service("authTokenGenerator")
@Scope("singleton")
public class AuthTokenGenerator {
	
	private final AtomicReference<byte[]> prefixRef;
	
	private final SecureRandom srand;
	
	private final AtomicInteger counter;
	
	public AuthTokenGenerator() {
		byte[] uuid = Bytes.asBytes(UUID.randomUUID());
		
		prefixRef = new AtomicReference<byte[]>(uuid);
		srand = new SecureRandom(uuid);
		
		counter = new AtomicInteger(0);
	}
	
	public byte[] generateBytes() {
		if (counter.compareAndSet(1000, 0)) {
			byte[] uuid = Bytes.asBytes(UUID.randomUUID());

			prefixRef.set(uuid);
			srand.setSeed(uuid);
		}

		byte[] b = new byte[28]; // 16 + 8 + 4 = 28

		System.arraycopy(prefixRef.get(), 0, b, 0, 16);
		Bytes.longToBytes(srand.nextLong(), b, 16);
		Bytes.intToBytes(counter.incrementAndGet(), b, 24);

		return b;
	}
	
	public String generateBase64() {
		return Bytes.toString64(generateBytes());
	}
}
