package net.animeimports.calendar;

public class AIEventEntry {
	private String name;
	private String date;
	private String time;
	private EVENT_TYPE eventType;
	private MTG_FORMAT mtgFormat;
	private MTG_EVENT_TYPE mtgEventType;
	private String summary;
	
	public enum EVENT_TYPE {
		MTG(0), 
		WARHAMMER(1),
		UNKNOWN(-1);
		private int value;
		private EVENT_TYPE(int value) {
			this.value = value;
		}
		public static EVENT_TYPE getValue(int n) {
			switch(n) {
			case 0:
				return EVENT_TYPE.MTG;
			case 1:
				return EVENT_TYPE.WARHAMMER;
			default: return null;
			}
		}
		public int getIntValue() {
			return value;
		}
	}
	
	public enum MTG_FORMAT {
		DRAFT(0), 
		SEALED(1), 
		MINIMASTER(2), 
		STANDARD(3), 
		EXTENDED(4), 
		MODERN(5), 
		LEGACY(6), 
		VINTAGE(7),
		UNKNOWN(-1);
		private int value;
		private MTG_FORMAT(int n) {
			this.value = n;
		}
		public static MTG_FORMAT getValue(int n) {
			switch(n) {
			case 0:	return MTG_FORMAT.DRAFT;
			case 1: return MTG_FORMAT.SEALED;
			case 2: return MTG_FORMAT.MINIMASTER;
			case 3: return MTG_FORMAT.STANDARD;
			case 4: return MTG_FORMAT.EXTENDED;
			case 5: return MTG_FORMAT.MODERN;
			case 6: return MTG_FORMAT.LEGACY;
			case 7: return MTG_FORMAT.VINTAGE;
			default: return null;
			}
		}
		public int getIntValue() {
			return value;
		}
	}
	
	public enum MTG_EVENT_TYPE {
		PRERELEASE(0), 
		RELEASE(1), 
		GPT(2), 
		GAMEDAY(3),
		UNKNOWN(-1);
		private int value;
		private MTG_EVENT_TYPE(int n) {
			this.value = n;
		}
		public static MTG_EVENT_TYPE getValue(int n) {
			switch(n) {
			case 0: return MTG_EVENT_TYPE.PRERELEASE;
			case 1: return MTG_EVENT_TYPE.RELEASE;
			case 2: return MTG_EVENT_TYPE.GPT;
			case 3: return MTG_EVENT_TYPE.GAMEDAY;
			default: return null;
			}
		}
		public int getIntValue() {
			return value;
		}
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
	
	/**
	 * Overridden method for ArrayAdapter display. If you want to modify what we show in our custom 
	 * AIEventAdapter, play with this
	 */
	public String toString() {
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
