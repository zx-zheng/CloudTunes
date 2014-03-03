package jp.zx.zheng.cloudmusic;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.dropbox.sync.android.DbxPath;

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
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.view.View.OnClickListener;

public class MusicTest extends Activity {

	private static final String TAG = MusicTest.class.getName();
	
	Ybox ybox;
	Dropbox mDropbox;
	MediaPlayer mp;
	ToggleButton playButton;
	MusicPlayer mMusicPlayer;
	public static ImageView albumArtView;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    
        mMusicPlayer = MusicPlayer.getInstance(getApplicationContext());
        Button loginButton = (Button)findViewById(R.id.login);
        loginButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//Intent loginIntent = new Intent(MusicTest.this, YConnectImplicitWebViewActivity.class);
				//startActivityForResult(loginIntent, 0);
				//Ybox.getInstance().requestFileList(MusicTest.this);
				mDropbox.login(MusicTest.this);
				Intent finder = new Intent(getApplicationContext(), Finder.class);
				startActivity(finder);
				}
		});   
        Button parseButton = (Button)findViewById(R.id.parse);
        parseButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AssetManager as = getResources().getAssets();
				try {
					InputStream xml = as.open("iTunes Music Library.xml");
					MusicLibraryParser parser = new MusicLibraryParser(getApplicationContext(), xml);
					parser.parse();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
        
        Button selectButton = (Button)findViewById(R.id.select);
        selectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MusicLibraryDBAdapter adapter = new MusicLibraryDBAdapter(getApplicationContext());
				adapter.open();
				//adapter.selectTracks();
				adapter.listAlbumArtists();
				adapter.close();
			}        	
        });
        
        Button libraryButton = (Button)findViewById(R.id.library);
        libraryButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getApplicationContext(), LibraryFinder.class);
				startActivity(intent);
			}
        	
        });
        
        playButton = (ToggleButton)findViewById(R.id.playButtun);
        playButton.setChecked(false);
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
        
        albumArtView = (ImageView)findViewById(R.id.albumArt);
        TextView text = (TextView)findViewById(R.id.textView1);
        Ybox.getInstance().init(this);
        mDropbox = new Dropbox(getApplicationContext());
        //Ybox.getInstance().setSid(this);
        text.setText(Ybox.getInstance().getSid());
        mp = new MediaPlayer();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (mDropbox.isLogin()) {
			//dropBoxRoot();
		}
	}
	
	public static void setAlbumArt(Bitmap bitmap) {		
		albumArtView.setImageBitmap(bitmap);
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
		if (!CacheManager.isCached(testFile)){
			file = mDropbox.getFileAndCache(testFile);
			CacheManager.saveCache(file, testFile);
			Log.d("Main", "file cached");
		} else {
			file = CacheManager.getCacheFile(testFile);
			Log.d("Main", "read cache");
		}
		try {
			mp.setDataSource(file.getFD());
			Toast.makeText(this, "Success, Path has been set", Toast.LENGTH_SHORT).show();
			mp.prepare();
			mp.start();
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
    
}
