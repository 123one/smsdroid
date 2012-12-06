package com.nnm.smsviet;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.nnm.SmsHandle.AccentRemover;
import com.nnm.widget.quickAction.ActionItem;
import com.nnm.widget.quickAction.QuickAction;
import com.nnm.widget.quickAction.QuickAction.OnActionItemClickListener;

@SuppressLint({ "NewApi", "NewApi" })
public class BrowseSms extends SherlockActivity implements OnItemClickListener,
		OnItemLongClickListener {
	private ProgressBar pgBar;
	private ListView listview;
	private SmSAdapter adapter;
	private QuickAction quickAction;
	private int selectIndex;
	public static final String favorites = "Favorites";
	private boolean isFav = false;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setTheme(PreferencesActivity.getTheme(this));
		this.setContentView(R.layout.sms_collection_list);

		this.getSupportActionBar().setBackgroundDrawable(
				new ColorDrawable(Color.parseColor("#1D3741")));
		// this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// this.getSupportActionBar().setHomeButtonEnabled(true);
		this.listview = (ListView) this.findViewById(R.id.list);
		this.listview.setOnItemClickListener(this);
		// this.listview.setOnItemLongClickListener(this);
		this.pgBar = (ProgressBar) this.findViewById(R.id.progressBar);

		Bundle bundle = this.getIntent().getExtras();
		String id = bundle.getString("id");
		if (id.equals("fav")) {
			this.isFav = true;
			FavSmS favdb = new FavSmS(this);
			ArrayList<SmsCollection> list = favdb.getListFav();

			this.setTitle(favorites);
			this.adapter = new SmSAdapter(this, list, null);
		} else {
			SmsDbHelper db = new SmsDbHelper(this);

			Bitmap bitmap = db.getBitmap(id);
			ArrayList<SmsCollection> list = db.getSms(id);

			this.setTitle(db.getTitle(id));
			this.adapter = new SmSAdapter(this, list, bitmap);
		}

		this.listview.setAdapter(this.adapter);
		this.pgBar.setVisibility(View.GONE);
		this.initQuickAction();
		Ads.loadAd(this, R.id.ll_ad, MessageListActivity.ADMOB_PUBID, null);
	}

	@Override
	public void onItemClick(final AdapterView<?> arg0, final View arg1,
			final int arg2, final long arg3) {
		// TODO Auto-generated method stub
		this.selectIndex = arg2;
		this.quickAction.show(arg1);
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> arg0, final View arg1,
			final int arg2, final long arg3) {
		// TODO Auto-generated method stub

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Gửi tin nhắn")
				.setPositiveButton("Không dấu",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								String sms = BrowseSms.this.adapter
										.getItem(arg2).sms;
								sms = sms.replace("�?", "D");
								sms = sms.replace("đ", "d");

								Intent i = BrowseSms.this.buildIntent(false,
										true, AccentRemover.LoaiBoDau(sms));
								BrowseSms.this.startActivity(i);
								dialog.cancel();
							}
						})
				.setNegativeButton("Có dấu",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								String sms = BrowseSms.this.adapter
										.getItem(arg2).sms;
								// Intent i = BrowseSms.this.buildIntent(false,
								// true, sms);
								// BrowseSms.this.startActivity(i);
								Intent i = new Intent(BrowseSms.this,
										SenderActivity.class);
								i.putExtra("AUTOSEND", "1");
								i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								i.putExtra("sms_body", sms);
								BrowseSms.this.startActivity(i);
								dialog.cancel();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();

		return false;
	}

	public Intent buildIntent(final boolean autosend,
			final boolean showChooser, final String text) {
		final Intent i = this.getComposeIntent(this, null);

		i.putExtra(Intent.EXTRA_TEXT, text);
		i.putExtra("sms_body", text);
		if (autosend && text.length() > 0) {
			i.putExtra("AUTOSEND", "1");
		}
		if (showChooser) {
			return Intent.createChooser(i, this.getString(R.string.reply));
		} else {
			return i;
		}
	}

	private Intent getComposeIntent(final Context context, final String address) {
		// final Intent i = new Intent(Intent.ACTION_SENDTO);
		Intent i = new Intent(this, SenderActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (address == null) {
			i.setData(Uri.parse("sms:"));
		} else {
			i.setData(Uri.parse("smsto:"
					+ PreferencesActivity.fixNumber(context, address)));
		}
		return i;
	}

	public void initQuickAction() {
		final int ID_SHARE = 0;
		final int ID_SEND = 1;
		final int ID_BOOKMARK = 2;
		final int ID_REMOVE = 3;
		final int ID_COPY = 4;

		this.quickAction = new QuickAction(this, QuickAction.VERTICAL);

		ActionItem share = new ActionItem(ID_SHARE, "Share", this
				.getResources().getDrawable(android.R.drawable.ic_menu_share));
		ActionItem send = new ActionItem(ID_SEND, "Send", this.getResources()
				.getDrawable(R.drawable.ic_menu_send));
		ActionItem bookmark = new ActionItem(ID_BOOKMARK, "Bookmark", this
				.getResources().getDrawable(android.R.drawable.ic_menu_add));
		ActionItem remove = new ActionItem(ID_REMOVE, "Delete", this
				.getResources().getDrawable(android.R.drawable.ic_menu_delete));

		ActionItem copy = new ActionItem(ID_COPY, "Copy", this.getResources()
				.getDrawable(android.R.drawable.ic_menu_edit));

		this.quickAction.addActionItem(share);
		this.quickAction.addActionItem(send);
		if (this.isFav) {
			this.quickAction.addActionItem(remove);
		} else {
			this.quickAction.addActionItem(bookmark);
		}
		this.quickAction.addActionItem(copy);

		this.quickAction
				.setOnActionItemClickListener(new OnActionItemClickListener() {

					@TargetApi(11)
					@SuppressLint({ "NewApi", "NewApi" })
					@Override
					public void onItemClick(final QuickAction source,
							final int pos, final int actionId) {
						// TODO Auto-generated method stub

						if (actionId == ID_SHARE) {
							String sms = BrowseSms.this.adapter
									.getItem(BrowseSms.this.selectIndex).sms;
							Intent sharingIntent = new Intent(
									Intent.ACTION_SEND);
							sharingIntent.setType("text/html");
							sharingIntent.putExtra(Intent.EXTRA_TEXT,
									Html.fromHtml(sms));
							BrowseSms.this.startActivity(Intent.createChooser(
									sharingIntent, "Share using"));

						} else if (actionId == ID_SEND) {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									BrowseSms.this);
							builder.setMessage("Gửi tin nhắn")
									.setPositiveButton(
											"Không dấu",
											new DialogInterface.OnClickListener() {
												public void onClick(
														final DialogInterface dialog,
														final int id) {
													String sms = BrowseSms.this.adapter
															.getItem(BrowseSms.this.selectIndex).sms;
													sms = sms
															.replace("�?", "D");
													sms = sms.replace("đ", "d");
													Intent i = BrowseSms.this
															.buildIntent(
																	false,
																	true,
																	AccentRemover
																			.LoaiBoDau(sms));
													BrowseSms.this
															.startActivity(i);
													dialog.cancel();
												}
											})
									.setNegativeButton(
											"Có dấu",
											new DialogInterface.OnClickListener() {
												public void onClick(
														final DialogInterface dialog,
														final int id) {
													String sms = BrowseSms.this.adapter
															.getItem(BrowseSms.this.selectIndex).sms;
													Intent i = BrowseSms.this
															.buildIntent(false,
																	true, sms);
													BrowseSms.this
															.startActivity(i);
													dialog.cancel();
												}
											});
							AlertDialog alert = builder.create();
							alert.show();
						} else if (actionId == ID_BOOKMARK) {
							SmsCollection sms = BrowseSms.this.adapter
									.getItem(BrowseSms.this.selectIndex);
							FavSmS favdb = new FavSmS(BrowseSms.this);
							favdb.insertSms(sms);
							Toast.makeText(BrowseSms.this, "Bookmark!",
									Toast.LENGTH_SHORT).show();
						} else if (actionId == ID_REMOVE) {
							SmsCollection sms = BrowseSms.this.adapter
									.getItem(BrowseSms.this.selectIndex);
							FavSmS favdb = new FavSmS(BrowseSms.this);
							favdb.deleteSms(sms);
							Toast.makeText(BrowseSms.this, "Deleted!",
									Toast.LENGTH_SHORT).show();
							BrowseSms.this.adapter.remove(sms);
							BrowseSms.this.adapter.notifyDataSetChanged();
						} else if (actionId == ID_COPY) {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									BrowseSms.this);
							builder.setMessage("Gửi tin nhắn")
									.setPositiveButton(
											"Không dấu",
											new DialogInterface.OnClickListener() {
												public void onClick(
														final DialogInterface dialog,
														final int id) {
													String sms = BrowseSms.this.adapter
															.getItem(BrowseSms.this.selectIndex).sms;
													sms = sms
															.replace("�?", "D");
													sms = sms.replace("đ", "d");
													BrowseSms.this
															.CopyToClipboard(AccentRemover
																	.LoaiBoDau(sms));
													dialog.cancel();
												}
											})
									.setNegativeButton(
											"Có dấu",
											new DialogInterface.OnClickListener() {
												public void onClick(
														final DialogInterface dialog,
														final int id) {
													BrowseSms.this
															.CopyToClipboard(BrowseSms.this.adapter
																	.getItem(BrowseSms.this.selectIndex).sms);
													dialog.cancel();
												}
											});
							AlertDialog alert = builder.create();
							alert.show();
						}
					}
				});
	}

	@TargetApi(11)
	@SuppressLint({ "NewApi", "NewApi", "NewApi" })
	public void CopyToClipboard(final String string) {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) BrowseSms.this
					.getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(string);
		} else {
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) BrowseSms.this
					.getSystemService(Context.CLIPBOARD_SERVICE);
			android.content.ClipData clip = android.content.ClipData
					.newPlainText("text label", string);
			clipboard.setPrimaryClip(clip);
		}
		Toast.makeText(BrowseSms.this, "Copy!", Toast.LENGTH_SHORT).show();
	}
}
