package com.robbi.android.token.totp;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Parcelable;

import com.robbi.android.token.C;

public class A1IntentService extends IntentService {
	
	public A1IntentService() {
		super("com.robbi.android.token.intent.service.A1");
	}
	
	protected void onHandleIntent(Intent intent) {
		if (!intent.hasExtra(C.intent.EXTRA_APPLICATION)) {
			return;
		}
		
		final Parcelable extraApp = intent.getParcelableExtra(C.intent.EXTRA_APPLICATION);
		if (!(extraApp instanceof PendingIntent)) {
			return;
		}
		
		Intent activityIntent = new Intent(this, A1Activity.class);
		activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_FROM_BACKGROUND);
		
		activityIntent.putExtra(C.intent.EXTRA_APPLICATION, extraApp);
		
		startActivity(activityIntent);
	}
}
