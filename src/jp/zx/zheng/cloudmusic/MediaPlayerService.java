package jp.zx.zheng.cloudmusic;

import java.io.FileInputStream;
import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

public class MediaPlayerService extends Service 
implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener{
	
	private MediaPlayer mMediaPlayer;
	private int mMusicDuration;
	
	public class MediaPlayerBinder extends Binder {
		MediaPlayerService getService() {
			return MediaPlayerService.this;
		}
	}
	@Override
    public void onCreate() {
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setOnPreparedListener(this);
		mMediaPlayer.setOnCompletionListener(this);
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	private MediaPlayerBinder mBinder = new MediaPlayerBinder(); 

	public void play(FileInputStream file) {
		mMediaPlayer.reset();
		try {
			mMediaPlayer.setDataSource(file.getFD());
			mMediaPlayer.prepareAsync();
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
	
	public void pause() {
		if(mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
		}
	}
	
	public boolean start() {
		mMediaPlayer.start();
		return mMediaPlayer.isPlaying();
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
		mp.start();		
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		MusicPlayer.getInstance(getApplicationContext()).playNextTrack();
	}
	
	public void reset() {
		mMediaPlayer.stop();
		mMediaPlayer.reset();
	}

}
