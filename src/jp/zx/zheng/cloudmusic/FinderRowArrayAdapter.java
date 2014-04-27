package jp.zx.zheng.cloudmusic;

import java.util.List;

import jp.zx.zheng.cloudstorage.CloudStoragePath;
import jp.zx.zheng.musictest.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FinderRowArrayAdapter extends ArrayAdapter<CloudStoragePath> {

	int resource;
	boolean hasParent;
	public FinderRowArrayAdapter(Context context, int resource,
			List<CloudStoragePath> objects, boolean hasParent) {
		super(context, resource, objects);
		this.resource = resource;
		this.hasParent = hasParent;
	}
	
	@Override
	public View getView (int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			LayoutInflater layout =
					(LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layout.inflate(resource, null);
		}
		
		CloudStoragePath path = getItem(position);
		TextView name = (TextView) convertView.findViewById(R.id.finder_row_name);
		TextView type = (TextView) convertView.findViewById(R.id.finder_row_file_type);
		type.setTypeface(MusicTest.mEntypo);
		
		if(position == 0 && hasParent) {
			name.setText("Back to Parent Dir");
			type.setText("\u2B06");
		} else {			
			name.setText(path.getName());		
			if(path.isDir()) {
				type.setText(new StringBuilder().appendCodePoint(0x1F4C1).toString());
			} else {
				type.setText(new StringBuilder().appendCodePoint(0x1F4C4).toString());
			}
		}
		return convertView;
	}
}
