package net.animeimports.android;

import java.net.URL;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarFeed;

import com.google.gdata.client.*;
import com.google.gdata.client.calendar.*;
import com.google.gdata.data.*;
import com.google.gdata.data.acl.*;
import com.google.gdata.data.calendar.*;
import com.google.gdata.data.extensions.*;
import com.google.gdata.util.*;
import com.google.common.collect.*;

public class AnimeImportsAppActivity extends Activity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.i("AI", "Inside!");
        
        CalendarService myService = new CalendarService("something?");
        try {
        	Log.i("AI", "Before credentials");
			//myService.setUserCredentials("username", "password");
			//URL feedUrl = new URL("https://www.google.com/calendar/feeds/default/allcalendars/full");
	        //CalendarFeed resultFeed = myService.getFeed(feedUrl, CalendarFeed.class);
	        /*System.out.println("Your calendars:\n");
	        for(int i = 0; i < resultFeed.getEntries().size(); i++) {
	        	CalendarEntry entry = resultFeed.getEntries().get(i);
	        	System.out.println("\t" + entry.getTitle().getPlainText());
	        }*/
		} 
        catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        setContentView(R.layout.main);
    }
}