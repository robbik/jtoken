package com.robbi.android.token.sync;

import static android.app.AlarmManager.ELAPSED_REALTIME_WAKEUP;

import java.net.HttpURLConnection;
import java.util.Random;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.os.SystemClock;

import com.robbi.android.token.App;
import com.robbi.android.token.C;
import com.robbi.android.token.client.HttpException;
import com.robbi.android.token.client.ServerHelper;
import com.robbi.android.token.client.SyncRequest;
import com.robbi.android.token.client.SyncResponse;
import com.robbi.android.token.sntp.SntpClient;
import com.robbi.android.token.util.Database;

public class SyncIntentService extends IntentService {
	
	private static PowerManager.WakeLock wakeLock = null;
	
	private static final int DEFAULT_BACKOFF_MS = 3 * 000;
	
	private static final int MAX_BACKOFF_MS = 3600 * 100;
	
	private static final long EACH_DAY_INTERVAL = 24 * 3600 * 1000;
	
	private SharedPreferences prefs;
	
	private AlarmManager am;
	
	private ConnectivityManager cm;
	
	private Random random;
	
	public SyncIntentService(String name) {
		super(C.sync.TAG);
		
		random = new Random(System.currentTimeMillis());
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		prefs = getSharedPreferences(C.PACKAGE_NAME, Context.MODE_PRIVATE);
		
		am = (AlarmManager) getSystemService(ALARM_SERVICE);
		
		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	}
	
	private static synchronized PowerManager.WakeLock getWakeLock(Context context) {
		if (wakeLock == null) {
			final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, C.sync.TAG);
			wakeLock.setReferenceCounted(true);
		}
		
		return wakeLock;
	}
	
	static void runIntentInService(Context context, Intent intent) {
		getWakeLock(context).acquire();
		
		intent.setClass(context, SyncIntentService.class);
		
		context.startService(intent);
	}

	private void scheduleNext(boolean backoff) {
		long nextAttempt;
		
		if (backoff) {
			int backoffTimeMs = prefs.getInt(C.shared_preferences.BACKOFF_MS, DEFAULT_BACKOFF_MS);
			
			nextAttempt = backoffTimeMs / 2 + random.nextInt(backoffTimeMs);
			
			if (backoffTimeMs < MAX_BACKOFF_MS) {
				Editor editor = prefs.edit();
				editor.putInt(C.shared_preferences.BACKOFF_MS, backoffTimeMs);
				editor.commit();
			}
		} else {
			nextAttempt = EACH_DAY_INTERVAL;
		}
		
		Intent intent = new Intent(this, SyncBroadcastReceiver.class);
		PendingIntent operation = PendingIntent.getBroadcast(this, 0, intent, 0);
		
		am.set(ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + nextAttempt, operation);
	}
	
	private long syncTime() throws Exception {
		SntpClient sntp = new SntpClient();
		
		sntp.setHost(prefs.getString(C.shared_preferences.NTP_HOST, C.ntp.DEFAULT_HOST));
		sntp.setTimeout(prefs.getInt(C.shared_preferences.NTP_TIMEOUT, C.ntp.DEFAULT_TIMEOUT));
		
		return sntp.sync();
	}
	
	private SyncResponse syncSeed() throws HttpException, Exception {
		SyncRequest req = new SyncRequest();
		req.version = prefs.getInt(C.shared_preferences.DATA_VERSION, 0);
		req.deviceId = App.getDeviceId(this);
		
		return ServerHelper.sync(req);
	}
	
	private void dosync() {
		long clockOffset;
		
		try {
			clockOffset = syncTime();
		} catch (Exception e) {
			scheduleNext(true);
			return;
		}
		
		SyncResponse resp = null;
		
		try {
			resp = syncSeed();
		} catch (HttpException e) {
			switch (e.getStatusCode()) {
			case HttpURLConnection.HTTP_NOT_MODIFIED:
				break;
			default:
				scheduleNext(true);
				return;
			}
		} catch (Exception e) {
			scheduleNext(true);
			return;
		}
		
		Editor editor = prefs.edit();
		editor.putLong(C.shared_preferences.CLOCK_OFFSET, clockOffset);
		
		if (resp != null) {
			editor.putLong(C.shared_preferences.TOTP_WINDOW, resp.tokenWindow);
			editor.putLong(C.shared_preferences.DATA_VERSION, resp.version);
		}
		
		editor.commit();

		if (resp != null) {
			Database db = new Database(this);
			
			// cleanup old entries
			db.cleanup();
			
			// put new entries
			for (SyncResponse.Seed seed : resp.seeds) {
				db.putIfNotExists(seed.seedId, seed.validDate, seed.expiredDate, seed.data);
			}
			
			for (SyncResponse.Compatibility e : resp.compatibilities) {
				db.put(e.name, e.algo, e.tokenType);
			}

			db.close();
		}

		scheduleNext(false);
	}
	
	protected void onHandleIntent(Intent intent) {
        try {
        	final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        	
        	if ((netInfo != null) && netInfo.isConnected()) {
        		dosync();
        	} else {
        		scheduleNext(true);
        	}
        } finally {
        	getWakeLock(this).release();
        }
	}
}
