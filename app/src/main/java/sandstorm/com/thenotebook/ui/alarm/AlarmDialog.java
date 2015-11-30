package sandstorm.com.thenotebook.ui.alarm;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import sandstorm.com.thenotebook.R;
import sandstorm.com.thenotebook.services.InfoService;
import sandstorm.com.thenotebook.ui.newNote.NewEditNote;

public class AlarmDialog extends Activity
{
    InfoService is;
    MediaPlayer mp;

    ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                is = ((InfoService.MyBinder) service).getService();
                playAlarmSound();
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

        try {
//            setContentView(R.layout.transparent);

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

            getWindow().setLayout(getResources().getDisplayMetrics().widthPixels,getResources().getDisplayMetrics().heightPixels);

            doTheRest();

            connectToInfoServiceAndPlayAlarm();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void doTheRest()
    {
        try{
            final Dialog d = new Dialog(AlarmDialog.this);
            d.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            d.setContentView(R.layout.alarm_trigger_dialog);
            d.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels, WindowManager.LayoutParams.WRAP_CONTENT);
            d.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });

            TextView title = (TextView)d.findViewById(R.id.alarm_title);
            TextView preview = (TextView)d.findViewById(R.id.alarm_preview);
            Button open = (Button)d.findViewById(R.id.alarm_open);
            Button dismiss = (Button)d.findViewById(R.id.alarm_dismiss);

            Log.d("myData", "receiver ==>" + title.getText().toString() + " " + preview.getText().toString());

            title.setText(getIntent().getExtras().getString("title"));
            preview.setText(getIntent().getExtras().getString("preview"));
            dismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    d.dismiss();
                    finish();
                }
            });
            open.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i2 = new Intent(AlarmDialog.this, NewEditNote.class);
                    i2.putExtra("request", getIntent().getExtras().getInt("request"));
                    i2.putExtra("notebookName", getIntent().getExtras().getString("notebookName"));
                    i2.putExtra("id", getIntent().getExtras().getLong("id"));
                    startActivity(i2);
                    d.dismiss();
                    finish();
                }
            });
            d.show();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void playAlarmSound()
    {
        try{
            mp = new MediaPlayer();
            mp.setLooping(true);
            try{
                Log.d("myData","alarm uri received in ad : "+is.getCacheData().getContentByName("alarm_uri"));
                mp.setDataSource(getApplicationContext(), Uri.parse(is.getCacheData().getContentByName("alarm_uri")));
                mp.prepare();
            }catch(Exception e){
                e.printStackTrace();
                try {
                    Toast.makeText(getApplicationContext(), getString(R.string.cannot_find_file), Toast.LENGTH_LONG).show();
                    mp.setDataSource(getApplicationContext(), Uri.parse("android.resource://sandstorm.com.thenotebook/" + R.raw.hardwell));
                    mp.prepare();
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }

            mp.start();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void connectToInfoServiceAndPlayAlarm()
    {
        try{
            Intent i = new Intent(getApplicationContext(), InfoService.class);
            bindService(i,sc,0);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        try{
            mp.stop();
            unbindService(sc);
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
