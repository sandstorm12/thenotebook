package sandstorm.com.thenotebook.ui.signinSignup;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import sandstorm.com.thenotebook.R;
import sandstorm.com.thenotebook.services.InfoService;
import sandstorm.com.thenotebook.ui.AccountManagment.ForgetPassword;
import sandstorm.com.thenotebook.ui.MainActivity;

public class Lock extends Activity
{
    Button go;
    TextView forget;
    EditText pass;
    InfoService is;

    ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try{
                is = ((InfoService.MyBinder)service).getService();

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.toolbar));
            }

            setContentView(R.layout.lock);

            Intent i = new Intent(getApplicationContext(), InfoService.class);
            bindService(i,sc,0);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void goToMainActivity()
    {
        try{
            if(getIntent().hasExtra("main")){
                Intent i = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(i);
            }
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void initializeComponents()
    {
        try{
            go = (Button)findViewById(R.id.lock_go);
            pass = (EditText)findViewById(R.id.lock_password);
            forget = (TextView)findViewById(R.id.lock_forget);

            go.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        if(pass.getText().toString().isEmpty()){
                            pass.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in));
                        }else
                        if(pass.getText().toString().equals(is.getCacheData().getContentByName("password"))){
                            goToMainActivity();
                        }else{
                            Toast.makeText(getApplicationContext(),getString(R.string.your_password_is_wrong),Toast.LENGTH_LONG).show();
                        }
                    }catch(Exception e){
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

            pass.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    try{
                        if(pass.getText().toString().isEmpty()){
                            pass.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in));
                        }else if(pass.getText().toString().equals(is.getCacheData().getContentByName("password"))){
                            goToMainActivity();
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
    public void onBackPressed() {
        try{

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
}
