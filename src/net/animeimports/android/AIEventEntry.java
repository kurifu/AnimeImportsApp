package net.animeimports.android;

public class AIEventEntry {
	private String name;
	private String date;
	private String time;
	private EVENT_TYPE eventType;
	private MTG_FORMAT mtgFormat;
	private MTG_EVENT_TYPE mtgEventType;
	private String summary;
	
	public enum EVENT_TYPE {
		MTG, WARHAMMER 
	}
	
	public enum MTG_FORMAT {
		DRAFT, SEALED, MINIMASTER, STANDARD, EXTENDED, MODERN, LEGACY, VINTAGE   
	}
	
	public enum MTG_EVENT_TYPE {
		PRERELEASE, RELEASE, GPT, GAMEDAY
	}
	
	/**
	 * @return the summary
	 */
	public String getSummary() {
		return summary;
	}

	/**
	 * @param summary the summary to set
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	/**
	 * @return the eventType
	 */
	public EVENT_TYPE getEventType() {
		return eventType;
	}

	/**
	 * @param eventType the eventType to set
	 */
	public void setEventType(EVENT_TYPE eventType) {
		this.eventType = eventType;
	}

	/**
	 * @return the mtgFormat
	 */
	public MTG_FORMAT getMtgFormat() {
		return mtgFormat;
	}

	/**
	 * @param mtgFormat the mtgFormat to set
	 */
	public void setMtgFormat(MTG_FORMAT mtgFormat) {
		this.mtgFormat = mtgFormat;
	}

	/**
	 * @return the mtgEventType
	 */
	public MTG_EVENT_TYPE getMtgEventType() {
		return mtgEventType;
	}

	/**
	 * @param mtgEventType the mtgEventType to set
	 */
	public void setMtgEventType(MTG_EVENT_TYPE mtgEventType) {
		this.mtgEventType = mtgEventType;
	}
	
	public String toString() {
		//System.out.println("CHECK: date: " + date + ", time: " + time + ", name: " + name);
		return date + ", " + time + "\t" + name;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}
	/**
	 * @param date the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}
	/**
	 * @return the time
	 */
	public String getTime() {
		return time;
	}
	/**
	 * @param time the time to set
	 */
	public void setTime(String time) {
		this.time = time;
	}
	
}
