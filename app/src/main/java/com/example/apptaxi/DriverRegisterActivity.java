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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverRegisterActivity extends AppCompatActivity  implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private Button register_button;
    private EditText email_txt;
    private EditText password_txt;
    private TextView login_driver;
    private ProgressDialog loading;
    private String  driverId;
    private DatabaseReference driverDataBaseRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_register);
        email_txt=(EditText)findViewById(R.id.email_driver);
        password_txt=(EditText)findViewById(R.id.password_driver);
        register_button=(Button)findViewById(R.id.signin_driver);
        login_driver=(TextView) findViewById(R.id.login_driver);
        mAuth = FirebaseAuth.getInstance();
        loading=new ProgressDialog(this);
        register_button.setOnClickListener(this);
        login_driver.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v==register_button)
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

            registerUser(email,password);
        }
        if(v==login_driver)
        {
            Intent intent=new Intent(DriverRegisterActivity.this,DriverLoginActivity.class);
            startActivity(intent);
            finish();
        }


    }
    protected  void registerUser(String email,String password)
    {
        //FirebaseUser currentUser = mAuth.getCurrentUser();
        loading.setTitle("Driver Registration");
        loading.setMessage("Registartion......");
        loading.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext()," succesuful",Toast.LENGTH_SHORT).show();
                                loading.dismiss();
                                driverId = mAuth.getCurrentUser().getUid();
                                driverDataBaseRef= FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId);
                                driverDataBaseRef.setValue(true);
                                Intent intent=new Intent(DriverRegisterActivity.this,DriversMapsActivity.class);
                                startActivity(intent);
                                finish();
                        } else {
                            loading.dismiss();
                            Toast.makeText(getApplicationContext()," Unsuccesuful",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}