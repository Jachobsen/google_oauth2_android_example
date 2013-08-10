package com.example.oauth;

import java.io.IOException;

import com.example.oauth.fragment.SelectAccountDialogFragment;
import com.example.oauth.fragment.SelectAccountDialogFragment.SelectAccountDialogProtocol;
import com.loopj.android.http.*;

import android.os.Bundle;
import android.accounts.*;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class GoogleLoginActivity extends Activity implements SelectAccountDialogProtocol {
	private AccountManager mAccountManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_google_login);

		findViewById(R.id.google_login_button).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						selectAccount();
					}
				});
	}

	private void selectAccount() {
		mAccountManager = AccountManager.get(GoogleLoginActivity.this);
		Account[] accounts = mAccountManager.getAccountsByType("com.google");
		
		if (accounts.length == 0) {
			Toast.makeText(getApplicationContext(), R.string.no_account_found_message, Toast.LENGTH_SHORT).show();
		} else if (accounts.length == 1) {
			getTokenForAccount(accounts[0]);
		} else {
			String[] accountNames = new String[accounts.length];
			
			for (int i = 0; i < accounts.length; i++) {
				accountNames[i] = accounts[i].name;
			}
			
			DialogFragment newFragment = SelectAccountDialogFragment.newInstance(R.string.select_account_dialog_title, accountNames);
			newFragment.show(getFragmentManager(), "dialog");
		}
	}

	public void gotAccount(int index) {
		Account[] accounts = mAccountManager.getAccountsByType("com.google");
		getTokenForAccount(accounts[index]);
	}
	
	@Override
	public void onBackPressed() {
		// Exit gracefully by starting OS Home Activity
		Intent setIntent = new Intent(Intent.ACTION_MAIN);
		setIntent.addCategory(Intent.CATEGORY_HOME);
		setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(setIntent);
	}
	
	private void getTokenForAccount(Account account) {
		Bundle options = new Bundle();

		mAccountManager.getAuthToken(
				account, 
				"oauth2:https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email", 
				options, 
				this, 
				new OnTokenAcquired(), 
				null
				);
	}
	
	private class OnTokenAcquired implements AccountManagerCallback<Bundle> {
		@Override
		public void run(AccountManagerFuture<Bundle> result) {

			Bundle bundle = null;
			try {
				bundle = result.getResult();
				
				String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
				Intent launch = (Intent) result.getResult().get(AccountManager.KEY_INTENT);
				
				if (launch != null) {
					startActivityForResult(launch, 0);
					return;
				}
				
				requestAPIKeyFromOAuthToken(token);
			} catch (OperationCanceledException e) {
				e.printStackTrace();
			} catch (AuthenticatorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private void requestAPIKeyFromOAuthToken(String token) {
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams("token", token);
		
		client.post("https://myrailsapp.com/api/v1/auth/verify", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				Log.w("OAUTH", "Got API key from server: " + response);
				finish();
			}
			
			@Override
			public void onFailure(Throwable e) {
				Log.w("OAUTH", "Error getting API key");
				Toast.makeText(getApplicationContext(), R.string.connection_error_message, Toast.LENGTH_LONG).show();
				
				e.printStackTrace();
			}
		});
	}
}
