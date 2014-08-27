package com.firstandroidapp;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class PostListActivity extends ListActivity {
	private String[] titles = {"Hello", "Good Morning", "Good Afternoon", "Good Evening", "Good Night"};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_postslist);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titles);
		setListAdapter(adapter);
		ListView postlist = getListView();
		postlist.setSelection(3);
	};	

}
