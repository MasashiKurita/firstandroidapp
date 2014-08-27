package com.firstandroidapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.Session;

public class MainActivity extends FragmentActivity {
		
	private MainFragment mainFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState == null) {
			// Add the fragment on initial activity setup
			mainFragment = new MainFragment();
			getSupportFragmentManager()
			.beginTransaction()
			.add(android.R.id.content, mainFragment)
			.commit();
		} else {
			// Or set the fragment from restored state info
			mainFragment = (MainFragment) getSupportFragmentManager()
					.findFragmentById(android.R.id.content);
		}
		setContentView(R.layout.activity_main);
		
//		// start Facebook Login
//		Log.d(TAG, "start Facebook Login");
//		Session.openActiveSession(this, true, new Session.StatusCallback() {
//			
//			// callback when session changes state
//			@Override
//			public void call(Session session, SessionState State, Exception excption) {
//				if (session.isOpened()) {
//					
//					// make request to the /me API
//					Log.d(TAG, "make request to the /me API");
//					Request.newMeRequest(session, new Request.GraphUserCallback() {
//						
//						// call back after Graph API response with user object
//						@Override
//						public void onCompleted(GraphUser user, Response response) {
//							
//							Log.d(TAG, "onCompleted");
//							if (user != null) {
//								TextView welcome = (TextView) findViewById(R.id.welcome);
//								welcome.setText("Hello " + user.getName() + "!");
//							}
//						}
//					}).executeAsync();
//				}
//			}
//		});
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		if (id == R.id.view_posts) {
			Intent intent = new Intent(this, PostListActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
