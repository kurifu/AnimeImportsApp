package net.animeimports.android;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

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
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import net.animeimports.calendar.AIEventAdapter;
import net.animeimports.calendar.AIEventEntry;
import net.animeimports.calendar.AIEventEntry.EVENT_TYPE;
import net.animeimports.calendar.AIEventEntry.MTG_FORMAT;
import net.animeimports.calendar.CustomCalendarURL;
import net.animeimports.league.AILeagueAdapter;
import net.animeimports.league.LeaguePlayer;
import net.animeimports.league.LeaguePlayerComparator;
import net.animeimports.league.XmlParser;

public class AnimeImportsAppActivity extends ListActivity {
	
	// GoogleCalendar
	private static final String TAG = "CalendarSample";
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
	CalendarClient client;
	CalendarAndroidRequestInitializer requestInitializer;
	
	// Events / Lists
	private List<String> storeInfo = Lists.newArrayList();
	private AIEventAdapter aiEventAdapter;
	private static final int DAYS_IN_FUTURE = 14;
	
	private int currMenu = 0;
	private final int NEWS = 1;
	private final int EVENTS = 2;
	private final int EVENT_DETAILS = 3;
	private final int INFO = 4;
	private final int LEAGUE_LIFETIME = 6;
	
	protected ProgressDialog mProgressDialog = null;
	private static ArrayList<AIEventEntry> events = null;
	private static ArrayList<LeaguePlayer> leagueStats = null;
	private static ArrayList<String> updates = null;
	
	private static final String URL_LEAGUE = "http://animeimports.net/league/MTGILEAGUE.xml";
	
	private int leagueSort = 0;
	private final int SORT_NAME = 1;
	private final int SORT_SESSION = 2;
	private final int SORT_LIFETIME = 3;
	
	ImageView imgInfo = null;
	ImageView imgLeague = null;
	ImageView imgEvents = null;
	ImageView imgNews = null;
	LinearLayout leagueHeader = null;
	TextView tvNameHeader = null;
	TextView tvSessionHeader = null;
	TextView tvLifetimeHeader = null;
	
	private DataManager dm = null;
	private Time lastLeagueFetch = null;
	
	/**
	 * Old code from GoogleCalendar Api examples, not entirely sure this is being used... 
	 * TODO: flag for delete
	 */
 	public class CalendarAndroidRequestInitializer extends CalendarRequestInitializer {
		String authToken;
		
		public CalendarAndroidRequestInitializer() {
			super(transport);
			authToken = settings.getString(PREF_AUTH_TOKEN, null);
			setGsessionid(settings.getString(PREF_GSESSIONID, null));
		}
		
		public void intercept(HttpRequest request) throws IOException {
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
        initializeApp();
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
	    dm = DataManager.getInstance(this);
	    
    	imgInfo = (ImageView) findViewById(R.id.imgInfo);
    	imgLeague = (ImageView) findViewById(R.id.imgLeague);
    	imgEvents = (ImageView) findViewById(R.id.imgEvents);
    	imgNews = (ImageView) findViewById(R.id.imgNews);
    	leagueHeader = (LinearLayout) findViewById(R.id.llLeagueHead);
    	tvNameHeader = (TextView) findViewById(R.id.tvNameHeader);
    	tvSessionHeader = (TextView) findViewById(R.id.tvSessionHeader);
    	tvLifetimeHeader = (TextView) findViewById(R.id.tvLifetimeHeader);
    	
    	currMenu = NEWS;
    	swapIcons();
    	loadNews();
    	
    	imgNews.setOnClickListener(new OnClickListener() {
    	    public void onClick(View v) {
    	    	currMenu = NEWS;
    	    	swapIcons();
    	    	toggleLeagHeader();
    	    	loadNews();
    	    }
    	});
    	imgEvents.setOnClickListener(new OnClickListener() {
    	    public void onClick(View v) {
    	    	currMenu = EVENTS;
    	    	swapIcons();
    	    	getEvents();
    	    }
    	});
    	imgInfo.setOnClickListener(new OnClickListener() {
    	    public void onClick(View v) {
    	    	currMenu = INFO;
    	    	swapIcons();
    	    	toggleLeagHeader();
    	    	loadStoreInfo();
    	    }
    	});
    	imgLeague.setOnClickListener(new OnClickListener() {
    	    public void onClick(View v) {
    	    	currMenu = LEAGUE_LIFETIME;
    	    	swapIcons();
    	    	getLeague();
    	    }
    	});
    	tvNameHeader.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			if(leagueStats != null) {
    				Collections.sort(leagueStats, new LeaguePlayerComparator(1));
    				AILeagueAdapter adapter = new AILeagueAdapter(AnimeImportsAppActivity.this, R.layout.row_league, leagueStats);
    				setListAdapter(adapter);
    				adapter.notifyDataSetChanged();
    			}
    		}
    	});
    	tvSessionHeader.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			if(leagueStats != null) {
    				Collections.sort(leagueStats, new LeaguePlayerComparator(2));
    				AILeagueAdapter adapter = new AILeagueAdapter(AnimeImportsAppActivity.this, R.layout.row_league, leagueStats);
    				setListAdapter(adapter);
    				adapter.notifyDataSetChanged();
    			}
    		}
    	});
    	tvLifetimeHeader.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			if(leagueStats != null) {
    				Collections.sort(leagueStats, new LeaguePlayerComparator(3));
    				AILeagueAdapter adapter = new AILeagueAdapter(AnimeImportsAppActivity.this, R.layout.row_league, leagueStats);
    				setListAdapter(adapter);
    				adapter.notifyDataSetChanged();
    			}
    		}
    	});
    }
    
    private void toggleLeagHeader() {
    	if(currMenu == LEAGUE_LIFETIME)
    		leagueHeader.setVisibility(View.VISIBLE);
    	else
    		leagueHeader.setVisibility(View.GONE);
    }
    
    /**
     * Toggles between an icon's on and off state
     * @param currIcon
     */
    private void swapIcons() {
    	imgInfo.setImageResource(R.drawable.ic_info_off);
    	imgLeague.setImageResource(R.drawable.ic_league_off);
    	imgEvents.setImageResource(R.drawable.ic_events_off);
    	imgNews.setImageResource(R.drawable.ic_news_off);
    	switch(currMenu) {
    	case NEWS:
    		imgNews.setImageResource(R.drawable.ic_news_on);
    		break;
    	case EVENTS:
    		imgEvents.setImageResource(R.drawable.ic_events_on);
    		break;
    	case INFO:
    		imgInfo.setImageResource(R.drawable.ic_info_on);
    		break;
    	case LEAGUE_LIFETIME:
    		imgLeague.setImageResource(R.drawable.ic_league_on);
    		break;
    	default:
    		break;
    	}
    }
    
    /**
     * Loads all details about a particular event, called when an Event is clicked
     * @param position
     */
    void handleEventClick(int position) {
    	currMenu = EVENT_DETAILS;
		ArrayList<String> eventDetails = Lists.newArrayList();
		AIEventEntry event = aiEventAdapter.getItems().get(position);
		
		eventDetails.add(event.getName());
		eventDetails.add("Date: " + event.getDate() + ", " + event.getTime());
		eventDetails.add("Event Type: " + event.getEventType());
		eventDetails.add("MTG Event Type: " + event.getMtgEventType());
		eventDetails.add("Format: " + event.getMtgFormat());
		eventDetails.add("Summary: " + event.getSummary());
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_event_details, eventDetails);
    	setListAdapter(adapter);
    	adapter.notifyDataSetChanged();
    }
    
    /**
     * Handles all clicks for any menu by looking at the currMenu member
     */
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	Log.i("DEBUG", "onListItemClick, menu is " + currMenu + ", position is " + position);
    	switch(currMenu) {
    	case NEWS:
    		break;
    	case EVENTS:
    		handleEventClick(position);
    		break;
    	case INFO:
    		if(position == 1) {
    			Intent phoneIntent = new Intent(Intent.ACTION_CALL);
    			phoneIntent.setData(Uri.parse("tel:" + this.getString(R.string.store_number)));
    			startActivity(phoneIntent);
    		}
    		break;
    	case LEAGUE_LIFETIME:
    		break;
    	default:
    		Log.i("DEBUG", "Nothing to see here");
    	}
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
    
    private void loadStoreInfo() {
    	if(storeInfo.size() == 0) {
    		storeInfo.add(this.getString(R.string.store_address));
    		storeInfo.add(this.getString(R.string.store_number));
    		storeInfo.add(this.getString(R.string.store_email));
    		storeInfo.add(this.getString(R.string.store_hours));
    	}
    	
    	ArrayAdapter<String> storeInfoAdapter = new ArrayAdapter<String>(this, R.layout.row_event_details, storeInfo);
    	setListAdapter(storeInfoAdapter);
    	storeInfoAdapter.notifyDataSetChanged();
    }
    
    private void loadNews() {
    	if(updates == null)
    		updates = new ArrayList<String>();
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_main_menu, updates);
    	setListAdapter(adapter);
    	adapter.notifyDataSetChanged();
    }

    /**
     * Calls the leagueFetchThread to fetch statistics from outside ONLY if we were just loaded into memory.
     * TODO: add a manual fetch of some sort
     * TODO: add db caching
     */
    void getLeague() {
    	if(leagueStats == null || okToFetch() ) {
    		System.out.println("ok to fetch!");
    		mProgressDialog = ProgressDialog.show(this, "Please wait...", "Retrieving leaderboard...");
	    	Thread thread = new Thread(null, leagueFetchThread, "LeagueFetchThread");
	    	thread.start();
	    	lastLeagueFetch = new Time();
	    	lastLeagueFetch.setToNow();
    	}
    	else {
    		System.out.println("not ok to fetch, hitting db");
    		mProgressDialog = ProgressDialog.show(this, "Please wait...", "Retrieving db...");
    		Thread t = new Thread(null, leagueDbFetchThread, "LeagueDbFetchThread");
    		t.start();
    	}
    }
    
    /**
     * Return whether it's ok to fetch new data (if it's been 1 hour since the last fetch)
     * @return
     */
    private Boolean okToFetch() {
    	Time now = new Time();
    	now.setToNow();
    	if(now.hour - lastLeagueFetch.hour >= 1 && now.after(lastLeagueFetch)) {
    		System.out.println("It's ok to fetch!");
    		return true;
    	}
    	else {
    		System.out.println("NOT ok to fetch!");
    		return false;
    	}
    }
    
    private Runnable leagueDbFetchThread = new Runnable() {
		@Override
		public void run() {
			leagueStats = (ArrayList<LeaguePlayer>)dm.selectAllLeague();
			if(mProgressDialog != null)
				mProgressDialog.dismiss();
			runOnUiThread(loadLeagueThread);
		}
    };
    
    private Runnable leagueFetchThread = new Runnable() {
    	@Override
    	public void run() {
    		if(leagueStats == null) {
	        	try {
	    	    	SAXParserFactory spf = SAXParserFactory.newInstance();
	    	    	SAXParser sp = spf.newSAXParser();
	    	    	XMLReader xr = sp.getXMLReader();
	    	    	URL sourceUrl = new URL(URL_LEAGUE);
	    	    	XmlParser handler = new XmlParser();
	    	    	xr.setContentHandler(handler);
	    	    	xr.parse(new InputSource(sourceUrl.openStream()));
	    	    	leagueStats = XmlParser.getStats();
	        	}
	        	catch (SAXException e) {
	        		e.printStackTrace();
	        	} 
	        	catch (ParserConfigurationException e) {
	    			e.printStackTrace();
	    		} 
	        	catch (MalformedURLException e) {
	    			e.printStackTrace();
	    		} 
	        	catch (UnknownHostException e) {
	        		runOnUiThread(recoverThread);
	        		Thread t = new Thread(null, leagueDbFetchThread, "LeagueDbFetchThread");
	        		t.start();
		    		return;
	        	}
	        	catch (IOException e) {
	    			e.printStackTrace();
	    		}
        	}
    		
    		if(mProgressDialog != null)
    			mProgressDialog.dismiss();
    		
    		if(leagueStats != null) {
    			Thread sl = new Thread(null, storeLeague, "StoreLeagueThread");
    			sl.start();
    		}
    	}
    };
    
    /**
     * Throw away stale data, store new results
     */
    private Runnable storeLeague = new Runnable() {
    	@Override
    	public void run() {
    		dm.deleteAllLeague();
    		
    		for (LeaguePlayer p : leagueStats) {
    			dm.insertLeague(p.getPlayerName(), p.getPointsSession(), p.getPointsLifetime());
    			System.out.println("Storing Name: " + p.getPlayerName() + ", Session: " + p.getPointsSession() + ", Lifetime: " + p.getPointsLifetime());
    		}
    		
    		if(mProgressDialog != null)
    			mProgressDialog.dismiss();
    		runOnUiThread(loadLeagueThread);
    	}
    };

    private Runnable loadLeagueThread = new Runnable() {
    	@Override
    	public void run() {
    		try {
	    		if(leagueSort == SORT_SESSION)
	    			Collections.sort(leagueStats, new LeaguePlayerComparator(2));
	    		else if(leagueSort == SORT_LIFETIME)
	    			Collections.sort(leagueStats, new LeaguePlayerComparator(3));
	    		else 
	    			Collections.sort(leagueStats, new LeaguePlayerComparator(1));
	    		
	    		AILeagueAdapter adapter = new AILeagueAdapter(AnimeImportsAppActivity.this, R.layout.row_league, leagueStats);
	            setListAdapter(adapter);
	            if(mProgressDialog != null)
	            	mProgressDialog.dismiss();
	    		adapter.notifyDataSetChanged();
	    		toggleLeagHeader();
    		}
    		catch(NullPointerException e) {
    			runOnUiThread(recoverThread);
    		}
    	}
    };

    /**
     * Calls the eventFetchThread to query GoogleCalendar for events and loads a ProgressDialog
     */
    void getEvents() {
    	mProgressDialog = ProgressDialog.show(AnimeImportsAppActivity.this, "Please wait...", "Retrieving data...", true);
		Thread thread = new Thread(null, eventFetchThread, "MagentoBackground");
		thread.start();
    }
    
    /**
     * Fetch calendar events from GoogleCalendar
     * TODO: figure out caching
     */
    private Runnable eventFetchThread = new Runnable() {
		@Override
		public void run() {
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
	    		loadNews();
	    		return;
	    	}
	    	catch(SocketTimeoutException e) {
	    		runOnUiThread(recoverThread);
	    		loadNews();
	    	}
	    	catch(IOException e) {
	    		System.out.println("IOException yo");
	    		e.printStackTrace();
	    	}
	    	
	    	runOnUiThread(loadEventsThread);
		}
	};
    
    /**
     * Called after eventFetchThread has already filled our events array with events
     */
    private Runnable loadEventsThread = new Runnable() {
    	@Override
    	public void run() {
    		aiEventAdapter = new AIEventAdapter(AnimeImportsAppActivity.this, R.layout.row_event, events);
            setListAdapter(aiEventAdapter);
            if(mProgressDialog != null)
            	mProgressDialog.dismiss();
    		aiEventAdapter.notifyDataSetChanged();
    		toggleLeagHeader();
    	}
    };
    
    /**
     * Called when we encounter an exception (usually some kind of connection issue)
     * Alert the user via Toast
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
}
