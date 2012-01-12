package net.animeimports.league;

import java.util.Comparator;

public class LeaguePlayerComparator implements Comparator<LeaguePlayer>{
	private boolean asc = false;
	
	// 0: sorting on lifetime points
	// 1: sorting on session points
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
		int p1points = (field == 0) ? p1.getPointsLifetime() : p1.getPointsSession();
		int p2points = (field == 0) ? p2.getPointsLifetime() : p2.getPointsSession();
		return (asc) ? p1points - p2points : p2points - p1points;
	}
}