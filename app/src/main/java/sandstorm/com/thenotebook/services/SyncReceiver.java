package sandstorm.com.thenotebook.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class SyncReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        try{
            Log.d("myData","broadcast received ****************");
            Intent i = new Intent();
            i.setAction("sync");

            LocalBroadcastManager.getInstance(context).sendBroadcast(i);

            Log.d("myData","broad cast sent ***************");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
