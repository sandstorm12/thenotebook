package sandstorm.com.thenotebook.ui;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.*;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.*;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;
import java.util.TimeZone;

import sandstorm.com.thenotebook.R;
import sandstorm.com.thenotebook.data_structures.*;
import sandstorm.com.thenotebook.services.*;
import sandstorm.com.thenotebook.ui.AccountManagment.UpgradeRequest;
import sandstorm.com.thenotebook.ui.drawer.*;
import sandstorm.com.thenotebook.ui.noteList.NotebookFragment;
import sandstorm.com.thenotebook.ui.setting.Setting;
import sandstorm.com.thenotebook.ui.signinSignup.Lock;
import sandstorm.com.thenotebook.ui.AccountManagment.ManageAccount;


public class MainActivity extends AppCompatActivity
{
    private DataService ds;
    private SyncService ss;
    private DataBundle db;
    private InfoService is;
    private InfoBundle ib;
    private Toolbar tb;
    private DrawerLayout dl;
    private ListView drawerListView;
    private FragmentManager fm;
    private NotebookFragment currentFragment;
    private LinearLayout ll;
    private Dialog d;
    private AlertDialog ad;
    private Menu menu;
    private Button sync;
    private ProgressBar pb;
    private SearchView sv;
    private int lastMode;
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ds = ((DataService.MyBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private ServiceConnection isc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try{
                is = ((InfoService.MyBinder)service).getService();
                checkForMessages();
                Log.d("myData","check message called");
                is.addOnInfoLoadedListener(new InfoService.OnInfoLoaded() {
                    @Override
                    public void onInfoLoaded() {
                        try{
                            ib = is.getCacheData();

                            initializeDrawerList();
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                is.loadData();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private ServiceConnection ssc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try{
                ss = ((SyncService.MyBinder)service).getService();
                ss.addConstantOnSyncStartedListener(new SyncService.OnSyncStartedListener() {
                    @Override
                    public void onSyncStart() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pb.setIndeterminate(true);
                                sync.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.blink));
                            }
                        });
                    }
                });
                ss.addConstantOnCurrentNoteChangeListener(new SyncService.OnCurrentNoteChangeListener() {
                    @Override
                    public void onCurrentNoteChange() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sync.setText(ss.getCurrentTask());
                            }
                        });
                    }
                });
                ss.addConstantOnSyncCompleteListener(new SyncService.OnSyncCompleteListener() {
                    @Override
                    public void onSyncCompleted() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(pb != null) pb.setIndeterminate(false);
                                sync.setText(R.string.sync);
                                sync.clearAnimation();
                            }
                        });
                    }
                });
                ss.addConstantOnSyncFailedListener(new SyncService.OnSyncFailedListener() {
                    @Override
                    public void onSyncFailed() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(pb != null) pb.setIndeterminate(false);
                                sync.setText(R.string.sync);
                                sync.clearAnimation();

                            }
                        });
                    }
                });
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

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public static final int REQUEST_NEW_TEXT = 0;
    public static final int REQUEST_NEW_SOUND = 2;
    public static final int REQUEST_NEW_VIDEO = 3;
    public static final int REQUEST_NEW_IMAGE = 4;
    public static final int REQUEST_NEW_FILE = 5;
    public static final int REQUEST_EDIT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.black));
            }

            setContentView(R.layout.activity_main);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.BLACK);
            }

            constructor();

            makeUI();

            initializeFragments();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try{
            connectToServices();
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

            i = new Intent(getApplicationContext(), SyncService.class);
            bindService(i,ssc,0);

            i = new Intent(getApplicationContext(), InfoService.class);
            bindService(i,isc,0);

            i = new Intent(getApplicationContext(), AlarmService.class);
            bindService(i,asc,0);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void makeUI()
    {
        try{
//            TOOLBAR
            setSupportActionBar(tb);

            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.notebooks);

            ActionBarDrawerToggle abdt = new ActionBarDrawerToggle(this,dl,tb,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
            dl.setDrawerListener(abdt);
            abdt.setDrawerIndicatorEnabled(true);
            abdt.syncState();

            dl.setScrimColor(Color.TRANSPARENT);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void constructor()
    {
        try{
//            CONSTRUCTION
            dl = (DrawerLayout)findViewById(R.id.main_drawerLayout);
            tb = (Toolbar)findViewById(R.id.main_toolbar);
            drawerListView = (ListView)findViewById(R.id.drawer_listView);
            ll = (LinearLayout)findViewById(R.id.main_frameLayout);
            sync = (Button)findViewById(R.id.drawer_sync);
            pb = (ProgressBar)findViewById(R.id.main_progressBar);
            sv = (android.support.v7.widget.SearchView)findViewById(R.id.main_search_view);

            sync.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        if(isConnected()) {
                            if(is.getCacheData().getContentByName("isPremium").equals("1")){
                                Log.d("myData","sync called by click");
                                ss.sync();
                            }else{
                                Intent i = new Intent(getApplicationContext(), UpgradeRequest.class);
                                startActivity(i);
                                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_in);
                            }
                        }else{
                            Toast.makeText(getApplicationContext(), getString(R.string.not_connceted), Toast.LENGTH_LONG).show();
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

    private void initializeDrawerList()
    {
        try{
            DrawerListAdapter dla = new DrawerListAdapter();

            ArrayList<DrawerListItem> data = new ArrayList();
            DrawerListItem one = new DrawerListItem(is.getCacheData().getContentByName("email"),getResources().getDrawable(R.drawable.temp_avatar),DrawerListItem.TYPE_ACCOUNT);
            DrawerListItem two = new DrawerListItem(getString(R.string.notebooks),getResources().getDrawable(R.drawable.notebook),DrawerListItem.TYPE_ITEM);
            DrawerListItem three = new DrawerListItem(getString(R.string.all_notes),getResources().getDrawable(R.drawable.note),DrawerListItem.TYPE_ITEM);
            DrawerListItem four = new DrawerListItem(getString(R.string.reminder),getResources().getDrawable(R.drawable.reminder),DrawerListItem.TYPE_ITEM);
            DrawerListItem five = new DrawerListItem(getString(R.string.trash),getResources().getDrawable(R.drawable.trash),DrawerListItem.TYPE_ITEM);
            DrawerListItem six = new DrawerListItem(getString(R.string.base)+" :",null,DrawerListItem.TYPE_SEPARATOR);
            DrawerListItem seven = new DrawerListItem(getString(R.string.setting),getResources().getDrawable(R.drawable.setting),DrawerListItem.TYPE_ITEM);

            data.add(one);
            data.add(two);
            data.add(three);
            data.add(four);
            data.add(five);
            data.add(six);
            data.add(seven);

            dla.setData(data);

            drawerListView.setAdapter(dla);

            drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try{
                        switch(position)
                        {
                            case 0 :
                                Intent i0 = new Intent(getApplicationContext(), ManageAccount.class);
                                startActivity(i0);
                                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                dl.closeDrawer(GravityCompat.START);
                                break;
                            case 1 :
                                currentFragment.setMode(NotebookFragment.MODE_NOTEBOOK, null);
                                getSupportActionBar().setTitle(R.string.notebooks);
                                dl.closeDrawer(GravityCompat.START);
                                showMenuItem(0);
                                showMenuItem(1);
                                break;
                            case 2 :
                                currentFragment.setMode(NotebookFragment.MODE_ALL_NOTES, null);
                                getSupportActionBar().setTitle(R.string.all_notes);
                                dl.closeDrawer(GravityCompat.START);
                                hideMenuItem(1);
                                showMenuItem(0);
                                break;
                            case 3 :
                                currentFragment.setMode(NotebookFragment.MODE_REMINDER, null);
                                getSupportActionBar().setTitle(R.string.reminder);
                                dl.closeDrawer(GravityCompat.START);
                                hideMenuItem(1);
                                showMenuItem(0);
                                break;
                            case 4 :
                                currentFragment.setMode(NotebookFragment.MODE_TRASH, null);
                                getSupportActionBar().setTitle(R.string.trash);
                                dl.closeDrawer(GravityCompat.START);
                                hideMenuItem(0);
                                hideMenuItem(1);
                                break;
                            case 6 :
                                Intent i = new Intent(getApplicationContext(),Setting.class);
                                startActivity(i);
                                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                dl.closeDrawer(GravityCompat.START);
                                break;
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });

            if(!is.getCacheData().getContentByName("lock").equals("0") && getIntent().hasExtra("new")) lock();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void initializeFragments()
    {
        try{
            fm = getSupportFragmentManager();
            currentFragment = (NotebookFragment)fm.findFragmentById(R.id.noteFragment);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            this.menu = menu;
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try{
            switch(item.getItemId())
            {
                case R.id.menu_add_notebook :
                    d = new Dialog(MainActivity.this);
                    d.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    d.setContentView(R.layout.new_notebook_dialog);
                    d.show();
                    d.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels*90/100, WindowManager.LayoutParams.WRAP_CONTENT);

                    final EditText name = (EditText)d.findViewById(R.id.new_notebook_dialog_name);

                    Button add = (Button)d.findViewById(R.id.new_notebook_dialog_add);
                    add.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Notebook n = new Notebook(System.currentTimeMillis(),currentFragment.getCurrentNotebookId(),Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis(),Notebook.STATUS_ACTIVE,name.getText().toString(),0);
                            ds.getCachedData().addNotebook(n);
                            ds.saveData(ds.getCachedData());
                            d.dismiss();
                        }
                    });
                    break;
                case R.id.menu_search :
                    tb.setVisibility(View.GONE);
                    sv.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_in));
                    sv.setVisibility(View.VISIBLE);
                    sv.setIconified(false);
                    lastMode = currentFragment.getMode();
                    currentFragment.setMode(NotebookFragment.MODE_SEARCH,sv.getQuery().toString());

                    sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            try{
                                currentFragment.setMode(NotebookFragment.MODE_SEARCH,sv.getQuery().toString());
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                            return false;
                        }
                    });

                    sv.setOnCloseListener(new SearchView.OnCloseListener() {
                        @Override
                        public boolean onClose() {
                            tb.setVisibility(View.VISIBLE);
                            sv.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_out));
                            sv.setVisibility(View.GONE);
                            currentFragment.setMode(lastMode,null);
                            return false;
                        }
                    });
                    break;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        try{
            unbindService(sc);
            unbindService(isc);
            unbindService(ssc);
            unbindService(asc);
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onStop();
    }

    public void hideMenuItem(int index)
    {
        try{
            menu.getItem(index).setVisible(false);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void showMenuItem(int index)
    {
        try{
            menu.getItem(index).setVisible(true);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private boolean isConnected()
    {
        try{
            ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            if(cm.getActiveNetworkInfo() == null) return false;
            else return true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        try{
            if(dl.isDrawerOpen(GravityCompat.START)){
                dl.closeDrawer(GravityCompat.START);
            }else if(currentFragment.getCurrentNotebookId() != 0){
                currentFragment.goOneLevelUp();
            }else if(sv.isShown()) {
                tb.setVisibility(View.VISIBLE);
                sv.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_out));
                sv.setVisibility(View.GONE);
                currentFragment.setMode(lastMode,null);
            }else if(currentFragment.isFabOpen()) {
                currentFragment.dispatchFabClick();
            }else{
                finish();
            }
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

    private void checkForMessages()
    {
        try{
            Log.d("myData","in check message : "+Boolean.toString(Calendar.getInstance().after(Long.parseLong(is.getCacheData().getContentByName("mb"))))+"  "+Long.toString(Calendar.getInstance().getTimeInMillis())+" "+is.getCacheData().getContentByName("mb"));
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(Long.parseLong(is.getCacheData().getContentByName("mb")));
            if(Calendar.getInstance().after(c)) {
                Log.d("myData","going to check message");
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL("http://sandstormweb.com/the_notebook/message.php");
                            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                            huc.setRequestMethod("POST");
                            huc.setDoInput(true);
                            huc.setDoOutput(true);

                            DataOutputStream dos = new DataOutputStream(huc.getOutputStream());
                            dos.write("version=0.1".getBytes("UTF-8"));
                            dos.close();

                            DataInputStream dis = new DataInputStream(huc.getInputStream());
                            Scanner sc = new Scanner(dis).useDelimiter("\\A");
                            final String result = sc.nextLine();

                            if (!result.equals("false")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
                                            adb.setTitle(getString(R.string.developer_message));
                                            adb.setMessage(result);
                                            adb.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    ad.dismiss();
                                                }
                                            });
                                            adb.setNegativeButton(getString(R.string.dont_show), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    try{
                                                        Calendar c = Calendar.getInstance();
                                                        c.add(Calendar.DATE,7);
                                                        is.getCacheData().setContentByName("mb", Long.toString(c.getTimeInMillis()));
                                                        is.saveData(is.getCacheData());
                                                        ad.dismiss();
                                                    }catch(Exception e){
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                            ad = adb.create();
                                            ad.show();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                t.start();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
