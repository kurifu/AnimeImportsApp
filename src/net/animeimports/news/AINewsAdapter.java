package net.animeimports.news;

import java.util.ArrayList;

import net.animeimports.android.R;
import net.animeimports.league.LeaguePlayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AINewsAdapter extends ArrayAdapter<AINewsItem> {
	private ArrayList<AINewsItem> items;
	private Context mContext = null;

	public ArrayList<AINewsItem> getItems() {
		return items;
	}
	
	public AINewsAdapter(Context context, int textViewResourceId, ArrayList<AINewsItem> items) {
		super(context, textViewResourceId, items);
		mContext = context;
		if(items.size() != 0) {
			this.items = items;
		}
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if(v == null) {
			LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.row_news_item, null);
		}
		AINewsItem p = items.get(position);
		TextView tNewsItem = (TextView) v.findViewById(R.id.tvNewsItem);
		TextView tNewsDate = (TextView) v.findViewById(R.id.tvNewsDate);
		
		if(p != null) {
			if(tNewsItem != null) {
				tNewsItem.setText(p.getItem());
			}
			if(tNewsDate != null) {
				tNewsDate.setText(p.getDate().toGMTString());
			}
		}
		
		return v;
	}
}
