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
	
	// Store Info
	// TODO: figure out how to store these in R.string...
	private static String STRING_STORE_ADDRESS = "1305 Palmetto Ave  Suite C\nPacifica, CA 94044";
	private static String STRING_STORE_NUMBER = "(650) 488-7900";
	private static String STRING_STORE_EMAIL = "webmaster@animeimports.net";
	private static String STRING_STORE_HOURS = "1:00pm - 7:00pm everyday";
	private static String STRING_STORE_URL = "http://www.animeimports.net";
	
	private static int depth = 0;
	private static String currentMenu = "";
	protected ProgressDialog m_ProgressDialog = null;
	private ArrayList<AIEventEntry> events = null;
	private ImageView mainLogo = null;
	
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
        setContentView(R.layout.main);
        
        mainLogo = (ImageView) findViewById(R.id.imageView1);
       
        initializeApp();
        runOnUiThread(loadMainMenuThread);
    }

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
    		storeInfo.add(STRING_STORE_ADDRESS);
    		storeInfo.add(STRING_STORE_NUMBER);
    		storeInfo.add(STRING_STORE_EMAIL);
    		storeInfo.add(STRING_STORE_HOURS);
    	}
    }
    
    void loadStoreInfo() {
    	depth = 1;
    	currentMenu = STORE_INFO;
    	ArrayAdapter<String> storeInfoAdapter = new ArrayAdapter<String>(this, R.layout.row_event_details, storeInfo);
    	setListAdapter(storeInfoAdapter);
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
    
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	System.out.println("Depth is " + depth);
    	// If this is the list of Events
    	if(depth > 0) {
    			// If back button was clicked, figure out which level we load
    			if(position == 0) {
    				if(depth == 1) {
    					//loadMainMenu();
    					runOnUiThread(loadMainMenuThread);
    				}
    				else if(depth == 2) {
    		            executeRefreshCalendars();
    				}
    			}
    			// Figure out which level we're on; Upcoming Events List or Store Info
    			else if(depth == 1) {
    				if(currentMenu.equals(UPCOMING_EVENTS)) {
    					handleEventClick(position);
    				}
    			}
    			// If you clicked on an Event's details, do nothing
    			else {
    				System.out.println("Not doing anything");
    			}
    	}
    	// Otherwise this is the Main Menu
    	else {
	    	String text = (String)((TextView)v).getText();
	    	System.out.println("Depth is 0, text is " + text);
	    	if(text.equals(UPDATES)) {
	    		
	    	}
	    	else if(text.equals(UPCOMING_EVENTS)) {
	    		// OLD PLACE
	    		Runnable eventFetchThread = new Runnable() {
	    			@Override
	    			public void run() {
	    				executeRefreshCalendars();
	    			}
	    		};
	    		Thread thread = new Thread(null, eventFetchThread, "MagentoBackground");
	    		thread.start();
	    		m_ProgressDialog = ProgressDialog.show(AnimeImportsAppActivity.this, "Please wait...", "Retrieving data...", true);
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
    	depth = 1;
    	currentMenu = UPCOMING_EVENTS;
    	
    	// TODO: figure out caching
    	events = new ArrayList<AIEventEntry>();
    	
    	// Setting up UI
    	try {
    		Calendar now = Calendar.getInstance();
    		
    		Date startDate = new Date();
    		startDate.setYear(now.get(Calendar.YEAR)-1900);
    		startDate.setMonth(now.get(Calendar.MONTH)-8);
    		startDate.setDate(now.get(Calendar.DATE)-10);
    		startDate.setHours(now.get(Calendar.HOUR));
    		startDate.setMinutes(now.get(Calendar.MINUTE));
    		startDate.setSeconds(now.get(Calendar.SECOND));
    		
    		Date endDate = new Date();
    		endDate.setYear(startDate.getYear());
    		endDate.setMonth(startDate.getMonth());
    		endDate.setDate(startDate.getDate() + (2*DAYS_IN_FUTURE));
    		endDate.setHours(startDate.getHours());
    		endDate.setMinutes(startDate.getMinutes());
    		endDate.setSeconds(startDate.getSeconds());

    		CustomCalendarURL customUrl = CustomCalendarURL.getUrl();
    		customUrl.startMin = new DateTime(startDate);
    		customUrl.startMax = new DateTime(endDate);
   
        	CalendarUrl url = new CalendarUrl(customUrl.build());
        	
        	// Throws an UnknownHostException if no connection to internet
        	// Throws a SocketTimeoutException 
    		EventFeed feed = client.eventFeed().list().execute(url);

    		for(EventEntry entry : feed.getEntries()) {
    			AIEventEntry e = new AIEventEntry();
    			//System.out.println("* Event:" + entry.title);
    			
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
	        			//System.out.println("When: " + entry.when.startTime);
    				}
    			}
    			
    			if (entry.summary != null) {
    				e.setSummary(entry.summary);
    			}
    			else {
    				e.setSummary("...");
    			}
    			
    			//System.out.println("Summary:" + entry.summary + "\n");
    			e.setName(entry.title);
    			events.add(e);
    		}
    	}
    	catch(UnknownHostException e) {
    		System.out.println("UnknownHostException, make sure you're connected to the internet");
    		//TODO
    		runOnUiThread(loadMainMenuThread);
    		runOnUiThread(toastThread);
    		return;
    	}
    	catch(SocketTimeoutException e) {
    		System.out.println("Server is taking longer than expected to respond, please try again");
    		runOnUiThread(loadMainMenuThread);
    	}
    	catch(IOException e) {
    		System.out.println("IOException yo");
    		e.printStackTrace();
    	}
    	
    	try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	runOnUiThread(returnRes);
    }
    
    private Runnable loadMainMenuThread = new Runnable() {
    	@Override
    	public void run() {
    		depth = 0;
    		
    		if(m_ProgressDialog != null)
    			m_ProgressDialog.dismiss();
        	currentMenu = "";
        	ArrayAdapter<String> options = new ArrayAdapter<String>(AnimeImportsAppActivity.this, R.layout.row_main_menu, optionsLinks);
        	setListAdapter(options);
        	//setContentView(R.layout.main);
        	mainLogo.setVisibility(View.VISIBLE);
    	}
    };
    
    private Runnable toastThread = new Runnable() {
    	@Override
    	public void run() {
    		Context context = getApplicationContext();
    		CharSequence text = "Hello!";
    		Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
    		toast.show();
    	}
    };
    
    /**
     * Return thread, called after executeRefreshCalendars has already filled our events array with events
     */
    private Runnable returnRes = new Runnable() {
    	@Override
    	public void run() {
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
	    			back.setName("Back");
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
	    				bt.setText("Date: " + event.getDate() + ", " + event.getTime());
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