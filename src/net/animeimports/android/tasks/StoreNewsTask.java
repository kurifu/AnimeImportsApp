package net.animeimports.android.tasks;

import java.util.ArrayList;

import net.animeimports.android.AnimeImportsAppActivity.NewsTaskListener;
import net.animeimports.android.DataManager;
import net.animeimports.calendar.AIEventEntry;
import net.animeimports.calendar.AIEventEntry.EVENT_TYPE;
import net.animeimports.calendar.AIEventEntry.MTG_EVENT_TYPE;
import net.animeimports.calendar.AIEventEntry.MTG_FORMAT;
import net.animeimports.news.AINewsItem;

import android.content.Context;
import android.os.AsyncTask;

public class StoreNewsTask extends AsyncTask<ArrayList<AINewsItem>, Void, ArrayList<AINewsItem>> {
	private DataManager dm = null;
	private Context mContext = null;
	public ArrayList<AINewsItem> news = null;
	private NewsTaskListener listener = null;
	
	public StoreNewsTask(ArrayList<AINewsItem> e, NewsTaskListener l, Context c) {
		this.mContext = c;
		this.listener = l;
		this.news = e;
		dm = DataManager.getInstance(mContext);
	}
	
	@Override
	protected ArrayList<AINewsItem> doInBackground(ArrayList<AINewsItem>... arg0) {
		dm.deleteAllEvents();
		
		for(AINewsItem n : news) {
			//dm.insertEvents(n.getName(), n.getDate(), n.getEventType().getIntValue(), n.getMtgFormat().getIntValue(), n.getMtgEventType().getIntValue(), n.getSummary());
			dm.insertNews(n.getItem(), n.getDate().toString());
			System.out.println("Date check: " + n.getDate().toString());
		}
		return news;
	}
	
	/**
	 * Notify the listener that we are ready to load our events into the main activity. Note that we pass in null because we have already
	 * loaded the data into main memory with either the EventFetchTask or EventFetchDbTask, which were run prior to this task
	 */
	@Override
	protected void onPostExecute(ArrayList<AINewsItem> result) {
		listener.onDbComplete(result);
	}
}