package com.firstandroidapp;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.FacebookDialog.PendingCall;
import com.facebook.widget.LoginButton;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;

public class MainFragment extends Fragment {

	private static final String TAG = "MainFragment";
	private static final List<String> PERMISSIONS = Arrays.asList("user_activities", "user_events", "user_location", "user_status", "publish_actions", "manage_pages");
	private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
	private boolean pendingPublishReauthorization = false;
	
	private Button shareButton;
	
	private UiLifecycleHelper uiHelper;
    private Session.StatusCallback statusCallback = new SessionStatusCallback();
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
		
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		if (state.isOpened()) {
			Log.i(TAG, "Logged in...");
			shareButton.setVisibility(View.VISIBLE);
			if (pendingPublishReauthorization
		     && state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
				pendingPublishReauthorization = false;
				publishStory();
			}
		} else if (state.isClosed()) {
			Log.i(TAG, "Logged out...");
			shareButton.setVisibility(View.INVISIBLE);
		}
	}
	
	private void publishStory() {
		Session session = Session.getActiveSession();
		
		if (session != null) {
			
			// Check for publish permissions
			List<String> permissions = session.getPermissions();
			if (!isSubsetOf(PERMISSIONS, permissions)) {
				pendingPublishReauthorization = true;
				Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, PERMISSIONS);
				session.requestNewPublishPermissions(newPermissionsRequest);
				return;
			}
		}
		
		Bundle postParams = new Bundle();
		postParams.putString("name", "Facebook SDK for Android");
		postParams.putString("caption", "Build great social apps and get more installs");
		postParams.putString("description", "The Facebook SDK for Android makes it easier and faster to develop Facebook integrated Android apps.");
		postParams.putString("link", "https://developers.facebook.com/android");
		postParams.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");
		
		Request.Callback callback = new Request.Callback() {

			@Override
			public void onCompleted(Response response) {
				JSONObject graphResponse = response.getGraphObject().getInnerJSONObject();
				String postId = null;
				try {
					postId = graphResponse.getString("id");
				} catch (JSONException e) {
					Log.i(TAG, "JSON error "+ e.getMessage());
				}
				FacebookRequestError error = response.getError();
				if (error != null) {
					Toast.makeText(getActivity().getApplicationContext(), error.getErrorMessage(), Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getActivity().getApplicationContext(), postId,  Toast.LENGTH_LONG).show();
				}
			}
		};
		
		Request request = new Request(session, "me/feed", postParams, HttpMethod.POST, callback);
		
		RequestAsyncTask task = new RequestAsyncTask(request);
		task.execute();
	}
	
	private void publishFeedDialog() {
		Bundle params = new Bundle();
		params.putString("name", "Facebook SDK for Android");
		params.putString("caption", "Build great social apps and get more installs.");
		params.putString("description", "The Facebook SDK for Android makes it easier and faster to develop Facebook sntegrated Android apps.");
		params.putString("link", "https://developers.facebook.com/android");
		params.putString("picture",  "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");
		
		WebDialog feedDialog = (
				new WebDialog.FeedDialogBuilder(getActivity(), Session.getActiveSession(), params)
				.setOnCompleteListener(new OnCompleteListener() {
					
					@Override
					public void onComplete(Bundle values, FacebookException error) {
						if (error == null) {
							// When the story is posted, echo the success
							// and the post Id.
							final String postId = values.getString("post_id");
							if (postId != null) {
								Toast.makeText(getActivity(), "Post story, id: "+postId, Toast.LENGTH_SHORT).show();
							} else {
								// User clicked the Cancel button
								Toast.makeText(getActivity().getApplicationContext(), "Publish cancelled", Toast.LENGTH_SHORT).show();								
							}
						} else if (error instanceof FacebookOperationCanceledException) {
							// User clicked the "x" button
							Toast.makeText(getActivity().getApplicationContext(), "Publish cancelled", Toast.LENGTH_SHORT).show();
						} else {
							// Generic, ex: network error
							Toast.makeText(getActivity().getApplicationContext(), "Error posting story", Toast.LENGTH_SHORT).show();
						}
						
					}
				})
				.build());
		feedDialog.show();
	}
	
	private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
		for (String string : subset) {
			if (!superset.contains(string)) {
				return false;
			}
		}
		return true;
	}
	
	private void onClickLogin() {
		Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this)
			.setPermissions(Arrays.asList("public_profile"))
			.setCallback(statusCallback));
		} else {
			Session.openActiveSession(getActivity(), this,  true, statusCallback);
		}
	}
	
	private class SessionStatusCallback implements Session.StatusCallback {
		
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			// Respond to session state changes, ex: updateing the view
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_main, container, false);
		
		LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
		authButton.setFragment(this);
		authButton.setReadPermissions(Arrays.asList("public_profile"));
		
		shareButton = (Button) view.findViewById(R.id.shareButton);
		shareButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//publishStory();
				
				if (FacebookDialog.canPresentShareDialog(getActivity().getApplicationContext(), FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
					// publish the post using the Share Dialog
					FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(getActivity())
					.setLink("https://developers.facebook.com/android")
					.build();
					uiHelper.trackPendingDialogCall(shareDialog.present());
				} else {
					publishFeedDialog();	
				}				
			}
		});
		
		if (savedInstanceState != null) {
			pendingPublishReauthorization = savedInstanceState.getBoolean(PENDING_PUBLISH_KEY, false);
		}
		
		return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(getActivity(), callback);
		uiHelper.onCreate(savedInstanceState);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// For scenarios where the main activitiy is lauched and user
		// session is not null, the sessionn state change notification
		// may not be triggered, Trigger it if it's open/closed.
		Session session = Session.getActiveSession();
		if (session != null 
	     && (session.isOpened() || session.isClosed())) {
			onSessionStateChange(session, session.getState(), null);
		}
		
		uiHelper.onResume();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
			
			@Override
			public void onError(PendingCall pendingCall, Exception error, Bundle data) {
				Log.e(TAG, String.format("Error %s", error.toString()));
			}
			
			@Override
			public void onComplete(PendingCall pendingCall, Bundle data) {
				Log.i(TAG, "Success!");
			}
		});
	}
	
	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(PENDING_PUBLISH_KEY, pendingPublishReauthorization);
		uiHelper.onSaveInstanceState(outState);
	}
}
