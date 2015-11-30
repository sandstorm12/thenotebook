package sandstorm.com.thenotebook.services;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import sandstorm.com.thenotebook.R;
import sandstorm.com.thenotebook.data_structures.Note;
import sandstorm.com.thenotebook.data_structures.micro_content.ContentText;
import sandstorm.com.thenotebook.ui.alarm.AlarmDialog;
import sandstorm.com.thenotebook.ui.newNote.NewEditNote;

public class AlarmReceiver extends BroadcastReceiver
{
    DataService ds;
    Intent i;
    Context context;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        try{
            Log.d("myData","alarm received");

            this.context = context;
            this.i = intent;

            doTheRest();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void doTheRest()
    {
        try{
            Intent i2 = new Intent(context, AlarmDialog.class);
            i2.putExtra("request",i.getExtras().getInt("request"));
            i2.putExtra("notebookName",i.getExtras().getString("notebookName"));
            i2.putExtra("id",i.getExtras().getLong("id"));
            i2.putExtra("title",i.getExtras().getString("title"));
            i2.putExtra("preview",i.getExtras().getString("preview"));
            i2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i2);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
