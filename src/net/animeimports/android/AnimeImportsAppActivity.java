package net.animeimports.android;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.api.services.calendar.CalendarClient;
import com.google.api.services.calendar.CalendarRequestInitializer;
import com.google.api.services.calendar.CalendarUrl;
import com.google.api.services.calendar.model.EventEntry;
import com.google.api.services.calendar.model.EventFeed;
import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.DateTime;
import com.google.common.collect.Lists;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import net.animeimports.android.AIEventEntry;
import net.animeimports.android.AIEventEntry.EVENT_TYPE;
import net.animeimports.android.AIEventEntry.MTG_FORMAT;

public class AnimeImportsAppActivity extends ListActivity {
	
	// GoogleCalendar
	private static final String TAG = "CalendarSample";
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
	
	// Events / Lists
	private List<String> optionsLinks = Lists.newArrayList();
	private List<String> storeInfo = Lists.newArrayList();
	private AIEventAdapter aiEventAdapter;
	private static final int DAYS_IN_FUTURE = 14;
	
	private static int depth = 0;
	private static String currentMenu = "";
	protected ProgressDialog m_ProgressDialog = null;
	private ArrayList<AIEventEntry> events = null;
	private ImageView mainLogo = null;
	
	/**
	 * Old code from GoogleCalendar Api examples, not entirely sure this is being used... 
	 * TODO: flag for delete
	 */
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
	
    /**
     *  Called when the activity is first created. 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mainLogo = (ImageView) findViewById(R.id.imageView1);
        initializeApp();
        runOnUiThread(loadMainMenuThread);
    }

    /**
     * Initialize arrays and data members
     */
    public void initializeApp() {
    	events = new ArrayList<AIEventEntry>();
		accountManager = new GoogleAccountManager(this);
	    settings = this.getSharedPreferences(PREF, 0);
	    requestInitializer = new CalendarAndroidRequestInitializer();
	    client = new CalendarClient(requestInitializer.createRequestFactory());
	    client.setPrettyPrint(true);
	    client.setApplicationName("AnimeImportsCalendarApp");
	    
    	if(optionsLinks.size() == 0) {
    		optionsLinks.add(UPDATES);
    		optionsLinks.add(UPCOMING_EVENTS);
    		optionsLinks.add(STORE);
    		optionsLinks.add(STORE_INFO);
    	}
    	
    	if(storeInfo.size() == 0) {
    		storeInfo.add("back");
    		storeInfo.add(this.getString(R.string.store_address));
    		storeInfo.add(this.getString(R.string.store_number));
    		storeInfo.add(this.getString(R.string.store_email));
    		storeInfo.add(this.getString(R.string.store_hours));
    	}
    }
    
    /**
     * Populate the array adapter, hide the main logo
     */
    void loadStoreInfo() {
    	depth = 1;
    	currentMenu = STORE_INFO;
    	ArrayAdapter<String> storeInfoAdapter = new ArrayAdapter<String>(this, R.layout.row_event_details, storeInfo);
    	setListAdapter(storeInfoAdapter);
    	mainLogo.setVisibility(View.GONE);
    }
    
    /**
     * Loads all details about a particular event, called when an Event is clicked
     * @param position
     */
    void handleEventClick(int position) {
    	depth = 2;		
    	currentMenu = UPCOMING_EVENTS;
		ArrayList<String> eventDetails = Lists.newArrayList();
		AIEventEntry event = aiEventAdapter.getItems().get(position);
		
		eventDetails.add("back");
		eventDetails.add(event.getName());
		eventDetails.add("Date: " + event.getDate() + ", " + event.getTime());
		eventDetails.add("Event Type: " + event.getEventType());
		eventDetails.add("MTG Event Type: " + event.getMtgEventType());
		eventDetails.add("Format: " + event.getMtgFormat());
		eventDetails.add("Summary: " + event.getSummary());
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_event_details, eventDetails);
    	setListAdapter(adapter);
    }
    
    /**
     * Handles all clicks for any menu depth using depth and position
     */
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	System.out.println("Depth is " + depth);
    	// If this is not the main menu
    	if(depth > 0) {
    			// If back button was clicked, figure out which level we load
    			if(position == 0) {
    				if(depth == 1) {
    					runOnUiThread(loadMainMenuThread);
    				}
    				else if(depth == 2) {
    					getEvents();
    				}
    			}
    			else if(position == 2) {
					Log.i("DEBUG", "about to call");
					Intent phoneIntent = new Intent(Intent.ACTION_CALL);
					phoneIntent.setData(Uri.parse("tel:" + this.getString(R.string.store_number)));
					startActivity(phoneIntent);
    			}
    			// Figure out which level we're on; Upcoming Events List or Store Info
    			else if(depth == 1) {
    				if(currentMenu.equals(UPCOMING_EVENTS)) {
    					handleEventClick(position);
    				}
    			}
    			else {
    				// Event's details, not implemented yet
    				Log.i("AI DEBUG", "Not implemented yet");
    			}
    	}
    	// Otherwise this is the Main Menu
    	else {
	    	String text = (String)((TextView)v).getText();
	    	if(text.equals(UPDATES)) {
	    		
	    	}
	    	else if(text.equals(UPCOMING_EVENTS)) {
	    		getEvents();
	    	}
			else if(text.equals(STORE)) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(this.getString(R.string.store_url)));
				startActivity(browserIntent);
			}
			else if(text.equals(STORE_INFO)) {
				loadStoreInfo();
			}
    	}
    }
   
    /**
     * Calls the eventFetchThread to query GoogleCalendar for events and loads a ProgressDialog
     */
    void getEvents() {
		Thread thread = new Thread(null, eventFetchThread, "MagentoBackground");
		thread.start();
		m_ProgressDialog = ProgressDialog.show(AnimeImportsAppActivity.this, "Please wait...", "Retrieving data...", true);
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
    
    private Date getStartDate() {
    	Calendar now = Calendar.getInstance();
    	Date startDate = new Date();
		startDate.setYear(now.get(Calendar.YEAR)-1900);
		startDate.setMonth(now.get(Calendar.MONTH));
		startDate.setDate(now.get(Calendar.DATE));
		startDate.setHours(now.get(Calendar.HOUR));
		startDate.setMinutes(now.get(Calendar.MINUTE));
		startDate.setSeconds(now.get(Calendar.SECOND));
		return startDate;
    }
    
    private Date getEndDate() {
    	Calendar now = Calendar.getInstance();
		Date endDate = new Date();
		endDate.setYear(now.get(Calendar.YEAR)-1900);
		endDate.setMonth(now.get(Calendar.MONTH));
		endDate.setDate(now.get(Calendar.DATE) + DAYS_IN_FUTURE);
		endDate.setHours(now.get(Calendar.HOUR));
		endDate.setMinutes(now.get(Calendar.MINUTE));
		endDate.setSeconds(now.get(Calendar.SECOND));
		return endDate;
    }
    
    private Runnable eventFetchThread = new Runnable() {
		@Override
		public void run() {
		
	    	depth = 1;
	    	currentMenu = UPCOMING_EVENTS;
	    	
	    	// TODO: figure out caching
	    	events = new ArrayList<AIEventEntry>();
	    	
	    	try {
	    		CustomCalendarURL customUrl = CustomCalendarURL.getUrl();
	    		customUrl.startMin = new DateTime(getStartDate());
	    		customUrl.startMax = new DateTime(getEndDate());
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
	    	catch(UnknownHostException e) {
	    		runOnUiThread(recoverThread);
	    		runOnUiThread(loadMainMenuThread);
	    		return;
	    	}
	    	catch(SocketTimeoutException e) {
	    		runOnUiThread(recoverThread);
	    		runOnUiThread(loadMainMenuThread);
	    	}
	    	catch(IOException e) {
	    		System.out.println("IOException yo");
	    		e.printStackTrace();
	    	}
	    	
	    	runOnUiThread(loadEventsThread);
		}
	};
    
	/**
	 * Loads the main menu, called when we have an UnknownHostException or SocketTimeoutException
	 */
    private Runnable loadMainMenuThread = new Runnable() {
    	@Override
    	public void run() {
    		depth = 0;
    		if(m_ProgressDialog != null)
    			m_ProgressDialog.dismiss();
        	currentMenu = "";
        	ArrayAdapter<String> options = new ArrayAdapter<String>(AnimeImportsAppActivity.this, R.layout.row_main_menu, optionsLinks);
        	setListAdapter(options);
        	mainLogo.setVisibility(View.VISIBLE);
    	}
    };
    
    /**
     * Called when we encounter an exception; alert the user and load the main menu
     */
    private Runnable recoverThread = new Runnable() {
    	@Override
    	public void run() {
    		Context context = getApplicationContext();
    		CharSequence text = "No internet connection or reception, try again later";
    		Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
    		toast.show();
    	}
    };
    
    /**
     * Called after eventFetchThread has already filled our events array with events
     */
    private Runnable loadEventsThread = new Runnable() {
    	@Override
    	public void run() {
    		mainLogo.setVisibility(View.GONE);
    		aiEventAdapter = new AIEventAdapter(AnimeImportsAppActivity.this, R.layout.row_event, events);
            setListAdapter(aiEventAdapter);
    		m_ProgressDialog.dismiss();
    		aiEventAdapter.notifyDataSetChanged();
    	}
    };
   
    public class AIEventAdapter extends ArrayAdapter<AIEventEntry> {
    	private ArrayList<AIEventEntry> items;
    	
		public ArrayList<AIEventEntry> getItems() {
			return items;
		}

		public AIEventAdapter(Context context, int textViewResourceId, ArrayList<AIEventEntry> items) {
    		super(context, textViewResourceId, items);
    		if(items.size() != 0) {
	    		this.items = items;
	    		if(!items.get(0).getName().equals("back")) {
	    			AIEventEntry back = new AIEventEntry();
	    			back.setName("back");
	    			this.items.add(0, back);
	    		}
    		}
    	}
    	
    	public View getView(int position, View convertView, ViewGroup parent) {
    		View v = convertView;
    		if(v == null) {
    			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    			v = vi.inflate(R.layout.row_event, null);
    		}
    		AIEventEntry event = items.get(position);
    		TextView tt = (TextView) v.findViewById(R.id.toptext);
			TextView bt = (TextView) v.findViewById(R.id.bottomtext);
			ImageView icon = (ImageView) v.findViewById(R.id.icon);
			
    		if(event != null) {
    			if(event.getName().equals("Back")) {
    				icon.setVisibility(icon.GONE);
    				if(tt != null) {
	    				tt.setText(event.getName());
	    			}
	    			if(bt != null) {
	    				bt.setVisibility(bt.GONE);
	    			}
    			}
    			else {
    				icon.setImageResource(getIcon(event.getName()));
	    			if(tt != null) {
	    				tt.setText(event.getName());
	    			}
	    			if(bt != null) {
	    				bt.setText(event.getDate() + ", " + event.getTime());
	    			}
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
    	int retVal = R.drawable.icon;
    	if(input.toLowerCase().contains("draft")) {
    		retVal = R.drawable.icon_draft;
    	}
    	else if(input.toLowerCase().contains("hg")) {
    		retVal = R.drawable.icon_2hg;
    	}
    	else if(input.toLowerCase().contains("sealed")) {
    		retVal = R.drawable.icon_sealed;
    	}
    	else if(input.toLowerCase().contains("standard")) {
    		retVal = R.drawable.icon_standard;
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
