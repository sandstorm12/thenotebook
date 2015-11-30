package sandstorm.com.thenotebook.ui.AccountManagment;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.DataInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Scanner;
import java.util.TimeZone;

import sandstorm.com.thenotebook.R;
import sandstorm.com.thenotebook.services.InfoService;
import util.IabHelper;
import util.IabResult;
import util.Inventory;
import util.Purchase;

public class Upgrade extends AppCompatActivity
{
    static final String TAG = "myData";

    IabHelper mHelper;
    InfoService is;
    AlertDialog ad;
    Button monthly, yearly;
    String PREMIUM_FOR_MONTH = "";
    String PREMIUM_FOR_YEAR = "";

    ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try{
                is = ((InfoService.MyBinder)service).getService();
                setupIAB();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.black));
            }

            Intent i = new Intent(getApplicationContext(), InfoService.class);
            bindService(i,sc,0);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void setupIAB()
    {
        try{
            String base64EncodedPublicKey = getString(R.string.public_key);

            mHelper = new IabHelper(this, base64EncodedPublicKey);

            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if(result.getResponse() == IabHelper.BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE){
                        final AlertDialog.Builder adb = new AlertDialog.Builder(getApplicationContext());
                        adb.setTitle(getString(R.string.notice));
                        adb.setMessage(getString(R.string.bazaar_not_installed));
                        adb.setCancelable(false);
                        adb.setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try{
                                    finish();
                                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_in);
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                        ad = adb.create();
                        ad.show();
                    }else if (!result.isSuccess()) {
                        Toast.makeText(getApplicationContext(),getString(R.string.cannot_connect_to_purchase_server),Toast.LENGTH_LONG).show();
                    }
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");
            if (result.isFailure()) {
                Toast.makeText(getApplicationContext(),getString(R.string.cannot_get_your_account_information_from_bazaar),Toast.LENGTH_LONG).show();
                return;
            }
            else {
                if(inventory.hasPurchase(PREMIUM_FOR_MONTH) || inventory.hasPurchase(PREMIUM_FOR_YEAR)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.you_are_premium_already), Toast.LENGTH_LONG).show();
                    finish();
                }

                getSKU();
            }
        }
    };

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                Toast.makeText(getApplicationContext(),getString(R.string.purchase_falied),Toast.LENGTH_LONG).show();
                return;
            }
            else if (purchase.getSku().equals(PREMIUM_FOR_MONTH)) {
                Toast.makeText(getApplicationContext(), getString(R.string.purchase_success), Toast.LENGTH_LONG).show();
                activePremium();
                finish();
            }else if(purchase.getSku().equals(PREMIUM_FOR_YEAR)){
                Toast.makeText(getApplicationContext(), getString(R.string.purchase_success), Toast.LENGTH_LONG).show();
                activePremium();
                finish();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    @Override
    protected void onDestroy() {
        try{
            mHelper.dispose();
            unbindService(sc);
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private void activePremium()
    {
        try{
            is.getCacheData().setContentByName("isPremium","1");
            is.saveData(is.getCacheData());

                    AlertDialog.Builder adb = new AlertDialog.Builder(getApplicationContext());
                    adb.setTitle(getString(R.string.notice));
                    adb.setMessage(getString(R.string.success_message));
                    adb.setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ad.dismiss();
                            finish();
                            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_in);
                        }
                    });
                    adb.setCancelable(false);
                    ad = adb.create();
                    ad.show();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void purchase(String sku)
    {
        try{
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            mHelper.launchPurchaseFlow(this,sku,1,mPurchaseFinishedListener,is.getCacheData().getContentByName("email")+" "+Long.toString(c.getTimeInMillis()));
        }catch(Exception e){
            e.printStackTrace();
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

    private void getSKU()
    {
        try{
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        URL url = new URL("http://sandstormweb.com/the_notebook/SKU.php");
                        HttpURLConnection huc = (HttpURLConnection)url.openConnection();
                        huc.setRequestMethod("POST");

                        DataInputStream dis = new DataInputStream(huc.getInputStream());
                        Scanner sc = new Scanner(dis).useDelimiter("\\A");

                        PREMIUM_FOR_MONTH = sc.nextLine();
                        PREMIUM_FOR_YEAR = sc.nextLine();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    setContentView(R.layout.upgrade);
                                    monthly = (Button)findViewById(R.id.upgrade_monthly);
                                    yearly = (Button)findViewById(R.id.upgrade_yearly);

                                    monthly.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            try{
                                                purchase(PREMIUM_FOR_MONTH);
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                    });

                                    yearly.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            try{
                                                purchase(PREMIUM_FOR_YEAR);
                                            }catch(Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
