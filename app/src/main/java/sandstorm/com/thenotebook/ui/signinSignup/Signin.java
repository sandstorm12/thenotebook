package sandstorm.com.thenotebook.ui.signinSignup;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
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
import sandstorm.com.thenotebook.services.AlarmService;
import sandstorm.com.thenotebook.services.DataService;
import sandstorm.com.thenotebook.services.InfoService;
import sandstorm.com.thenotebook.services.SyncService;
import sandstorm.com.thenotebook.ui.MainActivity;
import sandstorm.com.thenotebook.ui.AccountManagment.ForgetPassword;

public class Signin extends AppCompatActivity
{
    EditText email,password;
    Button signin;
    TextView forget;

    InfoService is;
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try{
                is = ((InfoService.MyBinder)service).getService();
            }catch (Exception e){
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

            setContentView(R.layout.signin);

            constructor();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try{
            connectToServices();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void constructor()
    {
        try{
            email = (EditText)findViewById(R.id.signin_email);
            password = (EditText)findViewById(R.id.signin_password);
            signin = (Button)findViewById(R.id.signin_signin);
            forget = (TextView)findViewById(R.id.signin_forget);

            signin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Animation an = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                        if(email.getText().toString().isEmpty()){
                            email.startAnimation(an);
                            return;
                        }
                        if(password.getText().toString().isEmpty()){
                            password.startAnimation(an);
                            return;
                        }
                        if(isConnected()){
                            an = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.blink);
                            signin.startAnimation(an);
                            signin.setText(R.string.wait);
                            signin();
                        }else{
                            Toast.makeText(getApplicationContext(), getString(R.string.not_connceted), Toast.LENGTH_LONG).show();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });

            forget.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        Intent i = new Intent(getApplicationContext(), ForgetPassword.class);
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

    private void connectToServices()
    {
        try{
            Intent i = new Intent(getApplicationContext(), InfoService.class);
            startService(i);

            bindService(i,sc,0);

            i = new Intent(getApplicationContext(),DataService.class);
            startService(i);

            i = new Intent(getApplicationContext(), SyncService.class);
            startService(i);

            i = new Intent(getApplicationContext(), AlarmService.class);
            startService(i);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        try{
            Intent i = new Intent(getApplicationContext(),Signup.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            finish();
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

    private void signin()
    {
        try{
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        URL url = new URL("http://sandstormweb.com/the_notebook/login.php");
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
                            is.updatePremium();
                            if(is.getCacheData().getContentByName("password").equals("null")){
                                Intent i = new Intent(getApplicationContext(),Lock.class);
                                startActivity(i);
                                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                                finish();
                            }else {
                                goToMainActivity();
                            }
                        }else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), R.string.account_not_exists, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),R.string.database_not_responding,Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            signin.clearAnimation();
                            signin.setText(R.string.signin);
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

    @Override
    protected void onStop() {
        try{
            unbindService(sc);
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onStop();
    }
}
