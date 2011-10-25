package net.animeimports.android;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.services.calendar.CalendarClient;
import com.google.api.services.calendar.CalendarRequestInitializer;
import com.google.api.services.calendar.CalendarUrl;
import com.google.api.services.calendar.model.CalendarEntry;
import com.google.api.services.calendar.model.CalendarFeed;
import com.google.api.services.calendar.model.EventEntry;
import com.google.api.services.calendar.model.EventFeed;
import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.DateTime;
import com.google.common.collect.Lists;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import net.animeimports.android.AIEventEntry;
import net.animeimports.android.AIEventEntry.EVENT_TYPE;
import net.animeimports.android.AIEventEntry.MTG_FORMAT;

public class AnimeImportsAppActivity extends ListActivity {
	private ArrayList<AIEventEntry> events = Lists.newArrayList();
	
	// GoogleCalendar
	private static Level LOGGING_LEVEL = Level.CONFIG;
	private static final String AUTH_TOKEN_TYPE = "cl";
	private static final String TAG = "CalendarSample";
	private static final int MENU_ADD = 0;
	private static final int MENU_ACCOUNTS = 1;
	private static final int CONTEXT_EDIT = 0;
	private static final int CONTEXT_DELETE = 1;
	private static final int REQUEST_AUTHENTICATE = 0;
	CalendarClient client;
	final HttpTransport transport = AndroidHttp.newCompatibleTransport();
	static final String PREF = TAG;
	static final String PREF_ACCOUNT_NAME = "accountName";
	static final String PREF_AUTH_TOKEN = "authToken";
	static final String PREF_GSESSIONID = "gsessionid";
	GoogleAccountManager accountManager;
	HttpRequestFactory requestFactory;
	SharedPreferences settings;
	String accountName;
	String authToken;
	CalendarAndroidRequestInitializer requestInitializer;
	
	// Menu related 
	private static final String STORE_INFO = "Store Info";
	private static final String UPCOMING_EVENTS = "Upcoming Events";
	private static final String UPDATES = "Updates";
	private static final String STORE = "Store";
	private static final TextView textViewStoreInfo = null;
	private static final TextView textViewUpcomingEvents = null;
	private static final TextView textViewUpdates = null;
	private static final TextView textViewStore = null;
	
	// Events / Lists
	private List<String> optionsLinks = Lists.newArrayList();
	private List<String> storeInfo = Lists.newArrayList();
	private final List<CalendarEntry> calendars = Lists.newArrayList();
	private AIEventAdapter aiEventAdapter;
	private static final int DAYS_IN_FUTURE = 14;
	
	// Store Info
	// TODO: figure out how to store these in R.string...
	private static String STRING_STORE_ADDRESS = "1305 Palmetto Ave  Suite C\nPacifica, CA 94044";
	private static String STRING_STORE_NUMBER = "(650) 488-7900";
	private static String STRING_STORE_EMAIL = "webmaster@animeimports.net";
	private static String STRING_STORE_HOURS = "1:00pm - 7:00pm everyday";
	private static String STRING_STORE_URL = "http://www.animeimports.net";
	
	public class CalendarAndroidRequestInitializer extends CalendarRequestInitializer {
		String authToken;
		
		public CalendarAndroidRequestInitializer() {
			super(transport);
			Log.i("DEBUG", "CalendarAndroidRequestInitializer, before setting authToken and GsessionId");
			authToken = settings.getString(PREF_AUTH_TOKEN, null);
			setGsessionid(settings.getString(PREF_GSESSIONID, null));
		}
		
		public void intercept(HttpRequest request) throws IOException {
			Log.i("DEBUG", "Intercept");
			super.intercept(request);
			request.getHeaders().setAuthorization(GoogleHeaders.getGoogleLoginValue(authToken));
		}

		public boolean handleResponse(HttpRequest request, HttpResponse response, boolean retrySupported) throws IOException {
			switch(response.getStatusCode()) {
				case 302:
					super.handleResponse(request, response, retrySupported);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString(PREF_GSESSIONID, getGsessionid());
					editor.commit();
					return true;
				case 401:
					accountManager.invalidateAuthToken(authToken);
					authToken = null;
					SharedPreferences.Editor editor2 = settings.edit();
					editor2.remove(PREF_AUTH_TOKEN);
					editor2.commit();
					return false;
			}
			return false;
		}
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);
        accountManager = new GoogleAccountManager(this);
        settings = this.getSharedPreferences(PREF, 0);
        requestInitializer = new CalendarAndroidRequestInitializer();
        client = new CalendarClient(requestInitializer.createRequestFactory());
        client.setPrettyPrint(true);
        client.setApplicationName("AnimeImportsCalendarApp");
        initializeArrays();
        loadMainMenu();
        
        //ArrayAdapter<AIEventEntry> songList = new ArrayAdapter<AIEventEntry>(this, R.layout.event, events);
		//setListAdapter(songList);
        
        //getListView().setTextFilterEnabled(true);
        //registerForContextMenu(getListView());
    }

    public void initializeArrays() {
    	if(optionsLinks.size() == 0) {
    		optionsLinks.add(UPDATES);
    		optionsLinks.add(UPCOMING_EVENTS);
    		optionsLinks.add(STORE);
    		optionsLinks.add(STORE_INFO);
    	}
    	
    	if(storeInfo.size() == 0) {
    		storeInfo.add("back");
    		storeInfo.add(STRING_STORE_ADDRESS);
    		storeInfo.add(STRING_STORE_NUMBER);
    		storeInfo.add(STRING_STORE_EMAIL);
    		storeInfo.add(STRING_STORE_HOURS);
    	}
    }
    
    void loadStoreInfo() {
    	ArrayAdapter<String> storeInfoAdapter = new ArrayAdapter<String>(this, R.layout.event_details, storeInfo);
    	setListAdapter(storeInfoAdapter);
    }
    
    void loadMainMenu() {
    	ArrayAdapter<String> options = new ArrayAdapter<String>(this, R.layout.main_menu_option, optionsLinks);
    	setListAdapter(options);
    	setContentView(R.layout.main);
    }
    
    void handleEventClick(int position) {
    	System.out.println("works, position is " + position);
		System.out.println("same position in aiEventAdapter is " + aiEventAdapter.getItems().get(position).getName());
		
		ArrayList<String> eventDetails = Lists.newArrayList();
		AIEventEntry event = aiEventAdapter.getItems().get(position);
		
		eventDetails.add(event.getName());
		eventDetails.add("Date: " + event.getDate() + ", " + event.getTime());
		eventDetails.add("Event Type: " + event.getEventType());
		eventDetails.add("MTG Event Type: " + event.getMtgEventType());
		eventDetails.add("Format: " + event.getMtgFormat());
		eventDetails.add("Summary: " + event.getSummary());
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.event_details, eventDetails);
    	setListAdapter(adapter);
    	setContentView(R.layout.regular_list);
    }
    
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	
    	if(l.getAdapter() == this.aiEventAdapter) {
    		handleEventClick(position);
    	}
    	else {
    	
	    	String text = (String)((TextView)v).getText();
	    	if(text.equals(UPDATES)) {
	    		
	    	}
	    	else if(text.equals(UPCOMING_EVENTS)) {
	    		getListView().setTextFilterEnabled(true);
	            registerForContextMenu(getListView());
	            executeRefreshCalendars();
	    	}
			else if(text.equals(STORE)) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(STRING_STORE_URL));
				startActivity(browserIntent);
			}
			else if(text.equals(STORE_INFO)) {
				Log.i("new", "inside STORE section");
				loadStoreInfo();
			}
			else if(text.equals(STRING_STORE_NUMBER)) {
				Log.i("DEBUG", "about to call");
				Intent phoneIntent = new Intent(Intent.ACTION_CALL);
				//phoneIntent.
				startActivity(phoneIntent);
			}
    	}
    }

    /**
     * Write a new authToken into preferences, set it for your requestInitializer
     * @param authToken
     */
    void setAuthToken(String authToken) {
    	SharedPreferences.Editor editor = settings.edit();
    	editor.putString(PREF_AUTH_TOKEN, authToken);
    	editor.commit();
    	requestInitializer.authToken = authToken;
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	Log.e("CHECK", "inside onActivityResult....");
    	/*switch(requestCode) {
    	case REQUEST_AUTHENTICATE:
    		if(resultCode == RESULT_OK) {
    			gotAccount();
    		}
    		else {
    			chooseAccount();
    		}
    		break;
    	}*/
    }
    
    void executeRefreshCalendars() {
    	Log.i("LOOK", "inside executeRefreshCalendars");
    	String[] calendarNames;
    	List<CalendarEntry> calendars = this.calendars;
    	calendars.clear();
    	events.clear();
    	
    	// Setting up UI
    	try {
    		//CalendarUrl url = new CalendarUrl(calendars.get(0).getEventFeedLink());
    		Calendar now = Calendar.getInstance();
    		
    		Date startDate = new Date();
    		startDate.setYear(now.get(Calendar.YEAR)-1900);
    		startDate.setMonth(now.get(Calendar.MONTH)-8);
    		startDate.setDate(now.get(Calendar.DATE)-18);
    		startDate.setHours(now.get(Calendar.HOUR));
    		startDate.setMinutes(now.get(Calendar.MINUTE));
    		startDate.setSeconds(now.get(Calendar.SECOND));
    		
    		Date endDate = new Date();
    		endDate.setYear(startDate.getYear());
    		endDate.setMonth(startDate.getMonth());
    		endDate.setDate(startDate.getDate() + DAYS_IN_FUTURE);
    		endDate.setHours(startDate.getHours());
    		endDate.setMinutes(startDate.getMinutes());
    		endDate.setSeconds(startDate.getSeconds());

    		CustomCalendarURL customUrl = CustomCalendarURL.getUrl();
    		customUrl.startMin = new DateTime(startDate);
    		customUrl.startMax = new DateTime(endDate);
   
        	CalendarUrl url = new CalendarUrl(customUrl.build());
        	System.out.println("CHECK: " + url.toString());
        	
        	// Throws an UnknownHostException if no connection to internet
    		EventFeed feed = client.eventFeed().list().execute(url);

    		for(EventEntry entry : feed.getEntries()) {
    			AIEventEntry e = new AIEventEntry();
    			System.out.println("* Event:" + entry.title);
    			
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
	        			System.out.println("When: " + entry.when.startTime);
    				}
    			}
    			
    			if (entry.summary != null) {
    				e.setSummary(entry.summary);
    			}
    			else {
    				e.setSummary("...");
    			}
    			
    			System.out.println("Summary:" + entry.summary);
    			System.out.println("\n");
    			e.setName(entry.title);
    			events.add(e);
    		}
    	}
    	catch(UnknownHostException e) {
    		System.out.println("Got it");
    		loadMainMenu();
    		return;
    	}
    	catch(IOException e) {
    		e.printStackTrace();
    	}
    	
    	this.aiEventAdapter = new AIEventAdapter(this, R.layout.row, events);
        setListAdapter(this.aiEventAdapter);
    }
    
    void handleException(Exception e) {
    	e.printStackTrace();
    	if(e instanceof HttpResponseException) {
    		Log.i("DEBUG", "HttpResponseException caught");
    		HttpResponse response = ((HttpResponseException) e).getResponse();
    		int statusCode = response.getStatusCode();
    		try {
    			response.ignore();
    		}
    		catch(IOException e1) {
    			System.out.println("cannot ignore");
    			e1.printStackTrace();
    		}
    		
    		if(statusCode == 401) {
    			Log.i("DEBUG", "statusCode 401, calling gotAccount?");
    			//gotAccount();
    			return;
    		}
    		try {
    			Log.e(TAG, response.parseAsString());
    		}
    		catch(IOException e2) {
    			System.out.println("cannot log response as a string");
    			e.printStackTrace();
    		}
    	}
    	Log.e(TAG, e.getMessage(), e);
    }
    
    /*private static CalendarUrl forRoot() {
    	return new CalendarUrl(CalendarUrl.ROOT_URL);
    }
    
    private static CalendarUrl forCalendarMetafeed() {
    	CalendarUrl result = forRoot();
    	result.getPathParts().add("default");
    	return result;
    }
    
    private static CalendarUrl forAllCalendarsFeed() {
    	CalendarUrl result = forCalendarMetafeed();
    	result.getPathParts().add("owncalendars");
    	result.getPathParts().add("full");
    	return result;
    }
    
    private static CalendarUrl forOwnCalendarsFeed() {
		CalendarUrl result = forCalendarMetafeed();
		result.getPathParts().add("owncalendars");
		result.getPathParts().add("full");
		return result;
    }*/
    
    public class AIEventAdapter extends ArrayAdapter<AIEventEntry> {
    	private ArrayList<AIEventEntry> items;
    	
    	/**
		 * @return the items
		 */
		public ArrayList<AIEventEntry> getItems() {
			return items;
		}

		public AIEventAdapter(Context context, int textViewResourceId, ArrayList<AIEventEntry> items) {
    		super(context, textViewResourceId, items);
    		this.items = items;
    	}
    	
    	public View getView(int position, View convertView, ViewGroup parent) {
    		View v = convertView;
    		if(v == null) {
    			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    			v = vi.inflate(R.layout.row, null);
    		}
    		AIEventEntry event = items.get(position);
    		if(event != null) {
    			TextView tt = (TextView) v.findViewById(R.id.toptext);
    			TextView bt = (TextView) v.findViewById(R.id.bottomtext);
    			ImageView icon = (ImageView) v.findViewById(R.id.icon);
    			icon.setImageResource(getIcon(event.getName()));
    			if(tt != null) {
    				tt.setText(event.getName());
    			}
    			if(bt != null) {
    				bt.setText("Date: " + event.getDate() + ", " + event.getTime());
    			}
    		}
    		return v;
    	}
    }
    
    /**
     * Returns the appropriate icon for the provided event
     * Draft: return the latest (at the time) set's Common symbol
     * FNM: return the latest (at the time) set's Uncommon symbol
     * Release/PreRelease/Gameday: return the latest (at the time) set's Rare symbol
     * GPT: return the latest (at the time) set's Mythic symbol
     * @param input
     * @return
     */
    private int getIcon(String input) {
    	int retVal = 0;
    	if(input.toLowerCase().contains("draft")) {
    		if(input.toLowerCase().contains("fnm")) {
        		retVal = R.drawable.icon_isd_uncommon;
        	}
    		else {
    			retVal = R.drawable.icon_isd_common;
    		}
    	}
    	
    	if(input.toLowerCase().contains("release") || input.toLowerCase().contains("prerelease") || 
    			input.toLowerCase().contains("game day") || input.toLowerCase().contains("launch")) {
    		retVal = R.drawable.icon_isd_rare;
    	}
    	
    	if(input.toLowerCase().contains("gpt") ||
    			input.toLowerCase().contains("grand prix")) {
    		retVal = R.drawable.icon_isd_mythic;
    	}
    	
    	if(input.toLowerCase().contains("edh")) {
    		if(input.toLowerCase().contains("free play")) {
    			retVal = R.drawable.icon_edh_common;
    		}
    		else {
    			retVal = R.drawable.icon_edh_uncommon;
    		}
    	}
    	
    	return retVal;
    }
}