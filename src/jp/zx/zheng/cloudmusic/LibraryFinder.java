package jp.zx.zheng.cloudmusic;

import java.util.List;

import jp.zx.zheng.cloudmusic.Finder.FinderClickedListener;
import jp.zx.zheng.db.MusicLibraryDBAdapter;
import jp.zx.zheng.musictest.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class LibraryFinder extends Activity {

	private static final String TAG = LibraryFinder.class.getName();
	private ListView mListView;
	private MusicLibraryDBAdapter mDbAdapter;
	private String mCurrentArtist;
	private String mCurrentAlbum;
	private MusicPlayer mMusicPlayer;
	
	private List<Track> mTempAlbum;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finder);
        mListView = (ListView)findViewById(R.id.FileListView);
        mDbAdapter = MusicLibraryDBAdapter.instance;
        mMusicPlayer = MusicPlayer.getInstance(getApplicationContext());
        loadArtists();
	}
	
	private void loadArtists() {
		mDbAdapter.open();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
				R.layout.simple_list_item_1_black, mDbAdapter.listAlbumArtists());
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new artistClickedListener());
	}
	
	private class artistClickedListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int arg2,
				long arg3) {
			TextView textView = (TextView)view;
			mCurrentArtist = textView.getText().toString();
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
					R.layout.simple_list_item_1_black, 
					mDbAdapter.listAlbum(mCurrentArtist));
			mListView.setAdapter(adapter);
			mListView.setOnItemClickListener(new albumClickedListener());
		}
	}
	
	private class albumClickedListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int arg2,
				long arg3) {
			TextView textView = (TextView)view;
			mCurrentAlbum = textView.getText().toString();
			mTempAlbum = mDbAdapter.listAlbumTracks(mCurrentArtist, mCurrentAlbum);
			ArrayAdapter<Track> adapter = new ArrayAdapter<Track>(getApplicationContext(),
					R.layout.simple_list_item_1_black, 
					mTempAlbum);
			mListView.setAdapter(adapter);
			mListView.setOnItemClickListener(new trackClickedListener());
		}
	}
	
	private class trackClickedListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long arg3) {
			mMusicPlayer.addToListAndPlay(mTempAlbum, position);
		}
	}
}
