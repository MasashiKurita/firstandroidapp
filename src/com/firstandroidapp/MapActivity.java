package com.firstandroidapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

// Facebook SDK
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
// Google Maps API
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapActivity extends Activity {
	private static final String TAG = "PostListActivity";
		
	private class MyRequestCallback implements Request.Callback {

		@Override
		public void onCompleted(Response response) {
			Log.d(TAG, "start " + response.getRequest().getGraphPath());
			Log.d(TAG, "raw response=(" + response.getRawResponse() + ")");

	        JSONObject graphResponse = response.getGraphObject().getInnerJSONObject();
	        
			try {
				
				setMarkers(graphResponse);
				
			} catch (JSONException e) {
				Log.i(TAG, "JSON error "+ e.getMessage());
			}
			FacebookRequestError error = response.getError();
			if (error != null) {
				Toast.makeText(getApplicationContext(), error.getErrorMessage(), Toast.LENGTH_SHORT).show();
			} else {
			}
		}


	    private void setMarkers(JSONObject graphResponse) throws JSONException {

			JSONArray events = graphResponse.names();

	        // MapFragmentオブジェクトを取得
//	        MapFragment mapFragment = (MapFragment) getFragmentManager()
//	                .findFragmentById(R.id.map);
//			googleMap = mapFragment.getMap();
			
			for (int i=0; i<events.length(); i++) {
				JSONObject event = graphResponse.getJSONObject(events.getString(i));
				JSONObject venue = event.getJSONObject("venue");

				LatLng location = new LatLng(venue.getDouble("latitude"), venue.getDouble("longitude"));
		        MarkerOptions options = new MarkerOptions();
		        options.position(location);
		        options.title(event.getString("location"));
		        options.snippet(location.toString());
		        
		        googleMap.addMarker(options);
			}
		
	    }
	    
	};

	
	
    // GoogleMapオブジェクトの宣言
    private GoogleMap googleMap;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // MapFragmentオブジェクトを取得
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);

        try {
            // GoogleMapオブジェクトの取得
            googleMap = mapFragment.getMap();

            // Activityが初回で生成されたとき
            if (savedInstanceState == null) {

                // MapFragmentのオブジェクトをセット
                mapFragment.setRetainInstance(true);

                // 地図の初期設定を行うメソッドの呼び出し
                mapInit();
            }
        }
        // GoogleMapが使用不可のときのためにtry catchで囲っています。
        catch (Exception e) {
        }
        
		Session session = Session.getActiveSession();
		if (session != null) {
			
			Request.Callback callback = new MyRequestCallback();

			Bundle param = new Bundle();
			param.putString("ids", "293823777472129,944207338929355,629132557185394,1407160042867804");
			Request request = new Request(session, "", param, HttpMethod.GET, callback);
			Log.d(TAG, "start get venues");
			
			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();

		}

    }

    // 地図の初期設定メソッド
    private void mapInit() {

        // 地図タイプ設定
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // 現在位置ボタンの表示を行なう
        googleMap.setMyLocationEnabled(true);

        LatLng location = new LatLng(35.681382, 139.766084);
        
        // 東京駅の位置、ズーム設定
        CameraPosition camerapos = new CameraPosition.Builder()
                .target(location).zoom(15.5f).build();

        // 地図の中心の変更する
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camerapos));
        
        MarkerOptions options = new MarkerOptions();
        options.position(location);
        options.title("Tokyo Station");
        options.snippet(location.toString());
        
        googleMap.addMarker(options);
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
		if (id == R.id.view_map) {
			Intent intent = new Intent(this, MapActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

}
