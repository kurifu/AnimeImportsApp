package net.animeimports.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.animeimports.android.tasks.EventFetchDbTask;
import net.animeimports.android.tasks.EventFetchTask;
import net.animeimports.android.tasks.LeagueFetchDbTask;
import net.animeimports.android.tasks.LeagueFetchTask;
import net.animeimports.android.tasks.NewsFetchDbTask;
import net.animeimports.android.tasks.NewsFetchTask;
import net.animeimports.android.tasks.StoreEventsTask;
import net.animeimports.android.tasks.StoreLeagueTask;
import net.animeimports.android.tasks.StoreNewsTask;
import net.animeimports.calendar.AIEventAdapter;
import net.animeimports.calendar.AIEventEntry;
import net.animeimports.league.AILeagueAdapter;
import net.animeimports.league.LeaguePlayer;
import net.animeimports.league.LeaguePlayerComparator;
import net.animeimports.news.AINewsAdapter;
import net.animeimports.news.AINewsItem;
import net.animeimports.news.AINewsManager;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;

/**
 * Main Activity for AI, performs the following functions:
 * 1. Fetch news updates via Twitter
 * 2. Provides store info, such as street address, phone number, website, email
 * 3. Find upcoming store events via GoogleCalendar
 * 4. Provide statistics for the current MTG League session, along with lifetime point totals
 * @author kurifuc4
 *
 */
public class AnimeImportsAppActivity extends ListActivity {

	// Events / Lists
	private List<String> storeInfo = null;
	private AIEventAdapter aiEventAdapter;
	private AINewsAdapter aiNewsAdapter;
	
	private int currMenu = 0;
	private final int NEWS = 1;
	private final int EVENTS = 2;
	private final int EVENT_DETAILS = 3;
	private final int INFO = 4;
	private final int LEAGUE_LIFETIME = 6;
	
	protected static ProgressDialog mProgressDialog = null;
	protected static ArrayList<AIEventEntry> events = null;
	private static ArrayList<LeaguePlayer> leagueStats = null;
	private static ArrayList<AINewsItem> updates = null;
	
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
	
	protected DataManager dm = null;
	EventTaskListener etListener = null;
	LeagueTaskListener ltListener = null;
	NewsTaskListener ntListener = null;
	Context mContext = null;
	
	private static final String MAPS_URL = "http://maps.google.com/maps?daddr=";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initializeApp();
    }
    
    /**
     * Find all UI elements we tap into, initialize static data
     * TODO: find a better way to initialize onclicklisteners
     */
    public void initializeApp() {
    	mContext = this;
    	events = new ArrayList<AIEventEntry>();
	    dm = DataManager.getInstance(this);
	    
    	imgInfo = (ImageView) findViewById(R.id.imgInfo);
    	imgLeague = (ImageView) findViewById(R.id.imgLeague);
    	imgEvents = (ImageView) findViewById(R.id.imgEvents);
    	imgNews = (ImageView) findViewById(R.id.imgNews);
    	leagueHeader = (LinearLayout) findViewById(R.id.llLeagueHead);
    	tvNameHeader = (TextView) findViewById(R.id.tvNameHeader);
    	tvSessionHeader = (TextView) findViewById(R.id.tvSessionHeader);
    	tvLifetimeHeader = (TextView) findViewById(R.id.tvLifetimeHeader);
    	etListener = new EventTaskListener();
    	ltListener = new LeagueTaskListener();
    	ntListener = new NewsTaskListener();
    	
    	storeInfo = Lists.newArrayList();
		storeInfo.add(this.getString(R.string.store_address));
		storeInfo.add(this.getString(R.string.store_number));
		storeInfo.add(this.getString(R.string.store_email));
		storeInfo.add(this.getString(R.string.store_hours));
    	
    	currMenu = NEWS;
    	swapIcons();
    	getNews();
    	
    	imgNews.setOnClickListener(new OnClickListener() {
    	    public void onClick(View v) {
    	    	currMenu = NEWS;
    	    	swapIcons();
    	    	getNews();
    	    	toggleLeagueHeader();
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
    	    	loadStoreInfo();
    	    	toggleLeagueHeader();
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
    				tvNameHeader.setTextColor(getResources().getColor(R.color.tv_highlight));
    				tvSessionHeader.setTextColor(Color.WHITE);
    				tvLifetimeHeader.setTextColor(Color.WHITE);
    				Collections.sort(leagueStats, new LeaguePlayerComparator(1));
    				AILeagueAdapter adapter = new AILeagueAdapter(AnimeImportsAppActivity.this, R.layout.row_league, leagueStats);
    				setListAdapter(adapter);
    				adapter.notifyDataSetChanged();
    				leagueSort = SORT_NAME;
    			}
    		}
    	});
    	tvSessionHeader.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			if(leagueStats != null) {
    				tvNameHeader.setTextColor(Color.WHITE);
    				tvSessionHeader.setTextColor(getResources().getColor(R.color.tv_highlight));
    				tvLifetimeHeader.setTextColor(Color.WHITE);
    				Collections.sort(leagueStats, new LeaguePlayerComparator(2));
    				AILeagueAdapter adapter = new AILeagueAdapter(AnimeImportsAppActivity.this, R.layout.row_league, leagueStats);
    				setListAdapter(adapter);
    				adapter.notifyDataSetChanged();
    				leagueSort = SORT_SESSION;
    			}
    		}
    	});
    	tvLifetimeHeader.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			if(leagueStats != null) {
    				tvNameHeader.setTextColor(Color.WHITE);
    				tvSessionHeader.setTextColor(Color.WHITE);
    				tvLifetimeHeader.setTextColor(getResources().getColor(R.color.tv_highlight));
    				Collections.sort(leagueStats, new LeaguePlayerComparator(3));
    				AILeagueAdapter adapter = new AILeagueAdapter(AnimeImportsAppActivity.this, R.layout.row_league, leagueStats);
    				setListAdapter(adapter);
    				adapter.notifyDataSetChanged();
    				leagueSort = SORT_LIFETIME;
    			}
    		}
    	});
    }
    
    /**
     * Toggle the Name/Session/League header bar displayed on the League screen
     */
    private void toggleLeagueHeader() {
    	if(currMenu == LEAGUE_LIFETIME)
    		leagueHeader.setVisibility(View.VISIBLE);
    	else
    		leagueHeader.setVisibility(View.GONE);
    }
    
    /**
     * Toggles between an icon's on and off state (main activity's menu icons)
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
    private void handleEventClick(int position) {
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
     * Grab the link in the news update, prompt user to open in browser
     * If no URL is found in the text, return. Note that we assume two cases:
     * 1. The hyperlink is followed with a space
     * 2. The hyperlink ends the news post
     * @param position
     */
    private void handleNewsClick(int position) {
    	String item = updates.get(position).getItem();
    	int start = item.indexOf("http");
    	if(start == -1)
    		return;
    	int end = item.indexOf(" ", start);
    	if(end == -1)
    		end = item.length();
    	final String url = item.substring(start, end);

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("Open link in browser?")
    		.setCancelable(false)
    		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent i = new Intent(Intent.ACTION_VIEW);
			    	i.setData(Uri.parse(url));
			    	startActivity(i);
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			}).show();
    }
    
    private void handleInfoClick(int position) {
    	Builder builder = new AlertDialog.Builder(mContext);
    	switch(position) {
    	case 0:
        	builder.setMessage("Show store location in Maps?")
        		.setCancelable(false)
        		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					Uri uri = Uri.parse("geo:" + mContext.getString(R.string.store_address_lattitude) + "," + mContext.getString(R.string.store_address_longitude));
    		    		Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
    		    		startActivity(mapIntent);
    				}
    			})
    			.setNegativeButton("No", new DialogInterface.OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					dialog.cancel();
    				}
    			}).show();
    		break;
    	case 1:
    		builder.setMessage("Call store?").setCancelable(false)
    			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent phoneIntent = new Intent(Intent.ACTION_CALL);
						phoneIntent.setData(Uri.parse("tel:" + mContext.getString(R.string.store_number)));
						startActivity(phoneIntent);
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).show();
			break;
    	case 2:
    		builder.setMessage("Email store?").setCancelable(false)
	    		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent emailIntent = new Intent(Intent.ACTION_SEND);
			    		emailIntent.setType("plain/text");
			    		startActivity(emailIntent.createChooser(emailIntent, "Send email with:"));
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).show();
    		break;
		default:
			break;
    	}
    }
    /**
     * Handles all clicks for any menu by looking at the currMenu member
     */
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	switch(currMenu) {
    	case NEWS:
    		handleNewsClick(position);
    		break;
    	case EVENTS:
    		handleEventClick(position);
    		break;
    	case INFO:
    		handleInfoClick(position);
    		break;
    	case LEAGUE_LIFETIME:
    		break;
    	default:
    		Log.i("DEBUG", "Nothing to see here");
    	}
    }
    
    private void loadStoreInfo() {
    	ArrayAdapter<String> storeInfoAdapter = new ArrayAdapter<String>(this, R.layout.row_event_details, storeInfo);
    	setListAdapter(storeInfoAdapter);
    	storeInfoAdapter.notifyDataSetChanged();
    	toggleLeagueHeader();
    }
    
    private void getNews() {
    	if(updates == null || updates.size() == 0) {
	    	NewsFetchTask task = new NewsFetchTask(ntListener);
	    	task.execute(updates);
    	}
    	else {
    		loadNews();
    	}
    }
    
    private void loadNews() {
    	aiNewsAdapter = new AINewsAdapter(mContext, R.layout.row_news_item, updates);
    	setListAdapter(aiNewsAdapter);
    	aiNewsAdapter.notifyDataSetChanged();
    	if(mProgressDialog != null)
			mProgressDialog.dismiss();
    	toggleLeagueHeader();
    }

    /**
     * Calls the leagueFetchThread to fetch statistics from outside ONLY if we were just loaded into memory.
     * TODO: add a manual fetch of some sort
     */
    @SuppressWarnings("unchecked")
	private void getLeague() {
    	if(dm.okToFetchLeague()) {
	    	LeagueFetchTask task = new LeagueFetchTask(ltListener);
	    	task.execute(leagueStats);
    	}
    	else if(leagueStats == null || leagueStats.size() == 0) {
    		LeagueFetchDbTask task = new LeagueFetchDbTask(ltListener, mContext);
    		task.execute(leagueStats);
    	}
    	else {
    		loadLeague();
    	}
    }

    private void loadLeague() {
		try {
    		if(leagueSort == SORT_SESSION) {
    			Collections.sort(leagueStats, new LeaguePlayerComparator(2));
    			tvSessionHeader.setTextColor(getResources().getColor(R.color.tv_highlight));
    			tvNameHeader.setTextColor(getResources().getColor(R.color.tv_normal));
    			tvLifetimeHeader.setTextColor(getResources().getColor(R.color.tv_normal));
    		}
    		else if(leagueSort == SORT_LIFETIME) {
    			Collections.sort(leagueStats, new LeaguePlayerComparator(3));
    			tvLifetimeHeader.setTextColor(getResources().getColor(R.color.tv_highlight));
    			tvSessionHeader.setTextColor(getResources().getColor(R.color.tv_normal));
    			tvNameHeader.setTextColor(getResources().getColor(R.color.tv_normal));
    		}
    		else {
    			Collections.sort(leagueStats, new LeaguePlayerComparator(1));
    			tvNameHeader.setTextColor(getResources().getColor(R.color.tv_highlight));
    			tvSessionHeader.setTextColor(getResources().getColor(R.color.tv_normal));
    			tvLifetimeHeader.setTextColor(getResources().getColor(R.color.tv_normal));
    		}
    		
    		AILeagueAdapter adapter = new AILeagueAdapter(mContext, R.layout.row_league, leagueStats);
            setListAdapter(adapter);
    		adapter.notifyDataSetChanged();
    		if(mProgressDialog != null)
            	mProgressDialog.dismiss();
    		toggleLeagueHeader();
		}
		catch(NullPointerException e) {
			runOnUiThread(recoverThread);
		}
    }

    @SuppressWarnings("unchecked")
	private void getEvents() {
    	if(dm.okToFetchEvents()) {
	    	EventFetchTask task = new EventFetchTask(etListener, mContext);
	    	task.execute(events);
    	}
    	else if(events == null || events.size() == 0) {
    		EventFetchDbTask task = new EventFetchDbTask(etListener, mContext);
    		task.execute(events);
    	}
    	else {
    		loadEvents();
    	}
    }
    
    protected void loadEvents() {
		aiEventAdapter = new AIEventAdapter(AnimeImportsAppActivity.this, R.layout.row_event, events);
        setListAdapter(aiEventAdapter);
        aiEventAdapter.notifyDataSetChanged();
        if(mProgressDialog != null)
        	mProgressDialog.dismiss();
		toggleLeagueHeader();
    }
    
    /**
     * Called when we encounter an exception (usually some kind of connection issue); alert the user via Toast
     * NOTE: you should only call this thread on the main UI thread!
     */
    public Runnable recoverThread = new Runnable() {
    	@Override
    	public void run() {
    		Context context = mContext;
    		CharSequence text = "No internet connection or reception, try again later";
    		Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
    		toast.show();
    	}
    };
    
    // TODO: Refactor all 3 Listeners into a single class 
	public class LeagueTaskListener {
		public void init() {
			mProgressDialog = ProgressDialog.show(mContext, "Please wait...", "Retrieving league statistics...");
		}
		public void onDbComplete(ArrayList<LeaguePlayer> stats) {
			if(stats != null)
				leagueStats = stats;
			loadLeague();
		}
		public void onComplete(boolean success, ArrayList<LeaguePlayer> stats) {
			if(success) {
				Log.i("DEBUG", "Succeeded in fetching league!");
				if(stats != null || stats.size() != 0) {
					leagueStats = stats;
					StoreLeagueTask task = new StoreLeagueTask(leagueStats, ltListener, mContext);
					task.execute();
				}
			}
			else {
				LeagueFetchDbTask task = new LeagueFetchDbTask(ltListener, mContext);
				task.execute();
			}
		}
		public void recover() {
			runOnUiThread(recoverThread);
		}
	}
    
    public class EventTaskListener {
		public void init() {
			mProgressDialog = ProgressDialog.show(mContext, "Please wait...", "Retrieving upcoming events...");
		}
		public void onDbComplete(ArrayList<AIEventEntry> result) {
			if(result != null)
				events = result;
			loadEvents();
		}
		public void onComplete(boolean success, ArrayList<AIEventEntry> result) {
			if(success) {
				System.out.println("Succeeded fetching!");
		    	if(result != null && result.size() != 0) {
		    		events = result;
		    		StoreEventsTask task = new StoreEventsTask(events, etListener, mContext);
		    		task.execute();
	    		}
			}
			else {
				System.out.println("FAILED, loading from db");
				EventFetchDbTask task = new EventFetchDbTask(etListener, mContext);
				task.execute();
			}
		}
		public void recover() {
			runOnUiThread(recoverThread);
		}
	}
    
    public class NewsTaskListener {
    	public void init() {
    		mProgressDialog = ProgressDialog.show(mContext, "Please wait...", "Retrieving latest news...");
    	}
    	public void onDbComplete(ArrayList<AINewsItem> result) {
    		if(result != null)
    			updates = result;
    		loadNews();
    	}
    	public void onComplete(boolean success, ArrayList<AINewsItem> result) {
    		if(success) {
    			System.out.println("Succeeded in fetching news!");
    			if(result != null && result.size() != 0) {
    				updates = result;
    				StoreNewsTask task = new StoreNewsTask(updates, ntListener, mContext);
    				task.execute();
    			}
    		}
    		else {
    			System.out.println("FAILED to fetch news, loading from db?");
    			NewsFetchDbTask task = new NewsFetchDbTask(ntListener, mContext);
    			task.execute();
    		}
    	}
    	public void recover() {
    		runOnUiThread(recoverThread);
    	}
    }
}
