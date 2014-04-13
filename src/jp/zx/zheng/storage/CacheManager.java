package jp.zx.zheng.storage;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import jp.zx.zheng.cloudmusic.MusicPlayer;
import jp.zx.zheng.cloudmusic.Track;

import com.dropbox.sync.android.DbxPath;

import android.os.Environment;
import android.util.Log;

public class CacheManager {

	private static final String TAG = CacheManager.class.getName();
	private static final String CACHE_DIR = "/CloudMusic/";
	
	public static String getCachePath(Track track) {
		String trackPath = pathTodbxPath(track.getLocation()).toString();
		return Environment.getExternalStorageDirectory() + CACHE_DIR + convertPath(trackPath);
	}
	
	public static boolean isCached (Track track) {
		//System.out.println(Environment.getExternalStorageDirectory());
		String fullPath = getCachePath(track);
		return new File(fullPath).exists();
	}
	
	public static FileInputStream getCacheFile (Track track) {
		String fullPath = getCachePath(track);
		File file = new File(fullPath);
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static void mkCacheParentDir(String filePath){
		String parentDirString = new File(Environment.getExternalStorageDirectory() 
				+ CACHE_DIR + convertPath(filePath)).getParent();
		File parentDir = new File(parentDirString);
		if (!parentDir.exists()) {
			parentDir.mkdirs();
		}
	}
	
	public static void saveCache (FileInputStream inFile, Track track) {
		String trackPath = pathTodbxPath(track.getLocation()).toString();
		mkCacheParentDir(trackPath);
		try {
			File file = new File(Environment.getExternalStorageDirectory() 
					+ CACHE_DIR + convertPath(trackPath));
			Log.d(TAG, "save: " + file.getAbsolutePath());
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			FileChannel out = fos.getChannel();
			FileChannel src = inFile.getChannel();
			out.transferFrom(src, 0, inFile.getChannel().size());
			src.close();
			out.close();
			fos.close();
			Log.d(TAG, track.getName() + " cached");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	private static String convertPath (String AbsPath) {
		if (AbsPath.charAt(0) == '/') {
			return AbsPath.substring(1);
		}
		return AbsPath;
	}
	
	public static DbxPath pathTodbxPath(String path) {
		String relativePath = path.substring(path.indexOf("iTunes") - 1);
		//Log.d(TAG, relativePath);
		return new DbxPath(relativePath);
	}
}
