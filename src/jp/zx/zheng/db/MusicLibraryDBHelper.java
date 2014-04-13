package jp.zx.zheng.db;

import java.util.ArrayList;
import java.util.List;

import jp.zx.zheng.cloudmusic.Playlist;
import jp.zx.zheng.cloudmusic.Track;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.ListView;

public class MusicLibraryDBHelper extends SQLiteOpenHelper {

	private static final String TAG = MusicLibraryDBHelper.class.getName();
	static final String DATABASE_NAME = "musicLibrary.db";
	static final int DATABASE_VERSION = 1;
	
	public static final String TABLE_TRACKS_NAME = "tracks";
	public static final String TABLE_PLAYLISTS_NAME = "playlists";
	public static final String TABLE_PLAYLIST_NAMES_NAME = "playlist_names";
	public static final String COL_ID = "_id";
	public static final String COL_NAME = "name";
	public static final String COL_ARTIST = "artist";
	public static final String COL_ALBUM_ARTIST = "album_artist";
	public static final String COL_ALBUM = "album";
	public static final String COL_GENRE = "genre";
	public static final String COL_DICS_NUMBER = "disc_number";
	public static final String COL_DICS_COUNT = "disc_count";
	public static final String COL_TRACK_NUMBER = "track_number";
	public static final String COL_TRACK_COUNT = "track_count";
	public static final String COL_PLAY_COUNT = "play_count";
	public static final String COL_YEAR = "year";
	public static final String COL_LOCATION = "location";
	public static final String COL_TRACK_ID = "track_id";
	public static final String COL_PLAYLIST_NAME = "playlist_id";
		  
	public MusicLibraryDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "db on create");
		db.execSQL(
				"CREATE TABLE " + TABLE_TRACKS_NAME + " ("
						+ COL_ID + " INTEGER PRIMARY KEY,"
						+ COL_NAME + " TEXT NOT NULL,"
						+ COL_ARTIST + " TEXT,"
						+ COL_ALBUM_ARTIST + " TEXT,"
						+ COL_ALBUM + " TEXT,"
						+ COL_GENRE + " TEXT,"
						+ COL_DICS_NUMBER + " INTEGER,"
						+ COL_DICS_COUNT + " INTEGER,"
						+ COL_TRACK_NUMBER + " INTEGER,"
						+ COL_TRACK_COUNT + " INTEGER,"
						+ COL_PLAY_COUNT + " INTEGER,"
						+ COL_YEAR + " INTEGER,"
						+ COL_LOCATION + " TEXT NOT NULL"
						+ ");");
		
		db.execSQL(
				"CREATE TABLE " + TABLE_PLAYLISTS_NAME + " ("
						+ COL_ID + " INTEGER,"
						+ COL_TRACK_ID + " INTEGER"
						+ ");");
		
		db.execSQL(
				"CREATE TABLE " + TABLE_PLAYLIST_NAMES_NAME + " ("
						+ COL_ID + " INTEGER PRIMARY KEY,"
						+ COL_PLAYLIST_NAME + " TEXT NOT NULL"
						+ ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	
	public long insertTrack(SQLiteDatabase db, int id, String name, String artist,
			String albumArtist, String album, String genre, int discNumber, int discCount,
			int trackNumber, int trackCount, int playCount, int year, String location) {
		ContentValues values = new ContentValues();
		values.put(COL_ID, id);
		values.put(COL_NAME, name);
		if(artist == null) artist = "no artist"; 
		values.put(COL_ARTIST, artist);
		if(albumArtist == null) albumArtist = artist;
		values.put(COL_ALBUM_ARTIST, albumArtist);
		if(album != null) values.put(COL_ALBUM, album);
		if(genre != null) values.put(COL_GENRE, genre);
		values.put(COL_DICS_NUMBER, discNumber);
		values.put(COL_DICS_COUNT, discCount);
		values.put(COL_TRACK_NUMBER, trackNumber);
		values.put(COL_TRACK_COUNT, trackCount);
		values.put(COL_PLAY_COUNT, playCount);
		values.put(COL_YEAR, year);
		values.put(COL_LOCATION, location);
		//Log.d(TAG, "insert track " + id);
		return db.insert(TABLE_TRACKS_NAME, "null", values);
	}
	
	public long insertTrackToPlaylist(SQLiteDatabase db, int playlistId, int trackId) {
		ContentValues values = new ContentValues();
		values.put(COL_ID, playlistId);
		values.put(COL_TRACK_ID, trackId);
		return db.insert(TABLE_PLAYLISTS_NAME, "null", values);
	}
	
	public long insertPlaylistName(SQLiteDatabase db, int playlistId, String name) {
		ContentValues values = new ContentValues();
		values.put(COL_ID, playlistId);
		values.put(COL_PLAYLIST_NAME, name);
		return db.insert(TABLE_PLAYLIST_NAMES_NAME, "null", values);
	}
	
	public List<Track> selectTracks(SQLiteDatabase db) {
		Cursor cursor = db.query(TABLE_TRACKS_NAME, null, null, null, null, null, null, "100");
		while(cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex(COL_NAME));
			Log.d(TAG, name);
		}
		cursor.close();
		
		return null;
	}
	
	public List<String> listAlbumArtists(SQLiteDatabase db) {
		List<String> artistList = new ArrayList<String>();
		String[] columns = {COL_ALBUM_ARTIST};
		Cursor cursor = db.query(true, TABLE_TRACKS_NAME, columns, 
				null, null, null, null, COL_ALBUM_ARTIST, null);
		while(cursor.moveToNext()) {
			String albumArtist = cursor.getString(cursor.getColumnIndex(COL_ALBUM_ARTIST));
			artistList.add(albumArtist);
			//Log.d(TAG, "artist:" + albumArtist);
		}
		return artistList;
	}
	
	public List<String> listAlbum(SQLiteDatabase db, String albumArtist) {
		List<String> albumList = new ArrayList<String>();
		String[] columns = {COL_ALBUM};
		Cursor cursor = db.query(true, TABLE_TRACKS_NAME, columns, 
				COL_ALBUM_ARTIST + " = ?",
				new String[]{albumArtist}, null, null, COL_ALBUM, null);
		
		while(cursor.moveToNext()) {
			String album = cursor.getString(cursor.getColumnIndex(COL_ALBUM));
			if(album != null) {
				albumList.add(album);
				//Log.d(TAG, "album:" + album);
			}
		}
		return albumList;
	}
	
	public List<Track> listAlbumTracks(SQLiteDatabase db, String albumArtist, String album) {
		List<Track> trackList = new ArrayList<Track>();
		String[] columns = {COL_ID, COL_NAME, COL_ARTIST, COL_LOCATION, COL_DICS_NUMBER, COL_TRACK_NUMBER};
		Cursor cursor = db.query(true, TABLE_TRACKS_NAME, columns, 
				COL_ALBUM_ARTIST + " = ? and " + COL_ALBUM +" = ?",
				new String[]{albumArtist, album}, null, null, 
				COL_DICS_NUMBER + "," + COL_TRACK_NUMBER, null);
		
		while(cursor.moveToNext()) {
			int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
			String name = cursor.getString(cursor.getColumnIndex(COL_NAME));
			String artist = cursor.getString(cursor.getColumnIndex(COL_ARTIST));
			String location = cursor.getString(cursor.getColumnIndex(COL_LOCATION));
			if(album != null) {
				trackList.add(new Track(id, name, artist, albumArtist, album, location));
				//Log.d(TAG, "album:" + album);
			}
		}
		return trackList;
	}
	
	public List<Playlist> listPlaylist(SQLiteDatabase db) {
		List<Playlist> playlists = new ArrayList<Playlist>();
		String[] columns = {COL_ID, COL_PLAYLIST_NAME};
		Cursor cursor = db.query(true, TABLE_PLAYLIST_NAMES_NAME, columns, 
				null, null, null, null, COL_ID, null);
		while(cursor.moveToNext()) {
			int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
			String name = cursor.getString(cursor.getColumnIndex(COL_PLAYLIST_NAME));
			playlists.add(new Playlist(id, name));
			//Log.d(TAG, "artist:" + albumArtist);
		}
		return playlists;
	}
	
	public List<Track> listPlaylistTracks(SQLiteDatabase db, int playlistId) {
		List<Track> trackList = new ArrayList<Track>();
		String sql = "SELECT * FROM " + TABLE_PLAYLISTS_NAME + " join "
				+ TABLE_TRACKS_NAME + " on " + TABLE_PLAYLISTS_NAME + "." + COL_TRACK_ID
				+ " = " + TABLE_TRACKS_NAME + "." + COL_ID + " where " + TABLE_PLAYLISTS_NAME
				+ "." + COL_ID + " = ?;"; 
		Log.d(TAG, sql);
		Cursor cursor = db.rawQuery(sql, new String[]{Integer.toString(playlistId)});
		while(cursor.moveToNext()) {
			int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
			String name = cursor.getString(cursor.getColumnIndex(COL_NAME));
			String artist = cursor.getString(cursor.getColumnIndex(COL_ARTIST));
			String albumArtist = cursor.getString(cursor.getColumnIndex(COL_ALBUM_ARTIST));
			String album = cursor.getString(cursor.getColumnIndex(COL_ALBUM));
			String location = cursor.getString(cursor.getColumnIndex(COL_LOCATION));
			trackList.add(new Track(id, name, artist, albumArtist, album, location));
		}
		return trackList;
	}

	public void truncate(SQLiteDatabase db) {
		db.delete(TABLE_TRACKS_NAME, null, null);
		db.delete(TABLE_PLAYLIST_NAMES_NAME, null, null);
		db.delete(TABLE_PLAYLISTS_NAME, null, null);
	}
}
