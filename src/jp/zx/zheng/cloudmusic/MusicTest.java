package jp.zx.zheng.cloudmusic;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import com.astuetz.PagerSlidingTabStrip;
import com.dropbox.sync.android.DbxPath;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;

import jp.zx.zheng.cloudstorage.CloudStoragePath;
import jp.zx.zheng.cloudstorage.dropbox.DbxPathAdapter;
import jp.zx.zheng.cloudstorage.dropbox.Dropbox;
import jp.zx.zheng.db.MusicLibraryDBAdapter;
import jp.zx.zheng.db.MusicLibraryDBHelper;
import jp.zx.zheng.musictest.R;
import jp.zx.zheng.musictest.R.drawable;
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
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.view.View.OnClickListener;

public class MusicTest extends FragmentActivity {

	private static final String TAG = MusicTest.class.getName();
	
	Dropbox mDropbox;
	MediaPlayer mp;
	ToggleButton playButton;
	ToggleButton mPlayButton1;
	static MusicPlayer mMusicPlayer;
	private static ActionBar mActionBar;
	private static View mActionbarView;
	private static PagerSlidingTabStrip mTabs;
	private static LinearLayout mLibraryView;
	private static ViewPager mViewPager;
	private static PagerSlidingTabStrip mCloudLibTabs;
	private static LinearLayout mCloudLibraryView;
	private static ViewPager mCloudLibViewPager;
	private AppSectionsPagerAdapter mAppSectionsPagerAdapter;
	public static Typeface mEntypo;
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;	
	private static ListView mAlbumListView;
	private static ListView mTrackListView;
	private static ListView mPlayListView;
	private static ListView mArtistListView;
	private static ListView mCloudArtistListView;
	private static ListView mCloudStorageFinderView;
	private static ImageView mAlbumArtImage;
	private static ImageView mSmallAlbumArtImage;
	private static ImageView mActionBarUpImage;
	private static String mCurrentArtist;
	private static String mCurrentAlbum;
	private SeekBar mSeekBar;
	public static SlidingUpPanelLayout mSlidingUpPanelLayout;
	private static FrameLayout mMainLayout;
	private View mDragView;
	private static ButtonsFragment mButtonsFragment;
	private static int mCurrentMainView = 0;
	public static List<Track> mSelectedTrackList;
	public static List<Playlist> mPlaylists;
	private static final int MAIN_VIEW = 0;
	private static final int ALBUMS_VIEW = 1;
	private static final int TRACKS_VIEW = 2;
	private static final int PLAYLIST_TRACKS_VIEW = 3;
	private static final int CLOUD_MAIN_VIEW = 4;
	private static final int CLOUD_STORAGE_FINDER_VIEW = 5;
	private static final int CLOUD_ALBUMS_VIEW = 6;
	private static final int CLOUD_TRACKS_VIEW = 7;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //init DB adapter
        MusicLibraryDBAdapter.init(getApplicationContext());
        
        //init font
        mEntypo = Typeface.createFromAsset(getAssets(), "Entypo.ttf");
        
        mActionbarView = getWindow().getDecorView();
        mActionBarUpImage = getHomeAsUpIndicator(mActionbarView);
        
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        
        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, new String[]{"iTunes Library", "Cloud Storage"}));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        mMainLayout = (FrameLayout) findViewById(R.id.mainLayout);
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
        
        initCloudLibraryView();
        
        /*
        int slideViewHeight = findViewById(R.id.slideView).getLayoutParams().height;
        int dragViewHeight = findViewById(R.id.smallAlbumArt).getHeight();
        int seekbarHeight = findViewById(R.id.musicSeekBar).getHeight();
        int controllerHeight = findViewById(R.id.controller).getHeight();

        Point windowSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(windowSize);
        int albumArtHeight = Math.min(slideViewHeight - dragViewHeight - seekbarHeight - controllerHeight,
        		windowSize.x);
        FrameLayout albumArtView = (FrameLayout) findViewById(R.id.AlbumArtView);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(windowSize.x, albumArtHeight);
        albumArtView.setLayoutParams(params);
        */
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
        mMusicPlayer.setAlbumArtView(
        		new ImageView[]{mAlbumArtImage, (ImageView)findViewById(R.id.smallAlbumArtImage)},
        		this);
        mMusicPlayer.setAlbumArtText(
        		new TextView[]{albumArtText,smallAlbumArtText});        				
        mMusicPlayer.setAlbumArt();
        
        mMusicPlayer.setLabels(
        		(TextView)findViewById(R.id.trackTitleLabel),
        		(TextView)findViewById(R.id.artistLabel));
        mMusicPlayer.setTrackLabels();
        
        mSeekBar = (SeekBar)findViewById(R.id.musicSeekBar);
        mMusicPlayer.setSeekBar(mSeekBar);
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
        mMusicPlayer.setPlayButtonStatus();
        
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
        
        mMusicPlayer.setProgressBar((ProgressBar) findViewById(R.id.progressBar));
        
        if (savedInstanceState == null) {
           // selectItem(0);
        }
        initCloudStorageFinderView();
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
	    super.onWindowFocusChanged(hasFocus);
	    
	    int slideViewHeight = findViewById(R.id.slideView).getHeight();
        int dragViewHeight = findViewById(R.id.smallAlbumArt).getHeight();
        int seekbarHeight = findViewById(R.id.musicSeekBar).getHeight();
        int controllerHeight = findViewById(R.id.controller).getHeight();
        
	    Point windowSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(windowSize);
        int albumArtHeight = Math.min(slideViewHeight - dragViewHeight - seekbarHeight - controllerHeight,
        		windowSize.x);
        FrameLayout albumArtView = (FrameLayout) findViewById(R.id.AlbumArtView);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(windowSize.x, albumArtHeight);
        albumArtView.setLayoutParams(params);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}
	 
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	private static void setActionBarTitle() {
		mActionBar.setTitle(R.string.app_name);
		mActionBar.setIcon(R.drawable.ic_cloud_music);
	}
	
	private static void setActionBarTitle(int resId) {
		mActionBar.setTitle(resId);
	}
	
	private static void setActionBarTitle(String title) {
		mActionBar.setTitle(title);
	}
	
	private static void setActionBarTitle(int resIdTitle, int resIdImage) {
		mActionBar.setTitle(resIdTitle);
		mActionBar.setIcon(resIdImage);
	}
	
	private static void setActionBarTitle(String title, int resId) {
		mActionBar.setTitle(title);
		mActionBar.setIcon(resId);
	}
	
	private void initCloudLibraryView() {
		mCloudLibraryView = (LinearLayout) findViewById(R.id.cloud_library);
		mCloudLibTabs = (PagerSlidingTabStrip) findViewById(R.id.cloud_Library_tabs);
		mCloudLibViewPager = (ViewPager) findViewById(R.id.cloud_library_pager);
		mCloudLibViewPager.setAdapter(new CloudLibraryAppSectionsPagerAdapter(getSupportFragmentManager()));
		mCloudLibTabs.setViewPager(mCloudLibViewPager);
		mCloudLibViewPager.setOnTouchListener(new OnTouchListener() {
			
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
		mMainLayout.removeView(mCloudLibraryView);
		//mCloudLibraryView.setVisibility(View.INVISIBLE);
	}
	
	private void initCloudStorageFinderView() {
		if(mCloudStorageFinderView == null) {
			mCloudStorageFinderView = (ListView) (getLayoutInflater().inflate(R.layout.finder, null));
			mCloudStorageFinderView.setOnItemClickListener(new CloudStorageFinderClickedListener());
		}
	}
	
	 /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        switch (position) {
		case 0:
			goToMainView();
			break;
		case 1:
			goToCloudLibrary();
			break;
		default:
			break;
		}
        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
    }
    
    private static void removeAllView() {
    	mMainLayout.removeView(mLibraryView);
    	mMainLayout.removeView(mAlbumListView);
		mMainLayout.removeView(mTrackListView);
		mMainLayout.removeView(mCloudLibraryView);
		mMainLayout.removeView(mCloudStorageFinderView);
    }
	
	private void goToMainView() {
		setHomeAsUpIndicator(R.drawable.ic_drawer);
		setActionBarTitle();
		removeAllView();
		mMainLayout.addView(mLibraryView);
		mLibraryView.requestFocus();
		mCurrentMainView = MAIN_VIEW;
	}
	
	private static void goToCloudLibrary() {
		setHomeAsUpIndicator(R.drawable.ic_drawer);
		removeAllView();
		setActionBarTitle(R.string.cloud_storage, R.drawable.ic_cloud_music);
		mMainLayout.addView(mCloudLibraryView);
		mCloudLibraryView.requestFocus();
		mCurrentMainView = CLOUD_MAIN_VIEW;
	}
	
	private static void goToCloudStorageFinder() {
		removeAllView();
		setHomeAsUpIndicator(R.drawable.ic_ab_back_holo_dark_am);
		mMainLayout.addView(mCloudStorageFinderView);
		mCloudStorageFinderView.requestFocus();
		mCurrentMainView = CLOUD_STORAGE_FINDER_VIEW;
	}
	
	private void goToAlbumsListView() {
		setHomeAsUpIndicator(R.drawable.ic_ab_back_holo_dark_am);
		mActionBar.setTitle(mCurrentArtist);
		mMainLayout.removeView(mTrackListView);
		mMainLayout.addView(mAlbumListView);
		if(mCurrentMainView == TRACKS_VIEW) {
			mCurrentMainView = ALBUMS_VIEW;
		} else if(mCurrentMainView == CLOUD_TRACKS_VIEW) {
			mCurrentMainView = CLOUD_ALBUMS_VIEW;
		}
	}
	
	public static class ArtistClickedListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int arg2,
				long arg3) {
			//mActionBar.setHomeAsUpIndicator(R.drawable.ic_ab_back_holo_dark_am);
			setHomeAsUpIndicator(R.drawable.ic_ab_back_holo_dark_am);
			mMainLayout.removeView(mLibraryView);
			mMainLayout.addView(mAlbumListView);
			mCurrentMainView = ALBUMS_VIEW;
			
			TextView textView = (TextView)view;
			mCurrentArtist = textView.getText().toString();
			mActionBar.setTitle(mCurrentArtist);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(parent.getContext(),
					R.layout.simple_list_item_1_black, 
					MusicLibraryDBAdapter.instance.listAlbum(mCurrentArtist));
			mAlbumListView.setAdapter(adapter);
			mAlbumListView.setOnItemClickListener(new AlbumClickedListener());
		}	
	}
	
	public static class CloudArtistClickedListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int arg2,
				long arg3) {
			//mActionBar.setDisplayHomeAsUpEnabled(true);
			//mActionBar.setHomeAsUpIndicator(R.drawable.ic_ab_back_holo_dark_am);
			setHomeAsUpIndicator(R.drawable.ic_ab_back_holo_dark_am);
			mMainLayout.removeView(mCloudLibraryView);
			mMainLayout.addView(mAlbumListView);
			mCurrentMainView = CLOUD_ALBUMS_VIEW;
			
			TextView textView = (TextView)view;
			mCurrentArtist = textView.getText().toString();
			mActionBar.setTitle(mCurrentArtist);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(parent.getContext(),
					R.layout.simple_list_item_1_black, 
					MusicLibraryDBAdapter.instance.listAlbum(
							MusicLibraryDBHelper.TABLE_CLOUD_TRACKS_NAME, mCurrentArtist));
			mAlbumListView.setAdapter(adapter);
			mAlbumListView.setOnItemClickListener(new AlbumClickedListener());
		}	
	}
	
	public static class AlbumClickedListener implements AdapterView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int arg2,
				long arg3) {
			//mActionBar.setDisplayHomeAsUpEnabled(true);
			//mActionBar.setHomeAsUpIndicator(R.drawable.ic_ab_back_holo_dark_am);
			setHomeAsUpIndicator(R.drawable.ic_ab_back_holo_dark_am);
			mMainLayout.removeView(mAlbumListView);
			mMainLayout.addView(mTrackListView);
			
			TextView textView = (TextView)view;
			mCurrentAlbum = textView.getText().toString();
			mActionBar.setTitle(mCurrentAlbum);
			
			if(mCurrentMainView == ALBUMS_VIEW) {
				mCurrentMainView = TRACKS_VIEW;
				mSelectedTrackList = 
						MusicLibraryDBAdapter.instance
						.listAlbumTracks(mCurrentArtist, mCurrentAlbum);
			} else if(mCurrentMainView == CLOUD_ALBUMS_VIEW){
				mCurrentMainView = CLOUD_TRACKS_VIEW;
				mSelectedTrackList = 
						MusicLibraryDBAdapter.instance
						.listAlbumTracks(MusicLibraryDBHelper.TABLE_CLOUD_TRACKS_NAME, mCurrentArtist, mCurrentAlbum);
			}
									
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
		Log.d(TAG, "onResume");
		mMusicPlayer.cancelNotification();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			if(backAction()){
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private boolean backAction() {
		if(mSlidingUpPanelLayout.isExpanded()) {
			mSlidingUpPanelLayout.collapsePane();
			return true;
		} else if(mCurrentMainView == ALBUMS_VIEW) {
			goToMainView();
			return true;
		} else if(mCurrentMainView == TRACKS_VIEW
				|| mCurrentMainView == CLOUD_TRACKS_VIEW) {
			goToAlbumsListView();
			return true;
		} else if(mCurrentMainView == PLAYLIST_TRACKS_VIEW) {
			goToMainView();
			return true;
		} else if(mCurrentMainView == CLOUD_ALBUMS_VIEW) {
			goToCloudLibrary();
			return true;
		} else if(mCurrentMainView == CLOUD_STORAGE_FINDER_VIEW) {
			if(new CloudStorageFinderClickedListener().isRoot()) {
				goToCloudLibrary();
			} else {
				mCloudStorageFinderView.performItemClick(mCloudStorageFinderView, 0, 0);
			}
		}
		return false;
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d(TAG, "onActivityResult");
    	switch (requestCode) {
    	case Dropbox.REQUEST_LINK_TO_DBX:
    		break;
		default:
			break;
		}
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	Log.d(TAG, "onPause");
    	mMusicPlayer.setUpAsForeground();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.cloud_music, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if(mCurrentMainView == CLOUD_MAIN_VIEW || mCurrentMainView == MAIN_VIEW) {
    		if (mDrawerToggle.onOptionsItemSelected(item)) {
    			return true;
    		}
    	}
    	switch (item.getItemId()) {
    	case R.id.action_settings:
    		setting();
    		return true;
    	case android.R.id.home:
    		backAction();
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
    private void setting(String action) {
    	Intent intent = new Intent(this, SettingActivity.class);
    	if(action != null) {
    		intent.setAction(action);
    	}
    	startActivity(intent);
    }
    
    private void setting() {
    	setting(null);
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
    
    public static class CloudLibraryAppSectionsPagerAdapter extends FragmentPagerAdapter {

    	private static String[] mPageTitles = {"Storages", "Artists"};
        public CloudLibraryAppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new CloudStorageListFragment();
                case 1:
                	return new CloudArtistListFragment();
                default:
                	return null; 
            }
        }

        @Override
        public int getCount() {
            return mPageTitles.length;
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
			setHomeAsUpIndicator(R.drawable.ic_ab_back_holo_dark_am);
			mActionBar.setTitle(mPlaylists.get(pos).name);
			mMainLayout.removeView(mLibraryView);
			mMainLayout.addView(mTrackListView);
			mCurrentMainView = PLAYLIST_TRACKS_VIEW;
			mSelectedTrackList = MusicLibraryDBAdapter.instance.listPlaylistTracks(mPlaylists.get(pos).id);
			/*
			ArrayAdapter<Track> adapter = new ArrayAdapter<Track>(parent.getContext(),
					R.layout.simple_list_item_1_black, 
					mSelectedTrackList);
					*/
			PlayListTrackArrayAdapter adapter = 
					new PlayListTrackArrayAdapter(parent.getContext(),
							R.layout.playlist_track, mSelectedTrackList);
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
	
	public static class CloudArtistListFragment extends Fragment {
		@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.finder, container, false);
			
			mCloudArtistListView = (ListView)rootView.findViewById(R.id.FileListView);
			reLoadArtists(getActivity());
			return rootView;
		}
		
		public static void reLoadArtists(Context context) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
					R.layout.simple_list_item_1_black, 
					MusicLibraryDBAdapter.instance.listAlbumArtists(MusicLibraryDBHelper.TABLE_CLOUD_TRACKS_NAME));
			mCloudArtistListView.setAdapter(adapter);
			mCloudArtistListView.setOnItemClickListener(new CloudArtistClickedListener());
		}
	}
	
	public static class CloudStorageListFragment extends Fragment {
		private String[] cloudstorages;
		private int[] cloudstorageIcons;
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.finder, container, false);
			ListView storageList = (ListView) rootView.findViewById(R.id.FileListView);
			cloudstorages = getResources().getStringArray(R.array.pref_cloud_storage_entry);
			cloudstorageIcons = new int[]{R.drawable.ic_dropbox};
			ArrayAdapter<String> adapter = new IconRowArrayAdapter(getActivity(),
					R.layout.icon_row,
					cloudstorages,
					cloudstorageIcons);
			storageList.setAdapter(adapter);
			storageList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long arg3) {
					Log.d(TAG, "Storage list clicked");
					goToCloudStorageFinder();
					setActionBarTitle(cloudstorages[position], cloudstorageIcons[position]);
					new CloudStorageFinderClickedListener().reloadListView(getActivity(), null);
				}
			});
			return rootView;
		}
		
	}
	
	private static class CloudStorageFinderClickedListener implements AdapterView.OnItemClickListener {

		private static CloudStoragePath mParentPath;
		private static CloudStoragePath mCurrentPath;
		
				
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long arg3) {
			CloudStoragePath path = (CloudStoragePath) parent.getItemAtPosition(position);
			mParentPath = mCurrentPath;
			mCurrentPath = path;
			if (!reloadListView(parent.getContext(), mCurrentPath)) {
				if(!mCurrentPath.getParent().isRoot()) {
					position--;
				}
				mMusicPlayer.addCloudPathToListAndPlay(
						Dropbox.getInstance().listDirectory((DbxPath) mCurrentPath.getParent().getPath(), true), position);
			}
		}
		
		public boolean reloadListView (Context context, CloudStoragePath path) {
			if(!Dropbox.getInstance(context).isLogin()) {
				Intent intent = new Intent(context, SettingActivity.class);
				intent.setAction(SettingActivity.DROPBOX_LOGIN);
				context.startActivity(intent);
				goToCloudLibrary();
				return true;
			}
			if(path == null) {
				path = new DbxPathAdapter(DbxPath.ROOT);
			} else {
				if(path.isRoot()) {
					setActionBarTitle(path.getRootName());
				} else {
					setActionBarTitle(path.getName());
				}
			}
			List<CloudStoragePath> list = new ArrayList<CloudStoragePath>();
			if(!path.isRoot()) {
				list.add(path.getParent());
			}
			List<CloudStoragePath> childList = 
					Dropbox.getInstance(context).listDirectory((DbxPath) path.getPath(), true); 		
			if (childList == null) {
				return false;
			}
			list.addAll(childList);
			Log.d(TAG, path.toString());
			FinderRowArrayAdapter adapter = new FinderRowArrayAdapter(context,
					R.layout.finder_row, list, !path.isRoot());
			mCloudStorageFinderView.setAdapter(adapter);
			return true;
		}
		
		public boolean isRoot() {
			if(mCurrentPath == null) {
				return true;
			}
			return mCurrentPath.isRoot();
		}
		
	}
			
	private static void setHomeAsUpIndicator(int resId) {
		if(mActionBarUpImage != null) {
			mActionBarUpImage.setImageResource(resId);
		}
	}
	
	// http://pandora.sblo.jp/article/69804183.html
	private ImageView getHomeAsUpIndicator(View view) { 
	    
	    final View home = view.findViewById(android.R.id.home); 
	    if (home == null) { 
	        // Action bar doesn't have a known configuration, an OEM messed with things. 
	        return null; 
	    }

	    final ViewGroup parent = (ViewGroup) home.getParent(); 
        final int childCount = parent.getChildCount(); 
        if (childCount != 2) { 
            // No idea which one will be the right one, an OEM messed with things. 
            return null; 
        }

        final View first = parent.getChildAt(0); 
        final View second = parent.getChildAt(1); 
        final View up = first.getId() == android.R.id.home ? second : first;

        if (up instanceof ImageView) { 
            // Jackpot! (Probably...) 
        	return (ImageView) up;
        } 
        return null;
	}
}
