package net.animeimports.calendar;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.calendar.CalendarRequestInitializer;

public class AICalendarManager {
	private static final String TAG = "CalendarSample";
	static final String PREF = TAG;
	static final String PREF_ACCOUNT_NAME = "accountName";
	static final String PREF_AUTH_TOKEN = "authToken";
	static final String PREF_GSESSIONID = "gsessionid";
	GoogleAccountManager accountManager;
	SharedPreferences settings;
	private final HttpTransport transport = AndroidHttp.newCompatibleTransport();
	Context mContext = null;
	private static AICalendarManager manager = null;
	private static CalendarAndroidRequestInitializer calReqInitializer = null;
	private static final int DAYS_IN_FUTURE = 14;
	
	private AICalendarManager(Context context) {
		mContext = context;
		accountManager = new GoogleAccountManager(mContext);
		settings = mContext.getSharedPreferences(PREF, 0);
		calReqInitializer = new CalendarAndroidRequestInitializer();
	}

	public static AICalendarManager getInstance(Context context) {
		if(manager == null)
			manager = new AICalendarManager(context);
		return manager;
	}
	
	/**
	 * Create a request initializer, needed to fetch events via CalendarClient in the main activity
	 */
 	public class CalendarAndroidRequestInitializer extends CalendarRequestInitializer {
		String authToken;
		
		public CalendarAndroidRequestInitializer() {
			super(transport);
			authToken = settings.getString(PREF_AUTH_TOKEN, null);
			setGsessionid(settings.getString(PREF_GSESSIONID, null));
		}
		
		public void intercept(HttpRequest request) throws IOException {
			super.intercept(request);
			request.getHeaders().setAuthorization(GoogleHeaders.getGoogleLoginValue(authToken));
		}

		public boolean handleResponse(HttpRequest request, HttpResponse response, boolean retrySupported) throws IOException {
			switch(response.getStatusCode()) {
				case 302:
					super.handleResponse(request, response, retrySupported);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString(PREF_GSESSIONID, getGsessionid());
					editor.commit();
					return true;
				case 401:
					accountManager.invalidateAuthToken(authToken);
					authToken = null;
					SharedPreferences.Editor editor2 = settings.edit();
					editor2.remove(PREF_AUTH_TOKEN);
					editor2.commit();
					return false;
			}
			return false;
		}
	}

    public Date getStartDate() {
    	Calendar now = Calendar.getInstance();
    	Date startDate = new Date();
		startDate.setYear(now.get(Calendar.YEAR)-1900);
		startDate.setMonth(now.get(Calendar.MONTH));
		startDate.setDate(now.get(Calendar.DATE));
		startDate.setHours(now.get(Calendar.HOUR));
		startDate.setMinutes(now.get(Calendar.MINUTE));
		startDate.setSeconds(now.get(Calendar.SECOND));
		return startDate;
    }
    
    public Date getEndDate() {
    	Calendar now = Calendar.getInstance();
		Date endDate = new Date();
		endDate.setYear(now.get(Calendar.YEAR)-1900);
		endDate.setMonth(now.get(Calendar.MONTH));
		endDate.setDate(now.get(Calendar.DATE) + DAYS_IN_FUTURE);
		endDate.setHours(now.get(Calendar.HOUR));
		endDate.setMinutes(now.get(Calendar.MINUTE));
		endDate.setSeconds(now.get(Calendar.SECOND));
		return endDate;
    }
 	
	/**
	 * @return the calReqInitializer
	 */
	public CalendarAndroidRequestInitializer getCalReqInitializer() {
		return calReqInitializer;
	}

	/**
	 * @param calReqInitializer the calReqInitializer to set
	 */
	public void setCalReqInitializer(CalendarAndroidRequestInitializer calReqInitializer) {
		AICalendarManager.calReqInitializer = calReqInitializer;
	}
}
