package xyz.willnwalker.parsee;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by William on 1/21/2017.
 */

public class NewAccountActivity extends AppCompatActivity {

    private final String TAG = "parsee.newpactivity";
    private boolean checked=false;
    private EditText service;
    private EditText passwordText;
    private EditText displayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);

        service=(EditText)findViewById(R.id.serviceName);
        passwordText=(EditText)findViewById(R.id.passwordText);
        displayName=(EditText)findViewById(R.id.displayName);
    }

    protected void showPassword(View v){
        this.checked=!checked;
        if(checked){
            passwordText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordText.setSelection(passwordText.getText().length());
        }
        else{
            passwordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordText.setSelection(passwordText.getText().length());
        }
    }

    public void submit(View v){
        final String u = service.getText().toString();
        final String p = passwordText.getText().toString();
        final String n = displayName.getText().toString();
        if(u.equals("")){
            Toast.makeText(getApplicationContext(),"Please enter your email.",Toast.LENGTH_LONG).show();
        }
        else if(p.equals("")){
            Toast.makeText(getApplicationContext(),"Please enter a password.",Toast.LENGTH_LONG).show();
        }
        else if(n.equals("")){
            Toast.makeText(getApplicationContext(),"Please enter a display name.",Toast.LENGTH_LONG).show();
        }
        else{
            Bundle b = new Bundle();
            b.putString("username",u);
            b.putString("password",p);
            b.putString("displayName",n);
            Intent i = new Intent();
            i.putExtras(b);
            setResult(RESULT_OK,i);
            finish();
        }
    }
}
