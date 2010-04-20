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
package de.ub0r.android.smsdroid.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;
import de.ub0r.android.smsdroid.Conversation;
import de.ub0r.android.smsdroid.R;

/**
 * @author flx
 */
public final class AsyncHelper extends AsyncTask<Void, Void, Void> {
	private final Context context;

	private String mAddress;
	private final long mThreadId;
	private final Conversation mConversation;
	private Bitmap mPhoto = null;
	private String mName = null;
	private int mCount = -1;

	private final TextView targetTvName;
	private final ImageView targetIvPhoto;
	private final TextView targetTvAddress;
	private final TextView targetTvCount;

	/**
	 * Fill Views by address.
	 * 
	 * @param c
	 *            {@link Context}
	 * @param address
	 *            address
	 * @param tvName
	 *            {@link TextView} for name
	 * @param ivPhoto
	 *            {@link ImageView} for photo
	 */
	private AsyncHelper(final Context c, final String address,
			final TextView tvName, final ImageView ivPhoto) {
		this.context = c;

		this.mThreadId = -1;
		this.mConversation = null;
		this.mAddress = address;

		this.targetTvName = tvName;
		this.targetIvPhoto = ivPhoto;

		this.targetTvAddress = null;
		this.targetTvCount = null;
	}

	/**
	 * Fill Views by threadId.
	 * 
	 * @param c
	 *            {@link Context}
	 * @param conversation
	 *            {@link Conversation}
	 * @param threadId
	 *            threadId
	 * @param tvAddress
	 *            {@link TextView} for address
	 * @param tvName
	 *            {@link TextView} for name
	 * @param tvCount
	 *            {@link TextView} for count
	 */
	private AsyncHelper(final Context c, final Conversation conversation,
			final long threadId, final TextView tvAddress,
			final TextView tvName, final TextView tvCount) {
		this.context = c;

		if (conversation != null) {
			this.mConversation = conversation;
			this.mThreadId = conversation.getThreadId();
			this.mAddress = conversation.getAddress();
		} else {
			this.mAddress = null;
			this.mConversation = null;
			this.mThreadId = threadId;
		}

		this.targetTvAddress = tvAddress;
		this.targetTvName = tvName;
		this.targetTvCount = tvCount;

		this.targetIvPhoto = null;
	}

	/**
	 * Fill Views by address.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param address
	 *            address
	 * @param tvName
	 *            {@link TextView} for name
	 * @param ivPhoto
	 *            {@link ImageView} for photo
	 */
	public static void fillByAddress(final Context context,
			final String address, final TextView tvName, // .
			final ImageView ivPhoto) {
		if (context == null || address == null) {
			return;
		}
		if (Persons.poke(address, ivPhoto != null)) {
			// load sync.
			if (tvName != null) {
				tvName.setText(Persons.getName(context, address));
			}
			if (ivPhoto != null) {
				ivPhoto.setImageBitmap(Persons.getPicture(context, address));
			}
		} else {
			// load async.
			new AsyncHelper(context, address, tvName, ivPhoto)
					.execute((Void[]) null);
		}

	}

	/**
	 * Fill Views by threadId.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param conversation
	 *            {@link Conversation}
	 * @param threadId
	 *            threadId
	 * @param tvAddress
	 *            {@link TextView} for address
	 * @param tvName
	 *            {@link TextView} for name
	 * @param tvCount
	 *            {@link TextView} for count
	 */
	public static void fillByThread(final Context context,
			final Conversation conversation, final long threadId,
			final TextView tvAddress, final TextView tvName,
			final TextView tvCount) {
		long tId = threadId;
		if (tId < 0 && conversation != null) {
			tId = conversation.getId();
		}
		if (context == null || tId < 0) {
			return;
		}
		if (Threads.poke(tId)) {
			if (tvAddress != null || tvName != null) {
				final String a = Threads.getAddress(context, tId);
				if (conversation != null && conversation.getAddress() == null
						&& a != null) {
					conversation.setAddress(a);
				}
				if (tvAddress != null) {
					tvAddress.setText(a);
				}
				if (tvName != null) {
					fillByAddress(context, a, tvName, null);
				}
			}
			if (tvCount != null) {
				tvCount.setText(// .
						"(" + Threads.getCount(context, tId) + ")");
			}
		} else {
			new AsyncHelper(context, conversation, tId, tvAddress, tvName,
					tvCount).execute((Void[]) null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Void doInBackground(final Void... arg0) {
		if (this.mThreadId < 0) { // run Persons
			// in: mAddress
			// out: *Photo, *Name
			if (this.targetIvPhoto != null) {
				this.mPhoto = Persons.getPicture(this.context, this.mAddress);
			}
			if (this.targetTvName != null) {
				this.mName = Persons.getName(this.context, this.mAddress);
			}
		} else { // run Threads
			// in: mThreadId
			// out: *Address, *Count, *Name,
			if (this.targetTvAddress != null || this.targetTvName != null) {
				this.mAddress = Threads
						.getAddress(this.context, this.mThreadId);
			}
			if (this.targetTvCount != null) {
				this.mCount = Threads.getCount(this.context, this.mThreadId);
			}
			if (this.targetTvName != null) {
				this.mName = Persons.getName(this.context, this.mAddress);
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onPreExecute() {
		if (this.targetIvPhoto != null) {
			this.targetIvPhoto.setImageResource(R.drawable.ic_contact_picture);
		}
		if (this.targetTvAddress != null) {
			this.targetTvAddress.setText("...");
		}
		if (this.targetTvCount != null) {
			this.targetTvCount.setText("");
		}
		if (this.targetTvName != null) {
			if (this.mAddress != null) {
				this.targetTvName.setText(this.mAddress);
			} else {
				this.targetTvName.setText("...");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onPostExecute(final Void result) {
		if (this.targetIvPhoto != null && this.mPhoto != null) {
			this.targetIvPhoto.setImageBitmap(this.mPhoto);
		}
		if (this.targetTvAddress != null && this.mAddress != null) {
			this.targetTvAddress.setText(this.mAddress);
		}
		if (this.targetTvCount != null && this.mCount > 0) {
			this.targetTvCount.setText("(" + this.mCount + ")");
		}
		if (this.targetTvName != null) {
			if (this.mName != null) {
				this.targetTvName.setText(this.mName);
			} else if (this.mAddress != null) {
				this.targetTvName.setText(this.mAddress);
			}
		}
		if (this.mConversation != null
				&& this.mConversation.getAddress() == null
				&& this.mAddress != null) {
			this.mConversation.setAddress(this.mAddress);
		}
	}
}
