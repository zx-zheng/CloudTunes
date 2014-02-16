package jp.zx.zheng.cloudstorage.ybox;


import jp.co.yahoo.yconnect.YConnectImplicit;
import jp.co.yahoo.yconnect.core.api.ApiClientException;
import jp.co.yahoo.yconnect.core.oidc.CheckIdException;
import jp.co.yahoo.yconnect.core.oidc.IdTokenObject;
import jp.co.yahoo.yconnect.core.oidc.OIDCDisplay;
import jp.co.yahoo.yconnect.core.oidc.OIDCPrompt;
import jp.co.yahoo.yconnect.core.oidc.OIDCScope;
import jp.co.yahoo.yconnect.core.oidc.UserInfoObject;
import jp.zx.zheng.musictest.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * Implicit with WebView Sample WebView
 *
 * @author Copyright (C) 2012 Yahoo Japan Corporation. All Rights Reserved.
 *
 */
@SuppressLint("SetJavaScriptEnabled")
public class YConnectImplicitWebViewAsyncTask extends AsyncTask<String, Integer, Long> {

	private final static String TAG = YConnectImplicitWebViewAsyncTask.class.getSimpleName();

	private Activity activity;
	private Handler handler;

	private String clientId;
	private String customUriScheme;
	private String idTokenString;
	private String guid;

	private WebView webView;
	private String template;

	public YConnectImplicitWebViewAsyncTask(Activity activity, String idTokenStr) {
		this.activity = activity;
		this.clientId = YConnectImplicitWebViewActivity.clientId;
		this.customUriScheme = YConnectImplicitWebViewActivity.customUriScheme;
		this.idTokenString = idTokenStr;
		handler = new Handler();
		webView = (WebView) activity.findViewById(R.id.webView1);
	}

	@Override
	protected Long doInBackground(String... params) {

		Log.d(TAG, params[0]);

		SharedPreferences sharedPreferences = activity.getSharedPreferences(
				YConnectImplicitWebViewActivity.YCONNECT_PREFERENCE_NAME, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		// YConnectインスタンス取得
		YConnectImplicit yconnect = YConnectImplicit.getInstance();

		try {

			/***********************
			     Check ID Token.
			 ***********************/

			Log.i(TAG, "Request ChechToken.");

			// nonceの読み込み
	    	String nonce = sharedPreferences.getString("nonce", null);

			// CheckTokenエンドポイントにリクエスト
			yconnect.requestCheckToken(idTokenString, nonce, clientId);
			// 復号化されたIDトークン情報を取得
			final IdTokenObject idtokenObject = yconnect.getIdTokenObject();

			/*************************
			     Request UserInfo.
			 *************************/

			Log.i(TAG, "Request UserInfo.");

			// アクセストークンの読み込み
			final String accessTokenString = sharedPreferences.getString("access_token", null);
			final long expiration = sharedPreferences.getLong("expiration", -1L);

			// UserInfoエンドポイントにリクエスト
			yconnect.requestUserInfo(accessTokenString);
			// UserInfo情報を取得
			final UserInfoObject userInfoObject = yconnect.getUserInfoObject();
			guid = userInfoObject.getUserId();
			editor.putString("guid", guid);
			editor.commit();
			
		} catch (ApiClientException e) {

			// エラーレスポンスが"Invalid_Token"であるかチェック
			if(e.isInvalidToken()) {

				/*****************************
			         Refresh Access Token.
				 *****************************/

				Log.i(TAG, "Refresh Access Token.");

				// Implicitフローでアクセストークンの有効期限がきれた場合は
				// 初回と同様に新たにアクセストークンを取得してください

				String state = "fj3qifjioajdsije545FDSAF";
				String nonce = "fjioasjfoij4350485049385joJFKDjfioe4jdjf84jjf94jafhip9046";
				String display = OIDCDisplay.SMART_PHONE;
				String[] prompt = { OIDCPrompt.DEFAULT };
				String[] scope = { OIDCScope.OPENID, OIDCScope.PROFILE,
						OIDCScope.EMAIL, OIDCScope.ADDRESS };

				yconnect.init(clientId, customUriScheme, state, display, prompt, scope, nonce);
				Uri authorizationUri = yconnect.generateAuthorizationUri();
				webView.clearCache(true);
				webView.setWebViewClient(new YConnectImplicitWebViewClient(activity));
				webView.setWebChromeClient(new WebChromeClient());
				webView.getSettings().setJavaScriptEnabled(true);
				webView.loadUrl(authorizationUri.toString());

			}

			Log.e(TAG, "error=" + e.getError() + ", error_description=" + e.getErrorDescription());
			e.printStackTrace();
		} catch (CheckIdException e) {
			Log.e(TAG, "error=" + e.getError() + ", error_description=" + e.getErrorDescription());
			e.printStackTrace();
		} catch (Exception e) {
			Log.e(TAG, "error=" + e.getMessage());
			e.printStackTrace();
		}

		return 1L;
	}

	@Override
	protected void onPostExecute(Long result) {
		activity.setResult(Activity.RESULT_OK, null);
		activity.finish();
	}

}
