package net.animeimports.news;

import java.util.Date;

import android.text.format.DateFormat;

public class AINewsItem {
	private String item;
	private Date date;
	
	/**
	 * @return the item
	 */
	public String getItem() {
		return item;
	}
	/**
	 * @param item the item to set
	 */
	public void setItem(String item) {
		this.item = item;
	}
	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}
	/**
	 * @param date the date to set
	 */
	public void setDate(String date) {
		Date d = new Date(date);
		System.out.println("Date is " + d);
		this.date = d;
	}
}
