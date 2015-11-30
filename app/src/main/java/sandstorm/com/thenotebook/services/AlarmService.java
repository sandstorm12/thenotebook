package sandstorm.com.thenotebook.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

import sandstorm.com.thenotebook.data_structures.DataBundle;
import sandstorm.com.thenotebook.data_structures.Note;
import sandstorm.com.thenotebook.data_structures.Notebook;
import sandstorm.com.thenotebook.data_structures.micro_content.ContentText;

public class AlarmService extends Service
{
    private DataService ds;
    private DataBundle db;
    private static int lastNoteSize;
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ds = ((DataService.MyBinder)service).getService();
            startBulletProofAlarm();
            ds.addConstantOnSaveCompleteListener(new DataService.OnSaveCompleteListener() {
                @Override
                public void onSaveCompleted() {
                    setAlarms();
                    Log.d("myData","alarm set called from Constant Save");
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public static final int REQUEST_EDIT = 1;

    public class MyBinder extends Binder
    {
        public AlarmService getService()
        {
            return AlarmService.this;
        }
    }
    public interface OnAlarmSetListener
    {
        public void onAlarmSet();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try{
            connectDataServiceAndLoadData();
        }catch(Exception e){
            e.printStackTrace();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    private synchronized void connectDataServiceAndLoadData()
    {
        try{
            Intent i = new Intent(getApplicationContext(),DataService.class);

            bindService(i,sc,0);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void setAlarms()
    {
        try{
            cancelAllAlarms();
            setAllAlarms();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void startBulletProofAlarm()
    {
        try{
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true)
                        {
                            setAlarms();

                            Thread.sleep(3*60*60*1000);
                        }
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

    private void setSingleAlarm(Note note, int index)
    {
        try{
            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);

            Intent i = new Intent(getApplicationContext(),AlarmReceiver.class);
            i.putExtra("request",REQUEST_EDIT);
            i.putExtra("id",note.getId());
            i.putExtra("title",note.getContent().getTitle());
            i.putExtra("preview",((ContentText)note.getContent().getContent(0)).getText());
            if(note.getNotebookId() == 0){
                i.putExtra("notebookName", Notebook.ROOT_NOTEBOOK_NAME);
            }else{
                i.putExtra("notebookName", ds.getCachedData().getNotebookById(note.getNotebookId()).getName());
            }

            PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(),index,i,0);

            Log.d("myData","is after : "+Boolean.toString(Calendar.getInstance().after(note.getContent().getReminder())));

            if(Calendar.getInstance().after(note.getContent().getReminder())){
                if(note.getContent().getRepeat() == 0) return;
                else{
                    while(Calendar.getInstance().after(note.getContent().getReminder()))
                    {
                        note.getContent().getReminder().add(Calendar.DATE,(int)(note.getContent().getRepeat()/24*60*60*1000));
                    }
                }
            }

            Log.d("myData","reminder alarm = "+Long.toString(note.getContent().getReminder().getTimeInMillis())+" repeat = "+Long.toString(note.getContent().getRepeat()));

                if (note.getContent().getRepeat() == 0) {
                    am.set(AlarmManager.RTC_WAKEUP, note.getContent().getReminder().getTimeInMillis(), pi);
                } else {
                    am.setRepeating(AlarmManager.RTC_WAKEUP, note.getContent().getReminder().getTimeInMillis(), note.getContent().getRepeat(), pi);
                }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void setAllAlarms()
    {
        try{
            ArrayList<Note> noteBundle = ds.getCachedData().getNoteBundle();
            lastNoteSize = noteBundle.size();
            for(int i = 0; i < noteBundle.size(); i++)
            {
                if(noteBundle.get(i).isReminder()) setSingleAlarm(noteBundle.get(i),i);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void cancelAllAlarms()
    {
        try{
            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
            for(int i = 0; i < lastNoteSize; i++)
            {
                Intent i2 = new Intent(getApplicationContext(),AlarmReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(),i,i2,0);
                am.cancel(pi);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        try{
            unbindService(sc);
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    public void eraseAllAlarms()
    {
        try{
            cancelAllAlarms();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
