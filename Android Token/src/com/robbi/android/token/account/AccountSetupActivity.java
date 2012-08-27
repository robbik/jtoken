package com.robbi.android.token.account;

import android.accounts.AccountAuthenticatorActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.robbi.android.token.R;

public class AccountSetupActivity extends AccountAuthenticatorActivity implements View.OnClickListener {
	
	@Override
	protected void onCreate(Bundle bnd) {
		super.onCreate(bnd);
		
		((Button) findViewById(R.id.btn_back)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_create)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_signin)).setOnClickListener(this);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_CANCELED) {
			setResult(RESULT_CANCELED);
		} else {
			setAccountAuthenticatorResult(data.getExtras());
			
			setResult(RESULT_OK);
		}
		
		finish();
	}

	public void onClick(View v) {
		Intent intent;
		
		switch (v.getId()) {
		case R.id.btn_back:
			setResult(RESULT_CANCELED);
			finish();
			break;
			
		case R.id.btn_create:
			intent = new Intent(this, AccountCreateActivity.class);
			
			startActivityForResult(intent, R.id.btn_create);
			break;
			
		case R.id.btn_signin:
			intent = new Intent(this, PINPromptActivity.class);
			
			startActivityForResult(intent, R.id.btn_signin);
			break;
		}
	}
}
