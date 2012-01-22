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
	private static final String DBNAME = "data.db";
	private static final int DBVERSION = 1;
	private static final String TABLENAME = "league_stats";
	private Context mContext;
	private SQLiteDatabase db;
	private SQLiteStatement insertStmt;
	private static final String INSERT = "insert into " + TABLENAME + "(name, pointsSession, pointsLifetime) values (?, ?, ?)";
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
		this.insertStmt = this.db.compileStatement(INSERT);
	}
	
	public long insert(String name, int pointsSession, int pointsLifetime) {
		this.insertStmt.bindString(1, name);
		this.insertStmt.bindLong(2, pointsSession);
		this.insertStmt.bindLong(3, pointsLifetime);
		return this.insertStmt.executeInsert();
	}
	
	// TODO: not sure if this should be public
	public void deleteAll() {
		Log.i("DEBUG", "inside deleteAll");
		this.db.delete(TABLENAME, null, null);
	}
	
	public List<LeaguePlayer> selectAll() {
		List<LeaguePlayer> list = new ArrayList<LeaguePlayer>();
		Cursor cursor = this.db.query(TABLENAME, new String[] {"name", "pointsSession", "pointsLifetime"}, null, null, null, null, "name desc");
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
			super(context, DBNAME, null, DBVERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.i("DEBUG", "Creating a database, should only be called once!");
			db.execSQL("CREATE TABLE " + TABLENAME + "(id INTEGER PRIMARY KEY, name TEXT, pointsSession INTEGER, pointsLifetime INTEGER)");
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i("DEBUG", "Upgrading db!!!");
			db.execSQL("DROP TABLE IF EXISTS " + TABLENAME);
			onCreate(db);
		}
	}
}
