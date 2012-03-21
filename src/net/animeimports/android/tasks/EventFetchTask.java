package net.animeimports.android.tasks;

import java.io.IOException;
import java.util.ArrayList;

import net.animeimports.android.AnimeImportsAppActivity.EventTaskListener;
import net.animeimports.calendar.AICalendarManager;
import net.animeimports.calendar.AIEventEntry;
import net.animeimports.calendar.CustomCalendarURL;
import net.animeimports.calendar.AICalendarManager.CalendarAndroidRequestInitializer;
import net.animeimports.calendar.AIEventEntry.EVENT_TYPE;
import net.animeimports.calendar.AIEventEntry.MTG_FORMAT;
import android.content.Context;
import android.os.AsyncTask;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarClient;
import com.google.api.services.calendar.CalendarUrl;
import com.google.api.services.calendar.model.EventEntry;
import com.google.api.services.calendar.model.EventFeed;

public class EventFetchTask extends AsyncTask<ArrayList<AIEventEntry>, Void, ArrayList<AIEventEntry>> {
	CalendarClient client;
	CalendarAndroidRequestInitializer requestInitializer;
	private AICalendarManager cManager = null;
	private boolean success = true;
	private Context mContext = null;
	private EventTaskListener listener = null;
	private ArrayList<AIEventEntry> events = null;
	
	public EventFetchTask(EventTaskListener l, Context c) {
		this.mContext = c;
		this.listener = l;
		cManager = AICalendarManager.getInstance(mContext);
		requestInitializer = cManager.getCalReqInitializer();
		client = new CalendarClient(requestInitializer.createRequestFactory());
	}
	
	@Override
	protected void onPreExecute() {
		listener.init();
	}
	
	@Override
	protected ArrayList<AIEventEntry> doInBackground(ArrayList<AIEventEntry>... arg0) {
		events = new ArrayList<AIEventEntry>();
    	
    	try {
    		CustomCalendarURL customUrl = CustomCalendarURL.getUrl();
    		customUrl.startMin = new DateTime(cManager.getStartDate());
    		customUrl.startMax = new DateTime(cManager.getEndDate());
        	CalendarUrl url = new CalendarUrl(customUrl.build());
    		EventFeed feed = client.eventFeed().list().execute(url);

    		for(EventEntry entry : feed.getEntries()) {
    			AIEventEntry e = new AIEventEntry();
    			if(entry.title.toUpperCase().contains("MTG")) {
    				e.setEventType(EVENT_TYPE.MTG);
    				if(entry.title.toUpperCase().contains("DRAFT")) {
    					e.setMtgFormat(MTG_FORMAT.DRAFT);
    				}
    			}
    			else if(entry.title.contains("Warhammer")) {
    				e.setEventType(EVENT_TYPE.WARHAMMER);
    			}
    			
    			if(entry.when != null) {
    				if(entry.when.startTime != null) {
	    				e.setDate(entry.when.startTime.toString().substring(0, 10));
	        			e.setTime(entry.when.startTime.toString().substring(11, 19));
    				}
    			}
    			
    			if (entry.summary != null) {
    				e.setSummary(entry.summary);
    			}
    			else {
    				e.setSummary("...");
    			}

    			e.setName(entry.title);
    			events.add(e);
    		}
    	}
    	catch(IOException e) {
    		if(listener != null)
    			listener.recover();
    		//e.printStackTrace();
    		success = false;
    	}
    	
    	return events;
	}
	
	@Override
	/**
	 * If we failed fetching, load from db. Otherwise load from memory
	 */
	protected void onPostExecute(ArrayList<AIEventEntry> result) {
		if(listener != null)
			listener.onComplete(success, result);
	}
}
