package com.st.cs;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;


public class ListClassesActivity extends Activity {

	    String batchcode;
	    TableLayout tableClasses;
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.listclasses);
			
			// get batchcode using intent
			batchcode = getIntent().getStringExtra("batchcode");
			tableClasses = (TableLayout) this.findViewById( R.id.tableClasses);
		}
		
		@Override
		public void onStart() {
			super.onStart();
			deleteRowsFromTable();
			addRowsToTable(tableClasses,batchcode);
		}
		
		public void deleteRowsFromTable() {
			if ( tableClasses.getChildCount() > 2) 
			    tableClasses.removeViews(2,tableClasses.getChildCount() - 2);
	    }
		
		private void addRowsToTable(TableLayout table, String batchcode) {
			
			 List<Class> classes = Database.getClasses(this, batchcode);
			 
			 TableRow tr = new TableRow(this);
             tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
             
             int classno = 1;
             for(final Class c : classes) {
            	    TableRow row = (TableRow) LayoutInflater.from(this).inflate(R.layout.classrow, null);
            	    
            	    ((TextView)row.findViewById(R.id.textNo)).setText( String.valueOf(classno));
            	    ((TextView)row.findViewById(R.id.textDate)).setText(c.getClassDate());
            	    ((TextView)row.findViewById(R.id.textTime)).setText(c.getClassTime());
            	    
            	    // handle update button 
            	    ImageButton btnUpdate = (ImageButton) row.findViewById(R.id.btnUpdate);
            	    btnUpdate.setOnClickListener( new OnClickListener() {
						@Override
						public void onClick(View v) {
                              Intent intent = new Intent( ListClassesActivity.this,UpdateClassActivity.class);
                              intent.putExtra("classid", c.getClassId()); 
                              startActivity(intent);
						}
					}); 
            	    
            	    table.addView(row);
            	    
            	    TableRow line = new TableRow(this);
            	    TextView tv = new TextView(this);
            	    tv.setBackgroundColor(Color.RED);
            	    TableRow.LayoutParams lp = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT,3);
            	    lp.span = 4;
            	    tv.setLayoutParams(lp);
            	    
            	    line.addView(tv);
            	    
            	    table.addView(line);
           	    
            	    classno ++;
             }
			
		}
		
		
}
