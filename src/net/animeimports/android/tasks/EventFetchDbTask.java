package net.animeimports.android.tasks;

import java.util.ArrayList;

import net.animeimports.android.AnimeImportsAppActivity.EventTaskListener;
import net.animeimports.android.DataManager;
import net.animeimports.calendar.AIEventEntry;
import android.content.Context;
import android.os.AsyncTask;

public class EventFetchDbTask extends AsyncTask<ArrayList<AIEventEntry>, Void, ArrayList<AIEventEntry>> {
	private EventTaskListener listener = null;
	public ArrayList<AIEventEntry> events = null;
	private DataManager dm = null;
	private Context mContext = null;
	
	public EventFetchDbTask(EventTaskListener l, Context c) {
		this.listener = l;
		this.mContext = c;
		dm = DataManager.getInstance(this.mContext);
	}
	
	@Override
	protected void onPreExecute() {
		events = new ArrayList<AIEventEntry>();
	}
	@Override
	protected ArrayList<AIEventEntry> doInBackground(ArrayList<AIEventEntry>... params) {
		events = dm.selectAllEvents();
		return events;
	}
	@Override
	protected void onPostExecute(ArrayList<AIEventEntry> result) {
		if(listener != null) 
			listener.onDbComplete(result);
	}
}