package net.animeimports.android;

import java.util.ArrayList;
import java.util.List;

import net.animeimports.league.LeaguePlayer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class DataManager {
	private static final String DB_NAME = "data_ai.db";
	private static final int DB_VERSION = 1;
	private static final String LEAGUE_TABLE_NAME = "league_stats";
	private static final String EVENTS_TABLE_NAME = "events";
	private Context mContext;
	private SQLiteDatabase db;
	private SQLiteStatement insert_stmt_league;
	private SQLiteStatement insert_stmt_events;
	private static final String INSERT_LEAGUE = "insert into " + LEAGUE_TABLE_NAME + "(name, pointsSession, pointsLifetime) values (?, ?, ?)";
	private static final String INSERT_EVENTS = "insert into " + EVENTS_TABLE_NAME + "(name, date, eventType, mtgFormat, mtgEventType, summary) values (?, ?, ?, ?, ?, ?)";
	private static DataManager dm = null;
	
	public static DataManager getInstance(Context context) {
		if(dm == null)
			dm = new DataManager(context);
		return dm;
	}
	
	private DataManager(Context context) {
		this.mContext = context;
		DataOpenHelper helper = new DataOpenHelper(this.mContext);
		this.db = helper.getWritableDatabase();
		this.insert_stmt_league = this.db.compileStatement(INSERT_LEAGUE);
		this.insert_stmt_events = this.db.compileStatement(INSERT_EVENTS);
	}
	
	public long insertEvents(String name, String date, int eventType, int mtgFormat, int mtgEventType, String summary) {
		this.insert_stmt_events.bindString(1, name);
		this.insert_stmt_events.bindString(2, date);
		this.insert_stmt_events.bindLong(3, eventType);
		this.insert_stmt_events.bindLong(4, mtgFormat);
		this.insert_stmt_events.bindLong(5, mtgEventType);
		this.insert_stmt_events.bindString(6, summary);
		return this.insert_stmt_events.executeInsert();
	}
	
	public long insertLeague(String name, int pointsSession, int pointsLifetime) {
		this.insert_stmt_league.bindString(1, name);
		this.insert_stmt_league.bindLong(2, pointsSession);
		this.insert_stmt_league.bindLong(3, pointsLifetime);
		return this.insert_stmt_league.executeInsert();
	}
	
	// TODO: not sure if this should be public
	public void deleteAllLeague() {
		Log.i("DEBUG", "inside deleteAll");
		this.db.delete(LEAGUE_TABLE_NAME, null, null);
	}
	
	public List<LeaguePlayer> selectAllLeague() {
		List<LeaguePlayer> list = new ArrayList<LeaguePlayer>();
		Cursor cursor = this.db.query(LEAGUE_TABLE_NAME, new String[] {"name", "pointsSession", "pointsLifetime"}, null, null, null, null, "name desc");
		if(cursor.moveToFirst()) {
			while(cursor.moveToNext()) {
				LeaguePlayer p = new LeaguePlayer();
				p.setPlayerName(cursor.getString(0));
				p.setPointsSession(cursor.getInt(1));
				p.setPointsLifetime(cursor.getInt(2));
				list.add(p);
			}
		}
		if(cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return list;
	}
	
	private static class DataOpenHelper extends SQLiteOpenHelper {
		DataOpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.i("DEBUG", "Creating a database, should only be called once!");
			db.execSQL("CREATE TABLE " + LEAGUE_TABLE_NAME + "(id INTEGER PRIMARY KEY, name TEXT, pointsSession INTEGER, pointsLifetime INTEGER)");
			db.execSQL("CREATE TABLE " + EVENTS_TABLE_NAME + "(id INTEGER PRIMARY KEY, name TEXT, date TEXT, eventType INTEGER, mtgFormat INTEGER, mtgEventType INTEGER, summary TEXT)");
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i("DEBUG", "Upgrading db!!!");
			db.execSQL("DROP TABLE IF EXISTS " + LEAGUE_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + EVENTS_TABLE_NAME);
			onCreate(db);
		}
	}
}
