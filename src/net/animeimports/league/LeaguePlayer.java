package net.animeimports.league;

public class LeaguePlayer {
	private String playerName = "";
	private int pointsLifetime = 0;
	private int pointsSession = 0;
	private int wins = 0;
	private int losses = 0;
	
	
	/**
	 * @return the playerName
	 */
	public String getPlayerName() {
		return playerName;
	}
	/**
	 * @param playerName the playerName to set
	 */
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	/**
	 * @return the pointsLifetime
	 */
	public int getPointsLifetime() {
		return pointsLifetime;
	}
	/**
	 * @param pointsLifetime the pointsLifetime to set
	 */
	public void setPointsLifetime(int pointsLifetime) {
		this.pointsLifetime = pointsLifetime;
	}
	/**
	 * @return the pointsSession
	 */
	public int getPointsSession() {
		return pointsSession;
	}
	/**
	 * @param pointsSession the pointsSession to set
	 */
	public void setPointsSession(int pointsSession) {
		this.pointsSession = pointsSession;
	}
	/**
	 * @return the wins
	 */
	public int getWins() {
		return wins;
	}
	/**
	 * @param wins the wins to set
	 */
	public void setWins(int wins) {
		this.wins = wins;
	}
	/**
	 * @return the losses
	 */
	public int getLosses() {
		return losses;
	}
	/**
	 * @param losses the losses to set
	 */
	public void setLosses(int losses) {
		this.losses = losses;
	}
}
