package xyz.willnwalker.parsee;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by William on 1/21/2017.
 */

public class LoginActivity extends AppCompatActivity {

    private final String TAG = "parsee.loginactivity";
    private static final int CREATE_ACCOUNT = 2149;
    private boolean checked=false;
    @BindView(R.id.serviceName) EditText service;
    @BindView(R.id.passwordText) EditText passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    protected void showPassword(View v){
        checked=!checked;
        if(checked){
            passwordText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordText.setSelection(passwordText.getText().length());
        }
        else{
            passwordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordText.setSelection(passwordText.getText().length());
        }
    }

    public void login(View v){
        final String u = service.getText().toString();
        final String p = passwordText.getText().toString();
        if(u.equals("")){
            Toast.makeText(getApplicationContext(),"Please enter your email.",Toast.LENGTH_LONG).show();
        }
        else if(p.equals("")){
            Toast.makeText(getApplicationContext(),"Please enter your password.",Toast.LENGTH_LONG).show();
        }
        else{
            FirebaseAuth.getInstance().signInWithEmailAndPassword(u,p).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(!task.isSuccessful()){
                        Log.d(TAG,"login failed");
                        Toast.makeText(getApplicationContext(),"Login failed! Please check your email and password and try again.",Toast.LENGTH_LONG).show();
                    }
                    else{
                        Bundle b = new Bundle();
                        b.putString("username",u);
                        b.putString("password",p);
                        Intent i = new Intent();
                        i.putExtras(b);
                        setResult(RESULT_OK,i);
                        finish();
                    }
                }
            });
        }
    }

    public void create(View v){
        Intent i = new Intent(this,NewAccountActivity.class);
        startActivityForResult(i,CREATE_ACCOUNT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG,"Activity Result received.");
        // Check which request we're responding to
        if (requestCode == CREATE_ACCOUNT) {
            Log.d(TAG,"code was create account");
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Intent i = new Intent();
                Bundle b = data.getExtras();
                b.putBoolean("newAccount",true);
                //Log.d(TAG,b.getString("username"));
                //Log.d(TAG,b.getString("password"));
                i.putExtras(b);
                setResult(RESULT_OK,i);
                finish();
            }
        }
    }
}
