package sandstorm.com.thenotebook.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import sandstorm.com.thenotebook.R;
import sandstorm.com.thenotebook.data_structures.Notebook;
import sandstorm.com.thenotebook.ui.MainActivity;
import sandstorm.com.thenotebook.ui.newNote.NewEditNote;
import sandstorm.com.thenotebook.ui.signinSignup.Signup;

public class ToolWidget extends AppWidgetProvider
{
    ComponentName cn;

    final static String App = "app";
    final static String Text = "text";
    final static String Image = "image";
    final static String Video = "video";
    final static String File = "file";
    final static String Record = "record";

    static final int REQUEST_NEW_TEXT = 0;
    static final int REQUEST_NEW_SOUND = 2;
    static final int REQUEST_NEW_VIDEO = 3;
    static final int REQUEST_NEW_IMAGE = 4;
    static final int REQUEST_NEW_FILE = 5;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        try{
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_toolbar);
            cn = new ComponentName(context,ToolWidget.class);

            rv.setOnClickPendingIntent(R.id.tool_widget_app,getPendingIntentForAction(context,App));
            rv.setOnClickPendingIntent(R.id.tool_widget_image,getPendingIntentForAction(context,Image));
            rv.setOnClickPendingIntent(R.id.tool_widget_text,getPendingIntentForAction(context,Text));
            rv.setOnClickPendingIntent(R.id.tool_widget_video,getPendingIntentForAction(context,Video));
            rv.setOnClickPendingIntent(R.id.tool_widget_file,getPendingIntentForAction(context,File));
            rv.setOnClickPendingIntent(R.id.tool_widget_record,getPendingIntentForAction(context,Record));

            appWidgetManager.updateAppWidget(cn,rv);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        try{
            Log.d("myData","what we receive : "+intent.getAction());
            switch(intent.getAction())
            {
                case App :
                    Intent i = new Intent(context, Signup.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra("new","new");
                    context.startActivity(i);
                    break;
                case Text :
                    Intent i1 = new Intent(context, NewEditNote.class);
                    i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i1.putExtra("request",REQUEST_NEW_TEXT);
                    i1.putExtra("notebookId",0l);
                    i1.putExtra("notebookName", Notebook.ROOT_NOTEBOOK_NAME);
                    context.startActivity(i1);
                    break;
                case Image :
                    Intent i2 = new Intent(context, NewEditNote.class);
                    i2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i2.putExtra("request",REQUEST_NEW_IMAGE);
                    i2.putExtra("notebookId",0l);
                    i2.putExtra("notebookName", Notebook.ROOT_NOTEBOOK_NAME);
                    context.startActivity(i2);
                    break;
                case Record :
                    Intent i3 = new Intent(context, NewEditNote.class);
                    i3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i3.putExtra("request",REQUEST_NEW_SOUND);
                    i3.putExtra("notebookId",0l);
                    i3.putExtra("notebookName", Notebook.ROOT_NOTEBOOK_NAME);
                    context.startActivity(i3);
                    break;
                case Video :
                    Intent i4 = new Intent(context, NewEditNote.class);
                    i4.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i4.putExtra("request",REQUEST_NEW_VIDEO);
                    i4.putExtra("notebookId",0l);
                    i4.putExtra("notebookName", Notebook.ROOT_NOTEBOOK_NAME);
                    context.startActivity(i4);
                    break;
                case File :
                    Intent i5 = new Intent(context, NewEditNote.class);
                    i5.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i5.putExtra("request",REQUEST_NEW_FILE);
                    i5.putExtra("notebookId",0l);
                    i5.putExtra("notebookName", Notebook.ROOT_NOTEBOOK_NAME);
                    context.startActivity(i5);
                    break;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private PendingIntent getPendingIntentForAction(Context context, String action)
    {
        try{
            Intent i = new Intent(context,getClass());
            i.setAction(action);
            return PendingIntent.getBroadcast(context,0,i,0);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
