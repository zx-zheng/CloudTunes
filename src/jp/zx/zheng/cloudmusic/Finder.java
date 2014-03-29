package jp.zx.zheng.cloudmusic;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Array;
import java.util.List;

import jp.zx.zheng.cloudstorage.dropbox.Dropbox;
import jp.zx.zheng.musictest.R;
import jp.zx.zheng.storage.CacheManager;

import com.dropbox.sync.android.DbxPath;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Finder extends Activity {
	
	private static final String TAG = Finder.class.getName();
	public static final String PARENT_DIR = "parentDir";
	private DbxPath mParentPath;
	private DbxPath mCurrentPath;
	private Dropbox mDropbox;
	private ListView mFileListView;
	private MediaPlayer mMediaPlayer;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finder);
        mFileListView = (ListView)findViewById(R.id.FileListView);
        mFileListView.setOnItemClickListener(new FinderClickedListener());
        mDropbox = Dropbox.getInstance(getApplicationContext());
        mMediaPlayer = new MediaPlayer();
        Intent intent = getIntent();
        String currentStringDir = intent.getStringExtra(Finder.PARENT_DIR);
        if (currentStringDir == null) {
        	mCurrentPath = DbxPath.ROOT;
        } else {
        	mCurrentPath = new DbxPath(currentStringDir);
        }
        if (mDropbox.isLogin()) {
        	reloadListView();
        }
	}
	
	private boolean reloadListView () {
		List<String> list = mDropbox.listDirectory(mCurrentPath);
		Log.d(TAG, mCurrentPath.toString());
		if (list == null) {
			return false;
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
				R.layout.simple_list_item_1_black, list);
		mFileListView.setAdapter(adapter);
		return true;
	}
	
	public class FinderClickedListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int arg2,
				long arg3) {
			TextView textView = (TextView)view;
			mParentPath = mCurrentPath;
			mCurrentPath = new DbxPath(textView.getText().toString());
			if (!reloadListView()) {
				//mCurrentPath = mParentPath;
				Intent data = new Intent();
				data.putExtra("path", mCurrentPath.toString());
				setResult(RESULT_OK, data);
				finish();
			}
		}
	}
}
