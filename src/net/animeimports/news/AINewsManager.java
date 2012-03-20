package net.animeimports.news;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class AINewsManager {
	private static AINewsManager nManager = null;
	private static ArrayList<AINewsItem> items = null;

	private static final String USER = "animeimports";
	private static final int LIMIT = 20;
	
	public static AINewsManager getInstance() {
		if(nManager == null)
			nManager = new AINewsManager();
		return nManager;
	}
	
	private AINewsManager() {
		Twitter twitter = new TwitterFactory().getInstance();
		items = new ArrayList<AINewsItem>();
		try {
			List<Status> statuses;
			Paging paging = new Paging(1, LIMIT);
			statuses = twitter.getUserTimeline(USER, paging);
			for(Status status : statuses) {
				AINewsItem newsItem = new AINewsItem();
				newsItem.setItem(status.getText());
				newsItem.setDate(status.getCreatedAt().toString());
				items.add(newsItem);
			}
		}
		catch(TwitterException e) {
            Log.e("AI ERROR", "Failed to get timeline: '" + e.getMessage() + "', statusCode: '" + e.getStatusCode() + "'");
            // TODO: run recover thread
		}
	}

	/**
	 * @return the items
	 */
	public ArrayList<AINewsItem> getItems() {
		return items;
	}
}
