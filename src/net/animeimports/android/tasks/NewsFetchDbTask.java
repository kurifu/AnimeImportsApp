package net.animeimports.android.tasks;

import java.util.ArrayList;

import net.animeimports.android.AnimeImportsAppActivity.NewsTaskListener;
import net.animeimports.android.DataManager;
import net.animeimports.news.AINewsItem;

import android.content.Context;
import android.os.AsyncTask;

public class NewsFetchDbTask extends AsyncTask<ArrayList<AINewsItem>, Void, ArrayList<AINewsItem>> {

	private NewsTaskListener listener = null;
	private Context mContext = null;
	private DataManager dm = null;
	private ArrayList<AINewsItem> news = null;
	
	public NewsFetchDbTask(NewsTaskListener l, Context c) {
		this.listener = l;
		this.mContext = c;
		dm = DataManager.getInstance(mContext);
	}
	
	@Override 
	protected void onPreExecute() {
		news = new ArrayList<AINewsItem>();
	}
	
	@Override
	protected ArrayList<AINewsItem> doInBackground(ArrayList<AINewsItem> ... arg0) {
		news = dm.selectAllNews();
		return news;
	}
	
	@Override
	protected void onPostExecute(ArrayList<AINewsItem> result) {
		if(listener != null)
			listener.onDbComplete(result);
	}
}