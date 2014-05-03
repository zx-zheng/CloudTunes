package jp.zx.zheng.cloudmusic;

import java.util.List;

import jp.zx.zheng.cloudstorage.CloudStoragePath;
import jp.zx.zheng.musictest.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class IconRowArrayAdapter extends ArrayAdapter<String> {

	int resource;
	int[] icons;
	public IconRowArrayAdapter(Context context, int resource,
			String[] objects, int[] icons) {
		super(context, resource, objects);
		this.resource = resource;
		this.icons = icons;
	}
	
	@Override
	public View getView (int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			LayoutInflater layout =
					(LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layout.inflate(resource, null);
		}
		
		((TextView) convertView.findViewById(R.id.row_name)).setText(getItem(position));;
		((ImageView) convertView.findViewById(R.id.row_icon)).setImageResource(icons[position]); 
		
		return convertView;
	}
}
