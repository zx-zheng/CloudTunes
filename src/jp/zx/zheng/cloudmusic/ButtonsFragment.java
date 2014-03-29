package jp.zx.zheng.cloudmusic;

import java.io.IOException;
import java.io.InputStream;

import jp.zx.zheng.cloudstorage.dropbox.Dropbox;
import jp.zx.zheng.db.MusicLibraryDBAdapter;
import jp.zx.zheng.musictest.R;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ButtonsFragment extends Fragment {

	Dropbox mDropbox;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.buttons, container, false);
		
		mDropbox = Dropbox.getInstance(getActivity().getApplicationContext());
		
		Button loginButton = (Button) rootView.findViewById(R.id.login);
		loginButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//Intent loginIntent = new Intent(MusicTest.this, YConnectImplicitWebViewActivity.class);
				//startActivityForResult(loginIntent, 0);
				//Ybox.getInstance().requestFileList(MusicTest.this);
				mDropbox.login(getActivity());
				Intent finder = new Intent(getActivity(), Finder.class);
				startActivity(finder);
			}
		});   
		Button parseButton = (Button) rootView.findViewById(R.id.parse);
		parseButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AssetManager as = getResources().getAssets();
				try {
					InputStream xml = as.open("iTunes Music Library.xml");
					MusicLibraryParser parser = new MusicLibraryParser(getActivity().getApplicationContext(), xml);
					parser.parse();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		Button selectButton = (Button) rootView.findViewById(R.id.select);
		selectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MusicLibraryDBAdapter adapter = MusicLibraryDBAdapter.instance;
				adapter.open();
				//adapter.selectTracks();
				adapter.listAlbumArtists();
				adapter.close();
			}        	
		});
		
		Button libraryButton = (Button) rootView.findViewById(R.id.library);
		libraryButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getActivity().getApplicationContext(), LibraryFinder.class);
				startActivity(intent);
			}
			
		});
		
		return rootView;
	}
}
