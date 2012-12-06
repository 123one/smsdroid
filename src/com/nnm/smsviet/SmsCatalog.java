package com.nnm.smsviet;

import android.graphics.Bitmap;

public class SmsCatalog {
	String title;
	String id;
	Bitmap bitmap;

	public SmsCatalog(final String id, final String title, final Bitmap bitmap) {
		this.title = title;
		this.id = id;
		this.bitmap = bitmap;
	}
}

class SmsCollection {
	String id;
	String sms;

	public SmsCollection(final String id, final String sms) {
		this.id = id;
		this.sms = sms;
	}
}