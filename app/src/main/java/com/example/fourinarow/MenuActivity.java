package com.example.fourinarow;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void buttonStartPress(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void buttonOptionsPress(View view){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void buttonExitPress(View view){
        this.finish();
        System.exit(0);
    }
}