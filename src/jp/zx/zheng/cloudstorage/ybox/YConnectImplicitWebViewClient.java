package jp.zx.zheng.cloudstorage.ybox;

import jp.co.yahoo.yconnect.YConnectImplicit;
import jp.co.yahoo.yconnect.core.oauth2.AuthorizationException;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Implicit with WebView Sample Class
 *
 * @author Copyright (C) 2012 Yahoo Japan Corporation. All Rights Reserved.
 *
 */
public class YConnectImplicitWebViewClient extends WebViewClient {

	private final static String TAG = YConnectImplicitWebViewClient.class.getSimpleName();
	public final static String ACCESS_TOKEN = "access_token";

	private Activity activity;
	private String customUriScheme;

	public YConnectImplicitWebViewClient(Activity activity) {
		this.activity = activity;
		this.customUriScheme = YConnectImplicitWebViewActivity.customUriScheme;
	}

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {

		if (url.startsWith(customUriScheme)) {

			SharedPreferences sharedPreferences = activity.getSharedPreferences(
					YConnectImplicitWebViewActivity.YCONNECT_PREFERENCE_NAME, Activity.MODE_PRIVATE);

			try {

				/*********************************************************
				     Parse the Callback URI and Save the Access Token.
				 *********************************************************/

				// YConnectインスタンス取得
				YConnectImplicit yconnect = YConnectImplicit.getInstance();

				// stateの読み込み
				String state = sharedPreferences.getString("state", null);

				// コールバックURLから各パラメータを抽出
		        Uri uri = Uri.parse(url);
				yconnect.parseAuthorizationResponse(uri, customUriScheme, state);

				// アクセストークン、IDトークンを取得
				String accessTokenString = yconnect.getAccessToken();
				long expiration = yconnect.getAccessTokenExpiration();
				String idTokenString = yconnect.getIdToken();
				
				Ybox.getInstance().setAccessToken(accessTokenString);

				// アクセストークンを保存
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString(ACCESS_TOKEN, accessTokenString);
				editor.putLong("expiration", expiration);
				editor.commit();

		        // 別スレッド(AsynckTask)でCheckToken、UserInfoエンドポイントにリクエスト
				YConnectImplicitWebViewAsyncTask asyncTask = new YConnectImplicitWebViewAsyncTask(activity, idTokenString);
				asyncTask.execute("Request Check Token and UserInfo.");

			} catch (AuthorizationException e) {
				Log.e(TAG, "error=" + e.getError() + ", error_description=" + e.getErrorDescription());
				e.printStackTrace();
			} catch (Exception e) {
				Log.e(TAG, "error=" + e.getMessage());
				e.printStackTrace();
			}

		}

	}

}
