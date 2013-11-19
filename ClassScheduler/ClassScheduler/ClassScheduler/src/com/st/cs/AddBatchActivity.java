package com.st.cs;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class AddBatchActivity extends Activity {
	private static final int DATE_DIALOG = 1;
	private static final int TIME_DIALOG = 2;
	private int day, month, year, hours, mins;
	private TextView textStartDate, textStartTime;
	private EditText editBatchcode,editCourse,editPeriod,editClasses,editClassesPerWeek, editRemarks;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addbatch);

		textStartDate = (TextView) this.findViewById(R.id.textStartDate);
		textStartTime = (TextView) this.findViewById(R.id.textStartTime);
		
		editBatchcode = (EditText) this.findViewById(R.id.editBatchCode) ;
		editCourse = (EditText) this.findViewById(R.id.editCourse) ;
		editPeriod = (EditText) this.findViewById(R.id.editPeriod) ;
		editClasses = (EditText) this.findViewById(R.id.editClasses) ;
		editClassesPerWeek = (EditText) this.findViewById(R.id.editClassesPerWeek) ;
		editRemarks = (EditText) this.findViewById(R.id.editRemarks) ;
		
		setDateToSysdate();
		updateDateDisplay();

	}

	private void setDateToSysdate() {
		Calendar c = Calendar.getInstance();
		day = c.get(Calendar.DAY_OF_MONTH);
		month = c.get(Calendar.MONTH);
		year = c.get(Calendar.YEAR);
	}

	public void addBatch(View v) {
		 boolean done = Database.addBatch(this,
				  editBatchcode.getText().toString(),
				  editCourse.getText().toString(),
				  textStartDate.getText().toString(),
				  textStartTime.getText().toString(),
				  editClasses.getText().toString(),
				  editPeriod.getText().toString(),
				  editClassesPerWeek.getText().toString(),
				  editRemarks.getText().toString());
		 
		 if ( done )
			 Toast.makeText(this,"Added batch successfully!", Toast.LENGTH_LONG).show();
		 else
			 Toast.makeText(this,"Sorry! Could not add batch!", Toast.LENGTH_LONG).show();
	}
	
	
	public void showDatePicker(View v) {
		showDialog(DATE_DIALOG);
	}
	
	public void showTimePicker(View v) {
		showDialog(TIME_DIALOG);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);

		switch (id) {
		 case DATE_DIALOG:
			return new DatePickerDialog(this, dateSetListener, year, month, day);
		 case TIME_DIALOG:
			return new TimePickerDialog(this, timeSetListener, hours,mins, false);			
		}
		return null;
	}

	private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int pYear, int pMonth, int pDay) {
			year = pYear;
			month = pMonth;
			day = pDay;
			updateDateDisplay();
		}
	};
	
	
	private TimePickerDialog.OnTimeSetListener timeSetListener = 
			   new TimePickerDialog.OnTimeSetListener() {

				@Override
				public void onTimeSet(TimePicker arg0, int pHours, int  pMins) {
					  hours = pHours;
					  mins = pMins;
					  updateTimeDisplay();
				}
	 
	};
	
	

	private void updateDateDisplay() {
		// Month is 0 based so add 1
		textStartDate.setText(String.format("%04d-%02d-%02d", year, month + 1,day));
	}
	
	private void updateTimeDisplay() {
		// Month is 0 based so add 1
		textStartTime.setText(String.format("%02d:%02d", hours,mins));
	}

}
