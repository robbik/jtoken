package com.robbi.android.token.sync;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SyncBroadcastReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {
		SyncIntentService.runIntentInService(context, intent);
		
		setResult(Activity.RESULT_OK, null, null);
	}
}
