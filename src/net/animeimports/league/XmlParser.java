package net.animeimports.league;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlParser extends DefaultHandler {
	Boolean currentElement = false;
	String currentValue = null;
	public static ArrayList<LeaguePlayer> stats = null;
	LeaguePlayer player = null;
	Boolean dataBegin = false;

	public XmlParser() {
		//this.stats = new ArrayList<LeaguePlayer>();
	}
	
	/**
	 * @return the stats
	 */
	public static ArrayList<LeaguePlayer> getStats() {
		return stats;
	}

	/**
	 * @param stats the stats to set
	 */
	public static void setStats(ArrayList<LeaguePlayer> stats) {
		XmlParser.stats = stats;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		currentElement = true;
		if(localName.equalsIgnoreCase("league")) {
			stats = new ArrayList<LeaguePlayer>();
		}
		else if(localName.equalsIgnoreCase("cd")) {
			player = new LeaguePlayer();
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		currentElement = false;
		
		if(localName.equalsIgnoreCase("cd") && !dataBegin) {
			dataBegin = true;
		}
		else if (dataBegin) {
			if(localName.equalsIgnoreCase("name")) {
				player.setPlayerName(currentValue);
			}
			else if(localName.equalsIgnoreCase("win")) {
				player.setWins(Integer.parseInt(currentValue));
			}
			else if(localName.equalsIgnoreCase("lose")) {
				player.setLosses(Integer.parseInt(currentValue));
			}
			else if(localName.equalsIgnoreCase("tpoint")) {
				player.setPointsLifetime(Integer.parseInt(currentValue));
			}
			else if(localName.equalsIgnoreCase("spoint")) {
				player.setPointsSession(Integer.parseInt(currentValue));
			}
			else if(localName.equalsIgnoreCase("cd")) {
				stats.add(player);
			}
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if(currentElement) {
			currentValue = new String(ch, start, length);
			currentElement = false;
		}
	}
}
