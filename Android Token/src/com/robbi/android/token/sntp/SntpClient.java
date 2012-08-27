package com.robbi.android.token.sntp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.os.SystemClock;

public class SntpClient {
	
    private static final int ORIGINATE_TIME_OFFSET = 24;
    
    private static final int RECEIVE_TIME_OFFSET = 32;
    
    private static final int TRANSMIT_TIME_OFFSET = 40;
    
    private static final int NTP_PACKET_SIZE = 48;

    private static final int NTP_PORT = 123;
    
    private static final int NTP_MODE_CLIENT = 3;
    
    private static final int NTP_VERSION = 3;
    
    private static final long OFFSET_1900_TO_1970 = ((365L * 70L) + 17L) * 24L * 60L * 60L;
    
    private String host;
    
    private InetAddress address;
    
    private int timeout;
    
    public SntpClient() {
    	host = null;
    	address = null;
    	
    	timeout = 30;
    }
    
    public void setHost(String host) {
    	this.host = host;
    	this.address = null;
    }
    
    public void setHost(InetAddress address) {
    	this.host = address.getHostAddress();
    	this.address = address;
    }
    
    public void setTimeout(int timeout) {
    	this.timeout = timeout;
    }
    
    private long send(DatagramSocket sock, byte[] buffer) throws Exception {
    	buffer[0] = NTP_MODE_CLIENT | (NTP_VERSION << 3);
    	
    	DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, NTP_PORT);
    	
    	long t0 = System.currentTimeMillis();
    	long tck0 = SystemClock.elapsedRealtime();
    	
    	writeTimestamp(buffer, TRANSMIT_TIME_OFFSET, t0);

    	sock.send(request);
    	
    	return tck0;
    }
    
    private long receive(DatagramSocket sock, byte[] buffer, long tck0) throws Exception {
		DatagramPacket response = new DatagramPacket(buffer, buffer.length);
		sock.receive(response);
		
		long tck1 = SystemClock.elapsedRealtime();
		
		long t1 = readTimestamp(buffer, ORIGINATE_TIME_OFFSET);
		long t2 = readTimestamp(buffer, RECEIVE_TIME_OFFSET);
		long t3 = readTimestamp(buffer, TRANSMIT_TIME_OFFSET);
		long t4 = t1 + (tck1 - tck0);
		
		return NtpTime.calculateOffset(t1, t2, t3, t4);
    }
    
	public long sync() throws Exception {
		DatagramSocket sock = null;
		
		long offset;
		
		try {
			if (address == null) {
				address = InetAddress.getByName(host);
			}
			
			sock = new DatagramSocket();
			
			if (timeout > 0) {
				sock.setSoTimeout(timeout);
			}
			
			byte[] buffer = new byte[NTP_PACKET_SIZE];
			
			long tck0 = send(sock, buffer);
			offset = receive(sock, buffer, tck0);
		} catch (Exception e) {
			throw e;
		} finally {
			if (sock != null) {
				try {
					sock.close();
				} catch (Throwable t) {
					// do nothing
				}
			}
		}
		
		return offset;
	}
    
    private static long read32(byte[] buffer, int offset) {
    	byte b0 = buffer[offset];
    	byte b1 = buffer[offset + 1];
    	byte b2 = buffer[offset + 2];
    	byte b3 = buffer[offset + 3];
    	
    	// convert signed bytes to unsigned values
    	int i0 = ((b0 & 0x80) == 0x80 ? (b0 & 0x7F) + 0x80 : b0);
    	int i1 = ((b1 & 0x80) == 0x80 ? (b1 & 0x7F) + 0x80 : b1);
    	int i2 = ((b2 & 0x80) == 0x80 ? (b2 & 0x7F) + 0x80 : b2);
    	int i3 = ((b3 & 0x80) == 0x80 ? (b3 & 0x7F) + 0x80 : b3);
    	
    	return ((long) i0 << 24) + ((long) i1 << 16) + ((long) i2 << 8) + (long) i3;
    }
    
    private static long readTimestamp(byte[] buffer, int offset) {
    	long seconds = read32(buffer, offset);
    	long fraction = read32(buffer, offset + 4);
    	
    	if (seconds < OFFSET_1900_TO_1970) {
    		return -1;
    	}
    	
    	return ((seconds - OFFSET_1900_TO_1970) * 1000) + ((fraction * 1000L) / 0x100000000L);
    }
    
    private static void writeTimestamp(byte[] buffer, int offset, long time) {
    	long seconds = time / 1000L;
    	long milliseconds = time - seconds * 1000L;
    	
    	seconds += OFFSET_1900_TO_1970;
    	
    	// write seconds in big endian format
    	buffer[offset++] = (byte)(seconds >> 24);
    	buffer[offset++] = (byte)(seconds >> 16);
    	buffer[offset++] = (byte)(seconds >> 8);
    	buffer[offset++] = (byte)(seconds >> 0);
    	
    	long fraction = milliseconds * 0x100000000L / 1000L;
    	
    	// write fraction in big endian format
    	buffer[offset++] = (byte)(fraction >> 24);
    	buffer[offset++] = (byte)(fraction >> 16);
    	buffer[offset++] = (byte)(fraction >> 8);
    	
    	// low order bits should be random data
    	buffer[offset++] = (byte)(Math.random() * 255.0);
    }
}
