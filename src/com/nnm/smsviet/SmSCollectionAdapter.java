package com.nnm.smsviet;

import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SmSCollectionAdapter extends ArrayAdapter<SmsCatalog> {

	private LayoutInflater inflater;
	private ArrayList<SmsCatalog> list;

	public SmSCollectionAdapter(final Context context,
			final ArrayList<SmsCatalog> list) {
		super(context, android.R.layout.simple_list_item_1);
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list = list;
	}

	@Override
	public void add(final SmsCatalog object) {
		// TODO Auto-generated method stub
		this.list.add(object);
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
			TextView title = (TextView) convertView.findViewById(R.id.title);
			title.setText(this.getItem(position).title);
			ImageView image = (ImageView) convertView.findViewById(R.id.thumb);
			image.setImageBitmap(this.getItem(position).bitmap);
		}

		return convertView;
	}

}
