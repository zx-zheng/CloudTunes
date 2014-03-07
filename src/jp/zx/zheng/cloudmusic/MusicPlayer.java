package jp.zx.zheng.cloudmusic;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import jp.zx.zheng.cloudstorage.dropbox.Downloader;
import jp.zx.zheng.cloudstorage.dropbox.Dropbox;
import jp.zx.zheng.storage.CacheManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.sax.StartElementListener;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.dropbox.sync.android.DbxPath;

public class MusicPlayer {
	
	private static final String TAG = MusicPlayer.class.getName();
	
	private static MusicPlayer mMusicPlayer;
	private SeekBar mSeekbar;
	private ImageView mAlbumArtView;
	
	private Dropbox mDropbox;
	private Downloader mDownloader;
	private MediaPlayerService mBoundMPservice;
	private ServiceConnection mConnection;
	private List<Track> mPlayList;
	private int currentPos;
	private Queue<Track> mReadyQueue;
	private MediaMetadataRetriever mMediaMetaData;
	private FileInputStream currentPlayingFile;
	private Handler mHandler;
	private boolean isPlaying = false;
	
	private Runnable mSeekBarSetter = new Runnable() {
		
		@Override
		public void run() {
			mSeekbar.setMax(mBoundMPservice.getDuration());
			if (mSeekbar != null) {
				mSeekbar.setProgress(mBoundMPservice.getCurrentPosition());
			}
			if (isPlaying) {
				mHandler.postDelayed(this, 500);
			}
		}
	};
	
	private OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {
		
		int progress;
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			mBoundMPservice.seekTo(progress);
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if(fromUser) {
				this.progress = progress;
			}
		}
	};
	
	public static MusicPlayer getInstance(Context context) {
		if(mMusicPlayer == null) {
			mMusicPlayer = new MusicPlayer(context);
		}
		return mMusicPlayer;
	}

	private MusicPlayer(Context context) {
		mDropbox = new Dropbox(context);
		mDownloader = new Downloader(mDropbox, this);
		mPlayList = new ArrayList<Track>();
		mReadyQueue = new LinkedList<Track>();
		mHandler = new Handler();
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
	
	public void setAlbumArtView(ImageView view) {
		mAlbumArtView = view;
	}
	
	public void setAlbumArt() {
		byte[] albumArtData = getAlbumArt();
		setAlbumArt(albumArtData);
	}
	
	public void setAlbumArt(byte[] albumArtData) {
		if (albumArtData != null) {
			mAlbumArtView.setImageBitmap(BitmapFactory.decodeByteArray(albumArtData, 0, albumArtData.length));
		}
	}
	
	public void setSeekBar(SeekBar seekBar) {
		mSeekbar = seekBar;
		mSeekbar.setOnSeekBarChangeListener(mSeekBarChangeListener);
	}

	public void playMusic(Track track) {
		FileInputStream file;
		if (!CacheManager.isCached(track)){
			//mDownloader.execute(track);
			return;
			//file = mDropbox.downloadFileAndCache(dbxPath);
			//Log.d("Main", "file cached");
		} else {
			file = CacheManager.getCacheFile(track);
			Log.d("Main", "read cache");
		}
		currentPlayingFile = file;
		mBoundMPservice.play(file);
		setAlbumArt();
		isPlaying = true;
		mHandler.postDelayed(mSeekBarSetter, 500);
	}
	
	public void playNextTrack() {
		isPlaying = false;
		currentPos++;
		if(currentPos == mPlayList.size()) {
			currentPos = 0;
		}
		if(mReadyQueue.contains(mPlayList.get(currentPos))) {
			playMusic(mPlayList.get(currentPos));
		}
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
	
	public void addToList(Track track) {
		mPlayList.add(track);
	}
	
	public void addToList(List<Track> trackList, int startPos) {
		mPlayList.clear();
		mPlayList.addAll(trackList);
		currentPos = startPos;
		preparePlayList();
	}
	
	private void preparePlayList() {
		mDownloader.execute((Track[])mPlayList.toArray(new Track[]{}));
	}
	
	public void addToReadyQueue(Track track) {
		mReadyQueue.offer(track);
		if (mReadyQueue.contains(mPlayList.get(currentPos)) && !isPlaying) {
			playMusic(mPlayList.get(currentPos));
		}
	}
	
	public void clearQuere() {
		mPlayList.clear();
	}
	
}
