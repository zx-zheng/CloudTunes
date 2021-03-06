package jp.zx.zheng.cloudstorage.dropbox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.zx.zheng.cloudmusic.MediaPlayerService;
import jp.zx.zheng.cloudmusic.MusicPlayer;
import jp.zx.zheng.cloudmusic.MusicTest;
import jp.zx.zheng.cloudmusic.Track;
import jp.zx.zheng.storage.CacheManager;

import com.dropbox.sync.android.DbxException.AlreadyOpen;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class Downloader extends AsyncTask<Track, Track, List<Track>> {

	private static final String TAG = Downloader.class.getName();
	private Dropbox mDropbox;
	private PendingIntent mPi;
	private Context mContext;
	private static HandlerThread mHandlerThread;
	private static Handler mHandler;
	private boolean fileDownloaded = false;
	public Downloader(Dropbox dropbox, PendingIntent pi, Context context) {
		mDropbox = dropbox;
		mPi = pi;
		mContext = context;
		mDropbox.startDownloadTask();
		if(mHandler == null) {
			mHandlerThread = new HandlerThread("handlerThread");
			mHandlerThread.start();
			mHandler = new Handler(mHandlerThread.getLooper());
		}
	}
	
	@Override
	protected List<Track> doInBackground(Track... tracks) {
		
		ArrayList<Track> list = new ArrayList<Track>();
		
		for (int i = 0; i < tracks.length; i++) {
			if(!isCancelled()) {
				Log.d(TAG, "Downloading " + tracks[i]);
				try {
					if(!CacheManager.isCached(tracks[i]) && mDropbox.downloadTrackFileAndCache(tracks[i]) != null) {				
						list.add(tracks[i]);
						fileDownloaded = true;
						tracks[i].insertToDb();
						mHandler.post(new Runnable() {
							
							@Override
							public void run() {
								MusicTest.CloudArtistListFragment.reLoadArtists(mContext);		
							}
						});						
						Log.d(TAG, "Download complete " + tracks[i]);
					}
					tracks[i].isPrepared = true;
				} catch (AlreadyOpen e1) {
					//e1.printStackTrace();
					tracks[i].isPrepared = false;
				}
				try {
					if(MediaPlayerService.isWaitingPrepare()) {
						mPi.send();
						//Log.d(TAG, "send pi");
					}
				} catch (CanceledException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				publishProgress(tracks[i]);
			} else {
				Log.d(TAG, "Download canceled: " + tracks[i]);
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list;
	}
	
	@Override  
    protected void onProgressUpdate(Track... tracks) {
		//cancel後は呼ばれない
		//mMusicPlayer.addToReadyQueue(tracks[0]);
		Log.d(TAG, tracks[0].getName() + " ready");
	}
	
	@Override
	protected void onPostExecute(List<Track> tracks) {
		mDropbox.endDownloadTask();
		if(fileDownloaded) {
			mDropbox.deleteCache();
		}
    }
	
	@Override
    protected void onCancelled(){
		Log.d(TAG, "download task cancelled");
		mDropbox.endDownloadTask();
		mDropbox.deleteCache();
	}

}
