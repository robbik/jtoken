package rk.gcm.demo;

import java.io.IOException;

import rk.experiment.c2dm.client.ServerRegistrar;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

public class GCMIntentService extends GCMBaseIntentService {

	public GCMIntentService() {
		super(Config.GCM_SENDER_ID);
	}

	protected void onMessage(Context context, Intent intent) {
		notify(context, intent.getStringExtra("message"));
	}

	protected void onRegistered(Context context, String registrationId) {
		final App app = (App) context.getApplicationContext();

		boolean registered;
		String reason;

		try {
			registered = ServerRegistrar.register(
					app.getUsername(),
					app.getPassword(),
					registrationId,
					5);
			
			reason = Config.EXTRA_REASON_AUTH_FAILED;
		} catch (IOException e) {
			if (Log.isLoggable(TAG, Log.DEBUG)) {
				Log.d(TAG, "dmControl: error in server registration", e);
			}

			registered = false;
			reason = Config.EXTRA_REASON_CONNECTION_ERROR;
		}
		
		if (registered) {
			// we are registered on server
			GCMRegistrar.setRegisteredOnServer(context, true);
			
			// send callback intent to main screen
			Intent intent = new Intent(Config.INTENT_SERVER_REGISTRATION_CALLBACK);
			intent.putExtra(Config.EXTRA_REASON, Config.EXTRA_REASON_AUTH_SUCCEED);

			context.sendBroadcast(intent);
		} else {
			// send callback intent to main screen after disconnecting from google service
			Intent intent = new Intent(Config.INTENT_SERVER_REGISTRATION_CALLBACK);
			intent.putExtra(Config.EXTRA_REASON, reason);

			app.setNextIntent(intent);

			// disconnect from Google Service
			GCMRegistrar.unregister(context);
		}
	}

	protected void onUnregistered(Context context, String registrationId) {
		final App app = (App) context.getApplicationContext();
		
		Intent intent = app.getAndSetNextIntent(null);
		
		if (GCMRegistrar.isRegisteredOnServer(context)) {
			// unregister from server
			ServerRegistrar.unregister(registrationId, 5);

			// we are not registered on server yet
			GCMRegistrar.setRegisteredOnServer(context, false);
		}

		if (intent == null) {
			// default intent is main screen with reason user logout
			intent = new Intent(Config.INTENT_SERVER_REGISTRATION_CALLBACK);
			intent.putExtra(Config.EXTRA_REASON, Config.EXTRA_REASON_LOGOUT);
		}
		
		// send broadcast
		context.sendBroadcast(intent);
	}

	public void onError(Context context, String errorId) {
		// return to main screen with reason connection error 
		Intent intent = new Intent(Config.INTENT_SERVER_REGISTRATION_CALLBACK);
		intent.putExtra(Config.EXTRA_REASON, Config.EXTRA_REASON_CONNECTION_ERROR);
		intent.putExtra(Config.EXTRA_ERROR_ID, errorId);
		
		// send broadcast
		context.sendBroadcast(intent);
	}

    private static void notify(Context context, String message) {
        int icon = R.drawable.ic_stat_gcm;
        
        long when = System.currentTimeMillis();
        
        Notification n = new Notification(icon, message, when);
        
        Intent intent = new Intent(context, Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        
        PendingIntent pintent = PendingIntent.getActivity(context, 0, intent, 0);
        
        n.setLatestEventInfo(context, context.getString(R.string.app_name), message, pintent);
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        
        NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mgr.notify(0, n);
    }
}
