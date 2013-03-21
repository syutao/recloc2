package com.example.recloc.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.recloc.Calc;
import com.example.recloc.ConstantsMain;
import com.example.recloc.R.id;

import java.io.File;

import com.example.recloc.db.Constants;

public class DBase
{
	private static final String TAG = "DataBase Processing";

	private static SQLiteDatabase bssidDB;
	private static SQLiteDatabase locTagDB;
	private static SQLiteDatabase rssiDB;

	public DBase()
	{
		File localFile = new File(Constants.DTABASE_PATH);
		if (!localFile.exists())
		{
			localFile.mkdir();
			System.out.println("111");
		}

		bssidDB = OpenDB("bssid.db");
		locTagDB = OpenDB("locLabel.db");
		rssiDB = OpenDB("fingerprint.db");
		System.out.println(Constants.DTABASE_PATH);
	}

	public SQLiteDatabase getRssiDB()
	{
		return rssiDB;
	}
	
	public SQLiteDatabase getlocTagDB()
	{
		return locTagDB;
	}

	public void UpdataRecording(Calc[] calcRSS, String tableName, int sizeofcalcRSS)
	{
		String seriesNum = "f"+UUIDGenerator.genTimeStamp();
		System.out.println(seriesNum);

		UpdatalocLoabel(seriesNum, tableName);
		UpdataRSSI(calcRSS, seriesNum, sizeofcalcRSS);
	}

	private void UpdatalocLoabel(String locCode, String locTag) {

		if (!TableExist(locTagDB, "Tags"))
		{
			CreateTable(locTagDB, "Tags", new String[]{"seriesNum","tag1","bssid"});
		}
		locTagDB.execSQL("INSERT INTO Tags VALUES(null,?,?,null)", new Object[]{locCode,locTag});
	}

	private void UpdataRSSI(Calc[] calcRSS, String tableName, int sizeofcalcRSS)
	{
		//CreateTable(SQLiteDatabase SQLiteDatabase, String tableName, String[] keyName)
		System.out.println(tableName+" creating");

		if (!TableExist(rssiDB, tableName))
		{
			CreateTable(rssiDB, tableName, new String[]{"bssid","meanRSS","varRSS", "v1RSS","v2RSS","v3RSS","v4RSS","v5RSS"});
		}		
		System.out.println(tableName+" created");

		rssiDB.beginTransaction();
		try{
			for(int i =0; i< sizeofcalcRSS; i++){
				//				rssiDB.execSQL("INSERT INTO "+tableName+" VALUES(null,?,?,?)", new Object[]{calcRSS[i].name,calcRSS[i].mean,calcRSS[i].var});
				rssiDB.execSQL("INSERT INTO "+tableName+" VALUES(null,?,?,?,?,?,?,?,?)", 
						new Object[]{calcRSS[i].name,calcRSS[i].mean,calcRSS[i].var,
						calcRSS[i].data[0],calcRSS[i].data[1],calcRSS[i].data[2],calcRSS[i].data[3],calcRSS[i].data[4]});
			}
			rssiDB.setTransactionSuccessful();
		}finally{
			rssiDB.endTransaction();
		}
		System.out.println(tableName+" updata success");
	}
	
	
	public void UpdataBssid(Calc[] calcRSS, int sizeofcalcRSS)
	{
		if (!TableExist(bssidDB, "bssid_Total"))
		{
			CreateTable(bssidDB, "bssid_Total", new String[]{"bssid"});
		}

		bssidDB.beginTransaction();
		try{
			for(int i =0; i< sizeofcalcRSS; i++){
				bssidDB.execSQL("INSERT INTO bssid_Total VALUES(null,?)", new Object[]{calcRSS[i].name});
			}
			bssidDB.setTransactionSuccessful();
		}finally{
			bssidDB.endTransaction();
		}
	}

	private boolean TableExist(SQLiteDatabase db, String tableName)
	{
		String str = "select * from " + tableName;
		boolean fTableExist = true;

		try 
		{
			Cursor cursor = db.rawQuery(str, null);
			if(-1 == cursor.getColumnIndex("bssid"))
			{
				fTableExist = false;
			}
		} 
		catch (SQLiteException e) 
		{
			fTableExist = false;
		}

		return fTableExist;
	}

	private SQLiteDatabase OpenDB(String dbName)
	{
		SQLiteDatabase localSQLiteDatabase;

		String str = Constants.DTABASE_PATH + "/" + dbName;
		localSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(str, null);
		System.out.println(str);
		return localSQLiteDatabase;
	}

	private boolean CreateTable(SQLiteDatabase db, String tableName, String[] keyName)
	{
		String sql = "CREATE TABLE " + tableName + "(_id INTEGER PRIMARY KEY";
		for (int j = 0; j < keyName.length; j++)
		{
			sql = sql + ", " + keyName[j] + " VARCHAR";
		}
		sql = sql + ")";

		db.execSQL(sql);

		return TableExist(db, tableName);
	}
}