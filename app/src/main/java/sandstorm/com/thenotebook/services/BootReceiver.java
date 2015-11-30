package sandstorm.com.thenotebook.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        try{
            Intent i = new Intent(context, DataService.class);
            context.startService(i);

            i = new Intent(context, InfoService.class);
            context.startService(i);

            i = new Intent(context, SyncService.class);
            context.startService(i);

            i = new Intent(context, AlarmService.class);
            context.startService(i);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
