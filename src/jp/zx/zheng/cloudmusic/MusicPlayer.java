package jp.zx.zheng.cloudmusic;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import jp.zx.zheng.cloudstorage.dropbox.Downloader;
import jp.zx.zheng.cloudstorage.dropbox.Dropbox;
import jp.zx.zheng.storage.CacheManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.sax.StartElementListener;
import android.util.Log;

import com.dropbox.sync.android.DbxPath;

public class MusicPlayer {
	
	private static final String TAG = MusicPlayer.class.getName();
	
	private static MusicPlayer mMusicPlayer;
	
	private Dropbox mDropbox;
	private Downloader mDownloader;
	private MediaPlayerService mBoundMPservice;
	private ServiceConnection mConnection;
	private Queue<String> mQueue;
	private MediaMetadataRetriever mMediaMetaData;
	private FileInputStream currentPlayingFile;
	private boolean isPlaying = false;
	
	public static MusicPlayer getInstance(Context context) {
		if(mMusicPlayer == null) {
			mMusicPlayer = new MusicPlayer(context);
		}
		return mMusicPlayer;
	}

	private MusicPlayer(Context context) {
		mDropbox = new Dropbox(context);
		mDownloader = new Downloader(mDropbox, this);
		mQueue = new LinkedList<String>();
		mMediaMetaData = new MediaMetadataRetriever();
		mConnection = new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				
				
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mBoundMPservice = ((MediaPlayerService.MediaPlayerBinder)service).getService();
			}
		};
		context.bindService(new Intent(context, MediaPlayerService.class), 
				mConnection, Context.BIND_AUTO_CREATE);
	}

	public void playMusic(String path) {
		DbxPath dbxPath = pathTodbxPath(path);
		FileInputStream file;
		if (!CacheManager.isCached(dbxPath.toString())){
			mDownloader.execute(dbxPath);
			return;
			//file = mDropbox.downloadFileAndCache(dbxPath);
			//Log.d("Main", "file cached");
		} else {
			file = CacheManager.getCacheFile(dbxPath.toString());
			Log.d("Main", "read cache");
		}
		currentPlayingFile = file;
		mBoundMPservice.play(file);
	}
	
	public void start() {
		mBoundMPservice.start();
	}
	
	public void pause() {
		mBoundMPservice.pause();
	}
	
	public byte[] getAlbumArt() {
		try {
			mMediaMetaData.setDataSource(currentPlayingFile.getFD());
			return mMediaMetaData.getEmbeddedPicture();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void addToQueue(String path) {
		mQueue.offer(path);
		if (!isPlaying) {
			playMusic(mQueue.poll());
		}
	}
	
	private DbxPath pathTodbxPath(String path) {
		String relativePath = path.substring(path.indexOf("iTunes") - 1);
		Log.d(TAG, relativePath);
		return new DbxPath(relativePath);
	}
}
