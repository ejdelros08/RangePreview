/**
 * Created by EJ Del Rosario
 * Copyright (c) 2015
 * Personal Intellectual Property
 * All Rights Reserved
 */

package ejdelrosario.framework.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public abstract class Table {
	
private SQLiteDatabase db;
	
	public abstract String getTableStructure();
	public abstract String getName();
	
	public long insert(ContentValues values) {
		db = DatabaseHelper.getInstance().getWritableDatabase();
		if(db != null) return db.insertOrThrow( getName(), null, values);
		return -1;

	}
	
	public long insertOrUpdate( ContentValues values , String filter ) {
		db = DatabaseHelper.getInstance().getWritableDatabase();
		int rowsAffected = db.update( getName() , values, filter , null );
		
		if(rowsAffected == 0 )
			return db.insertOrThrow( getName(), null, values);
		
		return rowsAffected;
	}
	
	public int update(ContentValues values, String whereClause) throws SQLiteException , SQLException {
		db = DatabaseHelper.getInstance().getWritableDatabase();
		if(db != null) return db.update( getName(), values, whereClause, null);
		return -1;
	}
	
	public int update(ContentValues values, String whereClause, String[] filterValues) throws SQLiteException , SQLException {
		db = DatabaseHelper.getInstance().getWritableDatabase();
		if(db != null) return db.update( getName(), values, whereClause, filterValues );
		return -1;
	}
	
	public int delete() throws SQLiteException , SQLException {
		db = DatabaseHelper.getInstance().getWritableDatabase();
		if(db != null) return db.delete( getName(), null, null);
		return -1;
	}
	
	public int delete(String whereClause, String[] filterValues) throws SQLiteException , SQLException {
		db = DatabaseHelper.getInstance().getWritableDatabase();
		if(db != null) return db.delete( getName(), whereClause, filterValues);
		return -1;
	}
	
	public Cursor select() throws SQLiteException , SQLException {
		db = DatabaseHelper.getInstance().getReadableDatabase();
		if(db != null) return db.rawQuery( "SELECT * FROM " + getName(), null);
		return null;
	}
	
	public Cursor select(String filter,String[] filterValues) throws SQLiteException , SQLException {
		db = DatabaseHelper.getInstance().getReadableDatabase();
		if(db != null) return db.rawQuery( "SELECT * FROM " + getName() + " where " + filter, filterValues );
		return null;
	}
	
	public Cursor rawQuery(String query, String[] filterValues) throws SQLiteException , SQLException {
		db = DatabaseHelper.getInstance().getReadableDatabase();
		if(db != null) return db.rawQuery( query , filterValues );
		return null;
	}

}
