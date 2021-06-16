package com.example.apptaxi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(8000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {

                    Intent CustumorDriverIntent = new Intent(MainActivity.this,CustumerDriverActivity.class);
                    startActivity(CustumorDriverIntent);
                }
            }
        };
        thread.start();


    }


    @Override
    protected void onPause() {
        super.onPause();
        //pour ne pas revenir a l'interface si elle est fermer
        finish();
    }
}
