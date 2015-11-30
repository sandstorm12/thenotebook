package sandstorm.com.thenotebook.ui.alarm;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.util.Calendar;

import sandstorm.com.thenotebook.R;
import sandstorm.com.thenotebook.ui.timePicker.PersianDatePicker;

public class SetAlarm extends AppCompatActivity
{
    TimePicker tp;
    PersianDatePicker pdp;
    Spinner spinner;
    Toolbar tb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.black));
            }

            setContentView(R.layout.set_alarm);

            constructor();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void constructor()
    {
        try{
            tb = (Toolbar)findViewById(R.id.set_alarm_toolbar);
            tp = (TimePicker)findViewById(R.id.set_alarm_time_picker);
            pdp = (PersianDatePicker)findViewById(R.id.set_alarm_date_picker);
            spinner = (Spinner)findViewById(R.id.set_alarm_spinner);

//            Toolbar
            setSupportActionBar(tb);

            getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

//            PDP


//            tp
            tp.setCurrentHour(6);
            tp.setCurrentMinute(0);
            tp.setIs24HourView(true);

//            spinner
            String[] repeats = {getString(R.string.no_repeat),getString(R.string.daily),getString(R.string.weekly)};

            ArrayAdapter aa = new ArrayAdapter(SetAlarm.this,android.R.layout.simple_list_item_1,repeats);

            spinner.setAdapter(aa);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            MenuInflater mi = getMenuInflater();
            mi.inflate(R.menu.set_alarm, menu);
        }catch(Exception e){
            e.printStackTrace();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try{
            if(item.getItemId() == R.id.set_alarm_menu_save) {
                tp.clearFocus();
//                GET_TIME_IN_MILLIS
                long alarm = pdp.getDisplayDate().getTime();
                long t2 = tp.getCurrentHour()*60*60*1000;
                t2 += tp.getCurrentMinute()*60*1000;
                long t1 = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)*60*60*1000;
                t1 += Calendar.getInstance().get(Calendar.MINUTE)*60*1000;
                alarm += t2 - t1;

                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(alarm);
                c.set(Calendar.SECOND,0);
                Log.d("myData","in SetAlarm ==> hour = "+Integer.toString(c.get(Calendar.HOUR_OF_DAY))+"  minute = "+Integer.toString(c.get(Calendar.MINUTE)));

                Intent i = new Intent();
                i.putExtra("alarm", c.getTimeInMillis());

                switch(spinner.getSelectedItemPosition())
                {
                    case 1 :
                        i.putExtra("repeat",(long)1*24*60*60*1000);
                        break;
                    case 2 :
                        i.putExtra("repeat",(long)7*24*60*60*1000);
                        break;
                    case 3 :
                        i.putExtra("repeat",(long)31*24*60*60*1000);
                        break;
                    case 0 :
                        i.putExtra("repeat",(long)0);
                }

                setResult(RESULT_OK,i);
                finish();
            }else if(item.getItemId() == android.R.id.home){
                finish();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return super.onOptionsItemSelected(item);
    }
}
