package net.animeimports.android;

import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.Key;

public class CustomCalendarURL extends GoogleUrl {
	@Key("start-min")
	public DateTime startMin;
	
	@Key("start-max")
	public DateTime startMax;
	
	public CustomCalendarURL(String encodeUrl) {
		super(encodeUrl);
	}
	
	private static CustomCalendarURL root() {
		return new CustomCalendarURL("https://www.google.com/calendar/feeds/anime.imports.com%40gmail.com/public/full");
		//return new CustomCalendarURL("https://www.google.com/calendar/feeds/freeformsoftware%40gmail.com/public/full");
	}
	
	public static CustomCalendarURL getUrl() {
		CustomCalendarURL result = root();
		//result.pathParts.add("currentLocation"); DEPRECATED
		return result;
	}
}
