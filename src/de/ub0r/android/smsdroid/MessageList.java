/*
 * Copyright (C) 2010 Felix Bechstein
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
package de.ub0r.android.smsdroid;

import java.util.List;

import android.app.ListActivity;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CallLog.Calls;
import android.text.ClipboardManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 * {@link ListActivity} showing a single conversation.
 * 
 * @author flx
 */
public class MessageList extends ListActivity implements OnClickListener,
		OnItemLongClickListener {
	/** Tag for output. */
	private static final String TAG = "SMSdroid.ml";

	/** Number of items. */
	private static final int WHICH_N = 4;
	// private static final int WHICH_N = 5;
	/** Index in dialog: forward. */
	private static final int WHICH_FORWARD = 0;
	/** Index in dialog: copy text. */
	private static final int WHICH_COPY_TEXT = 1;
	/** Index in dialog: view details. */
	private static final int WHICH_VIEW_DETAILS = 2;
	/** Index in dialog: delete. */
	private static final int WHICH_DELETE = 3;
	/** Index in dialog: speak. */
	private static final int WHICH_SPEAK = 4;

	/** Address. */
	private String address = null;
	/** URI to resolve. */
	static final String URI = "content://mms-sms/conversations/";

	/** Used {@link Uri}. */
	private Uri uri;

	/** Dialog items shown if an item was long clicked. */
	private String[] longItemClickDialog = null;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTheme(Preferences.getTheme(this));
		this.setContentView(R.layout.messagelist);

		// if (!SMSdroid.prefsNoAds) {
		// this.findViewById(R.id.ad).setVisibility(View.VISIBLE);
		// }

		this.findViewById(R.id.answer).setOnClickListener(this);

		final Intent i = this.getIntent();
		this.uri = i.getData();
		if (this.uri != null) {
			this.parseIntent(i);
		}
		final ListView list = this.getListView();
		list.setOnItemLongClickListener(this);
		this.longItemClickDialog = new String[WHICH_N];
		this.longItemClickDialog[WHICH_FORWARD] = this
				.getString(R.string.forward_);
		this.longItemClickDialog[WHICH_COPY_TEXT] = this
				.getString(R.string.copy_text_);
		this.longItemClickDialog[WHICH_VIEW_DETAILS] = this
				.getString(R.string.view_details_);
		this.longItemClickDialog[WHICH_DELETE] = this
				.getString(R.string.delete_message_);
		// this.longItemClickDialog[WHICH_SPEAK] =
		// this.getString(R.string.speak_);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		this.uri = intent.getData();
		if (this.uri != null) {
			this.parseIntent(intent);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void onClick(final View v) {
		switch (v.getId()) {
		case R.id.answer:
			try {
				final Intent i = new Intent(Intent.ACTION_SENDTO);
				i.setData(Uri.parse("smsto:" + this.address));
				this.startActivity(i);
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, "could not find app to compose message", e);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Parse data pushed by {@link Intent}.
	 * 
	 * @param intent
	 *            {@link Intent}
	 */
	private void parseIntent(final Intent intent) {
		Log.d(TAG, "got intent: " + this.uri.toString());

		List<String> p = this.uri.getPathSegments();
		String threadID = p.get(p.size() - 1);

		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		final ListView lv = this.getListView();
		String sort;
		if (prefs.getBoolean(Preferences.PREFS_MSGLIST_SORT, true)) {
			sort = MessageListAdapter.SORT_USD;
			lv.setStackFromBottom(true);
		} else {
			sort = MessageListAdapter.SORT_NORM;
			lv.setStackFromBottom(false);
		}

		Cursor mCursor = this.getContentResolver().query(this.uri,
				MessageListAdapter.PROJECTION, null, null, sort);
		this.startManagingCursor(mCursor);
		MessageListAdapter adapter = new MessageListAdapter(this, mCursor);
		this.setListAdapter(adapter);
		if (mCursor.moveToFirst()) {
			this.address = mCursor.getString(MessageListAdapter.INDEX_ADDRESS);
		}
		String pers = CachePersons.getName(this, this.address, null);
		if (pers == null) {
			pers = this.address;
		}

		this.setTitle(this.getString(R.string.app_name) + " > " + pers);
		this.setRead(threadID);
	}

	/**
	 * Set all messages in a given thread as read.
	 * 
	 * @param threadID
	 *            thread id
	 */
	private void setRead(final String threadID) {
		SMSdroid.markRead(this, Uri.parse(URI + threadID));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.messagelist, menu);
		return true;
	}

	/**
	 *{@inheritDoc}
	 */
	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_delete_thread:
			SMSdroid.deleteMessages(this, this.uri, R.string.delete_thread_);
			return true;
		case R.id.item_all_threads:
			this.startActivity(new Intent(this, SMSdroid.class));
			return true;
		case R.id.item_compose:
			try {
				final Intent i = new Intent(Intent.ACTION_SENDTO);
				i.setData(Uri.parse("sms:"));
				this.startActivity(i);
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, "could not find app to compose message", e);
			}
			return true;
		default:
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean onItemLongClick(final AdapterView<?> parent,
			final View view, final int position, final long id) {
		final Context context = this;
		final Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		final Uri target = Uri.parse("content://sms/"
				+ cursor.getInt(MessageListAdapter.INDEX_ID));
		Builder builder = new Builder(context);
		builder.setTitle(R.string.message_options_);
		builder.setItems(this.longItemClickDialog,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						switch (which) {
						case WHICH_FORWARD:
							final Intent i = new Intent(Intent.ACTION_SEND);
							i.setType("text/plain");
							i.putExtra(Intent.EXTRA_TEXT, cursor
									.getString(MessageListAdapter.INDEX_BODY));
							context.startActivity(Intent.createChooser(i,
									context.getString(R.string.forward_)));
							break;
						case WHICH_COPY_TEXT:
							final ClipboardManager cm = // .
							(ClipboardManager) context.getSystemService(// .
									Context.CLIPBOARD_SERVICE);
							cm.setText(cursor
									.getString(MessageListAdapter.INDEX_BODY));
							break;
						case WHICH_VIEW_DETAILS:
							final int t = cursor
									.getInt(MessageListAdapter.INDEX_TYPE);
							Builder b = new Builder(context);
							b.setTitle(R.string.view_details_);
							b.setCancelable(true);
							StringBuilder sb = new StringBuilder();
							final String a = cursor.getString(// .
									MessageListAdapter.INDEX_ADDRESS);
							final long d = cursor
									.getLong(MessageListAdapter.INDEX_DATE);
							final String ds = DateFormat.format(
									context.getString(// .
											R.string.DATEFORMAT_details), d)
									.toString();
							String sentReceived;
							String fromTo;
							if (t == Calls.INCOMING_TYPE) {
								sentReceived = context
										.getString(R.string.received_);
								fromTo = context.getString(R.string.from_);
							} else if (t == Calls.OUTGOING_TYPE) {
								sentReceived = context
										.getString(R.string.sent_);
								fromTo = context.getString(R.string.to_);
							} else {
								sentReceived = "ukwn:";
								fromTo = "ukwn:";
							}
							sb.append(sentReceived + " ");
							sb.append(ds);
							sb.append("\n");
							sb.append(fromTo + " ");
							sb.append(a);
							b.setMessage(sb.toString());
							b.setPositiveButton(android.R.string.ok, null);
							b.show();
							break;
						case WHICH_DELETE:
							SMSdroid.deleteMessages(context, target,
									R.string.delete_message_);
							break;
						case WHICH_SPEAK:
							// TODO: implement me
							Toast.makeText(context, R.string.not_implemented,
									Toast.LENGTH_SHORT).show();
							break;
						default:
							break;
						}
					}
				});
		builder.show();
		return true;
	}

}
