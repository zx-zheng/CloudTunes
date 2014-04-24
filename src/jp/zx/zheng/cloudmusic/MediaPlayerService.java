package jp.zx.zheng.cloudmusic;

import java.io.FileInputStream;
import java.io.IOException;

import jp.zx.zheng.musictest.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

public class MediaPlayerService extends Service 
implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener{
	
	private static final String TAG = MediaPlayerService.class.getName();
	
	private MediaPlayer mMediaPlayer;
	private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private RemoteViews mNotificationSmallView;
    private RemoteViews mNotificationView;
    private Track mCurrentTrack;
    private Notification mNotification;
	private int mMusicDuration;
	private static boolean isWatingPrepare = false;
	public boolean isPausing = false;
	
	final int NOTIFICATION_ID = 1;
	public static final String ACTION_PLAY = "mediaplayerservice.action.PLAY";
	public static final String ACTION_PAUSE = "mediaplayerservice.action.PAUSE";
	public static final String ACTION_PLAY_PAUSE = "mediaplayerservice.action.PLAY_PAUSE";
    public static final String ACTION_STOP = "cmediaplayerservice.action.STOP";
    public static final String ACTION_SKIP = "mediaplayerservice.action.SKIP";
    public static final String ACTION_REWIND = "mediaplayerservice.action.REWIND";
    public static final String ACTION_PREPARED = "mediaplayerservice.action.PREPARED";
    
    public static final int REQUEST_PLAY_STOP = 0;
    
    enum State {
        Retrieving, // the MediaRetriever is retrieving music
        Stopped,    // media player is stopped and not prepared to play
        Preparing,  // media player is preparing...
        Playing,    // playback active (media player ready!). (but the media player may actually be
                    // paused in this state if we don't have audio focus. But we stay in this state
                    // so that we know we have to resume playback once we get focus back)
        Paused      // playback paused (media player ready!)
    };
    
    State mState = State.Retrieving;
	
	public class MediaPlayerBinder extends Binder {
		MediaPlayerService getService() {
			return MediaPlayerService.this;
		}
	}
	
	public static boolean isWaitingPrepare() {
		return isWatingPrepare;
	}
	
	@Override
    public void onCreate() {
		Log.d(TAG, "onCreate");
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setOnPreparedListener(this);
		mMediaPlayer.setOnCompletionListener(this);
		
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotificationBuilder = new NotificationCompat.Builder(getApplicationContext());
		mNotificationSmallView = new RemoteViews(getPackageName(), R.layout.notification_small);
		
		mNotificationView = new RemoteViews(getPackageName(), R.layout.notification);
		mNotificationView.setTextViewText(R.id.title_notif, "");
		mNotificationView.setImageViewResource(R.id.playpause, R.drawable.ic_action_pause);
		
		mNotificationSmallView.setImageViewResource(R.id.playpause_small, R.drawable.ic_action_pause);
		
		
		//init pending intent
		Intent intent = new Intent(this, MediaPlayerService.class);
		intent.setAction(ACTION_PLAY_PAUSE);
		PendingIntent piPlayStop = PendingIntent.getService(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mNotificationView.setOnClickPendingIntent(R.id.playpause, piPlayStop);
		mNotificationSmallView.setOnClickPendingIntent(R.id.playpause_small, piPlayStop);
		
		intent = new Intent(this, MediaPlayerService.class);
		intent.setAction(ACTION_SKIP);
		PendingIntent piSkip = PendingIntent.getService(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mNotificationView.setOnClickPendingIntent(R.id.skip, piSkip);
		mNotificationSmallView.setOnClickPendingIntent(R.id.skip_small, piSkip);
		
		intent = new Intent(this, MediaPlayerService.class);
		intent.setAction(ACTION_REWIND);
		PendingIntent piRewind = PendingIntent.getService(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mNotificationView.setOnClickPendingIntent(R.id.rewind, piRewind);
		
		mNotificationBuilder.setContent(mNotificationSmallView);
		
		intent = new Intent(this, MediaPlayerService.class);
		intent.setAction(ACTION_PREPARED);
		PendingIntent piPrepared = PendingIntent.getService(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		MusicPlayer.getInstance(getApplicationContext()).mPiPrepared = piPrepared;
		
	}
	
	void createMediaPlayerIfNeeded() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();

            // Make sure the media player will acquire a wake-lock while playing. If we don't do
            // that, the CPU might go to sleep while the song is playing, causing playback to stop.
            //
            // Remember that to use this, we have to declare the android.permission.WAKE_LOCK
            // permission in AndroidManifest.xml.
            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            // we want the media player to notify us when it's ready preparing, and when it's done
            // playing:
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            //mMediaPlayer.setOnErrorListener(this);
        } else {
            mMediaPlayer.reset();
        }
    }
	
	void processTogglePlaybackRequest() {
        if (mState == State.Paused || mState == State.Stopped) {
            processPlayRequest(false);
        } else {
            processPauseRequest();
        }
        MusicPlayer.getInstance(getApplicationContext()).setPlayButtonStatus();
    }
	
	private void processPrearedAction() {
		if(mState == State.Playing) {
			return;
		}
		play(mCurrentTrack, false);
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		String action = intent.getAction();
		if(action.equals(ACTION_PLAY_PAUSE)) {
			processTogglePlaybackRequest();
		} else if(action.equals(ACTION_SKIP)) {
			processSkipRequest();
		} else if(action.equals(ACTION_REWIND)) {
			processRewindRequest();
		} else if(action.equals(ACTION_PREPARED)) {
			processPrearedAction();
		}
		return START_STICKY;
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	private MediaPlayerBinder mBinder = new MediaPlayerBinder(); 

	public void play(Track track, boolean isPrev) {
		updateUI(track);
		mCurrentTrack = track;
		if(track.isCached()){ 
			
		}else if(!track.isPrepared) {
			mState = State.Preparing;
			isWatingPrepare = true;
			return;
		}
		isWatingPrepare = false;
		if(!track.isCached()) {
			playNextTrack(isPrev);
			return;
		}
		releaseResouces(false);
		updateUI2(track);
		createMediaPlayerIfNeeded();
		try {
			mMediaPlayer.setDataSource(track.getFile().getFD());
			mMediaPlayer.prepareAsync();
			if(isPausing) {
				setUpAsForeground(track);
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void updateUI(Track track) {
		MusicPlayer.getInstance(getApplicationContext()).updateUI(track);
	}
	
	private void updateUI2(Track track) {
		MusicPlayer.getInstance(getApplicationContext()).updateUI2(track);
	}
	
	private void playCurrentTrack() {
		Track track = MusicPlayer.getInstance(getApplicationContext()).prepareCurrentTrack();
		processStopRequest(true);
		if(track == null) {
			return;
		}
		play(track, false);
	}
	
	private void playNextTrack(boolean isPrev) {
		Track nextTrack;
		if(isPrev) {
			nextTrack = MusicPlayer.getInstance(getApplicationContext()).preparePrevTrack();
		} else {
			nextTrack = MusicPlayer.getInstance(getApplicationContext()).prepareNextTrack();
		}
		processStopRequest(true);
		if(nextTrack == null) {
			return;
		}
		play(nextTrack, isPrev);
	}
	
	public void processPauseRequest() {
		if(mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
			mState = State.Paused;
			if(isPausing) {
				mNotificationView.setImageViewResource(R.id.playpause, R.drawable.ic_action_play);
				mNotificationSmallView.setImageViewResource(R.id.playpause_small, R.drawable.ic_action_play);
				mNotificationBuilder.setContent(mNotificationSmallView);
				Notification notif = mNotificationBuilder.build();
				notif.bigContentView = mNotificationView;
				mNotificationManager.notify(NOTIFICATION_ID, notif);
			}
		}
	}
	
	public void processPlayRequest(boolean force) {
		if(force) {
			playCurrentTrack();
		} else if(mState == State.Paused) {
			MusicPlayer.getInstance(getApplicationContext()).startSeekbar(mMusicDuration);
			mMediaPlayer.start();
			mState = State.Playing;
			if(isPausing) {
				mNotificationView.setImageViewResource(R.id.playpause, R.drawable.ic_action_pause);
				mNotificationSmallView.setImageViewResource(R.id.playpause_small, R.drawable.ic_action_pause);
				Notification notif = mNotificationBuilder.build();
				notif.bigContentView = mNotificationView;
				mNotificationManager.notify(NOTIFICATION_ID, notif);
			}
		}  else if(mState == State.Stopped) {
			playCurrentTrack();
		}
	}
	
	public void processSkipRequest() {
		processStopRequest(false);
		playNextTrack(false);
	}
	
	public void processRewindRequest() {
		if(mState == State.Playing || mState == State.Paused) {
			if(mMediaPlayer.getCurrentPosition() > 3000) {
				mMediaPlayer.seekTo(0);
				return;
			}
		}
		processStopRequest(false);
		playNextTrack(true);		
	}
	
	public void processStopRequest(boolean force) {
		if (mState == State.Playing || mState == State.Paused || force) {
            mState = State.Stopped;
            
            releaseResouces(true);
		}
	}
	
	private void releaseResouces(boolean releaseMediaPlayer) {
		// stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
	}
	
	public State getState() {
		return mState;
	}
	
	public int getDuration() {
		return mMusicDuration;
	}
	
	public int getCurrentPosition() {
		return mMediaPlayer.getCurrentPosition();
	}
	
	public void seekTo(int pos) {
		mMediaPlayer.seekTo(pos);
	}
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		mMusicDuration = mMediaPlayer.getDuration();
		mState = State.Playing;
		updateNotification();
		configAndStartMediaPlayer();
		MusicPlayer.getInstance(getApplicationContext()).setPlayButtonStatus();
		MusicPlayer.getInstance(getApplicationContext()).startSeekbar(mMusicDuration);
	}

	private void configAndStartMediaPlayer() {
		if (!mMediaPlayer.isPlaying()) {
			mMediaPlayer.start();
		}
	}
	
	@Override
	public void onCompletion(MediaPlayer mp) {
		MusicPlayer.getInstance(getApplicationContext()).playNextTrack();
		MusicPlayer.getInstance(getApplicationContext()).resetSeekbar();
	}
	
	public void stop() {
		mMediaPlayer.stop();
	}
	
	  /** Updates the notification. */
    void updateNotification() {
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MusicTest.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        //mNotification.setLatestEventInfo(getApplicationContext(), "CloudMusic", text, pi);
        //mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    /**
     * Configures service as a foreground service. A foreground service is a service that's doing
     * something the user is actively aware of (such as playing music), and must appear to the
     * user as a notification. That's why we create the notification here.
     */
    void setUpAsForeground(Track track) {
    	mNotificationBuilder.setTicker("CloudMusic")
    	.setSmallIcon(R.drawable.ic_cloud_music);
    	mNotificationView.setTextViewText(R.id.title_notif, track.getName());
    	mNotificationSmallView.setTextViewText(R.id.title_notif_small, track.getName());
    	mNotificationView.setTextViewText(R.id.artist_notif, track.getArtist());
    	mNotificationSmallView.setTextViewText(R.id.artist_notif_small, track.getArtist());
    	Bitmap bitmap = MusicPlayer.getInstance(getApplicationContext()).mCurrentAlbumArt;
    	if(bitmap != null) {
    		mNotificationView.setImageViewBitmap(R.id.albumart_notif, bitmap);
    		mNotificationSmallView.setImageViewBitmap(R.id.albumart_notif_small, bitmap);
    	} else {
    		mNotificationView.setImageViewResource(R.id.albumart_notif, R.drawable.ic_cloud_music);
    		mNotificationSmallView.setImageViewResource(R.id.albumart_notif, R.drawable.ic_cloud_music);
    	}
    	mNotificationBuilder.setAutoCancel(true);
    	mNotification = mNotificationBuilder.build();
    	mNotification.bigContentView = mNotificationView;
        startForeground(NOTIFICATION_ID, mNotification);
        //mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        Log.d("service", "notif");
    }
    
    public void cancelNotification() {
    	isPausing = false;
    	Log.d(TAG, "cancel Notification");
    	stopForeground(true);
    	mNotificationManager.cancel(NOTIFICATION_ID);
    }
    
    public void maybePrepareIsDoen() {
    	
    }

}
