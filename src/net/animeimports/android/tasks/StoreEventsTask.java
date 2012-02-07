package net.animeimports.android.tasks;

import java.util.ArrayList;

import net.animeimports.android.AnimeImportsAppActivity.EventTaskListener;
import net.animeimports.android.DataManager;
import net.animeimports.calendar.AIEventEntry;
import net.animeimports.calendar.AIEventEntry.EVENT_TYPE;
import net.animeimports.calendar.AIEventEntry.MTG_EVENT_TYPE;
import net.animeimports.calendar.AIEventEntry.MTG_FORMAT;
import android.content.Context;
import android.os.AsyncTask;

public class StoreEventsTask extends AsyncTask<ArrayList<AIEventEntry>, Void, Void> {
	private DataManager dm = null;
	private Context mContext = null;
	public ArrayList<AIEventEntry> events = null;
	private EventTaskListener listener = null;
	
	public StoreEventsTask(ArrayList<AIEventEntry> e, EventTaskListener l, Context c) {
		this.mContext = c;
		this.listener = l;
		this.events = e;
		dm = DataManager.getInstance(mContext);
	}
	
	@Override
	protected Void doInBackground(ArrayList<AIEventEntry>... arg0) {
		dm.deleteAllEvents();
		
		for(AIEventEntry e : events) {
			if(e.getMtgEventType() == null)
				e.setMtgEventType(MTG_EVENT_TYPE.UNKNOWN);
			if(e.getMtgFormat() == null)
				e.setMtgFormat(MTG_FORMAT.UNKNOWN);
			if(e.getEventType() == null)
				e.setEventType(EVENT_TYPE.UNKNOWN);
			dm.insertEvents(e.getName(), e.getDate(), e.getEventType().getIntValue(), e.getMtgFormat().getIntValue(), e.getMtgEventType().getIntValue(), e.getSummary());
		}
		return null;
	}
	
	/**
	 * Notify the listener that we are ready to load our events into the main activity. Note that we pass in null because we have already
	 * loaded the data into main memory with either the EventFetchTask or EventFetchDbTask, which were run prior to this task
	 */
	@Override
	protected void onPostExecute(Void result) {
		listener.onDbComplete(null);
	}
}