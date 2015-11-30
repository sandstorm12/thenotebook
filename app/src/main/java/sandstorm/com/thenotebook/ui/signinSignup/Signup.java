package sandstorm.com.thenotebook.ui.signinSignup;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import sandstorm.com.thenotebook.R;
import sandstorm.com.thenotebook.data_structures.InfoBundle;
import sandstorm.com.thenotebook.services.AlarmService;
import sandstorm.com.thenotebook.services.DataService;
import sandstorm.com.thenotebook.services.InfoService;
import sandstorm.com.thenotebook.services.SyncService;
import sandstorm.com.thenotebook.ui.MainActivity;
import sandstorm.com.thenotebook.ui.setting.TermsOfService;

public class Signup extends AppCompatActivity
{
    TextView haveAccount, terms;
    EditText email, password, cPassword;
    CheckBox agree;
    InfoService is;
    InfoBundle ib;
    Button signup;

    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try{
                is = ((InfoService.MyBinder)service).getService();
                is.addOnInfoLoadedListener(new InfoService.OnInfoLoaded() {
                    @Override
                    public void onInfoLoaded() {
                        try{
                            ib = is.getCacheData();

                            if(is.getCacheData().getContentByName("email").equalsIgnoreCase("null")){
                                setContentView(R.layout.signup);

                                constructor();
                            }else if(!is.getCacheData().getContentByName("lock").equals("0")){
                                Intent i = new Intent(getApplicationContext(),Lock.class);
                                i.putExtra("main","main");
                                startActivity(i);
                                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                finish();
                            }else{
                                goToMainActivity();
                            }
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.toolbar));
            }

            connectToServices();
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

            i = new Intent(getApplicationContext(), SyncService.class);
            startService(i);

            i = new Intent(getApplicationContext(), AlarmService.class);
            startService(i);

            i = new Intent(getApplicationContext(), InfoService.class);
            startService(i);

            bindService(i,sc,0);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void constructor()
    {
        try{
            haveAccount = (TextView)findViewById(R.id.signup_have_account);
            email = (EditText)findViewById(R.id.signup_email);
            password = (EditText)findViewById(R.id.signup_password);
            cPassword = (EditText)findViewById(R.id.signup_cpassword);
            agree = (CheckBox)findViewById(R.id.signup_agree);
            terms = (TextView)findViewById(R.id.signup_terms);
            signup = (Button)findViewById(R.id.signup_signup);

            haveAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        Intent i = new Intent(getApplicationContext(),Signin.class);
                        startActivity(i);
                        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                        finish();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });

            signup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        Animation an = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in);
                        if(email.getText().toString().isEmpty()){
                            email.startAnimation(an);
                            return;
                        }
                        if(password.getText().toString().isEmpty()){
                            password.startAnimation(an);
                            return;
                        }
                        if(cPassword.getText().toString().isEmpty()){
                            cPassword.startAnimation(an);
                            return;
                        }
                        if(!agree.isChecked())
                        {
                            agree.startAnimation(an);
                            return;
                        }
                        if(!password.getText().toString().equals(cPassword.getText().toString()))
                        {
                            password.startAnimation(an);
                            cPassword.startAnimation(an);
                            return;
                        }
                        if(isConnected()){
                            an = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.blink);
                            signup.startAnimation(an);
                            signup.setText(R.string.wait);
                            signup();
                        }else{
                            Toast.makeText(getApplicationContext(),getString(R.string.not_connceted),Toast.LENGTH_LONG).show();
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });

            terms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        Intent i = new Intent(getApplicationContext(), TermsOfService.class);
                        startActivity(i);
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
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
    protected void onDestroy(){
        try{
            unbindService(sc);
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
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

    private void signup()
    {
        try{
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        URL url = new URL("http://sandstormweb.com/the_notebook/signup.php");
                        HttpURLConnection huc = (HttpURLConnection)url.openConnection();
                        huc.setRequestMethod("POST");
                        huc.setDoInput(true);
                        huc.setDoOutput(true);

                        String Email = Base64.encodeToString(email.getText().toString().getBytes("UTF-8"), 0);
                        String Password = Base64.encodeToString(password.getText().toString().getBytes("UTF-8"),0);

                        String request = "email="+Email+"&password="+Password;

                        DataOutputStream dos = new DataOutputStream(huc.getOutputStream());
                        dos.write(request.getBytes("UTF-8"));
                        dos.close();

                        DataInputStream dis = new DataInputStream(huc.getInputStream());
                        Scanner scan = new Scanner(dis).useDelimiter("\\A");
                        String result = scan.nextLine();

                        if(result.equalsIgnoreCase("true")){
                            is.getCacheData().getInfoItemByName("email").setContent(email.getText().toString());
                            is.getCacheData().setContentByName("password",password.getText().toString());
                            is.saveData(is.getCacheData());
                            goToMainActivity();
                        }else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),getString(R.string.account_used_before),Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),getString(R.string.database_not_responding),Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            signup.clearAnimation();
                            signup.setText(R.string.signup);
                        }
                    });
                }
            });
            t.start();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void goToMainActivity()
    {
        try{
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            finish();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
