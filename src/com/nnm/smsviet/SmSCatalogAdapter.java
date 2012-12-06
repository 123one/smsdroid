package com.nnm.smsviet;

import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SmSCatalogAdapter extends ArrayAdapter<SmsCatalog> {

	private LayoutInflater inflater;
	private ArrayList<SmsCatalog> list;
	private Bitmap bitmap;

	public SmSCatalogAdapter(final Context context,
			final ArrayList<SmsCatalog> list) {
		super(context, android.R.layout.simple_list_item_1);
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list = list;
	}

	public SmSCatalogAdapter(final Context context,
			final ArrayList<SmsCatalog> list, final Bitmap bitmap) {
		super(context, android.R.layout.simple_list_item_1);
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list = list;
		this.bitmap = bitmap;
	}

	@Override
	public void add(final SmsCatalog object) {
		// TODO Auto-generated method stub
		this.list.add(object);
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		super.clear();
		this.list.clear();
	}

	@Override
	public void addAll(final Collection<? extends SmsCatalog> collection) {
		// TODO Auto-generated method stub
		this.list.addAll(collection);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return this.list.size();
	}

	@Override
	public SmsCatalog getItem(final int position) {
		// TODO Auto-generated method stub
		return this.list.get(position);
	}

	@Override
	public View getView(final int position, View convertView,
			final ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			convertView = this.inflater.inflate(R.layout.sms_collection_item,
					null);
		}
		TextView title = (TextView) convertView.findViewById(R.id.title);
		title.setText(this.getItem(position).title);
		ImageView image = (ImageView) convertView.findViewById(R.id.thumb);
		image.setImageBitmap(this.getItem(position).bitmap);

		return convertView;
	}
}

class SmSAdapter extends ArrayAdapter<SmsCollection> {

	private LayoutInflater inflater;
	private ArrayList<SmsCollection> list;
	private Bitmap bitmap;
	private Context context;

	public SmSAdapter(final Context context,
			final ArrayList<SmsCollection> list, final Bitmap bitmap) {
		super(context, android.R.layout.simple_list_item_1);
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list = list;
		this.bitmap = bitmap;
		this.context = context;
	}

	@Override
	public void remove(final SmsCollection object) {
		// TODO Auto-generated method stub
		this.list.remove(object);
	}

	@Override
	public void add(final SmsCollection object) {
		// TODO Auto-generated method stub
		this.list.add(object);
	}

	@Override
	public void addAll(final Collection<? extends SmsCollection> collection) {
		// TODO Auto-generated method stub
		this.list.addAll(collection);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return this.list.size();
	}

	@Override
	public SmsCollection getItem(final int position) {
		// TODO Auto-generated method stub
		return this.list.get(position);
	}

	@Override
	public View getView(final int position, View convertView,
			final ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			convertView = this.inflater.inflate(R.layout.sms_collection_item,
					null);
		}
		TextView title = (TextView) convertView.findViewById(R.id.title);
		title.setText(this.getItem(position).sms);
		ImageView image = (ImageView) convertView.findViewById(R.id.thumb);
		if (this.bitmap != null) {
			image.setImageBitmap(this.bitmap);
		} else {
			SmsDbHelper db = new SmsDbHelper(this.context);
			image.setImageBitmap(db.getBitmap(this.getItem(position).id));
		}

		return convertView;
	}
}
