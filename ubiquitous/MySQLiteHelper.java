package com.sajadian.ubiquitous;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MySQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_LOCATIONS = "locations";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_LOCATION = "location";
	public static final String COLUMN_RATE = "rate";
	public static final String COLUMN_BLOOD = "blood";
	public static final String COLUMN_POLLUTION = "pollution";
	public static final String COLUMN_SIGNS = "signs";
	public static final String COLUMN_DATE = "datetime";
	private static final String DATABASE_CREATE = "create table " + TABLE_LOCATIONS +
			"(" + COLUMN_ID + " integer primary key autoincrement, " +
			COLUMN_LOCATION + " text not null, " +
			COLUMN_RATE + " integer null, " +
			COLUMN_POLLUTION + " integer not null, " +
			COLUMN_DATE + " text not null, " +
			COLUMN_SIGNS + " text null, " +
			COLUMN_BLOOD + " text null);";
	private static final String DATABASE_NAME = "mylocations.db";
	private static final int DATABASE_VERSION = 2;

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(MySQLiteHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");

		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
		onCreate(db);
	}

	public void insert(Pollution pollution) {
		ContentValues newValue = new ContentValues();
		newValue.put(COLUMN_LOCATION, pollution.getLocation());
		newValue.put(COLUMN_POLLUTION, pollution.getPollution());
		newValue.put(COLUMN_RATE, pollution.getRate());
		newValue.put(COLUMN_DATE, pollution.getDate());
		newValue.put(COLUMN_SIGNS, pollution.saveSigns());
		newValue.put(COLUMN_BLOOD, pollution.saveBlood());
		SQLiteDatabase db = this.getWritableDatabase();
		db.insert(MySQLiteHelper.TABLE_LOCATIONS, null, newValue);
		db.close();
	}

	public void delete(long l) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = MySQLiteHelper.COLUMN_ID + " = " + l;
		db.delete(MySQLiteHelper.TABLE_LOCATIONS, where, null);
		db.close();
	}

	public ArrayList<Pollution> getAll() {
		ArrayList<Pollution> pollutionList = new ArrayList<Pollution>();
		String[] cols = new String[]{COLUMN_ID, COLUMN_LOCATION, COLUMN_RATE, COLUMN_POLLUTION, COLUMN_DATE, COLUMN_SIGNS, COLUMN_BLOOD};
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(MySQLiteHelper.TABLE_LOCATIONS, cols, null, null, null, null, COLUMN_ID + " DESC");

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Pollution pollution = cursorToPollutions(cursor);
			pollutionList.add(pollution);
			cursor.moveToNext();
		}
		cursor.close();
		db.close();
		return pollutionList;
	}

	public Pollution getLast() {
		String[] cols = new String[]{COLUMN_ID, COLUMN_LOCATION, COLUMN_RATE, COLUMN_POLLUTION, COLUMN_DATE, COLUMN_SIGNS, COLUMN_BLOOD};
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(MySQLiteHelper.TABLE_LOCATIONS, cols, null, null, null, null, COLUMN_ID + " DESC", "1");
		cursor.moveToFirst();
		try {
			while (!cursor.isAfterLast()) {
				Pollution pollution = cursorToPollutions(cursor);
				SimpleDateFormat df = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy", Locale.ENGLISH);
				Date d = df.parse(pollution.getDate());
				long now=System.currentTimeMillis();
				if(now-d.getTime()<300000){
					cursor.close();
					db.close();
					return pollution;
				}
				cursor.moveToNext();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		cursor.close();
		db.close();
		return null;
	}

	private Pollution cursorToPollutions(Cursor cursor) {
		Pollution pollution = new Pollution();
		pollution.setID(cursor.getInt(0));
		pollution.setLocation(cursor.getString(1));
		pollution.setRate(cursor.getInt(2));
		pollution.setPollution(cursor.getInt(3));
		pollution.setDate(cursor.getString(4));
		pollution.setSigns(cursor.getString(5));
		pollution.setBlood(cursor.getString(6));
		return pollution;
	}
}
