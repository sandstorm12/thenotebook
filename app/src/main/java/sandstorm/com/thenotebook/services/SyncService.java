package sandstorm.com.thenotebook.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.*;
import java.net.*;
import java.util.*;

import sandstorm.com.thenotebook.R;
import sandstorm.com.thenotebook.data_structures.AbstractNoteNotebook;
import sandstorm.com.thenotebook.data_structures.DataBundle;
import sandstorm.com.thenotebook.data_structures.HeavyNote;
import sandstorm.com.thenotebook.data_structures.Note;
import sandstorm.com.thenotebook.data_structures.Notebook;
import sandstorm.com.thenotebook.data_structures.micro_content.ContentImage;
import sandstorm.com.thenotebook.functions.Functions;
import sandstorm.com.thenotebook.ui.setting.Setting;

public class SyncService extends Service {
    private DataBundle db;
    private DataService ds;
    private InfoService is;
    private String currentTask;
    private Handler handler;
    private boolean isWaiting, isAuto;
    private long interval;
    private AlarmManager am;
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                ds = ((DataService.MyBinder) service).getService();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private ServiceConnection isc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                is = ((InfoService.MyBinder) service).getService();
                if(is.getCacheData().getContentByName("auto_sync").equals("1")){
                    startAutoSync();
                }
                is.addConstantOnInfoSavedListener(new InfoService.OnInfoSaved() {
                    @Override
                    public void onInfoSaved() {
                        Log.d("myData", "is auto sync changed : "+Boolean.toString(isAutoSyncChanged()));
                        if(isAutoSyncChanged()){
                            Intent i = new Intent();
                            PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), REQUEST_AUTO_SYNC, i, 0);
                            am.cancel(pi);
                            isAuto = false;

                            if(is.getCacheData().getContentByName("auto_sync").equals("1")){
                                startAutoSync();
                            }
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public BroadcastReceiver SyncReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try{
                if(intent.getAction().equals("sync")) {
                    Log.d("myData", "alarm received *************");
                    if (!isWaiting) waitForNetworkAndThen();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    };

    public class MyBinder extends Binder {
        public SyncService getService() {
            return SyncService.this;
        }
    }

    public interface OnSyncCompleteListener {
        public abstract void onSyncCompleted();
    }

    public interface OnSyncFailedListener {
        public abstract void onSyncFailed();
    }

    public interface OnSyncStartedListener {
        public void onSyncStart();
    }

    public interface OnCurrentNoteChangeListener {
        public void onCurrentNoteChange();
    }

    private ArrayList<OnSyncCompleteListener> syncCompleteListener = new ArrayList();
    private ArrayList<OnSyncFailedListener> syncFailedListener = new ArrayList();
    private ArrayList<OnSyncStartedListener> syncStartedListener = new ArrayList();
    private ArrayList<OnCurrentNoteChangeListener> noteChangedListener = new ArrayList();
    private ArrayList<OnSyncCompleteListener> constantSyncCompleteListener = new ArrayList();
    private ArrayList<OnSyncFailedListener> constantSyncFailedListener = new ArrayList();
    private ArrayList<OnSyncStartedListener> constantSyncStartedListener = new ArrayList();
    private ArrayList<OnCurrentNoteChangeListener> constantNoteChangedListener = new ArrayList();

    private int REQUEST_AUTO_SYNC = 85;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            handler = new Handler();
            bindService(new Intent(getApplicationContext(), InfoService.class), isc, 0);
            bindService(new Intent(getApplicationContext(), DataService.class), sc, 0);

            IntentFilter filter = new IntentFilter();
            filter.addAction("sync");
            registerReceiver(SyncReceiver, filter);

            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(SyncReceiver, filter);

            am = (AlarmManager)getSystemService(ALARM_SERVICE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        try {
            unbindService(sc);
            unbindService(isc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onUnbind(intent);
    }

    public void sync() {
        try {
            Log.d("myData","sync called");
            downloadHeaderAndStartSync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadHeaderAndStartSync() {
        try {
            final String email = Base64.encodeToString(is.getCacheData().getContentByName("email").getBytes("UTF-8"), 0);
            final String password = Base64.encodeToString(is.getCacheData().getContentByName("password").getBytes("UTF-8"),0);

            dispatchStart();
            currentTask = getString(R.string.download_header);
            dispatchNoteChange();

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL("http://sandstormweb.com//the_notebook//get_data_map.php");
                        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                        huc.setRequestMethod("POST");
                        huc.setDoOutput(true);
                        huc.setDoInput(true);

                        String EP = "email=" + email+"&password="+password;
                        byte[] data = EP.getBytes("UTF-8");

                        DataOutputStream dos = new DataOutputStream(huc.getOutputStream());
                        dos.write(data);
                        dos.close();

                        InputStreamReader isr = new InputStreamReader(huc.getInputStream());
                        Scanner sc = new Scanner(isr).useDelimiter("\\A");

                        String result = new String(Base64.decode(sc.nextLine(), 0), "UTF-8");

                        if(result.equalsIgnoreCase("false")) {
                            checkHeaderAndDoTheRest("");
                        }else if(result.equals("wrong")) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),getString(R.string.account_not_exists),Toast.LENGTH_LONG).show();
                                }
                            });
                            dispatchFailed();
                        }else if(result.equals("done")) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),getString(R.string.daily_limition),Toast.LENGTH_LONG).show();
                                }
                            });
                            dispatchFailed();
                        }else{
                            checkHeaderAndDoTheRest(result);
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                        dispatchFailed();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),getString(R.string.sync_failed),Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
            t.start();

        } catch (Exception e) {
            e.printStackTrace();
            dispatchFailed();
        }
    }

    private void checkHeaderAndDoTheRest(String header) {
        try {
            ArrayList<Long> download = new ArrayList();
            ArrayList<Long> upload = new ArrayList();
            ArrayList<Vector<Long>> serverMap = new ArrayList();
            ArrayList<Vector<Long>> sourceMap = new ArrayList();

//            Log.d("myData","header : "+header);


            String[] headerParts;
            if(header.isEmpty()){
                headerParts = new String[0];
            }else {
                headerParts = header.split("#");
            }
            for (int i = 0; i < headerParts.length; i++) {
                Vector<Long> v = new Vector();
                v.add(Long.parseLong(headerParts[i].split(",")[0]));
                v.add(Long.parseLong(headerParts[i].split(",")[1]));
                serverMap.add(v);
            }
            sourceMap = ds.getCachedData().getDataMap();
            for(int i = 0; i < sourceMap.size(); i++) {
//                Log.d("myData", i+" : source side header : "+Long.toString(sourceMap.get(i).get(0))+" , "+Long.toString(sourceMap.get(i).get(1)));
            }

            boolean access = false;
            for (int i = 0; i < serverMap.size(); i++) {
                access = false;
                for (int j = 0; j < sourceMap.size(); j++) {
                    if (serverMap.get(i).get(0).equals(sourceMap.get(j).get(0))) {
                        access = true;
                        if (serverMap.get(i).get(1).longValue() > sourceMap.get(j).get(1).longValue()) {
                            download.add(serverMap.get(i).get(0));
                            break;
                        } else if (serverMap.get(i).get(1).longValue() < sourceMap.get(j).get(1).longValue()) {
//                            Log.d("myData","why upload : "+Boolean.toString(serverMap.get(i).get(1).longValue() < sourceMap.get(j).get(1).longValue())+" "+Long.toString(serverMap.get(i).get(1).longValue())+" "+Long.toString(sourceMap.get(j).get(1).longValue()));
                            upload.add(sourceMap.get(i).get(0));
//                            Log.d("myData","upload added in 1");
                            break;
                        }
                    }
                }
                if(!access) download.add(serverMap.get(i).get(0));
            }

            for (int i = 0; i < sourceMap.size(); i++) {
                access = false;
                for (int j = 0; j < serverMap.size(); j++) {

                    if (serverMap.get(j).get(0).equals(sourceMap.get(i).get(0))) {
                        access = true;
                        break;
                    }
                }
                if(!access) {
                    upload.add(sourceMap.get(i).get(0));
//                    Log.d("myData","upload added in 2");
                }
            }

//            for(int i = 0; i < download.size(); i++)
//            {
//                Log.d("myData","downloads : "+Long.toString(download.get(i)));
//            }
//            for(int i = 0; i < upload.size(); i++)
//            {
//                Log.d("myData","uploads : "+Long.toString(upload.get(i)));
//            }

            saveNewServerData(download, upload);

        } catch (Exception e) {
            e.printStackTrace();
            dispatchFailed();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),getString(R.string.sync_failed),Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void saveNewServerData(final ArrayList<Long> download, final ArrayList<Long> upload)
    {
        try{
            String email = Base64.encodeToString(is.getCacheData().getContentByName("email").getBytes("UTF-8"), 0);

            for(int i = 0; i < download.size(); i++)
            {
                currentTask = getString(R.string.downloading)+" "+Integer.toString(download.size()-i);
                dispatchNoteChange();

                try {
                    URL url = new URL("http://sandstormweb.com//the_notebook//get_data.php");
                    HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                    huc.setRequestMethod("POST");
                    huc.setDoOutput(true);
                    huc.setDoInput(true);

                    ArrayList<Long> temp = new ArrayList();
                    temp.add(download.get(i));

                    String EP = "request=" + convertIdListToString(temp) + "&email=" + email;
                    byte[] data = EP.getBytes("UTF-8");

                    DataOutputStream dos = new DataOutputStream(huc.getOutputStream());
                    dos.write(data);
                    dos.close();

                    InputStreamReader isr = new InputStreamReader(huc.getInputStream());
                    Scanner sc = new Scanner(isr).useDelimiter("\\A");

                    File f = new File(getCacheDir()+"//sync.cache");
                    if(f.exists()){
                        f.delete();
                    }f.createNewFile();

                    FileWriter fw = new FileWriter(f);
                    BufferedWriter bw = new BufferedWriter(fw);

                    boolean access = false;
                    while(sc.hasNext())
                    {
                        if(access) bw.newLine();
                        bw.write(sc.nextLine());
                        access = true;
                    }

                    sc.close();
                    bw.close();

                    AbstractNoteNotebook ann = Functions.getAbstractNoteNotebookServerData(f, getApplicationContext());

                    if(ann.getType() == AbstractNoteNotebook.TYPE_NOTE){
//                        Log.d("myData","before save status : "+Integer.toString(ann.getStatus()));
                        ds.saveHeavyNote((HeavyNote)ann);
//                        Log.d("myData","after save status : "+Integer.toString(ds.getCachedData().getNote(0).getStatus()));
                    }else{
//                        Log.d("myData","in sync service notebook : "+((Notebook)ann).getName());
                        if(ds.getCachedData().getNotebookById(ann.getId())==null){
                            ds.getCachedData().addNotebook((Notebook)ann);
                        }else{
                            ds.getCachedData().updateNotebookById((Notebook)ann);
                        }
                        ds.saveData(ds.getCachedData());
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            sendNewSourceData(upload);

        } catch (Exception e) {
            e.printStackTrace();
            dispatchFailed();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),getString(R.string.sync_failed),Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private String convertIdListToString(ArrayList<Long> ids)
    {
        try{
            String output = "";

            boolean access = false;
            for(int i = 0; i < ids.size(); i++)
            {
                if(access) output += ",";
                output += Long.toString(ids.get(i));
                access = true;
            }

            return output;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private void sendNewSourceData(ArrayList<Long> upload)
    {
        try {
            String email = Base64.encodeToString(is.getCacheData().getContentByName("email").getBytes("UTF-8"), 0);

            for(int i = 0; i < upload.size(); i++) {
//                Log.d("myData","is ds.get null "+Boolean.toString(ds.getCachedData() == null));
//                Log.d("myData","is ds.get null "+Boolean.toString(ds.getCachedData() == null));

                AbstractNoteNotebook ann = ds.getCachedData().getNoteOrNotebookById(upload.get(i));

                if(ann.getType() == AbstractNoteNotebook.TYPE_NOTE) {
                    currentTask = getString(R.string.uploading)+" "+ ((Note)ann).getContent().getTitle();
                }else{
                    currentTask = getString(R.string.uploading)+" "+ ((Notebook)ann).getName();
                }
                dispatchNoteChange();

                URL url = new URL("http://sandstormweb.com//the_notebook//insert_update_data.php");
                HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                huc.setRequestMethod("POST");
                huc.setDoOutput(true);
                huc.setDoInput(true);

                File f = new File("/storage/extSdCard/test.txt");
                if(f.exists()){
                    f.delete();
                }
                f.createNewFile();

                DataOutputStream dos2 = new DataOutputStream(new FileOutputStream(f));


                byte[] data = ("email=" + email + "&request=").getBytes("UTF-8");

                DataOutputStream dos = new DataOutputStream(huc.getOutputStream());

                dos.write(data);
                dos2.write(data);

                File cache = ds.getNoteNotebookEncodedById(upload.get(i));

                FileReader fr = new FileReader(cache);
                BufferedReader br = new BufferedReader(fr);

                br.close();

                FileInputStream fis = new FileInputStream(cache);

                byte[] buffer = new byte[1000];
                int read = 0;
                int all = 0;

                while((read = fis.read(buffer))!=-1){
                    dos.write(buffer,0,read);
                    dos2.write(buffer,0,read);

                    all += read;
                }

                dos.close();
                dos2.close();

                InputStreamReader isr = new InputStreamReader(huc.getInputStream());
                Scanner sc = new Scanner(isr).useDelimiter("\\A");

//                while(sc.hasNext()){
//                    Log.d("myData","what we received : "+sc.nextLine());
//                }

                huc.disconnect();
            }

            ds.getCachedData().setAllSynced();
            ds.setAllSynced();
        }catch(Exception e) {
            e.printStackTrace();
            dispatchFailed();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),getString(R.string.sync_failed),Toast.LENGTH_LONG).show();
                }
            });
        }
        dispatchComplete();
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), getString(R.string.sync_success), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void dispatchFailed()
    {
        try{
            for(int i = 0; i < this.syncFailedListener.size(); i++)
            {
                syncFailedListener.get(i).onSyncFailed();
            }

            syncFailedListener = new ArrayList();

            for(int i = 0; i < constantSyncFailedListener.size(); i++)
            {
                constantSyncFailedListener.get(i).onSyncFailed();
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void dispatchComplete()
    {
        try{
            for(int i = 0; i < this.syncCompleteListener.size(); i++)
            {
                syncCompleteListener.get(i).onSyncCompleted();
            }

            syncCompleteListener = new ArrayList();

            for(int i = 0; i < constantSyncCompleteListener.size(); i++)
            {
                constantSyncCompleteListener.get(i).onSyncCompleted();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void dispatchStart()
    {
        try{
            for(int i = 0; i < this.syncStartedListener.size(); i++)
            {
                syncStartedListener.get(i).onSyncStart();
            }

            syncStartedListener = new ArrayList();

            for(int i = 0; i < constantSyncStartedListener.size(); i++)
            {
                constantSyncStartedListener.get(i).onSyncStart();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void dispatchNoteChange()
    {
        try{
            for(int i = 0; i < this.noteChangedListener.size(); i++)
            {
                noteChangedListener.get(i).onCurrentNoteChange();
            }

            noteChangedListener = new ArrayList();

            for(int i = 0; i < constantSyncCompleteListener.size(); i++)
            {
                constantNoteChangedListener.get(i).onCurrentNoteChange();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void addOnSyncCompleteListener(OnSyncCompleteListener onSyncCompleteListener)
    {
        this.syncCompleteListener.add(onSyncCompleteListener);
    }

    public void addOnSyncFailedListener(OnSyncFailedListener onSyncFailedListener)
    {
        this.syncFailedListener.add(onSyncFailedListener);
    }

    public void addOnSyncStartedListener(OnSyncStartedListener onSyncStartedListener)
    {
        this.syncStartedListener.add(onSyncStartedListener);
    }

    public void addOnCurrentNoteChangeListener(OnCurrentNoteChangeListener onCurrentNoteChangeListener)
    {
        this.noteChangedListener.add(onCurrentNoteChangeListener);
    }

    public void addConstantOnSyncCompleteListener(OnSyncCompleteListener onSyncCompleteListener)
    {
        if(this.constantSyncCompleteListener.size() > 20) return;
        this.constantSyncCompleteListener.add(onSyncCompleteListener);
    }

    public void addConstantOnSyncFailedListener(OnSyncFailedListener onSyncFailedListener)
    {
        if(this.constantSyncFailedListener.size() > 20) return;
        this.constantSyncFailedListener.add(onSyncFailedListener);
    }

    public void addConstantOnSyncStartedListener(OnSyncStartedListener onSyncStartedListener)
    {
        if(this.constantSyncStartedListener.size() > 20) return;
        this.constantSyncStartedListener.add(onSyncStartedListener);
    }

    public void addConstantOnCurrentNoteChangeListener(OnCurrentNoteChangeListener onCurrentNoteChangeListener)
    {
        if(this.constantNoteChangedListener.size() > 20) return;
        this.constantNoteChangedListener.add(onCurrentNoteChangeListener);
    }

    public String getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(String currentTask) {
        this.currentTask = currentTask;
    }

    @Override
    public void onDestroy() {
        try{
            unbindService(isc);
            unbindService(sc);
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private void startAutoSync()
    {
        try{
            isAuto = true;

            am = (AlarmManager)getSystemService(ALARM_SERVICE);

            Intent i = new Intent(getApplicationContext(), SyncReceiver.class);

            PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), REQUEST_AUTO_SYNC, i,0);

            am.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis()+1000, Long.parseLong(is.getCacheData().getContentByName("interval")), pi);

            Log.d("myData","alarm set *************");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void waitForNetworkAndThen()
    {
        try{
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        isWaiting = true;
                        while (!Functions.isConnectedToInternet(getApplicationContext())){
                            Thread.sleep(5*60*1000);
                        }
                        Log.d("myData","sync called by wait");
                        sync();
                        isWaiting = false;
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

    private boolean isAutoSyncChanged()
    {
        try{
            if(isAuto){
                if(is.getCacheData().getContentByName("auto_sync").equals("0") || interval != Long.parseLong(is.getCacheData().getContentByName("interval"))){
                    return true;
                }else{
                    return false;
                }
            }else{
                if(is.getCacheData().getContentByName("auto_sync").equals("1") || interval != Long.parseLong(is.getCacheData().getContentByName("interval"))){
                    return true;
                }else{
                    return false;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            return true;
        }
    }
}