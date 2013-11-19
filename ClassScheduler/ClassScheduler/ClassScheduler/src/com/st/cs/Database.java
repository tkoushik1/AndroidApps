package com.st.cs;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Database {
	public static final String BATCHES_TABLE_NAME = "batches";
	public static final String BATCHES_ID = "_id";
	public static final String BATCHES_BATCHCODE = "batchcode";
	public static final String BATCHES_COURSE = "course";
	public static final String BATCHES_STARTDATE = "startdate";
	public static final String BATCHES_STARTTIME = "starttime";
	public static final String BATCHES_CLASSES = "classes";
	public static final String BATCHES_PERIOD = "period";
	public static final String BATCHES_CLASSESPERWEEK = "classesperweek";
	public static final String BATCHES_REMARKS = "remarks";

	public static final String CLASSES_TABLE_NAME = "classes";
	public static final String CLASSES_CLASSES_ID = "_id";

	public static final String CLASSES_BATCHCODE = "batchcode";
	public static final String CLASSES_CLASSDATE = "classdate";
	public static final String CLASSES_CLASSTIME = "classtime";
	public static final String CLASSES_CLASSPERIOD = "period";
	public static final String CLASSES_TOPICS = "topics";
	public static final String CLASSES_REMARKS = "remarks";

	public static Batch cursorToBatch(Cursor batches) {
		
		Batch batch = new Batch();
		batch.setCode(batches.getString(batches
				.getColumnIndex(Database.BATCHES_BATCHCODE)));
		batch.setCourse(batches.getString(batches
				.getColumnIndex(Database.BATCHES_COURSE)));
		batch.setStartdate(batches.getString(batches
				.getColumnIndex(Database.BATCHES_STARTDATE)));
		batch.setStarttime(batches.getString(batches
				.getColumnIndex(Database.BATCHES_STARTTIME)));
		batch.setClasses(batches.getString(batches
				.getColumnIndex(Database.BATCHES_CLASSES)));
		batch.setClassesperweek(batches.getString(batches
				.getColumnIndex(Database.BATCHES_CLASSESPERWEEK)));
		batch.setPeriod(batches.getString(batches
				.getColumnIndex(Database.BATCHES_PERIOD)));
		batch.setRemarks(batches.getString(batches
				.getColumnIndex(Database.BATCHES_REMARKS)));
		return batch;
	}
	
    public static Class cursorToClass(Cursor classes) {
		Class cls = new Class();
		cls.setClassId( classes.getString( classes.getColumnIndex(Database.CLASSES_CLASSES_ID)));
		cls.setBatchCode( classes.getString( classes.getColumnIndex(Database.CLASSES_BATCHCODE)));
		cls.setClassDate( classes.getString( classes.getColumnIndex(Database.CLASSES_CLASSDATE)));
		cls.setClassTime( classes.getString( classes.getColumnIndex(Database.CLASSES_CLASSTIME)));
		cls.setPeriod( classes.getString( classes.getColumnIndex(Database.CLASSES_CLASSPERIOD)));
		cls.setTopics( classes.getString( classes.getColumnIndex(Database.CLASSES_TOPICS)));
		cls.setRemarks( classes.getString( classes.getColumnIndex(Database.CLASSES_REMARKS)));
		return cls;
	}


	public static boolean addClass(Context context, String batchCode, String classDate, String classTime, String period, String topics, String remarks, boolean adjust) {
		DBHelper dbhelper = null;
		SQLiteDatabase db = null;
		try {
			dbhelper = new DBHelper(context);
			db = dbhelper.getWritableDatabase();
			db.beginTransaction();
		
            if ( adjust ) {
        	  // delete last class for the batch
        	  boolean deleted =  deleteLastClass(db,batchCode);
        	  if ( !deleted) {
        		  db.endTransaction();
        		  return false;
        	  }
            }
        
            // add new class with the given details
            
            ContentValues values = new ContentValues();
			values.put(Database.CLASSES_BATCHCODE, batchCode);
			values.put(Database.CLASSES_CLASSDATE, classDate);
			values.put(Database.CLASSES_CLASSTIME, classTime);
			values.put(Database.CLASSES_CLASSPERIOD, period);
			values.put(Database.CLASSES_REMARKS,remarks);
			values.put(Database.CLASSES_TOPICS,topics);
			
			long rowid = db.insert(Database.CLASSES_TABLE_NAME, null,values);
			
  		    if ( rowid >=0 ) {
			     db.setTransactionSuccessful();
			     db.endTransaction();
			     return true;
  		    }
  		    else {
  		    	db.endTransaction();
  		    	return false; 
  		    }
		}
		catch(Exception ex) {
			Log.d("CS", "Error in addClass -->" + ex.getMessage());
			return false;
		}
		finally {
			if (db != null && db.isOpen()) {
				db.close();
			}
			
		}
	}
	
	public static boolean deleteLastClass(SQLiteDatabase db, String batchcode) {
		try {
			// find out classid for last class and then delete it 
			boolean done = false;
			Cursor lastClass = db.query(Database.CLASSES_TABLE_NAME, null,Database.CLASSES_BATCHCODE + " = ?",
					new String[] { batchcode},
					null, null, Database.CLASSES_CLASSDATE + " desc " , "1");
			
			String classid = null;
			
			if ( lastClass.moveToFirst() ) {
			    classid = lastClass.getString(  lastClass.getColumnIndex( Database.CLASSES_CLASSES_ID));	
    		    int rows = db.delete(Database.CLASSES_TABLE_NAME,Database.CLASSES_CLASSES_ID + " = ?",
				     new String[] { classid} );
		   	    done = rows == 1;
			}
			lastClass.close();
			return done;
		} 
		catch (Exception ex) {
			Log.d("Account", "Error in deleteLastClass-->" + ex.getMessage());
			return false;
		}
	} // deleteLastClass
	
	
	public static String calendarToString(Calendar c) {
		return String.format("%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
	}
	
	public static boolean cancelClass(Context context, String batchCode,String classid) {
		
		DBHelper dbhelper = null;
		SQLiteDatabase db = null;
		try {
			dbhelper = new DBHelper(context);
			db = dbhelper.getWritableDatabase();
			db.beginTransaction();
			
     		int rows = db.delete(Database.CLASSES_TABLE_NAME,Database.CLASSES_CLASSES_ID + " = ?",
				     new String[] { classid} );
     		if ( rows == 1)
     		{
     			// add a class after the last class
     			if ( addAfterLastClass(db,batchCode)) 
     			{
     				db.setTransactionSuccessful();
     				db.endTransaction();
     				return true;
     			}
     		}
     		
			db.endTransaction();
  			return false; 
		   	     
			}
		catch (Exception ex) {
			Log.d("CS", "Error in cancelClass-->" + ex.getMessage());
			return false;
		}
		finally {
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
		
	}
	
	public static boolean addAfterLastClass(SQLiteDatabase db, String batchcode) {
		try {
			// find out last class date 
			boolean done = false;
			Batch batch = null;
			
			Cursor lastClass = db.query(Database.CLASSES_TABLE_NAME, null,Database.CLASSES_BATCHCODE + " = ?",
					new String[] { batchcode},
					null, null, Database.CLASSES_CLASSDATE + " desc " , "1");
			
			String classdate = null;
  		    if ( lastClass.moveToFirst() ) {
			    classdate = lastClass.getString(lastClass.getColumnIndex( Database.CLASSES_CLASSDATE));
			    // get details of the batch 
			    batch = getBatch(db,batchcode);     
			    // find out next date after the last class 
			    Calendar c = getCalendar(classdate);
			    
			    int classesperweek = Integer.parseInt( batch.getClassesperweek());
      	        c.add( Calendar.DAY_OF_MONTH,1);
			    
      	        int dow = getDayOfWeek(c.get(Calendar.DAY_OF_WEEK));
      	        
      	        if ( dow == 7 && classesperweek == 6) {
      	        	c.add( Calendar.DAY_OF_MONTH,1);  // add one more day
      	        }
      	        else
      	        	if ( dow  == 6 && classesperweek == 5)
      	        		c.add( Calendar.DAY_OF_MONTH,2);  // add two more day
      	        
      	        // insert with new data
      	    
      	    lastClass.close();
     	    ContentValues values = new ContentValues();
  			values.put(Database.CLASSES_BATCHCODE, batch.getCode());
  			values.put(Database.CLASSES_CLASSDATE, calendarToString(c));
  			values.put(Database.CLASSES_CLASSTIME, batch.getStarttime());
  			values.put(Database.CLASSES_CLASSPERIOD, batch.getPeriod());
  			values.put(Database.CLASSES_REMARKS,"");
  			values.put(Database.CLASSES_TOPICS,"");
  			
  			long rowid = db.insert(Database.CLASSES_TABLE_NAME, null,values);
  		    return rowid >= 0; 
  		    }
  		    else
  		    	return false; 
		} 
		catch (Exception ex) {
			Log.d("Account", "Error in deleteLastClass-->" + ex.getMessage());
			return false;
		}
	} // deleteLastClass
		
	
	public static boolean deleteClass(Context context, String classid) {
		DBHelper dbhelper = null;
		SQLiteDatabase db = null;
		try {
			dbhelper = new DBHelper(context);
			db = dbhelper.getWritableDatabase();
     		int rows = db.delete(Database.CLASSES_TABLE_NAME,Database.CLASSES_CLASSES_ID + " = ?",
				     new String[] { classid} );
		   	     return  rows == 1;
			}
		catch (Exception ex) {
			Log.d("CS", "Error in deleteClass-->" + ex.getMessage());
			return false;
		}
		finally {
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	} // deleteClass
	
	public static boolean addBatch(Context context, String batchcode,
			String course, String startdate, String starttime, String classes,
			String period, String classesperweek, String remarks) {

		DBHelper dbhelper = null;
		SQLiteDatabase db = null;
		try {
			dbhelper = new DBHelper(context);
			db = dbhelper.getWritableDatabase();
			db.beginTransaction();

			// execute insert command
			ContentValues values = new ContentValues();
			values.put(Database.BATCHES_BATCHCODE, batchcode);
			values.put(Database.BATCHES_COURSE, course);
			values.put(Database.BATCHES_STARTDATE, startdate);
			values.put(Database.BATCHES_STARTTIME, starttime);
			values.put(Database.BATCHES_CLASSES, classes);
			values.put(Database.BATCHES_PERIOD, period);
			values.put(Database.BATCHES_CLASSESPERWEEK, classesperweek);
			values.put(Database.BATCHES_REMARKS, remarks);

			long rowid = db.insert(Database.BATCHES_TABLE_NAME, null, values);
			Log.d("CS", "Inserted into BATCHES " + rowid);

			addClasses(db, batchcode, startdate, starttime, classes, period,
					classesperweek);

			db.setTransactionSuccessful();
			db.endTransaction();
			return true;
		} catch (Exception ex) {
			Log.d("Account", "Error in addTransaction -->" + ex.getMessage());
			return false;
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	} // addBatch
	
	public static boolean updateBatch(Context context, String batchcode,
			String course, String starttime,String period, String remarks) {

		DBHelper dbhelper = null;
		SQLiteDatabase db = null;
		try {
			dbhelper = new DBHelper(context);
			db = dbhelper.getWritableDatabase();
			db.beginTransaction();

			// execute insert command
			ContentValues values = new ContentValues();
			values.put(Database.BATCHES_BATCHCODE, batchcode);
			values.put(Database.BATCHES_COURSE, course);
			values.put(Database.BATCHES_STARTTIME, starttime);
			values.put(Database.BATCHES_PERIOD, period);
			values.put(Database.BATCHES_REMARKS, remarks);

			int rows = db.update(Database.BATCHES_TABLE_NAME, values, Database.BATCHES_BATCHCODE + " = ?",
					     new String[] { batchcode} );
            if ( rows == 1)
	   	          db.setTransactionSuccessful();
            
			db.endTransaction();
			return true;
		} catch (Exception ex) {
			Log.d("Account", "Error in updateBatch-->" + ex.getMessage());
			return false;
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	} // updateBatch

	
	public static boolean updateClass(Context context, String classid,String classTime, String period,String topics, String remarks) {

		DBHelper dbhelper = null;
		SQLiteDatabase db = null;
		try {
			dbhelper = new DBHelper(context);
			db = dbhelper.getWritableDatabase();

			// execute insert command
			ContentValues values = new ContentValues();
			values.put(Database.CLASSES_CLASSTIME, classTime);
			values.put(Database.CLASSES_CLASSPERIOD,period);
			values.put(Database.CLASSES_TOPICS, topics);
			values.put(Database.CLASSES_REMARKS, remarks);

			int rows = db.update(Database.CLASSES_TABLE_NAME, values, Database.CLASSES_CLASSES_ID + " = ?",
					     new String[] { classid} );
            if ( rows == 1)
          			return true;
            else
            	    return false; 
		} catch (Exception ex) {
			Log.d("Account", "Error in updateClass-->" + ex.getMessage());
			return false;
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	} // updateBatch

	
	
	public static boolean deleteBatch(Context context, String batchcode) {
		DBHelper dbhelper = null;
		SQLiteDatabase db = null;
		try {
			dbhelper = new DBHelper(context);
			db = dbhelper.getWritableDatabase();
			db.beginTransaction();

			int rows = db.delete(Database.CLASSES_TABLE_NAME,Database.BATCHES_BATCHCODE + " = ?",
				     new String[] { batchcode} );
			
			rows = db.delete(Database.BATCHES_TABLE_NAME,Database.BATCHES_BATCHCODE + " = ?",
					     new String[] { batchcode} );
            if ( rows == 1)
	   	          db.setTransactionSuccessful();
            
			db.endTransaction();
			return true;
		} catch (Exception ex) {
			Log.d("Account", "Error in deleteBatch-->" + ex.getMessage());
			return false;
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	} // deleteBatch

	
	
	private static int getDayOfWeek(int dow) {

		if (dow == 1)
			return 7;
		else
			return dow - 1;
	}

	public static void addClasses(SQLiteDatabase db, String batchcode,
			String startdate, String starttime, String classes, String period,
			String classesperweek) throws Exception {
		try {

			// execute insert command
			ContentValues values = new ContentValues();
			values.put(Database.CLASSES_BATCHCODE, batchcode);
			values.put(Database.CLASSES_CLASSTIME, starttime);
			values.put(Database.CLASSES_CLASSPERIOD, period);
			values.put(Database.CLASSES_REMARKS, "");
			values.put(Database.CLASSES_TOPICS, "");

			String[] parts = startdate.split("-");
			int year = Integer.parseInt(parts[0]);
			int month = Integer.parseInt(parts[1]) - 1; // zero based month
			int day = Integer.parseInt(parts[2]);

			Calendar c = Calendar.getInstance();
			c.set(year, month, day);

			int noclasses = Integer.parseInt(classes);
			int cpw = Integer.parseInt(classesperweek);

			int classnumber = 1;

			do {
				int dow = c.get(Calendar.DAY_OF_WEEK);

				if (getDayOfWeek(dow) <= cpw) {
					values.put(Database.CLASSES_CLASSDATE, String.format(
							"%04d-%02d-%02d", c.get(Calendar.YEAR),
							c.get(Calendar.MONTH) + 1,
							c.get(Calendar.DAY_OF_MONTH)));

					long rowid = db.insert(Database.CLASSES_TABLE_NAME, null,
							values);
					Log.d("CS", "Inserted into CLASSES" + rowid);
					classnumber++;
				}

				c.add(Calendar.DAY_OF_MONTH, 1); // increment
			} while (classnumber <= noclasses);
		} catch (Exception ex) {
			Log.d("CS", "Error in addClasses -->" + ex.getMessage());
			throw ex;
		}

	} // addClasses

	public static ArrayList<Batch> getBatches(Context context) {
		DBHelper dbhelper = new DBHelper(context);
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		Cursor batches = db.query(Database.BATCHES_TABLE_NAME, null, null,
				null, null, null, null);
		ArrayList<Batch> list = new ArrayList<Batch>();

		while (batches.moveToNext()) {
			Batch batch = Database.cursorToBatch(batches);
			String enddate = getEndDate(db, batch.getCode());
			batch.setEnddate(enddate);
			list.add(batch);
		}
		batches.close();
		db.close();
		dbhelper.close();
		return list;
	} // getBatches
	
	
	public static ArrayList<Class> getClasses(Context context, String batchcode) {
		DBHelper dbhelper = new DBHelper(context);
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		Cursor classes = db.query(Database.CLASSES_TABLE_NAME, null,
				Database.CLASSES_BATCHCODE + " = ?",
				new String[] { batchcode},
				null, null, Database.CLASSES_CLASSDATE, null);
		
		ArrayList<Class> list = new ArrayList<Class>();

		while (classes.moveToNext()) {
			Class cls = Database.cursorToClass(classes);
			list.add(cls);
		}
		classes.close();
		db.close();
		dbhelper.close();
		return list;
	} // getBatches

	public static String getEndDate(SQLiteDatabase db, String batchcode) {
		Cursor cursor = db.query(Database.CLASSES_TABLE_NAME,
				new String[] { Database.CLASSES_CLASSDATE },
				Database.CLASSES_BATCHCODE + "=?", new String[] { batchcode },
				null, null, Database.CLASSES_CLASSDATE + " desc", "1");
		cursor.moveToFirst();
		String enddate  =  cursor.getString(cursor.getColumnIndex(Database.CLASSES_CLASSDATE));
		cursor.close();
		return enddate;
	}
	
	public static Batch getBatch (SQLiteDatabase db, String batchcode) {
		Cursor batches = db.query(Database.BATCHES_TABLE_NAME, null,
				Database.BATCHES_BATCHCODE + " = ?" ,
				new String [] { batchcode},
 				null, null, null, null);
        Batch batch;		
		if ( batches.moveToNext() ) 
			batch = Database.cursorToBatch(batches);
		else
			batch = null;
		batches.close();
		return batch;
	}
	
	public static Batch getBatch(Context context, String batchcode) {
		DBHelper dbhelper = new DBHelper(context);
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		Batch batch = getBatch(db,batchcode);
		db.close();
		dbhelper.close();
		return batch;
		
	} // getBatch
	
	public static Calendar getCalendar(String date) {
		
		String [] parts = date.split("-");
		Calendar c = Calendar.getInstance();
		c.set( Integer.parseInt( parts[0]), Integer.parseInt( parts[1]) -1, Integer.parseInt( parts[2]));
		return c;
	}
	
	public static Class getClass(Context context, String classid) {
		DBHelper dbhelper = new DBHelper(context);
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		Cursor classes = db.query(Database.CLASSES_TABLE_NAME, null,
				Database.CLASSES_CLASSES_ID + " = ?" ,
				new String [] { classid},
 				null, null, null, null);
        Class clas;		
		if ( classes.moveToNext() ) 
			clas = Database.cursorToClass(classes);
		else
			clas = null;
		
		classes.close();
		db.close();
		dbhelper.close();
		return clas;
		
	} // getBatch

}
