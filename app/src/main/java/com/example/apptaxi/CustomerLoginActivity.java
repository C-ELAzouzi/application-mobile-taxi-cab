package com.example.apptaxi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class CustomerLoginActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private Button login_button;
    private EditText email_txt;
    private EditText password_txt;
    private ProgressDialog loading;
    private TextView register_driver_txt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);
        mAuth=FirebaseAuth.getInstance();
        login_button=(Button)findViewById(R.id.btn_login_customer);
        email_txt=(EditText)findViewById(R.id.email_customer_login);
        password_txt=(EditText)findViewById(R.id.password_customer_login);
        register_driver_txt=(TextView) findViewById(R.id.register_customer);
        loading=new ProgressDialog(this);
        login_button.setOnClickListener(this);
        register_driver_txt.setOnClickListener(this);
    }
    private void loginUser(String email,String password)
    {
        loading.setTitle("login ");
        loading.setMessage("loading.....");
        loading.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loading.dismiss();
                            Intent intent=new Intent(CustomerLoginActivity.this,CustomersMapsActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            loading.dismiss();
                            Toast.makeText(getApplicationContext(),"email or password incorrect",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    @Override
    public void onClick(View v) {

        if(v==login_button)
        {
            String email,password;
            email=email_txt.getText().toString();
            password=password_txt.getText().toString();
            if(email.isEmpty())
            {
                email_txt.setError("email is required");
                return ;
            }
            if(password.isEmpty())
            {
                password_txt.setError("password is required");
                return;
            }
            if(password.length()<6)
            {
                password_txt.setError("length of the email must be more than 5 letter");
                return;
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            {
                email_txt.setError("enter a validate email");
                return;
            }
            loginUser(email,password);
        }


        else
        {
            Intent intent=new Intent(CustomerLoginActivity.this,CustumerRegisterActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
