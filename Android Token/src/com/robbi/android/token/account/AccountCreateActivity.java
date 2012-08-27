package com.robbi.android.token.account;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.robbi.android.token.App;
import com.robbi.android.token.C;
import com.robbi.android.token.R;
import com.robbi.android.token.client.SignupRequest;
import com.robbi.android.token.client.SignupResponse;
import com.robbi.android.token.client.HttpException;
import com.robbi.android.token.client.ServerHelper;
import com.robbi.android.token.util.ViewValidators;

public class AccountCreateActivity extends Activity implements View.OnClickListener {
    
    private AccountManager am;
	
	private BackgroundTask task;
	
	private Dialog dlg;
	
	private EditText tfirstname;
	
	private EditText tlastname;
	
	private EditText tusername;
	
	private EditText tpassword;
	
	private EditText temail;
    
    private Button bback;
    
    private Button bnext;
	
	@Override
	protected void onCreate(Bundle bnd) {
		super.onCreate(bnd);
		
		am = AccountManager.get(this);
		
		setContentView(R.layout.account_create_activity);
		
		bback = ((Button) findViewById(R.id.btn_back));
		bback.setOnClickListener(this);
		
		bnext = ((Button) findViewById(R.id.btn_next));
		bnext.setOnClickListener(this);
		
		tfirstname = (EditText) findViewById(R.id.firstname);
		tlastname = (EditText) findViewById(R.id.lastname);
		tusername = (EditText) findViewById(R.id.username);
		tpassword = (EditText) findViewById(R.id.password);
		temail = (EditText) findViewById(R.id.email);
		
		ViewValidators.cannotBeEmptyOrWhitespaceOnly(tfirstname);
		ViewValidators.cannotBeEmptyOrWhitespaceOnly(tusername);
		ViewValidators.cannotBeEmpty(tpassword);
		ViewValidators.cannotBeEmptyOrWhitespaceOnly(temail);
		
		BackgroundTask task = (BackgroundTask) getLastNonConfigurationInstance();
		if ((task != null) && !task.finished) {
			task.activity = this;
			
			dlg = ProgressDialog.show(this, "", getString(R.string.login_in_progress), true, false);
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
    
	public void onClick(View v) {
		if (v.getId() == R.id.btn_back) {
			setResult(RESULT_CANCELED);
			finish();
			
			return;
		}
		
		boolean valid = true;
		
		String firstname = tfirstname.getText().toString().trim();
		if (firstname.length() == 0) {
			tfirstname.setError(getString(R.string.field_cannot_be_blank));
			valid = false;
		}
		
		String lastname = tlastname.getText().toString().trim();
		if (lastname.length() == 0) {
			tlastname.setError(getString(R.string.field_cannot_be_blank));
			valid = false;
		}
		
		String username = tusername.getText().toString().trim();
		if (username.length() == 0) {
			tusername.setError(getString(R.string.field_cannot_be_blank));
			valid = false;
		}
		
		String password = tpassword.getText().toString();
		if (password.length() == 0) {
			tpassword.setError(getString(R.string.field_cannot_be_blank));
			valid = false;
		}
		
		String email = temail.getText().toString().trim();
		if (email.length() == 0) {
			temail.setError(getString(R.string.field_cannot_be_blank));
			valid = false;
		}
		
		if (valid) {
			SignupRequest request = new SignupRequest();
			request.firstname = firstname;
			request.lastname = lastname;
			request.username = username;
			request.password = password;
			request.email = email;
			
			bback.setEnabled(false);
			bnext.setEnabled(false);
			
			task = new BackgroundTask(this);
			dlg = ProgressDialog.show(this, "", getString(R.string.create_in_progress), true, false);
			
			task.execute(request);
		}
	}
	
	void accountCreateError(Exception e) {
		if (dlg != null) {
			dlg.dismiss();
			dlg = null;
		}

		bback.setEnabled(true);
		bnext.setEnabled(true);
		
		Toast.makeText(this, R.string.unable_to_connect, Toast.LENGTH_LONG).show();
	}
	
	void accountCreateCompleted(SignupResponse result) {
		if (dlg != null) {
			dlg.dismiss();
			dlg = null;
		}

		bback.setEnabled(true);
		bnext.setEnabled(true);
		
		Account account = null;
        
        Account[] accounts = am.getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE);
        for (Account e : accounts) {
        	if (e.name.equals(result.username)) {
        		account = e;
        		break;
        	}
        }
        
        if (account == null) {
            account = new Account(result.username, AccountAuthenticator.ACCOUNT_TYPE);
            
            if (!am.addAccountExplicitly(account, null, null)) {
            	Toast.makeText(this, R.string.unable_to_add_account, Toast.LENGTH_LONG).show();
            	return;
            }
        }
        
        am.setAuthToken(account, AccountAuthenticator.AUTHTOKEN_TYPE, result.authToken);
        
        ContentResolver.setIsSyncable(account, C.content_authority, 1);
        ContentResolver.setSyncAutomatically(account, C.content_authority, true);

        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, result.username);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AccountAuthenticator.ACCOUNT_TYPE);
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, result.authToken);
        
        setResult(RESULT_OK, intent);
        finish();
	}
	
	private static class BackgroundTask extends AsyncTask<SignupRequest, Void, Object> {
		
		boolean finished;
		
		AccountCreateActivity activity;
		
		BackgroundTask(AccountCreateActivity activity) {
			this.activity = activity;
		}

		protected Object doInBackground(SignupRequest... params) {
			final App app = (App) activity.getApplicationContext();
			
			SignupResponse result = null;
			
			try {
				result = ServerHelper.signup(params[0], app.getDeviceId());
			} catch (HttpException e) {
				return e;
			} catch (IOException e) {
				return e;
			}
			
			return result;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			
			if (result instanceof SignupResponse) {
				activity.accountCreateCompleted((SignupResponse) result);
			} else if (result instanceof Exception) {
				activity.accountCreateError((Exception) result);
			}
			
			finished = true;
		}
	}
}
