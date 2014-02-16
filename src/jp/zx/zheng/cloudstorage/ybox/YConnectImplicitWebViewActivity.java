package jp.zx.zheng.cloudstorage.ybox;

import jp.co.yahoo.yconnect.YConnectImplicit;
import jp.co.yahoo.yconnect.core.oidc.OIDCDisplay;
import jp.co.yahoo.yconnect.core.oidc.OIDCPrompt;
import jp.co.yahoo.yconnect.core.oidc.OIDCScope;
import jp.co.yahoo.yconnect.core.util.YConnectLogger;
import jp.zx.zheng.musictest.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * Implicit with WebView Sample Activity
 *
 * @author Copyright (C) 2012 Yahoo Japan Corporation. All Rights Reserved.
 *
 */
@SuppressLint("SetJavaScriptEnabled")
public class YConnectImplicitWebViewActivity extends Activity {

	private final static String TAG = YConnectImplicitWebViewActivity.class.getSimpleName();

	// アプリケーションID
	public final static String clientId = "dj0zaiZpPTBpVGFSVVVUZFgyeCZzPWNvbnN1bWVyc2VjcmV0Jng9ZDY-";

	// コールバックURI
	// (アプリケーションID発行時に登録したURI)
	public final static String customUriScheme = "yj-43ca2://cb";

	public final static String YCONNECT_PREFERENCE_NAME = "yconnect";

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.webauth);
		SharedPreferences sharedPreferences = getSharedPreferences(YCONNECT_PREFERENCE_NAME, Activity.MODE_PRIVATE);

		/****************************************************************
		     Request Authorization Endpoint for getting Access Token.
		 ****************************************************************/

		Log.i(TAG, "Request authorization.");

		// 各パラメーター初期化
		// リクエストとコールバック間の検証用のランダムな文字列を指定してください
		String state = "fj3qifjioajdsije545FDSAF";
		// リプレイアタック対策のランダムな文字列を指定してください
		String nonce = "fjioasjfoij4350485049385joJFKDjfioe4jdjf84jjf94jafhip9046";
		String display = OIDCDisplay.SMART_PHONE;
		String[] prompt = { OIDCPrompt.DEFAULT };
		String[] scope = { OIDCScope.OPENID };

		// state、nonceを保存
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("state", state);
		editor.putString("nonce", nonce);
		editor.commit();

		// ログレベル設定（必要に応じてレベルを設定してください）
		// YConnectLogger.setLogLevel(YConnectLogger.DEBUG);

		// YConnectインスタンス取得
		YConnectImplicit yconnect = YConnectImplicit.getInstance();
		// 各パラメーター初期化
		yconnect.init(clientId, customUriScheme, state, display, prompt, scope, nonce);
		// リクエストURIを生成
		Uri authorizationUri = yconnect.generateAuthorizationUri();

		WebView webView = (WebView)findViewById(R.id.webView1);
		webView.clearCache(true);
		// WebViewクラスにstate、nonceを設定
		webView.setWebViewClient(new YConnectImplicitWebViewClient(this));
		webView.setWebChromeClient(new WebChromeClient());
		webView.getSettings().setJavaScriptEnabled(true);

		// WebViewを起動(ブラウザーを起動して同意画面を表示)
		webView.loadUrl(authorizationUri.toString());

	}
}
