/*package net.animeimports.android.tasks;

import java.util.ArrayList;
import java.util.List;

import net.animeimports.android.AnimeImportsAppActivity.LeagueTaskListener;
import net.animeimports.news.AINewsItem;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;


import android.os.AsyncTask;
import android.util.Log;

public class NewsFetchTask extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> { 

	private boolean success = true;
	LeagueTaskListener listener = null;
	private static ArrayList<AINewsItem> items = null;
	private static final String USER = "animeimports";
	private static final int LIMIT = 20;
	
	public NewsFetchTask(LeagueTaskListener l) {
		this.listener = l;
		items = new ArrayList<AINewsItem>();
	}
	
	@Override
	protected void onPreExecute() {
		listener.init();
	}
	
	@Override
	protected ArrayList<AINewsItem> doInBackground(ArrayList<AINewsItem>... arg0) {
		Twitter twitter = new TwitterFactory().getInstance();
		items.clear();
		
		try {
			List<Status> statuses;
			Paging paging = new Paging(1, LIMIT);
			statuses = twitter.getUserTimeline(USER, paging);
			for(Status status : statuses) {
				AINewsItem newsItem = new AINewsItem();
				newsItem.setItem(status.getText());
				newsItem.setDate(status.getCreatedAt());
				items.add(newsItem);
			}
		}
		catch(TwitterException e) {
            Log.e("AI ERROR", "Failed to get timeline: '" + e.getMessage() + "', statusCode: '" + e.getStatusCode() + "'");
            // TODO: run recover thread
		}
		return items;
	}
	
	@Override
	protected void onPostExecute(ArrayList<AINewsItem> result) {
		if(listener != null)
			listener.onComplete(success, result);
	}
}*/