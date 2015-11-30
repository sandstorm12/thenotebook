package sandstorm.com.thenotebook.ui.AccountManagment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import sandstorm.com.thenotebook.R;
import sandstorm.com.thenotebook.functions.Functions;

public class UpgradeRequest extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            final Dialog d = new Dialog(UpgradeRequest.this);
            d.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            d.setContentView(R.layout.upgrade_request);
            d.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels*90/100, WindowManager.LayoutParams.WRAP_CONTENT);
            d.show();

            d.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });

            d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                }
            });

            Button go = (Button)d.findViewById(R.id.upgrade_request_go);
            go.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        if(Functions.isConnectedToInternet(getApplicationContext())){
                            Intent i = new Intent(getApplication(),Upgrade.class);
                            startActivity(i);
                            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_in);
                            d.dismiss();
                        }else{
                            Toast.makeText(getApplicationContext(),getString(R.string.not_connceted),Toast.LENGTH_LONG).show();
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
