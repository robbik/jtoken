package com.robbi.android.token.account;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.robbi.android.token.C;
import com.robbi.android.token.R;
import com.robbi.android.token.util.Crypto;
import com.robbi.android.token.util.Database;
import com.robbi.android.token.util.ViewValidators;

public class PINPromptActivity extends Activity implements View.OnClickListener {
	
	private String compatibility;
    
    private EditText tpin;
    
    private Button bnext;
    
	private BackgroundTask task;
	
	private Dialog dlg;

	@Override
	protected void onCreate(Bundle bnd) {
		super.onCreate(bnd);
		
		setContentView(R.layout.pin_prompt_activity);
		
		bnext = ((Button) findViewById(R.id.btn_next));
		bnext.setOnClickListener(this);
		
		tpin = (EditText) findViewById(R.id.password);
		
		ViewValidators.cannotBeEmpty(tpin);
		
		if (bnd.containsKey(C.intent.EXTRA_COMPATIBILITY)) {
			compatibility = bnd.getString(C.intent.EXTRA_COMPATIBILITY);
		} else {
			final Intent intent = getIntent();
			
			if (intent.hasExtra(C.intent.EXTRA_COMPATIBILITY)) {
				compatibility = intent.getStringExtra(C.intent.EXTRA_COMPATIBILITY);
			} else {
				compatibility = C.intent.COMPATIBILITY_NONE;
			}
		}
		
		BackgroundTask task = (BackgroundTask) getLastNonConfigurationInstance();
		if ((task != null) && !task.finished) {
			task.activity = this;
			
			dlg = ProgressDialog.show(this, "", getString(R.string.unlocking_seed), true, false);
		} else {
			dlg = null;
		}
		
		this.task = task;
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
    
	@Override
	protected void onSaveInstanceState(Bundle bnd) {
		bnd.putString(C.intent.EXTRA_COMPATIBILITY, compatibility);
	}

	public void onClick(View v) {
		if (v.getId() == R.id.btn_back) {
			setResult(RESULT_CANCELED);
			finish();

			return;
		}
		
		String pin = tpin.getText().toString();
		
		boolean valid = true;
		
		if (pin.length() == 0) {
			tpin.setError(getString(R.string.field_cannot_be_blank));
			valid = false;
		}
		
		if (valid) {
			bnext.setEnabled(false);
			
			task = new BackgroundTask(this);
			dlg = ProgressDialog.show(this, "", getString(R.string.unlocking_seed), true, false);
			
			task.execute(pin, compatibility);
		}
	}
	
	void error(Exception e) {
		if (dlg != null) {
			dlg.dismiss();
			dlg = null;
		}
		
		task = null;
		
		bnext.setEnabled(true);

		Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
	}
	
	void success(byte[] seedb) {
		if (dlg != null) {
			dlg.dismiss();
			dlg = null;
		}
		
		bnext.setEnabled(true);

		Intent resultIntent = new Intent();
		resultIntent.putExtra(C.intent.EXTRA_SEED, seedb);
        
        setResult(RESULT_OK, resultIntent);
        
        finish();
	}
	
	private static class BackgroundTask extends AsyncTask<String, Void, Object> {
		
		boolean finished;
		
		PINPromptActivity activity;
		
		BackgroundTask(PINPromptActivity activity) {
			this.activity = activity;
		}

		protected Object doInBackground(String... params) {
			Object result;
			
			final SharedPreferences prefs = activity.getSharedPreferences(C.PACKAGE_NAME, MODE_PRIVATE);
			
			if (!Crypto.sha256b64(params[0]).equals(prefs.getString(C.shared_preferences.PIN, ""))) {
				return new Exception(activity.getString(R.string.invalid_pin));
			}
			
			Bundle bnd = db.findCompatibility(params[1]);
			
			if (bnd == null) {
				db.close();

				result = new Exception(activity.getString(R.string.incompatible, params[1]));
			} else {
				String seed = db.findSeed();
				
				db.close();
				
				if (seed == null) {
					result = new Exception(activity.getString(R.string.sync_required));
				} else {
					byte[] seedb = Base64.decode(seed, Base64.DEFAULT);
					seedb = Crypto.decrypt(seedb, params[0]);
					
					result = seedb;
				}
			}
			
			return result;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			
			if (result instanceof byte[]) {
				activity.success((byte[]) result);
			} else if (result instanceof Exception) {
				activity.error((Exception) result);
			}
			
			finished = true;
		}
	}
}
