package sandstorm.com.thenotebook.ui.newNote;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.TimeZone;

import ru.bartwell.exfilepicker.ExFilePickerParcelObject;
import sandstorm.com.thenotebook.R;
import sandstorm.com.thenotebook.data_structures.AbstractNoteNotebook;
import sandstorm.com.thenotebook.data_structures.DataBundle;
import sandstorm.com.thenotebook.data_structures.HeavyNote;
import sandstorm.com.thenotebook.data_structures.Note;
import sandstorm.com.thenotebook.data_structures.micro_content.AbstractContent;
import sandstorm.com.thenotebook.data_structures.micro_content.ContentFile;
import sandstorm.com.thenotebook.data_structures.micro_content.ContentImage;
import sandstorm.com.thenotebook.data_structures.micro_content.ContentSound;
import sandstorm.com.thenotebook.data_structures.micro_content.ContentText;
import sandstorm.com.thenotebook.data_structures.micro_content.ContentVideo;
import sandstorm.com.thenotebook.data_structures.micro_content.NoteContent;
import sandstorm.com.thenotebook.functions.Functions;
import sandstorm.com.thenotebook.services.AlarmService;
import sandstorm.com.thenotebook.services.DataService;
import sandstorm.com.thenotebook.services.InfoService;
import sandstorm.com.thenotebook.ui.MainActivity;
import sandstorm.com.thenotebook.ui.alarm.SetAlarm;
import sandstorm.com.thenotebook.ui.noteList.NotebookFragment;
import sandstorm.com.thenotebook.ui.signinSignup.Lock;
import sandstorm.com.thenotebook.ui.signinSignup.Signup;

public class NewEditNote extends AppCompatActivity
{
    NewNoteListAdapter nnla;
    Button alarm, fab;
    TextView notebook, title;
    EditText text;
    Note note;
    ListView lv;
    Toolbar tb;
    DataService ds;
    DataBundle db;
    AlarmService as;
    AlertDialog ad;
    Dialog d;

    private long currentNotebookId, repeat;
    Calendar c;

    private final int REQUEST_IMAGE_CAMERA = 0;
    private final int REQUEST_IMAGE_GALLERY = 1;
    private final int REQUEST_SOUND_RECORD = 2;
    private final int REQUEST_SOUND_GALLERY = 6;
    private final int REQUEST_VIDEO_RECORD = 3;
    private final int REQUEST_VIDEO_GALLERY = 4;
    private final int REQUEST_FILE = 5;
    private final int REQUEST_ALARM = 7;


    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try{
                ds = ((DataService.MyBinder)service).getService();

                switch (getIntent().getExtras().getInt("request"))
                {
                    case MainActivity.REQUEST_NEW_TEXT :
                        startTextNote();
                        break;
                    case MainActivity.REQUEST_NEW_VIDEO :
                        startTextNote();
                        showVideoDialogAndDoTheRest();
                        break;
                    case MainActivity.REQUEST_NEW_FILE :
                        startFileNote();
                        break;
                    case MainActivity.REQUEST_NEW_IMAGE :
                        startTextNote();
                        showImageDialogAndDoTheRest();
                        break;
                    case MainActivity.REQUEST_NEW_SOUND :
                        startSoundNote();
                        break;
                    case MainActivity.REQUEST_EDIT :
                        startEditNote();
                        break;
                }


            }catch(Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private ServiceConnection asc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            as = ((AlarmService.MyBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private ServiceConnection isc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try{
                InfoService is = ((InfoService.MyBinder)service).getService();
                if(is.getCacheData().getContentByName("email").equals("null"))
                {
                    Intent i = new Intent(getApplicationContext(), Signup.class);
                    startActivity(i);
                    finish();
                }
                connectToServices();
                if(!is.getCacheData().getContentByName("lock").equals("0") && getIntent().hasExtra("new")) lock();
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.black));
            }

            setContentView(R.layout.new_edit_note);

            currentNotebookId = getIntent().getExtras().getLong("notebookId");

            constructor();

            initializeToolbar();

            Intent i = new Intent(getApplicationContext(), InfoService.class);
            bindService(i,isc,0);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void connectToServices()
    {
        try{
            Intent i = new Intent(getApplicationContext(),DataService.class);
            startService(i);
            bindService(i,sc,0);

            i = new Intent(getApplicationContext(),AlarmService.class);
            bindService(i,asc,BIND_AUTO_CREATE);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            MenuInflater mi = getMenuInflater();
            mi.inflate(R.menu.new_edit_menu, menu);
        }catch(Exception e){
            e.printStackTrace();
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try{
            switch(item.getItemId())
            {
                case R.id.menu_save :
                    Log.d("myData","==> save clicked");
                    final ProgressDialog pd = new ProgressDialog(NewEditNote.this);
                    pd.setIndeterminate(true);
                    pd.setMessage(getString(R.string.saving));
                    pd.show();

                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                if((getIntent().getExtras().getInt("request") == NotebookFragment.REQUEST_EDIT)) {
                                    Log.d("myData","==> in save edited");
                                    nnla.data.setTitle((title.getText().toString().isEmpty())?getString(R.string.title):title.getText().toString());
                                    nnla.data.setRepeat(repeat);
                                    nnla.data.setReminder(c);

                                    Log.d("myData","==> before newing the heavyNote");
                                    HeavyNote n = new HeavyNote(getIntent().getExtras().getLong("id"),currentNotebookId, Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis(),Note.STATUS_ACTIVE,AbstractNoteNotebook.IS_SYNCED_FALSE);
                                    n.setContent(nnla.data);
                                    n.setIsSynced(AbstractNoteNotebook.IS_SYNCED_FALSE);

                                    ds.saveHeavyNote(n);
                                    as.setAlarms();

                                    finish();
                                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                }else{
                                    nnla.data.setTitle((title.getText().toString().isEmpty())?getString(R.string.title):title.getText().toString());
                                    nnla.data.setRepeat(repeat);
                                    nnla.data.setReminder(c);

                                    HeavyNote n = new HeavyNote(System.currentTimeMillis(),currentNotebookId,Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis(),Note.STATUS_ACTIVE,AbstractNoteNotebook.IS_SYNCED_FALSE);
                                    n.setContent(nnla.data);
                                    ds.saveHeavyNote(n);
                                    as.setAlarms();
                                    finish();
                                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                }
                                pd.dismiss();
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                    t.setPriority(Thread.MAX_PRIORITY);
                    t.start();
                    break;
                case R.id.menu_image :
                    showImageDialogAndDoTheRest();
                    break;
                case R.id.menu_sound :
                    startSoundNote();
                    break;
                case R.id.menu_video :
                    showVideoDialogAndDoTheRest();
                    break;
                case R.id.menu_file :
                    startFileNote();
                    break;
                case android.R.id.home :
                    AlertDialog.Builder adb = new AlertDialog.Builder(NewEditNote.this);
                    adb.setTitle(getString(R.string.notice));
                    adb.setMessage(getString(R.string.close_withot_save));
                    adb.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try{
                                finish();
                                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                    adb.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try{
                                ad.dismiss();
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
        return super.onOptionsItemSelected(item);
    }

    private void constructor()
    {
        try{
            lv = (ListView)findViewById(R.id.new_edit_note_listView);
            title = (EditText)findViewById(R.id.new_edit_note_title);
            title.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    try{
                        if(tb != null) tb.setTitle(title.getText().toString());
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });
            notebook = (TextView)findViewById(R.id.new_edit_note_notebook);
            alarm = (Button)findViewById(R.id.new_edit_note_alarm);
            text = (EditText)findViewById(R.id.new_edit_note_text);

            alarm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(c.getTimeInMillis() != 0)
                    {
                        AlertDialog.Builder adb = new AlertDialog.Builder(NewEditNote.this);
                        adb.setTitle(getString(R.string.cancel_alarm));
                        adb.setMessage(R.string.wanna_cancel_alarm);
                        adb.setPositiveButton(R.string.yes,new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                c.setTimeInMillis(0);
                                repeat = 0;
                                nnla.data.setReminder(c);
                                nnla.data.setRepeat(0);
                                alarm.setBackgroundResource(R.drawable.set_alarm_deactive);
                            }
                        });
                        adb.setNegativeButton(R.string.no,new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ad.dismiss();
                            }
                        });
                        ad = adb.create();
                        ad.show();
                    }else {
                        Intent i = new Intent(getApplicationContext(), SetAlarm.class);
                        startActivityForResult(i, REQUEST_ALARM);
                    }
                }
            });

            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    try{
                        Log.d("myData","on long click");

                        if(position == 0) return false;
                        d = new Dialog(NewEditNote.this);
                        d.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                        d.setContentView(R.layout.raw_option_dialog);
                        d.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels*90/100, WindowManager.LayoutParams.WRAP_CONTENT);
                        d.show();

                        ListView lv2 = (ListView)d.findViewById(R.id.raw_dialog_listView);

                        String[] data = {getString(R.string.delete),getString(R.string.save_to_storage)};
                        ArrayAdapter aa = new ArrayAdapter(NewEditNote.this,android.R.layout.simple_list_item_1,data);

                        lv2.setAdapter(aa);

                        final int pos = position;
                        lv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                try{
                                    if(position == 0){
                                        nnla.data.removeContent(pos);

                                        lv.setAdapter(nnla);

                                        d.dismiss();
                                    }else if(position == 1){
                                        try{
                                            File origin;
                                            File destination;
                                            AlertDialog.Builder adb;

                                            switch(nnla.data.getContent(pos).getType())
                                            {
                                                case AbstractContent.TYPE_IMAGE :
                                                    origin = ((ContentImage)nnla.data.getContent(pos)).getFile();
                                                    destination = new File("/storage/emulated/0/The Notebook/"+origin.getName()+".png");
                                                    if(destination.exists()) destination.delete(); destination.createNewFile();
                                                    Functions.copyFile(origin, destination);

                                                    adb = new AlertDialog.Builder(NewEditNote.this);
                                                    adb.setTitle(getString(R.string.notice));
                                                    adb.setMessage(getString(R.string.file_saved));
                                                    adb.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            ad.dismiss();
                                                        }
                                                    });
                                                    ad = adb.create();
                                                    ad.show();
                                                    break;
                                                case AbstractContent.TYPE_FILE :
                                                    origin = ((ContentFile)nnla.data.getContent(pos)).getFile();
                                                    destination = new File("/storage/emulated/0/The Notebook/"+origin.getName()+".rename");
                                                    if(destination.exists()) destination.delete(); destination.createNewFile();
                                                    Functions.copyFile(origin, destination);

                                                    adb = new AlertDialog.Builder(NewEditNote.this);
                                                    adb.setTitle(getString(R.string.notice));
                                                    adb.setMessage(getString(R.string.file_saved));
                                                    adb.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            ad.dismiss();
                                                        }
                                                    });
                                                    ad = adb.create();
                                                    ad.show();
                                                    break;
                                                case AbstractContent.TYPE_SOUND :
                                                    origin = ((ContentImage)nnla.data.getContent(pos)).getFile();
                                                    destination = new File("/storage/emulated/0/The Notebook/"+origin.getName()+".mp3");
                                                    if(destination.exists()) destination.delete(); destination.createNewFile();
                                                    Functions.copyFile(origin, destination);

                                                    adb = new AlertDialog.Builder(NewEditNote.this);
                                                    adb.setTitle(getString(R.string.notice));
                                                    adb.setMessage(getString(R.string.file_saved));
                                                    adb.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            ad.dismiss();
                                                        }
                                                    });
                                                    ad = adb.create();
                                                    ad.show();
                                                    break;
                                                case AbstractContent.TYPE_VIDEO :
                                                    origin = ((ContentImage)nnla.data.getContent(pos)).getFile();
                                                    destination = new File("/storage/emulated/0/The Notebook/"+origin.getName()+".mp4");
                                                    if(destination.exists()) destination.delete(); destination.createNewFile();
                                                    Functions.copyFile(origin, destination);

                                                    adb = new AlertDialog.Builder(NewEditNote.this);
                                                    adb.setTitle(getString(R.string.notice));
                                                    adb.setMessage(getString(R.string.file_saved));
                                                    adb.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            ad.dismiss();
                                                        }
                                                    });
                                                    ad = adb.create();
                                                    ad.show();
                                                    break;
                                            }
                                            d.dismiss();
                                        }catch(Exception e){
                                            e.printStackTrace();
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
                    return false;
                }
            });

            text.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    try{
                        ((ContentText)nnla.data.getContent(0)).setText(text.getText().toString());
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });

//            CALENDAR
            c = Calendar.getInstance();
            c.setTimeInMillis(0);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void initializeToolbar()
    {
        try{
            tb = (Toolbar)findViewById(R.id.new_edit_note_toolbar);

            setSupportActionBar(tb);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void startTextNote()
    {
        try{
            NoteContent data = new NoteContent();
            data.addContent(new ContentText());

            c = Calendar.getInstance();
            c.setTimeInMillis(0);
            repeat = 0;

            if(data.getReminder().getTimeInMillis() == 0){
                alarm.setBackgroundResource(R.drawable.set_alarm_deactive);
            }else{
                alarm.setBackgroundResource(R.drawable.set_alarm);
            }

            notebook.setText(getIntent().getExtras().getString("notebookName"));

            nnla = new NewNoteListAdapter();
            nnla.setContent(data);

            lv.setAdapter(nnla);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void startEditNote()
    {
        try{
            NoteContent data = ds.getHeavyNoteFromDatabase(getIntent().getExtras().getLong("id")).getContent();

            c.setTimeInMillis(data.getReminder().getTimeInMillis());
            repeat = data.getRepeat();

            title.setText(data.getTitle());
            notebook.setText(getIntent().getExtras().getString("notebookName"));
            tb.setTitle(data.getTitle());
            text.setText(((ContentText)data.getContent(0)).getText());

            if(data.getReminder().getTimeInMillis() == 0){
                alarm.setBackgroundResource(R.drawable.set_alarm_deactive);
            }else{
                alarm.setBackgroundResource(R.drawable.set_alarm);
            }

            nnla = new NewNoteListAdapter();
            nnla.setContent(data);

            lv.setItemsCanFocus(true);
            lv.setAdapter(nnla);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void startFileNote()
    {
        try{
            if(getIntent().getExtras().getInt("request") != MainActivity.REQUEST_EDIT) startTextNote();

            Intent intent = new Intent(getApplicationContext(), ru.bartwell.exfilepicker.ExFilePickerActivity.class);
            intent.putExtra(ru.bartwell.exfilepicker.ExFilePicker.SET_ONLY_ONE_ITEM,true);
            startActivityForResult(intent, REQUEST_FILE);
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),getString(R.string.no_suitable_app),Toast.LENGTH_LONG).show();
        }
    }

    private void startSoundNote()
    {
        try{
            if(getIntent().getExtras().getInt("request") != MainActivity.REQUEST_EDIT) startTextNote();

            Intent i = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
            startActivityForResult(i, REQUEST_SOUND_RECORD);
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),getString(R.string.no_suitable_app),Toast.LENGTH_LONG).show();
        }
    }

    private void showImageDialogAndDoTheRest()
    {
        try{
            final Dialog d = new Dialog(NewEditNote.this);
            d.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            d.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels*90/100, WindowManager.LayoutParams.WRAP_CONTENT);
            d.setTitle(R.string.select_method);
            ColorDrawable cd = new ColorDrawable(getResources().getColor(R.color.drawer));
            d.getWindow().setBackgroundDrawable(cd);
            d.setContentView(R.layout.raw_option_dialog);
            d.show();
            ListView lv = (ListView)d.findViewById(R.id.raw_dialog_listView);

            String[] options = new String[2];
            options[0] = getString(R.string.take_new_one);
            options[1] = getString(R.string.select_existing);

            ArrayAdapter aa = new ArrayAdapter(d.getContext(),android.R.layout.simple_list_item_1,options);

            lv.setAdapter(aa);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try{
                        switch(position)
                        {
                            case 0 :
                                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(i,REQUEST_IMAGE_CAMERA);
                                d.dismiss();
                                break;
                            case 1 :
                                Intent i2 = new Intent(Intent.ACTION_PICK);
                                i2.setType("image/*");
                                startActivityForResult(i2, REQUEST_IMAGE_GALLERY);
                                d.dismiss();
                                break;
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),getString(R.string.no_suitable_app),Toast.LENGTH_LONG).show();
                    }
                }
            });

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void showVideoDialogAndDoTheRest()
    {
        try{
            final Dialog d = new Dialog(NewEditNote.this);
            d.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            d.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels*90/100, WindowManager.LayoutParams.WRAP_CONTENT);
            d.setTitle(R.string.select_method);
            ColorDrawable cd = new ColorDrawable(getResources().getColor(R.color.drawer));
            d.getWindow().setBackgroundDrawable(cd);
            d.setContentView(R.layout.raw_option_dialog);
            d.show();
            ListView lv = (ListView)d.findViewById(R.id.raw_dialog_listView);

            String[] options = new String[2];
            options[0] = getString(R.string.take_new_one);
            options[1] = getString(R.string.select_existing);

            ArrayAdapter aa = new ArrayAdapter(d.getContext(),android.R.layout.simple_list_item_1,options);

            lv.setAdapter(aa);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try{
                        switch(position)
                        {
                            case 0 :
                                Intent i = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                                startActivityForResult(i,REQUEST_VIDEO_RECORD);
                                d.dismiss();
                                break;
                            case 1 :
                                Intent i2 = new Intent(Intent.ACTION_PICK);
                                i2.setType("video/*");
                                startActivityForResult(i2, REQUEST_VIDEO_GALLERY);
                                d.dismiss();
                                break;
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),getString(R.string.no_suitable_app),Toast.LENGTH_LONG).show();
                    }
                }
            });

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        try{
            if(resultCode == RESULT_OK)
            {
                switch (requestCode)
                {
                    case REQUEST_IMAGE_CAMERA :
                        try{
                            Uri uriIC = data.getData();

                            File fIC = getFileFromUriImage(uriIC);

                            FileInputStream fis = new FileInputStream(fIC);

                            byte[] buffer = new byte[fis.available()];

                            fis.read(buffer);

                            if(nnla.data.getDataSize()+buffer.length > getResources().getInteger(R.integer.max_note_content_size)){
                                AlertDialog.Builder adb = new AlertDialog.Builder(NewEditNote.this);
                                adb.setTitle(R.string.max_size_reached);
                                adb.setMessage(getString(R.string.max_note_size_reached)+Integer.toString(getRemainingSize()));
                                adb.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ad.dismiss();
                                    }
                                });
                                ad = adb.create();
                                ad.show();
                            }else{
                                ContentImage ci = new ContentImage(buffer, getCacheDir());

                                nnla.data.addContent(ci);

                                lv.setAdapter(nnla);
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        break;
                    case REQUEST_IMAGE_GALLERY :
                        try{
                            Uri uriIG = data.getData();

                            File fIG = getFileFromUriImage(uriIG);

                            FileInputStream fis = new FileInputStream(fIG);

                            byte[] buffer = new byte[fis.available()];

                            fis.read(buffer);

                            if(nnla.data.getDataSize()+buffer.length > getResources().getInteger(R.integer.max_note_content_size)){
                                AlertDialog.Builder adb = new AlertDialog.Builder(NewEditNote.this);
                                adb.setTitle(R.string.max_size_reached);
                                adb.setMessage(getString(R.string.max_note_size_reached)+Integer.toString(getRemainingSize()));
                                adb.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ad.dismiss();
                                    }
                                });
                                ad = adb.create();
                                ad.show();
                            }else{
                                ContentImage ci = new ContentImage(buffer, getCacheDir());

                                nnla.data.addContent(ci);

                                lv.setAdapter(nnla);
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        break;
                    case REQUEST_SOUND_RECORD :
                        try{
                            Uri uriSR = data.getData();

                            File fSR = getFileFromUriSound(uriSR);

                            FileInputStream fis = new FileInputStream(fSR);

                            byte[] buffer = new byte[fis.available()];

                            fis.read(buffer);

                            if(nnla.data.getDataSize()+buffer.length > getResources().getInteger(R.integer.max_note_content_size)){
                                AlertDialog.Builder adb = new AlertDialog.Builder(NewEditNote.this);
                                adb.setTitle(R.string.max_size_reached);
                                adb.setMessage(getString(R.string.max_note_size_reached)+Integer.toString(getRemainingSize()));
                                adb.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ad.dismiss();
                                    }
                                });
                                ad = adb.create();
                                ad.show();
                            }else{
                                ContentSound cs = new ContentSound(buffer, getCacheDir());

                                nnla.data.addContent(cs);

                                lv.setAdapter(nnla);
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        break;
                    case REQUEST_VIDEO_RECORD :
                        try{
                            Uri uriVR = data.getData();

                            File fVR = getFileFromUriVideo(uriVR);

                            FileInputStream fis = new FileInputStream(fVR);

                            byte[] buffer = new byte[fis.available()];

                            fis.read(buffer);

                            if(nnla.data.getDataSize()+buffer.length > getResources().getInteger(R.integer.max_note_content_size)){
                                AlertDialog.Builder adb = new AlertDialog.Builder(NewEditNote.this);
                                adb.setTitle(R.string.max_size_reached);
                                adb.setMessage(getString(R.string.max_note_size_reached)+Integer.toString(getRemainingSize()));
                                adb.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ad.dismiss();
                                    }
                                });
                                ad = adb.create();
                                ad.show();
                            }else{
                                ContentVideo ci = new ContentVideo(buffer, getCacheDir());

                                nnla.data.addContent(ci);

                                lv.setAdapter(nnla);
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        break;
                    case REQUEST_VIDEO_GALLERY :
                        try{
                            Uri uriVG = data.getData();

                            File fVG = getFileFromUriVideo(uriVG);

                            FileInputStream fis = new FileInputStream(fVG);

                            byte[] buffer = new byte[fis.available()];

                            fis.read(buffer);

                            if(nnla.data.getDataSize()+buffer.length > getResources().getInteger(R.integer.max_note_content_size)){
                                AlertDialog.Builder adb = new AlertDialog.Builder(NewEditNote.this);
                                adb.setTitle(R.string.max_size_reached);
                                adb.setMessage(getString(R.string.max_note_size_reached)+Integer.toString(getRemainingSize()));
                                adb.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ad.dismiss();
                                    }
                                });
                                ad = adb.create();
                                ad.show();
                            }else{
                                ContentVideo ci = new ContentVideo(buffer, getCacheDir());

                                nnla.data.addContent(ci);

                                lv.setAdapter(nnla);
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        break;
                    case REQUEST_FILE :
                        try{
                            ExFilePickerParcelObject eo = data.getParcelableExtra(ExFilePickerParcelObject.class.getCanonicalName());

                            File fF = new File(eo.path+"//"+eo.names.get(0));

                            FileInputStream fis = new FileInputStream(fF);

                            byte[] buffer = new byte[fis.available()];

                            fis.read(buffer);

                            if(nnla.data.getDataSize()+buffer.length > getResources().getInteger(R.integer.max_note_content_size)){
                                AlertDialog.Builder adb = new AlertDialog.Builder(NewEditNote.this);
                                adb.setTitle(R.string.max_size_reached);
                                adb.setMessage(getString(R.string.max_note_size_reached)+Integer.toString(getRemainingSize()));
                                adb.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ad.dismiss();
                                    }
                                });
                                ad = adb.create();
                                ad.show();
                            }else{
                                ContentFile cf = new ContentFile(buffer, getCacheDir());

                                nnla.data.addContent(cf);

                                lv.setAdapter(nnla);
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        break;
                    case REQUEST_ALARM :
                        try{
                            c.setTimeInMillis(data.getExtras().getLong("alarm"));
                            repeat = data.getExtras().getLong("repeat");
                            alarm.setBackgroundResource(R.drawable.set_alarm);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private File getFileFromUriImage(Uri uri)
    {
        try{
            Cursor c = getContentResolver().query(uri,new String[]{MediaStore.Images.ImageColumns.DATA},null,null,null);
            c.moveToFirst();

            int index = c.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            String path = c.getString(index);
            c.close();
            return new File(path);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private File getFileFromUriVideo(Uri uri)
    {
        try{
            Cursor c = getContentResolver().query(uri,new String[]{MediaStore.Video.VideoColumns.DATA},null,null,null);
            c.moveToFirst();

            int index = c.getColumnIndex(MediaStore.Video.VideoColumns.DATA);
            String path = c.getString(index);
            c.close();
            return new File(path);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private File getFileFromUriSound(Uri uri)
    {
        try{
            Cursor c = getContentResolver().query(uri,new String[]{MediaStore.Audio.AudioColumns.DATA},null,null,null);
            c.moveToFirst();

            int index = c.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);
            String path = c.getString(index);
            c.close();
            return new File(path);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private File getFileFromUriFile(Uri uri)
    {
        try{
            Cursor c = getContentResolver().query(uri,new String[]{MediaStore.Files.FileColumns.DATA},null,null,null);
            c.moveToFirst();

            int index = c.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            String path = c.getString(index);
            c.close();
            return new File(path);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onDestroy()
    {
        try{
            Functions.deleteFilesInDirectory(new File(getCacheDir()+"//NewEditNoteCache"));
            unbindService(sc);
            unbindService(asc);
            unbindService(isc);
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private int getRemainingSize()
    {
        try{
            return getResources().getInteger(R.integer.max_note_content_size) - nnla.data.getDataSize();
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void onBackPressed() {
        try{
            AlertDialog.Builder adb = new AlertDialog.Builder(NewEditNote.this);
            adb.setTitle(getString(R.string.notice));
            adb.setMessage(getString(R.string.close_withot_save));
            adb.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try{
                        finish();
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });
            adb.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try{
                        ad.dismiss();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });
            ad = adb.create();
            ad.show();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void lock()
    {
        try{
            Intent i = new Intent(getApplicationContext(),Lock.class);
            startActivity(i);
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}