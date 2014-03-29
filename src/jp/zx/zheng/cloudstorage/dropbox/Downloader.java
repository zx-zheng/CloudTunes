package jp.zx.zheng.cloudstorage.dropbox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.zx.zheng.cloudmusic.MusicPlayer;
import jp.zx.zheng.cloudmusic.Track;
import jp.zx.zheng.storage.CacheManager;

import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;

import android.os.AsyncTask;
import android.util.Log;

public class Downloader extends AsyncTask<Track, Track, List<Track>> {

	private static final String TAG = Downloader.class.getName();
	private Dropbox mDropbox;
	private MusicPlayer mMusicPlayer;
	public Downloader(Dropbox dropbox, MusicPlayer musicPlayer) {
		mDropbox = dropbox;
		mMusicPlayer = musicPlayer;
	}
	
	@Override
	protected List<Track> doInBackground(Track... tracks) {
		
		ArrayList<Track> list = new ArrayList<Track>();
		
		for (int i = 0; i < tracks.length; i++) {
			Log.d(TAG, "Downloading " + tracks[i]);
			if(!CacheManager.isCached(tracks[i]) && mDropbox.downloadTrackFileAndCache(tracks[i]) != null) {
				list.add(tracks[i]);
				Log.d(TAG, "Download complete " + tracks[i]);
			}
			publishProgress(tracks[i]);
		}
		return list;
	}
	
	@Override  
    protected void onProgressUpdate(Track... tracks) {
		Log.d(TAG, tracks[0].getName() + " ready");
		mMusicPlayer.addToReadyQueue(tracks[0]);
	}
	
	@Override
	protected void onPostExecute(List<Track> tracks) {
        
    }

}
