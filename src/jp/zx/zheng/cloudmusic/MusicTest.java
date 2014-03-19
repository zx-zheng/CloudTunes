package jp.zx.zheng.cloudmusic;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.Inflater;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
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
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.view.View.OnClickListener;

public class MusicTest extends FragmentActivity implements TabListener {

	private static final String TAG = MusicTest.class.getName();
	
	Ybox ybox;
	Dropbox mDropbox;
	MediaPlayer mp;
	ToggleButton playButton;
	MusicPlayer mMusicPlayer;
	private ActionBar mActionBar;
	private AppSectionsPagerAdapter mAppSectionsPagerAdapter;
	private ViewPager mViewPager;
	private ListView mAlbumListView;
	private ListView mTrackListView;
	private ImageView mAlbumArtView;
	private String mCurrentArtist;
	private String mCurrentAlbum;
	private SeekBar mSeekBar;
	public static SlidingUpPanelLayout mSlidingUpPanelLayout;
	private static LinearLayout mMainLayout;
	private View mDragView;
	private static ArtistListFragment mArtistListFragment;
	private static ButtonsFragment mButtonsFragment;
	private int mCurrentMainView = 0;
	public List<Track> mSelectedAlbum;
	private static final int ARTISTS_VIEW = 0;
	private static final int ALBUMS_VIEW = 1;
	private static final int TRACKS_VIEW = 2;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //init DB adapter
        MusicLibraryDBAdapter.init(getApplicationContext());
        
        mMainLayout = (LinearLayout) findViewById(R.id.mainLayout);
        mSlidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mSlidingUpPanelLayout.setShadowDrawable(getResources().getDrawable(R.drawable.above_shadow));
       
        //mDragView = findViewById(R.id.dragView);
        mArtistListFragment = new ArtistListFragment();
        mArtistListFragment.setArtistsListViewListener(new ArtistClickedListener());
        mButtonsFragment = new ButtonsFragment();
        
        mMusicPlayer = MusicPlayer.getInstance(getApplicationContext());
        
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
        // Set up the action bar.
        mActionBar = getActionBar();

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        mActionBar.setHomeButtonEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                mActionBar.setSelectedNavigationItem(position);
            }
        });
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
        
        setTab();
        
        Point windowSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(windowSize);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(windowSize.x, windowSize.x);
        mAlbumArtView = (ImageView)findViewById(R.id.albumArt);
        mAlbumArtView.setLayoutParams(params);
        mAlbumArtView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
        mMusicPlayer.initAlbumArtView(mAlbumArtView);
        mMusicPlayer.setAlbumArt();
        
        mMusicPlayer.initLabels(
        		(TextView)findViewById(R.id.trackTitleLabel),
        		(TextView)findViewById(R.id.artistLabel));
        mSeekBar = (SeekBar)findViewById(R.id.musicSeekBar);
        mMusicPlayer.initSeekBar(mSeekBar);
        Ybox.getInstance().init(this);
        mDropbox = new Dropbox(getApplicationContext());
        //Ybox.getInstance().setSid(this);
        
        playButton = (ToggleButton)findViewById(R.id.playButtun);
        playButton.setChecked(!mMusicPlayer.isPlayingMusic());
        playButton.setMovementMethod(LinkMovementMethod.getInstance());
        mMusicPlayer.setPlayButton(playButton);
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
	}
	
	private void goToArtistsListView() {
		mMainLayout.removeView(mAlbumListView);
		mMainLayout.addView(mViewPager);
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		mCurrentMainView = ARTISTS_VIEW;
	}
	
	private void goToAlbumsListView() {
		mMainLayout.removeView(mTrackListView);
		mMainLayout.addView(mAlbumListView);
		mCurrentMainView = ALBUMS_VIEW;
	}
	
	public class ArtistClickedListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int arg2,
				long arg3) {
			mMainLayout.removeView(mViewPager);
			mMainLayout.addView(mAlbumListView);
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			mCurrentMainView = ALBUMS_VIEW;
			
			TextView textView = (TextView)view;
			mCurrentArtist = textView.getText().toString();
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
					R.layout.simple_list_item_1_black, 
					MusicLibraryDBAdapter.instance.listAlbum(mCurrentArtist));
			mAlbumListView.setAdapter(adapter);
			mAlbumListView.setOnItemClickListener(new AlbumClickedListener());
		}	
	}
	
	public class AlbumClickedListener implements AdapterView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int arg2,
				long arg3) {
			mMainLayout.removeView(mAlbumListView);
			mMainLayout.addView(mTrackListView);
			mCurrentMainView = TRACKS_VIEW;
			
			TextView textView = (TextView)view;
			mCurrentAlbum = textView.getText().toString();
			mSelectedAlbum = MusicLibraryDBAdapter.instance.listAlbumTracks(mCurrentArtist, mCurrentAlbum);
			ArrayAdapter<Track> adapter = new ArrayAdapter<Track>(getApplicationContext(),
					R.layout.simple_list_item_1_black, 
					mSelectedAlbum);
			mTrackListView.setAdapter(adapter);
			mTrackListView.setOnItemClickListener(new TrackClickedListener());
		}
	}
	
	private class TrackClickedListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long arg3) {
			mMusicPlayer.addToList(mSelectedAlbum, position);
		}
	}
	
	private void setTab() {
		// For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            mActionBar.addTab(
                    mActionBar.newTab()
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
	}
	
	void showChild(View v, StringBuilder sbTabs) {
		System.out.printf("%s%s\n", sbTabs, v.getClass().getSimpleName());
		if (v instanceof ViewGroup) {
			ViewGroup layout = (ViewGroup) v;
			sbTabs = sbTabs.append("\t");
			for (int i = 0; i < layout.getChildCount(); i++) {
				showChild(layout.getChildAt(i), new StringBuilder(sbTabs));
			}
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
				goToArtistsListView();
				return true;
			} else if(mCurrentMainView == TRACKS_VIEW) {
				goToAlbumsListView();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public void setAlbumArt(Bitmap bitmap) {		
		mAlbumArtView.setImageBitmap(bitmap);
	}
	
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
    	Log.d("Main", "onActivityResult");
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
        getMenuInflater().inflate(R.menu.music_test, menu);
        return true;
    }
    
    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

    	private static String[] mPageTitles = {"Artists", "Buttons"};
        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return mArtistListFragment;
                default:
                	return mButtonsFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mPageTitles[position];
        }
    }

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}
}
