package net.animeimports.android.tasks;

import java.util.ArrayList;

import net.animeimports.android.AnimeImportsAppActivity.LeagueTaskListener;
import net.animeimports.android.DataManager;
import net.animeimports.league.LeaguePlayer;
import android.content.Context;
import android.os.AsyncTask;

public class LeagueFetchDbTask extends AsyncTask<ArrayList<LeaguePlayer>, Void, ArrayList<LeaguePlayer>>{
	private LeagueTaskListener listener = null;
	private Context mContext = null;
	private DataManager dm = null;
	private ArrayList<LeaguePlayer> leagueStats = null;
	
	public LeagueFetchDbTask(LeagueTaskListener l, Context c) {
		this.listener = l;
		this.mContext = c;
		dm = DataManager.getInstance(mContext);
	}
	
	@Override 
	protected void onPreExecute() {
		leagueStats = new ArrayList<LeaguePlayer>();
	}
	
	@Override
	protected ArrayList<LeaguePlayer> doInBackground(ArrayList<LeaguePlayer> ... arg0) {
		leagueStats = dm.selectAllLeague();
		return leagueStats;
	}
	
	@Override
	protected void onPostExecute(ArrayList<LeaguePlayer> result) {
		if(listener != null)
			listener.onDbComplete(result);
	}
}