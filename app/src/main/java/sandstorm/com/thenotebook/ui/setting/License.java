package sandstorm.com.thenotebook.ui.setting;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import sandstorm.com.thenotebook.R;

public class License extends AppCompatActivity
{
    Toolbar tb;
    WebView wv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.black));
            }

            setContentView(R.layout.terms);

            initializeComponents();

            initializeToolbar();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void initializeComponents()
    {
        try{
            tb = (Toolbar)findViewById(R.id.terms_toolbar);
            tb.setTitle(getString(R.string.license));
            wv = (WebView)findViewById(R.id.terms_web_view);

            wv.setWebViewClient(new WebViewController());
            wv.loadUrl("http://sandstormweb.com/the_notebook/license.html");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void initializeToolbar()
    {
        try{
            setSupportActionBar(tb);

            getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try{
            if(item.getItemId() == android.R.id.home)
            {
                finish();
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return super.onOptionsItemSelected(item);
    }

    public class WebViewController extends WebViewClient
    {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        try{
            finish();
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onBackPressed();
    }
}
