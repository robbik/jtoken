package com.robbi.android.token.util;

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Base64;

import com.robbi.android.token.C;

public class Database extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "com.robbi.android.token.db";

	private static final int DATABASE_VERSION = 1;

	public Database(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE seeds (seed_id TEXT PRIMARY KEY, "
				+ "valid_date INTEGER, " + // yyyyMMdd
				"expired_date INTEGER, " + // yyyyMMdd
				"seed_data TEXT);");
		
		db.execSQL("CREATE TABLE compatibilities (name TEXT PRIMARY KEY, crypt_algo TEXT, token_type INTEGER);");
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// do nothing
	}

	public String findSeed() {
		String data = null;
		
		long today;
		
		Calendar cal = Calendar.getInstance();
		today = (cal.get(Calendar.YEAR) * 10000) + (cal.get(Calendar.MONTH) * 100) + cal.get(Calendar.DATE);
		
		final SQLiteDatabase db = getWritableDatabase();
		
		db.beginTransaction();
		
		try {
			Cursor cursor = db.query("seeds", new String[] { "seed_data" },
					"(valid_date <= " + today + ") AND (expired_date >= " + today + ")",
					null, null, null, "expired_date DESC"); 

			if (cursor.moveToFirst()) {
				data = cursor.getString(0);
			}
			
			cursor.close();
			
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
		return data;
	}

	public Bundle findCompatibility(String name) {
		Bundle data = null;
		
		final SQLiteDatabase db = getWritableDatabase();
		
		db.beginTransaction();
		
		try {
			Cursor cursor = db.query("compatibilities", new String[] { "crypt_algo", "token_type" }, "name = ?", new String[] { name }, null, null, null); 

			if (cursor.moveToFirst()) {
				data = new Bundle();
				
				data.putString(C.intent.EXTRA_CRYPT_ALGO, cursor.getString(0));
				data.putInt(C.intent.EXTRA_TOKEN_TYPE, cursor.getInt(1));
			}
			
			cursor.close();
			
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
		return data;
	}

	public void putIfNotExists(String seedId, long validDate, long expiredDate,
			String data) {
		final SQLiteDatabase db = getWritableDatabase();
		
		db.beginTransaction();

		boolean exists;
		
		try {
			Cursor cursor = db.query("seeds", new String[] { "seed_id" }, "seed_id = ?s", new String[] { seedId }, null, null, null);
			exists = cursor.moveToFirst();
			cursor.close();
			
			if (!exists) {
				ContentValues values = new ContentValues(4);
				values.put("seed_id", seedId);
				values.put("valid_date", Long.valueOf(validDate));
				values.put("expired_date", Long.valueOf(expiredDate));
				values.put("seed_data", data);
				
				db.insertOrThrow("seeds", "", values);
			}
			
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public void put(String compatibilityName, String algorithm, int tokenType) {
		final SQLiteDatabase db = getWritableDatabase();
		
		db.beginTransaction();

		boolean exists;
		
		try {
			Cursor cursor = db.query("compatibilities", new String[] { "name" }, "name = ?", new String[] { compatibilityName }, null, null, null);
			exists = cursor.moveToFirst();
			cursor.close();
			
			ContentValues values = new ContentValues(4);
			values.put("name", compatibilityName);
			values.put("crypt_algo", algorithm);
			values.put("token_type", tokenType);

			if (exists) {
				db.replaceOrThrow("compatibilities", null, values);
			} else {
				db.insertOrThrow("seeds", "", values);
			}
			
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
	
	public void reencrypt(String pin, String newpin) {
		byte[] hpin = Crypto.hash(pin);
		byte[] hnewpin = Crypto.hash(newpin);
		
		final SQLiteDatabase db = getWritableDatabase();
		
		db.beginTransaction();
		
		try {
			Cursor cursor = db.query("seeds", new String[] { "seed_id", "seed_data" }, null, null, null, null, null);
			
			if (cursor.moveToFirst()) {
				do {
					byte[] seedb = Base64.decode(cursor.getString(1), Base64.DEFAULT);
					
					seedb = Crypto.decrypt(seedb, hpin);
					seedb = Crypto.encrypt(seedb, hnewpin);
					
					db.up
				} while (cursor.moveToNext());
			}
			
			cursor.close();
			
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public void cleanup() {
		long maxExpiredDate;
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -1);
		
		maxExpiredDate = (cal.get(Calendar.YEAR) * 10000) + (cal.get(Calendar.MONTH) * 100) + cal.get(Calendar.DATE);
		
		final SQLiteDatabase db = getWritableDatabase();
		
		db.beginTransaction();
		
		try {
			db.execSQL("DELETE FROM seeds WHERE expired_date < ".concat(String.valueOf(maxExpiredDate)));
			
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
}
