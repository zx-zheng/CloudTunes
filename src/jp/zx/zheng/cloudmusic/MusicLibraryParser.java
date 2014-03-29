package jp.zx.zheng.cloudmusic;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.List;

import jp.zx.zheng.cloudstorage.dropbox.Dropbox;
import jp.zx.zheng.db.MusicLibraryDBAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class MusicLibraryParser extends AsyncTask<String, String, String>{
	
	private static final String TAG = MusicLibraryParser.class.getName();
	private static final String XML_TAG_DICT = "dict";
	private static final String XML_TAG_ARRAY = "array";
	private static final String XML_TAG_KEY = "key";
	private static final String XML_TAG_INTEGER = "integer";
	private static final String XML_NAME_TRACKS = "Tracks";
	private static final String XML_NAME_PLAYLISTS = "Playlists";
	private static final String XML_NAME_NAME = "Name";
	private static final String XML_NAME_ARTIST = "Artist";
	private static final String XML_NAME_ALBUM_ARTIST = "Album Artist";
	private static final String XML_NAME_ALBUM = "Album";
	private static final String XML_NAME_GENRE = "Genre";
	private static final String XML_NAME_DICS_NUMBER = "Disc Number";
	private static final String XML_NAME_DICS_COUNT = "Disc Count";
	private static final String XML_NAME_TRACK_NUMBER = "Track Number";
	private static final String XML_NAME_TRACK_COUNT = "Track Count";
	private static final String XML_NAME_PLAY_COUNT = "Play Count";
	private static final String XML_NAME_YEAR = "Year";
	private static final String XML_NAME_LOCATION = "Location";
	private static final String XML_NAME_MOVIE = "Movie";
	private static final String XML_NAME_MUSIC_VIDEO = "Music Video";
	private static final String XML_NAME_PLAYLIST_ID = "Playlist ID";
	private static final String XML_NAME_TRACK_ID = "Track ID";
	private static final String XML_NAME_PLAYLIST_ITEMS = "Playlist Items";
	private static final String XML_NAME_VISIBLE = "Visible";
	private static final String XML_NAME_MUSIC = "Music";
	private static final String XML_NAME_MOVIES = "Movies";
		
	Activity mActivity;
	String mPath;
	XmlPullParser mXpp;
	MusicLibraryDBAdapter mDbadapter;
	
	public MusicLibraryParser(Context context, InputStream xmlFile) {
		//mActivity = context;
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			BufferedInputStream bufferdXml = new BufferedInputStream(xmlFile);
			mXpp = factory.newPullParser();
			mXpp.setInput(bufferdXml, "UTF-8");
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		mDbadapter = MusicLibraryDBAdapter.instance;
	}
	
	public MusicLibraryParser(Activity activity, String path) {
		mActivity = activity;
		mPath = path;
		mDbadapter = MusicLibraryDBAdapter.instance;
	}
	
	private boolean downloadAndOpenXml(String path) {
		FileInputStream xmlFile = Dropbox.getInstance(mActivity).downloadFile(path);
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			BufferedInputStream bufferdXml = new BufferedInputStream(xmlFile);
			mXpp = factory.newPullParser();
			mXpp.setInput(bufferdXml, "UTF-8");
			return true;
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void parse(){
		if (mXpp == null) {
			Log.e(TAG, "Xml paser is null");
			return;
		}
		mDbadapter.open();
		mDbadapter.truncate();
		Log.i(TAG, "start parse");
		try {
			int eventType = mXpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					if (mXpp.getName().equals(XML_TAG_DICT)) {
						parseMainDict();
					}
				}
				eventType = mXpp.next();
			}
			Log.i(TAG, "end parse xml");
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void parseMainDict() throws XmlPullParserException, IOException {
		Log.i(TAG, "start parse main dict");
		int eventType = mXpp.next();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				String tagText = mXpp.nextText();
				if (tagText.equals(XML_NAME_TRACKS)){
					//</key>				
					mXpp.next();
					parseTracks();
				} else if (tagText.equals(XML_NAME_PLAYLISTS)) {
					Log.i(TAG, "start parse playlists");
					parsePlaylists();
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				String tagName = mXpp.getName();
				if(tagName.equals(XML_TAG_DICT)) {
					break;
				}
			}
			eventType = mXpp.next();
		}
	}
	
	private void parseTracks() throws XmlPullParserException, IOException {
		mActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(mActivity, "Loading Tracks", Toast.LENGTH_SHORT).show();	
			}
		});
		Log.i(TAG, "start parse tracks");
		//<dict>
		int eventType = mXpp.next();
		while (true) {
			eventType = mXpp.nextTag();
			if(eventType == XmlPullParser.END_TAG && mXpp.getName().equals(XML_TAG_DICT)) {
				Log.i(TAG, "end parse tracks");
				break;
			}
			parseTrack();
		}
	}
	
	private void parseTrack() throws XmlPullParserException, IOException {
		//<key>
		String key = mXpp.nextText();
		//Log.i(TAG, key);
		//<dict>
		mXpp.nextTag();
		int id = Integer.parseInt(key);
		String name = null, artist = null, albumArtist = null, album = null, genre = null, location = null;
		int discNumber = 0, discCount = 0, trackNumber = 0, trackCount = 0, playCount = 0, year = 0;
		boolean isMovie = false;
		int eventType;
		while (true) {
			//<key>
			eventType = mXpp.nextTag();
			if(eventType == XmlPullParser.END_TAG && mXpp.getName().equals(XML_TAG_DICT)) {
				break;
			}
			String tagText = mXpp.nextText();
			mXpp.nextTag();
			if(tagText.equals(XML_NAME_NAME)) {
				name = mXpp.nextText();
				//Log.d(TAG, name);
			} else if(tagText.equals(XML_NAME_ARTIST)) {
				artist = mXpp.nextText();
			} else if(tagText.equals(XML_NAME_ALBUM_ARTIST)) {
				albumArtist = mXpp.nextText();
			} else if(tagText.equals(XML_NAME_ALBUM)) {
				album = mXpp.nextText();
			} else if(tagText.equals(XML_NAME_GENRE)) {
				genre = mXpp.nextText();
			} else if(tagText.equals(XML_NAME_DICS_NUMBER)) {
				discNumber = Integer.parseInt(mXpp.nextText());
			} else if(tagText.equals(XML_NAME_DICS_COUNT)) {
				discCount = Integer.parseInt(mXpp.nextText());
			} else if(tagText.equals(XML_NAME_TRACK_NUMBER)) {
				trackNumber = Integer.parseInt(mXpp.nextText());
			} else if(tagText.equals(XML_NAME_TRACK_COUNT)) {
				trackCount = Integer.parseInt(mXpp.nextText());
			} else if(tagText.equals(XML_NAME_PLAY_COUNT)) {
				playCount = Integer.parseInt(mXpp.nextText());
			} else if(tagText.equals(XML_NAME_YEAR)) {
				year = Integer.parseInt(mXpp.nextText());
			} else if(tagText.equals(XML_NAME_LOCATION)) {
				//"+"がスペースにデコードされるため置換をおこなう
				location = URLDecoder.decode(mXpp.nextText().replaceAll("\\+", "%2B"), "UTF-8");
			} else if(tagText.equals(XML_NAME_MOVIE) || tagText.equals(XML_NAME_MUSIC_VIDEO)) {
				mXpp.nextText();
				isMovie = true;
			} else {
				mXpp.nextText();
			}
		}
		if(!isMovie) {
			long result = mDbadapter.insertTrack(id, name, artist, albumArtist, album, genre, 
					discNumber, discCount, trackNumber, trackCount, playCount, year, location);
			//Log.d(TAG, Long.toString(result));
		}
	}
	
	private void parsePlaylists() throws XmlPullParserException, IOException {
		mActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(mActivity, "Loading Playlists", Toast.LENGTH_SHORT).show();		
			}
		});
		//<array>
		int eventType = mXpp.nextTag();
		Log.d(TAG, mXpp.getName());
		while(true) {
			//<dict> or </dict>
			eventType = mXpp.nextTag();
			Log.d(TAG, mXpp.getName());
			if(eventType == XmlPullParser.END_TAG && mXpp.getName().equals(XML_TAG_ARRAY)) {
				Log.i(TAG, "end parse playlists");
				break;
			}
			parsePlaylist();
		}
	}
	
	private void parsePlaylist() throws XmlPullParserException, IOException {
		String name = null;
		int playlistID = 0;
		boolean visible = true;
		int eventType;
		while(true) {
			//start from <key>
			eventType = mXpp.nextTag();
			if(eventType == XmlPullParser.END_TAG && mXpp.getName().equals(XML_TAG_DICT)) {
				break;
			}
			String tagText = mXpp.nextText();
			mXpp.nextTag();
			if(tagText.equals(XML_NAME_NAME)) {
				name = mXpp.nextText();
				Log.d(TAG, name);
			} else if(tagText.equals(XML_NAME_PLAYLIST_ID)) {
				playlistID = Integer.parseInt(mXpp.nextText());
				Log.d(TAG, Integer.toString(playlistID));
			} else if(tagText.equals(XML_NAME_VISIBLE)) {
				visible = Boolean.parseBoolean(mXpp.getName());
				mXpp.nextText();
			} else if(tagText.equals(XML_NAME_MUSIC)) {
				visible = !Boolean.parseBoolean(mXpp.getName());
				mXpp.nextText();
			} else if(tagText.equals(XML_NAME_MOVIES)) {
				visible = !Boolean.parseBoolean(mXpp.getName());
				mXpp.nextText();
			} else if(tagText.equals(XML_NAME_PLAYLIST_ITEMS)) {
				parsePlaylistTracks(playlistID, visible);
			} else {
				mXpp.nextText();
			}
		}
		mDbadapter.insertPlaylistName(playlistID, name);
	}
	
	private void parsePlaylistTracks(int playlistID, boolean visble) throws XmlPullParserException, IOException {
		int eventType;
		if(visble) {
			while(true) {
				int trackID = 0;
				eventType = mXpp.nextTag();
				if(eventType == XmlPullParser.END_TAG && mXpp.getName().equals(XML_TAG_ARRAY)) {
					break;
				}
				mXpp.nextTag();
				mXpp.next();
				mXpp.nextTag();
				mXpp.nextTag();
				trackID = Integer.parseInt(mXpp.nextText());
				//Log.d(TAG, Integer.toString(trackID));
				mDbadapter.insertTrackToPlaylist(playlistID, trackID);
				mXpp.nextTag();
			}
		} else {
			Log.d(TAG, "invisible playlist");
			while(true) {
				eventType = mXpp.next();
				if(eventType == XmlPullParser.END_TAG && mXpp.getName().equals(XML_TAG_ARRAY)) {
					break;
				}
			}
		}
	}

	@Override
	protected String doInBackground(String... params) {
		mActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(mActivity, "Updating iTunes library", Toast.LENGTH_SHORT).show();	
			}
		});
		if(downloadAndOpenXml(mPath)) {
			parse();
		} else {
			//Toast.makeText(mContext, "Failed to Update iTunes library", Toast.LENGTH_SHORT).show();
		}
		return null;
	}
	
	@Override  
    protected void onProgressUpdate(String... progress) {
	
	}
	
	@Override
	protected void onPostExecute(String result) {
        MusicTest.PlayListFragment.reloadPlayList(mActivity);
    }
}
