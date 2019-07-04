package dolostar.dolostar;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Admin on 07-03-2017.
 */
public class CustomWebView extends WebView {
    public CustomWebView(Context context) {
        super(context);
        initView();
    }

    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CustomWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public CustomWebView(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(context, attrs, defStyleAttr, privateBrowsing);
        initView();
    }


    private void initView() {

        getSettings().setJavaScriptEnabled(true);
//        setBackgroundColor(Color.parseColor("#808080"));

        getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        //Set whether the DOM storage API is enabled.
        getSettings().setDomStorageEnabled(true);
        setWebViewClient(new WebViewClient());
        WebSettings webSettings = getSettings();
        webSettings.setDomStorageEnabled(true);
        getSettings().setJavaScriptEnabled(true);
        getSettings().setDomStorageEnabled(true);
        //setBuiltInZoomControls = false, removes +/- controls on screen
        getSettings().setBuiltInZoomControls(false);

        getSettings().setPluginState(WebSettings.PluginState.ON);
        getSettings().setAllowFileAccess(true);

        getSettings().setAppCacheMaxSize(1024 * 8);
        getSettings().setAppCacheEnabled(true);

        getSettings().setUseWideViewPort(false);
        setWebChromeClient(new WebChromeClient());
    }
}
