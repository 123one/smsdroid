package com.nnm.smsviet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class EmoAdapter extends BaseAdapter {
	private Context mContext;

	public EmoAdapter(final Context c) {
		this.mContext = c;
	}

	public int getCount() {
		return Cons.emo.length;
	}

	public Object getItem(final int position) {
		return null;
	}

	public long getItemId(final int position) {
		return 0;
	}

	// create a new ImageView for each item referenced by the Adapter
	public View getView(final int position, final View convertView,
			final ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) this.mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.emo_grid_row, null);
		TextView textview = (TextView) view.findViewById(R.id.emo_title);
		textview.setText(Cons.emo[position]);
		return view;
	}

	// references to our images
	// private Integer[] mThumbIds = {
	// R.drawable.sample_2, R.drawable.sample_3,
	// R.drawable.sample_4, R.drawable.sample_5,
	// R.drawable.sample_6, R.drawable.sample_7,
	// R.drawable.sample_0, R.drawable.sample_1,
	// R.drawable.sample_2, R.drawable.sample_3,
	// R.drawable.sample_4, R.drawable.sample_5,
	// R.drawable.sample_6, R.drawable.sample_7,
	// R.drawable.sample_0, R.drawable.sample_1,
	// R.drawable.sample_2, R.drawable.sample_3,
	// R.drawable.sample_4, R.drawable.sample_5,
	// R.drawable.sample_6, R.drawable.sample_7
	// };
}
