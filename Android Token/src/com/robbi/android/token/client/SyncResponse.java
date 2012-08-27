package com.robbi.android.token.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SyncResponse {
	
	public String authToken;
	
	public long tokenWindow;
	
	public long version;
	
	public List<Seed> seeds;
	
	public List<Compatibility> compatibilities;

	private SyncResponse() {
		seeds = new ArrayList<SyncResponse.Seed>();
		compatibilities = new ArrayList<SyncResponse.Compatibility>();
	}
	
	public static SyncResponse parse(InputStreamReader in) throws IOException {
		SyncResponse result = new SyncResponse();
		
		BufferedReader r = new BufferedReader(in);
		String line;
		
		while ((line = r.readLine()) != null) {
			if (line.startsWith("auth=")) {
				result.authToken = line.substring(5);
			} else if (line.startsWith("token_window=")) {
				result.tokenWindow = Long.parseLong(line.substring(13));
			} else if (line.startsWith("version=")) {
				result.version = Long.parseLong(line.substring(8));
			} else if (line.startsWith("seed=")) {
				result.seeds.add(Seed.parse(line.substring(5)));
			} else if (line.startsWith("compatibility=")) {
				result.compatibilities.add(Compatibility.parse(line.substring(14)));
			}
		}
		
		return result;
	}
	
	public static class Seed {
		public String seedId;
		
		public long validDate;
		
		public long expiredDate;
		
		public String data;
		
		static Seed parse(String line) throws IOException {
			String[] parts = line.split(" ");
			
			Seed result = new Seed();
			
			result.seedId = parts[0];
			result.validDate = Long.parseLong(parts[1]);
			result.expiredDate = Long.parseLong(parts[2]);
			result.data = parts[3];
			
			return result;
		}
	}
	
	public static class Compatibility {
		public String name;
		
		public String algo;
		
		public int tokenType;
		
		static Compatibility parse(String line) throws IOException {
			String[] parts = line.split(";");
			
			Compatibility result = new Compatibility();
			
			result.name = parts[0].trim();
			result.algo = parts[1].trim();
			result.tokenType = Integer.parseInt(parts[2].trim());
			
			return result;
		}
	}
}
