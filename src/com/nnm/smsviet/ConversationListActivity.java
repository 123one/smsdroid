/*
 * Copyright (C) 2010-2012 Felix Bechstein
 * 
 * This file is part of SMSdroid.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package com.nnm.smsviet;

import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.ads.AdView;
import com.nnm.SmsHandle.Utlis;

import de.ub0r.android.lib.apis.Contact;
import de.ub0r.android.lib.apis.ContactsWrapper;

/**
 * Main {@link SherlockActivity} showing conversations.
 * 
 * @author flx
 */
public final class ConversationListActivity extends SherlockActivity implements
		OnItemClickListener, OnItemLongClickListener {
	/** Tag for output. */
	public static final String TAG = "main";

	/** ORIG_URI to resolve. */
	static final Uri URI = Uri.parse("content://mms-sms/conversations/");

	/** Number of items. */
	private static final int WHICH_N = 6;
	/** Index in dialog: answer. */
	private static final int WHICH_ANSWER = 0;
	/** Index in dialog: answer. */
	private static final int WHICH_CALL = 1;
	/** Index in dialog: view/add contact. */
	private static final int WHICH_VIEW_CONTACT = 2;
	/** Index in dialog: view. */
	private static final int WHICH_VIEW = 3;
	/** Index in dialog: delete. */
	private static final int WHICH_DELETE = 4;
	/** Index in dialog: mark as spam. */
	private static final int WHICH_MARK_SPAM = 5;

	/** Minimum date. */
	public static final long MIN_DATE = 10000000000L;
	/** Miliseconds per seconds. */
	public static final long MILLIS = 1000L;

	/** Show contact's photo. */
	public static boolean showContactPhoto = false;
	/** Show emoticons in {@link MessageListActivity}. */
	public static boolean showEmoticons = false;

	/** Dialog items shown if an item was long clicked. */
	private String[] longItemClickDialog = null;

	/** Conversations. */
	private ConversationAdapter adapter = null;

	/** {@link Calendar} holding today 00:00. */
	private static final Calendar CAL_DAYAGO = Calendar.getInstance();
	private AdView adview;

	public static final String moreAppQuery = "market://search?q=pub:Minhsk";

	public static final String verAppUrl = "http://xemphimdb.googlecode.com/svn/trunk/datasms/smsvietver.txt";

	public static final int HAVE_NEW_APP_VS = 0;
	static {
		// Get time for now - 24 hours
		CAL_DAYAGO.add(Calendar.DAY_OF_MONTH, -1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStart() {
		super.onStart();
		AsyncHelper.setAdapter(this.adapter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStop() {
		super.onStop();
		AsyncHelper.setAdapter(null);
	}

	/**
	 * Get {@link AbsListView}.
	 * 
	 * @return {@link AbsListView}
	 */
	private AbsListView getListView() {
		return (AbsListView) this.findViewById(android.R.id.list);
	}

	/**
	 * Set {@link ListAdapter} to {@link ListView}.
	 * 
	 * @param la
	 *            ListAdapter
	 */
	private void setListAdapter(final ListAdapter la) {
		AbsListView v = this.getListView();
		if (v instanceof GridView) {
			((GridView) v).setAdapter(la);
		} else if (v instanceof ListView) {
			((ListView) v).setAdapter(la);
		}

	}

	/**
	 * Show all rows of a particular {@link Uri}.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param u
	 *            {@link Uri}
	 */
	static void showRows(final Context context, final Uri u) {
		Log.d(TAG, "-----GET HEADERS-----");
		Log.d(TAG, "-- " + u.toString() + " --");
		Cursor c = context.getContentResolver().query(u, null, null, null, null);
		if (c != null) {
			int l = c.getColumnCount();
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < l; i++) {
				buf.append(i + ":");
				buf.append(c.getColumnName(i));
				buf.append(" | ");
			}
			Log.d(TAG, buf.toString());
		}

	}

	/**
	 * Show rows for debugging purposes.
	 * 
	 * @param context
	 *            {@link Context}
	 */
	static void showRows(final Context context) {
		// this.showRows(ContactsWrapper.getInstance().getUriFilter());
		// showRows(context, URI);
		// showRows(context, Uri.parse("content://sms/"));
		// showRows(context, Uri.parse("content://mms/"));
		// showRows(context, Uri.parse("content://mms/part/"));
		// showRows(context, ConversationProvider.CONTENT_URI);
		// showRows(context, Uri.parse("content://mms-sms/threads"));
		// this.showRows(Uri.parse(MessageList.URI));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNewIntent(final Intent intent) {
		final Intent i = intent;
		if (i != null) {
			Log.d(TAG, "got intent: " + i.getAction());
			Log.d(TAG, "got uri: " + i.getData());
			final Bundle b = i.getExtras();
			if (b != null) {
				Log.d(TAG, "user_query: " + b.get("user_query"));
				Log.d(TAG, "got extra: " + b);
			}
			final String query = i.getStringExtra("user_query");
			Log.d(TAG, "user query: " + query);
			// TODO: do something with search query
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		final Intent i = this.getIntent();
		Log.d(TAG, "got intent: " + i.getAction());
		Log.d(TAG, "got uri: " + i.getData());
		Log.d(TAG, "got extra: " + i.getExtras());

		this.setTheme(PreferencesActivity.getTheme(this));
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("use_gridlayout", false)) {
			this.setContentView(R.layout.conversationgrid);
		} else {
			this.setContentView(R.layout.conversationlist);
		}
		// SMSdroid.fixActionBarBackground(this.getSupportActionBar(),
		// this.getResources(),
		// R.drawable.bg_striped, R.drawable.bg_striped_img);

		this.getSupportActionBar().setBackgroundDrawable(
				new ColorDrawable(Color.parseColor("#1D3741")));
		showRows(this);

		final AbsListView list = this.getListView();
		this.adapter = new ConversationAdapter(this);
		this.setListAdapter(this.adapter);
		list.setOnItemClickListener(this);
		list.setOnItemLongClickListener(this);
		this.longItemClickDialog = new String[WHICH_N];
		this.longItemClickDialog[WHICH_ANSWER] = this.getString(R.string.reply);
		this.longItemClickDialog[WHICH_CALL] = this.getString(R.string.call);
		this.longItemClickDialog[WHICH_VIEW_CONTACT] = this.getString(R.string.view_contact_);
		this.longItemClickDialog[WHICH_VIEW] = this.getString(R.string.view_thread_);
		this.longItemClickDialog[WHICH_DELETE] = this.getString(R.string.delete_thread_);
		this.longItemClickDialog[WHICH_MARK_SPAM] = this.getString(R.string.filter_spam_);

		// this.adview = (AdView) this.findViewById(R.id.adView);
		// this.adview.loadAd(new AdRequest());
		Ads.loadAd(this, R.id.ll_ad, MessageListActivity.ADMOB_PUBID, null);

		Runnable runVS = new Runnable() {
			@Override
			public void run() {
				InputStream input;
				try {
					input = new URL(verAppUrl).openStream();
					String newVersion = Utlis.convertStreamToString(input);
					newVersion = newVersion.trim();
					PackageInfo pInfo = ConversationListActivity.this.getPackageManager()
							.getPackageInfo(ConversationListActivity.this.getPackageName(), 0);
					if (!newVersion.equals(pInfo.versionName)) {
						android.os.Message msg = new android.os.Message();
						msg.what = HAVE_NEW_APP_VS;
						msg.obj = newVersion;
						ConversationListActivity.this.mHandler.sendMessage(msg);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		(new Thread(runVS)).start();
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(final android.os.Message msg) {
			// TODO Auto-generated method stub
			int what = msg.what;

			if (what == HAVE_NEW_APP_VS) {
				String curVersion = "";
				try {
					PackageInfo pInfo = ConversationListActivity.this.getPackageManager()
							.getPackageInfo(ConversationListActivity.this.getPackageName(), 0);
					curVersion = pInfo.versionName;
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(ConversationListActivity.this);
				builder.setTitle("Update Notice");
				builder.setMessage(
						"New version " + msg.obj.toString() + "\n" + "Current Version: "
								+ curVersion).setCancelable(false)
						.setPositiveButton("Update", new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog, final int id) {
								dialog.cancel();
								String PACKAGE_NAME = ConversationListActivity.this
										.getApplicationContext().getPackageName();
								Intent intent = new Intent(Intent.ACTION_VIEW, Uri
										.parse("market://details?id=" + PACKAGE_NAME));
								ConversationListActivity.this.startActivity(intent);
							}
						}).setNegativeButton("Skip", new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog, final int id) {
								dialog.cancel();
							}
						});
				AlertDialog alert = builder.create();
				alert.show();
			}
		}

	};

	@Override
	protected void onResume() {
		super.onResume();
		CAL_DAYAGO.setTimeInMillis(System.currentTimeMillis());
		CAL_DAYAGO.add(Calendar.DAY_OF_MONTH, -1);

		final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
		showContactPhoto = p.getBoolean(PreferencesActivity.PREFS_CONTACT_PHOTO, true);
		showEmoticons = p.getBoolean(PreferencesActivity.PREFS_EMOTICONS, false);
		this.adapter.startMsgListQuery();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		this.getSupportMenuInflater().inflate(R.menu.conversationlist, menu);
		// if (DonationHelper.hideAds(this)) {
		// menu.removeItem(R.id.item_donate);
		// }
		return true;
	}

	/**
	 * Mark all messages with a given {@link Uri} as read.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param uri
	 *            {@link Uri}
	 * @param read
	 *            read status
	 */
	static void markRead(final Context context, final Uri uri, final int read) {
		Log.d(TAG, "markRead(" + uri + "," + read + ")");
		if (uri == null) {
			return;
		}
		String[] sel = Message.SELECTION_UNREAD;
		if (read == 0) {
			sel = Message.SELECTION_READ;
		}
		final ContentResolver cr = context.getContentResolver();
		final ContentValues cv = new ContentValues();
		cv.put(Message.PROJECTION[Message.INDEX_READ], read);
		try {
			cr.update(uri, cv, Message.SELECTION_READ_UNREAD, sel);
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "failed update", e);
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		SmsReceiver.updateNewMessageNotification(context, null);
	}

	/**
	 * Delete messages with a given {@link Uri}.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param uri
	 *            {@link Uri}
	 * @param title
	 *            title of Dialog
	 * @param message
	 *            message of the Dialog
	 * @param activity
	 *            {@link Activity} to finish when deleting.
	 */
	static void deleteMessages(final Context context, final Uri uri, final int title,
			final int message, final Activity activity) {
		Log.i(TAG, "deleteMessages(..," + uri + " ,..)");
		final Builder builder = new Builder(context);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setNegativeButton(android.R.string.no, null);
		builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				final int ret = context.getContentResolver().delete(uri, null, null);
				Log.d(TAG, "deleted: " + ret);
				if (activity != null && !activity.isFinishing()) {
					activity.finish();
				}
				if (ret > 0) {
					Conversation.flushCache();
					Message.flushCache();
					SmsReceiver.updateNewMessageNotification(context, null);
				}
			}
		});
		builder.show();
	}

	/**
	 * Add or remove an entry to/from blacklist.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param addr
	 *            address
	 */
	private static void addToOrRemoveFromSpamlist(final Context context, final String addr) {
		final SpamDB db = new SpamDB(context);
		db.open();
		if (!db.isInDB(addr)) {
			db.insertNr(addr);
			Log.d(TAG, "Added " + addr + " to spam list");
		} else {
			db.removeNr(addr);
			Log.d(TAG, "Removed " + addr + " from spam list");
		}
		db.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_compose:
			// final Intent i = getComposeIntent(this, null);
			Intent i = new Intent(ConversationListActivity.this, SenderActivity.class);
			try {
				this.startActivity(i);
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, "error launching intent: " + i.getAction() + ", " + i.getData());
				Toast.makeText(this,
						"error launching messaging app!\n" + "Please contact the developer.",
						Toast.LENGTH_LONG).show();
			}
			return true;
			// case R.id.item_settings: // start settings activity
			// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// this.startActivity(new Intent(this,
			// Preferences11Activity.class));
			// } else {
			// this.startActivity(new Intent(this, PreferencesActivity.class));
			// }
			// return true;
		case R.id.item_delete_all_threads:
			deleteMessages(this, Uri.parse("content://sms/"), R.string.delete_threads_,
					R.string.delete_threads_question, null);
			return true;
		case R.id.item_mark_all_read:
			markRead(this, Uri.parse("content://sms/"), 1);
			markRead(this, Uri.parse("content://mms/"), 1);
			return true;
		case R.id.item_collection:
			Intent intent = new Intent(ConversationListActivity.this, BrowseSmsCatalog.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.startActivity(intent);
			return true;

		case R.id.item_more_app:
			Intent moreapp = new Intent(Intent.ACTION_VIEW, Uri.parse(moreAppQuery));
			ConversationListActivity.this.startActivity(moreapp);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Get a {@link Intent} for sending a new message.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param address
	 *            address
	 * @return {@link Intent}
	 */
	static Intent getComposeIntent(final Context context, final String address) {
		final Intent i = new Intent(Intent.ACTION_SENDTO);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (address == null) {
			i.setData(Uri.parse("sms:"));
		} else {
			i.setData(Uri.parse("smsto:" + PreferencesActivity.fixNumber(context, address)));
		}
		return i;
	}

	/**
	 * {@inheritDoc}
	 */
	public void onItemClick(final AdapterView<?> parent, final View view, final int position,
			final long id) {
		final Conversation c = Conversation.getConversation(this,
				(Cursor) parent.getItemAtPosition(position), false);
		final Uri target = c.getUri();
		final Intent i = new Intent(this, MessageListActivity.class);
		i.setData(target);
		try {
			this.startActivity(i);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "error launching intent: " + i.getAction() + ", " + i.getData());
			Toast.makeText(this,
					"error launching messaging app!\n" + "Please contact the developer.",
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean onItemLongClick(final AdapterView<?> parent, final View view,
			final int position, final long id) {
		final Conversation c = Conversation.getConversation(this,
				(Cursor) parent.getItemAtPosition(position), true);
		final Uri target = c.getUri();
		Builder builder = new Builder(this);
		String[] items = this.longItemClickDialog;
		final Contact contact = c.getContact();
		final String a = contact.getNumber();
		Log.d(TAG, "p: " + a);
		final String n = contact.getName();
		if (TextUtils.isEmpty(n)) {
			builder.setTitle(a);
			items = items.clone();
			items[WHICH_VIEW_CONTACT] = this.getString(R.string.add_contact_);
		} else {
			builder.setTitle(n);
		}
		final SpamDB db = new SpamDB(this.getApplicationContext());
		db.open();
		if (db.isInDB(a)) {
			items = items.clone();
			items[WHICH_MARK_SPAM] = this.getString(R.string.dont_filter_spam_);
		}
		db.close();
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				Intent i = null;
				switch (which) {
				case WHICH_ANSWER:
					ConversationListActivity.this.startActivity(getComposeIntent(
							ConversationListActivity.this, a));
					break;
				case WHICH_CALL:
					i = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + a));
					ConversationListActivity.this.startActivity(i);
					break;
				case WHICH_VIEW_CONTACT:
					if (n == null) {
						i = ContactsWrapper.getInstance().getInsertPickIntent(a);
						Conversation.flushCache();
					} else {
						final Uri uri = c.getContact().getUri();
						i = new Intent(Intent.ACTION_VIEW, uri);
					}
					ConversationListActivity.this.startActivity(i);
					break;
				case WHICH_VIEW:
					i = new Intent(ConversationListActivity.this, MessageListActivity.class);
					i.setData(target);
					ConversationListActivity.this.startActivity(i);
					break;
				case WHICH_DELETE:
					ConversationListActivity.deleteMessages(ConversationListActivity.this, target,
							R.string.delete_thread_, R.string.delete_thread_question, null);
					break;
				case WHICH_MARK_SPAM:
					ConversationListActivity.addToOrRemoveFromSpamlist(
							ConversationListActivity.this, c.getContact().getNumber());
					break;
				default:
					break;
				}
			}
		});
		builder.create().show();
		return true;
	}

	/**
	 * Convert time into formated date.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param time
	 *            time
	 * @return formated date.
	 */
	static String getDate(final Context context, final long time) {
		long t = time;
		if (t < MIN_DATE) {
			t *= MILLIS;
		}
		if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
				PreferencesActivity.PREFS_FULL_DATE, false)) {
			return DateFormat.getTimeFormat(context).format(t) + " "
					+ DateFormat.getDateFormat(context).format(t);
		} else if (t < CAL_DAYAGO.getTimeInMillis()) {
			return DateFormat.getDateFormat(context).format(t);
		} else {
			return DateFormat.getTimeFormat(context).format(t);
		}
	}
}
