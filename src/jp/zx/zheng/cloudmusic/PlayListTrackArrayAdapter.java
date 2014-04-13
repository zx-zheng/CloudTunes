package jp.zx.zheng.cloudmusic;

import java.util.List;

import jp.zx.zheng.musictest.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PlayListTrackArrayAdapter extends ArrayAdapter<Track> {

	int resource;
	public PlayListTrackArrayAdapter(Context context, int resource,
			List<Track> tracks) {
		super(context, resource, tracks);
		this.resource = resource;
	}
	
	@Override
	public View getView (int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			LayoutInflater layout =
					(LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layout.inflate(resource, null);
		}
		
		Track track = getItem(position);
		
		TextView titleText = (TextView) convertView.findViewById(R.id.playlist_track_title);
		TextView artistText = (TextView) convertView.findViewById(R.id.playlist_track_artist);
		
		titleText.setText(track.getName());
		artistText.setText(track.getArtist());
		
		return convertView;
	}

}
