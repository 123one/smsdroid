package com.nnm.smsviet;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.nnm.SmsHandle.Utlis;

public class FavSmS extends SQLiteOpenHelper {
	public static final String dbName = "favsms.sqlite";
	private Context context;

	@SuppressLint({ "NewApi", "NewApi" })
	@TargetApi(8)
	public FavSmS(final Context context) {
		super(context, context.getExternalFilesDir(null) + "/" + dbName, null, 1);
		// TODO Auto-generated constructor stub
		this.context = context;

		Log.i("TAG", "getExternalFilesDir " + Environment.getExternalStorageDirectory() + "/"
				+ dbName);
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String create_sms_table = "create TABLE Data (id TEXT, id_catalog TEXT, sms BLOB)";
		db.execSQL(create_sms_table);

		String create_catalog_table = "create TABLE Catalog (id TEXT, title)";
		db.execSQL(create_catalog_table);
	}

	public void insertSms(final SmsCollection sms) {
		ContentValues values = new ContentValues();
		values.put("id", String.valueOf(sms.sms.hashCode()));
		values.put("id_catalog", sms.id);
		values.put("sms", Utlis.Encrypt(sms.sms.getBytes()));

		SQLiteDatabase db = this.getWritableDatabase();
		db.insert("Data", null, values);
		db.close();
	}

	public void deleteSms(final SmsCollection sms) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete("Data", "id" + " = ?", new String[] { String.valueOf(sms.sms.hashCode()) });
		db.close();
	}

	public ArrayList<SmsCollection> getListFav() {
		ArrayList<SmsCollection> list = new ArrayList<SmsCollection>();
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from Data", null);
		if (cursor.getCount() != 0) {
			cursor.moveToFirst();

			do {
				String id = cursor.getString(cursor.getColumnIndex("id_catalog"));
				String sms = new String(Utlis.Decrypt(cursor.getBlob(cursor.getColumnIndex("sms"))));
				SmsCollection smsCollection = new SmsCollection(id, sms);
				list.add(smsCollection);
			} while (cursor.moveToNext());
		}
		db.close();
		cursor.close();
		return list;
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		// TODO Auto-generated method stub

	}

}
