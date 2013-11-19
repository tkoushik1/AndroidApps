package com.st.cs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class ListBatchesActivity  extends Activity { 

	ListView listBatches;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listbatches);
	}
	
	@Override 
	public void onStart() {
		super.onStart();
		listBatches  = (ListView) this.findViewById(R.id.listBatches);
		BatchesAdapter adapter  = new BatchesAdapter(this);
		listBatches.setAdapter(adapter);
	}
	
	public void addBatch(View v) {
		Intent intent = new Intent(this, AddBatchActivity.class);
		startActivity(intent);
	}
	
	
}
