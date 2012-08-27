package rk.gcm.demo;

import android.app.Application;
import android.content.Intent;

public class App extends Application {

	private String username;

	private String password;

	private Intent nextIntent;

	public App() {
		nextIntent = null;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Intent getAndSetNextIntent(Intent newValue) {
		Intent oldValue = nextIntent;
		nextIntent = newValue;
		
		return oldValue;
	}

	public void setNextIntent(Intent nextIntent) {
		this.nextIntent = nextIntent;
	}

}
