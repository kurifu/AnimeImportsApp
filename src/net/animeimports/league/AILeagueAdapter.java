package net.animeimports.league;

import java.util.ArrayList;

import net.animeimports.android.R;
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
	private int statMode = 0;
	private final int DATA_SESSION = 1;
	private final int DATA_LIFETIME = 2;
	
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
		TextView tData = (TextView) v.findViewById(R.id.tvData);
		
		if(p != null) {
			if(tName != null) {
				tName.setText(p.getPlayerName());
			}
			if(tData != null) {
				if(statMode == DATA_SESSION) {
					tData.setText(Integer.toString(p.getPointsSession()));
				}
				else if(statMode == DATA_LIFETIME) {
					tData.setText(Integer.toString(p.getPointsLifetime()));
				}
			}
		}
		return v;
	}
	
	/**
	 * @return the statMode
	 */
	public int getStatMode() {
		return statMode;
	}

	/**
	 * @param statMode the statMode to set
	 */
	public void setStatMode(int statMode) {
		this.statMode = statMode;
	}
}
