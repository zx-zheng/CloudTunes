package jp.zx.zheng.cloudstorage.ybox;

import jp.zx.zheng.cloudmusic.MusicTest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class YLoginManager {
	
	public static final int Y_LOGIN_COMPLETE = 1;
	private static SharedPreferences yConnectPref;
	
	public static String getAccessToken(Activity activity){
		if(yConnectPref == null){
			yConnectPref = activity.getSharedPreferences(
					YConnectImplicitWebViewActivity.YCONNECT_PREFERENCE_NAME, 
					Context.MODE_PRIVATE);
		}
		String accessToken = yConnectPref.getString(
				YConnectImplicitWebViewClient.ACCESS_TOKEN, null);
		if(accessToken == null){
			Intent loginIntent = new Intent(activity, YConnectImplicitWebViewActivity.class);
			activity.startActivityForResult(loginIntent, Y_LOGIN_COMPLETE);
		}
		return accessToken;
	}

}
