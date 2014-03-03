package jp.zx.zheng.cloudstorage.dropbox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.zx.zheng.cloudmusic.MusicPlayer;

import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;

import android.os.AsyncTask;
import android.util.Log;

public class Downloader extends AsyncTask<DbxPath, Integer, List<String>> {

	private static final String TAG = Downloader.class.getName();
	private Dropbox mDropbox;
	private MusicPlayer mMusicPlayer;
	public Downloader(Dropbox dropbox, MusicPlayer musicPlayer) {
		mDropbox = dropbox;
		mMusicPlayer = musicPlayer;
	}
	
	@Override
	protected List<String> doInBackground(DbxPath... paths) {
		
		ArrayList<String> list = new ArrayList<String>();
		
		for (int i = 0; i < paths.length; i++) {
			Log.d(TAG, "Downloading " + paths[i]);
			if(mDropbox.downloadFileAndCache(paths[i]) != null) {
				list.add(paths[i].toString());
				Log.d(TAG, "Download complete " + paths[i]);
			}
		}
		return list;
	}
	
	@Override
	protected void onPostExecute(List<String> list) {
        for(String path : list) {
        	mMusicPlayer.addToQueue(path);
        }
    }

}
