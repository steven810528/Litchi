package com.ncku.mis.litchi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ecareme.asuswebstorage.ASUSWebStorage;

import net.yostore.aws.api.ApiConfig;
import net.yostore.aws.asyntask.LoginTask;
import net.yostore.aws.dialog.MessageDialog;
import net.yostore.aws.menu.ActivityMenu;
import net.yostore.utility.MD5;


public class loginActivity extends ActionBarActivity {

    private Activity actLogin;

    private final String TAG = "Login";

    //save information used to login
    //private CustomizedInterface ci;
    private ApiConfig apiCfg = null;

    private EditText txtUid = null;
    private EditText txtPwd = null;
    private EditText txtSecure = null;

    private LinearLayout llAuth, llOTP;

    private int preStatus = 999;

    String android_id;
    String uid;
    String pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        actLogin = this;

        apiCfg = ASUSWebStorage.apiCfg;
        //Parse.enableLocalDatastore(this);
        //Parse.initialize(this, "9SEi5AMIUibTgWPBZAAFKxvw2DIOSYv0LQFrCFHu", "fGQhe7GTXIoO3CYAIhfesUdr87YWZ1Ezdu5wSsOQ");

        android_id = Secure.getString(this.getBaseContext().getContentResolver(), Secure.ANDROID_ID);

        //connect the layout item to java
        txtUid = (EditText) findViewById(R.id.edtUid);
        txtPwd = (EditText) findViewById(R.id.edtPwd);
        txtSecure = (EditText) findViewById(R.id.edtSecureCode);

        //txtUid.setText("asuscloudtrain");
        //txtPwd.setText("traintest");

        //txtUid.setText("steven810528@gmail.com");
        //txtPwd.setText("abc123456");


        txtUid.setText("r76031113@ncku.edu.tw");
        txtPwd.setText("abc123456");



        //connect to the input field
        llAuth = (LinearLayout) findViewById(R.id.llAuth);
        llOTP = (LinearLayout) findViewById(R.id.llOTP);

    }
    public void loginFunction(View v) {
        StringBuilder msg = new StringBuilder();

        uid = txtUid.getText().toString();

        pwd = txtPwd.getText().toString();
        String sec = txtSecure.getText().toString();


        //if id and pwd ==nil, then quit the function
        if (uid.trim().length() == 0 || pwd.trim().length() == 0) {
            msg.delete(0, msg.length());
            msg.append("Login info could not be empty!");
            Log.e(TAG, msg.toString());
            Toast.makeText(actLogin, msg.toString(), Toast.LENGTH_LONG).show();
            return;
        }
        //check the security mode
        if ((preStatus == 504 || preStatus == 508) && sec.trim().length() == 0) {
            msg.delete(0, msg.length());
            if (preStatus == 504)
                msg.append("Please input OTP security code!");
            else
                msg.append("Please input CAPTCHA code!");

            Log.e(TAG, msg.toString());

            Toast.makeText(actLogin, msg.toString(), Toast.LENGTH_LONG).show();
            return;
        }


        apiCfg.userid = uid.trim();



        // Hash password :
        // 1. make lower case of password
        // 2. Doing hash by MD5
        // 3. Converting to be Hex Text
        apiCfg.password = MD5.encode(pwd.trim().toLowerCase());
        //apiCfg.password =pwd.trim();

        // Switch to my space activity
        Intent intent = new Intent();
        intent.setClass(actLogin, myBrowseActivity.class);

        LoginTask loginTask = new LoginTask(actLogin, apiCfg, intent,
                llOTP.getVisibility() == View.VISIBLE ? sec != null
                        && sec.trim().length() > 0 ? sec : null : null) {

            @Override
            protected void loginFail(Integer result) {
                txtUid.setText("");
                txtPwd.setText("");
                txtSecure.setText("");

                if (result != 999)
                    MessageDialog.show(actLogin, getString(R.string.app_name),
                            "Login fail");
                else {
                    MessageDialog.show(actLogin, getString(R.string.app_name),
                            "System Error (" + result + ")");
                    finish();
                    return;
                }

                if (llAuth.getVisibility() != View.VISIBLE)
                    switch2Auth();
            }

            @Override
            protected void goOTP(Integer result) {
                preStatus = result;

                if (result == 505) {
                    MessageDialog.show(actLogin, getString(R.string.app_name),
                            "Your OTP has been LOCKED!");
                    finish();
                } else if (llOTP.getVisibility() != View.VISIBLE)
                    switch2OTP();
            }

        };
        try {
            loginTask.execute((Void) null);
        }
        catch(Exception ex)
        {
            Log.e(TAG,ex.toString());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inf = getMenuInflater();
        inf.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean bRtn = super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.mmuAbout:
                ActivityMenu.openOptionsDialog(actLogin);
                break;
            case R.id.mmuExit:
                finish();
        }
        return bRtn;
    }

    private void switch2OTP() {
        //loginParse(this.android_id);
        //MessageDialog.show(this,"id",this.android_id);
        llOTP.setVisibility(View.VISIBLE);
        llAuth.setVisibility(View.GONE);
    }

    private void switch2Auth() {
        //MessageDialog.show(this,"id",this.android_id);
        llAuth.setVisibility(View.VISIBLE);
        llOTP.setVisibility(View.GONE);
    }

}
