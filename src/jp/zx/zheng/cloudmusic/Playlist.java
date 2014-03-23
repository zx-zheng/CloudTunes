package jp.zx.zheng.cloudmusic;

import java.util.List;

public class Playlist {
	public int id;
	public String name;
	public List<Track> tracklist;
	
	public Playlist(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
