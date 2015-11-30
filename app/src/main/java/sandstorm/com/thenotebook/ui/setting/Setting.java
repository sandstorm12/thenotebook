package sandstorm.com.thenotebook.ui.setting;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Locale;

import sandstorm.com.thenotebook.R;
import sandstorm.com.thenotebook.functions.Functions;
import sandstorm.com.thenotebook.services.InfoService;
import sandstorm.com.thenotebook.ui.AccountManagment.ManageAccount;
import sandstorm.com.thenotebook.ui.AccountManagment.UpgradeRequest;

public class Setting extends AppCompatActivity
{
    Toolbar tb;
    Button alarm, interval, about, manageAccount, rate, terms, language, license;
    Switch autoSync, lock;
    InfoService is;
    MediaPlayer mp;
    Dialog d;
    AlertDialog ad;

    private final int REQUEST_SELECT_ALARM_TONE = 0;

    ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                is = ((InfoService.MyBinder) service).getService();

                initializeComponents();
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

            setContentView(R.layout.setting);

            Intent i = new Intent(getApplicationContext(),InfoService.class);
            bindService(i,sc,0);

            initializeToolbar();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void initializeToolbar()
    {
        try{
            tb = (Toolbar)findViewById(R.id.setting_toolbar);

            setSupportActionBar(tb);

            getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            getSupportActionBar().setTitle(R.string.setting);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void initializeComponents()
    {
        try{
            alarm = (Button)findViewById(R.id.setting_alarm_tone);
            interval = (Button)findViewById(R.id.setting_interval);
            autoSync = (Switch)findViewById(R.id.setting_switch);
            if(is.getCacheData().getContentByName("auto_sync").equals("1")){
                autoSync.setChecked(true);
            }else{
                autoSync.setChecked(false);
            }
            about = (Button)findViewById(R.id.setting_about);
            rate = (Button)findViewById(R.id.setting_rate);
            manageAccount = (Button)findViewById(R.id.setting_manage_account);
            lock = (Switch)findViewById(R.id.setting_switch_lock);
            if(is.getCacheData().getContentByName("lock").equals("1")){
                lock.setChecked(true);
            }else{
                lock.setChecked(false);
            }
            terms = (Button)findViewById(R.id.setting_terms);
            license = (Button)findViewById(R.id.setting_licence);
            language = (Button)findViewById(R.id.setting_language);

//            ALARM_TONE_SELECTION
            alarm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        final Dialog d = new Dialog(Setting.this);
                        d.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                        d.setContentView(R.layout.select_alarm_tone);
                        d.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels*90/100, WindowManager.LayoutParams.WRAP_CONTENT);
                        d.show();
                        d.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                if(mp != null){
                                    mp.stop();
                                }mp = null;
                            }
                        });

                        Button phone = (Button)d.findViewById(R.id.setting_alarm_phone);
                        phone.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                                i.setType("audio/*");
                                startActivityForResult(i, REQUEST_SELECT_ALARM_TONE);
                                d.dismiss();
                            }
                        });

                        final RadioGroup rg = (RadioGroup)d.findViewById(R.id.select_radio_group);
                        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                try {
                                    if(mp != null) mp.stop();
                                    mp = new MediaPlayer();
                                    switch (checkedId) {
                                        case R.id.select_1:
                                            mp.setDataSource(getApplicationContext(), Uri.parse("android.resource://sandstorm.com.thenotebook/"+R.raw.art));
                                            mp.prepare();
                                            break;
                                        case R.id.select_2:
                                            mp.setDataSource(getApplicationContext(), Uri.parse("android.resource://sandstorm.com.thenotebook/"+R.raw.beautiful));
                                            mp.prepare();
                                            break;
                                        case R.id.select_3:
                                            mp.setDataSource(getApplicationContext(), Uri.parse("android.resource://sandstorm.com.thenotebook/"+R.raw.bells));
                                            mp.prepare();
                                            break;
                                        case R.id.select_4:
                                            mp.setDataSource(getApplicationContext(), Uri.parse("android.resource://sandstorm.com.thenotebook/"+R.raw.bip_bip_bip));
                                            mp.prepare();
                                            break;
                                        case R.id.select_5:
                                            mp.setDataSource(getApplicationContext(), Uri.parse("android.resource://sandstorm.com.thenotebook/"+R.raw.earth_song));
                                            mp.prepare();
                                            break;
                                        case R.id.select_6:
                                            mp.setDataSource(getApplicationContext(), Uri.parse("android.resource://sandstorm.com.thenotebook/"+R.raw.elegant));
                                            mp.prepare();
                                            break;
                                        case R.id.select_7:
                                            mp.setDataSource(getApplicationContext(), Uri.parse("android.resource://sandstorm.com.thenotebook/"+R.raw.go));
                                            mp.prepare();
                                            break;
                                        case R.id.select_8:
                                            mp.setDataSource(getApplicationContext(), Uri.parse("android.resource://sandstorm.com.thenotebook/"+R.raw.hardwell));
                                            mp.prepare();
                                            break;
                                        case R.id.select_9:
                                            mp.setDataSource(getApplicationContext(), Uri.parse("android.resource://sandstorm.com.thenotebook/"+R.raw.let_her_go));
                                            mp.prepare();
                                            break;
                                        case R.id.select_10:
                                            mp.setDataSource(getApplicationContext(), Uri.parse("android.resource://sandstorm.com.thenotebook/"+R.raw.off_art));
                                            mp.prepare();
                                            break;
                                        case R.id.select_11:
                                            mp.setDataSource(getApplicationContext(), Uri.parse("android.resource://sandstorm.com.thenotebook/"+R.raw.off_art_2));
                                            mp.prepare();
                                            break;
                                    }
                                    mp.start();
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });

                        Button save = (Button)d.findViewById(R.id.select_save);
                        save.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try{
                                    switch (rg.getCheckedRadioButtonId())
                                    {
                                        case R.id.select_1 :
                                            is.getCacheData().setContentByName("alarm_uri","android.resource://sandstorm.com.thenotebook/"+R.raw.art);
                                            is.saveData(is.getCacheData());
                                            break;
                                        case R.id.select_2 :
                                            is.getCacheData().setContentByName("alarm_uri","android.resource://sandstorm.com.thenotebook/"+R.raw.beautiful);
                                            is.saveData(is.getCacheData());
                                            break;
                                        case R.id.select_3 :
                                            is.getCacheData().setContentByName("alarm_uri","android.resource://sandstorm.com.thenotebook/"+R.raw.bells);
                                            is.saveData(is.getCacheData());
                                            break;
                                        case R.id.select_4 :
                                            is.getCacheData().setContentByName("alarm_uri","android.resource://sandstorm.com.thenotebook/"+R.raw.bip_bip_bip);
                                            is.saveData(is.getCacheData());
                                            break;
                                        case R.id.select_5 :
                                            is.getCacheData().setContentByName("alarm_uri","android.resource://sandstorm.com.thenotebook/"+R.raw.earth_song);
                                            is.saveData(is.getCacheData());
                                            break;
                                        case R.id.select_6 :
                                            is.getCacheData().setContentByName("alarm_uri","android.resource://sandstorm.com.thenotebook/"+R.raw.elegant);
                                            is.saveData(is.getCacheData());
                                            break;
                                        case R.id.select_7 :
                                            is.getCacheData().setContentByName("alarm_uri","android.resource://sandstorm.com.thenotebook/"+R.raw.go);
                                            is.saveData(is.getCacheData());
                                            break;
                                        case R.id.select_8 :
                                            is.getCacheData().setContentByName("alarm_uri","android.resource://sandstorm.com.thenotebook/"+R.raw.hardwell);
                                            is.saveData(is.getCacheData());
                                            break;
                                        case R.id.select_9 :
                                            is.getCacheData().setContentByName("alarm_uri","android.resource://sandstorm.com.thenotebook/"+R.raw.let_her_go);
                                            is.saveData(is.getCacheData());
                                            break;
                                        case R.id.select_10 :
                                            is.getCacheData().setContentByName("alarm_uri","android.resource://sandstorm.com.thenotebook/"+R.raw.off_art);
                                            is.saveData(is.getCacheData());
                                            break;
                                        case R.id.select_11 :
                                            is.getCacheData().setContentByName("alarm_uri","android.resource://sandstorm.com.thenotebook/"+R.raw.off_art_2);
                                            is.saveData(is.getCacheData());
                                            break;
                                        case -1 :
                                            Toast.makeText(getApplicationContext(),getString(R.string.please_select_one),Toast.LENGTH_LONG).show();
                                            break;
                                    }
                                    if(rg.getCheckedRadioButtonId() != -1){
                                        d.dismiss();
                                        if(mp != null){
                                            mp.stop();
                                            mp = null;
                                        }
                                    }
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

//            SWITCH
            autoSync.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(is.getCacheData().getContentByName("isPremium").equals("1")){
                        if(autoSync.isChecked()) is.getCacheData().setContentByName("auto_sync","1");
                        else is.getCacheData().setContentByName("auto_sync","0");
                        is.saveData(is.getCacheData());
                    }else{
                        autoSync.setChecked(false);
                        Intent i = new Intent(getApplicationContext(), UpgradeRequest.class);
                        startActivity(i);
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_in);
                    }
                }
            });

            lock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(is.getCacheData().getContentByName("isPremium").equals("1")){
                        if(lock.isChecked()) is.getCacheData().setContentByName("lock","1");
                        else is.getCacheData().setContentByName("lock","0");
                        is.saveData(is.getCacheData());
                    }else{
                        lock.setChecked(false);
                        Intent i = new Intent(getApplicationContext(), UpgradeRequest.class);
                        startActivity(i);
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_in);
                    }
                }
            });


//            INTERVAL
            interval.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(is.getCacheData().getContentByName("isPremium").equals("1")){
                        d = new Dialog(Setting.this);
                        d.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                        d.setContentView(R.layout.interval);
                        d.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels*90/100, WindowManager.LayoutParams.WRAP_CONTENT);
                        d.show();

                        RadioButton rb;
                        switch (is.getCacheData().getContentByName("interval"))
                        {
                            case "3600000" :
                                rb = (RadioButton)d.findViewById(R.id.interval_1);
                                rb.setChecked(true);
                                break;
                            case "10800000" :
                                rb = (RadioButton)d.findViewById(R.id.interval_3);
                                rb.setChecked(true);
                                break;
                            case "21600000" :
                                rb = (RadioButton)d.findViewById(R.id.interval_6);
                                rb.setChecked(true);
                                break;
                            case "43200000" :
                                rb = (RadioButton)d.findViewById(R.id.interval_12);
                                rb.setChecked(true);
                                break;
                            case "86400000" :
                                rb = (RadioButton)d.findViewById(R.id.interval_24);
                                rb.setChecked(true);
                                break;
                        }

                        Button save = (Button)d.findViewById(R.id.interval_save);
                        save.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                RadioGroup rg = (RadioGroup)d.findViewById(R.id.interval_rg);
                                try{
                                    switch (rg.getCheckedRadioButtonId())
                                    {
                                        case -1 :
                                            Toast.makeText(getApplicationContext(),R.string.please_select_one, Toast.LENGTH_LONG).show();
                                            break;
                                        case R.id.interval_1 :
                                            is.getCacheData().setContentByName("interval","3600000");
                                            is.saveData(is.getCacheData());
                                            d.dismiss();
                                            break;
                                        case R.id.interval_3 :
                                            is.getCacheData().setContentByName("interval","10800000");
                                            is.saveData(is.getCacheData());
                                            d.dismiss();
                                            break;
                                        case R.id.interval_6 :
                                            is.getCacheData().setContentByName("interval","21600000");
                                            is.saveData(is.getCacheData());
                                            d.dismiss();
                                            break;
                                        case R.id.interval_12 :
                                            is.getCacheData().setContentByName("interval","43200000");
                                            is.saveData(is.getCacheData());
                                            d.dismiss();
                                            break;
                                        case R.id.interval_24 :
                                            is.getCacheData().setContentByName("interval","86400000");
                                            is.saveData(is.getCacheData());
                                            d.dismiss();
                                            break;
                                    }
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    }else{
                        Intent i = new Intent(getApplicationContext(), UpgradeRequest.class);
                        startActivity(i);
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    }
                }
            });

//            ABOUT
            about.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        Intent i = new Intent(getApplicationContext(),About.class);
                        startActivity(i);
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });


//            MANAGE_ACCOUNT
            manageAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        Intent i = new Intent(getApplicationContext(), ManageAccount.class);
                        startActivity(i);
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });

//            RATE
            rate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        Intent intent = new Intent(Intent.ACTION_EDIT);
                        intent.setData(Uri.parse("bazaar://details?id=" + "sandstorm.com.thenotebook"));
                        intent.setPackage("com.farsitel.bazaar");
                        startActivity(intent);
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),getString(R.string.bazaar_is_not_installed),Toast.LENGTH_LONG).show();
                    }
                }
            });


//            TERMS
            terms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        if(Functions.isConnectedToInternet(getApplicationContext())) {
                            Intent i = new Intent(getApplicationContext(), TermsOfService.class);
                            startActivity(i);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }else{
                            Toast.makeText(getApplicationContext(), getString(R.string.not_connceted), Toast.LENGTH_LONG).show();
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });

            license.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        if(Functions.isConnectedToInternet(getApplicationContext())) {
                            Intent i = new Intent(getApplicationContext(), License.class);
                            startActivity(i);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }else{
                            Toast.makeText(getApplicationContext(), getString(R.string.not_connceted), Toast.LENGTH_LONG).show();
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });

//            LANGUAGE
            language.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        d = new Dialog(Setting.this);
                        d.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                        d.setContentView(R.layout.raw_option_dialog);
                        d.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels*90/100, WindowManager.LayoutParams.WRAP_CONTENT);

                        ListView lv = (ListView)d.findViewById(R.id.raw_dialog_listView);

                        String[] data = {"english","فارسی"};

                        ArrayAdapter aa = new ArrayAdapter(Setting.this, android.R.layout.simple_list_item_1, data);

                        lv.setAdapter(aa);

                        d.show();

                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                try{
                                    Locale locale;
                                    Configuration config;
                                    AlertDialog.Builder adb;

                                    switch(position)
                                    {
                                        case 1 :
                                            locale = new Locale("fa");
                                            Locale.setDefault(locale);
                                            config = new Configuration();
                                            config.locale = locale;
                                            getResources().updateConfiguration(config,null);
                                            d.dismiss();
                                            adb = new AlertDialog.Builder(Setting.this);
                                            adb.setTitle(getString(R.string.notice));
                                            adb.setMessage(getString(R.string.restart_to_complete));
                                            adb.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    try{
                                                        ad.dismiss();
                                                        Intent i = new Intent(getApplicationContext(),Setting.class);
                                                        startActivity(i);
                                                        finish();
                                                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                                    }catch(Exception e){
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                            ad = adb.create();
                                            ad.show();
                                            break;
                                        case 0 :
                                            locale = new Locale("en_US");
                                            Locale.setDefault(locale);
                                            config = new Configuration();
                                            config.locale = locale;
                                            getResources().updateConfiguration(config,null);
                                            d.dismiss();
                                            adb = new AlertDialog.Builder(Setting.this);
                                            adb.setTitle(getString(R.string.notice));
                                            adb.setMessage(getString(R.string.restart_to_complete));
                                            adb.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    try{
                                                        ad.dismiss();
                                                        Intent i = new Intent(getApplicationContext(),Setting.class);
                                                        startActivity(i);
                                                        finish();
                                                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                                    }catch(Exception e){
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                            ad = adb.create();
                                            ad.show();
                                            break;
                                    }
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            if(resultCode == RESULT_OK) {
                switch (requestCode) {
                    case REQUEST_SELECT_ALARM_TONE:
                        is.getCacheData().setContentByName("alarm_uri", data.getData().toString());
                        is.saveData(is.getCacheData());
                        break;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        try{
            unbindService(sc);
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try{
            if(item.getItemId() == android.R.id.home){
                finish();
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return super.onOptionsItemSelected(item);
    }
}
