package com.nnm.smsviet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.nnm.SmsHandle.Utlis;

public class BrowseSmsCatalog extends SherlockActivity implements OnItemClickListener, Cons {
	protected static final int AUTO_CHECK = 1;
	public static final String version_file = "smsdbversion.txt";
	public static final String dbUrl = "http://xemphimdb.googlecode.com/svn/trunk/datasms/smsdb.sqlite";
	public static final String verURl = "http://xemphimdb.googlecode.com/svn/trunk/datasms/smsdbversion.txt";
	protected static final int DOWNLOAD_DB = 2;
	private ProgressBar progressBar;
	private SmSCatalogAdapter adapter;
	private ListView listview;
	private boolean isUpdating = false;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setTheme(PreferencesActivity.getTheme(this));
		this.setContentView(R.layout.sms_collection_list);
		this.getSupportActionBar().setBackgroundDrawable(
				new ColorDrawable(Color.parseColor("#1D3741")));
		// this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// this.getSupportActionBar().setDisplayShowHomeEnabled(true);
		this.setTitle("Sms collections");
		this.progressBar = (ProgressBar) this.findViewById(R.id.progressBar);
		File dbCheck = new File(SMSdroid.cacheDir + dbName);
		if (!dbCheck.exists()) {
			if (Utlis.checkNetworkStatus(this)) {
				this.isUpdating = true;
				Runnable aRun = new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Utlis.downLoadFile(SMSdroid.cacheDir + dbName, dbUrl);
						Utlis.downLoadFile(SMSdroid.cacheDir + "smsdbversion.txt", verURl);
						Message msg = new Message();
						msg.what = 0;
						BrowseSmsCatalog.this.mHandler.sendMessage(msg);
					}
				};
				(new Thread(aRun)).start();
			} else {
				Toast.makeText(this, "Check your internet connection!", Toast.LENGTH_LONG).show();
			}

		} else {
			this.initUI();

			Runnable aRun = new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (BrowseSmsCatalog.this.isNewVersion()) {
						Message msg = new Message();
						msg.what = AUTO_CHECK;
						BrowseSmsCatalog.this.mHandler.sendMessage(msg);
					}
				}
			};
			(new Thread(aRun)).start();
		}
		Ads.loadAd(this, R.id.ll_ad, MessageListActivity.ADMOB_PUBID, null);
	}

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (this.isUpdating) {
				return false;
			} else {
				this.finish();
			}
		}
		return true;
	}

	public void initUI() {
		try {
			BrowseSmsCatalog.this.isUpdating = false;
			this.listview = (ListView) this.findViewById(R.id.list);
			this.listview.setOnItemClickListener(this);
			SmsDbHelper db = new SmsDbHelper(this);
			ArrayList<SmsCatalog> catalog = db.getCatalog();
			this.adapter = new SmSCatalogAdapter(this, catalog);
			this.listview.setAdapter(this.adapter);
			this.progressBar.setVisibility(View.GONE);
		} catch (Exception e) {
			File dbCheck = new File(SMSdroid.cacheDir + dbName);
			dbCheck.delete();
		}

	}

	@Override
	public void onItemClick(final AdapterView<?> arg0, final View arg1, final int arg2,
			final long arg3) {

		Intent intent = new Intent(this.getApplicationContext(), BrowseSms.class);
		intent.putExtra("id", this.adapter.getItem(arg2).id);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startActivity(intent);
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(final Message msg) {
			int what = msg.what;
			if (what == 0) {

				BrowseSmsCatalog.this.initUI();
				BrowseSmsCatalog.this.progressBar.setVisibility(View.GONE);
			} else if (what == AUTO_CHECK) {
				AlertDialog.Builder builder = new AlertDialog.Builder(BrowseSmsCatalog.this);
				builder.setTitle("New Message!");
				builder.setMessage("Đã có thêm sms mới, bạn có muốn cập nhật không ?")
						.setCancelable(false)
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog, final int id) {
								dialog.cancel();
								BrowseSmsCatalog.this.update();
							}
						}).setNegativeButton("No", new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog, final int id) {
								dialog.cancel();
							}
						});
				AlertDialog alert = builder.create();
				alert.show();
			} else if (what == DOWNLOAD_DB) {
				BrowseSmsCatalog.this.isUpdating = false;
				SmsDbHelper db = new SmsDbHelper(BrowseSmsCatalog.this);
				ArrayList<SmsCatalog> catalog = db.getCatalog();
				BrowseSmsCatalog.this.adapter = new SmSCatalogAdapter(BrowseSmsCatalog.this,
						catalog);
				BrowseSmsCatalog.this.listview.setAdapter(BrowseSmsCatalog.this.adapter);
				BrowseSmsCatalog.this.progressBar.setVisibility(View.GONE);
			}
		}
	};

	public boolean isNewVersion() {
		if (!Utlis.checkNetworkStatus(this)) {
			return false;
		}
		try {
			InputStream input = new URL(verURl).openStream();
			String newVersion = convertStreamToString(input);
			String version = convertStreamToString(new FileInputStream(SMSdroid.cacheDir
					+ "smsdbversion.txt"));
			if (version != null) {
				if (!newVersion.equals(version)) {
					return true;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return true;
		}
		return false;
	}

	public static String convertStreamToString(final InputStream is) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		}
		is.close();
		return sb.toString();
	}

	public void update() {
		this.isUpdating = true;
		this.progressBar.setVisibility(View.VISIBLE);
		this.adapter.clear();
		Runnable aRun = new Runnable() {
			@Override
			public void run() {
				Utlis.downLoadFile(SMSdroid.cacheDir + dbName, dbUrl);
				Utlis.downLoadFile(SMSdroid.cacheDir + version_file, verURl);
				Message msg = new Message();
				msg.what = DOWNLOAD_DB;
				BrowseSmsCatalog.this.mHandler.sendMessage(msg);
			}
		};
		(new Thread(aRun)).start();
	}
}
