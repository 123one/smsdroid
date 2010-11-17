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

import java.util.concurrent.RejectedExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import de.ub0r.android.lib.Log;
import de.ub0r.android.lib.apis.ContactsWrapper;

/**
 * @author flx
 */
public final class AsyncHelper extends AsyncTask<Void, Void, Void> {
	/** Tag for logging. */
	static final String TAG = "ash";

	/** Pattern to clean up numbers. */
	private static final Pattern PATTERN_CLEAN_NUMBER = Pattern
			.compile("<(\\+?[0-9]+)>");

	/** Wrapper to use for contacts API. */
	private static final ContactsWrapper WRAPPER = ContactsWrapper
			.getInstance();

	/** {@link ConversationAdapter} to invalidate on new data. */
	private static ConversationAdapter adapter = null;

	/** {@link Context}. */
	private final Context context;
	/** {@link Conversation}. */
	private final Conversation mConversation;

	/**
	 * Fill {@link Conversation}.
	 * 
	 * @param c
	 *            {@link Context}
	 * @param conv
	 *            {@link Conversation}
	 */

	private AsyncHelper(final Context c, final Conversation conv) {
		this.context = c;
		this.mConversation = conv;
	}

	/**
	 * Fill Conversations data. If needed: spawn threads.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param c
	 *            {@link Conversation}
	 * @param sync
	 *            fetch of information
	 */
	public static void fillConversation(final Context context,
			final Conversation c, final boolean sync) {
		Log.d(TAG, "fillConversation(ctx, conv, " + sync + ")");
		if (context == null || c == null || c.getThreadId() < 0) {
			return;
		}
		AsyncHelper helper = new AsyncHelper(context, c);
		if (sync) {
			helper.doInBackground((Void) null);
		} else {
			try {
				helper.execute((Void) null);
			} catch (RejectedExecutionException e) {
				Log.e(TAG, "rejected exceution", e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Void doInBackground(final Void... arg0) {
		ContentValues cv = new ContentValues();
		if (this.mConversation == null) {
			return null;
		}
		Uri uri = this.mConversation.getUri();
		Cursor cursor = this.context.getContentResolver().query(uri,
				Message.PROJECTION_JOIN, null, null, null);

		// count
		this.mConversation.setCount(cursor.getCount());

		// address
		String address = this.mConversation.getAddress();
		Log.d(TAG, "address: " + address);
		if (address == null) {
			if (cursor.moveToLast()) {
				do {
					address = cursor.getString(Message.INDEX_ADDRESS);
				} while (address == null && cursor.moveToPrevious());
			}
			if (address != null) {
				this.mConversation.setAddress(address);
				Log.d(TAG, "new address: " + address);
				cv.put(ConversationProvider.PROJECTION[// .
						ConversationProvider.INDEX_ADDRESS], address);
			}
		}
		if (this.mConversation.getBody() == null && cursor.moveToLast()) {
			final Message m = Message.getMessage(this.context, cursor);
			final CharSequence b = m.getBody();
			if (b != null) {
				this.mConversation.setBody(b.toString());
			}
		}

		// contact
		String pid = this.mConversation.getPersonId();
		if ((pid == null || pid.length() == 0) && address != null) {
			final Cursor contact = getContact(this.context, address);
			if (contact != null) {
				pid = contact.getString(ContactsWrapper.FILTER_INDEX_ID);
				String n = contact.getString(ContactsWrapper.FILTER_INDEX_NAME);
				this.mConversation.setPersonId(pid);
				this.mConversation.setName(n);
				cv.put(ConversationProvider.PROJECTION[// .
						ConversationProvider.INDEX_PID], pid);
				cv.put(ConversationProvider.PROJECTION[// .
						ConversationProvider.INDEX_NAME], n);
			} else {
				this.mConversation.setPersonId(Conversation.NO_CONTACT);
			}
		}

		// read
		cursor = this.context.getContentResolver().query(uri,
				Message.PROJECTION,
				Message.PROJECTION[Message.INDEX_READ] + " = 0", null, null);
		if (cursor.getCount() == 0) {
			this.mConversation.setRead(1);
		} else {
			this.mConversation.setRead(0);
		}

		// update changes
		if (cv.size() > 0) {
			this.context.getContentResolver().update(
					this.mConversation.getInternalUri(), cv, null, null);
		}
		cursor.close();
		cursor = null;

		// photo
		if (ConversationList.showContactPhoto && // .
				this.mConversation.getPhoto() == null && pid != null) {
			this.mConversation.setPhoto(getPictureForPerson(this.context, pid));
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onPostExecute(final Void result) {
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * Set {@link ConversationAdapter} to invalidate data after refreshing.
	 * 
	 * @param a
	 *            {@link ConversationAdapter}
	 */
	public static void setAdapter(final ConversationAdapter a) {
		adapter = a;
	}

	/**
	 * Get a contact's name by address.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param address
	 *            address
	 * @return name
	 */
	public static String getContactName(final Context context,
			final String address) {
		Log.d(TAG, "getContactName(ctx, " + address + ")");
		if (address == null) {
			return null;
		}
		Cursor cursor = getContact(context, address);
		if (cursor == null) {
			return null;
		}
		return cursor.getString(ContactsWrapper.FILTER_INDEX_NAME);
	}

	/**
	 * Get (id, name) for address.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param address
	 *            address
	 * @return {@link Cursor}
	 */
	private static synchronized Cursor getContact(final Context context,
			final String address) {
		Log.d(TAG, "getContact(ctx, " + address + ")");
		if (address == null) {
			return null;
		}
		// clean up number
		String realAddress = address;
		final Matcher m = PATTERN_CLEAN_NUMBER.matcher(realAddress);
		if (m.find()) {
			realAddress = m.group(1);
		}
		// address contains the phone number
		try {
			final Cursor cursor = WRAPPER.getContact(context
					.getContentResolver(), realAddress);
			if (cursor != null && cursor.moveToFirst()) {
				return cursor;
			}
		} catch (Exception e) {
			Log.e(TAG, "failed to fetch contact", e);
		}
		Log.d(TAG, "nothing found!");
		return null;
	}

	/**
	 * Get picture for contact.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param pid
	 *            contact
	 * @return {@link Bitmap}
	 */
	private static Bitmap getPictureForPerson(final Context context,
			final String pid) {
		if (pid == null || pid.length() == 0 || pid == Conversation.NO_CONTACT) {
			return Conversation.NO_PHOTO;
		}
		Bitmap b = WRAPPER.loadContactPhoto(context, pid);
		if (b == null) {
			return Conversation.NO_PHOTO;
		} else {
			return b;
		}
	}
}
