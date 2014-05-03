package jp.zx.zheng.db;

import java.util.List;

import jp.zx.zheng.cloudmusic.MusicLibraryParser;
import jp.zx.zheng.cloudmusic.Playlist;
import jp.zx.zheng.cloudmusic.Track;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MusicLibraryDBAdapter {
	
	private static final String TAG = MusicLibraryDBAdapter.class.getName();
	private SQLiteDatabase mDb;
	private MusicLibraryDBHelper mHelper;
	
	public static MusicLibraryDBAdapter instance;
	
	public static void init(Context context) {
		instance = new MusicLibraryDBAdapter(context);
		instance.open();
	}
	
	private MusicLibraryDBAdapter(Context context) {
		mHelper = new MusicLibraryDBHelper(context);
	}
	
	public void open() {
		mDb = mHelper.getWritableDatabase();
	}
	
	public void close() {
		if(mDb == null) return;
		mDb.close();
	}
	
	public void beginTransaction() {
		mDb.beginTransaction();
	}
	
	public void commitAndEndTransaction() {
		mDb.setTransactionSuccessful();
		mDb.endTransaction();
	}
	
	public long insertTrack(int id, String name, String artist,
			String albumArtist, String album, String genre, int discNumber, int discCount,
			int trackNumber, int trackCount, int playCount, int year, String location) {
		return mHelper.insertTrack(mDb, id, name, artist, albumArtist, album, genre, discNumber, 
				discCount, trackNumber, trackCount, playCount, year, location);
	}
	
	public long insertTrackToCloudLibrary(String name, String artist,
			String albumArtist, String album, String genre, int discNumber, int discCount,
			int trackNumber, int trackCount, int playCount, int year, String location) {
		return mHelper.insertTrackToCloudLibrary(mDb, name, artist, albumArtist, album, genre, discNumber, 
				discCount, trackNumber, trackCount, playCount, year, location);
	}
	
	public long insertTrackToPlaylist(int playlistId, int trackId) {
		return mHelper.insertTrackToPlaylist(mDb, playlistId, trackId);
	}
	
	public long insertPlaylistName(int playlistId, String name) {
		return mHelper.insertPlaylistName(mDb, playlistId, name);
	}
	
	public void selectTracks() {
		mHelper.selectTracks(mDb);
	}
	
	public List<String> listAlbumArtists() {
		return mHelper.listAlbumArtists(mDb);
	}
	
	public List<String> listAlbumArtists(String table) {
		return mHelper.listAlbumArtists(mDb, table);
	}
	
	public List<String> listAlbum(String albumArtist) {
		return mHelper.listAlbum(mDb, albumArtist);
	}
	
	public List<String> listAlbum(String table, String albumArtist) {
		return mHelper.listAlbum(mDb, table, albumArtist);
	}
	
	public List<Track> listAlbumTracks(String albumArtist, String album) {
		return mHelper.listAlbumTracks(mDb, albumArtist, album);
	}
	
	public List<Track> listAlbumTracks(String table, String albumArtist, String album) {
		return mHelper.listAlbumTracks(mDb, table, albumArtist, album);
	}
	
	public List<Playlist> listPlaylist() {
		return mHelper.listPlaylist(mDb);
	}
	
	public List<Track> listPlaylistTracks(int playlistId) {
		return mHelper.listPlaylistTracks(mDb, playlistId);
	}
	
	public void truncate() {
		mHelper.truncate(mDb);
	}
}
