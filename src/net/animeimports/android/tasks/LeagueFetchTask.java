package net.animeimports.android.tasks;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.animeimports.android.AnimeImportsAppActivity.LeagueTaskListener;
import net.animeimports.league.LeaguePlayer;
import net.animeimports.league.XmlParser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.os.AsyncTask;

public class LeagueFetchTask extends AsyncTask<ArrayList<LeaguePlayer>, Void, ArrayList<LeaguePlayer>> {
	private static final String URL_LEAGUE = "http://animeimports.net/league/MTGILEAGUE.xml";
	private boolean success = true;
	LeagueTaskListener listener = null;
	ArrayList<LeaguePlayer> leagueStats = null;
	
	public LeagueFetchTask(LeagueTaskListener l) {
		this.listener = l;
		leagueStats = new ArrayList<LeaguePlayer>();
	}
	
	@Override
	protected void onPreExecute() {
		listener.init();
	}
	
	@Override
	protected ArrayList<LeaguePlayer> doInBackground(ArrayList<LeaguePlayer>... arg0) {
		if(leagueStats == null || leagueStats.size() == 0) {
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
        		//e.printStackTrace();
        		success = false;
        	} 
        	catch (ParserConfigurationException e) {
    			//e.printStackTrace();
    			success = false;
    		} 
        	catch (MalformedURLException e) {
    			//e.printStackTrace();
    			success = false;
    		} 
        	catch (UnknownHostException e) {
        		if(listener != null)
        			listener.recover();
        		//e.printStackTrace();
        		success = false;
        	}
        	catch (IOException e) {
    			//e.printStackTrace();
    			success = false;
    		}
    	}
		return leagueStats;
	}
	
	@Override
	protected void onPostExecute(ArrayList<LeaguePlayer> result) {
		if(listener != null)
			listener.onComplete(success, result);
	}
}