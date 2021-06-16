package com.example.apptaxi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class CustumerDriverActivity extends AppCompatActivity implements View.OnClickListener {

    Button driver_btn,costumer_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custumer_driver);
        driver_btn=(Button)findViewById(R.id.driver);
        costumer_btn=(Button)findViewById(R.id.costumer);
        driver_btn.setOnClickListener(this);
        costumer_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v==driver_btn)
        {
            Intent intent=new Intent(CustumerDriverActivity.this,DriverRegisterActivity.class);
            startActivity(intent);
            finish();
        }
        if(v==costumer_btn)
        {
            Intent intent=new Intent(CustumerDriverActivity.this,CustumerRegisterActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
