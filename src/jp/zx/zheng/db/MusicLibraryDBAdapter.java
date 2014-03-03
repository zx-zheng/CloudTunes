package jp.zx.zheng.db;

import java.util.List;

import jp.zx.zheng.cloudmusic.MusicLibraryParser;
import jp.zx.zheng.cloudmusic.Track;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MusicLibraryDBAdapter {
	
	private static final String TAG = MusicLibraryDBAdapter.class.getName();
	private SQLiteDatabase mDb;
	private MusicLibraryDBHelper mHelper;
	
	public MusicLibraryDBAdapter(Context context) {
		mHelper = new MusicLibraryDBHelper(context);
	}
	
	public void open() {
		mDb = mHelper.getWritableDatabase();
	}
	
	public void close() {
		if(mDb == null) return;
		mDb.close();
	}
	
	public long insertTrack(int id, String name, String artist,
			String albumArtist, String album, String genre, int discNumber, int discCount,
			int trackNumber, int trackCount, int playCount, int year, String location) {
		return mHelper.insertTrack(mDb, id, name, artist, albumArtist, album, genre, discNumber, 
				discCount, trackNumber, trackCount, playCount, year, location);
	}
	
	public void selectTracks() {
		mHelper.selectTracks(mDb);
	}
	
	public List<String> listAlbumArtists() {
		return mHelper.listAlbumArtists(mDb);
	}
	
	public List<String> listAlbum(String albumArtist) {
		return mHelper.listAlbum(mDb, albumArtist);
	}
	
	public List<Track> listAlbumTracks(String albumArtist, String album) {
		return mHelper.listAlbumTracks(mDb, albumArtist, album);
	}
	
	public void truncate() {
		mHelper.truncate(mDb);
	}
}
