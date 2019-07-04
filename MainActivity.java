package dolostar.dolostar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.HttpAuthHandler;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {

    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    /*private WebView webView;*/
    private WebSettings webSettings;
    //    private ValueCallback<Uri[]> mUploadMessage;
    private String mCameraPhotoPath = null;
    private long size = 0;

    private  CustomWebView mWebView;
    private CustomWebView mWebviewPop;
    private RelativeLayout noDataRL;
    private ProgressBar mProgressBar;
    private Button retry;
    private boolean ISBTNCLICKED=false;

    private ValueCallback<Uri> mUploadMessage;
    private final static int FILECHOOSER_RESULTCODE = 1;
//    private static final String target_url_prefix="https://m.facebook.com/v2.5/dialog/oauth";


    private String HomePageURL = "https://www.dolostar.com/webview-landing";
    //    private String mainURL = "http://athlete.runindiarun.org.in/coach"; //http://172.26.1.7/sportobuddy/public/webview
    private String mainURL = "https://www.dolostar.com/webview-landing"; //http://172.26.1.7/cricket/public/
    private FrameLayout mContainer;
    private String mCM;
    private ValueCallback<Uri> mUM;
    private ValueCallback<Uri[]> mUMA;
    private final static int FCR=1;
    private GoogleAnalytics sAnalytics;
    private Tracker sTracker;
    String activityname[];
    private Context mContext;
    private String[] split_one;
    private String[] split_two;
    private String googleauthredirecturl;
    private Uri uri;
    private String urlString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initWebview();

//        InternalContext.getInstance().setBaseContext(this);
        mContext=this.getApplicationContext();
        sAnalytics = GoogleAnalytics.getInstance(this);
        sTracker=getDefaultTracker();

        if(Build.VERSION.SDK_INT >=23 && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
        }
        CookieManager cookieManager=CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        if(getIntent().getExtras()!=null){
            mainURL=getIntent().getExtras().getString("mainURL");
        }else{

        }
//        mWebView.getSettings().setUserAgentString("Chrome/56.0.0.0 Mobile");
    }

    private void initView() {
        mContainer = (FrameLayout) findViewById(R.id.mContainer);
        mWebView = (CustomWebView) findViewById(R.id.webviews);
        noDataRL = (RelativeLayout)findViewById(R.id.noDataRL);
        mProgressBar= (ProgressBar) findViewById(R.id.mProgressBar);
        retry=(Button)findViewById(R.id.retry);
    }

    private void initWebview() {
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);
        settings.setAllowFileAccess(true);
        if(Build.VERSION.SDK_INT >= 21){
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }else if(Build.VERSION.SDK_INT >= 19){
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }else if(Build.VERSION.SDK_INT < 19){
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        if (isNetworkAvailable()){
            mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
            mWebView.setWebChromeClient(new MyWebChromeClient());
            mWebView.loadUrl(mainURL);
            mWebView.setVisibility(View.VISIBLE);
            noDataRL.setVisibility(View.GONE);
        }else{
            mWebView.setVisibility(View.GONE);
            noDataRL.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
//            singelButtonAlertDialog(this,"Ok","No internet Available.","Please try aagain after sometime.",true);
//            Toast.makeText(getApplicationContext(),"No Internet Available.",Toast.LENGTH_SHORT).show();
        }
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()){
//                        mWebView.reload();
                    ISBTNCLICKED=true;
                    mWebView.loadUrl( "javascript:window.location.reload( true )" );
                }else{
                    Toast.makeText(getApplicationContext(),"No Internet Available.",Toast.LENGTH_SHORT).show();

                }
            }
        });
        mWebViewClicked();

        mWebView.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                request.setMimeType(mimetype);
                //------------------------COOKIE!!------------------------
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                //------------------------COOKIE!!------------------------
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading file...");
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype));
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();


            }
        });
    }


    synchronized public Tracker getDefaultTracker() {
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        if (sTracker == null) {
            sTracker = sAnalytics.newTracker("UA-121816562-1");
        }

        return sTracker;
    }

    private class MyCustomWebViewClient extends WebViewClient {


//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request)

        @Override
        public boolean shouldOverrideUrlLoading(WebView webview, String url) {
            String host = Uri.parse(url).getHost();

            Log.d("url swsap ",url);
            if( url.startsWith("http:") || url.startsWith("https:") ) {

                if (host.equals("")) {
                    if (mWebView != null) {
                        mWebView.setVisibility(View.GONE);
                        mContainer.removeView(mWebView);
                        mWebView = null;
                    }
                    return false;
                }
                if (host.equals("m.facebook.com") || host.equals("www.facebook.com") || host.equals("facebook.com") || host.equals("mobile.facebook.com")) {
                    return false;
                }

                if (host.equals("m.google.com") || host.equals("www.google.com") || host.equals("google.com") || host.startsWith("https://accounts.google.com")) {
                    return false;
                }
                if(url.startsWith("https://accounts.google.com")){
                    return false;
                }

                if(url.startsWith("https://www4.ipg-online.com/connect/gateway/redirectAsPost")){
                    return false;
                }
                //https://www4.ipg-online.com/connect/gateway/processing?execution=e2s1
                //https://www4.ipg-online.com/connect/gateway/processing?
                if(url.startsWith("https://www4.ipg-online.com/connect/gateway/processing?")){
                    return false;
                }


                // Otherwise, the link is not for a page on my site, so launch
                // another Activity that handles URLs
               /* Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;*/
            }
            // Otherwise allow the OS to handle it
            else if(url.startsWith("webviewshare:")){
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Put Data Here");
                startActivity(Intent.createChooser(shareIntent,("How do you want to share")));
                return true;
            }
            /*else if (url.startsWith("tel:")) {
                Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                startActivity(tel);
                return true;
            }
            //This is again specific for my website
            else if (url.startsWith("mailto:")) {
                Intent mail = new Intent(Intent.ACTION_SEND);
                mail.setType("application/octet-stream");
                String AdressMail = new String(url.replace("mailto:" , "")) ;
                mail.putExtra(Intent.EXTRA_EMAIL, new String[]{ AdressMail });
                mail.putExtra(Intent.EXTRA_SUBJECT, "");
                mail.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(mail);
                return true;
            }*/
            else{
                return UrlLoadingMethod(url.toString());
            }
            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            mProgressBar.setVisibility(View.GONE);
            Log.d("onReceivedSslError", "onReceivedSslError");
            if (isNetworkAvailable()) {
                mWebView.reload();
            }

            //super.onReceivedSslError(view, handler, error);
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
            if (isNetworkAvailable()) {
                mWebView.reload();
            }

        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (isNetworkAvailable()) {
                mWebView.reload();
            }

        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.v("sandeep ","sandeep sss"+url);
            sendScreenName(url);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d("Url",url);
            mProgressBar.setVisibility(View.GONE);

            try {
                String host = Uri.parse(url).getHost();
                if(host.equals("m.facebook.com") || host.equals("mobile.facebook.com "))
                {
                    if(url.contains("oauth?")){
                        if(mWebviewPop!=null)
                        {
                            mWebviewPop.setVisibility(View.GONE);
                            mContainer.removeView(mWebviewPop);
                            mWebviewPop=null;
                        }
                        onResume();
//                webView.loadUrl("https://www.dolostar.com/dashboard");
                        return;
                    }
                }
            }catch (Exception d){}


            if(url.startsWith("https://accounts.google.com/o/oauth2/auth?"  ) ){

                /*split_one=url.split("redirect_uri=");
                split_two=split_one[1].split("&client_id");
                Log.v("gooleauthredirecturi...",split_two[0].toString());
                googleauthredirecturl=split_two[0].toString();*/
                try {
                    if(mWebviewPop!=null)
                    {
                        mWebviewPop.setVisibility(View.GONE);
                        mContainer.removeView(mWebviewPop);
                        mWebviewPop=null;
                    }
                }catch (Exception d){}

//                mWebView.loadUrl(googleauthredirecturl);
                onResume();
                return;
            }

            if(url.startsWith("https://accounts.google.com/o/oauth2/approval?"  ) ){

                if(mWebviewPop!=null)
                {
                    mWebviewPop.setVisibility(View.GONE);
                    mContainer.removeView(mWebviewPop);
                    mWebviewPop=null;
                }
//                uri=Uri.parse(googleauthredirecturl);
//                urlString=uri.buildUpon().build().toString();
//                mWebView.loadUrl(urlString);
                onResume();
                return;
            }


            //https://api.razorpay.com/v1/payments/pay_AlEcvP9diey8Se/callback/50e0c7b9ade71d45b061ab28e7a03213516b10eb/rzp_live_RC2jGZrXwSwEL7
            if(url.startsWith("https://api.razorpay.com/v1/payments"  ) ){

                try {
                    if(mWebviewPop!=null)
                    {
                        mWebviewPop.setVisibility(View.GONE);
                        mContainer.removeView(mWebviewPop);
                        mWebviewPop=null;
                    }
                }catch (Exception d){}

//                uri=Uri.parse(googleauthredirecturl);
//                urlString=uri.buildUpon().build().toString();
//                mWebView.loadUrl(urlString);
                onResume();
                return;
            }

            if (ISBTNCLICKED){
                if (isNetworkAvailable()){
                    mWebView.setVisibility(View.VISIBLE);
                    noDataRL.setVisibility(View.GONE);
                }else{
//                        mWebView.setVisibility(View.GONE);
//                        noDataRL.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
//                    Toast.makeText(getApplicationContext(),"No Internet Available.",Toast.LENGTH_SHORT).show();
                }

                ISBTNCLICKED=false;
            }

            mProgressBar.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            super.onReceivedHttpAuthRequest(view, handler, host, realm);
        }

        @Override
        public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
            super.onReceivedLoginRequest(view, realm, account, args);
        }


    }


    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public void mWebViewClicked(){


        mWebView.setWebViewClient(new WebViewClient() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//                SetHeaderMethod(request.getUrl().toString());

                return UrlLoadingMethod(request.getUrl().toString());

            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                SetHeaderMethod(url.toString());
                Log.v("url",url);
                return UrlLoadingMethod(url.toString());
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                sendScreenName(url);
                super.onPageStarted(view, url, favicon);
                mProgressBar.setVisibility(View.VISIBLE);
//                Toast.makeText(MainActivity.this, "url "+url, Toast.LENGTH_SHORT).show();

            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String encodedurl="";
                CookieSyncManager.getInstance().sync();

                Log.d("url...",url);

                if(url.startsWith("https://m.facebook.com/v2.5/dialog/oauth?")){
                    if(mWebviewPop!=null)
                    {
                        mWebviewPop.setVisibility(View.GONE);
                        mContainer.removeView(mWebviewPop);
                        mWebviewPop=null;
                    }
                    onResume();
//                webView.loadUrl("https://www.dolostar.com/dashboard");
                    return;
                }


                if(url.startsWith("https://accounts.google.com/o/oauth2/auth?"  ) ){
                    if(mWebviewPop!=null)
                    {
                        mWebviewPop.setVisibility(View.GONE);
                        mContainer.removeView(mWebviewPop);
                        mWebviewPop=null;
                    }
                    onResume();
                    return;
                }

                if(url.startsWith("https://accounts.google.com/o/oauth2/approval?"  ) ){
                    if(mWebviewPop!=null)
                    {
                        mWebviewPop.setVisibility(View.GONE);
                        mContainer.removeView(mWebviewPop);
                        mWebviewPop=null;
                    }
                    onResume();
                    return;
                }

                try {
                    encodedurl = URLEncoder.encode(url, "UTF-8");
                    Log.d("Cookie...", "url: " + url + ", cookies: " + CookieManager.getInstance().getCookie(url));
                }catch (Exception e){

                }

                if (ISBTNCLICKED){
                    if (isNetworkAvailable()){
                        mWebView.setVisibility(View.VISIBLE);
                        noDataRL.setVisibility(View.GONE);
                    }else{
//                        mWebView.setVisibility(View.GONE);
//                        noDataRL.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
//                    Toast.makeText(getApplicationContext(),"No Internet Available.",Toast.LENGTH_SHORT).show();
                    }

                    ISBTNCLICKED=false;
                }

                mProgressBar.setVisibility(View.GONE);

            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                Log.v("errorCode rrrrr ","onReceivedError "+error);
//                mWebView.setVisibility(View.GONE);
//                noDataRL.setVisibility(View.VISIBLE);
                if (isNetworkAvailable()) {
                    mWebView.reload();
                }

            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                Log.v("errorCode rrrrr ","onReceivedHttpError "+errorResponse);

//                mWebView.setVisibility(View.GONE);
//                noDataRL.setVisibility(View.VISIBLE);
                if (isNetworkAvailable()){
                    mWebView.reload();
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.v("errorCode X","onReceivedError "+errorCode);
//                mWebView.setVisibility(View.GONE);
//                noDataRL.setVisibility(View.VISIBLE);
                if (isNetworkAvailable()){
                    mWebView.reload();
                }
            }

            @Override
            public void onReceivedSslError(WebView view,
                                           SslErrorHandler handler, SslError error) {
                mProgressBar.setVisibility(View.GONE);
//                mWebView.setVisibility(View.GONE);
//                noDataRL.setVisibility(View.VISIBLE);
                if (isNetworkAvailable()){
                    mWebView.reload();
                }
                //Toast.makeText(TableContentsWithDisplay.this, "error "+error, Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void sendScreenName(String url) {

        Log.v("url","sandeep kushwah :"+url);

        if(url.contains("https://www.dolostar.com")) {
            Log.d("url... :", url);
            if(url.equalsIgnoreCase("https://www.dolostar.com/")) {
                sTracker.setScreenName("Activity~" + "Login");
            }
            else {
                activityname = url.split(".com/");
                sTracker.setScreenName("Activity~" + activityname[1]);
            }
            sTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }else {
            sTracker.setScreenName("Activity~" + url);
            sTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }

    }

    public void clearSession(){
        /*CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(MainActivity.this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        mWebView.clearCache(true);
        mWebView.clearHistory();*/
    }

    private void ActivityFinishAlert(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
        builder1.setMessage("Do you want to exit!");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        Log.v("onactivityResult....","results...."+intent.getData());

        /*if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            Uri result = intent == null || resultCode != RESULT_OK ? null
                    : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;

        }*/

        if(Build.VERSION.SDK_INT >= 21){
            Uri[] results = null;
            //Check if response is positive
            if(resultCode== Activity.RESULT_OK){
                if(requestCode == FCR){
                    if(null == mUMA){
                        return;
                    }
                    if(intent == null || intent.getData() == null){
                        //Capture Photo if no image available
                        if(mCM != null){
                            results = new Uri[]{Uri.parse(mCM)};
                        }
                    }else{
                        String dataString = intent.getDataString();
                        if(dataString != null){
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    }
                }
            }
            mUMA.onReceiveValue(results);
            mUMA = null;
        }else{
            if(requestCode == FCR){
                if(null == mUM) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                mUM.onReceiveValue(result);
                mUM = null;
            }
        }

    }

    class MyWebChromeClient extends WebChromeClient {


        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            Log.d(TAG,"url "+callback.toString());
            view.setVisibility(View.GONE);
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            Log.d(TAG,"url "+url);
            result.confirm();

            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            Log.d(TAG,"url "+url);
            result.confirm();
            return true;
        }

        @Override
        public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
            Log.d(TAG,"url "+url);
            result.confirm();
            return true;
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("javaScript dialog")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new AlertDialog.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    result.confirm();
                                }
                            })
                    .setCancelable(false)
                    .create()
                    .show();

            return true;
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, Message resultMsg) {

            mWebviewPop = new CustomWebView(mContext);

//            mWebviewPop.loadDataWithBaseURL();
            mWebviewPop.setVerticalScrollBarEnabled(true);
            mWebviewPop.setHorizontalScrollBarEnabled(true);
//            mWebviewPop.getSettings().setUserAgentString("Chrome/56.0.0.0 Mobile");

            mWebviewPop.setWebViewClient(new MyCustomWebViewClient());
            mWebviewPop.getSettings().setJavaScriptEnabled(true);
            mWebviewPop.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            mWebviewPop.getSettings().setSavePassword(false);
            mWebviewPop.getSettings().setSupportZoom(true);
            mWebviewPop.getSettings().setBuiltInZoomControls(true);
            mWebviewPop.getSettings().setSupportMultipleWindows(true);
            mWebviewPop.setWebChromeClient(new MyWebChromeClient());
            mWebviewPop.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));



            mContainer.addView(mWebviewPop);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(mWebviewPop);
            resultMsg.sendToTarget();
            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
            Log.d("onCloseWindow", "called");

        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg){
            mUM = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            MainActivity.this.startActivityForResult(Intent.createChooser(i,"File Chooser"), FCR);
        }
        // For Android 3.0+, above method not supported in some android 3+ versions, in such case we use this
        public void openFileChooser(ValueCallback uploadMsg, String acceptType){
            mUM = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            MainActivity.this.startActivityForResult(
                    Intent.createChooser(i, "File Browser"),
                    FCR);
        }
        //For Android 4.1+
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture){
            mUM = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            MainActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), MainActivity.FCR);
        }
        //For Android 5.0+
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                         FileChooserParams fileChooserParams){
            if(mUMA != null){
                mUMA.onReceiveValue(null);
            }
            mUMA = filePathCallback;
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null){
                File photoFile = null;
                try{
                    photoFile = createImageFile();
                    takePictureIntent.putExtra("PhotoPath", mCM);
                }catch(IOException ex){
                    Log.e(TAG, "Image file creation failed", ex);
                }
                if(photoFile != null){
                    mCM = "file:" + photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                }else{
                    takePictureIntent = null;
                }
            }
            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("*/*");
            Intent[] intentArray;
            if(takePictureIntent != null){
                intentArray = new Intent[]{takePictureIntent};
            }else{
                intentArray = new Intent[0];
            }

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
            startActivityForResult(chooserIntent, FCR);
            return true;
        }


    }
    @Override
    public void onBackPressed() {

        try {
            if (mWebView.getUrl().equalsIgnoreCase("https://www.dolostar.com/dashboard")  ){
                ActivityFinishAlert();
//                finish();
                DailogBackPress();
            }else{
                if(mWebviewPop!=null){
                    if(mWebviewPop.canGoBack()) {
                        mWebviewPop.goBack();
                    }
                }
                if (mWebView.canGoBack()) {
                    if ( mWebView.getUrl().equalsIgnoreCase(HomePageURL)){
                        ActivityFinishAlert();
                    }else{
                        mWebView.goBack();
                    }
                } else {
                    ActivityFinishAlert();
                }
            }
        }catch (Exception d){}

    }
    public void DailogBackPress(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Do you want to Exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user pressed "yes", then he is allowed to exit from application
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user select "No", just cancel this dialog and continue with app
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private File createImageFile() throws IOException{
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_"+timeStamp+"_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName,".jpg",storageDir);
    }

    /*@Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event){
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            switch(keyCode){
                case KeyEvent.KEYCODE_BACK:
                    if(mWebView.canGoBack()){
                        mWebView.goBack();
                    }else{
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }*/

    /*@Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }*/
    public Boolean UrlLoadingMethod(final String request) {

        if( request.toString().startsWith("http:") || request.toString().startsWith("https:") ) {
            return false;
        }

        // Otherwise allow the OS to handle it
        else if (request.toString().startsWith("tel:")) {
            Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(request.toString()));
            startActivity(tel);
            return true;
        }
        else if (request.toString().startsWith("mailto:")) {

            URL url = null;
            String[] body ;
            String subject1=null;
            String[] subject = request.split("subject=");

            body=subject[1].split("body=");
            try {
                url= new URL(URLDecoder.decode(body[1], "UTF-8"));
//                subject1= new URL(URLDecoder.decode(body[0], "UTF-8"));

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            subject1=body[0].toString().replace("%20"," ");

            Intent mail = new Intent(Intent.ACTION_SEND);
            mail.setType("text/plain");
            mail.putExtra(Intent.EXTRA_EMAIL, new String[]{""});
            mail.putExtra(Intent.EXTRA_SUBJECT, subject1);
            mail.putExtra(Intent.EXTRA_TEXT, url.toString());
            startActivity(mail);
            return true;
        }
        else if (request.toString().startsWith("whatsapp:")) {
            PackageManager pm=getPackageManager();
            try {

                String[] body ;
                String url=null;
                body=request.split("text=");

                try {
                    url= (URLDecoder.decode(body[1], "UTF-8"));
//                    url =new URL(body[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Intent waIntent = new Intent(Intent.ACTION_SEND);
                waIntent.setType("text/plain");
                PackageInfo info=pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
                //Check if package exists or not. If not then code
                //in catch block will be called
                waIntent.setPackage("com.whatsapp");

                waIntent.putExtra(Intent.EXTRA_TEXT,body[1].toString());
                startActivity(Intent.createChooser(waIntent, "Share with"));

            } catch (PackageManager.NameNotFoundException e) {
                Toast.makeText(MainActivity.this, "WhatsApp not Installed", Toast.LENGTH_SHORT)
                        .show();
            }

            return true;
        } else if (request.toString().startsWith("webviewshare:")){
//            SetReloadUrl=true;
            String whatsaapShareContent="";
            String[] item = request.toString().split(":");
            try {
                whatsaapShareContent = (URLDecoder.decode(item[1], "UTF-8"));
            }catch (Exception e){

            }
            String text=item[1].replace("+"," ");

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, whatsaapShareContent);
                startActivity(Intent.createChooser(shareIntent,"Select Share options."));
            }catch (Exception d){
                d.printStackTrace();
            }
            return true;
        }else{
//            mGetParamsFromUrl(request);
            return true;
        }

//        return true;

    }


}
