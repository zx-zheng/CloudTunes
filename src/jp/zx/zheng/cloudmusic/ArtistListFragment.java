package jp.zx.zheng.cloudmusic;

import java.util.List;

import com.dropbox.sync.android.DbxPath;

import jp.zx.zheng.cloudmusic.Finder.FinderClickedListener;
import jp.zx.zheng.cloudstorage.dropbox.Dropbox;
import jp.zx.zheng.db.MusicLibraryDBAdapter;
import jp.zx.zheng.musictest.R;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ArtistListFragment extends Fragment {

	private static final String TAG = Finder.class.getName();
	private ListView mListView;
	private MusicLibraryDBAdapter mDbAdapter;
	private String mCurrentArtist;
	private String mCurrentAlbum;
	private MusicPlayer mMusicPlayer;
	private AdapterView.OnItemClickListener mArtistListListener;
	
	private List<Track> mTempAlbum;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.finder, container, false);
		
		mListView = (ListView)rootView.findViewById(R.id.FileListView);
        mDbAdapter = MusicLibraryDBAdapter.instance;
        mMusicPlayer = MusicPlayer.getInstance(getActivity());
        loadArtists();
        if(mArtistListListener != null) {
        	Log.d(TAG, "listener changed");
        	mListView.setOnItemClickListener(mArtistListListener);
        }
		return rootView;
	}
	
	private void loadArtists() {
		mDbAdapter.open();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				R.layout.simple_list_item_1_black, mDbAdapter.listAlbumArtists());
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new artistClickedListener());
	}
	
	public void setArtistsListViewListener(AdapterView.OnItemClickListener listener) {
		mArtistListListener = listener;
		if(mListView != null) {
			mListView.setOnItemClickListener(listener);
		}
	}
	
	private class artistClickedListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int arg2,
				long arg3) {
			TextView textView = (TextView)view;
			mCurrentArtist = textView.getText().toString();
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(),
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
			ArrayAdapter<Track> adapter = new ArrayAdapter<Track>(getActivity().getApplicationContext(),
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
