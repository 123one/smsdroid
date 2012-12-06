package com.nnm.smsviet;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.nnm.SmsHandle.Utlis;

public class SmsDbHelper extends SQLiteOpenHelper {
	private String path;

	public SmsDbHelper(final Context context) {
		super(context, "smsdb", null, 1);
		// TODO Auto-generated constructor stub
		this.path = SMSdroid.cacheDir + Cons.dbName;
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		// TODO Auto-generated method stub

	}

	public Bitmap getBitmap(final String id) {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(this.path, null,
				SQLiteDatabase.OPEN_READWRITE);

		Cursor cursor = db.rawQuery("select * from Catalog where id = '" + id + "'", null);
		cursor.moveToFirst();

		byte[] temp = cursor.getBlob(cursor.getColumnIndex("image"));
		db.close();
		cursor.close();
		return BitmapFactory.decodeByteArray(temp, 0, temp.length);
	}

	public String getTitle(final String id) {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(this.path, null,
				SQLiteDatabase.OPEN_READWRITE);

		Cursor cursor = db.rawQuery("select * from Catalog where id = '" + id + "'", null);
		cursor.moveToFirst();

		String temp = cursor.getString(cursor.getColumnIndex("title"));
		db.close();
		cursor.close();
		return temp;
	}

	public ArrayList<SmsCatalog> getCatalog() {
		ArrayList<SmsCatalog> list = new ArrayList<SmsCatalog>();

		SQLiteDatabase db = SQLiteDatabase.openDatabase(this.path, null,
				SQLiteDatabase.OPEN_READWRITE);

		Cursor cursor = db.rawQuery("select * from Catalog", null);
		if (cursor.getCount() != 0) {
			cursor.moveToFirst();

			do {
				String title = cursor.getString(cursor.getColumnIndex("title"));
				String id = cursor.getString(cursor.getColumnIndex("id"));
				byte[] temp = cursor.getBlob(cursor.getColumnIndex("image"));
				Bitmap bitmap = BitmapFactory.decodeByteArray(temp, 0, temp.length);
				SmsCatalog sms = new SmsCatalog(id, title, bitmap);
				list.add(sms);
			} while (cursor.moveToNext());
		}
		db.close();
		cursor.close();
		return list;
	}

	public ArrayList<SmsCollection> getSms(final String id) {
		ArrayList<SmsCollection> smss = new ArrayList<SmsCollection>();
		SQLiteDatabase db = SQLiteDatabase.openDatabase(this.path, null,
				SQLiteDatabase.OPEN_READWRITE);
		Cursor cursor = db.rawQuery("select * from Data where id = '" + id + "'", null);
		if (cursor.getCount() != 0) {
			cursor.moveToFirst();
			do {
				String id_ = cursor.getString(cursor.getColumnIndex("id"));
				byte[] temp = cursor.getBlob(cursor.getColumnIndex("sms"));
				String sms_ = new String(Utlis.Decrypt(temp));
				SmsCollection sms = new SmsCollection(id_, sms_);
				smss.add(sms);
			} while (cursor.moveToNext());
		}
		db.close();
		cursor.close();
		return smss;
	}
}
