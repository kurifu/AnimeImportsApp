package net.animeimports.android;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
import android.widget.ListView;
import android.widget.TextView;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import net.animeimports.android.AIEventEntry;

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
	private List<String> options_links = Lists.newArrayList();
	private final List<CalendarEntry> calendars = Lists.newArrayList();
	private AIEventAdapter aiEventAdapter;
	
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
        
        /*for(int i = 0; i < 5; i++) {
        	AIEventEntry e = new AIEventEntry();
        	e.setDate("Right Now");
        	e.setName("Friday Night Magic");
        	e.setTime("7:00pm");
        	this.events.add(e);
        }*/
        
        loadMainMenu();
        
        //ArrayAdapter<AIEventEntry> songList = new ArrayAdapter<AIEventEntry>(this, R.layout.event, events);
		//setListAdapter(songList);
        
        //getListView().setTextFilterEnabled(true);
        //registerForContextMenu(getListView());
        //gotAccount();
    }
    
    void loadMainMenu() {
    	if(options_links.size() == 0) {
    		options_links.add(UPDATES);
    		options_links.add(UPCOMING_EVENTS);
    		options_links.add(STORE);
    		options_links.add(STORE_INFO);
    	}
    	ArrayAdapter<String> options = new ArrayAdapter<String>(this, R.layout.main_menu_option, options_links);
    	setListAdapter(options);
    	
    	//textViewUpdates = (TextView) options.getView(0, textViewUpdates, R.layout.main);
    	
    	setContentView(R.layout.main);
    }
    
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	String text = (String)((TextView)v).getText();
    	if(text.equals(UPDATES)) {
    		
    	}
    	else if(text.equals(UPCOMING_EVENTS)) {
    		getListView().setTextFilterEnabled(true);
            registerForContextMenu(getListView());
            gotAccount();
    	}
		else if(text.equals(STORE)) {
		    		
		}
		else if(text.equals(STORE_INFO)) {
			
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
    
    /**
     * Write a new account name to preferences, need to remove PREF_GSESSIONID apparently?
     * @param accountName
     */
    void setAccountName(String accountName) {
    	SharedPreferences.Editor editor = settings.edit();
    	editor.putString(PREF_ACCOUNT_NAME, accountName);
    	editor.remove(PREF_GSESSIONID);
    	editor.commit();
    	this.accountName = accountName;
    	requestInitializer.setGsessionid(null);
    }
    
    private void gotAccount() {
    	Account account = accountManager.getAccountByName(accountName);
    	
    	if(account != null) {
    		Log.i("LOOK", "inside gotAccount, account != null");
    		// If token invalid, authorize for first time with prompt?
    		if(requestInitializer.authToken == null) {
    			Log.i("LOOK", "inside gotAccount, gonna authenticate with screen?");	
    			accountManager.manager.getAuthToken(account, AUTH_TOKEN_TYPE, true, new AccountManagerCallback<Bundle>() {
    				public void run(AccountManagerFuture<Bundle> future) {
    					try {
    						Bundle bundle = future.getResult();
    						if(bundle.containsKey(AccountManager.KEY_INTENT)) {
    							Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
    							int flags = intent.getFlags();
    							flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;
    							intent.setFlags(flags);
    							startActivityForResult(intent, REQUEST_AUTHENTICATE);
    						}
    						else if(bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
    							setAuthToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));
    							executeRefreshCalendars();
    						}
    					}
    					catch (Exception e) {
    						handleException(e);
    					}
    				}
    			}, null);
    		}
    		else {
    			executeRefreshCalendars();
    		}
    		return;
    	}
    	Log.i("LOOK", "before chooseAccount in gotAccount");
    	chooseAccount();
    }
    
    private void chooseAccount() {
    	accountManager.manager.getAuthTokenByFeatures(GoogleAccountManager.ACCOUNT_TYPE, 
    		AUTH_TOKEN_TYPE, null, AnimeImportsAppActivity.this, null, null, new AccountManagerCallback<Bundle>() {
    		
    			public void run(AccountManagerFuture<Bundle> future) {
    				Log.i("LOOK", "inside chooseAccount > run");
    				Bundle bundle;
    				try {
    					bundle = future.getResult();
    					Log.i("DEBUG", "Account Name is " + bundle.getString(AccountManager.KEY_ACCOUNT_NAME));
    					setAccountName(bundle.getString(AccountManager.KEY_ACCOUNT_NAME));
    					//setAccountName("animeimports@gmail.com");
    					setAuthToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));
    					executeRefreshCalendars();
    				}
    				catch(OperationCanceledException e) {
    					Log.i("DEBUG", "OperateCanceled Exception?");
    				}
    				catch(AuthenticatorException e) {
    					Log.i("DEBUG", "AuthenticationException?");
    					handleException(e);
    				}
    				catch(IOException e) {
    					Log.i("DEBUG", "IOException?");
    					handleException(e);
    				}
    			}
    	}, null);
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	switch(requestCode) {
    	case REQUEST_AUTHENTICATE:
    		if(resultCode == RESULT_OK) {
    			gotAccount();
    		}
    		else {
    			chooseAccount();
    		}
    		break;
    	}
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, MENU_ACCOUNTS, 0, getString(R.string.new_calendar));
    	if(accountManager.getAccounts().length >= 2) {
    		Log.i("DEBUG", "AccountManager has more than 2 accounts!");
    		menu.add(0, MENU_ACCOUNTS, 0, getString(R.string.switch_account));
    	}
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	Log.i("DEBUG", "inside onOptionsItemSelected");
    	switch(item.getItemId()) {
    		case MENU_ADD:
    			CalendarUrl url = forOwnCalendarsFeed();//TODO
    			CalendarEntry calendar = new CalendarEntry();
    			calendar.title = "Calendar " + new DateTime(new Date());
    			try {
    				Log.i("DEBUG", "onOptionsItemSelected, long chain call");
    				client.calendarFeed().insert().execute(url, calendar);
    			}
    			catch(IOException e) {
    				handleException(e);
    			}
    			executeRefreshCalendars();
    			return true;
    		case MENU_ACCOUNTS:
    			chooseAccount();
    			return true;
    	}
    	return false;
    }
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	Log.i("DEBUG", "inside onCreateContextMenu");
    	menu.add(0, CONTEXT_EDIT, 0, getString(R.string.update_title));
    	menu.add(0, CONTEXT_DELETE, 0, getString(R.string.delete));
    }
    
    public boolean onContextItemSelected(MenuItem item) {
    	Log.i("DEBUG", "inside onContextItemSelected");
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	CalendarEntry calendar = calendars.get((int) info.id);
    	
    	CalendarUrl url = new CalendarUrl(calendar.getEventFeedLink());
    	System.out.println("*** CalendarURL is: " + url.toString());
    	url = new CalendarUrl("https://www.google.com/calendar/feeds/animeimports@gmail.com/public/full");
    	
    	try {
    		switch(item.getItemId()) {
    			case CONTEXT_EDIT:
    				CalendarEntry patchedCalendar = calendar.clone();
    				patchedCalendar.title = calendar.title + " UPDATED " + new DateTime(new Date());
    				client.executePatchRelativeToOriginal(calendar, patchedCalendar);
    				executeRefreshCalendars();
    				return true;
    			case CONTEXT_DELETE:
    				client.executeDelete(calendar);
    				executeRefreshCalendars();
    				return true;
    			default:
    				return super.onContextItemSelected(item);
    		}
    	}
    	catch(IOException e) {
    		handleException(e);
    	}
    	return false;
    }
    
    void executeRefreshCalendars() {
    	Log.i("LOOK", "inside executeRefreshCalendars");
    	String[] calendarNames;
    	List<CalendarEntry> calendars = this.calendars;
    	calendars.clear();
    	events.clear();
    	try {
    		CalendarUrl url = forAllCalendarsFeed();
    		// page through results?
    		while(true) {
    			CalendarFeed feed = client.calendarFeed().list().execute(url);
    			if(feed.calendars != null) {
    				Log.i("DEBUG", "calling calendars.addAll(feed.calendars)");
    				calendars.addAll(feed.calendars);
    			}
    			String nextLink = feed.getNextLink();
    			if(nextLink == null) {
    				break;
    			}
    		}
    		int numCalendars = calendars.size();
    		Log.i("DEBUG", "found " + numCalendars + " calendars");
    		calendarNames = new String[numCalendars];
    		for(int i = 0; i < numCalendars; i++) {
    			calendarNames[i] = calendars.get(i).title;
    		}
    	}
    	catch(IOException e) {
    		Log.i("DEBUG", "caught an IOException");
    		handleException(e);
    		Log.i("DEBUG*", "After handle exception");

    		calendarNames = new String[] {e.getMessage()};
    		calendars.clear();
    	}
    	// Setting up UI

    	try {
    		if(calendars.size() > 1) {
    			Log.i("DEBUG", "More than one calendar, doing something bad...?");
    		}
    		
    		CalendarUrl url = new CalendarUrl(calendars.get(0).getEventFeedLink());
        	//url = new CalendarUrl("https://www.google.com/calendar/feeds/animeimports@gmail.com/public/full");
    		
    		EventFeed feed = client.eventFeed().list().execute(url);
    		for(EventEntry entry : feed.getEntries()) {
    			AIEventEntry e = new AIEventEntry();
    			
    			System.out.println("\t* Event:" + entry.title);
    			System.out.println("\t\t Summary:" + entry.summary);
    			
    			if(entry.when == null) {
    				Log.i("ERROR", "* entry.when is null");
    				e.setDate("Unknown");
    				e.setTime("Unknown");
    				System.out.println("\t\t When: Unknown");
    			}
    			else {
    				e.setDate(entry.when.startTime.toStringRfc3339().substring(0, 10));
        			e.setTime(entry.when.startTime.toString().substring(11, 19));
        			System.out.println("\t\t When: " + entry.when.startTime);
    			}
    			
    			e.setName(entry.title);
    			events.add(e);
    		}
    		
    		//setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, feed));
    	}
    	catch(IOException e) {
    		e.printStackTrace();
    	}
    	
    	this.aiEventAdapter = new AIEventAdapter(this, R.layout.row, events);
        setListAdapter(this.aiEventAdapter);
        
    	//setListAdapter(new ArrayAdapter<AIEventEntry>(this, R.layout.row, events));
    	//Log.i("LOOK!", "before setting calendarNames");
    	//setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, calendarNames));
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
    			gotAccount();
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
    
    private static CalendarUrl forRoot() {
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
    }
    
    public class AIEventAdapter extends ArrayAdapter<AIEventEntry> {
    	private ArrayList<AIEventEntry> items;
    	
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
    			if(tt != null) {
    				tt.setText("Event: " + event.getName());
    			}
    			if(bt != null) {
    				bt.setText("Date: " + event.getDate() + ", " + event.getTime());
    			}
    		}
    		return v;
    	}
    }
}