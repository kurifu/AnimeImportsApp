package net.animeimports.android;

import java.util.ArrayList;

import net.animeimports.league.LeaguePlayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AILeagueAdapter extends ArrayAdapter<LeaguePlayer> {
	private ArrayList<LeaguePlayer> items;
	private Context mContext = null;

	public ArrayList<LeaguePlayer> getItems() {
		return items;
	}
	
	public AILeagueAdapter(Context context, int textViewResourceId, ArrayList<LeaguePlayer> items) {
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
			v = vi.inflate(R.layout.row_league, null);
		}
		LeaguePlayer p = items.get(position);
		TextView tName = (TextView) v.findViewById(R.id.tvName);
		TextView tSession = (TextView) v.findViewById(R.id.tvSession);
		TextView tLifetime = (TextView) v.findViewById(R.id.tvLifetime);
		
		if(p != null) {
			if(tName != null) {
				tName.setText(p.getPlayerName());
			}
			if(tSession != null) {
				tSession.setText(Integer.toString(p.getPointsSession()));
			}
			if(tLifetime != null) {
				tLifetime.setText(Integer.toString(p.getPointsLifetime()));
			}
		}
		
		return v;
	}
}
