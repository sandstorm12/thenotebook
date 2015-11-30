package sandstorm.com.thenotebook.services;

import android.app.Service;
import android.content.Intent;
import android.database.*;
import android.database.sqlite.SQLiteDatabase;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.DataInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import sandstorm.com.thenotebook.R;
import sandstorm.com.thenotebook.data_structures.*;
import sandstorm.com.thenotebook.data_structures.micro_content.InfoItem;
import sandstorm.com.thenotebook.functions.Functions;
import util.IabHelper;
import util.IabResult;
import util.Inventory;

public class InfoService extends Service
{
    private InfoBundle ib = new InfoBundle();
    private SQLiteDatabase myDatabase;
    private Cursor c;
    private String PREMIUM_FOR_MONTH = "";
    private String PREMIUM_FOR_YEAR = "";

    IabHelper mHelper;
    Handler handler;

    public interface OnInfoLoaded
    {
        public void onInfoLoaded();
    }
    public interface OnInfoSaved
    {
        public void onInfoSaved();
    }
    public class MyBinder extends Binder
    {
        public InfoService getService()
        {
            return InfoService.this;
        }
    }

    private ArrayList<OnInfoLoaded> constantInfoLoadedListener = new ArrayList();
    private ArrayList<OnInfoSaved> constantInfoSavedListener = new ArrayList();
    private ArrayList<OnInfoLoaded> infoLoadedListener = new ArrayList();
    private ArrayList<OnInfoSaved> infoSavedListener = new ArrayList();

    private final int RESULT_EXISTS = -1;
    private final int RESULT_NOT_EXISTS = -2;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try{
            handler = new Handler();
            loadData();
        }catch(Exception e){
            e.printStackTrace();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public synchronized void addOnInfoLoadedListener(OnInfoLoaded onInfoLoaded)
    {
        this.infoLoadedListener.add(onInfoLoaded);
    }

    public synchronized void addOnInfoSavedListener(OnInfoSaved onInfoSaved)
    {
        this.infoSavedListener.add(onInfoSaved);
    }

    public synchronized void addConstantOnInfoLoadedListener(OnInfoLoaded onInfoLoaded)
    {
        this.constantInfoLoadedListener.add(onInfoLoaded);
    }

    public synchronized void addConstantOnInfoSavedListener(OnInfoSaved onInfoSaved)
    {
        this.constantInfoSavedListener.add(onInfoSaved);
    }

    public synchronized void loadData()
    {
        try{
            if(myDatabase == null) myDatabase = openOrCreateDatabase("DATABASE",MODE_PRIVATE,null);
            if(!myDatabase.isOpen()) myDatabase = openOrCreateDatabase("DATABASE",MODE_PRIVATE,null);
            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS info (name VARCHAR AUTO_INCREMENT, content VARCHAR AUTO_INCREMENT)");
            c = myDatabase.rawQuery("SELECT name,content FROM info WHERE 1",null);
            c.moveToFirst();

            if(c.getCount() == 0){
                initializeInfo();
                return;
            }

            ArrayList<InfoItem> data = new ArrayList();
            for(int i = 0; i < c.getCount(); i++)
            {
                InfoItem temp = new InfoItem(c.getString(0),c.getString(1));
                data.add(temp);
                c.moveToNext();
            }

            ib.setData(data);

            dispatchLoad();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            c.close();
            myDatabase.close();
        }
    }

    public synchronized void saveData(InfoBundle infoBundle)
    {
        try{
            if(!myDatabase.isOpen()) myDatabase = openOrCreateDatabase("DATABASE",MODE_PRIVATE,null);
            ib = infoBundle;

            for(int i = 0; i < ib.getSize(); i++)
            {
                if(!isRecordExists(ib.getData().get(i).getName()))
                {
                    c = myDatabase.rawQuery("INSERT INTO info (name,content) VALUES(\"" + ib.getData().get(i).getName() + "\",\"" + ib.getData().get(i).getContent() + "\")", null);
                    c.moveToFirst();
                }else{
                    c = myDatabase.rawQuery("UPDATE info SET content=\""+ib.getData().get(i).getContent()+"\" WHERE name=\""+ib.getData().get(i).getName()+"\"",null);
                    c.moveToFirst();
                }
            }

            c.close();
            dispatchSave();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            c.close();
            myDatabase.close();
        }
    }

    public synchronized void initializeInfo()
    {
        try{
            ArrayList<InfoItem> data = new ArrayList();
            InfoItem ii = new InfoItem("sortBy","0");
            data.add(ii);
            ii = new InfoItem("sortDirection","0");
            data.add(ii);
            ii = new InfoItem("email","null");
            data.add(ii);
            ii = new InfoItem("isPremium","0");
            data.add(ii);
            ii = new InfoItem("alarm_uri","android.resource://sandstorm.com.thenotebook/"+ R.raw.hardwell);
            data.add(ii);
            ii = new InfoItem("interval","86400000");
            data.add(ii);
            ii = new InfoItem("auto_sync","0");
            data.add(ii);
            ii = new InfoItem("lock","0");
            data.add(ii);
            ii = new InfoItem("password","null");
            data.add(ii);
            ii = new InfoItem("mb","1000");
            data.add(ii);


            ib.setData(data);

            saveData(ib);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public synchronized InfoBundle getCacheData()
    {
        return ib;
    }

    private synchronized void dispatchSave()
    {
        try{
            loadData();

            for(int i = 0; i < infoSavedListener.size(); i++)
            {
                infoSavedListener.get(i).onInfoSaved();
            }

            infoSavedListener = new ArrayList();

            for(int i = 0; i < constantInfoSavedListener.size(); i++)
            {
                constantInfoSavedListener.get(i).onInfoSaved();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private synchronized void dispatchLoad()
    {
        try{
            for(int i = 0; i < infoLoadedListener.size(); i++)
            {
                infoLoadedListener.get(i).onInfoLoaded();
            }

            infoLoadedListener = new ArrayList();

            for(int i = 0; i < constantInfoLoadedListener.size(); i++)
            {
                constantInfoLoadedListener.get(i).onInfoLoaded();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private synchronized boolean isRecordExists(String name)
    {
        try{
            c = myDatabase.rawQuery("SELECT name FROM info WHERE 1",null);
            c.moveToFirst();

            for(int i = 0; i < c.getCount(); i++)
            {
                if(name.equalsIgnoreCase(c.getString(0))) return true;
                c.moveToNext();
            }

        }catch(Exception e){
            e.printStackTrace();
        }finally {
            c.close();
        }
        return false;
    }

    public void updatePremium()
    {
        try{
            if(Functions.isConnectedToInternet(getApplicationContext())) {
                setupIAB();
            }else{
                Toast.makeText(getApplicationContext(),getString(R.string.not_connceted),Toast.LENGTH_LONG).show();
            }
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
                    if (!result.isSuccess()) {
                        Toast.makeText(getApplicationContext(), getString(R.string.cannot_connect_to_purchase_server), Toast.LENGTH_LONG).show();
                    }else{
                        getSKU();
                    }
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            try {
                if (result.isSuccess()) {
                    if (inventory.hasPurchase(PREMIUM_FOR_MONTH) || inventory.hasPurchase(PREMIUM_FOR_YEAR)) {
                        ib.setContentByName("isPremium", "1");
                        Log.d("myData","is premium : true");
                    } else {
                        Log.d("myData","is premium : false");
                        ib.setContentByName("isPremium", "0");
                        ib.setContentByName("auto_sync","0");
                    }
                    saveData(ib);
                    Toast.makeText(getApplicationContext(),getString(R.string.account_information_updated),Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(),getString(R.string.database_not_responding),Toast.LENGTH_LONG).show();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    };

    public void eraseAllData()
    {
        try{
            deleteDatabase("DATABASE");
            Functions.deleteFilesInDirectory(getFilesDir());
        }catch(Exception e){
            e.printStackTrace();
        }
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

                        Log.d("myData","going to check premium");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    mHelper.queryInventoryAsync(mGotInventoryListener);
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    }catch(Exception e){
                        e.printStackTrace();
                        Log.d("myData","error here &&&&");
                    }
                }
            });
            t.start();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
