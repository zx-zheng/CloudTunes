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
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.sax.StartElementListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.dropbox.sync.android.DbxPath;

public class MusicPlayer {
	
	private static final String TAG = MusicPlayer.class.getName();
	
	private static MusicPlayer mMusicPlayer;
	private SeekBar mSeekbar;
	private ImageView[] mAlbumArtViews;
	private TextView[] mAlbumArtTexts;
	private ToggleButton[] mPlayButtons;
	private TextView mTrackNameLabel;
	private TextView mArtistNameLabel;
	
	private Dropbox mDropbox;
	private AsyncTask<Track, Track, List<Track>> mDownloader;
	private MediaPlayerService mBoundMPservice;
	private ServiceConnection mConnection;
	private List<Track> mPlayList;
	private int currentPos;
	private Queue<Track> mReadyQueue;
	private MediaMetadataRetriever mMediaMetaData;
	private FileInputStream currentPlayingFile;
	private Handler mHandler;
	private boolean isInPlayingState = false;
	private boolean isPlayingMusic = false;
	
	private Runnable mSeekBarSetter = new Runnable() {
		
		@Override
		public void run() {
			mSeekbar.setMax(mBoundMPservice.getDuration());
			if (mSeekbar != null) {
				mSeekbar.setProgress(mBoundMPservice.getCurrentPosition());
			}
			if (isInPlayingState) {
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
	
	public void initAlbumArtView(ImageView[] views) {
		mAlbumArtViews = views;
	}
	
	public void initAlbumArtText(TextView[] views) {
		mAlbumArtTexts = views;
	}
	
	public void setAlbumArt() {
		byte[] albumArtData = getAlbumArt();
		setAlbumArt(albumArtData);
	}
	
	public void setAlbumArt(byte[] albumArtData) {
		if (albumArtData != null) {
			for(TextView view: mAlbumArtTexts) {
				view.setVisibility(View.INVISIBLE);
			}
			for(ImageView view : mAlbumArtViews) {
				view.setVisibility(View.VISIBLE);
				view.setImageBitmap(BitmapFactory.decodeByteArray(albumArtData, 0, albumArtData.length));
			}
		} else {
			for(TextView view: mAlbumArtTexts) {
				view.setVisibility(View.VISIBLE);
			}
			for(ImageView view : mAlbumArtViews) {
				view.setVisibility(View.INVISIBLE);
			}
		}
	}
	
	public void initSeekBar(SeekBar seekBar) {
		mSeekbar = seekBar;
		mSeekbar.setOnSeekBarChangeListener(mSeekBarChangeListener);
	}
	
	public void initLabels(TextView trackNameLabel, TextView artistNameLabel) {
		mTrackNameLabel = trackNameLabel;
		mArtistNameLabel = artistNameLabel;
	}
	
	public void setPlayButton(ToggleButton[] buttons) {
		mPlayButtons = buttons;
	}

	public void playMusic(Track track) {
		FileInputStream file;
		if (!CacheManager.isCached(track)){
			return;
		} else {
			file = CacheManager.getCacheFile(track);
			Log.d("Main", "read cache");
		}
		currentPlayingFile = file;
		mBoundMPservice.play(file);
		setAlbumArt();
		isInPlayingState = true;
		isPlayingMusic = true;
		setPlayButtonStatus();
		setTrackLabels(track);
		mHandler.postDelayed(mSeekBarSetter, 500);
	}
	
	private void setTrackLabels(Track track) {
		mTrackNameLabel.setText(track.getName());
		mArtistNameLabel.setText(track.getArtist());
	}
	
	public void playNextTrack() {
		isInPlayingState = false;
		isPlayingMusic = false;
		setPlayButtonStatus();
		if(mPlayList.size() == 0) {
			return;
		}
		do {
			Log.d(TAG, "next track");
			if(currentPos == mPlayList.size() - 1) {
				currentPos = 0;
			} else {
				currentPos++;
			}
		} while(!(mPlayList.get(currentPos).isUploaded || mPlayList.get(currentPos).isCached()));
		
		if(mReadyQueue.contains(mPlayList.get(currentPos))) {
			playMusic(mPlayList.get(currentPos));
		}
	}
	
	public void playPrevTrack() {
		isInPlayingState = false;
		isPlayingMusic = false;
		setPlayButtonStatus();
		if(mPlayList.size() == 0) {
			return;
		}
		do {
			Log.d(TAG, "previous track");
			if(currentPos == 0) {
				currentPos = mPlayList.size() - 1;
			} else {
				currentPos--;
			}
		} while(!(mPlayList.get(currentPos).isUploaded || mPlayList.get(currentPos).isCached()));
		
		if(mReadyQueue.contains(mPlayList.get(currentPos))) {
			playMusic(mPlayList.get(currentPos));
		}
	}
	
	public void start() {
		if(mBoundMPservice.start()) {
			isPlayingMusic = true;
		}
		setPlayButtonStatus();
	}
	
	public void pause() {
		mBoundMPservice.pause();
		isPlayingMusic = false;
		setPlayButtonStatus();
	}
	
	private void setPlayButtonStatus() {
		for(ToggleButton button: mPlayButtons){
			button.setChecked(!isPlayingMusic);
		}
	}
	
	public boolean isPlayingMusic() {
		return isPlayingMusic;
	}
	
	public byte[] getAlbumArt() {
		try {
			if(currentPlayingFile != null) {
				mMediaMetaData.setDataSource(currentPlayingFile.getFD());
				return mMediaMetaData.getEmbeddedPicture();
			} else {
				return null;
			}
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
	
	public void addToListAndPlay(List<Track> trackList, int startPos) {
		mPlayList.clear();
		mReadyQueue.clear();
		isInPlayingState = false;
		mPlayList.addAll(trackList);
		currentPos = startPos;
		//download file in background
		preparePlayList();
		playMusic(mPlayList.get(currentPos));
	}
	
	private void preparePlayList() {
		//prepare selected track first
		Track[] trackArray = new Track[mPlayList.size()];
		for(int i = currentPos; i < mPlayList.size(); i++) {
			trackArray[i - currentPos] = mPlayList.get(i);
		}
		for(int i = 0; i < currentPos; i++) {
			trackArray[i + mPlayList.size() - currentPos] = mPlayList.get(i);
		}
		if(mDownloader != null) {
			mDownloader.cancel(true);
		}
		mDownloader = new Downloader(mDropbox, this).execute(trackArray);
	}
	
	public void addToReadyQueue(Track track) {
		mReadyQueue.offer(track);
		if (mReadyQueue.contains(mPlayList.get(currentPos)) && !isInPlayingState) {
			if(!track.isUploaded) {
				playNextTrack();
				return;
			}
			playMusic(mPlayList.get(currentPos));
		}
	}
	
	public void clearQuere() {
		mPlayList.clear();
	}
	
}
