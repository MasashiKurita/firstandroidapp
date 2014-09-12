package com.firstandroidapp;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;

public class PostListActivity extends ListActivity {
	private static final String TAG = "PostListActivity";

	private static final String GRAPH_PATH_PAGE_FEED = "1449310858633062/feed";
	private static final String GRAPH_PATH_PAGE_EVENT = "?ids=293823777472129,944207338929355,629132557185394,1407160042867804";
	
	private class MyRequestCallback implements Request.Callback {

		@Override
		public void onCompleted(Response response) {
			Log.d(TAG, "start " + response.getRequest().getGraphPath());
			Log.d(TAG, "raw response=(" + response.getRawResponse() + ")");

			List<String> messageList = new ArrayList<String>();
	        JSONObject graphResponse = response.getGraphObject().getInnerJSONObject();
	        
			try {
				
				if (response.getRequest().getGraphPath().equals(GRAPH_PATH_PAGE_FEED)) {
					List<String> eventIdList = getFeedList(graphResponse);
					StringBuilder sb = new StringBuilder();
					for (int i=0; i<eventIdList.size(); i++) {
						String id = eventIdList.get(i);
						sb.append(id);
						if (i < eventIdList.size()-1) {
							sb.append(",");
						}
					}
				} else {
					messageList.addAll(getVenueList(graphResponse));
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, messageList);
					setListAdapter(adapter);
				}
				
			} catch (JSONException e) {
				Log.i(TAG, "JSON error "+ e.getMessage());
			}
			FacebookRequestError error = response.getError();
			if (error != null) {
				Toast.makeText(getApplicationContext(), error.getErrorMessage(), Toast.LENGTH_SHORT).show();
			} else {
			}
		}

		private List<String> getFeedList(JSONObject graphResponse) throws JSONException {
			List<String> result = new ArrayList<String>();

			JSONArray feeds = graphResponse.getJSONArray("data");
			Log.d(TAG, "start read " + feeds.length() + " Feeds");
			for (int i=0; i<feeds.length(); i++) {
				JSONObject feed = feeds.getJSONObject(i);
				if (feed.has("link")) {
					String eventId = feed.getString("id").split("_")[1];
					result.add(eventId);
				}
				
			}
		
			return result;
		}

	    private List<String> getVenueList(JSONObject graphResponse) throws JSONException {
			List<String> result = new ArrayList<String>();

			JSONArray events = graphResponse.names();
			for (int i=0; i<events.length(); i++) {
				JSONObject event = graphResponse.getJSONObject(events.getString(i));
				JSONObject venue = event.getJSONObject("venue");
				String point = "location = " + event.getString("location") + "\n"
						     + "venue = (\n"
						     + "latitude = " + venue.getString("latitude") + "\n" 
						     + "longitude = "+ venue.getString("longitude") + "\n"
						     + ")";
				result.add(point);
			}
		
            return result;
	    }
	    
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_postslist);
		
		
		Session session = Session.getActiveSession();
		if (session != null) {
			
			Request.Callback callback = new MyRequestCallback();
			
//			Request request = new Request(session, GRAPH_PATH_PAGE_FEED, null, HttpMethod.GET, callback);
//			Log.d(TAG, "start get events");
//			RequestAsyncTask task = new RequestAsyncTask(request);
//			task.execute();


			Bundle param = new Bundle();
			param.putString("ids", "293823777472129,944207338929355,629132557185394,1407160042867804");
			Request request = new Request(session, "", param, HttpMethod.GET, callback);
			Log.d(TAG, "start get venues");
			
			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();

		}
		
	}
		
}
