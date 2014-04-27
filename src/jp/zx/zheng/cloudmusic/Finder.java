package jp.zx.zheng.cloudmusic;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

import jp.zx.zheng.cloudstorage.CloudStoragePath;
import jp.zx.zheng.cloudstorage.dropbox.DbxPathAdapter;
import jp.zx.zheng.cloudstorage.dropbox.Dropbox;
import jp.zx.zheng.musictest.R;
import jp.zx.zheng.storage.CacheManager;

import com.dropbox.sync.android.DbxPath;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.SyncStateContract.Columns;
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
	private CloudStoragePath mParentPath;
	private CloudStoragePath mCurrentPath;
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
        	mCurrentPath = new DbxPathAdapter(DbxPath.ROOT);
        } else {
        	mCurrentPath = new DbxPathAdapter(new DbxPath(currentStringDir));
        }
        if (mDropbox.isLogin()) {
        	reloadListView();
        }
	}
	
	private boolean reloadListView () {
		List<CloudStoragePath> list = new ArrayList<CloudStoragePath>();
		if(!mCurrentPath.isRoot()) {
			list.add(mCurrentPath.getParent());
		}
		List<CloudStoragePath> childList = mDropbox.listDirectory((DbxPath) mCurrentPath.getPath()); 		
		if (childList == null) {
			return false;
		}
		list.addAll(childList);
		Log.d(TAG, mCurrentPath.toString());
		FinderRowArrayAdapter adapter = new FinderRowArrayAdapter(getApplicationContext(),
				R.layout.finder_row, list, !mCurrentPath.isRoot());
		mFileListView.setAdapter(adapter);
		return true;
	}
	
	public class FinderClickedListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long arg3) {
			CloudStoragePath path = (CloudStoragePath) parent.getItemAtPosition(position);
			mParentPath = mCurrentPath;
			mCurrentPath = path;
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
