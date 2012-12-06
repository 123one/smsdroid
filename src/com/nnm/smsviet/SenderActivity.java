/**
 * 
 */
package com.nnm.smsviet;

import java.net.URLDecoder;
import java.util.ArrayList;

import android.app.Dialog;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nnm.SmsHandle.HandleSms;
import com.nnm.SmsHandle.Utlis;
import com.nnm.widget.quickAction.ActionItem;
import com.nnm.widget.quickAction.QuickAction;

import de.ub0r.android.lib.Log;
import de.ub0r.android.lib.apis.ContactsWrapper;

/**
 * Class sending messages via standard Messaging interface.
 * 
 * @author flx
 */
public final class SenderActivity extends SherlockActivity implements
		OnClickListener, Cons {
	/** Tag for output. */
	private static final String TAG = "send";

	/** {@link Uri} for saving messages. */
	private static final Uri URI_SMS = Uri.parse("content://sms");
	/** {@link Uri} for saving sent messages. */
	public static final Uri URI_SENT = Uri.parse("content://sms/sent");
	/** Projection for getting the id. */
	private static final String[] PROJECTION_ID = new String[] { BaseColumns._ID };
	/** SMS DB: address. */
	private static final String ADDRESS = "address";
	/** SMS DB: read. */
	private static final String READ = "read";
	/** SMS DB: type. */
	public static final String TYPE = "type";
	/** SMS DB: body. */
	private static final String BODY = "body";
	/** SMS DB: date. */
	private static final String DATE = "date";

	/** Message set action. */
	public static final String MESSAGE_SENT_ACTION = "com.android.mms.transaction.MESSAGE_SENT";

	/** Hold recipient and text. */
	private String to, text;
	/** {@link ClipboardManager}. */
	@SuppressWarnings("deprecation")
	private ClipboardManager cbmgr;

	private QuickAction buaQuickAction;
	public ProgressDialog processDialog;
	private Dialog emo_dialog;
	private EditText etText;
	private MultiAutoCompleteTextView mtv;
	private boolean isMordify = false;
	private String originalSmS = "";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.handleIntent(this.getIntent());
		this.initDialog();
		this.initQuickAction();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		this.handleIntent(intent);
	}

	/**
	 * Handle {@link Intent}.
	 * 
	 * @param intent
	 *            {@link Intent}
	 */
	@SuppressWarnings("deprecation")
	private void handleIntent(final Intent intent) {
		if (this.parseIntent(intent)) {
			this.setTheme(android.R.style.Theme_Translucent_NoTitleBar);
			this.send();
			this.finish();
		} else {
			this.setTheme(PreferencesActivity.getTheme(this));
			this.getSupportActionBar().setBackgroundDrawable(
					new ColorDrawable(Color.parseColor("#1D3741")));
			// SMSdroid.fixActionBarBackground(this.getSupportActionBar(),
			// this.getResources(),
			// R.drawable.bg_striped, R.drawable.bg_striped_img);
			this.setContentView(R.layout.sender);
			this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			this.findViewById(R.id.text_paste).setOnClickListener(this);
			this.etText = (EditText) this.findViewById(R.id.text);
			this.etText.addTextChangedListener(new MyTextWatcher(this,
					(TextView) this.findViewById(R.id.text_paste),
					(TextView) this.findViewById(R.id.text_)));
			this.etText.setText(this.text);
			this.mtv = (MultiAutoCompleteTextView) this.findViewById(R.id.to);
			this.mtv.setAdapter(new MobilePhoneAdapter(this));
			this.mtv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
			this.mtv.setText(this.to);
			if (!TextUtils.isEmpty(this.to)) {
				this.to = this.to.trim();
				if (this.to.endsWith(",")) {
					this.to = this.to.substring(0, this.to.length() - 1).trim();
				}
				if (this.to.indexOf('<') < 0) {
					// try to fetch recipient's name from phone book
					String n = ContactsWrapper.getInstance().getNameForNumber(
							this.getContentResolver(), this.to);
					if (n != null) {
						this.to = n + " <" + this.to + ">, ";
					}
				}
				this.mtv.setText(this.to);
				this.etText.requestFocus();
			} else {
				this.mtv.requestFocus();
			}
			this.cbmgr = (ClipboardManager) this
					.getSystemService(CLIPBOARD_SERVICE);
		}
	}

	/**
	 * Parse data pushed by {@link Intent}.
	 * 
	 * @param intent
	 *            {@link Intent}
	 * @return true if message is ready to send
	 */
	private boolean parseIntent(final Intent intent) {
		Log.d(TAG, "parseIntent(" + intent + ")");
		if (intent == null) {
			return false;
		}
		Log.d(TAG, "got action: " + intent.getAction());

		this.to = null;
		String u = intent.getDataString();
		try {
			if (!TextUtils.isEmpty(u) && u.contains(":")) {
				String t = u.split(":")[1];
				if (t.startsWith("+")) {
					this.to = "+" + URLDecoder.decode(t.substring(1));
				} else {
					this.to = URLDecoder.decode(t);
				}
			}
		} catch (IndexOutOfBoundsException e) {
			Log.w(TAG, "could not split at :", e);
		}
		u = null;

		CharSequence cstext = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
		this.text = null;
		if (cstext != null) {
			this.text = cstext.toString();
			cstext = null;
		}
		if (TextUtils.isEmpty(this.text)) {
			Log.i(TAG, "text missing");
			return false;
		}
		if (TextUtils.isEmpty(this.to)) {
			Log.i(TAG, "recipient missing");
			return false;
		}

		return true;
	}

	/**
	 * Send a message to a single recipient.
	 * 
	 * @param recipient
	 *            recipient
	 * @param message
	 *            message
	 */
	private void send(final String recipient, final String message) {
		Log.d(TAG, "text: " + recipient);
		int[] l = SmsMessage.calculateLength(message, false);
		Log.i(TAG, "text7: " + message.length() + ", " + l[0] + " " + l[1]
				+ " " + l[2] + " " + l[3]);
		l = SmsMessage.calculateLength(message, true);
		Log.i(TAG, "text8: " + message.length() + ", " + l[0] + " " + l[1]
				+ " " + l[2] + " " + l[3]);

		// save draft
		final ContentResolver cr = this.getContentResolver();
		ContentValues values = new ContentValues();
		values.put(TYPE, Message.SMS_DRAFT);
		values.put(BODY, message);
		values.put(READ, 1);
		values.put(ADDRESS, recipient);
		Uri draft = null;
		// save sms to content://sms/sent
		Cursor cursor = cr
				.query(URI_SMS, PROJECTION_ID, TYPE + " = " + Message.SMS_DRAFT
						+ " AND " + ADDRESS + " = '" + recipient + "' AND "
						+ BODY + " like '" + message.replace("'", "_") + "'",
						null, DATE + " DESC");
		if (cursor != null && cursor.moveToFirst()) {
			draft = URI_SENT.buildUpon().appendPath(cursor.getString(0))
					.build();
			Log.d(TAG, "skip saving draft: " + draft);
		} else {
			try {
				draft = cr.insert(URI_SENT, values);
				Log.d(TAG, "draft saved: " + draft);
			} catch (SQLiteException e) {
				Log.e(TAG, "unable to save draft", e);
			}
		}
		values = null;
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		cursor = null;
		SmsManager smsmgr = SmsManager.getDefault();
		final ArrayList<String> messages = smsmgr.divideMessage(message);
		final int c = messages.size();
		ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>(c);

		try {
			Log.d(TAG, "send messages to: " + recipient);

			for (int i = 0; i < c; i++) {
				final String m = messages.get(i);
				Log.d(TAG, "devided messages: " + m);

				final Intent sent = new Intent(MESSAGE_SENT_ACTION, draft,
						this, SmsReceiver.class);
				sentIntents.add(PendingIntent.getBroadcast(this, 0, sent, 0));
			}
			smsmgr.sendMultipartTextMessage(recipient, null, messages,
					sentIntents, null);
			Log.i(TAG, "message sent");
		} catch (Exception e) {
			Log.e(TAG, "unexpected error", e);
			for (PendingIntent pi : sentIntents) {
				if (pi != null) {
					try {
						pi.send();
					} catch (CanceledException e1) {
						Log.e(TAG, "unexpected error", e1);
					}
				}
			}
		}
	}

	/**
	 * Send a message.
	 * 
	 * @return true, if message was sent
	 */
	private boolean send() {
		if (TextUtils.isEmpty(this.to) || TextUtils.isEmpty(this.text)) {
			return false;
		}
		for (String r : this.to.split(",")) {
			r = MobilePhoneAdapter.cleanRecipient(r);
			if (TextUtils.isEmpty(r)) {
				Log.w(TAG, "skip empty recipipient: " + r);
				continue;
			}
			this.send(r, this.text);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.text_paste:
			final CharSequence s = this.cbmgr.getText();
			((EditText) this.findViewById(R.id.text)).setText(s);
			return;
		default:
			break;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		this.getSupportMenuInflater().inflate(R.menu.sender, menu);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in Action Bar clicked; go home
			Intent intent = new Intent(this, ConversationListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			return true;
		case R.id.item_send:
			EditText et = (EditText) this.findViewById(R.id.text);
			this.text = et.getText().toString();
			et = (MultiAutoCompleteTextView) this.findViewById(R.id.to);
			this.to = et.getText().toString();
			if (this.send()) {
				this.finish();
			}
			return true;
		case R.id.item_compose:
			if (!this.isMordify) {
				this.isMordify = true;
				this.originalSmS = this.etText.getText().toString();
			} else if (this.etText.getText().toString().length() != this.originalSmS
					.length()) {
				this.originalSmS = this.etText.getText().toString();
			}
			this.buaQuickAction.show(this.mtv);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void initQuickAction() {

		this.buaQuickAction = new QuickAction(this, QuickAction.VERTICAL);

		ActionItem vietlai = new ActionItem(ID_LAI, "Viết lái", this
				.getResources().getDrawable(R.drawable.smile_1));
		ActionItem daonguocchu = new ActionItem(ID_DAONGUOCCHU,
				"Đảo ngược từng từ", this.getResources().getDrawable(
						R.drawable.smile_2));
		ActionItem daonguoctu = new ActionItem(ID_DAONGUOCTU,
				"Đảo ngược từng chữ", this.getResources().getDrawable(
						R.drawable.smile_3));
		ActionItem bodau = new ActionItem(ID_BODAU, "Tự động bỏ dấu", this
				.getResources().getDrawable(R.drawable.smile_4));
		ActionItem vietnguoc = new ActionItem(ID_VIETNGUOC, "Viết chữ ngược",
				this.getResources().getDrawable(R.drawable.smile_5));
		ActionItem rusify = new ActionItem(ID_RUSIFY, "Rusify", this
				.getResources().getDrawable(R.drawable.smile_6));
		ActionItem textemo = new ActionItem(ID_TEXTEMO, "Text Emoticons", this
				.getResources().getDrawable(R.drawable.smile_7));
		ActionItem binhthuong = new ActionItem(ID_BINHTHUONG, "Bình thường",
				this.getResources().getDrawable(R.drawable.smile_8));
		ActionItem SmsCollections = new ActionItem(ID_SMS_COLLECTIONS,
				"Sms Collections", this.getResources().getDrawable(
						R.drawable.smile_8));
		this.buaQuickAction.addActionItem(vietlai);
		this.buaQuickAction.addActionItem(daonguocchu);
		this.buaQuickAction.addActionItem(daonguoctu);
		this.buaQuickAction.addActionItem(bodau);
		this.buaQuickAction.addActionItem(vietnguoc);
		this.buaQuickAction.addActionItem(rusify);
		this.buaQuickAction.addActionItem(textemo);
		this.buaQuickAction.addActionItem(binhthuong);
		this.buaQuickAction.addActionItem(SmsCollections);
		this.buaQuickAction
				.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {

					@Override
					public void onItemClick(final QuickAction source,
							final int pos, final int actionId) {
						final String sms = SenderActivity.this.etText.getText()
								.toString();
						String smstemp = sms;

						switch (actionId) {
						case 0:// lai tu
							SenderActivity.this.etText.setText(HandleSms
									.reserverSmS(sms));
							break;
						case 1: // dao tung chu
							SenderActivity.this.etText.setText(HandleSms
									.daoNguocTungChuSms(sms));
							break;
						case 2: // dao tung tu
							SenderActivity.this.etText.setText(HandleSms
									.daoNguocTungTuSms(sms));
							break;
						case 3: // tu bo dau
							if (!Utlis.checkNetworkStatus(SenderActivity.this)) {
								Toast.makeText(
										SenderActivity.this,
										"�?ể sử dụng chức năng này, bạn cần mở truy cập internet!",
										3000).show();
							} else {
								SenderActivity.this
										.showProcessDialog("Vui lòng ch�?!");
								Runnable aRun = new Runnable() {

									@Override
									public void run() {
										android.os.Message msg = new android.os.Message();
										msg.obj = HandleSms.tuDongBoDau(sms);
										msg.what = BO_DAU_SMS;
										SenderActivity.this.mHandler
												.sendMessage(msg);
									}
								};
								(new Thread(aRun)).start();

							}
							break;
						case 4: // viet nguoc
							if (Utlis.getAPILevel() < 14) {
								Toast.makeText(SenderActivity.this,
										"Only for Android 4.0 and higher!",
										Toast.LENGTH_LONG).show();
							} else {
								SenderActivity.this.etText.setText(Utlis
										.flipString(smstemp));
							}
							break;
						case 5: // rusify
							SenderActivity.this.etText.setText(Utlis
									.RusifyString(smstemp));
							break;
						case 6: // text emoticons
							// textEmoQA.show(buaBtn);
							SenderActivity.this.emo_dialog.show();
							break;
						case 7: // binh thuong
							SenderActivity.this.etText
									.setText(SenderActivity.this.originalSmS);
							break;
						case ID_SMS_COLLECTIONS:
							Intent intent = new Intent(SenderActivity.this,
									BrowseSmsCatalog.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							SenderActivity.this.startActivity(intent);
							break;
						}
					}
				});
	}

	public void initDialog() {
		this.emo_dialog = new Dialog(SenderActivity.this);
		// dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.emo_dialog.setTitle("Text Emoticons");
		this.emo_dialog.setCancelable(true);
		this.emo_dialog.setContentView(R.layout.emo_girdview);
		GridView gridview = (GridView) this.emo_dialog
				.findViewById(R.id.gridview);
		gridview.setAdapter(new EmoAdapter(this));
		this.emo_dialog.setCanceledOnTouchOutside(true);
		gridview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> arg0, final View arg1,
					final int arg2, final long arg3) {
				if (SenderActivity.this.etText.getText().toString()
						.endsWith(" ")) {
					SenderActivity.this.etText.append(emo[arg2]);
				} else {
					SenderActivity.this.etText.append(" " + emo[arg2]);
				}
				SenderActivity.this.emo_dialog.cancel();
			}
		});
	}

	public void showProcessDialog(final String status) {
		this.processDialog = new ProgressDialog(this);
		this.processDialog.setMessage(status);
		this.processDialog.show();
	}

	public void offProcessDialog() {
		if (this.processDialog != null) {
			this.processDialog.dismiss();
		}
	}

	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(final android.os.Message msg) {
			SenderActivity.this.offProcessDialog();
			SenderActivity.this.etText.setText(msg.obj.toString());
		}
	};
}
