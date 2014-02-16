package jp.zx.zheng.cloudmusic;

import jp.zx.zheng.cloudmusic.Finder.FinderClickedListener;
import jp.zx.zheng.db.MusicLibraryDBAdapter;
import jp.zx.zheng.musictest.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class LibraryFinder extends Activity {

	private ListView mListView;
	private MusicLibraryDBAdapter mDbAdapter;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finder);
        mListView = (ListView)findViewById(R.id.FileListView);
        mDbAdapter = new MusicLibraryDBAdapter(getApplicationContext());
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
			// TODO Auto-generated method stub
			TextView textView = (TextView)view;
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
					R.layout.simple_list_item_1_black, 
					mDbAdapter.listAlbum(textView.getText().toString()));
			mListView.setAdapter(adapter);
			mListView.setOnItemClickListener(null);
		}
		
	}
}
