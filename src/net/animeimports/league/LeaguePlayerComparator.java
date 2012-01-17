package net.animeimports.league;

import java.util.Comparator;

import android.util.Log;

public class LeaguePlayerComparator implements Comparator<LeaguePlayer>{
	private boolean asc = false;
	
	// 1: sorting on name
	// 2: sorting on session points
	// 3: sorting on lifetime points
	private int field = 0;
	
	/**
	 * Create a new comparator for sorting based on either Lifetime points or Session points
	 * We sort by descending order by default, not sure if we need to provide asc
	 * @param points
	 */
	public LeaguePlayerComparator(int field) {
		this.field = field ;
	}
	
	/**
	 * Compare two objects in DESC order
	 */
	@Override
	public int compare(LeaguePlayer p1, LeaguePlayer p2) {
		switch(this.field) {
		case 1: // sort by name
			String p1name = p1.getPlayerName();
			String p2name = p2.getPlayerName();
			return (asc) ? p2name.compareToIgnoreCase(p1name) : p1name.compareToIgnoreCase(p2name);
		case 2: // sort by session points
			return (asc) ? p1.getPointsSession() - p2.getPointsSession() : p2.getPointsSession() - p1.getPointsSession();
		case 3: // sort by lifetime points
			return (asc) ? p1.getPointsLifetime() - p2.getPointsLifetime() : p2.getPointsLifetime() - p1.getPointsLifetime();
		default: // undefined comparison mode!
			Log.e("AI ERROR", "Tried to compare LeaguePlayers based on undefined field!");
			return 0;
		}
	}
}