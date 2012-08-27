package com.robbi.android.token.totp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.util.Base64;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.robbi.android.token.App;
import com.robbi.android.token.C;
import com.robbi.android.token.R;
import com.robbi.android.token.crypto.AES;
import com.robbi.android.token.util.Database;

public class A1Activity extends FragmentActivity implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
	
	private App app;
	
	private Dialog dlg;
	
	private PendingIntent extraApp;
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		extraApp = (PendingIntent) getIntent().getParcelableExtra(C.intent.EXTRA_APPLICATION);
		
		app = (App) getApplicationContext();
		
		final TextView msgView = new TextView(this);
		msgView.setText(new SpannableString(getString(R.string.a1_message)));
		msgView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		msgView.setPadding(10, 10, 10, 10);
		msgView.setGravity(Gravity.CENTER_HORIZONTAL);
		
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		
		b.setIcon(R.drawable.icon);
		b.setTitle(R.string.application_name);
		
		b.setView(msgView);
		
		b.setPositiveButton(android.R.string.yes, this);
		b.setNegativeButton(android.R.string.no, this);

		b.setCancelable(false);
		b.setOnCancelListener(this);
		
		dlg = b.create();
		dlg.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (dlg != null) {
			dlg.dismiss();
			dlg = null;
		}
	}

	public void onClick(DialogInterface dialog, int which) {
		String token;
		
		int resultCode;
		
		if (which == DialogInterface.BUTTON_POSITIVE) {
			resultCode = Activity.RESULT_OK;
			token = calcToken();
		} else {
			resultCode = Activity.RESULT_CANCELED;
			token = null;
		}
		
		final Intent respi = new Intent();
		respi.putExtras(getIntent());
		
		if (token != null) {
			respi.putExtra(EXTRA_TOKEN, token);
		}
		
		try {
			extraApp.send(this, resultCode, respi);
		} catch (Exception e) {
			// do nothing
		}
		
		finish();
	}

	public void onCancel(DialogInterface dialog) {
		final Intent respi = new Intent();
		respi.putExtras(getIntent());
		
		try {
			extraApp.send(this, Activity.RESULT_CANCELED, respi);
		} catch (Exception e) {
			// do nothing
		}

		finish();
	}
	
	private String calcToken() {
		final SharedPreferences prefs = getSharedPreferences(C.PACKAGE_NAME, Context.MODE_PRIVATE);
		
		long offset = prefs.getLong(C.shared_preferences.token_offset, -1L);
		long window = prefs.getLong(C.shared_preferences.TOTP_WINDOW, -1L);
		
		String data;
		
		Database db = new Database(this);
		
		try {
			data = db.find();
		} finally {
			db.close();
		}
		
		byte[] key = Base64.decode(data, Base64.DEFAULT);
		
		AES aes = new AES();
		aes.init(app.getDeviceId().toCharArray(), null, true, false);
		
		key = aes.doFinal(key);
		
		TOTP totp = new TOTP(key, offset, window, false);
		
		return totp.generateAlphaNumeric(System.currentTimeMillis(), 12);
	}
}
