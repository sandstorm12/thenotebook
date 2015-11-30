package sandstorm.com.thenotebook.ui.AccountManagment;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import sandstorm.com.thenotebook.R;
import sandstorm.com.thenotebook.functions.Functions;
import sandstorm.com.thenotebook.services.AlarmService;
import sandstorm.com.thenotebook.services.DataService;
import sandstorm.com.thenotebook.services.InfoService;
import sandstorm.com.thenotebook.services.SyncService;
import sandstorm.com.thenotebook.ui.signinSignup.Signup;

public class ManageAccount extends AppCompatActivity
{
    Toolbar tb;
    Button seeOffers, updateInfo, changePassword, logout, deleteAccount;
    TextView type, email;
    InfoService is;
    Dialog d;
    AlertDialog ad;

    ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try{
                is = ((InfoService.MyBinder)service).getService();
                if(is.getCacheData().getContentByName("isPremium").equals("1")){
                    type.setText(getString(R.string.type_premium));
                }else{
                    type.setText(getString(R.string.type));
                }
                email.setText(is.getCacheData().getContentByName("email"));

                is.addConstantOnInfoSavedListener(new InfoService.OnInfoSaved() {
                    @Override
                    public void onInfoSaved() {
                        try{
                            if(is.getCacheData().getContentByName("isPremium").equals("1")){
                                type.setText(getString(R.string.type_premium));
                            }else{
                                type.setText(getString(R.string.type));
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

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    ServiceConnection asc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try{
                AlarmService as = ((AlarmService.MyBinder)service).getService();
                as.eraseAllAlarms();
                Intent i = new Intent(getApplicationContext(),AlarmService.class);
                stopService(i);

                i = new Intent(getApplicationContext(), DataService.class);
                stopService(i);

                i = new Intent(getApplicationContext(), Signup.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
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

            setContentView(R.layout.manage_account);

            initializeComponents();

            initializeToolbar();

            Intent i = new Intent(getApplicationContext(), InfoService.class);
            bindService(i,sc,0);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void initializeComponents()
    {
        try{
            tb = (Toolbar)findViewById(R.id.manage_account_toolbar);
            seeOffers = (Button)findViewById(R.id.manage_account_see_offers);
            type = (TextView)findViewById(R.id.manage_account_type);
            updateInfo = (Button)findViewById(R.id.manage_account_update);
            changePassword = (Button)findViewById(R.id.manage_account_change_password);
            deleteAccount = (Button)findViewById(R.id.manage_account_delete_account);
            logout = (Button)findViewById(R.id.manage_account_logout);
            email = (TextView)findViewById(R.id.manage_account_email);

            changePassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        if(Functions.isConnectedToInternet(getApplicationContext())) {
                            d = new Dialog(ManageAccount.this);
                            d.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

                            d.setContentView(R.layout.change_password);
                            d.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 90 / 100, WindowManager.LayoutParams.WRAP_CONTENT);
                            d.show();

                            final EditText old = (EditText) d.findViewById(R.id.change_password_old);
                            final EditText New = (EditText) d.findViewById(R.id.change_password_new);
                            final EditText confirm = (EditText) d.findViewById(R.id.change_password_confirm);
                            TextView forget = (TextView)d.findViewById(R.id.change_password_forget);
                            Button change = (Button) d.findViewById(R.id.change_password_change);
                            change.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (old.getText().toString().isEmpty()) {
                                        old.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
                                    } else if (New.getText().toString().isEmpty()) {
                                        New.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
                                    } else if (confirm.getText().toString().isEmpty()) {
                                        confirm.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
                                    } else if (!New.getText().toString().equals(confirm.getText().toString())) {
                                        New.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
                                        confirm.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
                                    } else {
                                        changePassword(is.getCacheData().getContentByName("email"), old.getText().toString(), New.getText().toString());
                                    }
                                }
                            });
                            forget.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try{
                                       Intent i = new Intent(getApplicationContext(),ForgetPassword.class);
                                       startActivity(i);
                                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(getApplicationContext(),getString(R.string.not_connceted),Toast.LENGTH_LONG).show();
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });

            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Functions.isConnectedToInternet(getApplicationContext())) {
                        logout();
                    }else{
                        Toast.makeText(getApplicationContext(), getString(R.string.not_connceted), Toast.LENGTH_LONG).show();
                    }
                }
            });

            deleteAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Functions.isConnectedToInternet(getApplicationContext())) {
                        deleteAccount();
                    }else{
                        Toast.makeText(getApplicationContext(), getString(R.string.not_connceted), Toast.LENGTH_LONG).show();
                    }
                }
            });

            seeOffers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if(Functions.isConnectedToInternet(getApplicationContext())) {
                            Intent i = new Intent(getApplicationContext(), Upgrade.class);
                            startActivity(i);
                        }else{
                            Toast.makeText(getApplicationContext(), getString(R.string.not_connceted), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            updateInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        if(Functions.isConnectedToInternet(getApplicationContext()))
                        {
                            Toast.makeText(getApplicationContext(), getString(R.string.please_wait), Toast.LENGTH_LONG).show();
                            is.updatePremium();
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

    private void initializeToolbar()
    {
        try{
            tb.setTitle(getString(R.string.manage_account));
            setSupportActionBar(tb);

            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        }catch(Exception e){
            e.printStackTrace();
        }
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

    private void changePassword(final String email, final String password, final String newPassword)
    {
        try{
            if(Functions.isConnectedToInternet(getApplicationContext())){
                Toast.makeText(getApplicationContext(),getString(R.string.please_wait),Toast.LENGTH_LONG).show();
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            URL url = new URL("http://sandstormweb.com/the_notebook/change_password.php");
                            HttpURLConnection huc = (HttpURLConnection)url.openConnection();
                            huc.setDoInput(true);
                            huc.setDoOutput(true);
                            huc.setRequestMethod("POST");

                            String e = Base64.encodeToString(email.getBytes("UTF-8"),0);
                            String p = Base64.encodeToString(password.getBytes("UTF-8"),0);
                            String np = Base64.encodeToString(newPassword.getBytes("UTF-8"),0);

                            String data = "email="+e+"&password="+p+"&newPassword="+np;

                            DataOutputStream dos = new DataOutputStream(huc.getOutputStream());
                            dos.write(data.getBytes("UTF-8"));
                            dos.close();

                            DataInputStream dis = new DataInputStream(huc.getInputStream());
                            Scanner sc = new Scanner(dis).useDelimiter("\\A");
                            final String result = sc.nextLine();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Log.d("myData","result : "+result );
                                        if (result.equalsIgnoreCase("true")) {
                                            d.dismiss();
                                            Toast.makeText(getApplicationContext(), getString(R.string.action_successfully_completed), Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), getString(R.string.account_not_exists), Toast.LENGTH_LONG).show();
                                        }
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }catch(Exception e){
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        Toast.makeText(getApplicationContext(),getString(R.string.database_not_responding),Toast.LENGTH_LONG).show();
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                });
                t.start();
            }else{
                Toast.makeText(getApplicationContext(),getString(R.string.not_connceted),Toast.LENGTH_LONG).show();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void deleteAccount()
    {
        try{
            if(Functions.isConnectedToInternet(getApplicationContext())){
                AlertDialog.Builder adb = new AlertDialog.Builder(ManageAccount.this);
                adb.setTitle(getString(R.string.caution));
                adb.setMessage(getString(R.string.you_really_wanna_delete_account));
                adb.setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            ad.dismiss();
                            AlertDialog.Builder adb = new AlertDialog.Builder(ManageAccount.this);
                            adb.setTitle(getString(R.string.caution));
                            adb.setMessage(getString(R.string.all_data_erased));
                            adb.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        try{
                                            if(Functions.isConnectedToInternet(getApplicationContext())) {
                                                d = new Dialog(ManageAccount.this);
                                                d.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                                                d.setContentView(R.layout.delete_account);
                                                d.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 90 / 100, WindowManager.LayoutParams.WRAP_CONTENT);
                                                d.show();

                                                final EditText email = (EditText) d.findViewById(R.id.delete_account_email);
                                                final EditText pass = (EditText) d.findViewById(R.id.delete_account_password);
                                                Button delete = (Button) d.findViewById(R.id.delete_account_delete);
                                                TextView forget = (TextView) d.findViewById(R.id.delete_account_forget);
                                                delete.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        if (email.getText().toString().isEmpty()) {
                                                            email.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
                                                        } else if (pass.getText().toString().isEmpty()) {
                                                            pass.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
                                                        } else {
                                                            deleteInternal(email.getText().toString(),pass.getText().toString());
                                                        }
                                                    }
                                                });
                                                forget.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        try{
                                                            Intent i = new Intent(getApplicationContext(),ForgetPassword.class);
                                                            startActivity(i);
                                                            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                                        }catch(Exception e){
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });
                                            }else{
                                                Toast.makeText(getApplicationContext(),getString(R.string.not_connceted),Toast.LENGTH_LONG).show();
                                            }
                                        }catch(Exception e){
                                            e.printStackTrace();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            adb.setNegativeButton(getString(R.string.cancel),new DialogInterface.OnClickListener() {
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
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }
                    }
                });
                adb.setNegativeButton(getString(R.string.cancel),new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            ad.dismiss();
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                ad = adb.create();
                ad.show();
            }else{
                Toast.makeText(getApplicationContext(),getString(R.string.not_connceted),Toast.LENGTH_LONG).show();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void logout()
    {
        try{
            AlertDialog.Builder adb = new AlertDialog.Builder(ManageAccount.this);
            adb.setTitle(getString(R.string.caution));
            adb.setMessage(getString(R.string.you_really_wanna_logout));
            adb.setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        ad.dismiss();
                        AlertDialog.Builder adb = new AlertDialog.Builder(ManageAccount.this);
                        adb.setTitle(getString(R.string.caution));
                        adb.setMessage(getString(R.string.all_your_data_will_));
                        adb.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    logoutInternal();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        adb.setNegativeButton(getString(R.string.cancel),new DialogInterface.OnClickListener() {
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
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
            });
            adb.setNegativeButton(getString(R.string.cancel),new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
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

    private void logoutInternal()
    {
        try{
            is.eraseAllData();

            Intent i = new Intent(getApplicationContext(), InfoService.class);
            try {
                stopService(i);
            }catch(Exception e){
                e.printStackTrace();
            }

            try {
                i = new Intent(getApplicationContext(), SyncService.class);
                stopService(i);
            }catch(Exception e){
                e.printStackTrace();
            }

            try {
                i = new Intent(getApplicationContext(), AlarmService.class);
                bindService(i, asc, 0);
            }catch(Exception e){
                e.printStackTrace();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        try{
            unbindService(sc);
            try {
                unbindService(asc);
            }catch(Exception e){
                e.printStackTrace();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private void deleteInternal(final String email, final String pass)
    {
        try{
            if(Functions.isConnectedToInternet(getApplicationContext())){
                Toast.makeText(getApplicationContext(),getString(R.string.please_wait),Toast.LENGTH_LONG).show();
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            URL url = new URL("http://sandstormweb.com/the_notebook/delete_account.php");
                            HttpURLConnection huc = (HttpURLConnection)url.openConnection();
                            huc.setDoInput(true);
                            huc.setDoOutput(true);
                            huc.setRequestMethod("POST");

                            String e = Base64.encodeToString(email.getBytes("UTF-8"),0);
                            String p = Base64.encodeToString(pass.getBytes("UTF-8"),0);

                            String data = "email="+e+"&password="+p;

                            DataOutputStream dos = new DataOutputStream(huc.getOutputStream());
                            dos.write(data.getBytes("UTF-8"));
                            dos.close();

                            DataInputStream dis = new DataInputStream(huc.getInputStream());
                            Scanner sc = new Scanner(dis).useDelimiter("\\A");
                            final String result = sc.nextLine();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Log.d("myData","result : "+result );
                                        if (result.equalsIgnoreCase("true")) {
                                            d.dismiss();
                                            logoutInternal();
                                            Toast.makeText(getApplicationContext(), getString(R.string.action_successfully_completed), Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), getString(R.string.account_not_exists), Toast.LENGTH_LONG).show();
                                        }
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }catch(Exception e){
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        Toast.makeText(getApplicationContext(),getString(R.string.database_not_responding),Toast.LENGTH_LONG).show();
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                });
                t.start();
            }else{
                Toast.makeText(getApplicationContext(),getString(R.string.not_connceted),Toast.LENGTH_LONG).show();
            }
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
        super.onBackPressed();
    }
}
