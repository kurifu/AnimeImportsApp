package net.animeimports.android.tasks;

import java.util.ArrayList;

import net.animeimports.android.AnimeImportsAppActivity.LeagueTaskListener;
import net.animeimports.android.DataManager;
import net.animeimports.league.LeaguePlayer;
import android.content.Context;
import android.os.AsyncTask;

public class StoreLeagueTask extends AsyncTask<ArrayList<LeaguePlayer>, Void, ArrayList<LeaguePlayer>> {
	private DataManager dm = null;
	private Context mContext = null;
	private ArrayList<LeaguePlayer> leagueStats = null;
	private LeagueTaskListener listener = null;
	
	public StoreLeagueTask(ArrayList<LeaguePlayer> stats, LeagueTaskListener l, Context c) {
		this.mContext = c;
		this.leagueStats = stats;
		this.listener = l;
		dm = DataManager.getInstance(mContext);
	}
	
	@Override
	protected ArrayList<LeaguePlayer> doInBackground(ArrayList<LeaguePlayer>... arg0) {
		dm.deleteAllLeague();
		
		for (LeaguePlayer p : leagueStats) {
			dm.insertLeague(p.getPlayerName(), p.getPointsSession(), p.getPointsLifetime());
		}
		return leagueStats;
	}
	
	/**
	 * Notify the listener that we are ready to load our stats into the main activity. Note that we pass in null because we have already
	 * loaded the data into main memory with either the LeagueFetchTask or LeagueFetchDbTask, which were run prior to this task
	 */
	@Override
	protected void onPostExecute(ArrayList<LeaguePlayer> result) {
		if(listener != null)
			listener.onDbComplete(null);
	}
};