package sandstorm.com.thenotebook.ui.AccountManagment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import sandstorm.com.thenotebook.R;
import sandstorm.com.thenotebook.functions.Functions;

public class ForgetPassword extends AppCompatActivity
{
    Dialog d;
    EditText email;
    Button confirm;
    AlertDialog ad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            getSupportActionBar().hide();

            initializeComponents();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void initializeComponents()
    {
        try{
            d = new Dialog(ForgetPassword.this);
            d.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            d.setContentView(R.layout.forget_password_dialog);
            d.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels*90/100, WindowManager.LayoutParams.WRAP_CONTENT);
            d.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    try{
                        finish();
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            d.show();

            email = (EditText)d.findViewById(R.id.forget_pass_email);
            confirm = (Button)d.findViewById(R.id.forget_pass_confirm);

            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        if(!Functions.isConnectedToInternet(getApplicationContext())) {
                            Toast.makeText(getApplicationContext(),getString(R.string.not_connceted),Toast.LENGTH_LONG).show();
                        }else if(email.getText().toString().isEmpty()) {
                            email.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in));
                        }else{
                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        URL url = new URL("http://sandstormweb.com/the_notebook/forget_password.php");
                                        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                                        huc.setDoInput(true);
                                        huc.setDoOutput(true);
                                        huc.setRequestMethod("POST");

                                        String em = "email="+Base64.encodeToString(email.getText().toString().getBytes("UTF-8"),0);

                                        Log.d("myData", "what we are sending : "+email.getText().toString());

                                        DataOutputStream dos = new DataOutputStream(huc.getOutputStream());
                                        dos.write(em.getBytes("UTF-8"));
                                        dos.close();

                                        DataInputStream dis = new DataInputStream(huc.getInputStream());
                                        Scanner sc = new Scanner(dis).useDelimiter("\\A");
                                        final String result = sc.nextLine();
                                        Log.d("myData","result of forget : "+result);

                                        if(result.equals("true")){
                                            d.dismiss();
                                            final AlertDialog.Builder adb = new AlertDialog.Builder(ForgetPassword.this);
                                            adb.setTitle(getString(R.string.forget_password));
                                            adb.setMessage(getString(R.string.password_sent));
                                            adb.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                                @Override
                                                public void onCancel(DialogInterface dialog) {
                                                    try{
                                                        finish();
                                                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                                    }catch(Exception e){
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                            adb.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    try{
                                                        ad.dismiss();
                                                        finish();
                                                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                                    }catch(Exception e){
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try{
                                                        ad = adb.create();
                                                        ad.show();
                                                    }catch(Exception e){
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });

                                        }else{
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        Toast.makeText(getApplicationContext(), getString(R.string.your_account_is_not_exist), Toast.LENGTH_LONG).show();
                                                    }catch(Exception e){
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(), getString(R.string.database_not_responding), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                            t.start();

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
}
