package jp.zx.zheng.cloudmusic;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import jp.zx.zheng.cloudmusic.MediaPlayerService.State;
import jp.zx.zheng.cloudstorage.CloudStoragePath;
import jp.zx.zheng.cloudstorage.dropbox.Downloader;
import jp.zx.zheng.cloudstorage.dropbox.Dropbox;
import jp.zx.zheng.storage.CacheManager;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.sax.StartElementListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
	private ProgressBar mProgressBar;
	private ToggleButton[] mPlayButtons;
	private Button mRepeatButton;
	private Button mShuffuleButton;
	private TextView mTrackNameLabel;
	private TextView mArtistNameLabel;
	private Activity mMainActivity;
	public Bitmap mCurrentAlbumArt;
	
	private Dropbox mDropbox;
	private AsyncTask<Track, Track, List<Track>> mDownloader;
	private MediaPlayerService mBoundMPservice;
	private ServiceConnection mConnection;
	private List<Track> mCurrentPlayList;
	private List<Track> mOriginalPlayList;
	private List<Track> mShuffledPlayList;
	private int currentPos;
	private Queue<Integer> mReadyQueue;
	private MediaMetadataRetriever mMediaMetaData;
	private FileInputStream mCurrentPlayingFile;
	private Track mCurrentTrack;
	private Handler mHandler;
	private SharedPreferences mPref;
	private static final String PREF_IS_REPEAT_KEY = "isRepeatMode";
	private boolean isInPlayingState = false;
	private boolean isPlayingMusic = false;
	private boolean isRepeatMode = false;
	private boolean isShuffleMode = false;
	
	public PendingIntent mPiPrepared;
	
	private static final int ACTIVE_BUTTON_COLOR = Color.BLACK;
	private static final int INACTIVE_BUTTON_COLOR = Color.LTGRAY;
	
	private Runnable mSeekBarSetter = new Runnable() {
		
		@Override
		public void run() {			
			if (mBoundMPservice.getState() == State.Playing) {
				mSeekbar.setProgress(mBoundMPservice.getCurrentPosition());
				mHandler.postDelayed(this, 500);
			}
		}
	};
	
	public void startSeekbar(int duration) {
		if (mSeekbar != null) {			
			mSeekbar.setMax(mBoundMPservice.getDuration());
			mHandler.postDelayed(mSeekBarSetter, 500);
		}
	}
	
	public void resetSeekbar() {
		mSeekbar.setMax(1);
		mSeekbar.setProgress(0);
	}
	
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
		mDropbox = Dropbox.getInstance(context);
		mOriginalPlayList = new ArrayList<Track>();
		mShuffledPlayList = new ArrayList<Track>();
		mReadyQueue = new LinkedList<Integer>();
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
		mPref = PreferenceManager.getDefaultSharedPreferences(context);
		isRepeatMode = mPref.getBoolean(PREF_IS_REPEAT_KEY, true);
		setRepeatButtonStatus();
	}
	
	public void setAlbumArtView(ImageView[] views, Activity activity) {
		mAlbumArtViews = views;
		mMainActivity = activity;
	}
	
	public void setAlbumArtText(TextView[] views) {
		mAlbumArtTexts = views;
	}
	
	public void setAlbumArt() {
		mMainActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				byte[] albumArtData = getAlbumArt();
				setAlbumArt(mCurrentAlbumArt);		
			}
		});		
	}
	
	public void setAlbumArt(Track track) {
		byte[] albumArtData = track.getAlbumArt();
    	Bitmap bitmap = null;
    	if(albumArtData != null) {
    		bitmap = BitmapFactory.decodeByteArray(albumArtData, 0, albumArtData.length);
    	}
    	mCurrentAlbumArt = bitmap;
		setAlbumArt(bitmap);	
	}
	
	public void setAlbumArt(Bitmap albumArt) {
		if (albumArt != null) {
			for(TextView view: mAlbumArtTexts) {
				view.setVisibility(View.INVISIBLE);
			}
			for(ImageView view : mAlbumArtViews) {
				view.setVisibility(View.VISIBLE);
				view.setImageBitmap(albumArt);
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
	
	public void setSeekBar(SeekBar seekBar) {
		mSeekbar = seekBar;
		mSeekbar.setOnSeekBarChangeListener(mSeekBarChangeListener);
	}
	
	public void setLabels(TextView trackNameLabel, TextView artistNameLabel) {
		mTrackNameLabel = trackNameLabel;
		mArtistNameLabel = artistNameLabel;
	}
	
	public void setPlayButton(ToggleButton[] buttons) {
		mPlayButtons = buttons;
	}
	
	public void setRepeateButoon(Button button) {
		mRepeatButton = button;
		setRepeatButtonStatus();
	}
	
	public void setShuffleButton(Button button) {
		mShuffuleButton = button;
		setShuffleButtonStatus();
	}
	
	public void setProgressBar(ProgressBar bar) {
		mProgressBar = bar;
		mProgressBar.setVisibility(View.INVISIBLE);
	}
	
	public void reset() {
		Log.d(TAG, "reset Music Player");
		mBoundMPservice.processStopRequest(true);
		isPlayingMusic = false;
		isInPlayingState =false;
		clearQuere();
		mCurrentPlayingFile = null;
		mCurrentTrack = null;
		mSeekbar.setProgress(0);
		mProgressBar.setVisibility(View.INVISIBLE);
		setPlayButtonStatus();
		//setAlbumArt();
		setTrackLabels();
	}
	
	//before download file
	public void updateUI(Track track) {
		setTrackLabels(track);
		if(!track.isCached()) {
			mProgressBar.setVisibility(View.VISIBLE);
		}
	}
	
	//after download file
	public void updateUI2(Track track) {
		if(track.getArtist() == null) {
			track.updateInfo();
			setTrackLabels(track);
		}
		setAlbumArt(track);
		mProgressBar.setVisibility(View.INVISIBLE);
	}
	
	public void setTrackLabels(Track track) {
		mTrackNameLabel.setText(track.getName());
		mArtistNameLabel.setText(track.getArtist());
	}
	
	public void setTrackLabels() {
		if(mCurrentTrack == null) {
			mTrackNameLabel.setText("");
			mArtistNameLabel.setText("");
			return;
		}
		mTrackNameLabel.setText(mCurrentTrack.getName());
		mArtistNameLabel.setText(mCurrentTrack.getArtist());
	}
	
	public void playNextTrack() {
		/*
		isInPlayingState = false;
		isPlayingMusic = false;
		setPlayButtonStatus();
		if(mCurrentPlayList.size() == 0) {
			return;
		}
		do {
			Log.d(TAG, currentPos + " next track");
			if(currentPos == mCurrentPlayList.size() - 1) {
				currentPos = 0;
				if(!isRepeatMode) {
					reset();
					return;
				} 
			} else {
				currentPos++;
			}
		} while(mReadyQueue.contains(mCurrentPlayList.get(currentPos).getId())
				&& !(mCurrentPlayList.get(currentPos).isUploaded || mCurrentPlayList.get(currentPos).isCached()));
		
		prepareTrack(currentPos);
		
		if(mReadyQueue.contains(mCurrentPlayList.get(currentPos).getId())) {
			playMusic(mCurrentPlayList.get(currentPos));
		}
		*/
		mBoundMPservice.processSkipRequest();
	}
	
	public Track prepareCurrentTrack() {
		mCurrentTrack = mCurrentPlayList.get(currentPos);
		return mCurrentTrack;
	}
	
	public Track prepareNextTrack() {
		if(currentPos < 0 || mCurrentPlayList == null || mCurrentPlayList.size() == 0) {
			return null;
		}
				
		if(currentPos == mCurrentPlayList.size() - 1) {			
			if(!isRepeatMode) {
				currentPos = -1;
				return null;
			} else {
				currentPos = 0;	
			}
		} else {
			currentPos++;
		}
		Track nextTrack = mCurrentPlayList.get(currentPos);
		mCurrentTrack = nextTrack;
		return nextTrack;
	}
	
	public Track preparePrevTrack() {
		if(currentPos < 0 || mCurrentPlayList == null || mCurrentPlayList.size() == 0) {
			return null;
		}
		
		if(currentPos == 0) {
			currentPos = mCurrentPlayList.size() - 1; 
		} else {
			currentPos--;
		}
		Track prevTrack = mCurrentPlayList.get(currentPos);
		mCurrentTrack = prevTrack;
		return prevTrack;
	}
	
	
	/*
	private void prepareTrack(int pos) {
		mBoundMPservice.stop();
		isInPlayingState = false;
		mSeekbar.setProgress(0);
		mCurrentTrack = mCurrentPlayList.get(pos);
		mCurrentPlayingFile = null;
		mProgressBar.setVisibility(View.VISIBLE);
		setTrackLabels();
		setAlbumArt();
	}
	*/
	
	public void playPrevTrack() {
		/*
		isInPlayingState = false;
		isPlayingMusic = false;
		setPlayButtonStatus();
		if(mCurrentPlayList.size() == 0) {
			return;
		}
		do {
			Log.d(TAG, "previous track");
			if(currentPos == 0) {
				currentPos = mCurrentPlayList.size() - 1;
			} else {
				currentPos--;
			}
		} while(!(mCurrentPlayList.get(currentPos).isUploaded || mCurrentPlayList.get(currentPos).isCached()));
		
		if(mReadyQueue.contains(mCurrentPlayList.get(currentPos).getId())) {
			playMusic(mCurrentPlayList.get(currentPos));
		}
		*/
		
		mBoundMPservice.processRewindRequest();
	}
	
	public void start() {
		mBoundMPservice.processPlayRequest(false);			
		setPlayButtonStatus();
	}
	
	public void pause() {
		mBoundMPservice.processPauseRequest();
		isPlayingMusic = false;
		setPlayButtonStatus();
	}
	
	public void setPlayButtonStatus() {
		if(mBoundMPservice != null) {
			for(ToggleButton button: mPlayButtons){
				button.setChecked(!(mBoundMPservice.getState() == State.Playing));
			}
		}
	}
	
	public void setRepeatButtonStatus() {
		if(mRepeatButton == null) {
			return;
		}
		if(isRepeatMode) {
			mRepeatButton.setTextColor(ACTIVE_BUTTON_COLOR);
		} else {
			mRepeatButton.setTextColor(INACTIVE_BUTTON_COLOR);
		}
	}
	
	public void toggleRepeatButton() {
		if(isRepeatMode) {
			isRepeatMode = false;
		} else {
			isRepeatMode = true;
		}
		Editor editor = mPref.edit();
		editor.putBoolean(PREF_IS_REPEAT_KEY, isRepeatMode);
		editor.apply();
		setRepeatButtonStatus();
	}
	
	public void setShuffleButtonStatus() {
		if(mShuffuleButton == null) {
			return;
		}
		if(isShuffleMode) {
			mShuffuleButton.setTextColor(ACTIVE_BUTTON_COLOR);
		} else {
			mShuffuleButton.setTextColor(INACTIVE_BUTTON_COLOR);
		}
	}
	
	public void toggleShuffleButton() {
		if(isShuffleMode) {
			isShuffleMode = false;
			mCurrentPlayList = mOriginalPlayList;
			if(mCurrentTrack != null) {
				currentPos = mOriginalPlayList.indexOf(mCurrentTrack);
			} else {
				currentPos = 0;
			}
			preparePlayList();
		} else {
			isShuffleMode = true;
			if(isInPlayingState) {
				shuffleTrackList(currentPos);
				mCurrentPlayList = mShuffledPlayList;
				currentPos = 0;
				preparePlayList();
			}
		}
		setShuffleButtonStatus();
	}
	
	public boolean isPlayingMusic() {
		return isPlayingMusic;
	}
	
	public byte[] getAlbumArt(Track track) {
		return track.getAlbumArt();
	}
	
	public byte[] getAlbumArt() {
		try {
			if(mCurrentPlayingFile != null) {
				mMediaMetaData.setDataSource(mCurrentPlayingFile.getFD());
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
	
	public void addToListAndPlay(List<Track> trackList, int startPos) {
		mOriginalPlayList.clear();
		mShuffledPlayList.clear();
		mReadyQueue.clear();
		isInPlayingState = false;
		mOriginalPlayList.addAll(trackList);
		if(isShuffleMode) {
			shuffleTrackList(startPos);
			mCurrentPlayList = mShuffledPlayList;
			currentPos = 0;
		} else {
			mCurrentPlayList = mOriginalPlayList;
			currentPos = startPos;
		}
		
		//download file in background
		preparePlayList();
		mBoundMPservice.resetSkipCount();
		mBoundMPservice.processPlayRequest(true);
		//playMusic(mCurrentPlayList.get(currentPos));
	}
	
	public void addCloudPathToListAndPlay(List<CloudStoragePath> pathList, int startPos) {
		mOriginalPlayList.clear();
		mShuffledPlayList.clear();
		mReadyQueue.clear();
		isInPlayingState = false;
		int fixedStartPos = startPos;
		for(int i = 0; i < pathList.size(); i++) {
			CloudStoragePath path = pathList.get(i);
			if(!path.isDir()) {
				mOriginalPlayList.add(new Track(0, path.getName(), null, null, null, 
						CacheManager.DROPBOX_DIR + path.getPath()));				
			} else {
				if(i < startPos) {
					fixedStartPos--;
				}
			}
		}
		Log.d(TAG, "" + fixedStartPos);
		if(isShuffleMode) {
			shuffleTrackList(fixedStartPos);
			mCurrentPlayList = mShuffledPlayList;
			currentPos = 0;
		} else {
			mCurrentPlayList = mOriginalPlayList;
			currentPos = fixedStartPos;
		}
		
		//download file in background
		preparePlayList();
		mBoundMPservice.resetSkipCount();
		mBoundMPservice.processPlayRequest(true);
		//playMusic(mCurrentPlayList.get(currentPos));
	}
	
	private void shuffleTrackList(int pos) {
		Log.d(TAG, "shuffle playlist");
		mShuffledPlayList.clear();
		mShuffledPlayList.addAll(mOriginalPlayList);
		Collections.shuffle(mShuffledPlayList);
		Collections.swap(mShuffledPlayList,
				mShuffledPlayList.indexOf(mOriginalPlayList.get(pos)), 0);
	}
	
	private void preparePlayList() {
		//prepare selected track first
		Track[] trackArray = new Track[mCurrentPlayList.size()];
		for(int i = currentPos; i < mCurrentPlayList.size(); i++) {
			trackArray[i - currentPos] = mCurrentPlayList.get(i);
		}
		for(int i = 0; i < currentPos; i++) {
			trackArray[i + mCurrentPlayList.size() - currentPos] = mCurrentPlayList.get(i);
		}
		if(mDownloader != null) {
			mDownloader.cancel(false);
		}
		Log.d(TAG, "execute new download task");
		mDownloader = new Downloader(mDropbox, mPiPrepared, mMainActivity.getApplicationContext()).
				executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, trackArray);
	}
	
	public int getTrackListSize() {
		return mOriginalPlayList.size();
	}
	
	public void addToReadyQueue(Track track) {
		mReadyQueue.offer(track.getId());
		if (mCurrentPlayList.get(currentPos).getId() == track.getId() && !isInPlayingState) {
			Log.d(TAG, track + " upload:" + track.isUploaded + " cache:" + track.isCached());
			if(!track.isUploaded && !track.isCached()) {
				playNextTrack();
				return;
			}
			Log.d(TAG, "play " + track.getName());			
			//playMusic(mCurrentPlayList.get(currentPos));
		}
	}
	
	public void clearQuere() {
		mCurrentPlayList.clear();
	}
	
	public void setUpAsForeground() {
		if(mBoundMPservice != null) {
			if(mBoundMPservice.getState() == State.Playing) {
				mBoundMPservice.isPausing = true;
				mBoundMPservice.setUpAsForeground(mCurrentTrack);
			}
		}
	}
	
	public void cancelNotification() {
		if(mBoundMPservice != null) {
			if(mBoundMPservice.getState() == State.Playing) {
				mBoundMPservice.cancelNotification();
			}
		}
	}
	
}
