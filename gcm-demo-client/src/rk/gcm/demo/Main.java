package rk.gcm.demo;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

public class Main extends Activity implements View.OnClickListener {
	
	private TextView txtMessage;
	
	private Button btnSignIn;
	
	private ProgressDialog signingDialog;
	
	private final BroadcastReceiver serverregCallback = new BroadcastReceiver() {
		
		public void onReceive(Context context, Intent intent) {
			final String reason = intent.getStringExtra(Config.EXTRA_REASON);

			// dismiss progress dialog
			if (signingDialog != null) {
				signingDialog.dismiss();
				signingDialog = null;
			}
			
			// follow up
			if (Config.EXTRA_REASON_AUTH_SUCCEED.equals(reason)) {
        		startActivity(new Intent(context, Home.class));
			} else if (Config.EXTRA_REASON_AUTH_FAILED.equals(reason)) {
				txtMessage.setText(getString(R.string.auth_failed));
				txtMessage.setVisibility(View.VISIBLE);
			} else if (Config.EXTRA_REASON_LOGOUT.equals(reason)) {
				txtMessage.setText("");
				txtMessage.setVisibility(View.GONE);
			} else if (Config.EXTRA_REASON_CONNECTION_ERROR.equals(reason)) {
				String message = getString(R.string.connection_error, intent.getStringExtra(Config.EXTRA_ERROR_ID));
				
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
				
				txtMessage.setText("");
				txtMessage.setVisibility(View.GONE);
			}
		}
	};

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make sure the device has the proper dependencies.
        GCMRegistrar.checkDevice(this);
        
        // Make sure the device has the proper manifest.
        GCMRegistrar.checkManifest(this);
        
        setContentView(R.layout.main);
        
        txtMessage = (TextView) findViewById(R.id.main_message);
        
        btnSignIn = (Button) findViewById(R.id.main_signin);
        btnSignIn.setOnClickListener(this);
        
        registerReceiver(serverregCallback, new IntentFilter(Config.INTENT_SERVER_REGISTRATION_CALLBACK));
        
        final String regId = GCMRegistrar.getRegistrationId(this);
        if (!"".equals(regId)) {
        	if (GCMRegistrar.isRegisteredOnServer(this)) {
        		startActivity(new Intent(this, Home.class));
        	}
        }
    }

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.main_signin:
			signingDialog = ProgressDialog.show(this, "", getString(R.string.signing_in), true, false);
			
			GCMRegistrar.register(this, Config.GCM_SENDER_ID);
			
			break;
		}
	}
}
