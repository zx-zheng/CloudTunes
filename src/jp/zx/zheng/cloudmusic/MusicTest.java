package jp.zx.zheng.cloudmusic;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.Inflater;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.astuetz.PagerSlidingTabStrip;
import com.dropbox.sync.android.DbxPath;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;

import jp.zx.zheng.cloudstorage.dropbox.Dropbox;
import jp.zx.zheng.cloudstorage.ybox.YConnectImplicitWebViewActivity;
import jp.zx.zheng.cloudstorage.ybox.YLoginManager;
import jp.zx.zheng.cloudstorage.ybox.Ybox;
import jp.zx.zheng.db.MusicLibraryDBAdapter;
import jp.zx.zheng.musictest.R;
import jp.zx.zheng.storage.CacheManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.view.View.OnClickListener;

public class MusicTest extends FragmentActivity {

	private static final String TAG = MusicTest.class.getName();
	
	Ybox ybox;
	Dropbox mDropbox;
	MediaPlayer mp;
	ToggleButton playButton;
	ToggleButton mPlayButton1;
	static MusicPlayer mMusicPlayer;
	private static ActionBar mActionBar;
	private static PagerSlidingTabStrip mTabs;
	private AppSectionsPagerAdapter mAppSectionsPagerAdapter;
	private static Typeface mEntypo;
	private static LinearLayout mLibraryView;
	private static ViewPager mViewPager;
	private static ListView mAlbumListView;
	private static ListView mTrackListView;
	private static ListView mPlayListView;
	private static ListView mArtistListView;
	private static ImageView mAlbumArtImage;
	private static ImageView mSmallAlbumArtImage;
	private static String mCurrentArtist;
	private static String mCurrentAlbum;
	private SeekBar mSeekBar;
	public static SlidingUpPanelLayout mSlidingUpPanelLayout;
	private static LinearLayout mMainLayout;
	private View mDragView;
	private static ButtonsFragment mButtonsFragment;
	private static int mCurrentMainView = 0;
	public static List<Track> mSelectedTrackList;
	public static List<Playlist> mPlaylists;
	private static final int ARTISTS_VIEW = 0;
	private static final int ALBUMS_VIEW = 1;
	private static final int TRACKS_VIEW = 2;
	private static final int PLAYLIST_TRACKS_VIEW = 3;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //init DB adapter
        MusicLibraryDBAdapter.init(getApplicationContext());
        
        //init font
        mEntypo = Typeface.createFromAsset(getAssets(), "Entypo.ttf");
        
        mMainLayout = (LinearLayout) findViewById(R.id.mainLayout);
        mLibraryView = (LinearLayout) findViewById(R.id.library);
        mTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mSlidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mSlidingUpPanelLayout.setShadowDrawable(getResources().getDrawable(R.drawable.above_shadow));
        mSlidingUpPanelLayout.setPanelSlideListener(new PanelSlideListener() {
			
			@Override
			public void onPanelSlide(View panel, float slideOffset) {
				
			}
			
			@Override
			public void onPanelExpanded(View panel) {
			}
			
			@Override
			public void onPanelCollapsed(View panel) {
			}
			
			@Override
			public void onPanelAnchored(View panel) {
			}
		});
       
        mButtonsFragment = new ButtonsFragment();
        
        mMusicPlayer = MusicPlayer.getInstance(getApplicationContext());
                
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
        // Set up the action bar.
        mActionBar = getActionBar();

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        mActionBar.setHomeButtonEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        //mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mTabs.setViewPager(mViewPager);
        
        mViewPager.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				//Log.d(TAG, "onTouchEvent");
				if(mSlidingUpPanelLayout.isExpanded()) {
					mSlidingUpPanelLayout.onTouchEvent(event);
					return true;
				}
				return false;
			}
		});
        
        Point windowSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(windowSize);
        FrameLayout albumArtView = (FrameLayout) findViewById(R.id.AlbumArtView);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(windowSize.x, windowSize.x);
        albumArtView.setLayoutParams(params);
        mAlbumArtImage = (ImageView)findViewById(R.id.albumArtImage);
        mAlbumArtImage.setOnTouchListener(new OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
        TextView albumArtText = (TextView) findViewById(R.id.albumArtText);
        albumArtText.setTypeface(mEntypo);
        mSmallAlbumArtImage = (ImageView)findViewById(R.id.smallAlbumArtImage);
        TextView smallAlbumArtText = (TextView) findViewById(R.id.smallalbumArtText);
        smallAlbumArtText.setTypeface(mEntypo);
        mMusicPlayer.initAlbumArtView(
        		new ImageView[]{mAlbumArtImage, (ImageView)findViewById(R.id.smallAlbumArtImage)});
        mMusicPlayer.initAlbumArtText(
        		new TextView[]{albumArtText,smallAlbumArtText});        				
        mMusicPlayer.setAlbumArt();
        
        mMusicPlayer.initLabels(
        		(TextView)findViewById(R.id.trackTitleLabel),
        		(TextView)findViewById(R.id.artistLabel));
        mMusicPlayer.setTrackLabels();
        
        mSeekBar = (SeekBar)findViewById(R.id.musicSeekBar);
        mMusicPlayer.initSeekBar(mSeekBar);
        //Ybox.getInstance().init(this);
        mDropbox = Dropbox.getInstance(getApplicationContext());
        //Ybox.getInstance().setSid(this);
        
        playButton = (ToggleButton)findViewById(R.id.playButtun);
        playButton.setMovementMethod(LinkMovementMethod.getInstance());
        playButton.setTypeface(mEntypo);
        playButton.setChecked(!mMusicPlayer.isPlayingMusic());
        playButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(playButton.isChecked()) {
					Log.d(TAG, "pause");
					mMusicPlayer.pause();
				} else {
					Log.d(TAG, "start");
					mMusicPlayer.start();
				}
			}
        });
        
        mAlbumListView = (ListView) (getLayoutInflater().inflate(R.layout.album_list, null));
        mTrackListView = (ListView) (getLayoutInflater().inflate(R.layout.track_list, null));
        
        mPlayButton1 = (ToggleButton) findViewById(R.id.playButton1);
        mPlayButton1.setTypeface(mEntypo);
        mPlayButton1.setTextOn("\u25B6");
        mPlayButton1.setTextOff("\u2016");
        mPlayButton1.setChecked(!mMusicPlayer.isPlayingMusic());
        mPlayButton1.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(mPlayButton1.isChecked()) {
					Log.d(TAG, "pause");
					mMusicPlayer.pause();
				} else {
					Log.d(TAG, "start");
					mMusicPlayer.start();
				}
			}
        });
                
        mMusicPlayer.setPlayButton(new ToggleButton[]{playButton, mPlayButton1});
        
        Button prevButton = (Button) findViewById(R.id.prevButton);
        prevButton.setTypeface(mEntypo);
        prevButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMusicPlayer.playPrevTrack();				
			}
		});
        
        Button nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setTypeface(mEntypo);
        nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMusicPlayer.playNextTrack();				
			}
		});
        //http://stackoverflow.com/questions/9834964/char-to-unicode-more-than-uffff-in-java
        Button repeatButton = (Button) findViewById(R.id.repeatButton);
        repeatButton.setTypeface(mEntypo);
        repeatButton.setText(new StringBuilder().appendCodePoint(0x1F501).toString());
        repeatButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMusicPlayer.toggleRepeatButton();
			}
		});
        mMusicPlayer.setRepeateButoon(repeatButton);
        
        Button shuffleButton = (Button) findViewById(R.id.shuffleButton);
        shuffleButton.setTypeface(mEntypo);
        shuffleButton.setText(new StringBuilder().appendCodePoint(0x1F500).toString());
        shuffleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMusicPlayer.toggleShuffleButton();
			}
		});
        mMusicPlayer.setShuffleButton(shuffleButton);
	}
	
	private void goToMainView() {
		mMainLayout.removeView(mAlbumListView);
		mMainLayout.removeView(mTrackListView);
		mMainLayout.addView(mLibraryView);
		mCurrentMainView = ARTISTS_VIEW;
	}
	
	private void goToAlbumsListView() {
		mMainLayout.removeView(mTrackListView);
		mMainLayout.addView(mAlbumListView);
		mCurrentMainView = ALBUMS_VIEW;
	}
	
	public static class ArtistClickedListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int arg2,
				long arg3) {
			mMainLayout.removeView(mLibraryView);
			mMainLayout.addView(mAlbumListView);
			mCurrentMainView = ALBUMS_VIEW;
			
			TextView textView = (TextView)view;
			mCurrentArtist = textView.getText().toString();
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(parent.getContext(),
					R.layout.simple_list_item_1_black, 
					MusicLibraryDBAdapter.instance.listAlbum(mCurrentArtist));
			mAlbumListView.setAdapter(adapter);
			mAlbumListView.setOnItemClickListener(new AlbumClickedListener());
		}	
	}
	
	public static class AlbumClickedListener implements AdapterView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int arg2,
				long arg3) {
			mMainLayout.removeView(mAlbumListView);
			mMainLayout.addView(mTrackListView);
			mCurrentMainView = TRACKS_VIEW;
			
			TextView textView = (TextView)view;
			mCurrentAlbum = textView.getText().toString();
			mSelectedTrackList = MusicLibraryDBAdapter.instance.listAlbumTracks(mCurrentArtist, mCurrentAlbum);
			ArrayAdapter<Track> adapter = new ArrayAdapter<Track>(parent.getContext(),
					R.layout.simple_list_item_1_black, 
					mSelectedTrackList);
			mTrackListView.setAdapter(adapter);
			mTrackListView.setOnItemClickListener(new TrackClickedListener());
		}
	}
	
	private static class TrackClickedListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long arg3) {
			mMusicPlayer.addToListAndPlay(mSelectedTrackList, position);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (mDropbox.isLogin()) {
			//dropBoxRoot();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			if(mSlidingUpPanelLayout.isExpanded()) {
				mSlidingUpPanelLayout.collapsePane();
				return true;
			} else if(mCurrentMainView == ALBUMS_VIEW) {
				goToMainView();
				return true;
			} else if(mCurrentMainView == TRACKS_VIEW) {
				goToAlbumsListView();
				return true;
			} else if(mCurrentMainView == PLAYLIST_TRACKS_VIEW) {
				goToMainView();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	/*
	public void setAlbumArt(Bitmap bitmap) {		
		mAlbumArtView.setImageBitmap(bitmap);
	}
	*/
	private void dropBoxRoot(){
		TextView text = (TextView)findViewById(R.id.textView1);
		text.setText("");
		List<String> fileList = mDropbox.listDirectory(DbxPath.ROOT);
		for (String file : fileList) {
			text.append(file + "\n");
		}
		String testFile = "02 自由の翼.m4a";
		FileInputStream file;
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d(TAG, "onActivityResult");
    	switch (requestCode) {
    	case Dropbox.REQUEST_LINK_TO_DBX:
    		dropBoxRoot();
    	case YLoginManager.Y_LOGIN_COMPLETE:
			Log.d("login", "login complete");
			Ybox.getInstance().setSid(this);
			break;
		default:
			break;
		}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.cloud_music, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.action_settings:
    		setting();
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
    private void setting() {
    	Intent intent = new Intent(this, SettingActivity.class);
    	startActivity(intent);
    }
    
    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

    	private static String[] mPageTitles = {"Artists", "Playlists", "Buttons"};
        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new ArtistListFragment();
                case 1:
                	return new PlayListFragment();
                default:
                	return mButtonsFragment;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mPageTitles[position];
        }
    }
	
	public static class PlayListFragment extends Fragment {
		
		@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.playlist_list, container, false);
			mPlayListView = (ListView) rootView.findViewById(R.id.playListView);
			mPlayListView.setOnItemClickListener(new PlaylistClickedListener());
			reloadPlayList(getActivity());
			return rootView;
		}
		
		public static void reloadPlayList(Context context) {
			if(mPlayListView != null) {
				mPlaylists = MusicLibraryDBAdapter.instance.listPlaylist();
				ArrayAdapter<Playlist> adapter = new ArrayAdapter<Playlist>(context, 
						R.layout.simple_list_item_1_black,
						mPlaylists);
				mPlayListView.setAdapter(adapter);
			}
		}
	}
	
	private static class PlaylistClickedListener implements AdapterView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int pos,
				long arg3) {
			mMainLayout.removeView(mLibraryView);
			mMainLayout.addView(mTrackListView);
			mCurrentMainView = PLAYLIST_TRACKS_VIEW;
			mSelectedTrackList = MusicLibraryDBAdapter.instance.listPlaylistTracks(mPlaylists.get(pos).id);
			ArrayAdapter<Track> adapter = new ArrayAdapter<Track>(parent.getContext(),
					R.layout.simple_list_item_1_black, 
					mSelectedTrackList);
			mTrackListView.setAdapter(adapter);
			mTrackListView.setOnItemClickListener(new TrackClickedListener());
		}
	}
	
	public static class ArtistListFragment extends Fragment {
		@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.finder, container, false);
			
			mArtistListView = (ListView)rootView.findViewById(R.id.FileListView);
	        mMusicPlayer = MusicPlayer.getInstance(getActivity());
	        reLoadArtists(getActivity());
			return rootView;
		}
		
		public static void reLoadArtists(Activity activity) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
					R.layout.simple_list_item_1_black, MusicLibraryDBAdapter.instance.listAlbumArtists());
			mArtistListView.setAdapter(adapter);
			mArtistListView.setOnItemClickListener(new ArtistClickedListener());
		}
	}
}
