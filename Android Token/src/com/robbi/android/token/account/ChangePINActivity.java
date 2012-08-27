package com.robbi.android.token.account;

import java.net.HttpURLConnection;

import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.robbi.android.token.C;
import com.robbi.android.token.R;
import com.robbi.android.token.client.HttpException;
import com.robbi.android.token.util.Crypto;
import com.robbi.android.token.util.Database;
import com.robbi.android.token.util.ViewValidators;

public class ChangePINActivity extends AccountAuthenticatorActivity implements View.OnClickListener {
	
	private BackgroundTask task;
	
	private Dialog dlg;
    
    private EditText tpin;
    
    private EditText tnewpin;

    private EditText tnewpin2;
    
    private Button bback;
    
    private Button bnext;

	@Override
	protected void onCreate(Bundle bnd) {
		super.onCreate(bnd);
		
		setContentView(R.layout.change_password_activity);
		
		bback = ((Button) findViewById(R.id.btn_back));
		bback.setOnClickListener(this);
		
		bnext = ((Button) findViewById(R.id.btn_next));
		bnext.setOnClickListener(this);
		
		tpin = (EditText) findViewById(R.id.password);
		tnewpin = (EditText) findViewById(R.id.new_password);
		tnewpin2 = (EditText) findViewById(R.id.reenter_new_password);
		
		initViewValidators();
		
		BackgroundTask task = (BackgroundTask) getLastNonConfigurationInstance();
		if ((task != null) && !task.finished) {
			task.activity = this;
			
			dlg = ProgressDialog.show(this, "", getString(R.string.changing_pin), true, false);
		} else {
			dlg = null;
		}
		
		this.task = task;
	}
	
	private void initViewValidators() {
		ViewValidators.cannotBeEmpty(tpin);
		
		tnewpin.setImeActionLabel("", EditorInfo.IME_ACTION_NEXT);
		tnewpin2.setImeActionLabel("", EditorInfo.IME_ACTION_NEXT);
		
		tnewpin.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId != EditorInfo.IME_ACTION_NEXT) {
					String newpasswd = v.getText().toString();
					String newpasswd2 = tnewpin2.getText().toString();
					
					if (newpasswd.length() == 0) {
						v.setError(getString(R.string.field_cannot_be_blank));
					}
					
					if (!newpasswd2.equals(newpasswd)) {
						tnewpin2.setError(getString(R.string.pin_not_equals));
					}
				}
				
				return false;
			}
		});
		
		tnewpin2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId != EditorInfo.IME_ACTION_NEXT) {
					String newpasswd = v.getText().toString();
					String newpasswd2 = tnewpin2.getText().toString();
					
					if (!newpasswd2.equals(newpasswd)) {
						tnewpin2.setError(getString(R.string.pin_not_equals));
					}
				}
				
				return false;
			}
		});
	}

    @Override
	public Object onRetainNonConfigurationInstance() {
    	final BackgroundTask task = this.task;
    	
    	if ((task != null) && !task.finished) {
    		return task;
    	} else {
    		return null;
    	}
	}

	public void onClick(View v) {
		if (v.getId() == R.id.btn_back) {
			setResult(RESULT_CANCELED);
			finish();

			return;
		}
		
		String pin = tpin.getText().toString();
		String newpin = tnewpin.getText().toString();
		String newpasswd2 = tnewpin2.getText().toString();
		
		boolean valid = true;
		
		if (pin.length() == 0) {
			tpin.setError(getString(R.string.field_cannot_be_blank));
			valid = false;
		}
		
		if (newpin.length() == 0) {
			tnewpin.setError(getString(R.string.field_cannot_be_blank));
			valid = false;
		}
		
		if (!newpin.equals(newpasswd2)) {
			tnewpin2.setError(getString(R.string.pin_not_equals));
			valid = false;
		}

		if (valid) {
			bback.setEnabled(false);
			bnext.setEnabled(false);
			
			task = new BackgroundTask(this);
			dlg = ProgressDialog.show(this, "", getString(R.string.changing_pin), true, false);
			
			task.execute(pin, newpin);
		}
	}
	
	void changePasswordError(Exception e) {
		if (dlg != null) {
			dlg.dismiss();
			dlg = null;
		}

		bback.setEnabled(true);
		bnext.setEnabled(true);
		
		if (e instanceof HttpException) {
			if (((HttpException) e).getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
				Toast.makeText(this, R.string.invalid_username_or_password, Toast.LENGTH_LONG).show();
				
				return;
			}
		}
		
		Toast.makeText(this, R.string.unable_to_connect, Toast.LENGTH_LONG).show();
	}
	
	void changePasswordCompleted() {
		if (dlg != null) {
			dlg.dismiss();
			dlg = null;
		}

		bback.setEnabled(true);
		bnext.setEnabled(true);
		
        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, username);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AccountAuthenticator.ACCOUNT_TYPE);
        
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        
        finish();
	}
	
	private static class BackgroundTask extends AsyncTask<String, Void, Object> {
		
		boolean finished;
		
		ChangePINActivity activity;
		
		BackgroundTask(ChangePINActivity activity) {
			this.activity = activity;
		}
		
		protected Object doInBackground(String... params) {
			final SharedPreferences prefs = activity.getSharedPreferences(C.PACKAGE_NAME, MODE_PRIVATE);
			
			if (!Crypto.sha256b64(params[0]).equals(prefs.getString(C.shared_preferences.PIN, ""))) {
				return new Exception(activity.getString(R.string.invalid_pin));
			}
			
			Database db = new Database(activity);
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			
			if (result == null) {
				activity.changePasswordCompleted();
			} else if (result instanceof Exception) {
				activity.changePasswordError((Exception) result);
			}
		}
	}
}
